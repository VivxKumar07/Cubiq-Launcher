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

    // Official full-resolution landscape key art for major releases
    private val VERSION_ARTWORK_URLS = mapOf(
        "1.21" to "https://launchercontent.mojang.com/v2/images/1-21-patchnotes.jpg",
        "1.20" to "https://launchercontent.mojang.com/v2/images/1-20-patchnotes.jpg",
        "1.19" to "https://launchercontent.mojang.com/v2/images/1-19-patchnotes.jpg",
        "1.18" to "https://launchercontent.mojang.com/v2/images/1-18-patchnotes.jpg",
        "1.17" to "https://launchercontent.mojang.com/v2/images/1-17-patchnotes.jpg",
        "1.16" to "https://launchercontent.mojang.com/v2/images/1-16-patchnotes.jpg",
        "1.15" to "https://launchercontent.mojang.com/v2/images/1-15-patchnotes.jpg",
        "1.14" to "https://launchercontent.mojang.com/v2/images/1-14-patchnotes.jpg"
    )

    private const val DEFAULT_HERO_URL = "https://launchercontent.mojang.com/v2/images/1-21-patchnotes.jpg"

    /**
     * Resolves the artwork model (File if cached, otherwise String URL) for a given version.
     * Also triggers asynchronous download and caching to disk if not already present.
     */
    fun getArtworkModel(version: Version?): Any {
        val verName = version?.getVersionName()?.lowercase() ?: "1.21"
        val matchedKey = VERSION_ARTWORK_URLS.keys.firstOrNull { verName.contains(it) } ?: "1.21"
        val remoteUrl = VERSION_ARTWORK_URLS[matchedKey] ?: DEFAULT_HERO_URL

        val safeKey = matchedKey.replace(".", "_")
        val cacheDir = PathManager.DIR_CACHE_HOME_PAGE
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val cachedFile = File(cacheDir, "hero_art_$safeKey.jpg")
        if (cachedFile.exists() && cachedFile.length() > 0) {
            return cachedFile
        }

        // Cache asynchronously
        cacheArtworkAsync(remoteUrl, cachedFile)
        return remoteUrl
    }

    private fun cacheArtworkAsync(urlStr: String, targetFile: File) {
        scope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val url = URL(urlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 10000
                    conn.connect()
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val tempFile = File(targetFile.parentFile, targetFile.name + ".tmp")
                        conn.inputStream.use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (tempFile.exists() && tempFile.length() > 0) {
                            tempFile.renameTo(targetFile)
                            Logger.info(TAG, "Cached hero artwork to ${targetFile.absolutePath}")
                        }
                    }
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed caching artwork for $urlStr: ${e.message}")
                }
            }
        }
    }
}
