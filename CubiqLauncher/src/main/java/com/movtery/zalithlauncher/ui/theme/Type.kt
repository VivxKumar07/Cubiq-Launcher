/*
 * Cubiq Launcher
 * Minecraft PC Authentic Typography & Font
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.R

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraft, FontWeight.Normal),
    Font(R.font.minecraft, FontWeight.Bold)
)

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = MinecraftFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = MinecraftFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = MinecraftFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = MinecraftFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = MinecraftFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = MinecraftFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = MinecraftFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = MinecraftFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = MinecraftFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = MinecraftFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = MinecraftFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = MinecraftFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = MinecraftFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = MinecraftFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = MinecraftFontFamily)
)
