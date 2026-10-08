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

package com.movtery.zalithlauncher.game.version.installed.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import java.io.File

private const val TAG = "VersionInfoUtils"

private const val VERSION_PATTERN = """(\d+\.\d+(?:\.\d+)?|\d{2}w\d{2}[a-z]|\d+\.\d+-(?:pre|rc|alpha|beta)[\d.]*)"""

// "1.20.4-OptiFine_HD_U_I7_pre3"       -> 1.20.4
// "1.21.3-OptiFine_HD_U_J2_pre6"       -> 1.21.3
private val OPTIFINE_ID_REGEX = """$VERSION_PATTERN-OptiFine""".toRegex()
// "1.20.2-forge-48.1.0"                -> 1.20.2
// "1.21.3-forge-53.0.23"               -> 1.21.3
private val FORGE_REGEX = """$VERSION_PATTERN-forge""".toRegex()
// "1.7.10-Forge10.13.4.1614-1.7.10"    -> 1.7.10
// "1.7.2-10.12.2.1161-mc172"           -> 1.7.2
private val FORGE_OLD_REGEX = """^$VERSION_PATTERN-(Forge[\d.]*)-mc\d+""".toRegex()
// "fabric-loader-0.15.7-1.20.4"        -> 1.20.4
// "fabric-loader-0.16.9-1.21.3"        -> 1.21.3
private val FABRIC_REGEX = """fabric-loader-[\w.-]+-$VERSION_PATTERN""".toRegex()
// "quilt-loader-0.23.1-1.20.4"         -> 1.20.4
// "quilt-loader-0.27.1-beta.1-1.21.3"  -> 1.21.3
private val QUILT_REGEX = """quilt-loader-[\w.-]+-$VERSION_PATTERN""".toRegex()

private val EXTRACT_MC_VERSION_REGEX = """(\d+\.\d+(?:\.\d+)?|\d{2}w\d{2}[a-z])""".toRegex()

private val LOADER_DETECTORS = listOf<(String) -> String?>(
    { id ->
        OPTIFINE_ID_REGEX.find(id)?.groupValues?.get(1)
    },
    { id ->
        FORGE_REGEX.find(id)?.groupValues?.get(1)
    },
    { id ->
        FORGE_OLD_REGEX.find(id)?.groupValues?.get(1)
    },
    { id ->
        FABRIC_REGEX.find(id)?.groupValues?.get(1)
    },
    { id ->
        QUILT_REGEX.find(id)?.groupValues?.get(1)
    }
)

/**
 * 在版本的json文件中，找到版本信息
 * @return 版本号、ModLoader信息
 */
fun parseJsonToVersionInfo(jsonFile: File): VersionInfo? {
    return runCatching {
        val rawJson = jsonFile.readText().trim()
        if (rawJson.isEmpty()) return null
        val jsonElement = JsonParser.parseString(rawJson)
        if (!jsonElement.isJsonObject) return null
        val jsonObject = jsonElement.asJsonObject

        val quickPlay = runCatching {
            ensureQuickPlay(jsonObject)
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to parse Quick Play", e)
            VersionInfo.QuickPlay(
                hasQuickPlaysSupport = false,
                isQuickPlaySingleplayer = false,
                isQuickPlayMultiplayer = false
            )
        }
        val (versionId, loaderInfo) = detectMinecraftAndLoader(jsonObject, jsonFile.parentFile?.name)
        VersionInfo(versionId, quickPlay, loaderInfo)
    }.getOrElse {
        Logger.error(TAG, "Error parsing version json: ${jsonFile.name}", it)
        // Fallback version info so valid files are never falsely marked as invalid
        val fallbackId = jsonFile.parentFile?.name ?: jsonFile.nameWithoutExtension
        VersionInfo(
            minecraftVersion = EXTRACT_MC_VERSION_REGEX.find(fallbackId)?.value ?: fallbackId,
            quickPlay = VersionInfo.QuickPlay(false, false, false),
            loaderInfo = null
        )
    }
}

/**
 * 确认是否可以使用 Quick Play
 */
private fun ensureQuickPlay(versionJson: JsonObject): VersionInfo.QuickPlay {
    var hasQuickPlaysSupport = false
    var isQuickPlaySingleplayer = false
    var isQuickPlayMultiplayer = false

    versionJson.getAsJsonObject("arguments")?.getAsJsonArray("game")?.forEach outerFor@{ element ->
        if (!element.isJsonObject) return@outerFor

        element.asJsonObject.getAsJsonArray("rules")?.forEach innerFor@{ rule ->
            if (!rule.isJsonObject) return@innerFor

            val features = rule.asJsonObject.getAsJsonObject("features") ?: return@innerFor

            hasQuickPlaysSupport = hasQuickPlaysSupport || features.get("has_quick_plays_support")?.asBoolean ?: false
            isQuickPlaySingleplayer = isQuickPlaySingleplayer || features.get("is_quick_play_singleplayer")?.asBoolean ?: false
            isQuickPlayMultiplayer = isQuickPlayMultiplayer || features.get("is_quick_play_multiplayer")?.asBoolean ?: false

            if (hasQuickPlaysSupport && isQuickPlaySingleplayer && isQuickPlayMultiplayer) {
                return@ensureQuickPlay VersionInfo.QuickPlay(
                    hasQuickPlaysSupport = true,
                    isQuickPlaySingleplayer = true,
                    isQuickPlayMultiplayer = true
                )
            }
        }
    }

    return VersionInfo.QuickPlay(
        hasQuickPlaysSupport = hasQuickPlaysSupport,
        isQuickPlaySingleplayer = isQuickPlaySingleplayer,
        isQuickPlayMultiplayer = isQuickPlayMultiplayer
    )
}

private fun detectMinecraftAndLoader(versionJson: JsonObject, folderName: String? = null): Pair<String, VersionInfo.LoaderInfo?> {
    val mcVersion = extractMinecraftVersion(versionJson, folderName)
    val loaderInfo = detectModLoader(versionJson)
    return mcVersion to loaderInfo
}

private fun extractMinecraftVersion(json: JsonObject, folderName: String? = null): String {
    //尝试识别HMCL版本
    if (json.has("patches") && json.get("patches").isJsonArray) {
        val patches = json.getAsJsonArray("patches")
        if (patches.size() > 0 && patches[0].isJsonObject) {
            val minecraft = patches[0].asJsonObject
            if (minecraft.has("version") && minecraft.get("version").isJsonPrimitive) {
                return minecraft.get("version").asString
            }
        }
    }

    //尝试识别PCL导出的整合包给的版本
    if (json.has("clientVersion") && json.get("clientVersion").isJsonPrimitive) {
        val clientVersion = json.get("clientVersion").asString
        if (clientVersion.isNotEmptyOrBlank()) return clientVersion
    }

    //尝试从 LaunchFor (ZL安装的版本) 获取信息
    json.getAsJsonObject("launchFor")
        ?.getAsJsonArray("infos")
        ?.firstOrNull { it.isJsonObject && it.asJsonObject["name"]?.asString == "Minecraft" }
        ?.asJsonObject
        ?.getAsJsonPrimitive("version")
        ?.asString
        ?.let {
            return it
        }

    //从minecraft库中获取
    json.getAsJsonArray("libraries")?.forEach { libElement ->
        if (!libElement.isJsonObject) return@forEach
        val lib = libElement.asJsonObject
        val nameElement = lib.get("name") ?: return@forEach
        if (!nameElement.isJsonPrimitive) return@forEach
        val parts = nameElement.asString.split(":")
        if (parts.size >= 3) {
            val group = parts[0]
            val artifact = parts[1]
            val version = parts[2]
            if (group == "net.minecraft" && (artifact == "client" || artifact == "server")) {
                return version
            }
        }
    }

    if (json.has("inheritsFrom") && json.get("inheritsFrom").isJsonPrimitive) {
        val inherits = json.get("inheritsFrom").asString
        val extracted = EXTRACT_MC_VERSION_REGEX.find(inherits)?.value
        if (extracted != null) return extracted
        return inherits
    }

    val id = if (json.has("id") && json.get("id").isJsonPrimitive) json.get("id").asString else (folderName ?: "Unknown")

    //尝试从ID中解析MC版本
    val loaderDetected = LOADER_DETECTORS.firstNotNullOfOrNull { it(id) }
    if (loaderDetected != null) return loaderDetected

    val extractedFromId = EXTRACT_MC_VERSION_REGEX.find(id)?.value
    if (extractedFromId != null) return extractedFromId

    if (folderName != null) {
        val extractedFromFolder = EXTRACT_MC_VERSION_REGEX.find(folderName)?.value
        if (extractedFromFolder != null) return extractedFromFolder
    }

    return id
}

/**
 * 通过库判断ModLoader信息：ModLoader名称、版本
 * @param versionJson 版本json对象
 */
private fun detectModLoader(versionJson: JsonObject): VersionInfo.LoaderInfo? {
    var hasFabric = false
    var hasLegacyFabric = false
    var hasBabric = false
    var fabricLoaderVer: String? = null

    versionJson.getAsJsonArray("libraries")?.forEach { libElement ->
        if (!libElement.isJsonObject) return@forEach
        val lib = libElement.asJsonObject
        val nameElement = lib.get("name") ?: return@forEach
        if (!nameElement.isJsonPrimitive) return@forEach
        val parts = nameElement.asString.split(":")
        if (parts.size < 2) return@forEach
        val group = parts[0]
        val artifact = parts[1]
        val version = if (parts.size > 2) parts[2] else ""

        when {
            //Fabric Loader
            group == "net.fabricmc" && artifact == "fabric-loader" -> {
                hasFabric = true
                fabricLoaderVer = version
            }

            //Legacy Fabric
            group == "net.legacyfabric" && artifact == "intermediary" -> {
                hasLegacyFabric = true
            }

            //Babric
            group == "babric" && artifact == "intermediary-upstream" -> {
                hasBabric = true
            }

            //Forge
            group == "net.minecraftforge" && (artifact == "forge" || artifact == "fmlloader") -> {
                val forgeVersion = when {
                    version.count { it == '-' } == 1 -> version.substringAfterLast('-')
                    version.count { it == '-' } >= 2 -> version.split("-").let { p ->
                        when {
                            p.size >= 3 && p.last().startsWith("mc") -> p[1]
                            p.size >= 3 && p[0] == p.last() -> p[1]
                            else -> version
                        }
                    }
                    else -> version
                }
                return VersionInfo.LoaderInfo(ModLoader.FORGE, forgeVersion)
            }

            //NeoForge
            group == "net.neoforged.fancymodloader" && artifact == "loader" -> {
                val neoVersion = versionJson.getAsJsonObject("arguments")
                    ?.getAsJsonArray("game")
                    ?.findNeoForgeVersion()
                    ?: version
                return VersionInfo.LoaderInfo(ModLoader.NEOFORGE, neoVersion)
            }

            //OptiFine
            (group == "optifine" || group == "net.optifine") && artifact == "OptiFine" ->
                return VersionInfo.LoaderInfo(ModLoader.OPTIFINE, version)

            //Quilt
            group == "org.quiltmc" && artifact == "quilt-loader" ->
                return VersionInfo.LoaderInfo(ModLoader.QUILT, version)

            //LiteLoader
            group == "com.mumfrey" && artifact == "liteloader" ->
                return VersionInfo.LoaderInfo(ModLoader.LITE_LOADER, version)

            //Cleanroom
            group == "com.cleanroommc" && artifact == "cleanroom" ->
                return VersionInfo.LoaderInfo(ModLoader.CLEANROOM, version)
        }
    }

    //Fabric 全家桶
    if (hasFabric && fabricLoaderVer != null) {
        val loader = if (hasLegacyFabric) {
            ModLoader.LEGACY_FABRIC
        } else if (hasBabric) {
            ModLoader.BABRIC
        } else {
            ModLoader.FABRIC
        }
        return VersionInfo.LoaderInfo(loader, fabricLoaderVer)
    }

    return null
}

/**
 * NeoForge会将版本号存放到游戏参数内
 * 尝试在 arguments: { "game": [] } 中寻找NeoForge的版本
 */
private fun JsonArray.findNeoForgeVersion(): String? {
    val args = this.mapNotNull { it.takeIf(JsonElement::isJsonPrimitive)?.asString }
    return args.zipWithNext().find { it.first == "--fml.neoForgeVersion" }?.second
}