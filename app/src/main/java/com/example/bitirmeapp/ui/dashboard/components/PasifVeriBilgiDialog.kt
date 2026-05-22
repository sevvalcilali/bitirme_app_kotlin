package com.example.bitirmeapp.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.ui.theme.NabzPrimary

@Composable
fun PasifVeriBilgiDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔒", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Veri Toplama Bilgisi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = NabzPrimary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Daha doğru risk analizi yapabilmek için son 7 gün boyunca verdiğin izinler çerçevesinde uyku, ekran kullanımı ve aktivite verileri arka planda toplanır.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tüm veriler cihazında kalır, hiçbir şey dış sunucuya kaydedilmez.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NabzPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Anladım", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
