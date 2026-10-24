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

package com.movtery.zalithlauncher.game.launch

import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.system.Os
import android.util.ArrayMap
import androidx.annotation.CallSuper
import androidx.compose.ui.unit.IntSize
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLNativeInvoker
import com.movtery.zalithlauncher.components.Components
import com.movtery.zalithlauncher.components.UnpackComponentsTask
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.multirt.Runtime
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.plugin.ffmpeg.FFmpegPluginManager
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.path.LibPath
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.getOrMin
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.device.Architecture.ARCH_X86
import com.movtery.zalithlauncher.utils.device.Architecture.is64BitsDevice
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.getSystemDnsServerAddresses
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes
import com.oracle.dalvik.VMLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.TimeZone

private const val TAG = "Launcher"

abstract class Launcher(
    val onExit: (code: Int, isSignal: Boolean) -> Unit,
    val openPath: (folder: File) -> Unit
) {
    lateinit var runtime: Runtime
        protected set

    /** 当前启动版本要求的 LWJGL 版本（见 [detectLwjglVersion]），0 = 未探测/默认 */
    protected var lwjglVersion: Int = 0
        private set

    /**
     * 当前启动版本的 LWJGL natives 目录
     */
    protected lateinit var lwjglNativesDir: String
        private set

    /**
     * 初始化 LWJGL 组件
     */
    protected suspend fun initLwjglComponent(context: Context, version: Int) {
        check(!::lwjglNativesDir.isInitialized) { "LWJGL component has already been initialized" }
        lwjglVersion = version
        lwjglNativesDir = File(
            PathManager.DIR_COMPONENTS,
            "lwjgl/${lwjglVersionDir(version)}/natives/${Architecture.archAsStringAndroid(Architecture.getDeviceArchitecture())}"
        ).absolutePath
        verifyLwjglNatives(context)
        sanitizeLwjglJarIfNeeded()
    }

    private fun sanitizeLwjglJarIfNeeded() {
        val versionDir = lwjglVersionDir(lwjglVersion)
        val jarFile = File(PathManager.DIR_COMPONENTS, "lwjgl/$versionDir/lwjgl.jar")
        if (!jarFile.isFile) return

        runCatching {
            var hasV25 = false
            java.util.zip.ZipFile(jarFile).use { zf ->
                val entries = zf.entries()
                while (entries.hasMoreElements()) {
                    if (entries.nextElement().name.startsWith("META-INF/versions/25")) {
                        hasV25 = true
                        break
                    }
                }
            }
            if (!hasV25) return

            LoggerBridge.appendInfo("LWJGL: Sanitizing $jarFile (removing incompatible FFM v25 classes)")
            val tempJar = File(jarFile.parentFile, jarFile.name + ".tmp")
            java.util.zip.ZipFile(jarFile).use { zin ->
                java.util.zip.ZipOutputStream(java.io.FileOutputStream(tempJar)).use { zout ->
                    val entries = zin.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (!entry.name.startsWith("META-INF/versions/25")) {
                            val newEntry = java.util.zip.ZipEntry(entry.name)
                            newEntry.time = entry.time
                            zout.putNextEntry(newEntry)
                            zin.getInputStream(entry).use { it.copyTo(zout) }
                            zout.closeEntry()
                        }
                    }
                }
            }
            if (tempJar.isFile && tempJar.length() > 0) {
                jarFile.delete()
                tempJar.renameTo(jarFile)
                LoggerBridge.appendInfo("LWJGL: Sanitized $jarFile successfully")
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to sanitize lwjgl.jar", it)
        }
    }

    private suspend fun verifyLwjglNatives(context: Context) {
        val versionDir = lwjglVersionDir(lwjglVersion)
        val coreLib = File(lwjglNativesDir, "liblwjgl.so")

        if (coreLib.isFile) {
            LoggerBridge.appendInfo("LWJGL: LWJGL $versionDir natives check passed: path=$lwjglNativesDir (liblwjgl.so found)")
            return
        }

        LoggerBridge.appendInfo("LWJGL: LWJGL natives check failed: liblwjgl.so not found in $lwjglNativesDir, re-unpacking the LWJGL component now")

        withContext(Dispatchers.IO) {
            runCatching {
                val component = Components.entries.firstOrNull {
                    it.component == "lwjgl/$versionDir"
                } ?: error("Unrecognized LWJGL component: lwjgl/$versionDir")
                UnpackComponentsTask(context, component).run()
            }.onFailure { e ->
                LoggerBridge.appendInfo("LWJGL: Failed to re-unpack the LWJGL component: ${e.getMessageOrToString()}")
            }
        }

        if (coreLib.isFile) {
            LoggerBridge.appendInfo("LWJGL: LWJGL $versionDir natives check passed after re-unpack: path=$lwjglNativesDir (liblwjgl.so found)")
        } else {
            LoggerBridge.appendInfo("LWJGL: LWJGL natives are still missing after re-unpack: liblwjgl.so not found in $lwjglNativesDir, the game may fail to launch")
        }
    }

    private val runtimeHome: String by lazy {
        RuntimesManager.getRuntimeHome(runtime.name).absolutePath
    }

    private fun getJavaHome(): String {
        val jreSub = File(runtimeHome, "jre")
        return if (jreSub.isDirectory) jreSub.absolutePath else runtimeHome
    }

    abstract suspend fun launch(screenSize: IntSize): Int
    abstract fun chdir(): String
    abstract fun getLogFile(): File
    abstract fun exit()

    /**
     * 游戏目录（.minecraft），默认为当前选择的游戏目录
     */
    protected open fun getMinecraftPath(): String = getGameHome()

    protected suspend fun launchJvm(
        context: Context,
        jvmArgs: List<String>,
        userHome: String,
        userArgs: String,
        screenSize: IntSize,
        useLocalLanguage: Boolean = true
    ): Int {
        ZLNativeInvoker.staticLauncher = this

        ZLBridge.setLdLibraryPath(getRuntimeLibraryPath())

        LoggerBridge.appendTitle("Env Map")
        setEnv(screenSize)

        LoggerBridge.appendTitle("DLOPEN Java Runtime")
        dlopenJavaRuntime()

        dlopenEngine()

        return launchJavaVM(
            context = context,
            jvmArgs = jvmArgs,
            userHome = userHome,
            userArgs = userArgs,
            screenSize = screenSize,
            useLocalLanguage = useLocalLanguage
        )
    }

    //伪 suspend 函数，等待 JVM 的退出代码
    private suspend fun launchJavaVM(
        context: Context,
        jvmArgs: List<String>,
        userHome: String,
        userArgs: String,
        screenSize: IntSize,
        useLocalLanguage: Boolean
    ): Int {
        val args = getJavaArgs(
            userHome = userHome,
            userArgumentsString = userArgs,
            screenSize = screenSize,
            useLocalLanguage = useLocalLanguage
        ).toMutableList()
        progressFinalUserArgs(args)

        args.addAll(jvmArgs)
        args.add(0, "java")

        LoggerBridge.appendTitle("JVM Args")
        val iterator = args.iterator()
        while (iterator.hasNext()) {
            val arg = iterator.next()
            if (arg.startsWith("--accessToken") && iterator.hasNext()) {
                iterator.next()
                LoggerBridge.appendInfo(arg)
                LoggerBridge.appendInfo("********************")
                continue
            }
            LoggerBridge.appendInfo(arg)
        }

        ZLBridge.setupExitMethod(context.applicationContext)
        ZLBridge.initializeGameExitHook()
        ZLBridge.chdir(chdir())

        val exitCode = VMLauncher.launchJVM(args.toTypedArray())
        LoggerBridge.append("Java Exit code: $exitCode")
        return exitCode
    }

    /**
     * 添加 JVM 参数
     */
    protected open fun MutableMap<String, String>.putJavaArgs() {}

    private fun getJavaArgs(
        userHome: String,
        userArgumentsString: String,
        screenSize: IntSize,
        useLocalLanguage: Boolean
    ): List<String> {
        val userArguments = userArgumentsString.splitPreservingQuotes().toMutableList()
        val resolvFile = ensureDNSConfig()

        val overridableArguments = mutableMapOf<String, String>().apply {
            put("java.home", getJavaHome())
            put("java.io.tmpdir", PathManager.DIR_CACHE.absolutePath)
            put("jna.boot.library.path", PathManager.DIR_NATIVE_LIB)
            put("user.home", userHome)
            if (useLocalLanguage) {
                put("user.language", System.getProperty("user.language") ?: "en")
                put("user.country", Locale.getDefault().country)
            }
            put("user.timezone", TimeZone.getDefault().id)
            put("os.name", "Linux")
            put("os.version", "Android-${Build.VERSION.RELEASE}")
            put("pojav.path.minecraft", getMinecraftPath())
            if (File("/system/lib64/libvulkan.so").exists() || File("/system/lib/libvulkan.so").exists() || File("/vendor/lib64/libvulkan.so").exists() || File("/vendor/lib/libvulkan.so").exists()) {
                put("org.lwjgl.vulkan.libname", "libvulkan.so")
            }
            // LWJGL 3.4 的 Library.loadSystem 通过该属性定位 native 库。
            // 指向 per-version natives 目录，保证 3.4.x 游戏加载对应版本的 liblwjgl.so 等。
            put("org.lwjgl.librarypath", lwjglNativesDir)
            put("ext.net.resolvPath", resolvFile.absolutePath)

            put("log4j2.formatMsgNoLookups", "true")
            // Fix RCE vulnerability of log4j2
            put("java.rmi.server.useCodebaseOnly", "true")
            put("com.sun.jndi.rmi.object.trustURLCodebase", "false")
            put("com.sun.jndi.cosnaming.object.trustURLCodebase", "false")

            put("net.minecraft.clientmodname", BuildKeys.LAUNCHER_NAME)

            // fml
            put("fml.earlyprogresswindow", "false")
            put("fml.ignoreInvalidMinecraftCertificates", "true")
            put("fml.ignorePatchDiscrepancies", "true")

            put("loader.disable_forked_guis", "true")
            put("jdk.lang.Process.launchMechanism", "FORK")

            put("sodium.checks.issue2561", "false")

            put("file.encoding", "UTF-8")
            put("sun.stdout.encoding", "UTF-8")
            put("sun.stderr.encoding", "UTF-8")

            put("cpu.name", getSocName())

            putJavaArgs()
        }.map { entry ->
            "-D${entry.key}=${entry.value}"
        }

        val additionalArguments = overridableArguments.filter { arg ->
            val stripped = arg.substringBefore('=')
            val overridden = userArguments.any { it.startsWith(stripped) }
            if (overridden) {
                Logger.info(TAG, "Arg skipped: $arg")
            }
            !overridden
        }

        userArguments += additionalArguments
        return userArguments
    }

    /**
     * 确保 DNS 配置文件存在
     */
    private fun ensureDNSConfig(): File {
        val resolvFile = File(PathManager.DIR_GAME, "resolv.conf")
        val servers = buildResolvConfSet()
        Logger.info(TAG, "Using DNS servers for game: $servers")
        val configText = servers.joinToString(separator = "\n") { "nameserver $it" }
        runCatching {
            // 配置文件不存在或内容不一致时覆写一次
            if (!resolvFile.exists() || resolvFile.readText().trim() != configText.trim()) {
                resolvFile.writeText(configText)
            }
        }.onFailure {
            Logger.warning(TAG, "Failed to create resolv.conf", it)
            FileUtils.deleteQuietly(resolvFile)
        }
        return resolvFile
    }

    private fun buildResolvConfSet(): Set<String> {
        return buildSet {
            getSystemDnsServerAddresses()
                // JNDI DNS 的 nameserver 解析无法处理裸 IPv6 地址，仅保留 IPv4
                ?.filterNot { it.contains(':') }
                ?.let { addAll(it) }

            // 按地区获取公共 DNS
            if (LocaleList.getDefault().get(0).displayName != Locale.CHINA.displayName) {
                add("1.1.1.1")
                add("1.0.0.1")
            } else {
                add("223.5.5.5")
                add("119.29.29.29")
            }
        }
    }

    /**
     * @param args 需要进行处理的参数
     * @param ramAllocation 指定内存空间大小
     */
    protected open fun progressFinalUserArgs(
        args: MutableList<String>,
        ramAllocation: Int = AllSettings.ramAllocation.getOrMin()
    ) {
        args.purgeArg("-Xms")
        args.purgeArg("-Xmx")
        args.purgeArg("-d32")
        args.purgeArg("-d64")
        args.purgeArg("-Xint")
        args.purgeArg("-XX:+UseTransparentHugePages")
        args.purgeArg("-XX:+UseLargePagesInMetaspace")
        args.purgeArg("-XX:+UseLargePages")
        args.purgeArg("-Dorg.lwjgl.opengl.libname")
        // Don't let the user specify a custom Freetype library (as the user is unlikely to specify a version compiled for Android)
        args.purgeArg("-Dorg.lwjgl.freetype.libname")
        // Overridden by us to specify the exact number of cores that the android system has
        args.purgeArg("-XX:ActiveProcessorCount")

        //Add automatically generated args
        val ramAllocationString = ramAllocation.toString()
        args.add("-Xms${ramAllocationString}M")
        args.add("-Xmx${ramAllocationString}M")

        args.add("-Dorg.lwjgl.openal.libname=${PathManager.DIR_NATIVE_LIB}/libopenal.so")

        // Force LWJGL to use the Freetype library intended for it, instead of using the one
        // that we ship with Java (since it may be older than what's needed).
        // 始终指向 LWJGL 组件 natives 目录内的库，禁止回退到应用原生 libs 目录（其中不含 LWJGL 系库）
        args.add("-Dorg.lwjgl.freetype.libname=${File(lwjglNativesDir, "libfreetype.so").absolutePath}")

        // Our spirv-cross is compiled shared, so it gets named shared.
        args.add("-Dorg.lwjgl.spvc.libname=spirv-cross-c-shared")

        // We don't have jemalloc for our LWJGL so set the allocator to system to avoid error logs
        args.add("-Dorg.lwjgl.system.allocator=system")

        // Some phones are not using the right number of cores, fix that
        args.add("-XX:ActiveProcessorCount=${java.lang.Runtime.getRuntime().availableProcessors()}")
    }

    protected fun MutableList<String>.purgeArg(argStart: String) {
        removeIf { arg: String -> arg.startsWith(argStart) }
    }

    protected fun getJavaLibDir(): String {
        val base = if (File(runtimeHome, "jre").isDirectory) File(runtimeHome, "jre") else File(runtimeHome)
        val possibleArchs = listOf("aarch64", "aarch32", "arm64", "arm", "amd64", "x86_64", "i386", "i486", "i586", "x86")
        for (arch in possibleArchs) {
            if (File(base, "lib/$arch").isDirectory) return "/lib/$arch"
        }
        if (File(base, "lib").isDirectory) return "/lib"
        return "/lib"
    }

    private fun getJvmLibDir(): String {
        val path = getJavaLibDir()
        val baseDir = if (File(runtimeHome, "jre").isDirectory) "$runtimeHome/jre" else runtimeHome
        val jvmFile = File("$baseDir$path/server/libjvm.so")
        return if (jvmFile.exists()) "/server" else "/client"
    }

    protected open fun getRuntimeLibraryPath(): String {
        val isJ8 = runtime.isJDK8 || runtime.javaVersion == 8 || RuntimesManager.isJDK8(runtimeHome)
        val javaLibDir = "$runtimeHome${getJavaLibDir()}"
        val baseDir = if (File(runtimeHome, "jre").isDirectory) "$runtimeHome/jre" else runtimeHome

        val libName = if (is64BitsDevice) "lib64" else "lib"
        val paths = buildList {
            FFmpegPluginManager.takeIf { it.isAvailable }?.libraryPath?.let { add(it) }
            val mobileGluesPath = if (com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.isInstalled()) com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.getInstalledPluginNativePath() else null
            val pluginPath = RendererPluginManager.selectedRendererPlugin?.path ?: mobileGluesPath
            pluginPath?.let { add(it) }
            addAll(NativePluginManager.getPaths())
            add("$javaLibDir/jli")
            add("$javaLibDir/server")
            add("$javaLibDir/client")
            add(javaLibDir)
            add("$baseDir/lib")
            add("$runtimeHome/lib")
            add("$runtimeHome/lib/jli")
            add("$runtimeHome/lib/server")
            add("$runtimeHome/lib/client")
            add("$runtimeHome/lib/aarch64")
            add("$runtimeHome/lib/aarch64/jli")
            add("$runtimeHome/lib/aarch64/server")
            add("$runtimeHome/lib/aarch32")
            add("$runtimeHome/lib/aarch32/jli")
            add("$runtimeHome/lib/aarch32/client")
            add("$runtimeHome/lib/arm")
            add("$runtimeHome/lib/arm/jli")
            add("$runtimeHome/jre/lib")
            add("$runtimeHome/jre/lib/jli")
            add("$runtimeHome/jre/lib/server")
            add("$runtimeHome/jre/lib/client")
            add("$runtimeHome/jre/lib/aarch64")
            add("$runtimeHome/jre/lib/aarch64/jli")
            add("$runtimeHome/jre/lib/aarch64/server")
            add("$runtimeHome/jre/lib/aarch32")
            add("$runtimeHome/jre/lib/aarch32/jli")
            add("$runtimeHome/jre/lib/aarch32/client")
            add("$runtimeHome/jre/lib/arm")
            add("$runtimeHome/jre/lib/arm/jli")
            add("/system/$libName")
            add("/vendor/$libName")
            add("/vendor/$libName/hw")
            add("/system_ext/$libName")
            add(LibPath.JNA.absolutePath)
            PathManager.DIR_RUNTIME_MOD?.absolutePath?.let { add(it) }
            add(lwjglNativesDir)
            add(PathManager.DIR_NATIVE_LIB)
        }
        return paths.joinToString(":")
    }

    protected fun getLibraryPath(): String {
        val libDirName = if (is64BitsDevice) "lib64" else "lib"
        val mobileGluesPath = if (com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.isInstalled()) com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.getInstalledPluginNativePath() else null
        val pluginPath = RendererPluginManager.selectedRendererPlugin?.path ?: mobileGluesPath
        val path = listOfNotNull(
            // per-version LWJGL natives 优先，避免 APK 内旧版 native 抢占
            lwjglNativesDir,
            "/system/$libDirName",
            "/vendor/$libDirName",
            "/vendor/$libDirName/hw",
            "/system_ext/$libDirName",
            pluginPath,
            PathManager.DIR_RUNTIME_MOD?.absolutePath,
            PathManager.DIR_NATIVE_LIB
        )
        return path.joinToString(":")
    }

    protected fun findInLdLibPath(libName: String): String {
        val path = getLibraryPath()
        return path.split(":").find { libPath ->
            val file = File(libPath, libName)
            file.exists() && file.isFile
        }?.let {
            File(it, libName).absolutePath
        } ?: libName
    }

    private fun locateLibs(path: File): List<File> {
        val children = path.listFiles() ?: return emptyList()
        return children.flatMap { file ->
            when {
                file.isFile && file.name.endsWith(".so") -> listOf(file)
                file.isDirectory -> locateLibs(file)
                else -> emptyList()
            }
        }
    }

    private fun setEnv(screenSize: IntSize) {
        val envMap = runCatching {
            initEnv(screenSize)
        }.onFailure {
            LoggerBridge.appendTitle("Init Env Failed")
            LoggerBridge.append(it.stackTraceToString())
        }.getOrThrow()
        envMap.forEach { (key, value) ->
            LoggerBridge.appendInfo("$key = $value")
            runCatching {
                Os.setenv(key, value, true)
            }.onFailure {
                Logger.error(TAG, "Unable to set environment variable.", it)
            }
        }
    }

    @CallSuper
    protected open fun initEnv(screenSize: IntSize): MutableMap<String, String> {
        val envMap: MutableMap<String, String> = ArrayMap()
        setJavaEnv(
            screenSize = screenSize,
            envMap = { envMap }
        )
        return envMap
    }

    private fun setJavaEnv(
        screenSize: IntSize,
        envMap: () -> MutableMap<String, String>
    ) {
        val hasJreSubdir = File(runtimeHome, "jre").isDirectory
        val path = listOfNotNull(
            "$runtimeHome/bin",
            if (hasJreSubdir) "$runtimeHome/jre/bin" else null,
            Os.getenv("PATH")
        )

        envMap().let { map ->
            map["POJAV_NATIVEDIR"] = PathManager.DIR_NATIVE_LIB
            map["JAVA_HOME"] = getJavaHome()
            map["HOME"] = PathManager.DIR_FILES_EXTERNAL.absolutePath
            map["TMPDIR"] = PathManager.DIR_CACHE.absolutePath
            map["LD_LIBRARY_PATH"] = getLibraryPath()
            map["PATH"] = path.joinToString(":")
            map["AWTSTUB_WIDTH"] = screenSize.width.toString()
            map["AWTSTUB_HEIGHT"] = screenSize.height.toString()
            map["MOD_ANDROID_RUNTIME"] = PathManager.DIR_RUNTIME_MOD?.absolutePath ?: ""
            map["DALVIK_JAVAVM"] = ZLBridge.getJavaVMPointer().toString()
            map["DALVIK_APPLICATION"] = ZLBridge.jObjectToString(GlobalContext.applicationContext)
            map["ALSOFT_DRIVERS"] = "opensl"

            if (AllSettings.dumpShaders.getValue()) map["LIBGL_VGPU_DUMP"] = "1"
            if (AllSettings.zinkPreferSystemDriver.getValue()) map["POJAV_ZINK_PREFER_SYSTEM_DRIVER"] = "1"
            if (AllSettings.vsyncInZink.getValue()) map["POJAV_VSYNC_IN_ZINK"] = "1"

            if (FFmpegPluginManager.isAvailable) map["POJAV_FFMPEG_PATH"] = FFmpegPluginManager.executablePath!!
        }
    }

    private fun dlopenJavaRuntime() {
        val baseDir = if (File(runtimeHome, "jre").isDirectory) "$runtimeHome/jre" else runtimeHome
        val javaLibDir = "$baseDir${getJavaLibDir()}"

        val jliCandidate = listOf(
            "$javaLibDir/jli/libjli.so",
            "$javaLibDir/libjli.so",
            "$baseDir/lib/jli/libjli.so",
            "$baseDir/lib/libjli.so",
            "$runtimeHome/lib/jli/libjli.so",
            "$runtimeHome/lib/libjli.so",
            "$runtimeHome/lib/aarch64/jli/libjli.so",
            "$runtimeHome/lib/aarch32/jli/libjli.so",
            "$runtimeHome/lib/arm/jli/libjli.so",
            "$runtimeHome/jre/lib/jli/libjli.so",
            "$runtimeHome/jre/lib/libjli.so",
            "$runtimeHome/jre/lib/aarch64/jli/libjli.so",
            "$runtimeHome/jre/lib/aarch32/jli/libjli.so",
            "$runtimeHome/jre/lib/arm/jli/libjli.so"
        ).map { File(it) }.firstOrNull { it.exists() && it.isFile }
            ?: locateLibs(File(runtimeHome)).firstOrNull { it.name == "libjli.so" }

        val jvmCandidate = listOf(
            "$javaLibDir/server/libjvm.so",
            "$javaLibDir/client/libjvm.so",
            "$javaLibDir${getJvmLibDir()}/libjvm.so",
            "$baseDir/lib/server/libjvm.so",
            "$baseDir/lib/client/libjvm.so",
            "$runtimeHome/lib/server/libjvm.so",
            "$runtimeHome/lib/client/libjvm.so",
            "$runtimeHome/lib/aarch64/server/libjvm.so",
            "$runtimeHome/lib/aarch32/client/libjvm.so",
            "$runtimeHome/lib/aarch32/server/libjvm.so",
            "$runtimeHome/jre/lib/server/libjvm.so",
            "$runtimeHome/jre/lib/client/libjvm.so",
            "$runtimeHome/jre/lib/aarch64/server/libjvm.so",
            "$runtimeHome/jre/lib/aarch32/client/libjvm.so"
        ).map { File(it) }.firstOrNull { it.exists() && it.isFile }
            ?: locateLibs(File(runtimeHome)).firstOrNull { it.name == "libjvm.so" }

        if (jliCandidate != null) {
            ZLBridge.dlopen(jliCandidate.absolutePath)
        } else {
            val fallback = listOf(
                File(javaLibDir, "libjli.so"),
                File(baseDir, "lib/libjli.so"),
                File(runtimeHome, "lib/libjli.so")
            ).firstOrNull { it.exists() }
            if (fallback != null) ZLBridge.dlopen(fallback.absolutePath)
        }

        if (jvmCandidate != null) {
            ZLBridge.dlopen(jvmCandidate.absolutePath)
        } else {
            val fallback = listOf(
                File("$javaLibDir/client/libjvm.so"),
                File("$javaLibDir/server/libjvm.so"),
                File("$baseDir/lib/server/libjvm.so")
            ).firstOrNull { it.exists() }
            if (fallback != null) ZLBridge.dlopen(fallback.absolutePath)
        }

        val requiredLibs = listOf(
            "libfreetype.so", "libverify.so", "libjava.so", "libnet.so",
            "libnio.so", "libawt.so", "libawt_headless.so", "libfontmanager.so"
        )
        for (lib in requiredLibs) {
            val file = File(javaLibDir, lib).takeIf { it.exists() }
                ?: File("$baseDir/lib", lib).takeIf { it.exists() }
                ?: File("$runtimeHome/lib", lib).takeIf { it.exists() }
                ?: locateLibs(File(runtimeHome)).firstOrNull { it.name == lib }
            if (file != null && file.exists()) {
                ZLBridge.dlopen(file.absolutePath)
            }
        }

        locateLibs(File(runtimeHome)).forEach { file ->
            ZLBridge.dlopen(file.absolutePath)
        }
    }

    @CallSuper
    protected open fun dlopenEngine() {
        ZLBridge.dlopen("${PathManager.DIR_NATIVE_LIB}/libopenal.so")
    }
}

fun getCacioJavaArgs(
    screenSize: IntSize,
    isJava8: Boolean
): List<String> {
    val argsList: MutableList<String> = ArrayList()

    // Caciocavallo config AWT-enabled version
    argsList.add("-Djava.awt.headless=false")
    argsList.add("-Dcacio.managed.screensize=${screenSize.width}x${screenSize.height}")
    argsList.add("-Dcacio.font.fontmanager=sun.awt.X11FontManager")
    argsList.add("-Dcacio.font.fontscaler=sun.font.FreetypeFontScaler")
    argsList.add("-Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel")
    if (isJava8) {
        argsList.add("-Dawt.toolkit=net.java.openjdk.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=net.java.openjdk.cacio.ctc.CTCGraphicsEnvironment")
    } else {
        argsList.add("-Dawt.toolkit=com.github.caciocavallosilano.cacio.ctc.CTCToolkit")
        argsList.add("-Djava.awt.graphicsenv=com.github.caciocavallosilano.cacio.ctc.CTCGraphicsEnvironment")
        argsList.add("-javaagent:${LibPath.CACIO_17_AGENT.absolutePath}")

        argsList.add("--add-exports=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.event=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED")
        argsList.add("--add-exports=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-exports=java.base/sun.security.action=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.util=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED")
        argsList.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED")
        argsList.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED")

        // Opens the java.net package to Arc DNS injector on Java 9+
        argsList.add("--add-opens=java.base/java.net=ALL-UNNAMED")
    }

    val cacioClassPath = StringBuilder()
    cacioClassPath.append("-Xbootclasspath/").append(if (isJava8) "p" else "a")
    val cacioFiles = if (isJava8) LibPath.CACIO_8 else LibPath.CACIO_17
    cacioFiles.listFiles()?.onEach {
        if (it.name.endsWith(".jar")) cacioClassPath.append(":").append(it.absolutePath)
    }

    argsList.add(cacioClassPath.toString())

    return argsList
}

/**
 * 获取设备 SoC 名称，在 API 31+ 读取系统属性 ro.soc.model，若失败则返回 Build.HARDWARE
 */
fun getSocName(): String {
    return runCatching {
        ProcessBuilder("getprop", "ro.soc.model")
            .start()
            .inputStream
            .bufferedReader()
            .use { reader ->
                reader.readLine()
            }
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: Build.HARDWARE
}
