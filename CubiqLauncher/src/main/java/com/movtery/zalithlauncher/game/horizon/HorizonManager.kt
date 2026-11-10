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

    private val WORLDGEN_TABLES = listOf(
        "noises" to "worldgen/noise",
        "densities" to "worldgen/density_function",
        "rules" to "worldgen/material_rule",
        "conditions" to "worldgen/material_condition",
        "settings" to "worldgen/noise_settings",
        "carvers" to "worldgen/carver",
        "biomes" to "worldgen/biome",
        "world_presets" to "worldgen/world_preset",
        "features" to "worldgen/feature",
        "placed_features" to "worldgen/placed_feature",
        "state_providers" to "worldgen/block_state_provider",
        "biome_tags" to "tags/worldgen/biome",
        "feature_tags" to "tags/worldgen/feature",
        "block_tags" to "tags/block",
        "fluid_tags" to "tags/fluid",
        "processor_lists" to "worldgen/processor_list",
        "template_pools" to "worldgen/template_pool",
        "structures" to "worldgen/structure",
        "structure_sets" to "worldgen/structure_set"
    )

    private val ROOT_TAG_TABLES = listOf(
        "block-tags" to "tags/block",
        "damage-tags" to "tags/damage_type",
        "damage-types" to "damage_type"
    )

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

    fun unpackEngineData(context: Context, targetDir: File): Boolean {
        return try {
            targetDir.mkdirs()
            val engineDataFile = File(context.filesDir, "engine-data.zip")
            if (!engineDataFile.exists() || engineDataFile.length() == 0L) {
                prepareEngineData(context)
            }
            val zipStream = if (engineDataFile.exists() && engineDataFile.length() > 0) {
                java.io.FileInputStream(engineDataFile)
            } else {
                context.assets.open("engine-data.zip")
            }
            java.util.zip.ZipInputStream(zipStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(targetDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            Logger.info("HorizonManager", "Unpacked engine data tables to ${targetDir.absolutePath}")
            true
        } catch (e: Exception) {
            Logger.error("HorizonManager", "Failed to unpack engine data tables: ${e.message}", e)
            false
        }
    }

    fun isInstalled(context: Context): Boolean {
        val root = getHorizonClientDir(context)
        val hasResources = File(root, "horizon-resources.json").isFile
        val hasBlockTags = File(root, "block-tags.json").isFile
        val hasDamageTags = File(root, "damage-tags.json").isFile
        val hasWorldgen = File(root, "worldgen/block_tags.json").isFile
        val hasTextures = File(root, "assets/minecraft/textures").isDirectory
        val hasModels = File(root, "assets/minecraft/models").isDirectory
        val hasManifest = File(root, "android-downloads-v2.json").isFile

        return hasResources && hasBlockTags && hasDamageTags && hasWorldgen && hasTextures && hasModels && hasManifest
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

    fun findLocalClientJar(context: Context): File? {
        val candidates = mutableListOf<File>()

        candidates.add(File(getHorizonClientDir(context), "client.jar"))
        candidates.add(File(context.filesDir, "client.jar"))

        val searchDirs = mutableListOf<File>()
        searchDirs.add(File(context.getExternalFilesDir(null), ".minecraft/versions"))
        searchDirs.add(File(com.movtery.zalithlauncher.game.path.GamePathManager.currentPath.value, "versions"))
        searchDirs.add(File(context.filesDir, ".minecraft/versions"))

        for (versionsDir in searchDirs) {
            if (versionsDir.exists() && versionsDir.isDirectory) {
                candidates.add(File(versionsDir, "$VANILLA_VERSION/$VANILLA_VERSION.jar"))
                candidates.add(File(versionsDir, "$VANILLA_VERSION/client.jar"))

                versionsDir.listFiles()?.forEach { dir ->
                    if (dir.isDirectory && dir.name.contains(VANILLA_VERSION)) {
                        dir.listFiles()?.forEach { file ->
                            if (file.isFile && file.name.endsWith(".jar") && file.length() > 5_000_000L) {
                                candidates.add(file)
                            }
                        }
                    }
                }

                versionsDir.listFiles()?.forEach { dir ->
                    if (dir.isDirectory && !dir.name.contains(VANILLA_VERSION)) {
                        dir.listFiles()?.forEach { file ->
                            if (file.isFile && file.name.endsWith(".jar") && file.length() > 20_000_000L) {
                                candidates.add(file)
                            }
                        }
                    }
                }
            }
        }

        return candidates.firstOrNull { it.exists() && it.isFile && it.length() > 5_000_000L }
    }

    fun prepareResourcesFromJar(
        context: Context,
        clientJar: File,
        targetDir: File,
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ) {
        targetDir.mkdirs()

        // 1. Unpack engine-data.zip tables
        onProgress("Unpacking engine data tables...", 0.05f)
        unpackEngineData(context, targetDir)
        prepareEngineData(context)

        // 2. Extract client.jar assets and data preserving hierarchy
        onProgress("Extracting Minecraft client assets & data...", 0.15f)
        ZipFile(clientJar).use { zip ->
            val total = zip.size()
            var count = 0
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                count++
                val name = entry.name
                if ((name.startsWith("assets/") || name.startsWith("data/")) && !entry.isDirectory) {
                    val outFile = File(targetDir, name)
                    outFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { inStream ->
                        FileOutputStream(outFile).use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
                if (count % 400 == 0) {
                    val pct = 0.15f + 0.45f * (count.toFloat() / total.coerceAtLeast(1))
                    onProgress("Extracting client assets ($count / $total)...", pct)
                }
            }
        }

        // 3. Rebuild aggregated tables
        onProgress("Aggregating block tags and worldgen tables...", 0.65f)
        rebuildAggregatedResources(targetDir) { step, pct ->
            onProgress(step, 0.65f + 0.25f * pct)
        }

        // 4. Save copy of client.jar
        val destJar = File(targetDir, "client.jar")
        if (!destJar.exists() || destJar.length() != clientJar.length()) {
            try {
                clientJar.copyTo(destJar, overwrite = true)
            } catch (ignored: Exception) {}
        }

        // 5. Finalize metadata markers
        onProgress("Finalizing metadata markers...", 0.95f)
        File(targetDir, "android-downloads-v2.json").writeText("{\"schema\":2}", Charsets.UTF_8)
        File(targetDir, "audio-complete.json").writeText("{}", Charsets.UTF_8)

        Logger.info("HorizonManager", "Completed preparation of Horizon resources in ${targetDir.absolutePath}")
    }

    private fun rebuildAggregatedResources(root: File, onStep: (String, Float) -> Unit) {
        val totalSteps = WORLDGEN_TABLES.size + ROOT_TAG_TABLES.size + 1
        var current = 0

        // Root tables (block-tags, damage-tags, damage-types)
        for ((name, source) in ROOT_TAG_TABLES) {
            current++
            onStep("Building $name...", current.toFloat() / totalSteps)
            aggregate(root, source, "$name.json")
        }

        // Worldgen tables
        for ((name, source) in WORLDGEN_TABLES) {
            current++
            onStep("Building worldgen/$name...", current.toFloat() / totalSteps)
            aggregate(root, source, "worldgen/$name.json")
        }

        // Copy structure NBTs
        current++
        onStep("Copying structure templates...", current.toFloat() / totalSteps)
        val structureBase = File(root, "data/minecraft/structure")
        if (structureBase.exists() && structureBase.isDirectory) {
            val destBase = File(root, "worldgen/structure/minecraft")
            val nbtFiles = mutableListOf<File>()
            walkFiles(structureBase, nbtFiles)
            for (file in nbtFiles) {
                if (file.name.endsWith(".nbt")) {
                    val rel = file.relativeTo(structureBase).path
                    val dest = File(destBase, rel)
                    dest.parentFile?.mkdirs()
                    try {
                        file.copyTo(dest, overwrite = true)
                    } catch (e: Exception) {
                        Logger.warning("HorizonManager", "Could not copy NBT $file: ${e.message}")
                    }
                }
            }
        }
    }

    private fun aggregate(root: File, source: String, targetRelPath: String) {
        val base = File(root, "data/minecraft/$source")
        val targetFile = File(root, targetRelPath)
        targetFile.parentFile?.mkdirs()

        if (!base.exists() || !base.isDirectory) {
            if (!targetFile.exists()) {
                targetFile.writeText("{}", Charsets.UTF_8)
            }
            return
        }

        val files = mutableListOf<File>()
        walkFiles(base, files)
        files.sortBy { it.absolutePath }

        val map = JSONObject()
        for (file in files) {
            if (!file.name.endsWith(".json")) continue
            val rel = file.relativeTo(base).path.removeSuffix(".json").replace('\\', '/')
            val key = "minecraft:$rel"
            try {
                val content = file.readText(Charsets.UTF_8)
                map.put(key, content)
            } catch (e: Exception) {
                Logger.warning("HorizonManager", "Failed to aggregate $file: ${e.message}")
            }
        }
        targetFile.writeText(map.toString(), Charsets.UTF_8)
    }

    private fun walkFiles(dir: File, list: MutableList<File>) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                walkFiles(file, list)
            } else {
                list.add(file)
            }
        }
    }

    fun recheckFiles(context: Context) {
        setupHorizon(context)
    }

    fun setupHorizon(context: Context) {
        if (setupJob?.isActive == true) return

        setupJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                _installState.value = InstallState.Preparing
                prepareEngineData(context)

                val targetDir = getHorizonClientDir(context)
                val stage = File(context.filesDir, ".local/horizon-client/stage-$VANILLA_VERSION")
                if (stage.exists()) stage.deleteRecursively()
                stage.mkdirs()

                // Check if local client jar already exists
                val localJar = findLocalClientJar(context)
                val clientJar = if (localJar != null && localJar.exists()) {
                    Logger.info("HorizonManager", "Found local client jar at ${localJar.absolutePath}, skipping download")
                    localJar
                } else {
                    // Download official Minecraft 26.3 client jar
                    _installState.value = InstallState.Downloading(
                        currentBytes = 0,
                        totalBytes = 35_000_000L,
                        currentItem = "Resolving Minecraft $VANILLA_VERSION...",
                        percent = 0.05f
                    )

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
                    val clientSize = clientObj.getLong("size")

                    val downloadedJar = File(stage, "client.jar")
                    var downloadedBytes = 0L

                    downloadFileWithProgress(clientUrl, downloadedJar) { chunk ->
                        downloadedBytes += chunk
                        val pct = (downloadedBytes.toFloat() / clientSize).coerceIn(0.05f, 0.45f)
                        _installState.value = InstallState.Downloading(
                            currentBytes = downloadedBytes,
                            totalBytes = clientSize,
                            currentItem = "client.jar",
                            percent = pct
                        )
                    }
                    downloadedJar
                }

                // Configuring / Preparing resources
                _installState.value = InstallState.Configuring
                prepareResourcesFromJar(context, clientJar, stage) { step, pct ->
                    _installState.value = InstallState.Downloading(
                        currentBytes = (pct * 100).toLong(),
                        totalBytes = 100L,
                        currentItem = step,
                        percent = pct
                    )
                }

                // Finalizing
                _installState.value = InstallState.Finalizing
                if (targetDir.exists()) targetDir.deleteRecursively()
                stage.renameTo(targetDir)

                Logger.info("HorizonManager", "Setup completed successfully at ${targetDir.absolutePath}")
                _installState.value = InstallState.Success

            } catch (e: Exception) {
                Logger.error("HorizonManager", "Setup failed: ${e.message}", e)
                _installState.value = InstallState.Failed(e.message ?: "Unknown setup error")
            }
        }
    }

    fun ensureAssetsAndTouch(context: Context) {
        try {
            val root = getHorizonClientDir(context)
            root.mkdirs()

            // 1. Ensure Title Background Panorama faces exist (panorama_0.png .. panorama_5.png) with official Mojang textures
            val panoDir = File(root, "assets/minecraft/textures/gui/title/background")
            panoDir.mkdirs()
            val resPanoDir = File(context.filesDir, "resources/assets/minecraft/textures/gui/title/background")
            resPanoDir.mkdirs()

            for (i in 0..5) {
                val f1 = File(panoDir, "panorama_$i.png")
                val f2 = File(resPanoDir, "panorama_$i.png")
                val needUpdate1 = !f1.exists() || f1.length() < 20_000L
                val needUpdate2 = !f2.exists() || f2.length() < 20_000L

                if (needUpdate1) {
                    try {
                        context.assets.open("panorama/panorama_$i.png").use { input ->
                            FileOutputStream(f1).use { out -> input.copyTo(out) }
                        }
                    } catch (e: Throwable) {
                        Logger.warning("HorizonManager", "Could not copy panorama_$i: ${e.message}")
                    }
                }
                if (needUpdate2) {
                    try {
                        if (f1.exists() && f1.length() >= 20_000L) {
                            f1.copyTo(f2, overwrite = true)
                        } else {
                            context.assets.open("panorama/panorama_$i.png").use { input ->
                                FileOutputStream(f2).use { out -> input.copyTo(out) }
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }

            // Also copy panorama_overlay.png
            val ov1 = File(panoDir, "panorama_overlay.png")
            val ov2 = File(resPanoDir, "panorama_overlay.png")
            if (!ov1.exists() || ov1.length() < 10L) {
                runCatching { context.assets.open("panorama/panorama_overlay.png").use { it.copyTo(FileOutputStream(ov1)) } }
            }
            if (!ov2.exists() || ov2.length() < 10L) {
                runCatching { context.assets.open("panorama/panorama_overlay.png").use { it.copyTo(FileOutputStream(ov2)) } }
            }

            // 2. Ensure official horizon-resources.json exists with valid data hashes
            val resourcesJson = File(root, "horizon-resources.json")
            val resResourcesJson = File(context.filesDir, "resources/horizon-resources.json")
            try {
                val fullRes = context.assets.open("horizon/horizon-resources.json").use { it.reader().readText() }
                if (!resourcesJson.exists() || !resourcesJson.readText().contains("client_sha256")) {
                    resourcesJson.writeText(fullRes, Charsets.UTF_8)
                }
                if (!resResourcesJson.exists() || !resResourcesJson.readText().contains("client_sha256")) {
                    resResourcesJson.parentFile?.mkdirs()
                    resResourcesJson.writeText(fullRes, Charsets.UTF_8)
                }
            } catch (e: Throwable) {
                Logger.warning("HorizonManager", "Could not copy horizon-resources.json: ${e.message}")
            }

            // 3. Deploy all 60 official Bedrock touch controls textures (dismount, dpad, attack, jump, etc.)
            val bedrockTarget = File(root, "assets/minecraft/textures/gui/bedrock")
            val resBedrockTarget = File(context.filesDir, "resources/assets/minecraft/textures/gui/bedrock")
            bedrockTarget.mkdirs()
            resBedrockTarget.mkdirs()
            try {
                copyAssetFolder(context, "horizon/bedrock", bedrockTarget)
                copyAssetFolder(context, "horizon/bedrock", resBedrockTarget)
            } catch (e: Throwable) {
                Logger.warning("HorizonManager", "Could not copy Bedrock touch assets: ${e.message}")
            }

            // 4. Disable native Bedrock touch controls
            val touchJsonFiles = listOf(File(context.filesDir, "horizon-touch.json"), File(root, "horizon-touch.json"))
            val disabledTouchConfig = JSONObject().apply {
                put("schema", 1)
                put("elements", org.json.JSONArray())
            }.toString()
            for (f in touchJsonFiles) {
                f.writeText(disabledTouchConfig, Charsets.UTF_8)
            }

            // 5. Ensure options.txt has proper mobile GUI scale (guiScale: 3) so UI elements are finger-friendly
            val optionsFiles = listOf(File(context.filesDir, "options.txt"), File(root, "options.txt"))
            for (optFile in optionsFiles) {
                var content = if (optFile.exists()) optFile.readText() else ""
                if (content.contains("guiScale:")) {
                    content = content.replace(Regex("guiScale:\\s*\\d+"), "guiScale:3")
                } else {
                    content += "\nguiScale:3\n"
                }
                optFile.writeText(content.trim() + "\n")
            }

            // 6. Ensure resources directory beside executable or root
            val resourcesDir = File(context.filesDir, "resources")
            if (!resourcesDir.exists()) {
                try {
                    java.nio.file.Files.createSymbolicLink(resourcesDir.toPath(), root.toPath())
                } catch (_: Throwable) {
                    try { root.copyRecursively(resourcesDir, overwrite = false) } catch (_: Throwable) {}
                }
            }

            // 7. Ensure Minecraft sound assets and sounds.json are mapped
            ensureSoundAssets(context, root)
        } catch (e: Throwable) {
            Logger.warning("HorizonManager", "ensureAssetsAndTouch failed: ${e.message}")
        }
    }

    private fun ensureSoundAssets(context: Context, root: File) {
        try {
            val mcAssetsDir = File(root, "assets/minecraft")
            val soundsJson = File(mcAssetsDir, "sounds.json")
            val soundsDir = File(mcAssetsDir, "sounds")

            val searchDirs = listOf(
                File(com.movtery.zalithlauncher.game.path.GamePathManager.currentPath.value, "assets"),
                File(context.getExternalFilesDir(null), ".minecraft/assets"),
                File(context.filesDir, ".minecraft/assets")
            )

            var foundIndex: File? = null
            var objectsDir: File? = null

            for (assetsBase in searchDirs) {
                if (assetsBase.exists() && assetsBase.isDirectory) {
                    val idxDir = File(assetsBase, "indexes")
                    val objDir = File(assetsBase, "objects")
                    if (idxDir.exists() && objDir.exists()) {
                        val indexes = idxDir.listFiles()
                        if (!indexes.isNullOrEmpty()) {
                            foundIndex = indexes.maxByOrNull { it.length() }
                            objectsDir = objDir
                            break
                        }
                    }
                }
            }

            if (foundIndex != null && objectsDir != null && foundIndex.exists()) {
                val indexJson = JSONObject(foundIndex.readText(Charsets.UTF_8))
                if (indexJson.has("objects")) {
                    val objects = indexJson.getJSONObject("objects")
                    mcAssetsDir.mkdirs()
                    soundsDir.mkdirs()

                    if (!soundsJson.exists() || soundsJson.length() == 0L) {
                        if (objects.has("minecraft/sounds.json")) {
                            val hash = objects.getJSONObject("minecraft/sounds.json").getString("hash")
                            val objFile = File(objectsDir, "${hash.substring(0, 2)}/$hash")
                            if (objFile.exists()) {
                                try {
                                    java.nio.file.Files.createSymbolicLink(soundsJson.toPath(), objFile.toPath())
                                } catch (_: Throwable) {
                                    objFile.copyTo(soundsJson, overwrite = true)
                                }
                            }
                        }
                    }

                    val keys = objects.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.startsWith("minecraft/sounds/")) {
                            val relSound = key.removePrefix("minecraft/sounds/")
                            val targetSound = File(soundsDir, relSound)
                            if (!targetSound.exists() || targetSound.length() == 0L) {
                                val hash = objects.getJSONObject(key).getString("hash")
                                val objFile = File(objectsDir, "${hash.substring(0, 2)}/$hash")
                                if (objFile.exists()) {
                                    targetSound.parentFile?.mkdirs()
                                    try {
                                        java.nio.file.Files.createSymbolicLink(targetSound.toPath(), objFile.toPath())
                                    } catch (_: Throwable) {
                                        objFile.copyTo(targetSound, overwrite = true)
                                    }
                                }
                            }
                        }
                    }
                    Logger.info("HorizonManager", "Mapped sound assets from ${foundIndex.name}")
                }
            }
        } catch (e: Throwable) {
            Logger.warning("HorizonManager", "ensureSoundAssets: ${e.message}")
        }
    }

    fun syncHorizonPaths(context: Context) {
        try {
            val filesDir = context.filesDir
            val parentDir = filesDir.parentFile
            if (parentDir != null && parentDir.exists()) {
                val parentLocal = File(parentDir, ".local")
                val filesLocal = File(filesDir, ".local")
                if (filesLocal.exists() && !parentLocal.exists()) {
                    try {
                        java.nio.file.Files.createSymbolicLink(parentLocal.toPath(), filesLocal.toPath())
                    } catch (ignored: Throwable) {
                        try { filesLocal.copyRecursively(parentLocal, overwrite = false) } catch (ignored2: Throwable) {}
                    }
                }
                val parentLauncher = File(parentDir, "android-launcher.json")
                val filesLauncher = File(filesDir, "android-launcher.json")
                if (filesLauncher.exists() && !parentLauncher.exists()) {
                    try { filesLauncher.copyTo(parentLauncher, overwrite = true) } catch (ignored: Throwable) {}
                }
            }
        } catch (e: Throwable) {
            Logger.warning("HorizonManager", "syncHorizonPaths failed: ${e.message}")
        }
    }

    fun launchHorizon(context: Context) {
        prepareEngineData(context)

        val targetDir = getHorizonClientDir(context)
        if (targetDir.exists()) {
            unpackEngineData(context, targetDir)
        }
        // If not completely installed, but local jar is available, auto-prepare immediately!
        if (!isInstalled(context)) {
            val localJar = findLocalClientJar(context)
            if (localJar != null) {
                Logger.info("HorizonManager", "Auto-preparing Horizon resources from ${localJar.absolutePath}")
                prepareResourcesFromJar(context, localJar, targetDir) { step, _ ->
                    Logger.info("HorizonManager", "Preparation: $step")
                }
            }
        }

        ensureAssetsAndTouch(context)
        syncHorizonPaths(context)

        // 1. Synchronize Account Details from Cubiq
        try {
            val account = AccountsManager.getCurrentAccount()
            val playerName = account?.username?.takeIf { it.isNotBlank() } ?: "Steve"

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
            json.put("input_host", "keyboard-mouse")
            json.put("keypad_controls", false)
            json.put("record_performance", false)
            json.put("worker_limit", 4)
            json.put("cache_budget_mib", 512)
            launcherJson.writeText(json.toString())

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

        // Clear any stale android-error.txt before launch
        try {
            val errFile = File(context.filesDir, "android-error.txt")
            if (errFile.exists()) errFile.delete()
        } catch (ignored: Exception) {}

        // 2. Start HorizonActivity with direct launch flag
        val controlName = com.movtery.zalithlauncher.setting.AllSettings.controlLayout.getValue()
        val layoutFile = java.io.File(com.movtery.zalithlauncher.path.PathManager.DIR_CONTROL_LAYOUTS, controlName)
        val alpha = (com.movtery.zalithlauncher.setting.AllSettings.controlsOpacity.getValue().toFloat() / 100f).coerceIn(0.1f, 1f)

        val intent = Intent(context, HorizonActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_DIRECT_LAUNCH", true)
            putExtra("EXTRA_CONTROL_LAYOUT_PATH", layoutFile.absolutePath)
            putExtra("EXTRA_CONTROL_LAYOUT_NAME", controlName)
            putExtra("EXTRA_CONTROLS_ALPHA", alpha)
        }
        context.startActivity(intent)
        Logger.info("HorizonManager", "Launched HorizonActivity (:horizon process) with layout ${layoutFile.name}")
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

    private fun copyAssetFolder(context: Context, assetPath: String, targetDir: File) {
        val list = context.assets.list(assetPath) ?: return
        targetDir.mkdirs()
        for (item in list) {
            val subAsset = if (assetPath.isEmpty()) item else "$assetPath/$item"
            val subList = context.assets.list(subAsset)
            if (subList != null && subList.isNotEmpty()) {
                val subTarget = File(targetDir, item)
                copyAssetFolder(context, subAsset, subTarget)
            } else {
                val targetFile = File(targetDir, item)
                if (!targetFile.exists() || targetFile.length() == 0L) {
                    try {
                        context.assets.open(subAsset).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } catch (ignored: Throwable) {}
                }
            }
        }
    }
}
