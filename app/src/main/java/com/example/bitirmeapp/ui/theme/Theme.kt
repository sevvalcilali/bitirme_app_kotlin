package com.example.bitirmeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NabzLightColorScheme = lightColorScheme(
    primary = NabzPrimary,
    onPrimary = NabzOnPrimary,
    secondary = NabzSecondary,
    onSecondary = NabzOnPrimary,
    background = NabzBackground,
    onBackground = NabzOnBackground,
    surface = NabzSurface,
    onSurface = NabzOnBackground,
    error = NabzError,
    onError = NabzOnPrimary
)

@Composable
fun NabzTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NabzLightColorScheme,
        typography = Typography,
        content = content
    )
}
