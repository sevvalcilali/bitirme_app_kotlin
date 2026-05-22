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
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzTheme

// PHQ-4 maddesi (Kroenke et al. 2009). 0-3 puan; dört madde de aynı bileşeni
// kullanır, sadece questionTitle değişir.
private data class Phq4Option(val value: Int, val label: String)

private val phq4Options = listOf(
    Phq4Option(0, "Hiç"),
    Phq4Option(1, "Birkaç gün"),
    Phq4Option(2, "Yarıdan fazla gün"),
    Phq4Option(3, "Neredeyse her gün")
)

@Composable
fun Phq4Question(
    subtitle: String = "Son 2 hafta içinde, aşağıdaki sorundan ne sıklıkla rahatsız oldun?",
    questionTitle: String,
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
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“$questionTitle”",
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
            phq4Options.forEach { option ->
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = NabzPrimary)
                        )
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
private fun Phq4QuestionPreview() {
    NabzTheme {
        Phq4Question(
            questionTitle = "Gergin, kaygılı veya huzursuz hissetme",
            selected = 1,
            onAnswered = {}
        )
    }
}
