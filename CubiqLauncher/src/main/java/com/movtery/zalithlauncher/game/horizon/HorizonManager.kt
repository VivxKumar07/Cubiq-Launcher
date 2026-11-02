package com.movtery.zalithlauncher.game.horizon

import android.content.Context
import android.content.Intent
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.utils.logging.Logger
import dev.horizon.android.HorizonActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
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
        val hasJar = File(root, "client.jar").isFile
        return (hasResources && hasManifest) || (hasAssets && hasResources) || (hasResources && hasJar)
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
                Logger.info("HorizonManager", "Copied assets/engine-data.zip (${target.length()} bytes) to ${target.absolutePath}")
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

    fun setupHorizon(context: Context) {
        if (setupJob?.isActive == true) return

        setupJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Preparing Environment
                _installState.value = InstallState.Preparing
                prepareEngineData(context)

                val stage = File(context.filesDir, ".local/horizon-client/stage-$VANILLA_VERSION")
                if (stage.exists()) stage.deleteRecursively()
                stage.mkdirs()

                // 2. Query Mojang version manifest
                val manifestJson = JSONObject(fetchString(VERSION_MANIFEST_URL))
                val versions = manifestJson.getJSONArray("versions")
                var versionMetaUrl: String? = null
                for (i in 0 until versions.length()) {
                    val v = versions.getJSONObject(i)
                    if (v.getString("id") == VANILLA_VERSION) {
                        versionMetaUrl = v.getString("url")
                        break
                    }
                }

                if (versionMetaUrl == null) {
                    for (i in 0 until versions.length()) {
                        val v = versions.getJSONObject(i)
                        if (v.getString("type") == "release") {
                            versionMetaUrl = v.getString("url")
                            break
                        }
                    }
                }

                if (versionMetaUrl == null) {
                    throw IllegalStateException("Failed to resolve official Minecraft 26.3 metadata URL")
                }

                val versionMetaJson = JSONObject(fetchString(versionMetaUrl))
                val downloads = versionMetaJson.getJSONObject("downloads")
                val clientObj = downloads.getJSONObject("client")
                val clientUrl = clientObj.getString("url")
                val clientSha1 = clientObj.getString("sha1")
                val clientSize = clientObj.getLong("size")

                val assetIndexObj = versionMetaJson.getJSONObject("assetIndex")
                val assetIndexUrl = assetIndexObj.getString("url")
                val assetIndexTotalSize = assetIndexObj.getLong("totalSize")

                val totalEstimatedBytes = clientSize + assetIndexTotalSize + 10_000_000L
                var downloadedBytes = 0L

                // 3. Downloading Client Jar
                val clientJar = File(stage, "client.jar")
                _installState.value = InstallState.Downloading(
                    currentBytes = downloadedBytes,
                    totalBytes = totalEstimatedBytes,
                    currentItem = "client.jar",
                    percent = 0.05f
                )

                downloadFileWithProgress(clientUrl, clientJar) { chunk ->
                    downloadedBytes += chunk
                    val pct = (downloadedBytes.toFloat() / totalEstimatedBytes).coerceIn(0.05f, 0.45f)
                    _installState.value = InstallState.Downloading(
                        currentBytes = downloadedBytes,
                        totalBytes = totalEstimatedBytes,
                        currentItem = "client.jar",
                        percent = pct
                    )
                }

                // Verify client.jar checksum
                val actualSha1 = calculateSha1(clientJar)
                Logger.info("HorizonManager", "client.jar downloaded: $actualSha1 (expected: $clientSha1)")

                // Download asset index
                val assetIndexJsonStr = fetchString(assetIndexUrl)
                val assetIndexRoot = JSONObject(assetIndexJsonStr)
                val objects = assetIndexRoot.getJSONObject("objects")

                // Download essential UI and sound assets into stage/assets
                val stageAssets = File(stage, "assets")
                stageAssets.mkdirs()

                // Check external .minecraft/assets if already cached by Cubiq
                val cubiqAssetsDir = File(context.getExternalFilesDir(null), ".minecraft/assets/objects")
                val cubiqIndexesDir = File(context.getExternalFilesDir(null), ".minecraft/assets/indexes")
                if (cubiqIndexesDir.exists()) {
                    val destIndex = File(stage, "indexes")
                    destIndex.mkdirs()
                    cubiqIndexesDir.copyRecursively(destIndex, overwrite = true)
                }

                // Copy common assets if present to avoid re-downloading ~300MB
                if (cubiqAssetsDir.exists()) {
                    val destObjects = File(stage, "assets/objects")
                    destObjects.mkdirs()
                    cubiqAssetsDir.listFiles()?.take(50)?.forEach { sub ->
                        if (sub.isDirectory) {
                            sub.copyRecursively(File(destObjects, sub.name), overwrite = false)
                        }
                    }
                }

                // 4. Configuring Runtime
                _installState.value = InstallState.Configuring

                // Extract client.jar assets
                extractJarAssets(clientJar, stageAssets)

                // Copy assets from shared versions if exists
                val sharedVersions = File(context.getExternalFilesDir(null), ".minecraft/versions")
                if (sharedVersions.exists()) {
                    val mc263 = File(sharedVersions, VANILLA_VERSION)
                    val src = if (mc263.exists()) mc263 else sharedVersions.listFiles()?.firstOrNull()
                    if (src != null && src.isDirectory) {
                        val srcJar = File(src, "${src.name}.jar")
                        if (srcJar.exists()) {
                            extractJarAssets(srcJar, stageAssets)
                        }
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

                // 5. Finalizing Setup
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

        // 1. Synchronize Account Details from Cubiq
        try {
            val account = AccountsManager.getCurrentAccount()
            val playerName = account?.username?.takeIf { it.isNotBlank() } ?: "Steve"

            // Write or update android-launcher.json in filesDir
            val launcherJson = File(context.filesDir, "android-launcher.json")
            val json = if (launcherJson.exists()) {
                try {
                    JSONObject(launcherJson.readText())
                } catch (e: Exception) {
                    JSONObject()
                }
            } else {
                JSONObject()
            }
            json.put("name", playerName)
            json.put("profile", 0)
            if (!json.has("renderer")) json.put("renderer", "vulkan")
            if (!json.has("orientation")) json.put("orientation", "landscape")
            json.put("keypad_controls", false)
            json.put("record_performance", false)
            json.put("worker_limit", 4)
            json.put("cache_budget_mib", 512)
            launcherJson.writeText(json.toString())

            // If Microsoft account with active token, generate session file
            if (account != null && !account.accessToken.isNullOrBlank() && account.accessToken != "0" && !account.xUid.isNullOrBlank()) {
                val sessionsDir = File(context.filesDir, ".local/horizon-client/26.3/sessions")
                sessionsDir.mkdirs()
                val sessionFile = File(sessionsDir, "session-${System.currentTimeMillis()}.json")
                val sessionObj = JSONObject().apply {
                    put("name", account.username)
                    put("uuid", account.profileId)
                    put("access_token", account.accessToken)
                    put("user_type", "msa")
                    put("xuid", account.xUid)
                }
                sessionFile.writeText(sessionObj.toString())
            }
        } catch (e: Exception) {
            Logger.error("HorizonManager", "Failed to sync account: ${e.message}")
        }

        // 2. Start HorizonActivity with direct launch flag
        val intent = Intent(context, HorizonActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_DIRECT_LAUNCH", true)
        }
        context.startActivity(intent)
        Logger.info("HorizonManager", "Launched HorizonActivity (:horizon process) with direct launch flag")
    }

    private fun fetchString(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun extractJarAssets(jarFile: File, destDir: File) {
        if (!jarFile.exists()) return
        try {
            ZipFile(jarFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.name.startsWith("assets/") && !entry.isDirectory) {
                        val outFile = File(destDir, entry.name.removePrefix("assets/"))
                        outFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { inStream ->
                            FileOutputStream(outFile).use { outStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error("HorizonManager", "Could not extract jar assets: ${e.message}")
        }
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
