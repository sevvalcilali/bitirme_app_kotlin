package com.example.bitirmeapp.data.pasif

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.HamPasifKayit
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ActivityRecognition güncellemelerini alır. Her seferinde önceki tespitle
// şimdi arasındaki süreyi (saniye) önceki aktivite tipine yazar.
class AktiviteAliciBR : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityRecognitionResult.hasResult(intent)) return
        val sonuc = ActivityRecognitionResult.extractResult(intent) ?: return
        val tipStr = aktiviteTipi(sonuc.mostProbableActivity.type) ?: return

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val simdi = System.currentTimeMillis()
        val oncekiTip = prefs.getString(KEY_TIP, null)
        val oncekiTs = prefs.getLong(KEY_TS, -1L)

        prefs.edit().putString(KEY_TIP, tipStr).putLong(KEY_TS, simdi).apply()

        if (oncekiTip == null || oncekiTs <= 0) return
        val gecenSn = (simdi - oncekiTs) / 1000.0
        if (gecenSn <= 0 || gecenSn > 7200) return // mantıksız aralıkları ele

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(context).hamPasifDao().ekle(
                    HamPasifKayit(timestamp = simdi, tip = oncekiTip, deger = gecenSn)
                )
            } finally {
                pending.finish()
            }
        }
    }

    private fun aktiviteTipi(type: Int): String? = when (type) {
        DetectedActivity.STILL -> "act_still"
        DetectedActivity.WALKING -> "act_walking"
        DetectedActivity.RUNNING -> "act_running"
        DetectedActivity.ON_BICYCLE -> "act_on_bike"
        DetectedActivity.IN_VEHICLE -> "act_in_vehicle"
        DetectedActivity.ON_FOOT -> "act_on_foot"
        else -> null
    }

    companion object {
        private const val PREFS = "pasif_aktivite_prefs"
        private const val KEY_TIP = "onceki_tip"
        private const val KEY_TS = "onceki_ts"
        const val ACTION = "com.example.bitirmeapp.AKTIVITE_GUNCELLEME"
    }
}
