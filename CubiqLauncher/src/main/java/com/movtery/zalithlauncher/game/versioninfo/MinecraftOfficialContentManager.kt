/*
 * Cubiq Launcher
 * Official Mojang Content Manager (News, Patch Notes, Latest Release)
 */

package com.movtery.zalithlauncher.game.versioninfo

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.GSON
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

data class OfficialNewsItem(
    val title: String,
    val category: String,
    val imageUrl: String,
    val readMoreUrl: String
)

data class OfficialPatchNote(
    val title: String,
    val version: String,
    val body: String,
    val imageUrl: String?,
    val contentPath: String? = null
)

object MinecraftOfficialContentManager {
    private const val TAG = "MinecraftContent"
    private const val URL_PATCH_NOTES = "https://launchercontent.mojang.com/v2/javaPatchNotes.json"
    private const val URL_NEWS = "https://launchercontent.mojang.com/v2/news.json"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _latestReleaseVersion = MutableStateFlow("26.3")
    val latestReleaseVersion = _latestReleaseVersion.asStateFlow()

    private val _latestHeroImageUrl = MutableStateFlow<String?>("https://launchercontent.mojang.com/v2/images/1-21-patchnotes.jpg")
    val latestHeroImageUrl = _latestHeroImageUrl.asStateFlow()

    private val _news = MutableStateFlow<List<OfficialNewsItem>>(emptyList())
    val news = _news.asStateFlow()

    private val _patchNotes = MutableStateFlow<List<OfficialPatchNote>>(emptyList())
    val patchNotes = _patchNotes.asStateFlow()

    init {
        loadCache()
        refresh()
    }

    fun refresh() {
        scope.launch {
            fetchContent()
        }
    }

    private suspend fun fetchContent() = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Patch Notes & Latest Version info from Mojang official servers
            val patchNotesJson = fetchStringFromUrls(listOf(URL_PATCH_NOTES))
            parsePatchNotes(patchNotesJson)
            runCatching {
                File(PathManager.DIR_CACHE_HOME_PAGE, "patch_notes.json").writeText(patchNotesJson)
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to fetch patch notes from Mojang: ${e.message}")
        }

        try {
            // 2. Fetch News from Mojang official servers
            val newsJson = fetchStringFromUrls(listOf(URL_NEWS))
            parseNews(newsJson)
            runCatching {
                File(PathManager.DIR_CACHE_HOME_PAGE, "news.json").writeText(newsJson)
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Failed to fetch news from Mojang: ${e.message}")
        }
    }

    fun fetchDetailedPatchNote(note: OfficialPatchNote) {
        val path = note.contentPath ?: return
        scope.launch {
            try {
                val fullUrl = if (path.startsWith("http")) path else "https://launchercontent.mojang.com/v2/$path"
                val json = fetchStringFromUrls(listOf(fullUrl))
                val obj = GSON.fromJson(json, JsonObject::class.java)
                val fullBody = obj.get("body")?.asString
                if (!fullBody.isNullOrBlank()) {
                    _patchNotes.value = _patchNotes.value.map {
                        if (it.version == note.version) it.copy(body = fullBody) else it
                    }
                }
            } catch (e: Exception) {
                Logger.warning(TAG, "Failed to load detailed patch note for ${note.version}: ${e.message}")
            }
        }
    }

    private fun parsePatchNotes(json: String) {
        try {
            val root = GSON.fromJson(json, JsonObject::class.java)
            val entries = root.getAsJsonArray("entries") ?: return
            val notes = mutableListOf<OfficialPatchNote>()

            for (elem in entries) {
                val obj = elem.asJsonObject
                val title = obj.get("title")?.asString ?: ""
                val version = obj.get("version")?.asString ?: ""
                val shortText = obj.get("shortText")?.asString ?: ""
                val body = obj.get("body")?.asString?.ifBlank { null } ?: shortText
                val contentPath = obj.get("contentPath")?.asString
                val imgObj = obj.getAsJsonObject("image")
                val imgUrl = imgObj?.get("url")?.asString?.let {
                    if (it.startsWith("http")) it else "https://launchercontent.mojang.com$it"
                }

                notes.add(OfficialPatchNote(title, version, body, imgUrl, contentPath))
            }

            if (notes.isNotEmpty()) {
                _patchNotes.value = notes
                val firstRelease = notes.firstOrNull { note ->
                    !note.version.contains("snapshot", ignoreCase = true) &&
                    !note.version.contains("rc", ignoreCase = true) &&
                    !note.version.contains("pre", ignoreCase = true)
                } ?: notes.first()

                _latestReleaseVersion.value = firstRelease.version
                if (!firstRelease.imageUrl.isNullOrBlank()) {
                    _latestHeroImageUrl.value = firstRelease.imageUrl
                }

                // Asynchronously fetch details for the first 3 patch notes
                notes.take(3).forEach { note ->
                    if (!note.contentPath.isNullOrBlank()) {
                        fetchDetailedPatchNote(note)
                    }
                }
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Error parsing patch notes: ${e.message}")
        }
    }

    private fun parseNews(json: String) {
        try {
            val root = GSON.fromJson(json, JsonObject::class.java)
            val entries = root.getAsJsonArray("entries") ?: return
            val newsList = mutableListOf<OfficialNewsItem>()

            for (elem in entries) {
                val obj = elem.asJsonObject
                val title = obj.get("title")?.asString ?: ""
                val category = obj.get("category")?.asString ?: "Minecraft"
                val readMore = obj.get("readMoreLink")?.asString ?: "https://www.minecraft.net"
                val playImg = obj.getAsJsonObject("playPageImage")
                val newsImg = obj.getAsJsonObject("newsPageImage")
                val relImg = playImg?.get("url")?.asString ?: newsImg?.get("url")?.asString
                val fullImg = if (relImg != null) {
                    if (relImg.startsWith("http")) relImg else "https://launchercontent.mojang.com$relImg"
                } else ""

                newsList.add(OfficialNewsItem(title, category, fullImg, readMore))
            }

            if (newsList.isNotEmpty()) {
                _news.value = newsList
            }
        } catch (e: Exception) {
            Logger.warning(TAG, "Error parsing news: ${e.message}")
        }
    }

    private fun loadCache() {
        runCatching {
            val cacheFileNotes = File(PathManager.DIR_CACHE_HOME_PAGE, "patch_notes.json")
            if (cacheFileNotes.exists()) parsePatchNotes(cacheFileNotes.readText())

            val cacheFileNews = File(PathManager.DIR_CACHE_HOME_PAGE, "news.json")
            if (cacheFileNews.exists()) parseNews(cacheFileNews.readText())
        }
    }
}
