package com.example.bitirmeapp.ui.ema

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.EmaDao
import com.example.bitirmeapp.data.local.EmaEntryEntity
import com.example.bitirmeapp.data.local.KullaniciDao
import com.example.bitirmeapp.data.pasif.PasifEpoch
import com.example.bitirmeapp.data.remote.EMAGirdi
import com.example.bitirmeapp.data.remote.RetrofitClient
import com.example.bitirmeapp.data.remote.TahminCevabi
import com.example.bitirmeapp.data.remote.TahminIstegi
import com.example.bitirmeapp.util.OturumYoneticisi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EmaState(
    val stress: Int? = null,
    val pam: Int? = null,
    val socialLevel: Int? = null,
    val phq4Q1: Int? = null,
    val phq4Q2: Int? = null,
    val phq4Q3: Int? = null,
    val phq4Q4: Int? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val savedEntryId: Long? = null,
    val tahminHatasi: String? = null
)

class EmaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: EmaDao = AppDatabase.getInstance(application).emaDao()
    private val kullaniciDao: KullaniciDao = AppDatabase.getInstance(application).kullaniciDao()

    private val _state = MutableStateFlow(EmaState())
    val state: StateFlow<EmaState> = _state.asStateFlow()

    fun updateStress(value: Int) = _state.update { it.copy(stress = value) }
    fun updatePam(value: Int) = _state.update { it.copy(pam = value) }
    fun updateSocialLevel(value: Int) = _state.update { it.copy(socialLevel = value) }
    fun updatePhq4Q1(value: Int) = _state.update { it.copy(phq4Q1 = value) }
    fun updatePhq4Q2(value: Int) = _state.update { it.copy(phq4Q2 = value) }
    fun updatePhq4Q3(value: Int) = _state.update { it.copy(phq4Q3 = value) }
    fun updatePhq4Q4(value: Int) = _state.update { it.copy(phq4Q4 = value) }

    // BİTİR: cevapları Room'a kaydet, modele gönder, dönen tahmini güncelle.
    // API başarısızsa yerel risk skoru fallback.
    fun completeAndSave() {
        val s = _state.value
        val stress = s.stress ?: return
        val pam = s.pam ?: return
        val social = s.socialLevel ?: return
        val q1 = s.phq4Q1 ?: return
        val q2 = s.phq4Q2 ?: return
        val q3 = s.phq4Q3 ?: return
        val q4 = s.phq4Q4 ?: return

        val phq4Total = q1 + q2 + q3 + q4
        val fallbackRisk = yerelRiskHesapla(phq4Total)
        val gun = LocalDate.now().toString()

        _state.update { it.copy(isSaving = true, tahminHatasi = null) }
        SonGonderimBilgisi.temizle()

        viewModelScope.launch {
            // Kayıt aktif kullanıcıya bağlansın ki hesaplar birbirini görmesin.
            val aktifEmail = OturumYoneticisi.aktifEmail(getApplication())
            val aktifUserId = aktifEmail?.let { kullaniciDao.emailIleBul(it)?.id } ?: 0L

            // Önce Room'a kaydet, tahmin alanları null.
            val entry = EmaEntryEntity(
                userId = aktifUserId,
                date = gun,
                stress = stress,
                pam = pam,
                socialLevel = social,
                phq4Q1 = q1,
                phq4Q2 = q2,
                phq4Q3 = q3,
                phq4Q4 = q4,
                phq4Total = phq4Total,
                riskLevel = fallbackRisk,
                createdAt = System.currentTimeMillis()
            )
            val newId = dao.insert(entry)

            // Pasif veriyi epoch formatında üret.
            val pasifMap: Map<String, Double> = try {
                PasifEpoch.pasifVeriOlustur(getApplication(), LocalDate.now())
            } catch (_: Exception) {
                emptyMap()
            }

            // Son 7 günün serisi; backend rolling/lag'i bundan üretiyor.
            val pasifGunlukSeri: List<Map<String, Any>> = try {
                PasifEpoch.pasifGunlukSeri(getApplication(), LocalDate.now())
            } catch (_: Exception) {
                emptyList()
            }

            val uid = OturumYoneticisi.aktifEmail(getApplication())
                ?: "telefon_kullanici_001"
            val tahmin = mobilTahminAl(
                uid = uid,
                gun = gun,
                ema = EMAGirdi(
                    stres = stress,
                    pamSkoru = pam,
                    sosyalSeviye = social,
                    phq4Q1 = q1,
                    phq4Q2 = q2,
                    phq4Q3 = q3,
                    phq4Q4 = q4
                ),
                pasif = pasifMap,
                pasifGunluk = pasifGunlukSeri
            )

            // Tahmin başarısızsa yarım kaydı sil (dilim harcanmasın), hata göster,
            // sonuç ekranına geçme.
            if (tahmin == null) {
                dao.silById(newId)
                SonGonderimBilgisi.hata = null
                _state.update {
                    it.copy(
                        isSaving = false,
                        isComplete = false,
                        savedEntryId = null,
                        tahminHatasi = "Sunucuya ulaşılamadı. Bağlantını kontrol edip tekrar dene."
                    )
                }
                return@launch
            }

            // Tahmin geldi, Room'u güncelle.
            dao.tahminGuncelle(
                id = newId,
                predictedRisk = tahmin.finalRisk,
                finalIsim = tahmin.finalIsim,
                finalRenk = tahmin.finalRenk,
                guvenilirlik = tahmin.guvenilirlik,
                top5Neden = tahmin.top5Neden,
                aciklama = tahmin.aciklama,
                oneri = tahmin.oneri,
                cascade = tahmin.cascade,
                hibritMl = tahmin.hibritMl,
                pasifMl = tahmin.pasifMl,
                forecasting = tahmin.forecasting,
                mobilMeta = tahmin.mobilMeta,
                psikolojikAnaliz = tahmin.psikolojikAnaliz
            )
            SonGonderimBilgisi.mobilMeta = tahmin.mobilMeta
            SonGonderimBilgisi.hata = null

            _state.update {
                it.copy(
                    isSaving = false,
                    isComplete = true,
                    savedEntryId = newId
                )
            }
        }
    }

    // EMA + pasif veriyi /predict/mobile'a gönderir, hatada null döner.
    private suspend fun mobilTahminAl(
        uid: String,
        gun: String,
        ema: EMAGirdi,
        pasif: Map<String, Double>,
        pasifGunluk: List<Map<String, Any>>
    ): TahminCevabi? {
        return try {
            RetrofitClient.api.mobilTahmin(
                istek = TahminIstegi(
                    uid = uid,
                    gun = gun,
                    ema = ema,
                    pasif = pasif,
                    pasifGunluk = pasifGunluk.ifEmpty { null }
                )
            )
        } catch (e: Exception) {
            _state.update { it.copy(tahminHatasi = e.message ?: "Tahmin alınamadı") }
            null
        }
    }

    fun reset() {
        _state.value = EmaState()
    }

    /** Kural-tabanlı yerel risk skoru — API başarısız olursa fallback olarak kullanılır. */
    private fun yerelRiskHesapla(phq4Total: Int): Int = when {
        phq4Total >= 9 -> 3
        phq4Total >= 6 -> 2
        phq4Total >= 3 -> 1
        else -> 0
    }
}
