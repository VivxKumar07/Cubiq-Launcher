/*
 * Cubiq Launcher
 * GitHub Releases & Announcements Manager
 * Connects to VivxKumar07/Cubiq-Launcher repository for authentic updates and announcements.
 */

package com.movtery.zalithlauncher.game.versioninfo

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CubiqReleaseItem(
    val tagName: String,
    val title: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val downloadUrl: String?
)

object CubiqGitHubManager {
    private const val TAG = "CubiqGitHub"
    private const val URL_RELEASES = "https://api.github.com/repos/VivxKumar07/Cubiq-Launcher/releases"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _releases = MutableStateFlow<List<CubiqReleaseItem>>(emptyList())
    val releases = _releases.asStateFlow()

    private val _newUpdate = MutableStateFlow<CubiqReleaseItem?>(null)
    val newUpdate = _newUpdate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadCache()
        fetchReleases()
    }

    fun fetchReleases(force: Boolean = false) {
        scope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                try {
                    val json = fetchStringFromUrls(listOf(URL_RELEASES))
                    parseReleases(json)
                    runCatching {
                        val cacheFile = File(PathManager.DIR_CACHE_HOME_PAGE, "cubiq_releases.json")
                        cacheFile.parentFile?.mkdirs()
                        cacheFile.writeText(json)
                    }
                } catch (e: Exception) {
                    Logger.warning(TAG, "Failed to fetch GitHub releases: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun parseReleases(json: String) {
        try {
            val array = GSON.fromJson(json, JsonArray::class.java) ?: return
            val list = mutableListOf<CubiqReleaseItem>()

            for (elem in array) {
                if (!elem.isJsonObject) continue
                val obj = elem.asJsonObject
                val tagName = obj.get("tag_name")?.asString ?: ""
                val title = obj.get("name")?.asString?.ifBlank { tagName } ?: tagName
                val body = obj.get("body")?.asString ?: "No changelog provided."
                val publishedAt = obj.get("published_at")?.asString?.take(10) ?: ""
                val htmlUrl = obj.get("html_url")?.asString ?: "https://github.com/VivxKumar07/Cubiq-Launcher/releases"

                var downloadUrl: String? = null
                val assets = obj.getAsJsonArray("assets")
                if (assets != null) {
                    val apkList = mutableListOf<Pair<String, String>>()
                    for (a in assets) {
                        val aObj = a.asJsonObject
                        val name = aObj.get("name")?.asString ?: ""
                        val url = aObj.get("browser_download_url")?.asString
                        if (name.endsWith(".apk", ignoreCase = true) && !url.isNullOrBlank()) {
                            apkList.add(name to url)
                        }
                    }

                    val targetKeyword = getTargetApkKeyword()
                    val matched = when (targetKeyword) {
                        "arm64-v8a" -> apkList.firstOrNull { it.first.contains("arm64", ignoreCase = true) || it.first.contains("v8a", ignoreCase = true) }
                        "armeabi-v7a" -> apkList.firstOrNull {
                            (it.first.contains("v7a", ignoreCase = true) || it.first.contains("armeabi", ignoreCase = true) ||
                             it.first.contains("-arm.", ignoreCase = true) || it.first.contains("-arm-", ignoreCase = true)) &&
                            !it.first.contains("arm64", ignoreCase = true)
                        }
                        "x86_64" -> apkList.firstOrNull { it.first.contains("x86_64", ignoreCase = true) }
                        "x86" -> apkList.firstOrNull { it.first.contains("x86", ignoreCase = true) && !it.first.contains("x86_64", ignoreCase = true) }
                        else -> null
                    }

                    downloadUrl = matched?.second ?: apkList.firstOrNull()?.second
                }
                if (downloadUrl == null) {
                    downloadUrl = htmlUrl
                }

                list.add(CubiqReleaseItem(tagName, title, body, publishedAt, htmlUrl, downloadUrl))
            }

            if (list.isNotEmpty()) {
                _releases.value = list
                checkUpdateAvailable(list.first())
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Error parsing GitHub releases: ${e.message}")
        }
    }

    private fun getTargetApkKeyword(): String {
        val arch = runCatching { BuildKeys.BUILD_ARCH.lowercase().trim() }.getOrDefault("")
        return when (arch) {
            "arm", "armeabi-v7a", "v7a" -> "armeabi-v7a"
            "arm64", "arm64-v8a", "aarch64" -> "arm64-v8a"
            "x86" -> "x86"
            "x86_64" -> "x86_64"
            else -> {
                val is64Bit = runCatching { android.os.Process.is64Bit() }.getOrDefault(true)
                val isX86 = Architecture.isx86Device()
                when {
                    isX86 && is64Bit -> "x86_64"
                    isX86 && !is64Bit -> "x86"
                    !isX86 && is64Bit -> "arm64-v8a"
                    else -> "armeabi-v7a"
                }
            }
        }
    }

    private fun checkUpdateAvailable(latest: CubiqReleaseItem) {
        val currentVer = BuildConfig.VERSION_NAME.removePrefix("v").trim()
        val latestVer = latest.tagName.removePrefix("v").trim()

        if (isVersionGreater(latestVer, currentVer)) {
            val ignoredVersion = AllSettings.lastIgnoredVersion.getValue()
            val latestCode = parseVersionToCode(latestVer)
            if (ignoredVersion == null || ignoredVersion != latestCode) {
                _newUpdate.value = latest
            }
        }
    }

    fun dismissUpdate(ignoreForVersion: Boolean = false) {
        val current = _newUpdate.value
        if (ignoreForVersion && current != null) {
            val code = parseVersionToCode(current.tagName.removePrefix("v").trim())
            AllSettings.lastIgnoredVersion.save(code)
        }
        _newUpdate.value = null
    }

    private fun parseVersionToCode(version: String): Int {
        val parts = version.split(".").mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }
        val major = parts.getOrNull(0) ?: 1
        val minor = parts.getOrNull(1) ?: 0
        val patch = parts.getOrNull(2) ?: 0
        return major * 100000 + minor * 1000 + patch
    }

    private fun isVersionGreater(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.filter { ch -> ch.isDigit() }.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    private fun loadCache() {
        runCatching {
            val cacheFile = File(PathManager.DIR_CACHE_HOME_PAGE, "cubiq_releases.json")
            if (cacheFile.exists()) {
                parseReleases(cacheFile.readText())
            }
        }
    }
}
