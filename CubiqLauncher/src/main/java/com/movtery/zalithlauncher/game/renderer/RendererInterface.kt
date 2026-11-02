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

package com.movtery.zalithlauncher.game.renderer

import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * 启动器渲染器实现
 */
interface RendererInterface {
    /**
     * 获取渲染器的ID
     */
    fun getRendererId(): String

    /**
     * 获取渲染器的唯一标识ID
     */
    fun getUniqueIdentifier(): String

    /**
     * 获取渲染器的名称
     */
    fun getRendererName(): String

    /**
     * 获取渲染器的描述
     */
    fun getRendererSummary(): String? = null

    /**
     * 获取渲染器最低兼容版本
     */
    fun getMinMCVersion(): String? = null

    /**
     * 获取渲染器最高兼容版本
     */
    fun getMaxMCVersion(): String? = null

    /**
     * Whether this renderer supports the selected Minecraft version.
     * Versions without a recognizable numeric version remain unrestricted for plugin compatibility.
     */
    fun isCompatibleWith(minecraftVersion: String, context: android.content.Context? = null): Boolean {
        val parsedVersion = Regex("""^\s*(\d+(?:\.\d+){1,2})""")
            .find(minecraftVersion)
            ?.groupValues
            ?.getOrNull(1)
            ?: return true

        if (getUniqueIdentifier() == com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.PACKAGE_NAME && context != null) {
            if (!com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.isInstalled(context)) {
                return false
            }
        }

        val minVersion = getMinMCVersion()?.let {
            Regex("""^\s*(\d+(?:\.\d+){1,2})""").find(it)?.groupValues?.getOrNull(1) ?: it
        }
        val maxVersion = getMaxMCVersion()?.let {
            Regex("""^\s*(\d+(?:\.\d+){1,2})""").find(it)?.groupValues?.getOrNull(1) ?: it
        }
        val isAtLeastMin = minVersion == null || GameVersionNumber.compare(parsedVersion, minVersion) >= 0
        val isAtMostMax = maxVersion == null || GameVersionNumber.compare(parsedVersion, maxVersion) <= 0
        return isAtLeastMin && isAtMostMax
    }

    /**
     * 获取渲染器最低兼容版本的展示用版本号
     */
    fun getDisplayMinMCVersion(): String? = getMinMCVersion()

    /**
     * 获取渲染器最高兼容版本的展示用版本号
     */
    fun getDisplayMaxMCVersion(): String? = getMaxMCVersion()

    /**
     * 是否为官方推荐选项
     */
    fun isRecommended(): Boolean = false

    /**
     * 获取渲染器兼容性说明（展示最佳兼容的 Minecraft 版本及架构信息）
     */
    fun getCompatibilityDescription(): String {
        val min = getDisplayMinMCVersion()
        val max = getDisplayMaxMCVersion()
        return when {
            min != null && max != null -> "MC $min – $max"
            min != null -> "MC $min+"
            max != null -> "MC <= $max"
            else -> "All MC Versions"
        }
    }

    /**
     * 获取渲染器的环境变量
     */
    fun getRendererEnv(): Lazy<Map<String, String>>

    /**
     * 获取需要dlopen的库
     */
    fun getDlopenLibrary(): Lazy<List<String>>

    /**
     * 获取渲染器的库
     */
    fun getRendererLibrary(): String

    /**
     * 获取EGL名称
     */
    fun getRendererEGL(): String? = null
}