package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Backend'in döndürdüğü tam cevap. cascade hep dolu; hibrit/pasif ML pasif
// veri yoksa null. final_* birleştirilmiş nihai karar.
data class TahminCevabi(
    @SerializedName("kullanici")    val kullanici: String? = null,
    @SerializedName("gun")          val gun: String? = null,
    @SerializedName("cascade")      val cascade: Cascade? = null,
    @SerializedName("hibrit_ml")    val hibritMl: MLBilgi? = null,
    @SerializedName("pasif_ml")     val pasifMl: MLBilgi? = null,
    @SerializedName("final_risk")   val finalRisk: Int,
    @SerializedName("final_isim")   val finalIsim: String,
    @SerializedName("final_renk")   val finalRenk: String? = null,
    @SerializedName("guvenilirlik") val guvenilirlik: String? = null,
    @SerializedName("aciklama")     val aciklama: String? = null,
    @SerializedName("oneri")        val oneri: String? = null,
    @SerializedName("top_5_neden")  val top5Neden: List<String> = emptyList(),
    @SerializedName("mobil_meta")        val mobilMeta: MobilMeta? = null,
    @SerializedName("forecasting")       val forecasting: Forecasting? = null,
    @SerializedName("psikolojik_analiz") val psikolojikAnaliz: PsikolojikAnaliz? = null
)
