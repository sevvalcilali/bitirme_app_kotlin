package com.example.bitirmeapp.ui.ema

import android.app.Application
import com.example.bitirmeapp.util.OturumYoneticisi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.data.local.AppDatabase
import com.example.bitirmeapp.ui.theme.NabzBackground
import com.example.bitirmeapp.ui.theme.NabzHeaderGradient
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzSecondary
import com.example.bitirmeapp.ui.theme.NabzTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

// Günde 2x EMA (sabah/akşam) compliance için iyi (Stone & Shiffman 2002).
// Eşik 12:00: 00-12 sabah, 12-24 akşam. Aynı dilim ikinci kez doldurulamaz.
private enum class DilimDurum { AKTIF, TAMAM, KILITLI }

@Composable
fun EmaIntroScreen(onStartClick: () -> Unit) {
    val context = LocalContext.current
    var morningDone by remember { mutableStateOf(false) }
    var eveningDone by remember { mutableStateOf(false) }
    var yuklendi by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context.applicationContext as Application)
        val dao = db.emaDao()
        val kullaniciDao = db.kullaniciDao()
        val aktifEmail = OturumYoneticisi.aktifEmail(context)
        val aktifUserId = aktifEmail?.let { kullaniciDao.emailIleBul(it)?.id } ?: 0L
        val bugun = LocalDate.now().toString()
        val zone = ZoneId.systemDefault()
        val kayitlar = dao.getForDate(aktifUserId, bugun)
        morningDone = kayitlar.any {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).hour < 12
        }
        eveningDone = kayitlar.any {
            Instant.ofEpochMilli(it.createdAt).atZone(zone).hour >= 12
        }
        yuklendi = true
    }

    val suankiSabah = LocalTime.now().hour < 12

    // Sadece bulunduğun dilimin Başla'sı aktif olsun ki 2x/gün korunsun.
    val sabahDurum = when {
        morningDone -> DilimDurum.TAMAM
        suankiSabah -> DilimDurum.AKTIF
        else -> DilimDurum.KILITLI
    }
    val aksamDurum = when {
        eveningDone -> DilimDurum.TAMAM
        !suankiSabah -> DilimDurum.AKTIF
        else -> DilimDurum.KILITLI
    }

    val hepsiDolu = morningDone && eveningDone
    val durumMesaji: String? = when {
        !yuklendi -> null
        hepsiDolu -> "Bugünkü iki değerlendirmeni de tamamladın 🎉 Yarın tekrar gel."
        morningDone && suankiSabah -> "Sabah değerlendirmen tamamlandı. Akşam (12:00 sonrası) tekrar gel."
        eveningDone && !suankiSabah -> "Akşam değerlendirmen tamamlandı. Yarın sabah tekrar gel."
        else -> null
    }

    EmaIntroContent(
        yuklendi = yuklendi,
        sabahDurum = sabahDurum,
        aksamDurum = aksamDurum,
        durumMesaji = durumMesaji,
        onStartClick = onStartClick
    )
}

@Composable
private fun EmaIntroContent(
    yuklendi: Boolean,
    sabahDurum: DilimDurum,
    aksamDurum: DilimDurum,
    durumMesaji: String?,
    onStartClick: () -> Unit
) {
    Scaffold(
        containerColor = NabzBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gradient hero başlık
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(NabzHeaderGradient)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📝", fontSize = 34.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Günlük Değerlendirme",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bugün nasıl hissettiğini öğrenmek için kısa bir ankete katıl.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoCard()

                Text(
                    text = "Bugünkü Dilimler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NabzPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                SlotCard(
                    icon = Icons.Default.WbSunny,
                    baslik = "Sabah Değerlendirmesi",
                    altBaslik = "00:00 – 11:59 arası",
                    durum = sabahDurum,
                    enabled = yuklendi,
                    onStartClick = onStartClick
                )

                SlotCard(
                    icon = Icons.Default.NightsStay,
                    baslik = "Akşam Değerlendirmesi",
                    altBaslik = "12:00 – 23:59 arası",
                    durum = aksamDurum,
                    enabled = yuklendi,
                    onStartClick = onStartClick
                )

                if (durumMesaji != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NabzPrimary.copy(alpha = 0.12f)
                        )
                    ) {
                        Text(
                            text = "ℹ️ $durumMesaji",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NabzPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = "Yanıtların gizlidir ve sadece sana yöneliktir.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SlotCard(
    icon: ImageVector,
    baslik: String,
    altBaslik: String,
    durum: DilimDurum,
    enabled: Boolean,
    onStartClick: () -> Unit
) {
    val aktif = durum == DilimDurum.AKTIF
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (aktif) MaterialTheme.colorScheme.surface
            else NabzSecondary.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (aktif) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NabzPrimary.copy(alpha = if (aktif) 0.14f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NabzPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = baslik,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = altBaslik,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                when (durum) {
                    DilimDurum.TAMAM -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Tamamlandı",
                        tint = NabzPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    DilimDurum.KILITLI -> Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Kilitli",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    DilimDurum.AKTIF -> {}
                }
            }

            when (durum) {
                DilimDurum.AKTIF -> Button(
                    onClick = onStartClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NabzPrimary,
                        disabledContainerColor = NabzPrimary.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "BAŞLA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                DilimDurum.TAMAM -> Text(
                    text = "Tamamlandı ✓",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NabzPrimary
                )
                DilimDurum.KILITLI -> Text(
                    text = "Bu dilim henüz açılmadı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "ℹ️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bu anketi günde 2 kez doldurman önerilir",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoChip(icon = Icons.Default.Assignment, title = "7", subtitle = "soru")
                InfoChip(icon = Icons.Default.Timer, title = "~2 dk", subtitle = "süre")
                InfoChip(icon = Icons.Default.WbSunny, title = "2x", subtitle = "günde")
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NabzPrimary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EmaIntroScreenPreview() {
    NabzTheme {
        EmaIntroContent(
            yuklendi = true,
            sabahDurum = DilimDurum.TAMAM,
            aksamDurum = DilimDurum.AKTIF,
            durumMesaji = null,
            onStartClick = {}
        )
    }
}
