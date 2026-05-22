package com.example.bitirmeapp.data.local

import androidx.room.TypeConverter
import com.example.bitirmeapp.data.remote.Cascade
import com.example.bitirmeapp.data.remote.Forecasting
import com.example.bitirmeapp.data.remote.MLBilgi
import com.example.bitirmeapp.data.remote.MobilMeta
import com.example.bitirmeapp.data.remote.PsikolojikAnaliz
import com.google.gson.Gson

// Room converter'ları. List<String> -> "|" ile birleşik string,
// Cascade/ML/analiz alanları -> Gson JSON.
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString(separator = "|")

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.split("|")?.filter { it.isNotBlank() }

    @TypeConverter
    fun fromCascade(c: Cascade?): String? =
        c?.let { gson.toJson(it) }

    @TypeConverter
    fun toCascade(s: String?): Cascade? =
        s?.let { gson.fromJson(it, Cascade::class.java) }

    @TypeConverter
    fun fromMLBilgi(m: MLBilgi?): String? =
        m?.let { gson.toJson(it) }

    @TypeConverter
    fun toMLBilgi(s: String?): MLBilgi? =
        s?.let { gson.fromJson(it, MLBilgi::class.java) }

    @TypeConverter
    fun fromForecasting(f: Forecasting?): String? =
        f?.let { gson.toJson(it) }

    @TypeConverter
    fun toForecasting(s: String?): Forecasting? =
        s?.let { gson.fromJson(it, Forecasting::class.java) }

    @TypeConverter
    fun fromMobilMeta(m: MobilMeta?): String? =
        m?.let { gson.toJson(it) }

    @TypeConverter
    fun toMobilMeta(s: String?): MobilMeta? =
        s?.let { gson.fromJson(it, MobilMeta::class.java) }

    @TypeConverter
    fun fromPsikolojikAnaliz(p: PsikolojikAnaliz?): String? =
        p?.let { gson.toJson(it) }

    @TypeConverter
    fun toPsikolojikAnaliz(s: String?): PsikolojikAnaliz? =
        s?.let { gson.fromJson(it, PsikolojikAnaliz::class.java) }
}
