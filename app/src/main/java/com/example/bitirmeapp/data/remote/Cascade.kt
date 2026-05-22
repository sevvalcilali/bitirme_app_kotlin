package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Kural-tabanlı cascade modelin tahmini (cevaptaki "cascade" alanı).
data class Cascade(
    @SerializedName("risk_sinifi")  val riskSinifi: Int,
    @SerializedName("profil_id")    val profilId: String,
    @SerializedName("profil_isim")  val profilIsim: String,
    @SerializedName("phq4_total")   val phq4Total: Int,
    @SerializedName("pam_quadrant") val pamQuadrant: String
)
