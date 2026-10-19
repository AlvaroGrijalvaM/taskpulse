package com.taskpulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// Wear OS suele estar en modo oscuro; TaskPulse fuerza esa estética para
// ahorrar batería en pantallas OLED y mantener legibilidad en la muñeca.
private val Colors = Colors(
    primary = Brand,
    primaryVariant = BrandDim,
    secondary = Brand,
    secondaryVariant = BrandDim,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = Overdue,
    onPrimary = BackgroundDark,
    onSecondary = BackgroundDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    onError = BackgroundDark
)

@Composable
fun TaskPulseTheme(content: @Composable () -> Unit) {
    // isSystemInDarkTheme se ignora a propósito: la app siempre es oscura.
    MaterialTheme(colors = Colors, content = content)
}
