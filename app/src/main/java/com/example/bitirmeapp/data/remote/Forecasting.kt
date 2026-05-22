package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Bugünkü EMA + pasif veriden yarınki riski tahmin eden ana model.
data class Forecasting(
    @SerializedName("risk_binary")     val riskBinary: Int,
    @SerializedName("olasilik")        val olasilik: Double,
    @SerializedName("kullanilan_esik") val kullanilanEsik: Double,
    @SerializedName("feature_count")   val featureCount: Int,
    @SerializedName("auc")             val auc: Double = 0.0,
    @SerializedName("hedef")           val hedef: String = "yarin"
)
