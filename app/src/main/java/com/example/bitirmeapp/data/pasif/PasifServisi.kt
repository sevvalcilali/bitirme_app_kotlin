package com.example.bitirmeapp.data.pasif

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Saatlik ham pasif veri toplayan foreground service.
// Her saat usage + konum toplar; aktiviteyi 60 sn'de bir ister, sonuç
// AktiviteAliciBR üzerinden Room'a yazılır. İzin yoksa o kaynak atlanır.
class PasifServisi : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var baslatildi = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!baslatildi) {
            baslatildi = true
            foregroundBaslat()
            aktiviteAboneOl()
            saatlikDonguBaslat()
        }
        return START_STICKY
    }

    private fun foregroundBaslat() {
        val kanalId = "pasif_toplama"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(kanalId) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        kanalId,
                        "Pasif veri toplama",
                        NotificationManager.IMPORTANCE_MIN
                    )
                )
            }
        }
        val bildirim: Notification = NotificationCompat.Builder(this, kanalId)
            .setContentTitle("Nabz")
            .setContentText("Ruh hali analizi için arka planda veri toplanıyor")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var tip = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                if (PasifToplayici.konumIzniVar(this)) {
                    tip = tip or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                }
                startForeground(NOTIF_ID, bildirim, tip)
            } else {
                startForeground(NOTIF_ID, bildirim)
            }
        } catch (_: Exception) {
            // Bazı cihazlarda tip kısıtı çıkıyor; servisi düşürmemek için sade dene.
            try {
                startForeground(NOTIF_ID, bildirim)
            } catch (_: Exception) {
            }
        }
    }

    private fun aktiviteAboneOl() {
        val izinVar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        if (!izinVar) return

        val intent = Intent(this, AktiviteAliciBR::class.java).apply {
            action = AktiviteAliciBR.ACTION
        }
        val bayrak = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pi = PendingIntent.getBroadcast(this, 1001, intent, bayrak)
        try {
            @Suppress("MissingPermission")
            ActivityRecognition.getClient(this)
                .requestActivityUpdates(60_000L, pi)
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun saatlikDonguBaslat() {
        scope.launch {
            var sonCalisma = System.currentTimeMillis() - SAAT_MS
            while (isActive) {
                val simdi = System.currentTimeMillis()
                try {
                    PasifToplayici.usageTopla(applicationContext, sonCalisma, simdi)
                    PasifToplayici.konumTopla(applicationContext)
                } catch (_: Exception) {
                }
                sonCalisma = simdi
                delay(SAAT_MS)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 4242
        private const val SAAT_MS = 60L * 60L * 1000L

        fun baslat(context: Context) {
            val i = Intent(context, PasifServisi::class.java)
            ContextCompat.startForegroundService(context, i)
        }
    }
}
