package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// EMA'nın 7 cevabı. Alan adları backend'in beklediği JSON anahtarlarıyla aynı.
data class EMAGirdi(
    @SerializedName("stress")       val stres: Int,
    @SerializedName("pam_score")    val pamSkoru: Int,
    @SerializedName("social_level") val sosyalSeviye: Int,
    @SerializedName("phq4_q1")      val phq4Q1: Int,
    @SerializedName("phq4_q2")      val phq4Q2: Int,
    @SerializedName("phq4_q3")      val phq4Q3: Int,
    @SerializedName("phq4_q4")      val phq4Q4: Int
)
