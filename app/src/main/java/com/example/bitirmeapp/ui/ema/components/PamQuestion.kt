package com.example.bitirmeapp.ui.ema.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzTheme

// PAM ruh hali ölçeği (Pollak ve ark. 2011; Russell 1980, circumplex).
// Emoji'ler gerçek PAM görselleri için geçici yer tutucu.
@Composable
fun PamQuestion(
    selected: Int?,
    onAnswered: (Int) -> Unit
) {
    val emojis = listOf(
        "🌅", "😌", "🎉", "🚀",
        "🌸", "☺️", "🤩", "🌟",
        "😔", "😞", "😣", "😟",
        "😴", "😢", "😩", "😰"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Şu anki ruh halini en iyi yansıtan resmi seç",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = NabzPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(emojis.withIndex().toList(), key = { it.index }) { (index, emoji) ->
                val value = index + 1
                val isSelected = selected == value
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NabzPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(3.dp, NabzPrimary) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    onClick = { onAnswered(value) }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PamQuestionPreview() {
    NabzTheme {
        PamQuestion(selected = 6, onAnswered = {})
    }
}
