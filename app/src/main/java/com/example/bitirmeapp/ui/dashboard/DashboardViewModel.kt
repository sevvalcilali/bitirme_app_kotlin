package com.example.bitirmeapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.EmaDao
import com.example.bitirmeapp.data.local.EmaEntryEntity
import com.example.bitirmeapp.data.local.KullaniciDao
import com.example.bitirmeapp.util.OturumYoneticisi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class TodayStatus {
    object NotCompleted : TodayStatus()
    data class Completed(
        val riskLevel: Int,
        val riskLabel: String,
        val riskPercentage: Int,
        val timestamp: String,
        val recommendation: String,
        val emaEntryId: Long
    ) : TodayStatus()
}

data class DayStatus(
    val dayLabel: String,
    val riskLevel: Int?
)

data class DashboardUIState(
    val userName: String = "Kullanıcı",
    val todayDate: String = "",
    val todayStatus: TodayStatus = TodayStatus.NotCompleted,
    val last7Days: List<DayStatus> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: EmaDao = AppDatabase.getInstance(application).emaDao()
    private val kullaniciDao: KullaniciDao = AppDatabase.getInstance(application).kullaniciDao()

    private val _uiState = MutableStateFlow(DashboardUIState())
    val uiState: StateFlow<DashboardUIState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    // Dashboard'u Room'daki verilerden doldurur: bugünkü durum + son 7 gün özeti.
    fun loadDashboard() {
        viewModelScope.launch {
            val locale = Locale("tr")
            val dateFormatter = DateTimeFormatter.ofPattern("d MMMM EEEE", locale)
            val today = LocalDate.now()
            val todayIso = today.toString()
            val formattedDate = today.format(dateFormatter)

            // Aktif kullanıcı; veriler bunun id'siyle filtrelenir.
            val aktifKullanici = OturumYoneticisi.aktifEmail(getApplication())
                ?.let { kullaniciDao.emailIleBul(it) }
            val aktifUserId = aktifKullanici?.id ?: 0L
            val aktifAd = aktifKullanici?.ad
                ?.trim()
                ?.split(" ")
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "Kullanıcı"

            val todayEntries = dao.getForDate(aktifUserId, todayIso)
            val latest = todayEntries.firstOrNull()
            val last7Entries = dao.observeRecent(aktifUserId).first()

            val status: TodayStatus = if (latest == null) {
                TodayStatus.NotCompleted
            } else {
                val effectiveRisk = latest.predictedRisk ?: latest.riskLevel
                TodayStatus.Completed(
                    riskLevel = effectiveRisk,
                    riskLabel = latest.finalIsim?.uppercase()
                        ?: defaultRiskLabel(effectiveRisk),
                    riskPercentage = riskYuzdesi(effectiveRisk),
                    timestamp = formatTimestamp(latest.createdAt),
                    recommendation = latest.oneri ?: defaultAdvice(effectiveRisk),
                    emaEntryId = latest.id
                )
            }

            _uiState.value = DashboardUIState(
                userName = aktifAd,
                todayDate = formattedDate,
                todayStatus = status,
                last7Days = haftalikOzet(last7Entries, today)
            )
        }
    }

    // Son 7 günü Pzt..Bug. sırasıyla döner; veri olmayan gün null risk.
    private fun haftalikOzet(entries: List<EmaEntryEntity>, today: LocalDate): List<DayStatus> {
        // Gün -> en güncel risk seviyesi.
        val byDay: Map<String, Int> = entries
            .groupBy { it.date }
            .mapValues { (_, list) ->
                val newest = list.maxByOrNull { it.createdAt }
                newest?.predictedRisk ?: newest?.riskLevel ?: 0
            }
        val labels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        // 6 gün öncesinden bugüne.
        return (6 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val isoKey = day.toString()
            val isToday = (offset == 0)
            DayStatus(
                dayLabel = if (isToday) "Bug." else labels[day.dayOfWeek.value - 1].take(1),
                riskLevel = byDay[isoKey]
            )
        }
    }

    private fun defaultRiskLabel(level: Int): String = when (level) {
        0 -> "İYİ"
        1 -> "HAFİF RİSK"
        2 -> "ORTA RİSK"
        else -> "YÜKSEK RİSK"
    }

    private fun riskYuzdesi(level: Int): Int = when (level) {
        0 -> 12
        1 -> 38
        2 -> 63
        else -> 88
    }

    private fun defaultAdvice(level: Int): String = when (level) {
        0 -> "İyi gidiyorsun! Mevcut alışkanlıklarını sürdür."
        1 -> "Hafif gerginlik var. Yürüyüş ve erken uyku önerilir."
        2 -> "Orta düzeyde stres. Nefes egzersizleri yardımcı olabilir."
        else -> "Yüksek risk. PDR birimiyle görüşmeni öneririz."
    }

    private fun formatTimestamp(epochMs: Long): String {
        val time = LocalTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
        return time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
