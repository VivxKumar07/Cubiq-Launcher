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

object PanfrostRenderer : RendererInterface {
    override fun getRendererId(): String = "gallium_panfrost"

    override fun getUniqueIdentifier(): String = "9b2808c4-11af-4c72-a9c6-94c940396475"

    override fun getRendererName(): String = "Panfrost"

    override fun getRendererSummary(): String = "Open-source Mesa driver for ARM Mali GPUs only."

    override fun getMinMCVersion(): String = "1.12.2"

    override fun getCompatibilityDescription(): String = "MC 1.12.2+"

    override fun getMaxMCVersion(): String = "1.21.4"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "GALLIUM_DRIVER" to "panfrost",
            "MESA_LOADER_DRIVER_OVERRIDE" to "panfrost"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_2300d.so"
}