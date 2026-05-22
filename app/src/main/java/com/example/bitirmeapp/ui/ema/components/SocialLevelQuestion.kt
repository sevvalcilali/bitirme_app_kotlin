package com.example.bitirmeapp.ui.ema.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzTheme

// Sosyal seviye ölçümü, StudentLife CES temelli (Nepal et al. 2024).
private data class SocialOption(val value: Int, val emoji: String, val label: String)

private val socialOptions = listOf(
    SocialOption(1, "🏠", "Yalnız hissettim"),
    SocialOption(2, "👤", "Az sosyaldim"),
    SocialOption(3, "👥", "Normaldim"),
    SocialOption(4, "👨‍👩‍👦", "Sosyaldim"),
    SocialOption(5, "🎉", "Çok sosyaldim")
)

@Composable
fun SocialLevelQuestion(
    selected: Int?,
    onAnswered: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Bugün ne kadar sosyal hissettin?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = NabzPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            socialOptions.forEach { option ->
                val isSelected = selected == option.value
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NabzPrimary.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(2.dp, NabzPrimary) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    onClick = { onAnswered(option.value) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = NabzPrimary)
                        )
                        Text(text = option.emoji, fontSize = 24.sp)
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SocialLevelQuestionPreview() {
    NabzTheme {
        SocialLevelQuestion(selected = 3, onAnswered = {})
    }
}
