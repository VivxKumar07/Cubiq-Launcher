/*
 * Cubiq Launcher
 * Minecraft Java Edition Landscape PC Layout Replica
 * Strictly matching user design specifications (Cubiq Launcher Landscape UI Collage)
 */

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.minecraft.InstallationDownloadManager
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftInstallationsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftModsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftNewsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftProfileScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftSkinsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftPatchNotesScreen
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * Landscape Mode Layout strictly matching Image 2 collage:
 * - Left vertical sidebar: Avatar, Username, Status, Play, Installations, Skins, Patch Notes, News, Versions, Mods, Profile
 * - Top-right: FILES & MULTIPLAYER buttons
 * - Main content: Play screen hero banner + floating bottom dock, or the respective sub-screen
 */
@Composable
fun LandscapeLauncherLayout(
    account: Account?,
    accountsList: List<Account>,
    currentVersion: Version?,
    allVersions: List<Version>,
    isRefreshingVersions: Boolean,
    selectedBottomNav: Int,
    activeTab: Int,
    onSelectTab: (bottomNav: Int, tab: Int) -> Unit,
    handlePlayClick: () -> Unit,
    handlePlayVersion: (Version) -> Unit,
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit
) {
    val context = LocalContext.current
    var showVersionMenu by remember { mutableStateOf(false) }

    // Map the active view:
    // 0: Play
    // 1: Installations
    // 2: Skins
    // 3: Patch Notes
    // 4: News
    // 5: Mods
    // 6: Profile
    val activeLandscapeIndex = when {
        selectedBottomNav == 0 -> 4 // News
        selectedBottomNav == 2 -> 5 // Mods
        selectedBottomNav == 3 -> 6 // Profile
        selectedBottomNav == 1 && activeTab == 0 -> 1 // Installations
        selectedBottomNav == 1 && activeTab == 1 -> 2 // Skins
        selectedBottomNav == 1 && activeTab == 2 -> 3 // Patch Notes
        else -> 0 // Play
    }

    val onOpenFiles: () -> Unit = {
        com.movtery.zalithlauncher.filemanager.FileManagerLauncher.launch(
            context = context,
            rootPath = PathManager.DIR_FILES_EXTERNAL.absolutePath,
            currentPath = null,
            logsDir = PathManager.DIR_LAUNCHER_LOGS.absolutePath
        )
    }

    val onOpenMultiplayer: () -> Unit = {
        backStackViewModel.mainScreen.removeAndNavigateTo(
            removes = backStackViewModel.clearBeforeNavKeys,
            screenKey = NormalNavKey.Multiplayer
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1012))
    ) {
        // 1. Left Vertical Navigation Sidebar
        Column(
            modifier = Modifier
                .width(210.dp)
                .fillMaxHeight()
                .background(Color(0xFF141517))
                .border(BorderStroke(1.dp, Color(0xFF232528)))
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            // User Header Profile Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSelectTab(3, -1) } // Go to profile
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val skinFile = account?.getSkinFile()
                if (account != null && skinFile != null && skinFile.exists()) {
                    PlayerFace(account = account, avatarSize = 34.dp)
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_profile),
                        contentDescription = "Player Face",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account?.username ?: "Cubiq",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = account?.let { "(${getAccountTypeName(it)})" } ?: "(Offline)",
                        color = Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items List with Scroll Slider
            val sidebarScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScrollWithBar(sidebarScrollState),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Group 1: Game & Customization
                LandscapeNavButton(
                    label = "Play",
                    iconRes = R.drawable.ic_mc_pc_versions,
                    isActive = activeLandscapeIndex == 0,
                    onClick = { onSelectTab(1, -1) }
                )
                LandscapeNavButton(
                    label = "Installations",
                    iconRes = R.drawable.block_crafting_table,
                    isActive = activeLandscapeIndex == 1,
                    onClick = { onSelectTab(1, 0) }
                )
                LandscapeNavButton(
                    label = "Skins",
                    iconRes = R.drawable.ic_mc_steve_head,
                    isActive = activeLandscapeIndex == 2,
                    onClick = { onSelectTab(1, 1) }
                )
                LandscapeNavButton(
                    label = "Patch Notes",
                    iconRes = R.drawable.ic_mc_book_and_quill,
                    isActive = activeLandscapeIndex == 3,
                    onClick = { onSelectTab(1, 2) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Group 2: Community & Launcher Settings
                LandscapeNavButton(
                    label = "News",
                    iconRes = R.drawable.ic_mc_pc_news,
                    isActive = activeLandscapeIndex == 4,
                    onClick = { onSelectTab(0, -1) }
                )
                LandscapeNavButton(
                    label = "Mods",
                    iconRes = R.drawable.ic_mc_pc_mods,
                    isActive = activeLandscapeIndex == 5,
                    onClick = { onSelectTab(2, -1) }
                )
                LandscapeNavButton(
                    label = "Profile",
                    iconRes = R.drawable.ic_mc_pc_profile,
                    isActive = activeLandscapeIndex == 6,
                    onClick = { onSelectTab(3, -1) }
                )
            }
        }

        // 2. Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (activeLandscapeIndex) {
                // 0: Play Screen (Full Hero banner + Centered Logo + Floating Bottom Dock)
                0 -> {
                    LandscapePlayScreen(
                        currentVersion = currentVersion,
                        allVersions = allVersions,
                        account = account,
                        isRefreshingVersions = isRefreshingVersions,
                        showVersionMenu = showVersionMenu,
                        onShowVersionMenuChange = { showVersionMenu = it },
                        onPlayClick = handlePlayClick,
                        onManageInstallations = { onSelectTab(1, 0) },
                        onOpenFiles = onOpenFiles,
                        onOpenMultiplayer = onOpenMultiplayer,
                        onSelectTab = onSelectTab
                    )
                }

                // 1: Installations
                1 -> {
                    MinecraftInstallationsScreen(
                        currentVersion = currentVersion,
                        backStackViewModel = backStackViewModel,
                        navigateToVersions = navigateToVersions,
                        onLaunchGame = onLaunchGame,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }

                // 2: Skins
                2 -> {
                    MinecraftSkinsScreen(
                        account = account,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }

                // 3: Patch Notes
                3 -> {
                    MinecraftPatchNotesScreen(
                        onOpenLink = onOpenLink,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }

                // 4: News
                4 -> {
                    MinecraftNewsScreen(
                        onOpenLink = onOpenLink,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }

                // 5: Mods
                5 -> {
                    MinecraftModsScreen(
                        currentVersion = currentVersion,
                        backStackViewModel = backStackViewModel,
                        navigateToVersions = navigateToVersions,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }

                // 6: Profile
                6 -> {
                    MinecraftProfileScreen(
                        account = account,
                        backStackViewModel = backStackViewModel,
                        onBackClick = { onSelectTab(1, -1) }
                    )
                }
            }

            if (activeLandscapeIndex != 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CompactBackgroundDownloadBar(
                        onExpand = {
                            InstallationDownloadManager.showDialog()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Landscape Play Screen matching Image 2 top-left:
 * - Full hero art banner
 * - Centered Minecraft Java Edition logo
 * - Top-right FILES & MULTIPLAYER buttons
 * - Floating bottom dock with Selected Version card & Green PLAY button
 */
/**
 * Landscape Play Screen:
 * - Centered high-resolution Wilderness Bound hero art banner
 * - Centered Minecraft Java Edition logo
 * - Top-right Screen Rotation, FILES & MULTIPLAYER buttons
 * - Floating bottom dock with Selected Version card & textured Green PLAY button
 */
@Composable
private fun LandscapePlayScreen(
    currentVersion: Version?,
    allVersions: List<Version>,
    account: Account?,
    isRefreshingVersions: Boolean,
    showVersionMenu: Boolean,
    onShowVersionMenuChange: (Boolean) -> Unit,
    onPlayClick: () -> Unit,
    onManageInstallations: () -> Unit,
    onOpenFiles: () -> Unit,
    onOpenMultiplayer: () -> Unit,
    onSelectTab: (bottomNav: Int, tab: Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // High-res portrait artwork centered in landscape
        Image(
            painter = painterResource(R.drawable.img_hero_wilderness),
            contentDescription = "Minecraft Java Edition",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        0.80f to Color(0x99000000),
                        1.0f to Color(0xEE0B0C0E)
                    )
                )
        )

        // Top-Right Actions: FILES, MULTIPLAYER, Rotation
        LandscapeTopRightActions(
            onOpenFiles = onOpenFiles,
            onOpenMultiplayer = onOpenMultiplayer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 12.dp)
        )

        // Centered Minecraft Java Edition Logo
        Image(
            painter = painterResource(R.drawable.img_mc_java_logo),
            contentDescription = "Minecraft Java Edition",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp)
                .height(76.dp)
                .widthIn(max = 340.dp),
            contentScale = ContentScale.Fit
        )

        // Floating Bottom Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Background Download Bar if downloading
                CompactBackgroundDownloadBar(
                    onExpand = {
                        onSelectTab(1, 0)
                        InstallationDownloadManager.showDialog()
                    }
                )

                // Dock Container
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Selected Version Card
                    val latestReleaseVersion by MinecraftOfficialContentManager.latestReleaseVersion.collectAsStateWithLifecycle()
                    val activeVer = currentVersion ?: allVersions.firstOrNull()
                    val mcVer = activeVer?.getVersionInfo()?.minecraftVersion
                    val verTitle = if (activeVer == null) "Latest Release" else "Selected Version"
                    val verDisplay = if (mcVer != null && activeVer != null) {
                        "${activeVer.getVersionName()} ($mcVer)"
                    } else {
                        activeVer?.getVersionName() ?: latestReleaseVersion
                    }
                    val verIconFile = activeVer?.let { com.movtery.zalithlauncher.game.version.installed.getVersionIconFile(it.getVersionPath()) }?.takeIf { it.exists() }

                    Box {
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF16181B))
                                .border(BorderStroke(1.5.dp, Color(0xFF32353A)), RoundedCornerShape(6.dp))
                            .clickable {
                                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                onShowVersionMenuChange(true)
                            }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (verIconFile != null) {
                            coil3.compose.AsyncImage(
                                model = verIconFile,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.block_crafting_table),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                text = verTitle,
                                color = Color(0xFFAAAAAA),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp
                            )
                            Text(
                                text = verDisplay,
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Version Dropdown
                    DropdownMenu(
                        expanded = showVersionMenu,
                        onDismissRequest = { onShowVersionMenuChange(false) },
                        modifier = Modifier
                            .width(280.dp)
                            .background(Color(0xFF1C1C1E))
                            .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                    ) {
                        if (allVersions.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Latest Release ($latestReleaseVersion)",
                                        color = Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    onShowVersionMenuChange(false)
                                    onManageInstallations()
                                }
                            )
                        } else {
                                allVersions.forEach { ver ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CommonVersionInfoLayout(
                                                    modifier = Modifier.weight(1f),
                                                    version = ver,
                                                    iconSize = 22.dp
                                                )
                                            }
                                        },
                                        onClick = {
                                            VersionsManager.saveVersion(ver)
                                            onShowVersionMenuChange(false)
                                        }
                                    )
                                }
                            }

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "+ Manage Installations",
                                        color = Color(0xFF55FF55),
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    onShowVersionMenuChange(false)
                                    onManageInstallations()
                                }
                            )
                        }
                    }

                    // Right: Textured Minecraft Green PLAY Button
                    MinecraftButton(
                        onClick = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            onPlayClick()
                        },
                        style = MinecraftButtonStyle.GREEN,
                        isLoading = isRefreshingVersions,
                        text = "PLAY",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .width(220.dp)
                            .height(48.dp)
                    )
                }
            }
        }
    }
}

/**
 * Top right action buttons: Rotation lock toggle, FILES & MULTIPLAYER
 */
@Composable
private fun LandscapeTopRightActions(
    onOpenFiles: () -> Unit,
    onOpenMultiplayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // FILES Button
        MinecraftButton(
            onClick = {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onOpenFiles()
            },
            style = MinecraftButtonStyle.STONE,
            text = "FILES",
            fontSize = 11.sp,
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 64.dp)
        )

        // MULTIPLAYER Button
        MinecraftButton(
            onClick = {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onOpenMultiplayer()
            },
            style = MinecraftButtonStyle.GREEN,
            text = "MULTIPLAYER",
            fontSize = 11.sp,
            modifier = Modifier
                .height(32.dp)
                .widthIn(min = 96.dp)
        )

        // Orientation Toggle Button (placed at far right edge)
        MinecraftButton(
            onClick = {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                val currentOrientation = activity?.resources?.configuration?.orientation
                if (currentOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    com.movtery.zalithlauncher.setting.AllSettings.launcherOrientationLock.save("landscape")
                } else {
                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    com.movtery.zalithlauncher.setting.AllSettings.launcherOrientationLock.save("portrait")
                }
            },
            style = MinecraftButtonStyle.STONE,
            modifier = Modifier
                .height(32.dp)
                .width(36.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_screen_rotation),
                contentDescription = "Rotate Screen",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Navigation button in the left sidebar with authentic pixel icons
 */
@Composable
private fun LandscapeNavButton(
    label: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgModifier = if (isActive) {
        Modifier
            .background(Color(0xFF1E3A19), RoundedCornerShape(6.dp))
            .border(BorderStroke(1.5.dp, Color(0xFF55FF55)), RoundedCornerShape(6.dp))
    } else {
        Modifier.background(Color.Transparent)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .then(bgModifier)
            .clickable {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xFFAAAAAA),
            fontFamily = MinecraftFontFamily,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

/**
 * Startup Unofficial Fork Notice Modal Dialog (Requirement #11)
 */
@Composable
fun ForkWarningDialog(
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(4.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Warning Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF3D3200))
                            .border(BorderStroke(1.dp, Color(0xFFFFCC00)), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_info),
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "UNOFFICIAL FORK NOTICE",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "GPLv3 Open Source Attribution",
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF333333))
                )

                // Body content
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Cubiq Launcher is an independent, unofficial fork of Zalith Launcher 2, licensed under the GNU General Public License v3.0 (GPLv3).",
                        color = Color(0xFFE2E2E2),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF161618))
                            .border(BorderStroke(1.dp, Color(0xFF2E2E32)), RoundedCornerShape(3.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "This launcher is NOT affiliated with, endorsed by, or supported by the original Zalith Launcher authors, Mojang Studios, or Microsoft Corporation.",
                            color = Color(0xFFFFAA00),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Text(
                        text = "All modifications and source code comply with GPLv3. For support or bug reports, please use the official Cubiq Launcher community.",
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Balanced Side-by-Side Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinecraftButton(
                        onClick = onDontShowAgain,
                        text = "DON'T SHOW AGAIN",
                        style = MinecraftButtonStyle.STONE,
                        fontSize = 10.5.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    )

                    MinecraftButton(
                        onClick = onDismiss,
                        text = "I UNDERSTOOD",
                        style = MinecraftButtonStyle.GREEN,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    )
                }
            }
        }
    }
}
