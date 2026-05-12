package com.turbobooster.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// CYBERPUNK COLOR PALETTE
val NeonGreen = Color(0xFF00FF88)
val NeonPurple = Color(0xFF9B59FF)
val NeonBlue = Color(0xFF00D4FF)
val NeonRed = Color(0xFFFF3B5C)
val NeonOrange = Color(0xFFFF6B00)

val BgDeep = Color(0xFF080810)
val BgCard = Color(0xFF0F0F1A)
val BgCardBright = Color(0xFF161628)
val BorderNeon = Color(0xFF1A1A35)

val TextPrimary = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFF6666AA)
val TextDim = Color(0xFF333355)

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonPurple,
    tertiary = NeonBlue,
    background = BgDeep,
    surface = BgCard,
    onPrimary = BgDeep,
    onSecondary = BgDeep,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = NeonRed
)

@Composable
fun TurboTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
