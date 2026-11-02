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

import com.movtery.zalithlauncher.game.renderer.RendererInterface

object KopperZinkRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_desktopgl_zink_kopper"

    override fun getUniqueIdentifier(): String = "0fa435e2-46df-45c9-906c-b29606aaef00"

    override fun getRendererName(): String = "KopperZink"

    override fun getRendererSummary(): String = "Desktop-grade Vulkan pipeline for 1.17+. Best for Sodium and shaders."

    override fun getMinMCVersion(): String = "1.17"

    override fun getCompatibilityDescription(): String = "MC 1.17+"

    override fun getMaxMCVersion(): String? = null

    override fun getDisplayMaxMCVersion(): String? = null

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "MESA_LOADER_DRIVER_OVERRIDE" to "zink",
            "GALLIUM_DRIVER" to "zink",
            "LIBGL_ES" to "3"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libglxshim.so"

    override fun getRendererEGL(): String = "libEGL_mesa.so"
}