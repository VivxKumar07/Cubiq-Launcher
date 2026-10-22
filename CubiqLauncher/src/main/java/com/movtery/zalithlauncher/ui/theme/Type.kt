/*
 * Cubiq Launcher
 * Dynamic Typography & Font Customizer
 */

package com.movtery.zalithlauncher.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import java.io.File

private val defaultMinecraftFamily = FontFamily(
    Font(R.font.minecraft, FontWeight.Normal),
    Font(R.font.minecraft, FontWeight.Bold)
)

object FontManager {
    const val FONT_MINECRAFT = "minecraft"
    const val FONT_SANS = "sans"
    const val FONT_MONOSPACE = "monospace"
    const val FONT_SERIF = "serif"
    const val FONT_SYSTEM = "system"
    const val FONT_CUSTOM = "custom"

    var currentFontType by mutableStateOf(AllSettings.launcherFont.getValue().ifBlank { FONT_MINECRAFT })
    var customFontPath by mutableStateOf(AllSettings.customFontPath.getValue())

    private fun resolveFontFamily(type: String, path: String): FontFamily {
        return when (type) {
            FONT_SANS -> FontFamily.SansSerif
            FONT_MONOSPACE -> FontFamily.Monospace
            FONT_SERIF -> FontFamily.Serif
            FONT_SYSTEM -> FontFamily.Default
            FONT_CUSTOM -> {
                val f = File(path)
                if (f.exists() && f.isFile) {
                    runCatching {
                        FontFamily(Typeface.createFromFile(f))
                    }.getOrDefault(FontFamily.SansSerif)
                } else {
                    FontFamily.SansSerif
                }
            }
            else -> defaultMinecraftFamily
        }
    }

    var currentFontFamily by mutableStateOf(resolveFontFamily(currentFontType, customFontPath))
        private set

    fun setFont(type: String, customPath: String = "") {
        currentFontType = type
        customFontPath = customPath
        currentFontFamily = resolveFontFamily(type, customPath)
        AllSettings.launcherFont.save(type)
        AllSettings.customFontPath.save(customPath)
    }
}

val MinecraftFontFamily: FontFamily
    get() = FontManager.currentFontFamily

private val defaultTypography = Typography()

val AppTypography: Typography
    get() = Typography(
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
