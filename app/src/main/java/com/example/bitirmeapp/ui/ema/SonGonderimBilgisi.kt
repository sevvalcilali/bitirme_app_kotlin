package com.example.bitirmeapp.ui.ema

import com.example.bitirmeapp.data.remote.MobilMeta

// Son gönderimin geçici bilgisi (mobil_meta + hata). Sonuç ekranında
// gösterilir, her gönderimde üzerine yazılır. Kalıcı alanlar Room'da.
object SonGonderimBilgisi {
    @Volatile var mobilMeta: MobilMeta? = null
    @Volatile var hata: String? = null

    fun temizle() {
        mobilMeta = null
        hata = null
    }
}
