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
import com.movtery.zalithlauncher.path.PathManager
import java.io.File

object VirGLRenderer : RendererInterface {
    override fun getRendererId(): String = "gallium_virgl"

    override fun getUniqueIdentifier(): String = "a3ccc1fe-de3f-4a81-8c45-2485181b63b3"

    override fun getRendererName(): String = "VirGLRenderer"

    override fun getRendererSummary(): String = "Legacy virtualized renderer for 1.12.2–1.16.5 fallback only."

    override fun getMinMCVersion(): String = "1.12.2"

    override fun getCompatibilityDescription(): String = "MC 1.12.2 – 1.16.5"

    override fun getMaxMCVersion(): String = "1.16.5"

    override fun getDisplayMaxMCVersion(): String? = null

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "VTEST_SOCKET_NAME" to File(runCatching { PathManager.DIR_CACHE }.getOrNull() ?: File(""), ".virgl_test").absolutePath
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libOSMesa_2121.so"
}