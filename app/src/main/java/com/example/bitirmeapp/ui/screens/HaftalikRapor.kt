package com.example.bitirmeapp.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.EmaEntryEntity
import com.example.bitirmeapp.ui.theme.*
import com.example.bitirmeapp.util.OturumYoneticisi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun HaftalikRapor(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context.applicationContext as Application) }
    val dao = remember { db.emaDao() }
    val kullaniciDao = remember { db.kullaniciDao() }

    // Aktif kullanıcının id'sini async bul, sonra ona göre flow'u filtrele.
    val userIdFlow = remember { MutableStateFlow<Long?>(null) }
    LaunchedEffect(Unit) {
        val email = OturumYoneticisi.aktifEmail(context)
        userIdFlow.value = email?.let { kullaniciDao.emailIleBul(it)?.id } ?: 0L
    }
    val entries by userIdFlow
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else dao.observeRecent(uid)
        }
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Haftalık Rapor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = NabzPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NabzBackground
                )
            )
        },
        containerColor = NabzBackground
    ) { paddingValues ->
        if (entries.isEmpty()) {
            EmptyState(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item { SummaryCard(entries) }
                item { ChartCard(entries) }
                item { DailyListCard(entries) }
                item { InsightCard(entries) }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📊", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Henüz yeterli veri yok",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NabzPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "EMA anketini birkaç gün doldurduktan sonra haftalık özetin burada görünecek.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryCard(entries: List<EmaEntryEntity>) {
    val avgRisk = entries.map { it.riskLevel }.average()
    val avgLabel = riskAverageLabel(avgRisk)
    val counts = IntArray(4)
    entries.forEach { counts[it.riskLevel.coerceIn(0, 3)]++ }

    SectionCard(title = "📊 Bu Hafta Özeti") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ortalama Risk",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "%.1f".format(avgRisk),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = NabzPrimary
                )
                Text(
                    text = avgLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${entries.size} kayıt",
                style = MaterialTheme.typography.labelLarge,
                color = NabzPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )

        RiskCountRow(label = "İyi", color = RiskGood, count = counts[0])
        RiskCountRow(label = "Hafif", color = RiskMild, count = counts[1])
        RiskCountRow(label = "Orta", color = RiskModerate, count = counts[2])
        RiskCountRow(label = "Yüksek", color = RiskHigh, count = counts[3])
    }
}

@Composable
private fun RiskCountRow(label: String, color: Color, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count gün",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ChartCard(entries: List<EmaEntryEntity>) {
    val ordered = entries.reversed()
    val maxHeightDp = 120
    val maxRisk = 3f

    SectionCard(title = "📈 Risk Trendi") {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((maxHeightDp + 36).dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ordered.forEach { entry ->
                val ratio = ((entry.riskLevel + 1) / (maxRisk + 1)).coerceIn(0.15f, 1f)
                val barColor = when (entry.riskLevel) {
                    0 -> RiskGood
                    1 -> RiskMild
                    2 -> RiskModerate
                    else -> RiskHigh
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height((maxHeightDp * ratio).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = shortDayLabel(entry.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyListCard(entries: List<EmaEntryEntity>) {
    SectionCard(title = "📅 Günlük Detay") {
        entries.forEach { entry ->
            DailyRow(entry)
        }
    }
}

@Composable
private fun DailyRow(entry: EmaEntryEntity) {
    val (color, emoji, label) = riskVisualsForList(entry.riskLevel)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatDayDate(entry.date),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "PHQ-4: ${entry.phq4Total}/12  •  Stres: ${entry.stress}/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InsightCard(entries: List<EmaEntryEntity>) {
    val avg = entries.map { it.riskLevel }.average()
    val maxEntry = entries.maxByOrNull { it.riskLevel }
    val insight = when {
        avg < 0.5 -> "Bu hafta gayet iyiydin. Mevcut alışkanlıklarını koru."
        avg < 1.5 -> "Bu hafta ortalama olarak hafif gerginlik yaşadın. Dinlenmeye zaman ayırmaya devam et."
        avg < 2.5 -> "Bu hafta orta düzeyde stres göstergesi yüksekti. Nefes ve uyku düzenine dikkat et."
        else -> "Bu hafta zorlu geçmiş görünüyor. Bir uzmandan destek almayı düşünmen önerilir."
    }
    val peak = maxEntry?.let { "En yüksek risk: ${formatDayDate(it.date)} (${riskShort(it.riskLevel)})" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NabzPrimary.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = NabzPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Haftalık İçgörü",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = NabzPrimary
                )
            }
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            peak?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NabzPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

private data class RiskListVisuals(val color: Color, val emoji: String, val label: String)

private fun riskVisualsForList(level: Int): RiskListVisuals = when (level) {
    0 -> RiskListVisuals(RiskGood, "🟢", "İYİ")
    1 -> RiskListVisuals(RiskMild, "🟡", "HAFİF")
    2 -> RiskListVisuals(RiskModerate, "🟠", "ORTA")
    else -> RiskListVisuals(RiskHigh, "🔴", "YÜKSEK")
}

private fun riskShort(level: Int): String = when (level) {
    0 -> "İyi"; 1 -> "Hafif"; 2 -> "Orta"; else -> "Yüksek"
}

private fun riskAverageLabel(avg: Double): String = when {
    avg < 0.5 -> "İyi"
    avg < 1.5 -> "Hafif"
    avg < 2.5 -> "Orta"
    else -> "Yüksek"
}

private fun shortDayLabel(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate)
        val day = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale("tr"))
        day
    } catch (e: Exception) {
        "?"
    }
}

private fun formatDayDate(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate)
        val fmt = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale("tr"))
        date.format(fmt).replaceFirstChar { it.uppercase(Locale("tr")) }
    } catch (e: Exception) {
        isoDate
    }
}
