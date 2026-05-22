package com.example.bitirmeapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Nabz renk paleti (lavanta + lacivert).
val NabzPrimary = Color(0xFF5B4B8A)
val NabzPrimaryDark = Color(0xFF453A6B)   // degrade üstü
val NabzSecondary = Color(0xFFA294D1)     // açık lavanta
val NabzBackground = Color(0xFFF5F3FA)
val NabzSurface = Color(0xFFFFFFFF)
val NabzOnPrimary = Color(0xFFFFFFFF)
val NabzOnBackground = Color(0xFF2C3E5C)
val NabzError = Color(0xFFF44336)

// Risk renkleri (Mehta & Zhu 2009).
val RiskGood = Color(0xFF4CAF50)      // 0 - yeşil
val RiskMild = Color(0xFFFFC107)      // 1 - sarı
val RiskModerate = Color(0xFFFF9800)  // 2 - turuncu
val RiskHigh = Color(0xFFF44336)      // 3 - kırmızı

// Hero ve auth ekranları için marka degradesi.
val NabzGradient = Brush.verticalGradient(
    colors = listOf(NabzPrimaryDark, NabzPrimary, NabzSecondary)
)

val NabzHeaderGradient = Brush.horizontalGradient(
    colors = listOf(NabzPrimary, NabzSecondary)
)
