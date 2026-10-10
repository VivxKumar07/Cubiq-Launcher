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

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.isType
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.classes.Quadruple
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.toLocal
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "SelectGameVersion"

/** 版本列表加载状态 */
private sealed interface VersionState {
    /** 加载中 */
    data object Loading : VersionState
    /** 加载完成 */
    data class None(val versions: List<MinecraftVersion>) : VersionState
    /** 加载出现异常 */
    data class Failure(val message: AndroidStringText) : VersionState
}

/**
 * 版本过滤条件
 * @param release 是否保留正式版本
 * @param snapshot 是否保留快照版本
 * @param old 是否保留旧版本
 * @param id 搜索并过滤版本ID
 */
private data class VersionFilter(
    val release: Boolean = true,
    val snapshot: Boolean = false,
    val aprilFools: Boolean = false,
    val old: Boolean = false,
    val id: String = ""
)

private class VersionsViewModel: ViewModel() {
    var versionState by mutableStateOf<VersionState>(VersionState.Loading)
        private set

    //简易版本类型过滤器
    var versionFilter by mutableStateOf(VersionFilter())
        private set

    fun filterWith(filter: VersionFilter) {
        versionFilter = filter
        viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            versionState = VersionState.None(
                versions = allVersions.filterVersions(versionFilter)
            )
        }
    }

    fun refresh(forceReload: Boolean = false) {
        viewModelScope.launch {
            versionState = VersionState.Loading
            versionState = runCatching {
                MinecraftVersions.refreshVersions(forceReload)
                val allVersions = MinecraftVersions.allVersions.value
                VersionState.None(allVersions.filterVersions(versionFilter))
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to get version manifest!", e)
                val message: AndroidStringText = when(e) {
                    is HttpRequestTimeoutException -> androidText(R.string.error_timeout)
                    is UnknownHostException, is UnresolvedAddressException -> androidText(R.string.error_network_unreachable)
                    is ConnectException -> androidText(R.string.error_connection_failed)
                    is ResponseException -> e.toLocal()
                    else -> {
                        Logger.error(TAG, "An unknown exception was caught!", e)
                        androidText(e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error")
                    }
                }
                VersionState.Failure(message)
            }
        }
    }

    init {
        //初始化后，刷新版本列表
        refresh()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
fun SelectGameVersionScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey),
            Pair(NestedNavKey.DownloadGame::class.java, downloadScreenKey)
        ),
        Triple(NormalNavKey.DownloadGame.SelectGameVersion, downloadGameScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        ) {
            when (val state = viewModel.versionState) {
                is VersionState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        com.movtery.zalithlauncher.ui.components.MinecraftLoadingBlock("FETCHING VERSIONS...")
                    }
                }

                is VersionState.Failure -> {
                    Box(Modifier.fillMaxSize()) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = {
                                AndroidStringText(
                                    text = androidText(
                                        R.string.download_game_failed_to_get_versions,
                                        state.message
                                    )
                                )
                            },
                            onClick = {
                                viewModel.refresh(true)
                            }
                        )
                    }
                }

                is VersionState.None -> {
                    Column {
                        VersionHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            versionFilter = viewModel.versionFilter,
                            onVersionFilterChange = { viewModel.filterWith(it) },
                            itemContainerColor = cardColor(),
                            itemContentColor = onCardColor(),
                            onRefreshClick = {
                                viewModel.refresh(true)
                            }
                        )

                        VersionList(
                            modifier = Modifier.weight(1f),
                            versions = state.versions,
                            onVersionSelect = onVersionSelect,
                            openLink = { url ->
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简易过滤器，过滤特定类型的版本
 */
private fun List<MinecraftVersion>.filterVersions(
    versionFilter: VersionFilter
) = this.filter { version ->
    version.isType(
        release = versionFilter.release,
        snapshot = versionFilter.snapshot,
        aprilFools = versionFilter.aprilFools,
        old = versionFilter.old
    )
}.filter { version ->
    //Fix：单独过滤版本名称
    val versionId = versionFilter.id
    versionId.isEmptyOrBlank() || version.version.id.contains(versionId)
}

@Composable
private fun VersionHeader(
    modifier: Modifier = Modifier,
    versionFilter: VersionFilter,
    onVersionFilterChange: (VersionFilter) -> Unit,
    itemContainerColor: Color,
    itemContentColor: Color,
    onRefreshClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fadeEdge(
                    state = scrollState,
                    direction = EdgeDirection.Horizontal
                )
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //版本筛选条件
            VersionTypeItem(
                selected = versionFilter.release,
                onClick = {
                    onVersionFilterChange(versionFilter.copy(release = versionFilter.release.not()))
                },
                text = stringResource(R.string.download_game_type_release)
            )
            VersionTypeItem(
                selected = versionFilter.snapshot,
                onClick = {
                    onVersionFilterChange(versionFilter.copy(snapshot = versionFilter.snapshot.not()))
                },
                text = stringResource(R.string.download_game_type_snapshot)
            )
            VersionTypeItem(
                selected = versionFilter.aprilFools,
                onClick = {
                    onVersionFilterChange(versionFilter.copy(aprilFools = versionFilter.aprilFools.not()))
                },
                text = stringResource(R.string.download_game_type_april_fools)
            )
            VersionTypeItem(
                selected = versionFilter.old,
                onClick = {
                    onVersionFilterChange(versionFilter.copy(old = versionFilter.old.not()))
                },
                text = stringResource(R.string.download_game_type_old)
            )
        }

        // Cubiq Blocky Search Box & Refresh Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1E20))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (versionFilter.id.isNotEmpty()) Color(0xFF55FF55) else Color(0xFF383838)
                        ),
                        RoundedCornerShape(3.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = if (versionFilter.id.isNotEmpty()) Color(0xFF55FF55) else Color(0xFF888888),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (versionFilter.id.isEmpty()) {
                            Text(
                                text = stringResource(R.string.generic_search),
                                color = Color(0xFF777777),
                                fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                fontSize = 11.5.sp
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = versionFilter.id,
                            onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF55FF55)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onRefreshClick()
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1E20))
                    .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(R.string.generic_refresh),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF383838)
        )
    }
}

@Composable
private fun VersionTypeItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val outerBorder = if (selected) Color(0xFF1B4E12) else Color(0xFF38383C)
    val topHighlight = if (selected) {
        if (isPressed) Color(0xFF194411) else Color(0xFF5AC636)
    } else {
        if (isPressed) Color(0xFF1E1E22) else Color(0xFF3E3E44)
    }
    val bottomShadow = if (selected) {
        if (isPressed) Color(0xFF5AC636) else Color(0xFF194411)
    } else {
        if (isPressed) Color(0xFF3E3E44) else Color(0xFF141416)
    }
    val bgGradient = if (selected) {
        if (isPressed) listOf(Color(0xFF286D19), Color(0xFF205814))
        else listOf(Color(0xFF388E23), Color(0xFF2C741B))
    } else {
        if (isPressed) listOf(Color(0xFF1A1A1E), Color(0xFF161618))
        else listOf(Color(0xFF2A2A2E), Color(0xFF202024))
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, if (isPressed) 2 else 0) }
            .clip(RoundedCornerShape(3.dp))
            .border(
                BorderStroke(1.dp, outerBorder),
                RoundedCornerShape(3.dp)
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
                    onClick()
                }
            )
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFFCCCCCC),
            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            style = TextStyle(
                shadow = Shadow(
                    color = if (selected) Color(0xFF0F2B0A) else Color.Black,
                    offset = Offset(1.5f, 1.5f),
                    blurRadius = 0f
                )
            )
        )
    }
}

@Composable
private fun VersionList(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (url: String) -> Unit
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.nonInteractiveScrollbar(
            state = scrollState.scrollIndicatorState!!,
            orientation = Orientation.Vertical,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        state = scrollState,
    ) {
        items(versions) { version ->
            VersionItemLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                version = version,
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onVersionSelect(version.version.id)
                },
                onAccessWiki = { wikiUrl ->
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    openLink(wikiUrl)
                },
            )
        }
    }
}

@Composable
private fun VersionItemLayout(
    modifier: Modifier = Modifier,
    version: MinecraftVersion,
    onClick: () -> Unit = {},
    onAccessWiki: (String) -> Unit = {},
    shape: Shape = RoundedCornerShape(3.dp),
    influencedByBackground: Boolean = true,
    color: Color = Color(0xFF1A1A1C),
    contentColor: Color = Color.White,
    blur: Int = AllSettings.backgroundBlur.state,
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    val (icon, versionType, wikiUrl, summary) = getVersionComponents(version)

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier
                .clip(shape = shape)
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { versionIcon ->
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = versionIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = version.version.id,
                        style = MaterialTheme.typography.labelLarge
                    )

                    LittleTextLabel(
                        text = versionType
                    )
                }

                summary?.let { text ->
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = text,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = formatDate(
                        input = version.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            wikiUrl?.let { url ->
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onAccessWiki(url) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = "Wiki"
                    )
                }
            }
        }
    }
}

@Composable
private fun getVersionComponents(
    version: MinecraftVersion
): Quadruple<Painter?, String, String?, String?> {
    val vmVer = version.version
    val summary = version.summary?.let { stringResource(it) }
    val urlSuffix = version.urlSuffix ?: vmVer.id

    return when (version.type) {
        MinecraftVersion.Type.Release -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.download_game_type_release),
                stringResource(R.string.url_wiki_minecraft_game_release, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.Snapshot -> {
            Quadruple(
                painterResource(R.drawable.block_command_block),
                stringResource(R.string.download_game_type_snapshot),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.AprilFools -> {
            Quadruple(
                painterResource(R.drawable.block_diamond_block),
                stringResource(R.string.download_game_type_april_fools),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.OldBeta -> {
            Quadruple(
                painterResource(R.drawable.block_stone),
                stringResource(R.string.download_game_type_old_beta),
                null,
                summary
            )
        }
        MinecraftVersion.Type.OldAlpha -> {
            Quadruple(
                painterResource(R.drawable.block_dirt),
                stringResource(R.string.download_game_type_old_alpha),
                null,
                summary
            )
        }
        else -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.generic_unknown),
                null,
                version.summary?.let { stringResource(it) }
            )
        }
    }
}