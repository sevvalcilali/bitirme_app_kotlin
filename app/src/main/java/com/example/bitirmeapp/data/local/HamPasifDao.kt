package com.example.bitirmeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HamPasifDao {

    @Insert
    suspend fun ekle(kayit: HamPasifKayit): Long

    /** Belirli tipteki en son kayıt (konum mesafe/durak hesabı için). */
    @Query("SELECT * FROM ham_pasif_kayit WHERE tip = :tip ORDER BY timestamp DESC LIMIT 1")
    suspend fun sonKayit(tip: String): HamPasifKayit?

    /** Bir zaman aralığındaki tüm kayıtlar — epoch'a bölerken kullanılır. */
    @Query("SELECT * FROM ham_pasif_kayit WHERE timestamp BETWEEN :baslangic AND :bitis ORDER BY timestamp ASC")
    suspend fun araliktakiler(baslangic: Long, bitis: Long): List<HamPasifKayit>

    @Query("SELECT COUNT(*) FROM ham_pasif_kayit")
    suspend fun toplamSayi(): Int

    @Query("DELETE FROM ham_pasif_kayit WHERE id IN (:idler)")
    suspend fun sil(idler: List<Long>)
}
