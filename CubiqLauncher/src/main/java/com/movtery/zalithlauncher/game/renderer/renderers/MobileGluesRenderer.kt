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

package com.movtery.zalithlauncher.game.renderer.renderers

import android.content.Context
import android.content.pm.ApplicationInfo
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.plugin.Plugin
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.path.PathManager
import java.io.File

object MobileGluesRenderer : RendererInterface, Plugin {
    const val PACKAGE_NAME = "com.fcl.plugin.mobileglues"

    override fun getIdentifier(): String = PACKAGE_NAME

    override fun getNativeLibPath(): String {
        return getInstalledPluginNativePath() ?: PathManager.DIR_NATIVE_LIB
    }

    override fun getRendererId(): String = "opengles3_mobileglues"

    override fun getUniqueIdentifier(): String = PACKAGE_NAME

    override fun getRendererName(): String = "MobileGlues"

    override fun getRendererSummary(): String = "Advanced mobile OpenGL ES translation layer with deep shader support (BSL/Iris/Create mod). Requires companion app."

    override fun getCompatibilityDescription(): String = "MC 1.16.5 – 1.21.4+ • Shaders & Heavy Mods Support"

    override fun getMaxMCVersion(): String? = null

    override fun getDisplayMaxMCVersion(): String? = null

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "POJAV_RENDERER" to "opengles3_mobileglues",
            "LIBGL_ES" to "3"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy {
        val path = getInstalledPluginNativePath()
        if (path != null) listOf("$path/libmobileglues.so") else emptyList()
    }

    override fun getRendererLibrary(): String {
        val path = getInstalledPluginNativePath()
        return if (path != null) "$path/libmobileglues.so" else "libmobileglues.so"
    }

    override fun getRendererEGL(): String? = null

    @JvmStatic
    fun isInstalled(context: Context = GlobalContext): Boolean {
        return getInstalledPluginInfo(context) != null
    }

    @JvmStatic
    fun getInstalledPluginInfo(context: Context = GlobalContext): ApplicationInfo? {
        return runCatching {
            context.packageManager.getApplicationInfo(PACKAGE_NAME, 0)
        }.getOrNull()
    }

    @JvmStatic
    fun getInstalledPluginNativePath(context: Context = GlobalContext): String? {
        val nativeDir = getInstalledPluginInfo(context)?.nativeLibraryDir ?: return null
        val soFile = File(nativeDir, "libmobileglues.so")
        return if (soFile.exists()) nativeDir else nativeDir
    }
}
