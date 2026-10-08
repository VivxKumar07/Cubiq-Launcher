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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

        // Active navigation state from singleton so any screen can trigger tab change
        val activeTab = LauncherNavState.activeTab
        val selectedBottomNav = LauncherNavState.selectedBottomNav

        var showVersionMenu by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val toAccountManageScreen = {
            LauncherNavState.selectedBottomNav = 3 // Switch directly to custom Profile tab
            LauncherNavState.activeTab = -1
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Account Header with Interactive Account Switcher Dropdown
                TopAccountHeader(
                    account = account,
                    accountsList = accountsList,
                    onSelectAccount = { selectedAcc ->
                        AccountsManager.setCurrentAccount(selectedAcc)
                        Toast.makeText(context, "Switched to ${selectedAcc.username}", Toast.LENGTH_SHORT).show()
                    },
                    onManageAccounts = toAccountManageScreen
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
                                                                text = "Latest Release (26.3)",
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
                                            onPlayClick = {
                                                if (account == null) {
                                                    Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                                                    LauncherNavState.selectedBottomNav = 3
                                                    LauncherNavState.activeTab = -1
                                                    return@PlayActionRow
                                                }
                                                val targetVersion = currentVersion ?: allVersions.firstOrNull()
                                                if (targetVersion == null) {
                                                    Toast.makeText(context, "Please create an installation first!", Toast.LENGTH_SHORT).show()
                                                    LauncherNavState.activeTab = 0
                                                    return@PlayActionRow
                                                }
                                                onLaunchGame(targetVersion)
                                            }
                                        )

                                        // Installed Versions Cards (Replaced Security Advisory Card)
                                        InstalledVersionsSection(
                                            allVersions = allVersions,
                                            currentVersion = currentVersion,
                                            onSelectVersion = { ver ->
                                                VersionsManager.saveVersion(ver)
                                            },
                                            onPlayVersion = { ver ->
                                                if (account == null) {
                                                    Toast.makeText(context, "Please create an account first!", Toast.LENGTH_SHORT).show()
                                                    LauncherNavState.selectedBottomNav = 3
                                                    LauncherNavState.activeTab = -1
                                                    return@InstalledVersionsSection
                                                }
                                                VersionsManager.saveVersion(ver)
                                                onLaunchGame(ver)
                                            },
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

                // 1:1 Replica Minecraft PC Mobile Launcher Bottom Navigation Bar
                // Options: News | Versions | Mods | Profile
                MinecraftPCBottomNavBar(
                    selectedItem = selectedBottomNav,
                    onItemSelected = { index ->
                        LauncherNavState.selectedBottomNav = index
                        LauncherNavState.activeTab = -1
                    }
                )
            }
        }
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
    onManageAccounts: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161616))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Head Avatar with vibrant blue rounded square border
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(BorderStroke(2.dp, Color(0xFF1E88E5)), RoundedCornerShape(6.dp))
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                val skinFile = account?.getSkinFile()
                if (account != null && skinFile != null && skinFile.exists()) {
                    PlayerFace(
                        account = account,
                        avatarSize = 36.dp
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_profile),
                        contentDescription = "Steve Face",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                        fontSize = 16.sp,
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
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }
        }

        // Account Dropdown Selector
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(280.dp)
                .background(Color(0xFF1C1C1E))
                .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
        ) {
            Text(
                text = "SWITCH ACCOUNT",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            if (accountsList.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "No saved accounts",
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    onClick = {
                        expanded = false
                        onManageAccounts()
                    }
                )
            } else {
                accountsList.forEach { acc ->
                    val isCurrent = account?.uniqueUUID == acc.uniqueUUID
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PlayerFace(account = acc, avatarSize = 22.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.username,
                                        color = if (isCurrent) Color(0xFF55FF55) else Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = getAccountTypeName(acc),
                                        color = Color(0xFF888888),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 10.sp
                                    )
                                }
                                if (isCurrent) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color(0xFF55FF55),
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelectAccount(acc)
                        }
                    )
                }
            }

            DropdownMenuItem(
                text = {
                    Text(
                        text = "+ Manage Accounts & Settings",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                onClick = {
                    expanded = false
                    onManageAccounts()
                }
            )
        }
    }
}

/**
 * Sub-Navigation Tabs: Installations | Skins | Patch Notes
 * Centered horizontally across the screen as requested
 */
@Composable
private fun SubNavigationTabs(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Installations", "Skins", "Patch Notes")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161616))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = activeTab == index
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                fontFamily = MinecraftFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            )
        }
    }
}

/**
 * Hero Card with authentic Minecraft pixel frame border, dynamic key art based on selected version,
 * 3D embossed MINECRAFT JAVA EDITION logo, and bottom version selector pill.
 */
@Composable
private fun HeroMinecraftCard(
    version: Version?,
    account: Account?,
    onVersionClick: () -> Unit
) {
    val latestReleaseVersion by MinecraftOfficialContentManager.latestReleaseVersion.collectAsStateWithLifecycle()
    val latestHeroUrl by MinecraftOfficialContentManager.latestHeroImageUrl.collectAsStateWithLifecycle()
    val heroArtRes = getHeroArtForVersion(version)

    MinecraftPixelBorderContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(290.dp),
        borderColor = Color(0xFF3C8527)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork (Dynamic based on selected version or latest release)
            val artModel = remember(version) {
                MinecraftArtworkManager.getArtworkModel(version)
            }
            AsyncImage(
                model = artModel,
                placeholder = painterResource(heroArtRes),
                error = painterResource(heroArtRes),
                fallback = painterResource(heroArtRes),
                contentDescription = "Minecraft Version Key Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            // Scrim gradient for contrast at the bottom where version selector sits
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.60f to Color.Transparent,
                            0.82f to Color(0x66000000),
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
                // Version Selector Dropdown Pill (Stone Block Beveled Style)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xEE1E1E1E))
                        .border(BorderStroke(1.5.dp, Color(0xFF444444)), RoundedCornerShape(3.dp))
                        .clickable(onClick = onVersionClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_minecraft),
                        contentDescription = "Grass Block",
                        modifier = Modifier.size(26.dp)
                    )
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
                        Text(
                            text = version?.getVersionName() ?: latestReleaseVersion,
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp,
                            maxLines = 1
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
            onClick = onPlayClick,
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
 * Displays all installations as distinct Minecraft-themed cards below the main latest version card.
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
                    .clickable(onClick = onAddNewClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (allVersions.isEmpty()) {
            MinecraftPixelBorderContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddNewClick),
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
                        onClick = onAddNewClick,
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
                val heroRes = getHeroArtForVersion(ver)
                MinecraftPixelBorderContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectVersion(ver) },
                    borderColor = if (isSelected) Color(0xFF55FF55) else Color(0xFF333333)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF1E281C) else Color(0xFF1A1A1C))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(heroRes),
                            contentDescription = ver.getVersionName(),
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            contentScale = ContentScale.Crop
                        )

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
                            val loaderName = ver.getVersionInfo()?.loaderInfo?.loader?.displayName ?: "Vanilla"
                            Text(
                                text = "Loader: $loaderName",
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
                            onClick = { onPlayVersion(ver) },
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
