package com.example.bitirmeapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kullanicilar",
    indices = [Index(value = ["email"], unique = true)]
)
data class KullaniciEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ad: String,
    val email: String,
    val sifreHash: String,
    val yas: Int? = null,
    val cinsiyet: String? = null,
    val onayTarihi: Long? = null
)
