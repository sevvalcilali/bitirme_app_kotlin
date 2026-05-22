package com.example.bitirmeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bitirmeapp.data.remote.Cascade
import com.example.bitirmeapp.data.remote.Forecasting
import com.example.bitirmeapp.data.remote.MLBilgi
import com.example.bitirmeapp.data.remote.MobilMeta
import com.example.bitirmeapp.data.remote.PsikolojikAnaliz

@Entity(tableName = "ema_entries")
data class EmaEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val date: String,
    val stress: Int,
    val pam: Int,
    val socialLevel: Int,
    val phq4Q1: Int,
    val phq4Q2: Int,
    val phq4Q3: Int,
    val phq4Q4: Int,
    val phq4Total: Int,
    val riskLevel: Int,
    val createdAt: Long,

    // ML model tahmini — API çağrısı başarısız olursa null kalır
    val predictedRisk: Int? = null,
    val finalIsim: String? = null,
    val finalRenk: String? = null,
    val guvenilirlik: String? = null,
    val top5Neden: List<String>? = null,
    val aciklama: String? = null,
    val oneri: String? = null,
    val cascade: Cascade? = null,
    val hibritMl: MLBilgi? = null,
    val pasifMl: MLBilgi? = null,
    val forecasting: Forecasting? = null,
    val mobilMeta: MobilMeta? = null,
    val psikolojikAnaliz: PsikolojikAnaliz? = null
)
