/*
 * Cubiq Launcher
 * MojoLauncher-style On-Demand JRE Downloader & Custom JRE Importer
 * Fetches universal + binpack components reliably without crashing.
 */

package com.movtery.zalithlauncher.game.multirt

import android.content.Context
import android.net.Uri
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.FileUtils

object JreDownloadManager {
    private const val TAG = "JreDownloadManager"

    // MojoLauncher Official JRE Component Distribution
    private const val MOJO_JRE_BASE = "https://mojolauncher.github.io/jre-download/components"
    private const val MOJO_ROLLING_BASE = "https://github.com/MojoLauncher/android-openjdk-build-multiarch/releases/download/rolling"
    private val PROXY_PREFIXES = listOf("", "https://mirror.ghproxy.com/", "https://ghproxy.net/")

    /**
     * Downloads and installs the requested Java Runtime on-demand if missing.
     * Uses MojoLauncher's universal + binpack architecture.
     * @param context Application/Activity context
     * @param runtimeName Name of runtime, e.g. "Internal-8", "Internal-17", "Internal-21", "Internal-25"
     */
    suspend fun downloadAndInstallJre(
        context: Context,
        runtimeName: String,
        onProgress: (progress: Int, message: String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        val jreEnum = Jre.entries.firstOrNull { it.jreName == runtimeName }
        val majorVersion = jreEnum?.majorVersion ?: runtimeName.filter { it.isDigit() }.toIntOrNull() ?: 17

        val arch = when (ZLApplication.DEVICE_ARCHITECTURE) {
            Architecture.ARCH_ARM64 -> "arm64"
            Architecture.ARCH_ARM -> "arm"
            Architecture.ARCH_X86_64 -> "x86_64"
            Architecture.ARCH_X86 -> "x86"
            else -> "arm64"
        }

        val dest = File(PathManager.DIR_MULTIRT, runtimeName)

        // 1. Check local offline backups first (e.g. offline_runtimes_backup directory or assets)
        val localFolderName = when (majorVersion) {
            8 -> "jre-8"
            17 -> "jre-17"
            21 -> "jre-21"
            25 -> "jre-25"
            else -> "jre-$majorVersion"
        }
        val possibleBackupDirs = listOfNotNull(
            File(context.filesDir, "offline_runtimes_backup/$localFolderName"),
            File(context.getExternalFilesDir(null), "offline_runtimes_backup/$localFolderName"),
            File(PathManager.DIR_FILES_EXTERNAL, "offline_runtimes_backup/$localFolderName")
        )

        for (bDir in possibleBackupDirs) {
            val localUniversal = File(bDir, "universal.tar.xz")
            val localBin = File(bDir, "bin-$arch.tar.xz")
            if (localUniversal.exists() && localBin.exists() && localUniversal.length() > 1000L && localBin.length() > 1000L) {
                Logger.info(TAG, "Found local JRE backup in ${bDir.absolutePath}, extracting...")
                onProgress(50, "Extracting local Java Runtime...")
                try {
                    localUniversal.inputStream().use { uStream ->
                        localBin.inputStream().use { bStream ->
                            RuntimesManager.installRuntimeBinPack(
                                universalFileInputStream = uStream,
                                platformBinsInputStream = bStream,
                                name = runtimeName,
                                binPackVersion = "1.0.0",
                                updateProgress = { p, _ -> onProgress(50 + p / 2, "Unpacking files...") }
                            )
                        }
                    }
                    postInstallFixup(dest, majorVersion, arch)
                    if (!RuntimesManager.isRuntimeValid(dest)) {
                        Logger.error(TAG, "Runtime $runtimeName extracted from local backup failed validation!")
                        FileUtils.deleteDirectory(dest)
                        return@withContext false
                    }
                    RuntimesManager.forceReload(runtimeName)
                    onProgress(100, "Java Runtime ready!")
                    return@withContext true
                } catch (e: Exception) {
                    Logger.warning(TAG, "Failed to unpack local backup JRE", e)
                }
            }
        }

        // 2. Online MojoLauncher Fetch
        val tempDir = File(context.cacheDir, "jre_downloads").apply { mkdirs() }

        if (majorVersion == 8) {
            // Java 8 is packaged as a single archive or rolling release
            val tempZip = File(tempDir, "jre8_${arch}.zip")
            val candidateUrls = getJava8CandidateUrls(arch)
            var downloadSuccess = false

            for (url in candidateUrls) {
                Logger.info(TAG, "Attempting to download JRE 8 from $url")
                onProgress(10, "Downloading Java 8 runtime...")
                if (downloadFile(url, tempZip, onProgress)) {
                    downloadSuccess = true
                    break
                }
            }

            if (!downloadSuccess || !tempZip.exists() || tempZip.length() < 10000L) {
                Logger.error(TAG, "Failed to download JRE 8")
                tempZip.delete()
                return@withContext false
            }

            onProgress(85, "Installing Java 8 Runtime...")
            try {
                var extracted = false
                // Check if tempZip is a zip containing binpack archives (like jre8-pojav.zip containing universal.tar.xz and bin-$arch.tar.xz)
                runCatching {
                    java.util.zip.ZipFile(tempZip).use { zf ->
                        val uEntry = zf.getEntry("universal.tar.xz")
                        val bEntry = zf.getEntry("bin-$arch.tar.xz")
                            ?: (if (arch == "arm64") zf.getEntry("bin-arm64.tar.xz") else null)
                            ?: (if (arch == "arm") zf.getEntry("bin-arm.tar.xz") else null)
                            ?: (if (arch == "x86_64") zf.getEntry("bin-x86_64.tar.xz") else null)
                            ?: (if (arch == "x86") zf.getEntry("bin-x86.tar.xz") else null)
                        if (uEntry != null && bEntry != null) {
                            Logger.info(TAG, "Unpacking Java 8 multi-arch binpack from zip...")
                            zf.getInputStream(uEntry).use { uStream ->
                                zf.getInputStream(bEntry).use { bStream ->
                                    RuntimesManager.installRuntimeBinPack(
                                        universalFileInputStream = uStream,
                                        platformBinsInputStream = bStream,
                                        name = runtimeName,
                                        binPackVersion = "1.0.0",
                                        updateProgress = { p, _ -> onProgress(85 + (p * 15 / 100), "Unpacking files...") }
                                    )
                                }
                            }
                            extracted = true
                        }
                    }
                }

                if (!extracted) {
                    val nativeLibDir = context.applicationInfo.nativeLibraryDir
                    tempZip.inputStream().use { stream ->
                        RuntimesManager.installRuntime(
                            nativeLibDir = nativeLibDir,
                            inputStream = stream,
                            name = runtimeName,
                            updateProgress = { p, _ -> onProgress(85 + (p * 15 / 100), "Unpacking files...") }
                        )
                    }
                }
                postInstallFixup(dest, 8, arch)
                if (!RuntimesManager.isRuntimeValid(dest)) {
                    Logger.error(TAG, "Java 8 runtime $runtimeName failed validation after installation!")
                    FileUtils.deleteDirectory(dest)
                    return@withContext false
                }
                RuntimesManager.forceReload(runtimeName)
                onProgress(100, "Java 8 ready!")
                return@withContext true
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to extract JRE 8", e)
                return@withContext false
            } finally {
                tempZip.delete()
            }
        }

        // Java 17, 21, 25: Universal archive + Arch-specific binpack
        val mojoFolder = when (majorVersion) {
            17 -> "jre-new"
            21 -> "jre-21"
            25 -> "jre-25"
            else -> "jre-new"
        }

        val tempUniversal = File(tempDir, "universal_${runtimeName}.tar.xz")
        val tempBin = File(tempDir, "bin_${runtimeName}_$arch.tar.xz")

        val universalUrls = getMojoUrls("$MOJO_JRE_BASE/$mojoFolder/universal.tar.xz")
        val binUrls = getMojoUrls("$MOJO_JRE_BASE/$mojoFolder/bin-$arch.tar.xz")

        // Step A: Download Universal
        var universalSuccess = false
        for (uUrl in universalUrls) {
            Logger.info(TAG, "Attempting to download Universal JRE from $uUrl")
            onProgress(15, "Downloading Java Universal core...")
            if (downloadFile(uUrl, tempUniversal) { p, m -> onProgress((p * 0.6).toInt(), m) }) {
                universalSuccess = true
                break
            }
        }

        if (!universalSuccess || !tempUniversal.exists() || tempUniversal.length() < 100000L) {
            Logger.error(TAG, "Failed to download universal JRE component for $runtimeName")
            tempUniversal.delete()
            tempBin.delete()
            return@withContext false
        }

        // Step B: Download Architecture Binaries
        var binSuccess = false
        for (bUrl in binUrls) {
            Logger.info(TAG, "Attempting to download Binpack JRE from $bUrl")
            onProgress(65, "Downloading Java platform binaries ($arch)...")
            if (downloadFile(bUrl, tempBin) { p, m -> onProgress(60 + (p * 0.25).toInt(), m) }) {
                binSuccess = true
                break
            }
        }

        if (!binSuccess || !tempBin.exists() || tempBin.length() < 50000L) {
            Logger.error(TAG, "Failed to download binpack JRE component for $runtimeName arch $arch")
            tempUniversal.delete()
            tempBin.delete()
            return@withContext false
        }

        // Step C: Extract and combine using RuntimesManager.installRuntimeBinPack
        onProgress(88, "Extracting and assembling Java Runtime...")
        try {
            tempUniversal.inputStream().use { uStream ->
                tempBin.inputStream().use { bStream ->
                    RuntimesManager.installRuntimeBinPack(
                        universalFileInputStream = uStream,
                        platformBinsInputStream = bStream,
                        name = runtimeName,
                        binPackVersion = "1.0.0",
                        updateProgress = { p, _ ->
                            onProgress(88 + (p * 12 / 100), "Assembling files...")
                        }
                    )
                }
            }

            postInstallFixup(dest, majorVersion, arch)
            if (!RuntimesManager.isRuntimeValid(dest)) {
                Logger.error(TAG, "Runtime $runtimeName failed validation after assembly!")
                FileUtils.deleteDirectory(dest)
                return@withContext false
            }
            RuntimesManager.forceReload(runtimeName)
            onProgress(100, "Java Runtime ready!")
            Logger.info(TAG, "Successfully installed Mojo-style JRE $runtimeName on-demand")
            return@withContext true
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to install JRE binpack for $runtimeName", e)
            return@withContext false
        } finally {
            tempUniversal.delete()
            tempBin.delete()
        }
    }

    private fun postInstallFixup(dest: File, majorVersion: Int, arch: String) {
        if (!dest.exists()) return
        val versionFile = File(dest, "version")
        if (!versionFile.exists()) {
            versionFile.writeText(if (majorVersion == 8) "1.8.0" else "$majorVersion.0.0")
        }
        val releaseFile = File(dest, "release")
        if (!releaseFile.exists()) {
            val archStr = Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE)
            releaseFile.writeText("JAVA_VERSION=\"$majorVersion.0.0\"\nOS_ARCH=\"$archStr\"\n")
        }

        // Ensure executable permissions on all binaries and shared libraries
        val binDir = File(dest, "bin")
        if (binDir.isDirectory) {
            binDir.listFiles()?.forEach { file ->
                file.setExecutable(true, false)
                file.setReadable(true, false)
            }
        }
        val libDir = File(dest, "lib")
        if (libDir.isDirectory) {
            libDir.walkTopDown().forEach { file ->
                if (file.name.endsWith(".so")) {
                    file.setExecutable(true, false)
                    file.setReadable(true, false)
                }
            }
        }
    }

    /**
     * Import custom user-uploaded JRE archive (.tar.xz or .zip) from Uri
     */
    suspend fun importCustomJre(
        context: Context,
        uri: Uri,
        name: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(10, "Reading custom JRE file...")
            val nativeLibDir = context.applicationInfo.nativeLibraryDir
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open input stream from uri")

            stream.use { inputStream ->
                RuntimesManager.installRuntime(
                    nativeLibDir = nativeLibDir,
                    inputStream = inputStream,
                    name = name,
                    updateProgress = { progress, _ ->
                        onProgress(progress, "Installing custom runtime ($progress%)...")
                    }
                )
            }

            val dest = File(PathManager.DIR_MULTIRT, name)
            postInstallFixup(dest, 17, Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE))
            if (!RuntimesManager.isRuntimeValid(dest)) {
                Logger.error(TAG, "Custom JRE $name failed validation after import!")
                FileUtils.deleteDirectory(dest)
                return@withContext false
            }

            RuntimesManager.forceReload(name)
            onProgress(100, "Custom JRE installed successfully!")
            true
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to import custom JRE", e)
            false
        }
    }

    private fun getMojoUrls(url: String): List<String> {
        val list = mutableListOf(url)
        for (prefix in PROXY_PREFIXES) {
            if (prefix.isNotEmpty()) list.add("$prefix$url")
        }
        return list
    }

    private fun getJava8CandidateUrls(arch: String): List<String> {
        val urls = mutableListOf<String>()
        val mainZip = "$MOJO_ROLLING_BASE/jre8-pojav.zip"
        urls.add(mainZip)
        for (prefix in PROXY_PREFIXES) {
            if (prefix.isNotEmpty()) urls.add("$prefix$mainZip")
        }
        // Pojav release fallback
        val pojavTar = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download/jre8-40df388/jre8-$arch-20220811-release.tar.xz"
        urls.add(pojavTar)
        return urls
    }

    private fun downloadFile(
        urlString: String,
        destination: File,
        onProgress: (Int, String) -> Unit
    ): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            var targetUrl = urlString
            var redirects = 0

            while (redirects < 10) {
                val url = URL(targetUrl)
                connection?.disconnect()
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 25000
                    readTimeout = 40000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "CubiqLauncher/1.0 (Android)")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308 || status == 303 || status == 301 || status == 302
                ) {
                    val location = connection.getHeaderField("Location") ?: break
                    targetUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                    redirects++
                } else {
                    break
                }
            }

            val status = connection?.responseCode ?: -1
            if (status != HttpURLConnection.HTTP_OK) {
                Logger.warning(TAG, "Server returned HTTP $status for $urlString")
                return false
            }

            val conn = connection ?: return false
            val totalBytes = conn.contentLengthLong
            var downloadedBytes = 0L

            conn.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val percent = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(0, 100)
                            onProgress(percent, "Downloading: ${downloadedBytes / 1024 / 1024}MB / ${totalBytes / 1024 / 1024}MB")
                        }
                    }
                }
            }
            destination.exists() && destination.length() > 0L
        } catch (e: Exception) {
            Logger.warning(TAG, "Error downloading from $urlString: ${e.message}")
            destination.delete()
            false
        } finally {
            connection?.disconnect()
        }
    }
}
