package com.moviebox.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0F1014)
val Card = Color(0xFF1C1D23)
val NavBg = Color(0xFF14151A)
val Field = Color(0xFF23242B)
val Divider = Color(0xFF2A2B33)
val Muted = Color(0xFFA3A7B3)
val Accent = Color(0xFF2EE6A6)
val Blue = Color(0xFF18B4F0)
val OnAccent = Color(0xFF06281D)
val Gradient = Brush.horizontalGradient(listOf(Blue, Accent))

@Composable
fun MovieBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(background = Bg, surface = Bg, primary = Accent, onPrimary = OnAccent),
        content = content
    )
}
