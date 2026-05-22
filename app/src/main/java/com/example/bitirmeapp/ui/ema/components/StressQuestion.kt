package com.example.bitirmeapp.ui.ema.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzTheme

// Stres sorusu, PSS temelli (Cohen 1983). 1-5 Likert.
@Composable
fun StressQuestion(
    selected: Int?,
    onAnswered: (Int) -> Unit
) {
    var sliderValue by remember(selected) {
        mutableFloatStateOf(selected?.toFloat() ?: 3f)
    }

    LaunchedEffect(Unit) {
        if (selected == null) onAnswered(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Şu an ne kadar streslisin?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = NabzPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("😌", "😐", "😟", "😰", "😱").forEachIndexed { index, emoji ->
                val isCurrent = sliderValue.toInt() == index + 1
                Text(
                    text = emoji,
                    fontSize = if (isCurrent) 40.sp else 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onAnswered(sliderValue.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = NabzPrimary,
                activeTrackColor = NabzPrimary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = labelFor(sliderValue.toInt()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

private fun labelFor(value: Int): String = when (value) {
    1 -> "Hiç stresli değilim"
    2 -> "Az stresli"
    3 -> "Orta düzeyde stresli"
    4 -> "Çok stresli"
    5 -> "Aşırı stresli"
    else -> ""
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StressQuestionPreview() {
    NabzTheme {
        StressQuestion(selected = null, onAnswered = {})
    }
}
