package com.example.bitirmeapp.ui.izinler

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bitirmeapp.ui.theme.NabzBackground
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.RiskGood

// Sadece izin ister ve durumu gösterir; burada veri toplanmaz.
// İzin verilmese de DEVAM ile geçilir, backend eksikleri medyanla doldurur.
@Composable
fun IzinlerEkrani(onDevam: () -> Unit) {
    val context = LocalContext.current

    var aktiviteVerildi by remember { mutableStateOf(aktiviteIzniVar(context)) }
    var konumVerildi by remember { mutableStateOf(konumIzniVar(context)) }
    var kullanimVerildi by remember { mutableStateOf(kullanimIzniVar(context)) }

    fun durumlariYenile() {
        aktiviteVerildi = aktiviteIzniVar(context)
        konumVerildi = konumIzniVar(context)
        kullanimVerildi = kullanimIzniVar(context)
    }

    val izinLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { durumlariYenile() }

    val ayarlarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { durumlariYenile() }

    LaunchedEffect(Unit) { durumlariYenile() }

    Scaffold(containerColor = NabzBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = "📱 İzinler",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NabzPrimary
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Ruh hali tahminini daha doğru yapabilmek için telefon " +
                    "kullanım, aktivite ve konum verisi kullanıyoruz. İzin " +
                    "vermesen de uygulama çalışır — model eksik veriyi " +
                    "ortalama değerlerle tamamlar.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(28.dp))

            IzinSatiri(
                baslik = "Aktivite Tanıma",
                aciklama = "Yürüme, durağan kalma, hareket süresi",
                verildi = aktiviteVerildi,
                onIzinVer = {
                    val izinler = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        izinler.add(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    izinLauncher.launch(izinler.toTypedArray())
                }
            )

            Spacer(Modifier.height(14.dp))

            IzinSatiri(
                baslik = "Konum",
                aciklama = "Hareketlilik / gidilen mesafe (yaklaşık)",
                verildi = konumVerildi,
                onIzinVer = {
                    izinLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            Spacer(Modifier.height(14.dp))

            IzinSatiri(
                baslik = "Kullanım İstatistiği",
                aciklama = "Ekran/uygulama kullanım süresi (Ayarlar'dan açılır)",
                verildi = kullanimVerildi,
                onIzinVer = {
                    ayarlarLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = onDevam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NabzPrimary)
            ) {
                Text(
                    text = "DEVAM ET",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IzinSatiri(
    baslik: String,
    aciklama: String,
    verildi: Boolean,
    onIzinVer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (verildi) RiskGood.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (verildi) "✓" else "✗", fontSize = 20.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = baslik,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (verildi) "İzin verildi" else aciklama,
                    fontSize = 12.sp,
                    color = if (verildi) RiskGood else MaterialTheme.colorScheme.outline
                )
            }

            if (!verildi) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onIzinVer) {
                    Text(
                        text = "İzin Ver",
                        color = NabzPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun aktiviteIzniVar(context: Context): Boolean {
    // Android 10 (API 29) altında ACTIVITY_RECOGNITION runtime izni yoktur;
    // o sürümlerde otomatik kabul sayılır.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACTIVITY_RECOGNITION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun konumIzniVar(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun kullanimIzniVar(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}
