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

object LTWRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_ltw"

    override fun getUniqueIdentifier(): String = "d2c1e8b4-55a1-4e78-9032-11cf4e87d3a1"

    override fun getRendererName(): String = "LTW (Large Thin Wrapper)"

    override fun getRendererSummary(): String = "High-performance OpenGL-to-ES wrapper optimized for modern Minecraft 1.20+ and optimization mods (Sodium/Iris)."

    override fun isRecommended(): Boolean = true

    override fun getCompatibilityDescription(): String = "MC 1.17 – 1.21.4+ • Recommended for modern versions & Sodium"

    override fun getMaxMCVersion(): String? = null

    override fun getDisplayMaxMCVersion(): String? = null

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "POJAV_RENDERER" to "opengles3_ltw",
            "LIBGL_ES" to "3"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libltw.so"

    override fun getRendererEGL(): String = "libltw.so"
}
