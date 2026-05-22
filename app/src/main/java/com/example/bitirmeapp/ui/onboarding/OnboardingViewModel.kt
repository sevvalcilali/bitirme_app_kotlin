package com.example.bitirmeapp.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.KullaniciEntity
import com.example.bitirmeapp.util.OturumYoneticisi
import com.example.bitirmeapp.util.SifreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Onboarding form state'i, 6 ekran paylaşır. Son ekranda kaydet() çağrılır.
data class OnboardingState(
    val adSoyad: String = "",
    val email: String = "",
    val sifre: String = "",
    val yas: String = "",
    val cinsiyet: String = "Belirtmek istemiyorum",
    val onayli: Boolean = false,
    val isLoading: Boolean = false,
    val hata: String? = null,
    val basariliMi: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).kullaniciDao()

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setAdSoyad(v: String) = _state.update { it.copy(adSoyad = v, hata = null) }
    fun setEmail(v: String) = _state.update { it.copy(email = v, hata = null) }
    fun setSifre(v: String) = _state.update { it.copy(sifre = v, hata = null) }
    fun setYas(v: String) = _state.update {
        it.copy(yas = v.filter { c -> c.isDigit() }.take(2), hata = null)
    }
    fun setCinsiyet(v: String) = _state.update { it.copy(cinsiyet = v) }
    fun setOnayli(v: Boolean) = _state.update { it.copy(onayli = v, hata = null) }

    /** Son ekranda GÖNDER → Room'a kullanıcı yaz + oturum aç. */
    fun kaydet() {
        val s = _state.value
        val ad = s.adSoyad.trim()
        val email = s.email.trim().lowercase()
        val yasInt = s.yas.toIntOrNull() ?: 0

        if (ad.isBlank()) { hataVer("Lütfen ad soyad girin."); return }
        if (!emailGecerli(email)) { hataVer("Geçerli bir e-posta girin."); return }
        if (s.sifre.length < 6) { hataVer("Şifre en az 6 karakter olmalı."); return }
        if (yasInt < 18 || yasInt > 99) { hataVer("Yaş 18–99 arasında olmalı."); return }
        if (!s.onayli) { hataVer("Aydınlatmayı onaylayın."); return }

        _state.update { it.copy(isLoading = true, hata = null) }
        viewModelScope.launch {
            if (dao.emailIleBul(email) != null) {
                _state.update { it.copy(isLoading = false, hata = "Bu e-posta zaten kayıtlı.") }
                return@launch
            }
            val satir = dao.ekle(
                KullaniciEntity(
                    ad = ad,
                    email = email,
                    sifreHash = SifreUtil.hashle(s.sifre),
                    yas = yasInt,
                    cinsiyet = s.cinsiyet,
                    onayTarihi = System.currentTimeMillis()
                )
            )
            if (satir == -1L) {
                _state.update { it.copy(isLoading = false, hata = "Bu e-posta zaten kayıtlı.") }
            } else {
                OturumYoneticisi.girisYap(getApplication(), email)
                _state.update { it.copy(isLoading = false, basariliMi = true) }
            }
        }
    }

    fun tuket() = _state.update { it.copy(basariliMi = false, hata = null) }

    private fun hataVer(m: String) = _state.update { it.copy(hata = m) }

    private fun emailGecerli(e: String) =
        e.contains("@") && e.substringAfterLast("@").contains(".")
}
