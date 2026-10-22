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

object MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_mobileglues"

    override fun getUniqueIdentifier(): String = "com.fcl.plugin.mobileglues"

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

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"

    override fun getRendererEGL(): String = "libmobileglues.so"
}
