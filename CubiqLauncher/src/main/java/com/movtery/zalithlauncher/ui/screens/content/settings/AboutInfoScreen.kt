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
                            text = "🎮 TOUCH CONTROLS & EDITORS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Multi-touch joystick conflict fixed: Sprint, look around, and tap attack (PRI) / mine at the same time without losing control.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Mojo Launcher Controls perfected: Clean two-line corner hint placement that never collides with the Mouse button, plus an ultra-responsive 90ms properties sidebar.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Zalith Launcher 1 (ZL1) Controls: Sleek deep dark canvas (#121214), dark rounded-square gear menu button, and eliminated phantom color picker popups on button clicks.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Authentic ZL1 Button Settings: Equal-width size inputs (91.0 x 91.0), compressed sidebar layout, and seamless control menu styling.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Bedrock Touch Gestures: Added dedicated toggle for Bedrock-style tap-to-place and hold-to-break block interactions.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Layout naming & creation fix: Default layouts now show as 'Mojo Launcher Controls' and 'Zalith Launcher 1 Controls', and '+New Layout' saves your custom name instead of random hex codes.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Normalized control button scale and opacity slider widths across settings.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Full Keyboard & Mouse remapping cards added directly in Cubiq settings.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🖥️ UI, HOMESCREEN & LANDSCAPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Official 3D Steve block-head settings icon matched across portrait bottom nav and landscape sidebar.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Landscape Hero Card Polish: Excluded Files, Multiplayer, and Rotation buttons from overlapping the hero card's green highlighter boundary.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Hero Artwork Carousel: 23 official artwork themes with smooth forward-only auto-scrolling and authentic green highlighter borders.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• MJ Launcher styled green rectangular switches and block-thumb seekbars across all menus and control editors.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Completely resolved dark text on dark surfaces in version selectors and dialog fields.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📦 DOWNLOADS HUB & CONTENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Category filter dropdowns restored across Modpacks, Mods, Resource Packs, Shaders, and Saves on both Modrinth and CurseForge.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• 1-Tap file associations: Direct .jar mod, .mrpack, and resource pack/world import right from Android's 'Open with' menu.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ RENDERERS & PERFORMANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• LTW (Large Thin Wrapper) renderer crash fixed: Corrected SDL system EGL driver resolution so LTW runs smoothly on MC 1.14 - 1.21+ with Sodium and Iris.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• High-performance G1GC garbage collection tuning for smooth 60+ FPS gameplay with zero micro-stutters.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Instant file manager: Zero-lag cold start for browsing files, configs, and logs.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎨 SKINS & MULTIPLAYER",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF55FF55)
                        )
                        Text(
                            text = "• Decoupled 3px (Slim) and 4px (Classic) arm models from character skins: Toggling arm size now stays on Steve or Alex instead of swapping characters.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Fixed LAN discovery with Android Wi-Fi Multicast locks so local worlds show up instantly.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Fixed offline player UUIDs for cracked servers with standard RFC-4122 compliance.",
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