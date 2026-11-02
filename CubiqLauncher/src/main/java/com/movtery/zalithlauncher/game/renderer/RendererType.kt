/*
 * Cubiq Launcher
 * Copyright (C) 2026 Cubiq Launcher contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.movtery.zalithlauncher.game.renderer

import android.content.Context
import android.os.Build
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.renderer.renderers.FreedrenoRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.KopperZinkRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.NGGL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.PanfrostRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.VirGLRenderer
import com.movtery.zalithlauncher.utils.isAdrenoGPU
import com.movtery.zalithlauncher.utils.isMaliGPU
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * Categorized Renderer Definitions for Cubiq Launcher.
 * Groups 8 renderers into 4 distinct categories with compatibility validation & hardware detection.
 */
enum class RendererCategory(val title: String) {
    CATEGORY_1_LEGACY("Legacy Engine (Minecraft <= 1.16.5)"),
    CATEGORY_2_MODERN("Modern Engine (1.17 to Modern/26.x)"),
    CATEGORY_3_HIGH_PERFORMANCE("High Performance & Modding (1.17 to Modern/26.x)"),
    CATEGORY_4_EXPERIMENTAL("Experimental / Hardware-Specific")
}

enum class RendererType(
    val id: String,
    val uniqueIdentifier: String,
    val displayName: String,
    val subtitle: String,
    val category: RendererCategory,
    val rendererImpl: RendererInterface
) {
    // Category 1: Legacy Engine (Minecraft <= 1.16.5)
    GL4ES(
        id = "opengles2",
        uniqueIdentifier = "8b52d82d-8f6d-4d3a-a767-dc93f8b72fc7",
        displayName = "GL4ES",
        subtitle = "Default for 1.16.5 and below. Fast and battery efficient.",
        category = RendererCategory.CATEGORY_1_LEGACY,
        rendererImpl = GL4ESRenderer
    ),
    VIRGL(
        id = "gallium_virgl",
        uniqueIdentifier = "a3ccc1fe-de3f-4a81-8c45-2485181b63b3",
        displayName = "VirGLRenderer",
        subtitle = "Virtualized fallback for 1.12.2–1.16.5 if GL4ES fails to boot.",
        category = RendererCategory.CATEGORY_1_LEGACY,
        rendererImpl = VirGLRenderer
    ),

    // Category 2: Modern Engine (1.17 to Modern/26.x)
    LTW(
        id = "opengles3_ltw",
        uniqueIdentifier = "d2c1e8b4-55a1-4e78-9032-11cf4e87d3a1",
        displayName = "LTW (Large Thin Wrapper)",
        subtitle = "OpenGL 3.3 Core to GLES layer. Low CPU usage for vanilla 1.17+.",
        category = RendererCategory.CATEGORY_2_MODERN,
        rendererImpl = LTWRenderer
    ),
    KRYPTON(
        id = "opengles3",
        uniqueIdentifier = "e7b90ed6-e518-4d4e-93dc-5c7133cd5b31",
        displayName = "Krypton Wrapper",
        subtitle = "Lightweight GLES wrapper for 1.17–1.20+. Good on older Android versions.",
        category = RendererCategory.CATEGORY_2_MODERN,
        rendererImpl = NGGL4ESRenderer
    ),
    MOBILE_GLUES(
        id = "opengles3_mobileglues",
        uniqueIdentifier = MobileGluesRenderer.PACKAGE_NAME,
        displayName = "MobileGlues",
        subtitle = "External desktop OpenGL service. Best for Mali/MediaTek chips on 1.17+.",
        category = RendererCategory.CATEGORY_2_MODERN,
        rendererImpl = MobileGluesRenderer
    ),

    // Category 3: High Performance & Modding (1.17 to Modern/26.x)
    KOPPER_ZINK(
        id = "opengles3_desktopgl_zink_kopper",
        uniqueIdentifier = "0fa435e2-46df-45c9-906c-b29606aaef00",
        displayName = "KopperZink",
        subtitle = "Desktop OpenGL over Vulkan. Required for Iris shaders; high power draw.",
        category = RendererCategory.CATEGORY_3_HIGH_PERFORMANCE,
        rendererImpl = KopperZinkRenderer
    ),
    FREEDRENO(
        id = "gallium_freedreno",
        uniqueIdentifier = "1ad7249f-5784-4f00-bc72-174b3578ee46",
        displayName = "FreeDreno",
        subtitle = "Direct open-source Gallium driver for Snapdragon Adreno GPUs.",
        category = RendererCategory.CATEGORY_3_HIGH_PERFORMANCE,
        rendererImpl = FreedrenoRenderer
    ),

    // Category 4: Experimental / Hardware-Specific
    PANFROST(
        id = "gallium_panfrost",
        uniqueIdentifier = "9b2808c4-11af-4c72-a9c6-94c940396475",
        displayName = "Panfrost",
        subtitle = "Reverse-engineered Mesa driver for ARM Mali GPUs. Unstable on stock ROMs.",
        category = RendererCategory.CATEGORY_4_EXPERIMENTAL,
        rendererImpl = PanfrostRenderer
    );

    /**
     * Checks if this renderer is compatible with the provided Minecraft version.
     * Supports semver and calendar-year versions (e.g. 1.16.5, 1.20.1, 26.1).
     */
    fun isCompatibleWithVersion(mcVersion: String): Boolean {
        val parsedVersion = Regex("""^\s*(\d+(?:\.\d+){1,2})""")
            .find(mcVersion)
            ?.groupValues
            ?.getOrNull(1) ?: mcVersion.trim()

        return when (this) {
            GL4ES, VIRGL -> {
                // Category 1: Only compatible with <= 1.16.5
                GameVersionNumber.compare(parsedVersion, "1.16.5") <= 0
            }
            LTW, KRYPTON, MOBILE_GLUES, KOPPER_ZINK -> {
                // Category 2 & 3: Only compatible with >= 1.17
                GameVersionNumber.compare(parsedVersion, "1.17") >= 0
            }
            FREEDRENO -> {
                // FreeDreno: Gallium driver >= 1.12.2
                GameVersionNumber.compare(parsedVersion, "1.12.2") >= 0
            }
            PANFROST -> {
                // Panfrost: Gallium driver >= 1.12.2 and <= 1.21.4
                GameVersionNumber.compare(parsedVersion, "1.12.2") >= 0 &&
                        GameVersionNumber.compare(parsedVersion, "1.21.4") <= 0
            }
        }
    }

    /**
     * Checks if the device's hardware supports this renderer.
     * Detects Adreno vs Mali via EGL context / Build.HARDWARE / Build.SOC_MODEL.
     */
    fun isHardwareSupported(context: Context = GlobalContext): Boolean {
        return when (this) {
            FREEDRENO -> isAdrenoGPU()
            PANFROST -> isMaliGPU()
            MOBILE_GLUES -> {
                // Requires package com.mobileglues.app (or companion plugin) to be installed
                MobileGluesRenderer.isInstalled(context)
            }
            else -> true
        }
    }

    /**
     * Inline hardware and version requirement tag for UI display.
     */
    fun getInlineWarningTag(mcVersion: String? = null, context: Context = GlobalContext): String? {
        if (!isHardwareSupported(context)) {
            return when (this) {
                FREEDRENO -> "[Snapdragon only]"
                PANFROST -> "[Mali only]"
                MOBILE_GLUES -> "[Requires MobileGlues App]"
                else -> "[Hardware unsupported]"
            }
        }

        if (mcVersion != null && !isCompatibleWithVersion(mcVersion)) {
            return when (this) {
                GL4ES, VIRGL -> "[Requires <= 1.16.5]"
                LTW, KRYPTON, MOBILE_GLUES, KOPPER_ZINK -> "[Requires >= 1.17]"
                FREEDRENO, PANFROST -> "[Requires 1.12.2+]"
            }
        }

        return null
    }

    companion object {
        fun fromUniqueIdentifier(uid: String?): RendererType {
            return entries.find { it.uniqueIdentifier == uid } ?: KRYPTON
        }

        fun fromRendererInterface(renderer: RendererInterface): RendererType {
            return entries.find { it.uniqueIdentifier == renderer.getUniqueIdentifier() } ?: KRYPTON
        }
    }
}

