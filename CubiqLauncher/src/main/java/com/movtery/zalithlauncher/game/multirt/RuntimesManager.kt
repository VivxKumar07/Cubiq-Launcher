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
import com.movtery.zalithlauncher.utils.file.SafePaths
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.math.findNearestPositive
import com.movtery.zalithlauncher.utils.string.compareVersion
import com.movtery.zalithlauncher.utils.string.extractUntilCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

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

    fun forceReload(name: String): Runtime {
        cache.remove(name)
        return loadRuntime(name)
    }

    fun loadRuntime(name: String, forceLoad: Boolean = false): Runtime {
        return cache[name]?.takeIf { !forceLoad } ?: run {
            val runtimeDir = SafePaths.resolveChild(RUNTIME_FOLDER, name)
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
                isJDK8 = isJDK8(runtimeDir.absolutePath) || (majorVersion == 8 && File(runtimeDir, "jre").exists())
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
        val dest = SafePaths.resolveChild(RUNTIME_FOLDER, name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            uncompressTarXZ(inputStream, dest, updateProgress)
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
        val dest = SafePaths.resolveChild(RUNTIME_FOLDER, name)
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

        if (dest.child(libFolder, arch).exists()) {
            libFolder += "/$arch"
        }

        val isJDK8 = isJDK8(dest.absolutePath)
        if (isJDK8) {
            libFolder = "/jre$libFolder"
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
    }

    @Throws(IOException::class)
    suspend fun installRuntimeBinPack(
        universalFileInputStream: InputStream,
        platformBinsInputStream: InputStream,
        name: String,
        binPackVersion: String,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dest = SafePaths.resolveChild(RUNTIME_FOLDER, name)
        try {
            if (dest.exists()) FileUtils.deleteDirectory(dest)
            installRuntimeNoRemove(universalFileInputStream, dest, updateProgress)
            installRuntimeNoRemove(platformBinsInputStream, dest, updateProgress)

            unpack200(PathManager.DIR_NATIVE_LIB, dest.absolutePath)

            val versionFile = File(dest, "version")
            versionFile.writeText(binPackVersion)

            forceReload(name)
        } catch (e: Exception) {
            FileUtils.deleteDirectory(dest)
            throw e
        }
    }

    fun loadInternalRuntimeVersion(name: String): String? {
        val versionFile = RUNTIME_FOLDER.child(name, "version")
        try {
            return if (versionFile.exists()) {
                versionFile.readText()
            } else {
                null
            }
        } catch (_: IOException) {
            return null
        }
    }

    @Throws(IOException::class)
    fun removeRuntime(name: String) {
        val dest: File = SafePaths.resolveChild(RUNTIME_FOLDER, name).takeIf { it.exists() } ?: return
        FileUtils.deleteDirectory(dest)
        cache.remove(name)
    }

    fun getRuntimeHome(name: String): File {
        val dest = SafePaths.resolveChild(RUNTIME_FOLDER, name)
        if (!dest.exists() || forceReload(name).versionString == null) {
            throw RuntimeException("Selected runtime is broken!")
        }

        return dest
    }

    /**
     * Unpacks all .pack files into .jar Serves only for java 8, as java 9 brought project jigsaw
     * @param nativeLibraryDir The native lib path, required to execute the unpack200 binary
     * @param runtimePath The path to the runtime to walk into
     */
    private suspend fun unpack200(
        nativeLibraryDir: String,
        runtimePath: String
    ) = withContext(Dispatchers.Default) {
            val basePath = File(runtimePath)
            val files: Collection<File> = FileUtils.listFiles(basePath, arrayOf("pack"), true)

            val workDir = File(nativeLibraryDir)
            val processBuilder = ProcessBuilder().directory(workDir)

            files.forEach { jarFile ->
                ensureActive()
                runCatching {
                    val destPath = jarFile.absolutePath.replace(".pack", "")
                    processBuilder.command(
                        "./libunpack200.so",
                        "-r",
                        jarFile.absolutePath,
                        destPath
                    ).start().apply {
                        waitFor()
                    }
                }.onFailure { e ->
                    if (e is IOException) {
                        Logger.error(TAG, "Failed to unpack the runtime!", e)
                    } else throw e
                }
            }
        }

    @Throws(IOException::class)
    private suspend fun installRuntimeNoRemove(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        uncompressTarXZ(inputStream, dest, updateProgress)
        inputStream.close()
    }

    @Throws(IOException::class)
    private suspend fun uncompressTarXZ(
        inputStream: InputStream,
        dest: File,
        updateProgress: (Int, Array<Any>) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        dest.ensureDirectory()
        val buffer = ByteArray(8192)

        TarArchiveInputStream(XZCompressorInputStream(inputStream)).use { tarIn ->
            generateSequence { tarIn.nextEntry }.forEach { tarEntry ->
                ensureActive()
                val tarEntryName = tarEntry.name
                updateProgress(R.string.generic_unpacking, arrayOf(tarEntryName))

                val destPath = SafePaths.resolveChild(dest, tarEntryName).ensureParentDirectory()

                when {
                    tarEntry.isSymbolicLink -> {
                        SafePaths.validateRelativeSymlink(dest, destPath, tarEntry.linkName)
                        try {
                            if (destPath.exists()) destPath.delete()
                            Os.symlink(tarEntry.linkName, destPath.absolutePath)
                        } catch (e: Throwable) {
                            Logger.warning(TAG, "Failed to create symbolic link: ${destPath.absolutePath} -> ${tarEntry.linkName} (${e.message})")
                        }
                    }

                    tarEntry.isDirectory -> destPath.ensureDirectory()
                    !destPath.exists() || destPath.length() != tarEntry.size ->
                        FileOutputStream(destPath).use { os ->
                            IOUtils.copyLarge(tarIn, os, buffer)
                        }
                }
            }
        }
    }

    fun isJDK8(runtimeDir: String): Boolean {
        return File(runtimeDir, "jre").exists() && File(runtimeDir, "bin/javac").exists()
    }
}
