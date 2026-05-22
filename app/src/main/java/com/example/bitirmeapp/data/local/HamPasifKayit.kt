package com.example.bitirmeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tek bir ham pasif ölçüm. Burada sadece ham birikim tutulur; epoch'a bölme
// ve gönderme sonra yapılır.
// tip: unlock_num/unlock_duration, act_still/walking/running/..., loc_dist, loc_visit_num
// deger: adet, saniye veya metre.
@Entity(tableName = "ham_pasif_kayit")
data class HamPasifKayit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val tip: String,
    val deger: Double
)
