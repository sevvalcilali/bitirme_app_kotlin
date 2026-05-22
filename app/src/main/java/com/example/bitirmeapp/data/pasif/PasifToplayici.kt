package com.example.bitirmeapp.data.pasif

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.HamPasifKayit
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Ham pasif veriyi okur ve Room'a yazar. Epoch/JSON/gönderme burada yok.
// İzin yoksa o kaynak atlanır, çağıran çökmez.
object PasifToplayici {

    private const val PREFS = "pasif_prefs"
    private const val KEY_LAST_LAT = "son_lat"
    private const val KEY_LAST_LON = "son_lon"
    private const val KEY_STOP_LAT = "durak_lat"
    private const val KEY_STOP_LON = "durak_lon"
    private const val DURAK_ESIK_M = 100f

    // --- İzin kontrolleri ---

    fun kullanimIzniVar(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun konumIzniVar(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    // --- Telefon kullanımı (UsageStatsManager) ---

    suspend fun usageTopla(context: Context, baslangic: Long, bitis: Long) {
        if (!kullanimIzniVar(context)) return
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return
        val dao = AppDatabase.getInstance(context).hamPasifDao()

        var unlockNum = 0
        var ekranAcikSn = 0.0
        var ekranAcikBasi = -1L

        var medyaAcma = 0
        var medyaSureSn = 0.0
        val medyaFgBasi = HashMap<String, Long>()

        val events = usm.queryEvents(baslangic, bitis)
        val e = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            when (e.eventType) {
                UsageEvents.Event.KEYGUARD_HIDDEN -> unlockNum++

                UsageEvents.Event.SCREEN_INTERACTIVE -> ekranAcikBasi = e.timeStamp

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (ekranAcikBasi > 0) {
                        ekranAcikSn += (e.timeStamp - ekranAcikBasi) / 1000.0
                        ekranAcikBasi = -1L
                    }
                }

                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (medyaUygulamasiMi(context, e.packageName)) {
                        medyaAcma++
                        medyaFgBasi[e.packageName] = e.timeStamp
                    }
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val basi = medyaFgBasi.remove(e.packageName)
                    if (basi != null) {
                        medyaSureSn += (e.timeStamp - basi) / 1000.0
                    }
                }
            }
        }

        val ts = bitis
        if (unlockNum > 0) dao.ekle(HamPasifKayit(timestamp = ts, tip = "unlock_num", deger = unlockNum.toDouble()))
        if (ekranAcikSn > 0) dao.ekle(HamPasifKayit(timestamp = ts, tip = "unlock_duration", deger = ekranAcikSn))
        if (medyaAcma > 0) dao.ekle(HamPasifKayit(timestamp = ts, tip = "other_playing_num", deger = medyaAcma.toDouble()))
        if (medyaSureSn > 0) dao.ekle(HamPasifKayit(timestamp = ts, tip = "other_playing_duration", deger = medyaSureSn))
    }

    private val MEDYA_PAKET = setOf(
        "com.spotify.music",
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient",
        "deezer.android.app",
        "tv.twitch.android.app"
    )

    private fun medyaUygulamasiMi(context: Context, paket: String?): Boolean {
        if (paket == null) return false
        if (paket in MEDYA_PAKET) return true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            val info: ApplicationInfo =
                context.packageManager.getApplicationInfo(paket, 0)
            info.category == ApplicationInfo.CATEGORY_AUDIO ||
                info.category == ApplicationInfo.CATEGORY_VIDEO
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    // --- Konum (FusedLocationProviderClient) ---

    suspend fun konumTopla(context: Context) {
        if (!konumIzniVar(context)) return
        val fused = LocationServices.getFusedLocationProviderClient(context)
        val konum: Location = try {
            suspendCancellableCoroutine { cont ->
                @Suppress("MissingPermission")
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            } ?: return
        } catch (_: SecurityException) {
            return
        } catch (_: Exception) {
            return
        }

        val dao = AppDatabase.getInstance(context).hamPasifDao()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ts = System.currentTimeMillis()

        val sonLat = prefs.getString(KEY_LAST_LAT, null)?.toDoubleOrNull()
        val sonLon = prefs.getString(KEY_LAST_LON, null)?.toDoubleOrNull()
        if (sonLat != null && sonLon != null) {
            val sonuc = FloatArray(1)
            Location.distanceBetween(sonLat, sonLon, konum.latitude, konum.longitude, sonuc)
            dao.ekle(HamPasifKayit(timestamp = ts, tip = "loc_dist", deger = sonuc[0].toDouble()))
        }

        val durakLat = prefs.getString(KEY_STOP_LAT, null)?.toDoubleOrNull()
        val durakLon = prefs.getString(KEY_STOP_LON, null)?.toDoubleOrNull()
        val yeniDurak: Boolean = if (durakLat == null || durakLon == null) {
            true
        } else {
            val s = FloatArray(1)
            Location.distanceBetween(durakLat, durakLon, konum.latitude, konum.longitude, s)
            s[0] >= DURAK_ESIK_M
        }
        if (yeniDurak) {
            dao.ekle(HamPasifKayit(timestamp = ts, tip = "loc_visit_num", deger = 1.0))
            prefs.edit()
                .putString(KEY_STOP_LAT, konum.latitude.toString())
                .putString(KEY_STOP_LON, konum.longitude.toString())
                .apply()
        }

        prefs.edit()
            .putString(KEY_LAST_LAT, konum.latitude.toString())
            .putString(KEY_LAST_LON, konum.longitude.toString())
            .apply()
    }

    // --- Geçmiş UsageStats backfill (ilk kurulumda tek sefer) ---

    // Son gunSayisi günün kullanımını UsageStats geçmişinden Room'a doldurur.
    // Kayıt günün sonuna damgalanır. Sadece usage; aktivite/konum geçmişi yok.
    suspend fun gecmisUsageBackfill(context: Context, gunSayisi: Int = 7) {
        if (!kullanimIzniVar(context)) return
        val zone = java.time.ZoneId.systemDefault()
        val bugun = java.time.LocalDate.now()
        for (k in gunSayisi downTo 1) {
            val gun = bugun.minusDays(k.toLong())
            val bas = gun.atStartOfDay(zone).toInstant().toEpochMilli()
            val bit = gun.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            usageTopla(context, bas, bit)
        }
    }
}
