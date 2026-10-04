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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.navigatePage
import com.movtery.zalithlauncher.game.download.assets.platform.nextPage
import com.movtery.zalithlauncher.game.download.assets.platform.previousPage
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.searchMcMods
import com.movtery.zalithlauncher.game.version.mod.InstalledMod
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.popularVersions
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchFilter
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SearchAssetsScreen"

/**
 * 资源搜索屏幕的 view model
 * @param initialPlatform 初始设定的平台
 * @param platformClasses 资源搜索的类型
 */
private class SearchScreenViewModel(
    initialPlatform: Platform,
    private val platformClasses: PlatformClasses
): ViewModel() {
    var searchResult by mutableStateOf<SearchAssetsState>(SearchAssetsState.Searching)
    val pages = mutableStateListOf<AssetsPage?>()

    var searchPlatform by mutableStateOf(initialPlatform)
    var searchFilter by mutableStateOf(PlatformSearchFilter())

    private val _searchedMcMods = MutableStateFlow<List<ModTranslations.McMod>>(emptyList())
    /** 搜索得到的所有 MCMOD 项目 */
    val searchedMcMods = _searchedMcMods.asStateFlow()
    private val _searchedVersions = MutableStateFlow<List<String>>(emptyList())
    /** 搜索得到的所有Minecraft版本 */
    val searchedVersions = _searchedVersions.asStateFlow()

    var currentSearchJob: Job? = null
    var currentSearchMCMODSJob: Job? = null
    var currentSearchVersionJob: Job? = null

    /**
     * 仅更新搜索名称
     */
    fun updateNameFilter(searchName: String) {
        searchFilter = searchFilter.copy(searchName = searchName)
        currentSearchMCMODSJob?.cancel()
        currentSearchMCMODSJob = viewModelScope.launch {
            val result = try {
                searchName.searchMcMods(classes = platformClasses) ?: emptyList()
            } catch (_: CancellationException) {
                emptyList()
            }.take(20) //仅展示20个搜索结果
            withContext(Dispatchers.Main) {
                _searchedMcMods.update { result }
            }
            currentSearchMCMODSJob = null
        }
    }

    /**
     * 仅更新版本名称
     */
    fun updateVersionFilter(version: String) {
        searchFilter = searchFilter.copy(gameVersion = version)
        refreshVerSuggestions(version)
    }

    private fun refreshVerSuggestions(
        version: String
    ) {
        currentSearchVersionJob?.cancel()
        currentSearchVersionJob = viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            val result: List<String> = when {
                version.isEmpty() -> popularVersions
                allVersions.isEmpty() -> popularVersions.filter { ver ->
                    ver.contains(version)
                }.take(20) //仅展示20个搜索结果
                else -> allVersions.filter {
                    it.version.id.contains(version) &&
                            //CurseForge只能使用正式版进行过滤
                            (searchPlatform != Platform.CURSEFORGE || it.type == MinecraftVersion.Type.Release)
                }.map { it.version.id }.take(20) //仅展示20个搜索结果
            }
            withContext(Dispatchers.Main) {
                _searchedVersions.update { result }
            }
            currentSearchVersionJob = null
        }
    }

    /**
     * 重置并重新搜索
     */
    fun resetSearch() {
        pages.clear()
        searchFilter = searchFilter.copy(index = 0) //重置索引到起始处
        search()
    }

    /**
     * 更新过滤器时，重置已有结果，重新触发搜索
     */
    fun researchWithFilter(filter: PlatformSearchFilter) {
        pages.clear()
        searchFilter = filter.copy(index = 0) //重置索引到起始处
        search()
    }

    private fun putResult(result: PlatformSearchResult) {
        result.getAssetsPage(platformClasses).also { page ->
            Logger.info(TAG, "Searched page info: {pageNumber: ${page.pageNumber}, pageIndex: ${page.pageIndex}, totalPage: ${page.totalPage}, isLastPage: ${page.isLastPage}}")

            val targetIndex = page.pageNumber - 1

            if (pages.size > targetIndex) {
                pages[targetIndex] = page //替换已有页
            } else {
                while (pages.size < targetIndex) {
                    pages += null
                }
                pages += page
            }

            searchResult = SearchAssetsState.Success(page)
        }
    }

    fun search() {
        currentSearchJob?.cancel() //取消上一个搜索

        currentSearchJob = viewModelScope.launch {
            searchResult = SearchAssetsState.Searching
            searchAssets(
                searchPlatform = searchPlatform,
                searchFilter = searchFilter,
                platformClasses = platformClasses,
                onSuccess = { result ->
                    putResult(result)
                },
                onError = {
                    searchResult = it
                }
            )
        }
    }

    init {
        //初始化后，执行一次搜索
        search()
        refreshVerSuggestions("")
        viewModelScope.launch {
            runCatching {
                MinecraftVersions.refreshVersions(force = false)
            }.onFailure {
                Logger.warning(TAG, "Failed to refresh Minecraft versions")
            }
        }
    }

    override fun onCleared() {
        currentSearchJob?.cancel()
        currentSearchMCMODSJob?.cancel()
    }
}

@Composable
private fun rememberSearchAssetsViewModel(
    navKey: TitledNavKey,
    initialPlatform: Platform,
    platformClasses: PlatformClasses
): SearchScreenViewModel {
    val screenKey = navKey.toString()
    return viewModel(
        key = "${screenKey}_search"
    ) {
        SearchScreenViewModel(initialPlatform, platformClasses)
    }
}

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param screenKey 屏幕的Key
 * @param currentKey 当前的Key
 * @param platformClasses 搜索资源的分类
 * @param initialPlatform 初始搜索平台
 * @param onPlatformChange 搜索平台变更
 * @param enablePlatform 是否允许更改平台
 * @param getCategories 根据平台获取可用的资源类别过滤器
 * @param enableModLoader 是否允许更改模组加载器
 * @param getModloaders 根据平台获取可用的模组加载器过滤器
 * @param mapCategories 通过平台获取类别本地化信息
 * @param swapToDownload 跳转到下载详情页
 * @param installedInfo 查询项目本地是否已安装，键为平台与平台项目ID
 * @param extraFilter 额外的过滤器UI
 */
@Composable
fun SearchAssetsScreen(
    mainScreenKey: TitledNavKey?,
    parentScreenKey: TitledNavKey,
    parentCurrentKey: TitledNavKey?,
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    platformClasses: PlatformClasses,
    initialPlatform: Platform,
    onPlatformChange: (Platform) -> Unit = {},
    enablePlatform: Boolean = true,
    getCategories: (Platform) -> List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    getModloaders: (Platform) -> List<PlatformDisplayLabel> = { emptyList() },
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null,
    extraFilter: (LazyListScope.() -> Unit)? = null
) {
    val viewModel: SearchScreenViewModel = rememberSearchAssetsViewModel(
        navKey = screenKey,
        initialPlatform = initialPlatform,
        platformClasses = platformClasses
    )

    //跟随平台自动变更的内容
    val categories = remember(viewModel.searchPlatform) {
        getCategories(viewModel.searchPlatform)
    }
    val modloaders = remember(viewModel.searchPlatform) {
        getModloaders(viewModel.searchPlatform)
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(screenKey, currentKey, false)
    ) { isVisible ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414))
        ) {
            val searchedVersions by viewModel.searchedVersions.collectAsStateWithLifecycle()

            // TOP SEARCH & FILTER BAR (PORTRAIT FRIENDLY IN MINECRAFT STYLE)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E20))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // 1. Search Box & Search Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF262628))
                            .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = viewModel.searchFilter.searchName,
                                onValueChange = { viewModel.updateNameFilter(it) },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55)),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { viewModel.resetSearch() }),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (viewModel.searchFilter.searchName.isEmpty()) {
                                        Text(
                                            text = "Search mods / packs...",
                                            color = Color(0xFF666666),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (viewModel.searchFilter.searchName.isNotEmpty()) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            viewModel.updateNameFilter("")
                                            viewModel.resetSearch()
                                        }
                                )
                            }
                        }
                    }

                    MinecraftButton(
                        onClick = { viewModel.resetSearch() },
                        style = MinecraftButtonStyle.GREEN,
                        text = "SEARCH",
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .width(76.dp)
                            .height(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Platform Selector: Modrinth vs CurseForge (if enabled)
                if (enablePlatform) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF3C8527))
                                .border(
                                    BorderStroke(1.dp, Color(0xFF55FF55)),
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable {
                                    viewModel.searchPlatform = Platform.CURSEFORGE
                                    viewModel.researchWithFilter(
                                        viewModel.searchFilter.copy(categories = emptyList(), modloader = null)
                                    )
                                    onPlatformChange(Platform.CURSEFORGE)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CURSEFORGE",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 3. Horizontal Filter Chips (Version, Loader, Sort)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Version Filter Chip
                    item {
                        var showVerDialog by remember { mutableStateOf(false) }
                        val curVer = viewModel.searchFilter.gameVersion.ifBlank { "All Versions" }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (viewModel.searchFilter.gameVersion.isNotBlank()) Color(0xFF2A3D24) else Color(0xFF262628))
                                .border(
                                    BorderStroke(1.dp, if (viewModel.searchFilter.gameVersion.isNotBlank()) Color(0xFF55FF55) else Color(0xFF383838)),
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable { showVerDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ver: $curVer",
                                color = if (viewModel.searchFilter.gameVersion.isNotBlank()) Color(0xFF55FF55) else Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp
                            )
                        }

                        if (showVerDialog) {
                            var verInput by remember { mutableStateOf(viewModel.searchFilter.gameVersion) }
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showVerDialog = false },
                                containerColor = Color(0xFF1E1E20),
                                title = {
                                    Text(
                                        text = "FILTER BY VERSION",
                                        color = Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 14.sp
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        androidx.compose.material3.OutlinedTextField(
                                            value = verInput,
                                            onValueChange = { verInput = it },
                                            placeholder = { Text("e.g. 1.21.4, 1.20.1", color = Color(0xFF666666)) },
                                            singleLine = true,
                                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF55FF55),
                                                unfocusedBorderColor = Color(0xFF383838),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("1.21.4", "1.20.1", "1.19.2", "1.16.5").forEach { v ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(Color(0xFF28282A))
                                                        .clickable {
                                                            verInput = v
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                                ) {
                                                    Text(v, color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    MinecraftButton(
                                        onClick = {
                                            viewModel.updateVersionFilter(verInput.trim())
                                            viewModel.resetSearch()
                                            showVerDialog = false
                                        },
                                        style = MinecraftButtonStyle.GREEN,
                                        text = "APPLY",
                                        fontSize = 11.sp,
                                        modifier = Modifier.width(76.dp).height(32.dp)
                                    )
                                },
                                dismissButton = {
                                    MinecraftButton(
                                        onClick = {
                                            viewModel.updateVersionFilter("")
                                            viewModel.resetSearch()
                                            showVerDialog = false
                                        },
                                        style = MinecraftButtonStyle.STONE,
                                        text = "CLEAR",
                                        fontSize = 11.sp,
                                        modifier = Modifier.width(76.dp).height(32.dp)
                                    )
                                }
                            )
                        }
                    }

                    // ModLoader Filter Chip (if enabled)
                    if (enableModLoader && modloaders.isNotEmpty()) {
                        item {
                            var showLoaderMenu by remember { mutableStateOf(false) }
                            val currentLoaderName = viewModel.searchFilter.modloader?.getDisplayName() ?: "All Loaders"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (viewModel.searchFilter.modloader != null) Color(0xFF2A3D24) else Color(0xFF262628))
                                    .border(
                                        BorderStroke(1.dp, if (viewModel.searchFilter.modloader != null) Color(0xFF55FF55) else Color(0xFF383838)),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .clickable { showLoaderMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Loader: $currentLoaderName",
                                    color = if (viewModel.searchFilter.modloader != null) Color(0xFF55FF55) else Color(0xFFCCCCCC),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )

                                androidx.compose.material3.DropdownMenu(
                                    expanded = showLoaderMenu,
                                    onDismissRequest = { showLoaderMenu = false },
                                    modifier = Modifier.background(Color(0xFF1E1E20))
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("All Loaders", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.researchWithFilter(viewModel.searchFilter.copy(modloader = null))
                                            showLoaderMenu = false
                                        }
                                    )
                                    modloaders.forEach { loader ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(loader.getDisplayName(), color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 11.sp) },
                                            onClick = {
                                                viewModel.researchWithFilter(viewModel.searchFilter.copy(modloader = loader))
                                                showLoaderMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sort Field Chip
                    item {
                        var showSortMenu by remember { mutableStateOf(false) }
                        val currentSortName = viewModel.searchFilter.sortField.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF262628))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(2.dp))
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sort: $currentSortName",
                                color = Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp
                            )

                            androidx.compose.material3.DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(Color(0xFF1E1E20))
                            ) {
                                PlatformSortField.entries.forEach { sf ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(sf.name, color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.researchWithFilter(viewModel.searchFilter.copy(sortField = sf))
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Result List taking full screen width in portrait!
            ResultListLayout(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                classes = platformClasses,
                searchState = viewModel.searchResult,
                onReload = {
                    viewModel.search()
                },
                swapToDownload = swapToDownload,
                installedInfo = installedInfo,
                onPreviousPage = { pageNumber ->
                    previousPage(
                        pageNumber = pageNumber,
                        pages = viewModel.pages,
                        index = viewModel.searchFilter.index,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { previousPage ->
                            viewModel.searchResult = SearchAssetsState.Success(previousPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search()
                        }
                    )
                },
                onNextPage = { pageNumber, isLastPage ->
                    nextPage(
                        pageNumber = pageNumber,
                        isLastPage = isLastPage,
                        pages = viewModel.pages,
                        index = viewModel.searchFilter.index,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { nextPage ->
                            viewModel.searchResult = SearchAssetsState.Success(nextPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search()
                        }
                    )
                },
                onNavigatePage = { pageNumber ->
                    navigatePage(
                        pageNumber = pageNumber,
                        pages = viewModel.pages,
                        limit = viewModel.searchFilter.limit,
                        onSuccess = { nextPage ->
                            viewModel.searchResult = SearchAssetsState.Success(nextPage)
                        },
                        onSearch = { newIndex ->
                            viewModel.searchFilter = viewModel.searchFilter.copy(index = newIndex)
                            viewModel.search()
                        }
                    )
                }
            )
        }
    }
}