package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

data class PsikolojikAnaliz(
    @SerializedName("ema_yorum")       val emaYorum: String? = null,
    @SerializedName("pasif_yorum")     val pasifYorum: String? = null,
    @SerializedName("birlesik_yorum")  val birlesikYorum: String? = null,
    @SerializedName("guvenilirlik")    val guvenilirlik: String? = null,
    @SerializedName("oneri")           val oneri: String? = null,
    @SerializedName("metin_butun")     val metinButun: String? = null,
    @SerializedName("renk")            val renk: String? = null,
    @SerializedName("phq4_duzey")      val phq4Duzey: String? = null,
    @SerializedName("pasif_duzey")     val pasifDuzey: String? = null,
    @SerializedName("dayanak")         val dayanak: String? = null
)
