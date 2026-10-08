/*
 * Cubiq Launcher
 * Minecraft Official Artwork Manager with High-Resolution Artwork Resolution & Disk Caching
 */

package com.movtery.zalithlauncher.game.versioninfo

import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object MinecraftArtworkManager {
    private const val TAG = "MinecraftArtwork"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Resolves the artwork model (File if cached, otherwise bundled drawable ResId) for a given version.
     * Guarantees an authentic high-resolution landscape key art is always displayed immediately.
     */
    fun getArtworkModel(version: Version?): Any {
        val verName = version?.getVersionName()?.lowercase() ?: "1.21"
        val safeKey = when {
            verName.contains("1.21") -> "1_21"
            verName.contains("1.20") -> "1_20"
            verName.contains("1.19") -> "1_19"
            verName.contains("1.18") -> "1_18"
            verName.contains("1.17") -> "1_18"
            verName.contains("1.16") -> "1_16"
            verName.contains("1.15") -> "1_14"
            verName.contains("1.14") -> "1_14"
            verName.contains("1.13") -> "1_13"
            else -> "classic"
        }

        val cacheDir = PathManager.DIR_CACHE_HOME_PAGE
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val cachedFile = File(cacheDir, "hero_art_$safeKey.jpg")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            return cachedFile
        }

        return getHeroArtResource(safeKey)
    }

    fun getHeroArtResource(safeKey: String): Int {
        return when (safeKey) {
            "1_21" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_21
            "1_20" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_20
            "1_19" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_19
            "1_18" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_18
            "1_16" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_16
            "1_14" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_14
            "1_13" -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_1_13
            else -> com.movtery.zalithlauncher.R.drawable.img_mc_hero_classic
        }
    }
}

