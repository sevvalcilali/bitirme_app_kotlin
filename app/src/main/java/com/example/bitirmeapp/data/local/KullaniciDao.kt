package com.example.bitirmeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KullaniciDao {

    /** Email zaten varsa unique index nedeniyle eklenmez, -1 döner. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ekle(kullanici: KullaniciEntity): Long

    @Query("SELECT * FROM kullanicilar WHERE email = :email LIMIT 1")
    suspend fun emailIleBul(email: String): KullaniciEntity?

    @Query("SELECT COUNT(*) FROM kullanicilar")
    suspend fun sayisi(): Int
}
