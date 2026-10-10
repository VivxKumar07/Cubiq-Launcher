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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadFavoritesScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadGameScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModPackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadModScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadResourcePackScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadSavesScreen
import com.movtery.zalithlauncher.ui.screens.content.download.DownloadShadersScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchIdScreen
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem
import com.movtery.zalithlauncher.ui.screens.navigateOnce
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 导航至DownloadScreen
 */
fun ScreenBackStackViewModel.navigateToDownload(targetScreen: TitledNavKey? = null) {
    downloadScreen.clearWith(targetScreen ?: downloadGameScreen)
    mainScreen.removeAndNavigateTo(
        removes = clearBeforeNavKeys,
        screenKey = downloadScreen,
        useClassEquality = true
    )
}

/**
 * 跳转到资源类型对应的下载分类屏幕，并进入项目详情页
 */
private fun ScreenBackStackViewModel.swapToCategoryAssets(
    platform: Platform,
    classes: PlatformClasses,
    projectId: String,
    iconUrl: String?
) {
    val targetScreen = when (classes) {
        PlatformClasses.MOD -> downloadModScreen
        PlatformClasses.MOD_PACK -> downloadModPackScreen
        PlatformClasses.RESOURCE_PACK -> downloadResourcePackScreen
        PlatformClasses.SAVES -> downloadSavesScreen
        PlatformClasses.SHADERS -> downloadShadersScreen
    }
    navigateToDownload(
        targetScreen = targetScreen.apply {
            navigateTo(
                NormalNavKey.DownloadAssets(
                    platform = platform,
                    projectId = projectId,
                    classes = classes,
                    iconUrl = iconUrl
                )
            )
        }
    )
}

@Composable
fun DownloadScreen(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    //进入下载屏幕时确保收藏仓库完成初始化
    LaunchedEffect(Unit) {
        FavoriteProjectsRepository.ensureLoaded()
    }

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color(0xFF141414))
        ) {
            // Top Bar with Back Button and Hub Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color(0xFF1E1E20))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                com.movtery.zalithlauncher.ui.components.MinecraftButton(
                    onClick = {
                        backScreenViewModel.mainScreen.navigateTo(NormalNavKey.LauncherMain)
                    },
                    style = com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle.STONE,
                    text = "< BACK",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .width(76.dp)
                        .height(32.dp)
                )

                Text(
                    text = "DOWNLOAD HUB",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(76.dp))
            }

            // Top Horizontal Tabs Menu (clean, responsive, portrait-friendly)
            HorizontalTabMenu(
                backStack = key.backStack,
                backScreenViewModel = backScreenViewModel
            )

            // Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                NavigationUI(
                    key = key,
                    backScreenViewModel = backScreenViewModel,
                    eventViewModel = eventViewModel,
                    modpackImportViewModel = modpackImportViewModel,
                    submitError = submitError,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Always show Minecraft PC Bottom Navigation Bar
            com.movtery.zalithlauncher.ui.components.MinecraftPCBottomNavBar(
                selectedItem = 2,
                onItemSelected = { index ->
                    com.movtery.zalithlauncher.ui.screens.content.LauncherNavState.selectedBottomNav = index
                    backScreenViewModel.mainScreen.navigateTo(NormalNavKey.LauncherMain)
                }
            )
        }
    }
}

@Composable
private fun HorizontalTabMenu(
    backStack: NavBackStack<TitledNavKey>,
    backScreenViewModel: ScreenBackStackViewModel
) {
    val downloadsList = listOf(
        CategoryItem(backScreenViewModel.downloadModScreen, { CategoryIcon(R.drawable.ic_extension_outlined, R.string.download_category_mod) }, R.string.download_category_mod),
        CategoryItem(backScreenViewModel.downloadModPackScreen, { CategoryIcon(R.drawable.ic_package_2_outlined, R.string.download_category_modpack) }, R.string.download_category_modpack),
        CategoryItem(backScreenViewModel.downloadResourcePackScreen, { CategoryIcon(R.drawable.ic_format_paint_outlined, R.string.download_category_resource_pack) }, R.string.download_category_resource_pack),
        CategoryItem(backScreenViewModel.downloadShadersScreen, { CategoryIcon(R.drawable.ic_lightbulb, R.string.download_category_shaders) }, R.string.download_category_shaders),
        CategoryItem(backScreenViewModel.downloadGameScreen, { CategoryIcon(R.drawable.ic_sports_esports_outlined, R.string.download_category_game) }, R.string.download_category_game),
        CategoryItem(backScreenViewModel.downloadSavesScreen, { CategoryIcon(R.drawable.ic_public, R.string.download_category_saves) }, R.string.download_category_saves),
        CategoryItem(NormalNavKey.SearchId, { CategoryIcon(R.drawable.ic_card, R.string.download_category_by_id) }, R.string.download_category_by_id),
        CategoryItem(backScreenViewModel.downloadFavoritesScreen, { CategoryIcon(R.drawable.ic_favorite_outlined, R.string.download_category_favorites) }, R.string.download_category_favorites),
    )

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(0xFF18181A))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(downloadsList) { item ->
            val isSelected = backScreenViewModel.downloadScreen.currentKey == item.key
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val outerBorder = if (isSelected) Color(0xFF1B4E12) else Color(0xFF38383C)
            val topHighlight = if (isSelected) {
                if (isPressed) Color(0xFF194411) else Color(0xFF5AC636)
            } else {
                if (isPressed) Color(0xFF1E1E22) else Color(0xFF3E3E44)
            }
            val bottomShadow = if (isSelected) {
                if (isPressed) Color(0xFF5AC636) else Color(0xFF194411)
            } else {
                if (isPressed) Color(0xFF3E3E44) else Color(0xFF141416)
            }
            val bgGradient = if (isSelected) {
                if (isPressed) listOf(Color(0xFF286D19), Color(0xFF205814))
                else listOf(Color(0xFF388E23), Color(0xFF2C741B))
            } else {
                if (isPressed) listOf(Color(0xFF1A1A1E), Color(0xFF161618))
                else listOf(Color(0xFF2A2A2E), Color(0xFF202024))
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(0, if (isPressed) 2 else 0) }
                    .clip(RoundedCornerShape(4.dp))
                    .border(
                        BorderStroke(1.dp, outerBorder),
                        RoundedCornerShape(4.dp)
                    )
                    .background(Brush.verticalGradient(bgGradient))
                    .drawWithContent {
                        drawContent()
                        val highlightThick = 2.dp.toPx()
                        val shadowThick = 2.5.dp.toPx()
                        // Top highlight
                        drawRect(
                            color = topHighlight,
                            topLeft = Offset(0.5f, 0.5f),
                            size = Size(size.width - 1f, highlightThick)
                        )
                        // Bottom shadow
                        drawRect(
                            color = bottomShadow,
                            topLeft = Offset(0.5f, size.height - shadowThick),
                            size = Size(size.width - 1f, shadowThick)
                        )
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            backStack.navigateOnce(item.key)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(item.textRes),
                    color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                    fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 12.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = if (isSelected) Color(0xFF0F2B0A) else Color.Black,
                            offset = Offset(1.5f, 1.5f),
                            blurRadius = 0f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        backScreenViewModel.downloadScreen.currentKey = stackTopKey
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NestedNavKey.DownloadGame> { key ->
                    DownloadGameScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadGameScreenKey = backScreenViewModel.downloadGameScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadGameScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadModPack> { key ->
                    DownloadModPackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModPackScreenKey = backScreenViewModel.downloadModPackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModPackScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel,
                        importerViewModel = modpackImportViewModel
                    )
                }
                entry<NestedNavKey.DownloadMod> { key ->
                    DownloadModScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModScreenKey = backScreenViewModel.downloadModScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadResourcePack> { key ->
                    DownloadResourcePackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadResourcePackScreenKey = backScreenViewModel.downloadResourcePackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadResourcePackScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadSaves> { key ->
                    DownloadSavesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadSavesScreenKey = backScreenViewModel.downloadSavesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadSavesScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadShaders> { key ->
                    DownloadShadersScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadShadersScreenKey = backScreenViewModel.downloadShadersScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadShadersScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.SearchId> {
                    SearchIdScreen(
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                        },
                        openLink = { link ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(link))
                        }
                    )
                }
                entry<NestedNavKey.DownloadFavorites> { key ->
                    DownloadFavoritesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadFavoritesScreenKey = backScreenViewModel.downloadFavoritesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadFavoritesScreen.currentKey = newKey
                        },
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            backScreenViewModel.swapToCategoryAssets(platform, classes, projectId, iconUrl)
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}