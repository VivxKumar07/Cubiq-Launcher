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



package com.movtery.zalithlauncher.ui.screens.content.settings



import androidx.compose.foundation.Image

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.RowScope

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button

import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.alpha

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.painter.Painter

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.style.TextDecoration

import androidx.compose.ui.unit.IntOffset

import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import coil3.request.ImageRequest

import com.movtery.zalithlauncher.BuildConfig

import com.movtery.zalithlauncher.BuildKeys

import com.movtery.zalithlauncher.R

import com.movtery.zalithlauncher.game.plugin.ApkPlugin

import com.movtery.zalithlauncher.game.plugin.PluginLoader

import com.movtery.zalithlauncher.game.plugin.appCacheIcon

import com.movtery.zalithlauncher.library.LibraryInfo

import com.movtery.zalithlauncher.library.libraryData

import com.movtery.zalithlauncher.path.URL_COMMUNITY

import com.movtery.zalithlauncher.path.URL_MCMOD

import com.movtery.zalithlauncher.path.URL_PROJECT

import com.movtery.zalithlauncher.path.URL_SUPPORT

import com.movtery.zalithlauncher.path.URL_WEBLATE

import com.movtery.zalithlauncher.ui.base.BaseScreen

import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn

import com.movtery.zalithlauncher.ui.components.CardTitleLayout

import com.movtery.zalithlauncher.ui.screens.NestedNavKey

import com.movtery.zalithlauncher.ui.screens.NormalNavKey

import com.movtery.zalithlauncher.ui.screens.TitledNavKey

import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition

import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard

import com.movtery.zalithlauncher.ui.theme.itemColor

import com.movtery.zalithlauncher.ui.theme.onItemColor



@Composable

fun AboutInfoScreen(

    key: NestedNavKey.Settings,

    settingsScreenKey: TitledNavKey?,

    mainScreenKey: TitledNavKey?,

    checkUpdate: () -> Unit,

    openLicense: (raw: Int) -> Unit,

    openLink: (url: String) -> Unit

) {

    BaseScreen(

        Triple(key, mainScreenKey, false),

        Triple(NormalNavKey.Settings.AboutInfo, settingsScreenKey, false)

    ) { isVisible ->

        AnimatedLazyColumn(

            modifier = Modifier.fillMaxSize(),

            isVisible = isVisible,

            contentPadding = PaddingValues(all = 12.dp)

        ) { scope ->

            animatedItem(scope) { yOffset ->

                ChunkLayout(

                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },

                    title = stringResource(R.string.about_launcher_title)

                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        ButtonIconItem(

                            icon = painterResource(R.drawable.img_launcher),

                            title = BuildKeys.LAUNCHER_NAME,

                            text = "v${BuildConfig.VERSION_NAME.removeSuffix("-debug")} (Release) • Unofficial Fork of Zalith Launcher 2",
                            button = {

                                Button(

                                    onClick = { openLink(URL_PROJECT) }

                                ) {

                                    Text(text = "GitHub")

                                }

                                Button(

                                    onClick = { openLink(com.movtery.zalithlauncher.path.URL_DISCORD) }

                                ) {

                                    Text(text = "Discord")

                                }

                            }

                        )



                        Text(

                            text = "Notice: Cubiq Launcher is an unofficial version (fork) of Zalith Launcher 2, modified under the GNU General Public License v3.",

                            style = MaterialTheme.typography.bodySmall,

                            color = MaterialTheme.colorScheme.onSurfaceVariant

                        )

                    }

                }

            }



            animatedItem(scope) { yOffset ->

                ChunkLayout(

                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },

                    title = "What's New in v1.2.0"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🦀 HORIZON & MINECRAFT RUST EDITION",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Direct Launch: Tapping Play on Rust Edition starts the game instantly without opening any intermediate launcher screens.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Custom Loading Screen: Seamless black launch splash with 'Cubiq Launcher — Minecraft Java but in Rust'.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Dedicated Rust Settings & Mods: Tune Vulkan/OpenGL renderers, resolution scaling, worker threads, and toggle .hmod mods directly in Cubiq.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• One-Tap File Repair: Instant 'Re-check files' to verify engine tables and assets if anything ever gets corrupted.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎮 CONTROLS, GYROSCOPE & MOUSE REMAPPING",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Universal ZL1 Gyroscope Default: ZL1 gyro engine is now enabled by default and serves as the sole gyro mechanism across ALL control schemes (ZL1, ZL2, Mojo, Pocket Edition).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Zalith Launcher 1 (ZL1) Controls Restored: ZL1 control layout is preserved and unpacked by default alongside ZL2, Mojo Launcher, and Pocket Edition.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Interactive Key & Mouse Remap Popup: Remapping now opens a dedicated popup with 1-tap hardware button/key detection and category tabs.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Custom Mouse Cursor Packs: Choose from Classic, Obsidian, Crosshair, Bedrock Arrow, Emerald, or import any custom .png/.zip cursor.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Fixed Hotbar Touch Desync: Resolved coordinate mismatch on scaled resolutions (like 60%) so hotbar slot taps always register accurately.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• No More Double Typing: Fixed chat typing where phone keyboards typed duplicate letters (like '//' instead of '/').",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "☕ JAVA EDITION & MINECRAFT 26.3 LINKER FIX",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Native Library Resolution: Resolved java.library.path and Pojav library loader to explicitly include APK DIR_NATIVE_LIB, permanently fixing libpojavexec.so and libSDL3.so linking errors.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Rust Minecraft (Horizon) CWD Fix: Set process working directory and HOME/HORIZON_HOME environment variables, resolving (os error 2) resource preparation failure.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Zero Regressions: Complete compatibility across legacy and modern versions, with full support for LTW, MobileGlues, and Krypton renderers.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📱 SYSTEM BARS & ELY.BY DIALOG FIXES",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Portrait Mode System Bars: Restored Android status bar and bottom gestural hint bar in portrait mode without awkward letterboxing.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Landscape Ely.by Login Dialog: Soft keyboard resize with IME padding keeps input fields visible, and Back press dismisses keyboard instead of the entire dialog.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ RENDERERS & PERFORMANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• 'Follow Global' Fix: Changing your profile renderer no longer overrides a version's 'Follow Global' setting on relaunch.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Full Renderer Library: Out-of-the-box support for LTW, Zink, Angle, VirGL, and MobileGlues with auto-detection and G1GC optimization.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📦 DOWNLOADS HUB & MULTIPLAYER",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Restored Category Filter: Brought back the ZL2 Category dropdown (Multiplayer, Combat, Biomes, Dimensions, Optimization, etc.) in the landscape sidebar for Mods, Modpacks, and Resource Packs.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Skillora SMP Built-In: Added Skillora SMP (paid51.skilloraclouds.com:20015) to default multiplayer server presets.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Cleaner Capture Paths: In-game recordings save cleanly to Movies/Cubiq and screenshots to Pictures/Cubiq with saved path alerts.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👤 ACCOUNTS & UI REFINEMENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Fast Ely.by Sign-in: Redesigned the Ely.by dialog for landscape, added direct register links, and made auth take 1-2 seconds with background skin caching.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Offline Session Fallback: If auth servers are down or tokens expire, easily skip refresh and launch right away.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Clean Hero Presentation: Single clean artwork card when one version is installed, and authentic 3D Steve icon in navigation.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }



            animatedItem(scope) { yOffset ->

                ChunkLayout(

                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },

                    title = stringResource(R.string.about_acknowledgements_title)

                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        ButtonIconItem(

                            icon = painterResource(R.drawable.img_avatar_bangbang93),

                            title = "bangbang93",

                            text = stringResource(R.string.about_acknowledgements_bangbang93_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            button = {

                                Button(

                                    onClick = { openLink("https://ifdian.net/a/bangbang93") }

                                ) {

                                    Text(text = stringResource(R.string.about_sponsor))

                                }

                            }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_launcher_fcl),

                            title = "Fold Craft Launcher",

                            text = stringResource(R.string.about_acknowledgements_fcl_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            openLicense = { openLicense(R.raw.fcl_license) },

                            openLink = { openLink("https://github.com/FCL-Team/FoldCraftLauncher") }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_launcher_hmcl),

                            title = "Hello Minecraft! Launcher",

                            text = stringResource(R.string.about_acknowledgements_hmcl_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            openLicense = { openLicense(R.raw.hmcl_license) },

                            openLink = { openLink("https://github.com/HMCL-dev/HMCL") }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_platform_mcmod),

                            title = stringResource(R.string.about_acknowledgements_mcmod),

                            text = stringResource(R.string.about_acknowledgements_mcmod_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            openLink = { openLink(URL_MCMOD) }

                        )

                        ButtonIconItem(

                            icon = painterResource(R.drawable.img_avatar_mcim),

                            title = "mcmod-info-mirror",

                            text = stringResource(R.string.about_acknowledgements_mcim_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            button = {

                                Button(

                                    onClick = { openLink("https://www.mcimirror.top/sponsor") }

                                ) {

                                    Text(text = stringResource(R.string.about_sponsor))

                                }

                            }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_launcher_pcl2),

                            title = "Plain Craft Launcher 2",

                            text = stringResource(R.string.about_acknowledgements_pcl_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            openLink = { openLink("https://github.com/Meloong-Git/PCL") }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_launcher_pojav),

                            title = "PojavLauncher",

                            text = stringResource(R.string.about_acknowledgements_pojav_text, BuildKeys.LAUNCHER_SHORT_NAME),

                            openLicense = { openLicense(R.raw.lgpl_3_license) },

                            openLink = { openLink("https://github.com/PojavLauncherTeam/PojavLauncher") }

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.ic_github),

                            title = stringResource(R.string.about_acknowledgements_github_community),

                            text = stringResource(R.string.about_acknowledgements_github_community_text),

                            openLink = { openLink(URL_COMMUNITY) },

                            useImage = false

                        )

                        LinkIconItem(

                            icon = painterResource(R.drawable.img_platform_weblate),

                            title = stringResource(R.string.about_acknowledgements_weblate_community),

                            text = stringResource(R.string.about_acknowledgements_weblate_community_text),

                            openLink = { openLink(URL_WEBLATE) }

                        )

                    }

                }

            }



            //额外依赖库板块

            animatedItem(scope) { yOffset ->

                ChunkLayout(

                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },

                    title = stringResource(R.string.about_library_title)

                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        libraryData.forEach { info ->

                            LibraryInfoItem(info = info, openLicense = openLicense, openLink = openLink)

                        }

                    }

                }

            }



            //已加载插件板块

            PluginLoader.allPlugins.takeIf { it.isNotEmpty() }?.let { allPlugins ->

                animatedItem(scope) { yOffset ->

                    ChunkLayout(

                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },

                        title = stringResource(R.string.about_plugin_title)

                    ) {

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            allPlugins.forEach { apkPlugin ->

                                PluginInfoItem(apkPlugin = apkPlugin)

                            }

                        }

                    }

                }

            }

        }

    }

}



@Composable

private fun ChunkLayout(

    modifier: Modifier = Modifier,

    title: String,

    content: @Composable () -> Unit

) {

    SettingsCard(

        modifier = modifier,

        position = CardPosition.Single

    ) {

        Column(modifier = Modifier.fillMaxWidth()) {

            CardTitleLayout {

                Text(

                    modifier = Modifier.padding(all = 16.dp),

                    text = title,

                    style = MaterialTheme.typography.titleSmall,

                    color = MaterialTheme.colorScheme.onSurface

                )

            }

            Column(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(all = 12.dp)

            ) {

                content()

            }

        }

    }

}



@Composable

private fun LinkIconItem(

    modifier: Modifier = Modifier,

    icon: Painter,

    title: String,

    text: String,

    openLicense: (() -> Unit)? = null,

    openLink: (() -> Unit)? = null,

    color: Color = itemColor(),

    contentColor: Color = onItemColor(),

    useImage: Boolean = true

) {

    Surface(

        modifier = modifier,

        color = color,

        contentColor = contentColor,

        shape = MaterialTheme.shapes.large,

        onClick = {}

    ) {

        Row(

            modifier = Modifier

                .padding(horizontal = 14.dp, vertical = 8.dp)

                .fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(12.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            val iconModifier = Modifier

                .size(34.dp)

                .clip(shape = RoundedCornerShape(6.dp))

            if (useImage) {

                Image(

                    modifier = iconModifier,

                    painter = icon,

                    contentDescription = null,

                    contentScale = ContentScale.Fit

                )

            } else {

                Icon(

                    modifier = iconModifier,

                    painter = icon,

                    contentDescription = null

                )

            }



            Column(

                modifier = Modifier.weight(1f)

            ) {

                Text(

                    text = title,

                    style = MaterialTheme.typography.titleSmall

                )

                Text(

                    modifier = Modifier.alpha(0.7f),

                    text = text,

                    style = MaterialTheme.typography.bodySmall

                )

            }



            Row {

                openLicense?.let {

                    IconButton(

                        onClick = it

                    ) {

                        Icon(

                            modifier = Modifier.size(22.dp),

                            painter = painterResource(R.drawable.ic_copyright_outlined),

                            contentDescription = "License"

                        )

                    }

                }

                openLink?.let {

                    IconButton(

                        onClick = it

                    ) {

                        Icon(

                            painter = painterResource(R.drawable.ic_link),

                            contentDescription = stringResource(R.string.generic_open_link)

                        )

                    }

                }

            }

        }

    }

}



@Composable

private fun ButtonIconItem(

    modifier: Modifier = Modifier,

    icon: Painter,

    title: String,

    text: String,

    button: @Composable RowScope.() -> Unit,

    color: Color = itemColor(),

    contentColor: Color = onItemColor(),

) {

    Surface(

        modifier = modifier,

        color = color,

        contentColor = contentColor,

        shape = MaterialTheme.shapes.large,

        onClick = {}

    ) {

        Row(

            modifier = Modifier

                .padding(horizontal = 14.dp, vertical = 8.dp)

                .fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(12.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Image(

                modifier = Modifier

                    .size(34.dp)

                    .clip(shape = RoundedCornerShape(6.dp)),

                painter = icon,

                contentDescription = null,

                contentScale = ContentScale.Fit

            )



            Column(

                modifier = Modifier.weight(1f)

            ) {

                Text(

                    text = title,

                    style = MaterialTheme.typography.titleSmall

                )

                Text(

                    modifier = Modifier.alpha(0.7f),

                    text = text,

                    style = MaterialTheme.typography.bodySmall

                )

            }



            button()

        }

    }

}



@Composable

private fun PluginInfoItem(

    apkPlugin: ApkPlugin,

    modifier: Modifier = Modifier,

    color: Color = itemColor(),

    contentColor: Color = onItemColor(),

) {

    Surface(

        modifier = modifier,

        color = color,

        contentColor = contentColor,

        shape = MaterialTheme.shapes.large,

        onClick = {}

    ) {

        val context = LocalContext.current

        Row(

            modifier = Modifier

                .padding(all = 12.dp)

                .fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            val iconFile = appCacheIcon(apkPlugin.packageName)

            if (iconFile.exists()) {

                val model = remember(context, iconFile) {

                    ImageRequest.Builder(context)

                        .data(iconFile)

                        .build()

                }

                AsyncImage(

                    modifier = Modifier

                        .size(34.dp)

                        .clip(shape = RoundedCornerShape(8.dp)),

                    model = model,

                    contentDescription = null,

                    contentScale = ContentScale.Fit

                )

            } else {

                Image(

                    modifier = Modifier.size(34.dp),

                    painter = painterResource(R.drawable.ic_unknown_icon),

                    contentDescription = null,

                    contentScale = ContentScale.Fit

                )

            }



            Column(

                modifier = Modifier.align(Alignment.CenterVertically)

            ) {

                Text(

                    text = apkPlugin.appName,

                    style = MaterialTheme.typography.titleSmall

                )

                Row(

                    modifier = Modifier.alpha(0.7f),

                    horizontalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Text(

                        text = apkPlugin.packageName,

                        style = MaterialTheme.typography.bodySmall

                    )

                    if (apkPlugin.appVersion.isNotEmpty()) {

                        Text(

                            text = apkPlugin.appVersion,

                            style = MaterialTheme.typography.bodySmall

                        )

                    }

                }

            }

        }

    }

}



@Composable

private fun LibraryInfoItem(

    info: LibraryInfo,

    modifier: Modifier = Modifier,

    color: Color = itemColor(),

    contentColor: Color = onItemColor(),

    openLicense: (Int) -> Unit,

    openLink: (url: String) -> Unit

) {

    Surface(

        modifier = modifier,

        color = color,

        contentColor = contentColor,

        shape = MaterialTheme.shapes.large,

        onClick = {}

    ) {

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .padding(horizontal = 14.dp, vertical = 8.dp)

        ) {

            Column(

                modifier = Modifier.weight(1f),

                verticalArrangement = Arrangement.Center

            ) {

                Text(

                    text = info.name,

                    style = MaterialTheme.typography.titleSmall

                )

                Column(

                    modifier = Modifier.alpha(0.7f)

                ) {

                    info.copyrightInfo?.let { copyrightInfo ->

                        Text(

                            text = copyrightInfo,

                            style = MaterialTheme.typography.bodySmall

                        )

                    }

                    Text(

                        modifier = Modifier.clickable(

                            onClick = {

                                openLicense(info.license.raw)

                            }

                        ),

                        text = "Licensed under the ${info.license.name}",

                        style = MaterialTheme.typography.bodySmall.copy(

                            textDecoration = TextDecoration.Underline

                        )

                    )

                }

            }

            IconButton(

                modifier = Modifier.align(Alignment.CenterVertically),

                onClick = {

                    openLink(info.webUrl)

                }

            ) {

                Icon(

                    painter = painterResource(R.drawable.ic_link),

                    contentDescription = null

                )

            }

        }

    }

}