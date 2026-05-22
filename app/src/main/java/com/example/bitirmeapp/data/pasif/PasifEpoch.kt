package com.example.bitirmeapp.data.pasif

import android.content.Context
import com.example.bitirmeapp.data.local.AppDatabase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Bir günün ham kayıtlarını backend'in beklediği {tip}_ep_{n} formatına çevirir.
// Epoch'lar (yerel saat): ep_1=00-09, ep_2=09-18, ep_3=18-24, ep_0=tüm gün.
// Toplama = SUM. Verisi olmayan feature hiç eklenmez (0 değil, yok).
object PasifEpoch {

    // Varyant tip isimlerini backend'in kullandığı isme çevir.
    private val TIP_NORMALIZE = mapOf(
        "act_on_bicycle" to "act_on_bike"
    )

    // Backend'in beklediği 30 feature. Bunun dışı gönderilmez.
    private val IZINLI = setOf(
        "unlock_num_ep_0", "unlock_num_ep_1", "unlock_num_ep_2", "unlock_num_ep_3",
        "unlock_duration_ep_0", "unlock_duration_ep_1", "unlock_duration_ep_2", "unlock_duration_ep_3",
        "act_still_ep_0", "act_still_ep_1", "act_still_ep_2", "act_still_ep_3",
        "act_walking_ep_0", "act_walking_ep_1", "act_walking_ep_2", "act_walking_ep_3",
        "act_on_foot_ep_0", "act_on_foot_ep_1", "act_on_foot_ep_2", "act_on_foot_ep_3",
        "act_running_ep_0",
        "act_on_bike_ep_0",
        "act_in_vehicle_ep_0",
        "loc_dist_ep_0", "loc_dist_ep_1", "loc_dist_ep_2", "loc_dist_ep_3",
        "loc_visit_num_ep_0",
        "other_playing_duration_ep_0",
        "other_playing_num_ep_0"
    )

    suspend fun pasifVeriOlustur(context: Context, gun: LocalDate): Map<String, Double> {
        val zone = ZoneId.systemDefault()
        val baslangic = gun.atStartOfDay(zone).toInstant().toEpochMilli()
        val bitis = gun.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val dao = AppDatabase.getInstance(context).hamPasifDao()
        val kayitlar = dao.araliktakiler(baslangic, bitis)

        val toplam = HashMap<String, Double>()
        for (k in kayitlar) {
            val tip = TIP_NORMALIZE[k.tip] ?: k.tip
            val saat = Instant.ofEpochMilli(k.timestamp).atZone(zone).hour
            val ep = when (saat) {
                in 0..8 -> 1
                in 9..17 -> 2
                else -> 3 // 18..23
            }
            ekle(toplam, "${tip}_ep_0", k.deger)
            ekle(toplam, "${tip}_ep_$ep", k.deger)
        }

        return toplam.filterKeys { it in IZINLI }
    }

    private fun ekle(m: HashMap<String, Double>, key: String, v: Double) {
        m[key] = (m[key] ?: 0.0) + v
    }

    // Son gunSayisi günün günlük feature listesi (eskiden yeniye). Telefonda
    // rolling/lag yok; backend bu listeden türetiyor. Boş gün atlanır.
    suspend fun pasifGunlukSeri(
        context: Context,
        bugun: LocalDate,
        gunSayisi: Int = 7
    ): List<Map<String, Any>> {
        val liste = mutableListOf<Map<String, Any>>()
        for (k in (gunSayisi - 1) downTo 0) {
            val g = bugun.minusDays(k.toLong())
            val feats = pasifVeriOlustur(context, g)
            if (feats.isNotEmpty()) {
                val m = HashMap<String, Any>()
                m["gun"] = g.toString()
                feats.forEach { (key, v) -> m[key] = v }
                liste.add(m)
            }
        }
        return liste
    }
}
