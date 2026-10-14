package com.saeed.musicapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Dark only: the design has no light variant
private val AppColors = darkColorScheme(
    primary = Emerald,
    onPrimary = OnAccent,
    secondary = Teal,
    onSecondary = OnAccent,
    tertiary = Mint,
    onTertiary = OnAccent,
    background = Night,
    onBackground = TextPrimary,
    surface = Night,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceRaised,
    outline = Outline,
    error = ErrorRed,
)

// Rounded cards; buttons use their own pill shape by default
private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun MusicAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
