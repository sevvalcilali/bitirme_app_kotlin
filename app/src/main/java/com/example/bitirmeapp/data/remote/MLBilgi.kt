package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Hibrit veya pasif ML modelin tahmini. Pasif veri yoksa null gelir.
data class MLBilgi(
    @SerializedName("risk_binary")     val riskBinary: Int,
    @SerializedName("olasilik")        val olasilik: Double,
    @SerializedName("kullanilan_esik") val kullanilanEsik: Double,
    @SerializedName("feature_count")   val featureCount: Int
)
