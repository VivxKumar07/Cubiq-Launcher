/*
 * Cubiq Launcher
 * Minecraft Java Edition PC Mobile Replica Launcher UI
 */

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import java.io.File
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import android.net.Uri
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MicrosoftLoginDialog
import com.movtery.zalithlauncher.ui.toAndroidString
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MinecraftBlockCard
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.MinecraftPCBottomNavBar
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.game.versioninfo.MinecraftArtworkManager
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftInstallationsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftModsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftNewsScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftPatchNotesScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftProfileScreen
import com.movtery.zalithlauncher.ui.screens.content.minecraft.MinecraftSkinsScreen
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.ui.screens.content.minecraft.InstallationDownloadManager
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.ui.resolveAndroidString
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

object LauncherNavState {
    var selectedBottomNav by mutableIntStateOf(1)
    var activeTab by mutableIntStateOf(-1)
}

/**
 * Returns the appropriate official key art image resource based on the version name.
 * Supports all major versions and their respective subversions.
 */
fun getHeroArtForVersion(version: Version?): Int {
    val versionName = version?.getVersionName()?.lowercase() ?: "1.21"
    return when {
        versionName.contains("1.21") -> R.drawable.img_mc_hero_1_21
        versionName.contains("1.20") -> R.drawable.img_mc_hero_1_20
        versionName.contains("1.19") -> R.drawable.img_mc_hero_1_19
        versionName.contains("1.18") -> R.drawable.img_mc_hero_1_18
        versionName.contains("1.17") -> R.drawable.img_mc_hero_1_18
        versionName.contains("1.16") -> R.drawable.img_mc_hero_1_16
        versionName.contains("1.15") -> R.drawable.img_mc_hero_1_14
        versionName.contains("1.14") -> R.drawable.img_mc_hero_1_14
        versionName.contains("1.13") -> R.drawable.img_mc_hero_1_13
        else -> R.drawable.img_mc_hero_classic
    }
}

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    startGuideOnce: (GuideKeys.Keys) -> Unit,
) {
    // Guide intentionally omitted as requested by user to eliminate popup overlays

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { _ ->
        val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
        val accountsList by AccountsManager.accountsFlow.collectAsStateWithLifecycle()
        val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
        val allVersions by VersionsManager.versions.collectAsStateWithLifecycle()
        val isRefreshingVersions by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
        val runningTasks by com.movtery.zalithlauncher.coroutine.TaskSystem.tasksFlow.collectAsStateWithLifecycle()

        // Active navigation state from singleton so any screen can trigger tab change
        val activeTab = LauncherNavState.activeTab
        val selectedBottomNav = LauncherNavState.selectedBottomNav

        var showVersionMenu by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val toAccountManageScreen = {
            LauncherNavState.selectedBottomNav = 3 // Switch directly to custom Profile tab
            LauncherNavState.activeTab = -1
        }

        val handlePlayClick = {
            val isDownloading = runningTasks.any { it.id != com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK }
            if (isDownloading) {
                Toast.makeText(context, "Please wait for the game to finish downloading...", Toast.LENGTH_SHORT).show()
            } else if (account == null) {
                Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                LauncherNavState.selectedBottomNav = 3
                LauncherNavState.activeTab = -1
            } else {
                val hasDownloadedVersion = allVersions.any { ver ->
                    val clientJar = ver.getClientJar()
                    val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                    val inheritedJar = ver.getInheritedClientJar(mcVer)
                    (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                }
                if (!hasDownloadedVersion) {
                    Toast.makeText(context, "Please create an installation first!", Toast.LENGTH_SHORT).show()
                    LauncherNavState.activeTab = 0
                } else {
                    val targetVersion = currentVersion?.takeIf { ver ->
                        val clientJar = ver.getClientJar()
                        val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                        val inheritedJar = ver.getInheritedClientJar(mcVer)
                        (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                    } ?: allVersions.firstOrNull { ver ->
                        val clientJar = ver.getClientJar()
                        val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                        val inheritedJar = ver.getInheritedClientJar(mcVer)
                        (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                    }
                    if (targetVersion == null) {
                        Toast.makeText(context, "Please create an installation first!", Toast.LENGTH_SHORT).show()
                        LauncherNavState.activeTab = 0
                    } else {
                        onLaunchGame(targetVersion)
                    }
                }
            }
        }

        val handlePlayVersion: (Version) -> Unit = { ver ->
            val isDownloading = runningTasks.any { it.id != com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK }
            if (isDownloading) {
                Toast.makeText(context, "Please wait for the game to finish downloading...", Toast.LENGTH_SHORT).show()
            } else if (account == null) {
                Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                LauncherNavState.selectedBottomNav = 3
                LauncherNavState.activeTab = -1
            } else {
                val clientJar = ver.getClientJar()
                val verMc = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                val inheritedJar = ver.getInheritedClientJar(verMc)
                val isDownloaded = (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                if (!isDownloaded) {
                    Toast.makeText(context, "Please download this installation in Installations tab first!", Toast.LENGTH_SHORT).show()
                    LauncherNavState.activeTab = 0
                } else {
                    VersionsManager.saveVersion(ver)
                    onLaunchGame(ver)
                }
            }
        }

        // Handle Android back button for sub-tab navigation:
        // When in a sub-tab (Installations, Skins, News, Mods, Profile), go back to home view
        val isInSubView = activeTab != -1 || selectedBottomNav != 1
        BackHandler(enabled = isInSubView) {
            if (activeTab != -1) {
                LauncherNavState.activeTab = -1
            } else if (selectedBottomNav != 1) {
                LauncherNavState.selectedBottomNav = 1
            }
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        var showForkWarning by rememberSaveable {
            mutableStateOf(!com.movtery.zalithlauncher.setting.AllSettings.forkNoticeDismissed.getValue())
        }
        if (showForkWarning) {
            ForkWarningDialog(
                onDismiss = { showForkWarning = false },
                onDontShowAgain = {
                    com.movtery.zalithlauncher.setting.AllSettings.forkNoticeDismissed.save(true)
                    showForkWarning = false
                }
            )
        }

        val lastCrashExitCode = remember { com.movtery.zalithlauncher.setting.AllSettings.lastCrashCode.getValue() }
        val lastCrashLogPath = remember { com.movtery.zalithlauncher.setting.AllSettings.lastCrashLogPath.getValue() }
        val lastCrashIsSignal = remember { com.movtery.zalithlauncher.setting.AllSettings.lastCrashIsSignal.getValue() }
        var showCrashDialog by rememberSaveable {
            mutableStateOf(lastCrashExitCode != 0)
        }
        if (showCrashDialog) {
            PersistentCrashDialog(
                exitCode = lastCrashExitCode,
                isSignal = lastCrashIsSignal,
                logPath = lastCrashLogPath,
                onDismiss = {
                    com.movtery.zalithlauncher.setting.AllSettings.lastCrashCode.save(0)
                    showCrashDialog = false
                }
            )
        }

        if (isLandscape) {
            LandscapeLauncherLayout(
                account = account,
                accountsList = accountsList,
                currentVersion = currentVersion,
                allVersions = allVersions,
                isRefreshingVersions = isRefreshingVersions,
                selectedBottomNav = selectedBottomNav,
                activeTab = activeTab,
                onSelectTab = { bNav, tab ->
                    LauncherNavState.selectedBottomNav = bNav
                    LauncherNavState.activeTab = tab
                },
                handlePlayClick = handlePlayClick,
                handlePlayVersion = handlePlayVersion,
                backStackViewModel = backStackViewModel,
                navigateToVersions = navigateToVersions,
                onLaunchGame = onLaunchGame,
                onOpenLink = onOpenLink
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141414))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Account Header with Interactive Account Switcher Dropdown and Files / Multiplayer Quick Actions
                    TopAccountHeader(
                        account = account,
                    accountsList = accountsList,
                    onSelectAccount = { selectedAcc ->
                        AccountsManager.setCurrentAccount(selectedAcc)
                        Toast.makeText(context, "Switched to ${selectedAcc.username}", Toast.LENGTH_SHORT).show()
                    },
                    onManageAccounts = toAccountManageScreen,
                    onOpenMultiplayer = {
                        backStackViewModel.mainScreen.removeAndNavigateTo(
                            removes = backStackViewModel.clearBeforeNavKeys,
                            screenKey = NormalNavKey.Multiplayer
                        )
                    }
                )

                // Sub-navigation Tabs: Installations | Skins | Patch Notes
                // (Only visible on Versions/Home tab)
                if (selectedBottomNav == 1) {
                    SubNavigationTabs(
                        activeTab = activeTab,
                        onTabSelected = { tabIndex ->
                            LauncherNavState.activeTab = if (LauncherNavState.activeTab == tabIndex) -1 else tabIndex
                        }
                    )
                }

                // Main Content Switching
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        // Bottom Nav: News
                        selectedBottomNav == 0 -> {
                            MinecraftNewsScreen(
                                onOpenLink = onOpenLink,
                                onBackClick = { LauncherNavState.selectedBottomNav = 1 }
                            )
                        }
                        // Bottom Nav: Mods
                        selectedBottomNav == 2 -> {
                            MinecraftModsScreen(
                                currentVersion = currentVersion,
                                backStackViewModel = backStackViewModel,
                                navigateToVersions = navigateToVersions,
                                onBackClick = { LauncherNavState.selectedBottomNav = 1 }
                            )
                        }
                        // Bottom Nav: Profile
                        selectedBottomNav == 3 -> {
                            MinecraftProfileScreen(
                                account = account,
                                backStackViewModel = backStackViewModel,
                                onBackClick = { LauncherNavState.selectedBottomNav = 1 }
                            )
                        }
                        // Bottom Nav: Versions (Home)
                        else -> {
                            when (activeTab) {
                                0 -> {
                                    // Installations Screen (1:1 replica of Installations.png)
                                    MinecraftInstallationsScreen(
                                        currentVersion = currentVersion,
                                        backStackViewModel = backStackViewModel,
                                        navigateToVersions = navigateToVersions,
                                        onLaunchGame = onLaunchGame,
                                        onBackClick = { LauncherNavState.activeTab = -1 }
                                    )
                                }
                                1 -> {
                                    // Skins Screen
                                    MinecraftSkinsScreen(
                                        account = account,
                                        onBackClick = { LauncherNavState.activeTab = -1 }
                                    )
                                }
                                2 -> {
                                    // Patch Notes Screen
                                    MinecraftPatchNotesScreen(
                                        onOpenLink = onOpenLink,
                                        onBackClick = { LauncherNavState.activeTab = -1 }
                                    )
                                }
                                else -> {
                                    // Default Home / Play Screen
                                    val handlePlayClick = {
                                        val isDownloading = runningTasks.any { it.id != com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK }
                                        if (isDownloading) {
                                            Toast.makeText(context, "Please wait for the game to finish downloading...", Toast.LENGTH_SHORT).show()
                                        } else if (account == null) {
                                            Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                                            LauncherNavState.selectedBottomNav = 3
                                            LauncherNavState.activeTab = -1
                                        } else {
                                            val hasDownloadedVersion = allVersions.any { ver ->
                                                val clientJar = ver.getClientJar()
                                                val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                                                val inheritedJar = ver.getInheritedClientJar(mcVer)
                                                (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                                            }
                                            if (!hasDownloadedVersion) {
                                                Toast.makeText(context, "Please create an installation first!", Toast.LENGTH_SHORT).show()
                                                LauncherNavState.activeTab = 0
                                            } else {
                                                val targetVersion = currentVersion?.takeIf { ver ->
                                                    val clientJar = ver.getClientJar()
                                                    val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                                                    val inheritedJar = ver.getInheritedClientJar(mcVer)
                                                    (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                                                } ?: allVersions.firstOrNull { ver ->
                                                    val clientJar = ver.getClientJar()
                                                    val mcVer = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                                                    val inheritedJar = ver.getInheritedClientJar(mcVer)
                                                    (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                                                }
                                                if (targetVersion == null) {
                                                    Toast.makeText(context, "Please create an installation first!", Toast.LENGTH_SHORT).show()
                                                    LauncherNavState.activeTab = 0
                                                } else {
                                                    onLaunchGame(targetVersion)
                                                }
                                            }
                                        }
                                    }

                                    val handlePlayVersion: (Version) -> Unit = { ver ->
                                        val isDownloading = runningTasks.any { it.id != com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK }
                                        if (isDownloading) {
                                            Toast.makeText(context, "Please wait for the game to finish downloading...", Toast.LENGTH_SHORT).show()
                                        } else if (account == null) {
                                            Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                                            LauncherNavState.selectedBottomNav = 3
                                            LauncherNavState.activeTab = -1
                                        } else {
                                            val clientJar = ver.getClientJar()
                                            val verMc = ver.getVersionInfo()?.minecraftVersion ?: ver.getVersionName()
                                            val inheritedJar = ver.getInheritedClientJar(verMc)
                                            val isDownloaded = (clientJar.exists() && clientJar.length() > 0) || (inheritedJar != null && inheritedJar.exists() && inheritedJar.length() > 0)
                                            if (!isDownloaded) {
                                                Toast.makeText(context, "Please download this installation in Installations tab first!", Toast.LENGTH_SHORT).show()
                                                LauncherNavState.activeTab = 0
                                            } else {
                                                VersionsManager.saveVersion(ver)
                                                onLaunchGame(ver)
                                            }
                                        }
                                    }

                                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                        val isLandscape = maxWidth > maxHeight
                                        if (isLandscape) {
                                            // Responsive 2-Column Landscape Layout
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Left: Hero Minecraft Artwork Card (Full vertical height, half width)
                                                HeroMinecraftCard(
                                                    modifier = Modifier
                                                        .weight(0.48f)
                                                        .fillMaxHeight(),
                                                    version = currentVersion,
                                                    account = account,
                                                    onVersionClick = { showVersionMenu = true }
                                                )

                                                // Right: Scrollable actions & installations list
                                                Column(
                                                    modifier = Modifier
                                                        .weight(0.52f)
                                                        .fillMaxHeight()
                                                        .verticalScroll(rememberScrollState())
                                                ) {
                                                    Box(modifier = Modifier.fillMaxWidth()) {
                                                        DropdownMenu(
                                                            expanded = showVersionMenu,
                                                            onDismissRequest = { showVersionMenu = false },
                                                            modifier = Modifier
                                                                .width(280.dp)
                                                                .background(Color(0xFF1C1C1E))
                                                                .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                                        ) {
                                                            if (allVersions.isEmpty()) {
                                                                DropdownMenuItem(
                                                                    text = {
                                                                        Text(
                                                                            text = "Latest Release (${MinecraftOfficialContentManager.latestReleaseVersion.value})",
                                                                            color = Color.White,
                                                                            fontFamily = MinecraftFontFamily,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    },
                                                                    onClick = {
                                                                        allVersions.firstOrNull()?.let { VersionsManager.saveVersion(it) }
                                                                        showVersionMenu = false
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
                                                                                    iconSize = 24.dp
                                                                                )
                                                                            }
                                                                        },
                                                                        onClick = {
                                                                            VersionsManager.saveVersion(ver)
                                                                            showVersionMenu = false
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
                                                                    showVersionMenu = false
                                                                    LauncherNavState.activeTab = 0
                                                                }
                                                            )
                                                        }
                                                    }

                                                    // PLAY Action Row (1:1 Replica Textured Green PLAY Button)
                                                    PlayActionRow(
                                                        account = account,
                                                        isLaunching = isRefreshingVersions,
                                                        onPlayClick = handlePlayClick
                                                    )

                                                    // Installed Versions Cards
                                                    InstalledVersionsSection(
                                                        allVersions = allVersions,
                                                        currentVersion = currentVersion,
                                                        onSelectVersion = { ver ->
                                                            VersionsManager.saveVersion(ver)
                                                        },
                                                        onPlayVersion = handlePlayVersion,
                                                        onAddNewClick = {
                                                            LauncherNavState.activeTab = 0
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            // Portrait 1-Column Layout
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(bottom = 6.dp)
                                            ) {
                                                // Hero Card (Dynamic Key Art per Version)
                                                HeroMinecraftCard(
                                                    version = currentVersion,
                                                    account = account,
                                                    onVersionClick = { showVersionMenu = true }
                                                )

                                                // Version Dropdown Menu
                                                Box(modifier = Modifier.padding(start = 16.dp)) {
                                                    DropdownMenu(
                                                        expanded = showVersionMenu,
                                                        onDismissRequest = { showVersionMenu = false },
                                                        modifier = Modifier
                                                            .width(280.dp)
                                                            .background(Color(0xFF1C1C1E))
                                                            .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                                    ) {
                                                        if (allVersions.isEmpty()) {
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        text = "Latest Release (${MinecraftOfficialContentManager.latestReleaseVersion.value})",
                                                                        color = Color.White,
                                                                        fontFamily = MinecraftFontFamily,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                },
                                                                onClick = {
                                                                    allVersions.firstOrNull()?.let { VersionsManager.saveVersion(it) }
                                                                    showVersionMenu = false
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
                                                                                iconSize = 24.dp
                                                                            )
                                                                        }
                                                                    },
                                                                    onClick = {
                                                                        VersionsManager.saveVersion(ver)
                                                                        showVersionMenu = false
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
                                                                showVersionMenu = false
                                                                LauncherNavState.activeTab = 0
                                                            }
                                                        )
                                                    }
                                                }

                                                // PLAY Action Row (1:1 Replica Textured Green PLAY Button)
                                                PlayActionRow(
                                                    account = account,
                                                    isLaunching = isRefreshingVersions,
                                                    onPlayClick = handlePlayClick
                                                )

                                                // Installed Versions Cards (Replaced Security Advisory Card)
                                                InstalledVersionsSection(
                                                    allVersions = allVersions,
                                                    currentVersion = currentVersion,
                                                    onSelectVersion = { ver ->
                                                        VersionsManager.saveVersion(ver)
                                                    },
                                                    onPlayVersion = handlePlayVersion,
                                                    onAddNewClick = {
                                                        LauncherNavState.activeTab = 0
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Compact Bedrock Background Download Progress Bar Docked Above Bottom Nav Bar
                CompactBackgroundDownloadBar(
                    onExpand = {
                        LauncherNavState.selectedBottomNav = 1
                        LauncherNavState.activeTab = 0
                        InstallationDownloadManager.showDialog()
                    }
                )

                // 1:1 Replica Minecraft PC Mobile Launcher Bottom Navigation Bar
                // Options: News | Versions | Mods | Profile
                MinecraftPCBottomNavBar(
                    selectedItem = selectedBottomNav,
                    onItemSelected = { index ->
                        LauncherNavState.selectedBottomNav = index
                        LauncherNavState.activeTab = -1
                    },
                    barHeight = 56.dp
                )
            }
        }
        }

        InstallationDownloadForegroundDialog()
    }
}

@Composable
private fun MinecraftLaunchLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "mc_loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cube_rotation"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111113))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Authentic 3D Minecraft Java Edition Logo
            Image(
                painter = painterResource(R.drawable.img_mc_java_logo),
                contentDescription = "Minecraft Java Edition",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(68.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Animated Rotating Minecraft Block
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.img_minecraft),
                    contentDescription = "Loading Block",
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Minecraft Green Loading Bar
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF222224))
                    .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pulseAlpha)
                        .background(Color(0xFF55FF55))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LOADING CUBIQ...",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Top Account Header Bar:
 * Displays Player Head with cyan/blue border, Player Name with chevron arrow, and (Microsoft Account)
 */
/**
 * Top Account Header Bar:
 * Displays Player Head with cyan/blue border, Player Name with interactive dropdown menu for instant account switching,
 * and navigation to the full Profile / Manage Account screen.
 */
@Composable
private fun TopAccountHeader(
    account: Account?,
    accountsList: List<Account>,
    onSelectAccount: (Account) -> Unit,
    onManageAccounts: () -> Unit,
    onOpenMultiplayer: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var isLoggingInMicrosoft by remember { mutableStateOf(false) }
    var msDeviceCodeText by remember { mutableStateOf<String?>(null) }
    var msVerificationUrl by remember { mutableStateOf<String?>(null) }
    var showAddOfflineDialog by remember { mutableStateOf(false) }
    var showAddElybyDialog by remember { mutableStateOf(false) }
    var offlineUsername by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161616))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Player Head and Name clickable for Account Switcher
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        expanded = true
                    }
                    .padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Head Avatar with vibrant blue rounded square border
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(BorderStroke(2.dp, Color(0xFF1E88E5)), RoundedCornerShape(6.dp))
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                ) {
                    val skinFile = account?.getSkinFile()
                    if (account != null && skinFile != null && skinFile.exists()) {
                        PlayerFace(
                            account = account,
                            avatarSize = 34.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_mc_pc_profile),
                            contentDescription = "Steve Face",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = account?.username ?: "Player",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = account?.let { "(${getAccountTypeName(it)})" } ?: "(No Active Account)",
                        color = Color(0xFF9E9E9E),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Right: Orientation, Files and Multiplayer Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Orientation Toggle Button (Icon on the left of FILES)
                val activity = context as? android.app.Activity
                MinecraftButton(
                    onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        val currentOrientation = activity?.resources?.configuration?.orientation
                        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            com.movtery.zalithlauncher.setting.AllSettings.launcherOrientationLock.save("landscape")
                        } else {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            com.movtery.zalithlauncher.setting.AllSettings.launcherOrientationLock.save("portrait")
                        }
                    },
                    style = MinecraftButtonStyle.STONE,
                    modifier = Modifier
                        .height(36.dp)
                        .width(42.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_screen_rotation),
                        contentDescription = "Rotate Screen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Files Quick Action (3D Beveled Stone Button)
                MinecraftButton(
                    onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        com.movtery.zalithlauncher.filemanager.FileManagerLauncher.launch(
                            context = context,
                            rootPath = com.movtery.zalithlauncher.path.PathManager.DIR_FILES_EXTERNAL.absolutePath,
                            currentPath = null,
                            logsDir = com.movtery.zalithlauncher.path.PathManager.DIR_LAUNCHER_LOGS.absolutePath
                        )
                    },
                    style = MinecraftButtonStyle.STONE,
                    text = "FILES",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .height(36.dp)
                        .widthIn(min = 68.dp)
                )

                // Multiplayer Quick Action (3D Beveled Green Button)
                MinecraftButton(
                    onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        onOpenMultiplayer()
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = "MULTIPLAYER",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .height(36.dp)
                        .widthIn(min = 106.dp)
                )
            }
        }

        // Account Dropdown Selector
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(290.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person_outlined),
                        contentDescription = null,
                        tint = Color(0xFF55FF55),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ACCOUNTS",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF28282A))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${accountsList.size} Saved",
                        color = Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF2D2D30))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Accounts List
            if (accountsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF161618))
                        .border(BorderStroke(1.dp, Color(0xFF2A2A2D)), RoundedCornerShape(3.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved accounts yet.\nAdd an account below to play!",
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    accountsList.forEach { acc ->
                        val isCurrent = account?.uniqueUUID == acc.uniqueUUID
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) Color(0xFF1E331E) else Color(0xFF242426))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isCurrent) Color(0xFF55FF55) else Color(0xFF333336)
                                    ),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable {
                                    expanded = false
                                    onSelectAccount(acc)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlayerFace(account = acc, avatarSize = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.username,
                                    color = if (isCurrent) Color(0xFF55FF55) else Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = getAccountTypeName(acc),
                                    color = if (isCurrent) Color(0xFF99EE99) else Color(0xFF888888),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 9.5.sp
                                )
                            }
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF2E6930))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color(0xFF55FF55),
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF2D2D30))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action 1: Add Microsoft Account
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF162232))
                    .border(BorderStroke(1.dp, Color(0xFF2563EB)), RoundedCornerShape(3.dp))
                .clickable {
                    expanded = false
                    if (!isLoggingInMicrosoft) {
                        isLoggingInMicrosoft = true
                        microsoftLogin(
                            context = context,
                            toWeb = { url ->
                                msVerificationUrl = url
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            backToMain = {
                                isLoggingInMicrosoft = false
                                msDeviceCodeText = null
                            },
                            checkIfInWebScreen = { true },
                            updateOperation = {},
                            showToast = { text, _ ->
                                Toast.makeText(context, text.toAndroidString(context), Toast.LENGTH_SHORT).show()
                            },
                            submitError = { err ->
                                isLoggingInMicrosoft = false
                                msDeviceCodeText = null
                                Toast.makeText(context, "Microsoft Login Failed: ${err.message.toAndroidString(context)}", Toast.LENGTH_LONG).show()
                            },
                            onSuccess = {
                                isLoggingInMicrosoft = false
                                msDeviceCodeText = null
                                Toast.makeText(context, "Successfully logged into Microsoft Account!", Toast.LENGTH_SHORT).show()
                            },
                            onDeviceCode = { deviceCode ->
                                msDeviceCodeText = deviceCode.userCode
                                msVerificationUrl = deviceCode.verificationUrl
                            }
                        )
                    }
                }
                .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E3A8A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Add Microsoft Account",
                        color = Color(0xFF93C5FD),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Official Mojang / Xbox sign in",
                        color = Color(0xFF64748B),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action 2: Add Ely.by Account
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF0C2438))
                    .border(BorderStroke(1.dp, Color(0xFF0284C7)), RoundedCornerShape(3.dp))
                    .clickable {
                        expanded = false
                        showAddElybyDialog = true
                    }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0369A1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Add Ely.by Account",
                        color = Color(0xFF38BDF8),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Custom skins, capes & authlib login",
                        color = Color(0xFF64748B),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action 3: Add Offline Account
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF182818))
                    .border(BorderStroke(1.dp, Color(0xFF16A34A)), RoundedCornerShape(3.dp))
                    .clickable {
                        expanded = false
                        showAddOfflineDialog = true
                    }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF14532D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Add Offline Account",
                        color = Color(0xFF4ADE80),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Play locally without signing in",
                        color = Color(0xFF64748B),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action 4: Manage Accounts & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF222224))
                    .border(BorderStroke(1.dp, Color(0xFF38383B)), RoundedCornerShape(3.dp))
                    .clickable {
                        expanded = false
                        onManageAccounts()
                    }
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_filled),
                    contentDescription = null,
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage Accounts & Settings",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
            }
        }
    }

    if (showAddElybyDialog) {
        com.movtery.zalithlauncher.ui.screens.content.minecraft.ElybyLoginDialog(
            onDismiss = { showAddElybyDialog = false },
            onSuccess = { showAddElybyDialog = false }
        )
    }

    if (isLoggingInMicrosoft) {
        MicrosoftLoginDialog(
            msDeviceCodeText = msDeviceCodeText,
            msVerificationUrl = msVerificationUrl,
            onDismiss = {
                TaskSystem.cancelTask(MICROSOFT_LOGGING_TASK)
                isLoggingInMicrosoft = false
                msDeviceCodeText = null
            }
        )
    }

    if (showAddOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showAddOfflineDialog = false },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    text = "ADD OFFLINE ACCOUNT",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter username for local offline play:",
                        color = Color(0xFFCCCCCC),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = offlineUsername,
                        onValueChange = { offlineUsername = it },
                        placeholder = { Text("Username", color = Color(0xFF666666), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF242426),
                            unfocusedContainerColor = Color(0xFF242426),
                            focusedBorderColor = Color(0xFF55FF55),
                            unfocusedBorderColor = Color(0xFF383838),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                MinecraftButton(
                    onClick = {
                        val trimmed = offlineUsername.trim()
                        if (trimmed.isNotBlank()) {
                            coroutineScope.launch {
                                val newAcc = Account(
                                    username = trimmed,
                                    accountType = com.movtery.zalithlauncher.game.account.AccountType.LOCAL.tag
                                )
                                withContext(Dispatchers.IO) {
                                    AccountsManager.suspendSaveAccount(newAcc)
                                }
                                AccountsManager.setCurrentAccount(newAcc)
                                Toast.makeText(context, "Account '$trimmed' added!", Toast.LENGTH_SHORT).show()
                                showAddOfflineDialog = false
                                offlineUsername = ""
                            }
                        } else {
                            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = "CREATE",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .width(84.dp)
                        .height(34.dp)
                )
            },
            dismissButton = {
                MinecraftButton(
                    onClick = { showAddOfflineDialog = false },
                    style = MinecraftButtonStyle.STONE,
                    text = "CANCEL",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .width(84.dp)
                        .height(34.dp)
                )
            }
        )
    }
}

/**
 * Sub-Navigation Tabs: Play | Installations | Skins | Patch Notes
 */
@Composable
private fun SubNavigationTabs(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // -1: Play, 0: Installations, 1: Skins, 2: Patch Notes
    val tabs = listOf(
        Pair("Play", -1),
        Pair("Installations", 0),
        Pair("Skins", 1),
        Pair("Patch Notes", 2)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141416))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (title, tabIndex) ->
            val isSelected = activeTab == tabIndex
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        onTabSelected(tabIndex)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFF38D122))
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.5.dp))
                }
            }
        }
    }
}

/**
 * Hero Card with authentic Wilderness Bound hero banner centered-aligned,
 * centered 3D embossed MINECRAFT JAVA EDITION typography logo, and bottom version selector pill.
 */
@Composable
private fun HeroMinecraftCard(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp)
        .height(290.dp),
    version: Version?,
    account: Account?,
    onVersionClick: () -> Unit
) {
    val latestReleaseVersion by MinecraftOfficialContentManager.latestReleaseVersion.collectAsStateWithLifecycle()

    val verIconFile = remember(version) { version?.getVersionIconFile() }
    val verHasCustomIcon = verIconFile != null && verIconFile.exists()
    val verFallbackIcon = remember(version) {
        version?.let { com.movtery.zalithlauncher.ui.screens.content.elements.getLoaderIconRes(it.getVersionInfo()?.loaderInfo?.loader) } ?: R.drawable.img_minecraft
    }

    MinecraftPixelBorderContainer(
        modifier = modifier,
        borderColor = Color(0xFF3C8527)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork (Single centered Wilderness Bound banner from official folder)
            Image(
                painter = painterResource(R.drawable.img_hero_wilderness),
                contentDescription = "Minecraft Wilderness Bound Hero Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            // Minecraft Java Edition Typography Logo positioned in upper area
            Image(
                painter = painterResource(R.drawable.img_mc_java_logo),
                contentDescription = "Minecraft Java Edition",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .fillMaxWidth(0.82f)
                    .height(68.dp),
                contentScale = ContentScale.Fit
            )

            // Scrim gradient for contrast at the bottom where version selector sits
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.55f to Color.Transparent,
                            0.78f to Color(0x77000000),
                            1.0f to Color(0xDD0D0D0D)
                        )
                    )
            )

            // Bottom controls inside Hero Card
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Version Selector Dropdown Pill (Stone Block Beveled Style with Version's Installation Icon)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xEE1E1E1E))
                        .border(BorderStroke(1.5.dp, Color(0xFF444444)), RoundedCornerShape(3.dp))
                        .clickable(onClick = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            onVersionClick()
                        })
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (verHasCustomIcon) {
                        AsyncImage(
                            model = verIconFile,
                            contentDescription = "Selected Version Icon",
                            modifier = Modifier.size(26.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(verFallbackIcon),
                            contentDescription = "Selected Version Icon",
                            modifier = Modifier.size(26.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (version == null) "Latest Release" else "Selected Version",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val mcVer = version?.getVersionInfo()?.minecraftVersion
                        val verDisplay = if (mcVer != null && version != null) {
                            "${version.getVersionName()} ($mcVer)"
                        } else {
                            version?.getVersionName() ?: latestReleaseVersion
                        }
                        Text(
                            text = verDisplay,
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Play Action Row directly below Hero Card:
 * Features the 1:1 replica textured green PLAY button
 */
@Composable
private fun PlayActionRow(
    account: Account?,
    isLaunching: Boolean,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        MinecraftButton(
            onClick = {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onPlayClick()
            },
            style = MinecraftButtonStyle.GREEN,
            isLoading = isLaunching,
            text = "PLAY",
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        )
    }
}

/**
 * Installed Versions Section:
 * Displays all installations as distinct Minecraft-themed cards with their respective selected installation icons.
 */
@Composable
private fun InstalledVersionsSection(
    allVersions: List<Version>,
    currentVersion: Version?,
    onSelectVersion: (Version) -> Unit,
    onPlayVersion: (Version) -> Unit,
    onAddNewClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "YOUR INSTALLATIONS",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "+ ADD NEW",
                color = Color(0xFF55FF55),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable(onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        onAddNewClick()
                    })
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (allVersions.isEmpty()) {
            MinecraftPixelBorderContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        onAddNewClick()
                    }),
                borderColor = Color(0xFF383838)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1C))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No custom installations yet",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MinecraftButton(
                        onClick = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            onAddNewClick()
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = "+ CREATE INSTALLATION",
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(34.dp)
                    )
                }
            }
        } else {
            allVersions.forEach { ver ->
                val isSelected = ver.getVersionName() == currentVersion?.getVersionName()
                val iconFile = remember(ver) { ver.getVersionIconFile() }
                val hasCustomIcon = iconFile.exists()
                val fallbackIcon = remember(ver) {
                    com.movtery.zalithlauncher.ui.screens.content.elements.getLoaderIconRes(ver.getVersionInfo()?.loaderInfo?.loader)
                }

                MinecraftPixelBorderContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            onSelectVersion(ver)
                        },
                    borderColor = if (isSelected) Color(0xFF55FF55) else Color(0xFF333333)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF1E281C) else Color(0xFF1A1A1C))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Respective Installation Block/Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF222224))
                                .border(BorderStroke(1.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)), RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasCustomIcon) {
                                AsyncImage(
                                    model = iconFile,
                                    contentDescription = ver.getVersionName(),
                                    modifier = Modifier.size(46.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Image(
                                    painter = painterResource(fallbackIcon),
                                    contentDescription = ver.getVersionName(),
                                    modifier = Modifier.size(46.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ver.getVersionName(),
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val mcVer = ver.getVersionInfo()?.minecraftVersion
                            val loaderName = ver.getVersionInfo()?.loaderInfo?.loader?.displayName ?: "Vanilla"
                            val hasRealMc = mcVer != null && (mcVer != ver.getVersionName() || mcVer.contains("."))
                            val subtitleText = if (hasRealMc) {
                                "$loaderName • $mcVer"
                            } else {
                                loaderName
                            }
                            Text(
                                text = subtitleText,
                                color = Color(0xFFAAAAAA),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.5.sp
                            )
                            if (isSelected) {
                                Text(
                                    text = "● ACTIVE",
                                    color = Color(0xFF55FF55),
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        MinecraftButton(
                            onClick = {
                                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                onPlayVersion(ver)
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "PLAY",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .width(64.dp)
                                .height(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pixelated Minecraft Container Border:
 * Draws outer border and authentic retro L-bracket stepped notches at each corner
 */
@Composable
private fun MinecraftPixelBorderContainer(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF3C8527),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(3.dp))
            .drawWithContent {
                drawContent()
                val bracketLen = 10.dp.toPx()
                val step = 3.dp.toPx()
                val strokeW = 2.dp.toPx()

                // Top-Left corner bracket
                drawLine(borderColor, Offset(0f, step), Offset(step, step), strokeW)
                drawLine(borderColor, Offset(step, 0f), Offset(step, step), strokeW)
                drawLine(borderColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
                drawLine(borderColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

                // Top-Right corner bracket
                val w = size.width
                drawLine(borderColor, Offset(w - step, step), Offset(w, step), strokeW)
                drawLine(borderColor, Offset(w - step, 0f), Offset(w - step, step), strokeW)
                drawLine(borderColor, Offset(w - bracketLen, 0f), Offset(w, 0f), strokeW)
                drawLine(borderColor, Offset(w, 0f), Offset(w, bracketLen), strokeW)

                // Bottom-Left corner bracket
                val h = size.height
                drawLine(borderColor, Offset(0f, h - step), Offset(step, h - step), strokeW)
                drawLine(borderColor, Offset(step, h - step), Offset(step, h), strokeW)
                drawLine(borderColor, Offset(0f, h), Offset(bracketLen, h), strokeW)
                drawLine(borderColor, Offset(0f, h - bracketLen), Offset(0f, h), strokeW)

                // Bottom-Right corner bracket
                drawLine(borderColor, Offset(w - step, h - step), Offset(w, h - step), strokeW)
                drawLine(borderColor, Offset(w - step, h - step), Offset(w - step, h), strokeW)
                drawLine(borderColor, Offset(w - bracketLen, h), Offset(w, h), strokeW)
                drawLine(borderColor, Offset(w, h - bracketLen), Offset(w, h), strokeW)
            }
    ) {
        content()
    }
}

/**
 * Patch Notes Modal Dialog
 */
@Composable
private fun PatchNotesDialog(
    onDismiss: () -> Unit,
    onOpenChangelog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.img_minecraft),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Minecraft 1.21.4 Patch Notes",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Welcome to the latest release of Minecraft Java Edition!\n\n" +
                            "Tricky Trials Features:\n" +
                            "• Trial Chambers: Explore sprawling underground structures filled with perilous challenges, trial spawners, and vaults.\n" +
                            "• The Breeze: A hostile mob that leaps circles around opponents and fires wind charges.\n" +
                            "• The Crafter: A new Redstone block that enables automated crafting of items.\n" +
                            "• The Mace: A powerful new weapon delivering devastating smash attacks from high falls.\n" +
                            "• Bug fixes, performance improvements, and security enhancements for Java Edition.",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = onOpenChangelog,
                style = MinecraftButtonStyle.GREEN,
                text = "CHANGELOG",
                fontSize = 12.sp,
                modifier = Modifier
                    .width(130.dp)
                    .height(38.dp)
            )
        },
        dismissButton = {
            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.STONE,
                text = "CLOSE",
                fontSize = 12.sp,
                modifier = Modifier
                    .width(85.dp)
                    .height(38.dp)
            )
        }
    )
}

/**
 * News Security Advisory Modal Dialog
 */
@Composable
private fun NewsSecurityDialog(
    onDismiss: () -> Unit,
    onOpenArticle: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "Security Vulnerability Advisory",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(R.drawable.img_mc_news_cave),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Important Message regarding building and running Java Edition:\n\n" +
                            "Mojang Studios and the Minecraft security team have released critical security advisories regarding network packet processing and external asset loaders.\n\n" +
                            "Cubiq includes patched native libraries and secure JVM arguments to keep your game, world data, and multiplayer sessions safe.",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = onOpenArticle,
                style = MinecraftButtonStyle.GREEN,
                text = "READ ADVISORY",
                fontSize = 12.sp,
                modifier = Modifier
                    .width(140.dp)
                    .height(38.dp)
            )
        },
        dismissButton = {
            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.STONE,
                text = "CLOSE",
                fontSize = 12.sp,
                modifier = Modifier
                    .width(85.dp)
                    .height(38.dp)
            )
        }
    )
}

@Composable
fun CompactBackgroundDownloadBar(
    onExpand: () -> Unit
) {
    val runningTasks by com.movtery.zalithlauncher.coroutine.TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    val activeInstaller by InstallationDownloadManager.activeInstaller.collectAsStateWithLifecycle()
    val isPreparing by InstallationDownloadManager.isPreparing.collectAsStateWithLifecycle()
    val showForegroundDialog by InstallationDownloadManager.showForegroundDialog.collectAsStateWithLifecycle()
    val activeInstallName by InstallationDownloadManager.activeInstallationName.collectAsStateWithLifecycle()

    val relevantTasks = remember(runningTasks) {
        runningTasks.filter { it.id != com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK }
    }

    val hasActiveTasks = relevantTasks.isNotEmpty() || ((activeInstaller != null || isPreparing) && !showForegroundDialog)
    if (!hasActiveTasks) return

    val currentTask = if (activeInstaller != null) {
        val tasks by activeInstaller!!.tasksFlow.collectAsStateWithLifecycle()
        tasks.find { it.task.stage.value == com.movtery.zalithlauncher.coroutine.TaskStage.RUNNING }?.task
            ?: tasks.firstOrNull()?.task
    } else {
        relevantTasks.firstOrNull()
    }

    val progress = currentTask?.progress?.collectAsStateWithLifecycle()?.value ?: if (isPreparing) 0.15f else 0f
    val message = currentTask?.message?.collectAsStateWithLifecycle()?.value
    val speed = currentTask?.rateBytesPerSec?.collectAsStateWithLifecycle()?.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
            .clickable { onExpand() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "compact_dl")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "compact_alpha"
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = null,
                tint = Color(0xFF55FF55).copy(alpha = alpha),
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeInstallName != null) {
                        Text(
                            text = if (isPreparing) "PREPARING ${activeInstallName!!.uppercase()}..." else "DOWNLOADING ${activeInstallName!!.uppercase()}",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    } else if (message != null) {
                        Text(
                            text = resolveAndroidString(message!!).text,
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = if (isPreparing) "PREPARING GAME INSTALLATION..." else "DOWNLOADING GAME FILES...",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    val spd = speed ?: 0L
                    if (spd > 0L) {
                        Text(
                            text = "${formatFileSize(spd)}/s",
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                // Progress Bar
                val progressFloat = progress.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2A2A2D))
                ) {
                    if (progressFloat > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFloat)
                                .fillMaxHeight()
                                .background(Color(0xFF55FF55))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .fillMaxHeight()
                                .background(Color(0xFF3C8527).copy(alpha = alpha))
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2E2E32))
                    .border(BorderStroke(1.dp, Color(0xFF444448)), RoundedCornerShape(2.dp))
                    .clickable { onExpand() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIEW",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PersistentCrashDialog(
    exitCode: Int,
    isSignal: Boolean,
    logPath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val logFile = remember(logPath) { File(logPath) }
    val logLines = remember(logPath) {
        runCatching {
            if (logFile.exists()) {
                val lines = logFile.readLines()
                lines.takeLast(25).joinToString("\n")
            } else {
                "No crash log file found at: $logPath"
            }
        }.getOrDefault("Failed to read log file.")
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(1.5.dp, Color(0xFFEF4444)), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF451A1A))
                            .border(BorderStroke(1.dp, Color(0xFFEF4444)), RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            text = "GAME CRASH DETECTED",
                            color = Color(0xFFEF4444),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isSignal) "Terminated by system signal (Code $exitCode)" else "JVM exited abnormally with code $exitCode",
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

                Text(
                    text = "The previous Minecraft session crashed. You can review the log output below or share it for troubleshooting:",
                    color = Color(0xFFDDDDDD),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Log tail preview box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF141416))
                        .border(BorderStroke(1.dp, Color(0xFF2E2E32)), RoundedCornerShape(3.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(
                        text = logLines,
                        color = Color(0xFFCCCCCC),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MinecraftButton(
                        onClick = {
                            if (logFile.exists()) {
                                com.movtery.zalithlauncher.utils.file.shareFile(context, logFile)
                            } else {
                                Toast.makeText(context, "Log file does not exist", Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.STONE,
                        text = "SHARE LOG",
                        fontSize = 10.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    )

                    MinecraftButton(
                        onClick = {
                            if (logFile.exists()) {
                                com.movtery.zalithlauncher.utils.copyText("Crash Log", logFile.readText(), context, true)
                                Toast.makeText(context, "Copied full log to clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Log file not found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.STONE,
                        text = "COPY LOG",
                        fontSize = 10.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    )

                    MinecraftButton(
                        onClick = onDismiss,
                        style = MinecraftButtonStyle.GREEN,
                        text = "DISMISS",
                        fontSize = 10.sp,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InstallationDownloadForegroundDialog() {
    val activeInstaller by InstallationDownloadManager.activeInstaller.collectAsStateWithLifecycle()
    val isPreparing by InstallationDownloadManager.isPreparing.collectAsStateWithLifecycle()
    val showInstallerDialog by InstallationDownloadManager.showForegroundDialog.collectAsStateWithLifecycle()
    val activeInstallName by InstallationDownloadManager.activeInstallationName.collectAsStateWithLifecycle()

    if (!showInstallerDialog) return

    if (activeInstaller != null) {
        val installTasks by activeInstaller!!.tasksFlow.collectAsStateWithLifecycle()
        val installLog by activeInstaller!!.logOutput.collectAsStateWithLifecycle()
        if (installTasks.isNotEmpty()) {
            com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog(
                title = "INSTALLING ${activeInstallName?.uppercase() ?: "MINECRAFT"}",
                tasks = installTasks,
                onCancel = {
                    InstallationDownloadManager.cancelActiveInstallation()
                },
                onBackground = {
                    InstallationDownloadManager.dismissToBackground()
                },
                logOutput = installLog
            )
        }
    } else if (isPreparing) {
        val preparingTask = remember(activeInstallName) {
            val task = com.movtery.zalithlauncher.coroutine.Task.runTask(id = "preparing_task", task = { })
            listOf(
                com.movtery.zalithlauncher.coroutine.TitledTask(
                    title = com.movtery.zalithlauncher.ui.androidText("Preparing components & resolving loaders..."),
                    task = task
                )
            )
        }
        com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog(
            title = "INSTALLING ${activeInstallName?.uppercase() ?: "MINECRAFT"}",
            tasks = preparingTask,
            onCancel = {
                InstallationDownloadManager.cancelActiveInstallation()
            },
            onBackground = {
                InstallationDownloadManager.dismissToBackground()
            }
        )
    }
}
