package com.turbobooster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaCores = darkColorScheme(
    primary = VerdeNeon,
    secondary = AzulNeon,
    tertiary = VioletaNeon,
    background = FundoPrincipal,
    surface = FundoCard,
    onPrimary = Color.Black,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
    error = VermelhoDanger
)

@Composable
fun TurboBoosterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCores,
        content = content
    )
}
