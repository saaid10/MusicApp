package com.saeed.musicapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.saeed.musicapp.R

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

// Material 3's default text styles (sizes, line heights), with the font swapped
private val defaults = Typography()

val AppTypography = Typography(
    displayLarge = defaults.displayLarge.copy(fontFamily = PlusJakartaSans),
    displayMedium = defaults.displayMedium.copy(fontFamily = PlusJakartaSans),
    displaySmall = defaults.displaySmall.copy(fontFamily = PlusJakartaSans),
    headlineLarge = defaults.headlineLarge.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold),
    headlineMedium = defaults.headlineMedium.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold),
    headlineSmall = defaults.headlineSmall.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold),
    titleLarge = defaults.titleLarge.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold),
    titleSmall = defaults.titleSmall.copy(fontFamily = PlusJakartaSans),
    bodyLarge = defaults.bodyLarge.copy(fontFamily = PlusJakartaSans),
    bodyMedium = defaults.bodyMedium.copy(fontFamily = PlusJakartaSans),
    bodySmall = defaults.bodySmall.copy(fontFamily = PlusJakartaSans),
    labelLarge = defaults.labelLarge.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold),
    labelMedium = defaults.labelMedium.copy(fontFamily = PlusJakartaSans),
    labelSmall = defaults.labelSmall.copy(fontFamily = PlusJakartaSans),
)
