package com.movtery.zalithlauncher.game.horizon

import android.content.Context
import android.content.Intent
import com.movtery.zalithlauncher.utils.logging.Logger
import dev.horizon.android.HorizonActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object HorizonManager {
    const val VANILLA_VERSION = "26.3"
    const val PINNED_CLIENT_SHA1 = "e877b6a07acd633fb3bb475002175cec036e7b87"
    const val VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    sealed class InstallState {
        object Idle : InstallState()
        object Preparing : InstallState()
        data class Downloading(
            val currentBytes: Long,
            val totalBytes: Long,
            val currentItem: String,
            val percent: Float
        ) : InstallState()
        object Configuring : InstallState()
        object Finalizing : InstallState()
        object Success : InstallState()
        data class Failed(val error: String) : InstallState()
    }

    private val _installState = MutableStateFlow<InstallState>(InstallState.Idle)
    val installState = _installState.asStateFlow()

    private var setupJob: Job? = null

    fun getHorizonClientDir(context: Context): File {
        return File(context.filesDir, ".local/horizon-client/$VANILLA_VERSION")
    }

    fun isInstalled(context: Context): Boolean {
        val root = getHorizonClientDir(context)
        val hasResources = File(root, "horizon-resources.json").isFile
        val hasManifest = File(root, "android-downloads-v2.json").isFile
        val hasAssets = File(root, "assets").isDirectory
        return (hasResources && hasManifest) || (hasAssets && hasResources)
    }

    fun prepareEngineData(context: Context): Boolean {
        return try {
            val target = File(context.filesDir, "engine-data.zip")
            if (!target.exists() || target.length() == 0L) {
                target.parentFile?.mkdirs()
                context.assets.open("engine-data.zip").use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
                Logger.info("HorizonManager", "Extracted engine-data.zip to ${target.absolutePath}")
            }
            true
        } catch (e: Exception) {
            Logger.error("HorizonManager", "Failed to extract engine-data.zip: ${e.message}", e)
            false
        }
    }

    fun cancelSetup() {
        setupJob?.cancel()
        setupJob = null
        _installState.value = InstallState.Idle
    }

    fun resetState() {
        _installState.value = InstallState.Idle
    }

    fun startSetup(context: Context, scope: CoroutineScope) {
        if (_installState.value is InstallState.Preparing || _installState.value is InstallState.Downloading || _installState.value is InstallState.Configuring) {
            return
        }

        setupJob = scope.launch(Dispatchers.IO) {
            try {
                // 1. Preparing Environment
                _installState.value = InstallState.Preparing
                prepareEngineData(context)

                val downloadCache = File(context.filesDir, ".local/downloads/$VANILLA_VERSION")
                val objectsDir = File(downloadCache, "objects")
                downloadCache.mkdirs()
                objectsDir.mkdirs()

                val versionsDir = File(context.filesDir, ".local/horizon-client")
                versionsDir.mkdirs()
                val stage = File(versionsDir, "official.partial")
                if (stage.exists()) stage.deleteRecursively()
                stage.mkdirs()

                // 2. Downloading Version Manifest & Resolving Assets
                _installState.value = InstallState.Downloading(
                    currentBytes = 0L,
                    totalBytes = 500L * 1024 * 1024,
                    currentItem = "Fetching Minecraft 26.3 metadata...",
                    percent = 0.05f
                )

                val manifestV2Json = fetchString(VERSION_MANIFEST_URL)
                val manifestV2 = JSONObject(manifestV2Json)
                val versionsArray = manifestV2.getJSONArray("versions")
                var version263Url: String? = null
                for (i in 0 until versionsArray.length()) {
                    val v = versionsArray.getJSONObject(i)
                    if (v.getString("id") == VANILLA_VERSION) {
                        version263Url = v.getString("url")
                        break
                    }
                }

                if (version263Url == null) {
                    throw IllegalStateException("Mojang does not list Minecraft $VANILLA_VERSION in version manifest")
                }

                val versionJsonStr = fetchString(version263Url)
                val versionJson = JSONObject(versionJsonStr)

                // Client JAR
                val clientObj = versionJson.getJSONObject("downloads").getJSONObject("client")
                val clientUrl = clientObj.getString("url")
                val clientSha1 = clientObj.getString("sha1")
                val clientSize = clientObj.optLong("size", 30L * 1024 * 1024)

                // Asset Index
                val assetIndexObj = versionJson.getJSONObject("assetIndex")
                val assetIndexUrl = assetIndexObj.getString("url")
                val assetIndexJsonStr = fetchString(assetIndexUrl)
                val assetIndexJson = JSONObject(assetIndexJsonStr)
                val assetObjects = assetIndexJson.getJSONObject("objects")

                data class AssetFile(val name: String, val hash: String, val size: Long, val url: String)
                val assetList = mutableListOf<AssetFile>()
                var totalDownloadSize = clientSize

                val keys = assetObjects.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = assetObjects.getJSONObject(key)
                    val hash = obj.getString("hash")
                    val size = obj.getLong("size")
                    val sub = hash.substring(0, 2)
                    val url = "https://resources.download.minecraft.net/$sub/$hash"
                    assetList.add(AssetFile(key, hash, size, url))
                    totalDownloadSize += size
                }

                var downloadedBytes = 0L

                // Download client.jar
                val clientFile = File(downloadCache, "client.jar")
                if (!clientFile.exists() || calculateSha1(clientFile) != clientSha1) {
                    downloadFileWithProgress(clientUrl, clientFile) { chunk ->
                        downloadedBytes += chunk
                        _installState.value = InstallState.Downloading(
                            currentBytes = downloadedBytes,
                            totalBytes = totalDownloadSize,
                            currentItem = "Downloading client.jar (${(downloadedBytes / 1048576)} MB / ${(totalDownloadSize / 1048576)} MB)",
                            percent = (downloadedBytes.toFloat() / totalDownloadSize.coerceAtLeast(1L)).coerceIn(0f, 1f)
                        )
                    }
                } else {
                    downloadedBytes += clientSize
                }

                // Download Objects in Batches
                for (asset in assetList) {
                    val objFile = File(objectsDir, asset.hash)
                    if (!objFile.exists() || objFile.length() != asset.size) {
                        downloadFileWithProgress(asset.url, objFile) { chunk ->
                            downloadedBytes += chunk
                        }
                    } else {
                        downloadedBytes += asset.size
                    }

                    _installState.value = InstallState.Downloading(
                        currentBytes = downloadedBytes,
                        totalBytes = totalDownloadSize,
                        currentItem = "Downloading game assets (${(downloadedBytes / 1048576)} MB / ${(totalDownloadSize / 1048576)} MB)",
                        percent = (downloadedBytes.toFloat() / totalDownloadSize.coerceAtLeast(1L)).coerceIn(0.1f, 0.95f)
                    )
                }

                // 3. Configuring Runtime & Unpacking
                _installState.value = InstallState.Configuring

                // Unpack assets & data from client.jar
                ZipFile(clientFile).use { zip ->
                    val entries = zip.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val name = entry.name
                        if ((name.startsWith("assets/") || name.startsWith("data/")) && !entry.isDirectory) {
                            val outFile = File(stage, name)
                            outFile.parentFile?.mkdirs()
                            zip.getInputStream(entry).use { inStream ->
                                FileOutputStream(outFile).use { outStream ->
                                    inStream.copyTo(outStream)
                                }
                            }
                        }
                    }
                }

                // Copy asset objects to stage/assets/...
                val stageAssets = File(stage, "assets")
                for (asset in assetList) {
                    val src = File(objectsDir, asset.hash)
                    if (src.exists()) {
                        val dest = File(stageAssets, asset.name)
                        dest.parentFile?.mkdirs()
                        src.copyTo(dest, overwrite = true)
                    }
                }

                // Unpack engine-data.zip into stage
                val engineDataFile = File(context.filesDir, "engine-data.zip")
                if (engineDataFile.exists()) {
                    ZipFile(engineDataFile).use { zip ->
                        val entries = zip.entries()
                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            if (!entry.isDirectory) {
                                val outFile = File(stage, entry.name)
                                outFile.parentFile?.mkdirs()
                                zip.getInputStream(entry).use { inStream ->
                                    FileOutputStream(outFile).use { outStream ->
                                        inStream.copyTo(outStream)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Finalizing Setup
                _installState.value = InstallState.Finalizing

                File(stage, "android-downloads-v2.json").writeText("{\"schema\":2}")
                File(stage, "asset-index.json").writeText(assetIndexJsonStr)
                File(stage, "audio-complete.json").writeText("{}")

                val finalDir = getHorizonClientDir(context)
                if (finalDir.exists()) finalDir.deleteRecursively()
                stage.renameTo(finalDir)

                Logger.info("HorizonManager", "Setup completed successfully for Minecraft Rust 26.3 at ${finalDir.absolutePath}")
                _installState.value = InstallState.Success

            } catch (e: Exception) {
                Logger.error("HorizonManager", "Setup failed: ${e.message}", e)
                _installState.value = InstallState.Failed(e.message ?: "Unknown setup error")
            }
        }
    }

    fun launchHorizon(context: Context) {
        prepareEngineData(context)
        val intent = Intent(context, HorizonActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
        Logger.info("HorizonManager", "Launched HorizonActivity (:horizon process)")
    }

    private fun fetchString(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun downloadFileWithProgress(urlStr: String, dest: File, onChunk: (Long) -> Unit) {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true

        dest.parentFile?.mkdirs()
        val temp = File(dest.parentFile, "${dest.name}.tmp")

        conn.inputStream.use { input ->
            FileOutputStream(temp).use { output ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    onChunk(read.toLong())
                }
            }
        }
        temp.renameTo(dest)
    }

    fun calculateSha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
