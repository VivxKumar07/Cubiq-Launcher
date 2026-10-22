/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.multirt

import android.system.Os
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication.Companion.DEVICE_ARCHITECTURE
import com.movtery.zalithlauncher.components.jre.Jre
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.math.findNearestPositive
import com.movtery.zalithlauncher.utils.string.compareVersion
import com.movtery.zalithlauncher.utils.string.extractUntilCharacter
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

private const val TAG = "RuntimesManager"

/**
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/multirt/MultiRTUtils.java)
 */
object RuntimesManager {
    private val cache = ConcurrentHashMap<String, Runtime>()

    private val RUNTIME_FOLDER = PathManager.DIR_MULTIRT
    private const val JAVA_VERSION_STR: String = "JAVA_VERSION=\""
    private const val OS_ARCH_STR: String = "OS_ARCH=\""

    fun getRuntimes(forceLoad: Boolean = false): List<Runtime> {
        if (!RUNTIME_FOLDER.exists()) {
            Logger.warning(TAG, "Runtime directory not found: ${RUNTIME_FOLDER.absolutePath}")
            return emptyList()
        }

        return RUNTIME_FOLDER.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { loadRuntime(it.name, forceLoad = forceLoad) }
            ?.sortedWith { o1, o2 ->
                val thisVer = o1.versionString ?: o1.name
                -thisVer.compareVersion(o2.versionString ?: o2.name)
            }
            ?: throw IllegalStateException("Failed to access runtime directory")
    }

    fun getExactJreName(majorVersion: Int): String? {
        return getRuntimes().firstOrNull { it.javaVersion == majorVersion }?.name
    }

    fun getNearestJreName(majorVersion: Int): String? {
        return findNearestPositive(majorVersion, getRuntimes()) { it.javaVersion }?.value?.name
    }

    /**
     * Determines the required Java major version for a given Minecraft version and modloader.
     * Rules:
     * - Official manifest javaVersion takes highest priority if present and valid.
     * - Cleanroom requires Java 25 (if >= 0.4.4-alpha) or Java 21.
     * - Babric requires Java 17.
     * - NeoForge requires Java 21 for MC 1.20.5+, Java 17 for MC 1.20.2-1.20.4.
     * - MC 1.20.5+, 1.20.6, 1.21+, 26.x snapshots require Java 21.
     * - MC 1.17, 1.18, 1.19, 1.20 - 1.20.4 require Java 17 (Java 16 requested by 1.17 is fully compatible with Java 17).
     * - MC <= 1.16.5 requires Java 8.
     */
    fun determineTargetJavaVersion(version: com.movtery.zalithlauncher.game.version.installed.Version, manifest: com.movtery.zalithlauncher.game.versioninfo.models.GameManifest? = null): Int {
        val manifestMajor = manifest?.javaVersion?.majorVersion
        if (manifestMajor != null && manifestMajor > 0) {
            return if (manifestMajor == 16) 17 else manifestMajor
        }

        val loaderInfo = version.getVersionInfo()?.loaderInfo
        when (loaderInfo?.loader) {
            com.movtery.zalithlauncher.game.addons.modloader.ModLoader.BABRIC -> return 17
            com.movtery.zalithlauncher.game.addons.modloader.ModLoader.CLEANROOM -> {
                return if (loaderInfo.version.isBiggerTo("0.4.4-alpha")) 25 else 21
            }
            com.movtery.zalithlauncher.game.addons.modloader.ModLoader.NEOFORGE -> {
                val mcVer = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName()
                return if (mcVer.contains("1.20.5") || mcVer.contains("1.20.6") || mcVer.contains("1.21") || mcVer.contains("26.")) 21 else 17
            }
            else -> {}
        }

        val mcVersionStr = version.getVersionInfo()?.minecraftVersion ?: version.getVersionName()
        return when {
            mcVersionStr.contains("1.20.5") || mcVersionStr.contains("1.20.6") || mcVersionStr.contains("1.21") || mcVersionStr.contains("26.") -> 21
            mcVersionStr.contains("1.17") || mcVersionStr.contains("1.18") || mcVersionStr.contains("1.19") || mcVersionStr.contains("1.20") -> 17
            else -> 8
        }
    }

    fun getTargetRuntimeName(version: com.movtery.zalithlauncher.game.version.installed.Version, manifest: com.movtery.zalithlauncher.game.versioninfo.models.GameManifest? = null): String {
        val targetJavaVersion = determineTargetJavaVersion(version, manifest)
        return Jre.entries.firstOrNull { it.majorVersion == targetJavaVersion }?.jreName
            ?: getExactJreName(targetJavaVersion)
            ?: getNearestJreName(targetJavaVersion)
            ?: "Internal-$targetJavaVersion"
    }

    fun isRuntimeReady(name: String): Boolean {
        val dir = File(RUNTIME_FOLDER, name)
        return isRuntimeValid(dir)
    }

    fun forceReload(name: String): Runtime {
        cache.remove(name)
        return loadRuntime(name)
    }

    fun loadRuntime(name: String, forceLoad: Boolean = false): Runtime {
        return cache[name]?.takeIf { !forceLoad } ?: run {
            val runtimeDir = File(RUNTIME_FOLDER, name)
            val internalJre = Jre.entries.firstOrNull { it.jreName == name }
            val defaultMajorVersion = internalJre?.majorVersion ?: 0

            if (!runtimeDir.exists()) {
                return Runtime(
                    name = name,
                    versionString = null,
                    arch = null,
                    javaVersion = defaultMajorVersion,
                    isProvidedByLauncher = internalJre != null,
                    isJDK8 = internalJre == Jre.JRE_8
                ).also { cache[name] = it }
            }

            val releaseFile = File(runtimeDir, "release").takeIf { it.exists() }
                ?: File(runtimeDir, "jre/release").takeIf { it.exists() }

            var detectedJavaVersion: String? = null
            var detectedArch: String? = null

            if (releaseFile != null) {
                runCatching {
                    val content = releaseFile.readText()
                    detectedJavaVersion = content.extractUntilCharacter(JAVA_VERSION_STR, '"')
                    detectedArch = content.extractUntilCharacter(OS_ARCH_STR, '"')
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to read release file for runtime $name", e)
                }
            }

            // Fallback arch detection if missing or release file didn't specify
            if (detectedArch == null) {
                val libDirs = listOf(File(runtimeDir, "lib"), File(runtimeDir, "jre/lib"))
                for (libDir in libDirs) {
                    if (libDir.exists() && libDir.isDirectory) {
                        val archDir = libDir.listFiles()?.firstOrNull { child ->
                            child.isDirectory && Architecture.archAsInt(child.name) != Architecture.UNSUPPORTED_ARCH
                        }
                        if (archDir != null) {
                            detectedArch = archDir.name
                            break
                        }
                    }
                }
                if (detectedArch == null) {
                    detectedArch = when (DEVICE_ARCHITECTURE) {
                        Architecture.ARCH_ARM64 -> "aarch64"
                        Architecture.ARCH_ARM -> "arm"
                        Architecture.ARCH_X86_64 -> "x86_64"
                        Architecture.ARCH_X86 -> "i386"
                        else -> null
                    }
                }
            }

            // Fallback major version detection
            val majorVersion = if (detectedJavaVersion != null) {
                val versionParts = detectedJavaVersion!!.split('.')
                if (versionParts.first() == "1") {
                    versionParts.getOrNull(1)?.toIntOrNull() ?: defaultMajorVersion
                } else {
                    versionParts.first().toIntOrNull() ?: defaultMajorVersion
                }
            } else {
                defaultMajorVersion
            }

            if (detectedJavaVersion == null && majorVersion > 0) {
                detectedJavaVersion = if (majorVersion == 8) "1.8.0" else "$majorVersion.0.0"
            }

            Runtime(
                name = name,
                versionString = detectedJavaVersion,
                arch = detectedArch,
                javaVersion = majorVersion,
                isProvidedByLauncher = internalJre != null,
                isJDK8 = (majorVersion == 8) || isJDK8(runtimeDir.absolutePath)
            ).also { cache[name] = it }
        }
    }

    @Throws(IOException::class)
    suspend fun installRuntime(
        nativeLibDir: String,
        inputStream: InputStream,
        name: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            uncompressArchive(inputStream, dest, updateProgress)
            unpack200(nativeLibDir, dest.absolutePath)
            loadRuntime(name).also { runtime ->
                postPrepare(runtime)
            }
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    @Throws(IOException::class)
    suspend fun postPrepare(name: String) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists()) return@withContext
        val runtime = loadRuntime(name)
        postPrepare(runtime)
    }

    @Throws(IOException::class)
    suspend fun postPrepare(runtime: Runtime) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(runtime.name)
        if (!dest.exists()) return@withContext
        var libFolder = "lib"

        val arch = runtime.arch ?: run {
            val libDir = dest.child("lib")
            libDir.listFiles()?.firstOrNull { child ->
                child.isDirectory && Architecture.archAsInt(child.name) != Architecture.UNSUPPORTED_ARCH
            }?.name ?: Architecture.archAsString(DEVICE_ARCHITECTURE)
        }

        val isJDK8 = isJDK8(dest.absolutePath) || runtime.javaVersion == 8 || runtime.name.contains("8")
        if (dest.child("jre").exists()) {
            libFolder = "jre/lib"
        }
        val possibleArchs = listOf("aarch64", "aarch32", "arm", "amd64", "x86_64", "i386", arch)
        for (a in possibleArchs) {
            if (dest.child(libFolder, a).exists()) {
                libFolder += "/$a"
                break
            }
        }

        runCatching {
            val ftIn = dest.child(libFolder, "libfreetype.so.6")
            val ftOut = dest.child(libFolder, "libfreetype.so")
            if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
                if (ftOut.exists()) ftOut.delete()
                ftIn.renameTo(ftOut)
            }
        }

        val localXawtLib = File(PathManager.DIR_NATIVE_LIB, "libawt_xawt.so")
        val targetXawtLib = dest.child(libFolder, "libawt_xawt.so")
        if (localXawtLib.exists()) {
            runCatching {
                if (targetXawtLib.exists()) targetXawtLib.delete()
                FileUtils.copyFile(localXawtLib, targetXawtLib)
            }.onFailure { e ->
                Logger.warning(TAG, "Failed to copy libawt_xawt.so: ${e.message}")
            }
        }

        listOf(dest.child("bin"), dest.child("jre/bin")).forEach { binDir ->
            if (binDir.isDirectory) {
                binDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.setExecutable(true, false)
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun installRuntimeBinPack(
        universalFileInputStream: InputStream,
        platformBinsInputStream: InputStream,
        name: String,
        binPackVersion: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = RUNTIME_FOLDER.child(name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            installRuntimeNoRemove(universalFileInputStream, dest, updateProgress)
            installRuntimeNoRemove(platformBinsInputStream, dest, updateProgress)

            unpack200(PathManager.DIR_NATIVE_LIB, dest.absolutePath)

            loadRuntime(name).also { runtime ->
                postPrepare(runtime)
            }

            val versionFile = File(dest, "version")
            versionFile.writeText(binPackVersion)

            forceReload(name)
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    fun isRuntimeValid(name: String): Boolean {
        return isRuntimeValid(RUNTIME_FOLDER.child(name))
    }

    fun isRuntimeValid(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        val hasRelease = File(dir, "release").exists() || File(dir, "jre/release").exists()
        val hasJava = File(dir, "bin/java").exists() || File(dir, "jre/bin/java").exists()
        if (!hasRelease || !hasJava) return false

        // Check essential shared libraries (libjli.so, libjvm.so, libjava.so)
        val hasLibJli = listOf(
            File(dir, "lib/jli/libjli.so"),
            File(dir, "lib/libjli.so"),
            File(dir, "lib/aarch64/jli/libjli.so"),
            File(dir, "lib/aarch32/jli/libjli.so"),
            File(dir, "lib/arm/jli/libjli.so"),
            File(dir, "lib/x86_64/jli/libjli.so"),
            File(dir, "lib/i386/jli/libjli.so"),
            File(dir, "jre/lib/jli/libjli.so"),
            File(dir, "jre/lib/libjli.so"),
            File(dir, "jre/lib/aarch64/jli/libjli.so"),
            File(dir, "jre/lib/aarch32/jli/libjli.so"),
            File(dir, "jre/lib/arm/jli/libjli.so")
        ).any { it.exists() && it.isFile } || runCatching {
            dir.walkTopDown().maxDepth(5).any { it.name == "libjli.so" && it.isFile }
        }.getOrDefault(false)

        val hasLibJvm = listOf(
            File(dir, "lib/server/libjvm.so"),
            File(dir, "lib/client/libjvm.so"),
            File(dir, "lib/aarch64/server/libjvm.so"),
            File(dir, "lib/aarch32/client/libjvm.so"),
            File(dir, "lib/aarch32/server/libjvm.so"),
            File(dir, "lib/arm/server/libjvm.so"),
            File(dir, "lib/arm/client/libjvm.so"),
            File(dir, "jre/lib/server/libjvm.so"),
            File(dir, "jre/lib/client/libjvm.so"),
            File(dir, "jre/lib/aarch64/server/libjvm.so"),
            File(dir, "jre/lib/aarch32/client/libjvm.so")
        ).any { it.exists() && it.isFile } || runCatching {
            dir.walkTopDown().maxDepth(5).any { it.name == "libjvm.so" && it.isFile }
        }.getOrDefault(false)

        val hasLibJava = listOf(
            File(dir, "lib/libjava.so"),
            File(dir, "lib/aarch64/libjava.so"),
            File(dir, "lib/aarch32/libjava.so"),
            File(dir, "lib/arm/libjava.so"),
            File(dir, "jre/lib/libjava.so"),
            File(dir, "jre/lib/aarch64/libjava.so"),
            File(dir, "jre/lib/aarch32/libjava.so"),
            File(dir, "jre/lib/arm/libjava.so")
        ).any { it.exists() && it.isFile } || runCatching {
            dir.walkTopDown().maxDepth(5).any { it.name == "libjava.so" && it.isFile }
        }.getOrDefault(false)

        if (!hasLibJli || !hasLibJvm || !hasLibJava) return false

        // Check classes packaging
        val isJ8 = isJDK8(dir.absolutePath)
        if (isJ8) {
            val hasRt = File(dir, "lib/rt.jar").exists() || File(dir, "jre/lib/rt.jar").exists()
            // If .pack files remain, unpacking was incomplete
            val hasRemainingPacks = runCatching {
                FileUtils.listFiles(dir, arrayOf("pack"), true).isNotEmpty()
            }.getOrDefault(false)
            if (!hasRt || hasRemainingPacks) return false
        } else {
            val hasModules = File(dir, "lib/modules").exists()
            if (!hasModules) return false
        }

        return true
    }

    fun loadInternalRuntimeVersion(name: String): String? {
        val dest = RUNTIME_FOLDER.child(name)
        if (!isRuntimeValid(dest)) return null
        val versionFile = File(dest, "version")
        return try {
            if (versionFile.exists()) versionFile.readText() else null
        } catch (_: IOException) {
            null
        }
    }

    @Throws(IOException::class)
    fun removeRuntime(name: String) {
        val dest: File = RUNTIME_FOLDER.child(name).takeIf { it.exists() } ?: return
        FileUtils.deleteDirectory(dest)
        cache.remove(name)
    }

    fun getRuntimeHome(name: String): File {
        val dest = RUNTIME_FOLDER.child(name)
        if (!dest.exists() || !isRuntimeValid(dest)) {
            throw RuntimeException("Selected Java Runtime '$name' is not installed or incomplete.")
        }
        return dest
    }

    /**
     * Unpacks all .pack files into .jar Serves only for java 8, as java 9 brought project jigsaw
     *
     * @param nativeLibraryDir The native lib path, required to execute the unpack200 binary
     * @param runtimePath The path to the runtime to walk into
     */
    private suspend fun unpack200(
        nativeLibraryDir: String,
        runtimePath: String
    ) = withContext(Dispatchers.Default) {
        val basePath = File(runtimePath)
        val files: Collection<File> = runCatching {
            FileUtils.listFiles(basePath, arrayOf("pack"), true)
        }.getOrDefault(emptyList())

        if (files.isEmpty()) return@withContext

        val nativeUnpack = File(nativeLibraryDir, "libunpack200.so")
        val runtimeUnpack = File(runtimePath, "bin/unpack200")
        val unpackBinary: File? = when {
            nativeUnpack.exists() && nativeUnpack.canExecute() -> nativeUnpack
            nativeUnpack.exists() -> {
                runCatching { nativeUnpack.setExecutable(true, false) }
                nativeUnpack
            }
            runtimeUnpack.exists() -> {
                runCatching { runtimeUnpack.setExecutable(true, false) }
                runtimeUnpack
            }
            else -> null
        }

        if (unpackBinary == null) {
            throw IOException("No unpack200 binary found in $nativeLibraryDir or $runtimePath to unpack .pack files!")
        }

        files.forEach { jarFile ->
            ensureActive()
            val destPath = jarFile.absolutePath.replace(".pack", "")
            val pb = ProcessBuilder(
                unpackBinary.absolutePath,
                "-r",
                jarFile.absolutePath,
                destPath
            )
            val process = pb.start()
            val code = process.waitFor()
            if (code == 0) {
                jarFile.delete()
                Logger.info(TAG, "Unpacked ${jarFile.name} -> ${File(destPath).name}")
            } else {
                val err = runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
                throw IOException("unpack200 exited with $code for ${jarFile.name}: $err")
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun installRuntimeNoRemove(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        uncompressArchive(inputStream, dest, updateProgress)
        inputStream.close()
    }

    @Throws(IOException::class)
    private suspend fun uncompressTarXZ(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = uncompressArchive(inputStream, dest, updateProgress)

    @Throws(IOException::class)
    private suspend fun uncompressArchive(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        dest.ensureDirectory()
        val buffer = ByteArray(8192)
        val bis = BufferedInputStream(inputStream)
        bis.mark(16)
        val header = ByteArray(6)
        val read = bis.read(header)
        bis.reset()

        val isZip = read >= 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
        val isGzip = read >= 2 && header[0] == 0x1F.toByte() && header[1] == 0x8B.toByte()
        val isXz = read >= 6 && header[0] == 0xFD.toByte() && header[1] == 0x37.toByte() && header[2] == 0x7A.toByte() && header[3] == 0x58.toByte() && header[4] == 0x5A.toByte() && header[5] == 0x00.toByte()

        if (isZip) {
            ZipInputStream(bis).use { zipIn ->
                generateSequence { zipIn.nextEntry }.forEach { entry ->
                    ensureActive()
                    updateProgress(R.string.generic_unpacking, arrayOf(entry.name))
                    val destPath = File(dest, entry.name).ensureParentDirectory()
                    if (entry.isDirectory) {
                        destPath.ensureDirectory()
                    } else {
                        FileOutputStream(destPath).use { os ->
                            IOUtils.copyLarge(zipIn, os, buffer)
                        }
                    }
                    zipIn.closeEntry()
                }
            }
        } else {
            val tarStream: InputStream = when {
                isGzip -> GZIPInputStream(bis)
                isXz -> XZCompressorInputStream(bis)
                else -> bis
            }
            TarArchiveInputStream(tarStream).use { tarIn ->
                generateSequence { tarIn.nextEntry }.forEach { tarEntry ->
                    ensureActive()
                    val tarEntryName = tarEntry.name
                    updateProgress(R.string.generic_unpacking, arrayOf(tarEntryName))

                    val destPath = File(dest, tarEntryName).ensureParentDirectory()

                    when {
                        tarEntry.isSymbolicLink -> try {
                            if (destPath.exists()) destPath.delete()
                            Os.symlink(tarEntry.linkName, destPath.absolutePath)
                        } catch (e: Throwable) {
                            Logger.warning(TAG, "Failed to create symbolic link: ${destPath.absolutePath} -> ${tarEntry.linkName} (${e.message})")
                        }

                        tarEntry.isDirectory -> destPath.ensureDirectory()
                        !destPath.exists() || destPath.length() != tarEntry.size -> {
                            FileOutputStream(destPath).use { os ->
                                IOUtils.copyLarge(tarIn, os, buffer)
                            }
                            if (tarEntry.mode != 0) {
                                try {
                                    Os.chmod(destPath.absolutePath, tarEntry.mode and 0b111_111_111)
                                } catch (_: Throwable) {
                                    if ((tarEntry.mode and 0b001_000_000) != 0) {
                                        destPath.setExecutable(true, false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun isJDK8(runtimeDir: String): Boolean {
        val dir = File(runtimeDir)
        if (File(dir, "release").exists()) {
            val releaseText = runCatching { File(dir, "release").readText() }.getOrDefault("")
            if (releaseText.contains("JAVA_VERSION=\"1.8") || releaseText.contains("JAVA_VERSION=\"8")) return true
        }
        return dir.name.contains("8") ||
                File(dir, "lib/aarch64").exists() ||
                File(dir, "lib/aarch32").exists() ||
                File(dir, "jre/lib/aarch64").exists() ||
                File(dir, "jre/lib/aarch32").exists() ||
                (File(dir, "jre").exists() && File(dir, "bin/javac").exists())
    }
}