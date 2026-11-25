/*
 * Cubiq Launcher
 * Dynamic Typography & Font Customizer
 */

package com.movtery.zalithlauncher.ui.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.fonts.Font as PlatformFont
import android.graphics.fonts.FontFamily as PlatformFontFamily
import android.graphics.fonts.FontStyle as PlatformFontStyle
import android.icu.util.ULocale
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale

private val minecraftRegular = Font(R.font.minecraft, FontWeight.Normal)
private val minecraftBold = Font(R.font.minecraft, FontWeight.Bold)
private val defaultMinecraftFamily = FontFamily(minecraftRegular, minecraftBold)

private val minecraftWithChineseFallback = FontFamily(
    MinecraftChineseFont(FontWeight.Normal),
    minecraftRegular,
    MinecraftChineseFont(FontWeight.Bold),
    minecraftBold
)

private val fusionPixelFamily = FontFamily(
    Font(R.font.fusion_pixel_zh_hans, loadingStrategy = FontLoadingStrategy.Async),
    minecraftRegular
)

private class MinecraftChineseFont(override val weight: FontWeight) : AndroidFont(
    FontLoadingStrategy.Async,
    MinecraftChineseFontLoader,
    FontVariation.Settings()
) {
    override val style: FontStyle = FontStyle.Normal
}

private object MinecraftChineseFontLoader : AndroidFont.TypefaceLoader {
    override fun loadBlocking(context: Context, font: AndroidFont): Typeface? = null

    override suspend fun awaitLoad(context: Context, font: AndroidFont): Typeface? =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext null
            try {
                loadTypeface(context.resources, font.weight.weight)
            } catch (e: IOException) {
                Logger.warning("FontManager", "Could not load the Chinese pixel font", e)
                null
            } catch (e: RuntimeException) {
                Logger.warning("FontManager", "Could not load the Chinese pixel font", e)
                null
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadTypeface(resources: Resources, weight: Int): Typeface {
        val minecraft = PlatformFont.Builder(resources, R.font.minecraft).setWeight(weight).build()
        val chinese = PlatformFont.Builder(resources, R.font.fusion_pixel_zh_hans).build()
        return Typeface.CustomFallbackBuilder(PlatformFontFamily.Builder(minecraft).build())
            .addCustomFallback(PlatformFontFamily.Builder(chinese).build())
            .setStyle(PlatformFontStyle(weight, PlatformFontStyle.FONT_SLANT_UPRIGHT))
            .build()
    }
}

private fun isSimplifiedChinese(locale: Locale): Boolean =
    locale.language == "zh" && ULocale.addLikelySubtags(ULocale.forLocale(locale)).script == "Hans"

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
    @Composable get() {
        val current = FontManager.currentFontFamily
        if (current != defaultMinecraftFamily || !AllSettings.launcherChinesePixelFont.state) return current

        val locale = LocalConfiguration.current.locales[0] ?: return current
        if (!remember(locale) { isSimplifiedChinese(locale) }) return current

        // Android 8 and 9 can load Fusion Pixel directly but cannot compose a custom fallback chain.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) minecraftWithChineseFallback else fusionPixelFamily
    }

private val defaultTypography = Typography()

@Composable
internal fun launcherTypography(): Typography {
    val fontFamily = MinecraftFontFamily
    return remember(fontFamily) {
        Typography(
            displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
        )
    }
}
