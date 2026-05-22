package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Backend'e giden istek gövdesi. gun = yyyy-MM-dd, ema = 7 cevap.
data class TahminIstegi(
    @SerializedName("uid") val uid: String,
    @SerializedName("gun") val gun: String,
    @SerializedName("ema") val ema: EMAGirdi,
    // Sadece /predict/mobile için. null ise Gson atlar.
    @SerializedName("pasif") val pasif: Map<String, Double>? = null,
    // Son ~7 günün günlük feature serisi. Doluysa backend rolling/lag'i buradan
    // hesaplar, boşsa tek-gün moduna düşer.
    @SerializedName("pasif_gunluk") val pasifGunluk: List<Map<String, Any>>? = null
)
