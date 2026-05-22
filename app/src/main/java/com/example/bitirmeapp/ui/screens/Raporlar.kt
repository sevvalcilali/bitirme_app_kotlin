package com.example.bitirmeapp.ui.screens

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.data.local.EmaEntryEntity
import com.example.bitirmeapp.ui.theme.NabzBackground
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.RiskGood
import com.example.bitirmeapp.ui.theme.RiskHigh
import com.example.bitirmeapp.ui.theme.RiskMild
import com.example.bitirmeapp.ui.theme.RiskModerate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Raporlar(
    entryId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var entry by remember { mutableStateOf<EmaEntryEntity?>(null) }

    LaunchedEffect(entryId) {
        val dao = AppDatabase.getInstance(context.applicationContext as Application).emaDao()
        entry = dao.getById(entryId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detaylı Rapor", fontWeight = FontWeight.Bold) },
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
        val current = entry
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NabzPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { HeroRiskSection(current) }
                current.forecasting?.let { item { YarinTahminSection(it) } }
                item { EmaCevaplarBarSection(current) }
                item { Phq4DonutSection(current) }
                current.cascade?.let { item { ProfilBolgeGrid(it) } }
                current.pasifMl?.let { item { TelefonGaugeSection(it, current.mobilMeta) } }
                current.psikolojikAnaliz?.let { item { KisiselDegerlendirmeQuote(it) } }
                val nedenler = current.top5Neden.orEmpty()
                if (nedenler.isNotEmpty()) item { Top5NedenList(nedenler) }
                current.oneri?.takeIf { it.isNotBlank() }?.let { item { OneriSection(it) } }
                item { MinimalFooter() }
            }
        }
    }
}

// 1) Hero: gradient + dairesel risk halkası.
@Composable
private fun HeroRiskSection(entry: EmaEntryEntity) {
    val visuals = riskVisuals(entry.predictedRisk ?: entry.riskLevel)
    val mainColor = renkAdiniRengeMapla(entry.finalRenk) ?: visuals.color
    val percentage = riskYuzdesi(entry.predictedRisk ?: entry.riskLevel)
    val label = entry.finalIsim?.uppercase() ?: visuals.label

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        mainColor.copy(alpha = 0.95f),
                        mainColor.copy(alpha = 0.70f)
                    )
                )
            )
            .padding(vertical = 28.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = Color.White.copy(alpha = 0.25f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * (percentage / 100f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = visuals.emoji, fontSize = 36.sp)
                    Text(
                        text = "%$percentage",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formatTimestamp(entry.createdAt),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            entry.guvenilirlik?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Güvenilirlik: $it",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// 2) Yarın için: ikincil halka + metin.
@Composable
private fun YarinTahminSection(f: com.example.bitirmeapp.data.remote.Forecasting) {
    val yuksek = f.riskBinary == 1
    val pct = (f.olasilik * 100).toInt()
    val renk = if (yuksek) RiskHigh else RiskGood

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NabzPrimary.copy(alpha = 0.08f))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 9.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = renk.copy(alpha = 0.18f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = renk,
                        startAngle = -90f,
                        sweepAngle = 360f * (pct / 100f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "%$pct",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = renk
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🔮 Yarın Seni Bekleyen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NabzPrimary
                )
                Text(
                    text = if (yuksek) "Yüksek risk olasılığı" else "Düşük risk",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = renk
                )
                Text(
                    text = "EMA + telefon verisi birleştirilerek hesaplandı. " +
                        "Akademik doğruluk: AUC %${(f.auc * 100).toInt()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// 3) EMA cevapları: yatay bar.
@Composable
private fun EmaCevaplarBarSection(entry: EmaEntryEntity) {
    SoftCard(title = "Bugünkü Cevapların") {
        BarRow(
            label = "Stres",
            value = entry.stress,
            max = 5,
            sub = stressLabel(entry.stress),
            color = stressBarColor(entry.stress)
        )
        BarRow(
            label = "Ruh Hali (PAM)",
            value = entry.pam,
            max = 16,
            sub = pamLabel(entry.pam),
            color = NabzPrimary
        )
        BarRow(
            label = "Sosyallik",
            value = entry.socialLevel,
            max = 5,
            sub = socialLabel(entry.socialLevel),
            color = sosyalBarColor(entry.socialLevel)
        )
    }
}

@Composable
private fun BarRow(label: String, value: Int, max: Int, sub: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$value / $max",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.toFloat() / max.toFloat())
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Text(
            text = sub,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

// 4) PHQ-4: bölünmüş donut (anksiyete + depresyon).
@Composable
private fun Phq4DonutSection(entry: EmaEntryEntity) {
    val anx = entry.phq4Q1 + entry.phq4Q2
    val dep = entry.phq4Q3 + entry.phq4Q4
    val anxColor = Color(0xFFEF4444)
    val depColor = Color(0xFF8B5CF6)

    SoftCard(title = "PHQ-4 Klinik Tarama") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 16.dp.toPx()
                    val inset = stroke / 2
                    val topLeft = Offset(inset, inset)
                    val s = Size(size.width - stroke, size.height - stroke)
                    // Anksiyete — sol yarım (180° başla, sol yöne)
                    drawArc(
                        color = anxColor.copy(alpha = 0.15f),
                        startAngle = 90f, sweepAngle = 180f, useCenter = false,
                        topLeft = topLeft, size = s,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = anxColor,
                        startAngle = 90f,
                        sweepAngle = 180f * (anx / 6f),
                        useCenter = false,
                        topLeft = topLeft, size = s,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Depresyon — sağ yarım
                    drawArc(
                        color = depColor.copy(alpha = 0.15f),
                        startAngle = 270f, sweepAngle = 180f, useCenter = false,
                        topLeft = topLeft, size = s,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = depColor,
                        startAngle = 270f,
                        sweepAngle = 180f * (dep / 6f),
                        useCenter = false,
                        topLeft = topLeft, size = s,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${entry.phq4Total}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NabzPrimary
                    )
                    Text(
                        text = "/ 12",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Phq4Side(label = "Anksiyete", value = anx, color = anxColor)
                Phq4Side(label = "Depresyon", value = dep, color = depColor)
                Text(
                    text = phq4Seviye(entry.phq4Total).replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NabzPrimary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Phq4Mini("Gerginlik", entry.phq4Q1, anxColor)
        Phq4Mini("Endişe", entry.phq4Q2, anxColor)
        Phq4Mini("İlgi kaybı", entry.phq4Q3, depColor)
        Phq4Mini("Çökkünlük", entry.phq4Q4, depColor)
    }
}

@Composable
private fun Phq4Side(label: String, value: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$value / 6",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun Phq4Mini(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(85.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 3f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Text(
            text = phq4Label(value),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.End
        )
    }
}

// 5) Profil & ruh hali: yan yana 2 kart.
@Composable
private fun ProfilBolgeGrid(c: com.example.bitirmeapp.data.remote.Cascade) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniKart(
            modifier = Modifier.weight(1f),
            etiket = "🎯 Profilin",
            badge = c.profilId,
            baslik = c.profilIsim,
            aciklama = profilAciklama(c.profilId),
            renk = NabzPrimary
        )
        MiniKart(
            modifier = Modifier.weight(1f),
            etiket = "🎭 Ruh Hali",
            badge = c.pamQuadrant,
            baslik = pamBolgeBaslik(c.pamQuadrant),
            aciklama = pamBolgeMetni(c.pamQuadrant),
            renk = pamBolgeRenk(c.pamQuadrant)
        )
    }
}

@Composable
private fun MiniKart(
    modifier: Modifier = Modifier,
    etiket: String,
    badge: String,
    baslik: String,
    aciklama: String,
    renk: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = etiket,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(renk.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = renk
                )
            }
            Text(
                text = baslik,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = aciklama,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                lineHeight = 15.sp
            )
        }
    }
}

// 6) Telefon davranışı: yarım daire gösterge.
@Composable
private fun TelefonGaugeSection(
    p: com.example.bitirmeapp.data.remote.MLBilgi,
    meta: com.example.bitirmeapp.data.remote.MobilMeta?
) {
    val yuksek = p.riskBinary == 1
    val renk = if (yuksek) RiskHigh else RiskGood
    val pct = (p.olasilik * 100).toInt()

    SoftCard(title = "Telefon Davranış Sinyali") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 14.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = renk.copy(alpha = 0.18f),
                        startAngle = 180f, sweepAngle = 180f, useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height * 2 - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = renk,
                        startAngle = 180f,
                        sweepAngle = 180f * (pct / 100f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height * 2 - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "%$pct",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = renk
                    )
                    Text(
                        text = if (yuksek) "Yüksek" else "Düşük",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = renk
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (meta?.mod == "7-gunluk") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NabzPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${meta.pasifGunSayisi ?: 7} günlük veri",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NabzPrimary
                        )
                    }
                }
                meta?.let { m ->
                    Text(
                        text = "Telefondan ${m.telefondanGelen} ölçüm alındı, " +
                            "${m.medyanlaDolduruldu} değer ortalamayla tamamlandı.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 16.sp
                    )
                }
                Text(
                    text = "Telefon kullanım örüntüne dayalı bağımsız ikinci sinyaldir.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// 7) Kişisel değerlendirme: alıntı bloğu (sol kenarlık + italik).
@Composable
private fun KisiselDegerlendirmeQuote(pa: com.example.bitirmeapp.data.remote.PsikolojikAnaliz) {
    val renk = when (pa.renk?.lowercase()) {
        "kırmızı", "kirmizi" -> RiskHigh
        "turuncu" -> Color(0xFFFB923C)
        "sarı", "sari" -> Color(0xFFFBBF24)
        else -> RiskGood
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(renk.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(renk)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Kişisel Değerlendirme",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = renk
            )
            pa.emaYorum?.let {
                Text(text = it, fontSize = 14.sp, lineHeight = 20.sp, fontStyle = FontStyle.Italic)
            }
            pa.pasifYorum?.let {
                Text(text = it, fontSize = 14.sp, lineHeight = 20.sp, fontStyle = FontStyle.Italic)
            }
            pa.birlesikYorum?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            pa.oneri?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "💡", fontSize = 18.sp)
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            pa.guvenilirlik?.let {
                Text(
                    text = "Güvenilirlik: $it",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            pa.dayanak?.takeIf { it.isNotBlank() }?.let { dy ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                )
                Text(
                    text = "📚 Akademik Dayanak",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NabzPrimary
                )
                Text(
                    text = dy,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// 8) Top 5 neden: numaralı liste.
@Composable
private fun Top5NedenList(nedenler: List<String>) {
    SoftCard(title = "Bu Sonucu En Çok Etkileyen") {
        nedenler.forEachIndexed { idx, neden ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NabzPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = neden,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 9) Öneri.
@Composable
private fun OneriSection(oneri: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NabzPrimary.copy(alpha = 0.10f))
            .padding(18.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "💡", fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Bugünkü Tavsiye",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NabzPrimary
            )
            Text(
                text = oneri,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 10) Footer: uyarı + kaynakça.
@Composable
private fun MinimalFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NabzPrimary.copy(alpha = 0.06f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "ⓘ", fontSize = 14.sp, color = NabzPrimary)
            Text(
                text = "Bu uygulama tıbbi tanı aracı değildir. Endişe duyuyorsan bir uzmana danış.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = "Kaynaklar: PHQ-4 Kroenke 2009 · PAM Pollak 2011 · Russell 1980 · StudentLife Wang 2014",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Ortak: yumuşak kart kabuğu.
@Composable
private fun SoftCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

// Yardımcılar.
private fun renkAdiniRengeMapla(ad: String?): Color? = when (ad?.lowercase()) {
    "yesil", "yeşil", "green" -> RiskGood
    "sari", "sarı", "yellow" -> RiskMild
    "turuncu", "orange" -> RiskModerate
    "kirmizi", "kırmızı", "red" -> RiskHigh
    else -> null
}

private data class RiskVisuals(val color: Color, val emoji: String, val label: String)

private fun riskVisuals(level: Int): RiskVisuals = when (level) {
    0 -> RiskVisuals(RiskGood, "😊", "İYİ")
    1 -> RiskVisuals(RiskMild, "🙂", "HAFİF RİSK")
    2 -> RiskVisuals(RiskModerate, "😟", "ORTA RİSK")
    else -> RiskVisuals(RiskHigh, "😣", "YÜKSEK RİSK")
}

private fun riskYuzdesi(level: Int): Int = when (level) {
    0 -> 12
    1 -> 38
    2 -> 63
    else -> 88
}

private fun stressBarColor(v: Int): Color = when (v) {
    1, 2 -> RiskGood
    3 -> RiskMild
    4 -> RiskModerate
    else -> RiskHigh
}

private fun sosyalBarColor(v: Int): Color = when (v) {
    1 -> RiskHigh
    2 -> RiskModerate
    3 -> RiskMild
    else -> RiskGood
}

private fun stressLabel(v: Int): String = when (v) {
    1 -> "Hiç stresli değil"
    2 -> "Az stresli"
    3 -> "Orta düzeyde"
    4 -> "Çok stresli"
    else -> "Aşırı stresli"
}

private fun socialLabel(v: Int): String = when (v) {
    1 -> "Yalnız hissetti"
    2 -> "Az sosyal"
    3 -> "Normal"
    4 -> "Sosyal"
    else -> "Çok sosyal"
}

private fun pamLabel(v: Int): String = when {
    v <= 4 -> "Pozitif & Sakin"
    v <= 8 -> "Pozitif & Enerjik"
    v <= 12 -> "Negatif & Sakin"
    else -> "Negatif & Enerjik"
}

private fun phq4Label(v: Int): String = when (v) {
    0 -> "Hiç"
    1 -> "Birkaç gün"
    2 -> "Yarıdan fazla"
    else -> "Neredeyse her gün"
}

private fun formatTimestamp(epochMs: Long): String {
    val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("tr"))
    return dt.format(fmt)
}

private fun phq4Seviye(total: Int): String = when {
    total <= 2 -> "düşük düzey"
    total <= 5 -> "hafif düzey"
    total <= 8 -> "orta düzey"
    else -> "yüksek düzey"
}

private fun pamBolgeBaslik(q: String): String = when (q) {
    "Q1" -> "Canlı & Coşkulu"
    "Q2" -> "Sakin & Huzurlu"
    "Q3" -> "Gergin & Anksiyeteli"
    "Q4" -> "Durgun & Çökkün"
    else -> q
}

private fun pamBolgeMetni(q: String): String = when (q) {
    "Q1" -> "Yüksek enerji, olumlu"
    "Q2" -> "Düşük enerji, olumlu"
    "Q3" -> "Yüksek enerji, olumsuz"
    "Q4" -> "Düşük enerji, olumsuz"
    else -> q
}

private fun pamBolgeRenk(q: String): Color = when (q) {
    "Q1" -> RiskGood
    "Q2" -> Color(0xFF06B6D4)
    "Q3" -> RiskHigh
    "Q4" -> Color(0xFF6366F1)
    else -> NabzPrimary
}

private fun profilAciklama(id: String): String = when (id) {
    "A" -> "Optimum denge / akış hâli"
    "B" -> "Üretken, olumlu coşku"
    "C" -> "Normal akademik stres"
    "D" -> "Hafif anksiyete belirtisi"
    "E" -> "Orta anksiyete + sosyal izolasyon"
    "F" -> "Hafif depresif eğilim"
    "G" -> "Orta depresif belirti + yalnızlık"
    "H" -> "Karma yüksek risk"
    "I" -> "Akut anksiyete — kriz işareti"
    "J" -> "Akut depresif tablo — kriz işareti"
    "K" -> "Maskelenmiş tükenmişlik"
    "L" -> "Görünmez kriz / yalnızlık"
    else -> "Genel duygu durum değerlendirmesi"
}
