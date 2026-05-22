package com.example.bitirmeapp.ui.ema.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bitirmeapp.ui.theme.NabzPrimary
import com.example.bitirmeapp.ui.theme.NabzTheme

@Composable
fun EmaProgressBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val target = (currentPage + 1).toFloat() / totalPages.toFloat()
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 350),
        label = "emaProgress"
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = NabzPrimary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
private fun EmaProgressBarPreview() {
    NabzTheme {
        EmaProgressBar(currentPage = 3, totalPages = 7)
    }
}
