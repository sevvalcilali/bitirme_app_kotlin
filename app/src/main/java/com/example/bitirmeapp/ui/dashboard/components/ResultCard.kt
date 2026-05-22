package com.example.bitirmeapp.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.ui.theme.*

@Composable
fun ResultCard(
    riskLevel: Int,
    riskLabel: String,
    riskPercentage: Int,
    timestamp: String,
    recommendation: String,
    onDetailClick: () -> Unit
) {
    val backgroundColor = when (riskLevel) {
        0 -> RiskGood
        1 -> RiskMild
        2 -> RiskModerate
        else -> RiskHigh
    }

    val contentColor = Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val emoji = when (riskLevel) {
                0 -> "🟢"
                1 -> "🟡"
                2 -> "🟠"
                else -> "🔴"
            }

            Text(text = emoji, fontSize = 40.sp)
            
            Text(
                text = riskLabel.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            Text(
                text = "Risk: %$riskPercentage",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Son güncelleme: $timestamp",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "💡 Bugünkü Tavsiye:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(contentColor))
            ) {
                Text(text = "DETAYLI GÖR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
