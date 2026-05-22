package com.example.bitirmeapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.util.OturumYoneticisi
import com.example.bitirmeapp.util.SifreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KimlikState(
    val isLoading: Boolean = false,
    val hata: String? = null,
    val girisBasarili: Boolean = false
)

class KimlikViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).kullaniciDao()

    private val _state = MutableStateFlow(KimlikState())
    val state: StateFlow<KimlikState> = _state.asStateFlow()

    fun girisYap(email: String, sifre: String) {
        val emailT = email.trim().lowercase()
        if (emailT.isBlank() || sifre.isBlank()) {
            hataVer("E-posta ve şifre boş olamaz."); return
        }
        _state.update { it.copy(isLoading = true, hata = null) }
        viewModelScope.launch {
            val k = dao.emailIleBul(emailT)
            if (k == null || k.sifreHash != SifreUtil.hashle(sifre)) {
                _state.update { it.copy(isLoading = false, hata = "E-posta veya şifre hatalı.") }
            } else {
                OturumYoneticisi.girisYap(getApplication(), emailT)
                _state.update { it.copy(isLoading = false, girisBasarili = true) }
            }
        }
    }

    // Navigasyon tüketildikten sonra bayrağı/hatayı sıfırlar.
    fun tuket() {
        _state.update { it.copy(girisBasarili = false, hata = null) }
    }

    private fun hataVer(mesaj: String) = _state.update { it.copy(hata = mesaj) }
}
