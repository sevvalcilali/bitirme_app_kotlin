package com.example.bitirmeapp.data.remote

import com.google.gson.annotations.SerializedName

// Pasif veri özeti: kaç feature telefondan geldi, kaçı medyanla dolduruldu.
data class MobilMeta(
    @SerializedName("telefondan_gelen_feature") val telefondanGelen: Int,
    @SerializedName("medyanla_doldurulan_feature") val medyanlaDolduruldu: Int,
    @SerializedName("toplam_pasif_feature") val toplamPasif: Int,
    /** "7-gunluk" veya "tek-gun" — backend hangi modda çalıştı. */
    @SerializedName("mod") val mod: String? = null,
    /** pasif_gunluk serisinde kaç gün veri vardı. */
    @SerializedName("pasif_gun_sayisi") val pasifGunSayisi: Int? = null
)
