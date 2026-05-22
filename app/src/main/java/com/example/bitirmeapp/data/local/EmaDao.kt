package com.example.bitirmeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bitirmeapp.data.remote.Cascade
import com.example.bitirmeapp.data.remote.Forecasting
import com.example.bitirmeapp.data.remote.MLBilgi
import com.example.bitirmeapp.data.remote.MobilMeta
import com.example.bitirmeapp.data.remote.PsikolojikAnaliz
import kotlinx.coroutines.flow.Flow

@Dao
interface EmaDao {
    @Insert
    suspend fun insert(entry: EmaEntryEntity): Long

    @Query("SELECT * FROM ema_entries WHERE id = :id")
    suspend fun getById(id: Long): EmaEntryEntity?

    /** Tahmin başarısız olunca yarım kalan kaydı siler — dilim "harcanmaz". */
    @Query("DELETE FROM ema_entries WHERE id = :id")
    suspend fun silById(id: Long)

    @Query("SELECT * FROM ema_entries WHERE userId = :userId ORDER BY createdAt DESC LIMIT 7")
    fun observeRecent(userId: Long): Flow<List<EmaEntryEntity>>

    @Query("SELECT COUNT(*) FROM ema_entries WHERE userId = :userId AND date = :date")
    suspend fun countForDate(userId: Long, date: String): Int

    @Query("SELECT * FROM ema_entries WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    suspend fun getForDate(userId: Long, date: String): List<EmaEntryEntity>

    @Query("""
        UPDATE ema_entries
        SET predictedRisk = :predictedRisk,
            finalIsim = :finalIsim,
            finalRenk = :finalRenk,
            guvenilirlik = :guvenilirlik,
            top5Neden = :top5Neden,
            aciklama = :aciklama,
            oneri = :oneri,
            cascade = :cascade,
            hibritMl = :hibritMl,
            pasifMl = :pasifMl,
            forecasting = :forecasting,
            mobilMeta = :mobilMeta,
            psikolojikAnaliz = :psikolojikAnaliz
        WHERE id = :id
    """)
    suspend fun tahminGuncelle(
        id: Long,
        predictedRisk: Int?,
        finalIsim: String?,
        finalRenk: String?,
        guvenilirlik: String?,
        top5Neden: List<String>?,
        aciklama: String?,
        oneri: String?,
        cascade: Cascade?,
        hibritMl: MLBilgi?,
        pasifMl: MLBilgi?,
        forecasting: Forecasting?,
        mobilMeta: MobilMeta?,
        psikolojikAnaliz: PsikolojikAnaliz?
    )
}
