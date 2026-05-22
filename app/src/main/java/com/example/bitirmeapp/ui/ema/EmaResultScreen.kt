package com.example.bitirmeapp.ui.ema

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun EmaResultScreen(
    entryId: Long,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var entry by remember { mutableStateOf<EmaEntryEntity?>(null) }
    val mobilMeta = remember { SonGonderimBilgisi.mobilMeta }
    val gonderimHatasi = remember { SonGonderimBilgisi.hata }

    LaunchedEffect(entryId) {
        val dao = AppDatabase.getInstance(context.applicationContext as Application).emaDao()
        entry = dao.getById(entryId)
    }

    Scaffold(containerColor = NabzBackground) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val current = entry
            if (current == null) {
                Spacer(modifier = Modifier.height(120.dp))
                CircularProgressIndicator(color = NabzPrimary)
            } else {
                val effectiveRisk = current.predictedRisk ?: current.riskLevel
                val info = riskInfo(effectiveRisk)
                val color = renkAdiniRengeMapla(current.finalRenk) ?: info.color
                val emoji = info.emoji
                val label = current.finalIsim?.uppercase() ?: info.label
                val advice = current.oneri ?: info.advice

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 60.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    textAlign = TextAlign.Center
                )

                if (!current.guvenilirlik.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = current.guvenilirlik!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PHQ-4 toplam: ${current.phq4Total}/12",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                if (gonderimHatasi != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = RiskHigh.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(
                            text = "⚠️ $gonderimHatasi",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RiskHigh
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "💡 Bugünkü Tavsiye",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NabzPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                val nedenler = current.top5Neden.orEmpty().filter { it.isNotBlank() }
                if (nedenler.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "🔍 Neden?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NabzPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            nedenler.forEach { neden ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = "•  ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NabzPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = neden,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                if (mobilMeta != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "📱 Telefondan ${mobilMeta.telefondanGelen} ölçüm, " +
                            "${mobilMeta.medyanlaDolduruldu} tahmini değerle tamamlandı",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NabzPrimary)
                ) {
                    Text(
                        text = "TAMAM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class RiskInfo(val color: Color, val emoji: String, val label: String, val advice: String)

private fun riskInfo(level: Int): RiskInfo = when (level) {
    0 -> RiskInfo(RiskGood, "🟢", "İYİ", "İyi gidiyorsun! Mevcut alışkanlıklarını sürdür.")
    1 -> RiskInfo(RiskMild, "🟡", "HAFİF RİSK", "Hafif gerginlik var. Yürüyüş ve erken uyku önerilir.")
    2 -> RiskInfo(RiskModerate, "🟠", "ORTA RİSK", "Orta düzeyde stres. Nefes egzersizleri yardımcı olabilir.")
    else -> RiskInfo(RiskHigh, "🔴", "YÜKSEK RİSK", "Yüksek risk. PDR birimiyle görüşmeni öneririz.")
}

private fun renkAdiniRengeMapla(ad: String?): Color? = when (ad?.lowercase()) {
    "yesil", "yeşil", "green" -> RiskGood
    "sari", "sarı", "yellow" -> RiskMild
    "turuncu", "orange" -> RiskModerate
    "kirmizi", "kırmızı", "red" -> RiskHigh
    else -> null
}
