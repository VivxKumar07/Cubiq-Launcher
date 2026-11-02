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

object FreedrenoRenderer : RendererInterface {
    override fun getRendererId(): String = "gallium_freedreno"

    override fun getUniqueIdentifier(): String = "1ad7249f-5784-4f00-bc72-174b3578ee46"

    override fun getRendererName(): String = "FreeDreno"

    override fun getRendererSummary(): String = "Native Mesa driver for Snapdragon (Adreno) GPUs only."

    override fun getMinMCVersion(): String = "1.12.2"

    override fun getCompatibilityDescription(): String = "MC 1.12.2+"

    override fun getMaxMCVersion(): String? = null

    override fun getDisplayMaxMCVersion(): String? = null

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "GALLIUM_DRIVER" to "freedreno",
            "MESA_LOADER_DRIVER_OVERRIDE" to "freedreno"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}