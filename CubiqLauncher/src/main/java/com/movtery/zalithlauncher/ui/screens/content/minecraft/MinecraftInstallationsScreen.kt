/*
 * Cubiq Launcher
 * Minecraft PC Launcher Installations Screen (1:1 Replica with Real Version Creation & Selection)
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.ui.components.IsometricBlockIcons
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.navigateToDownload
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import org.apache.commons.io.FileUtils
import java.io.File

@Composable
fun MinecraftInstallationsScreen(
    currentVersion: Version?,
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: ((Version) -> Unit)? = null,
    onLaunchGame: (Version?) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val allVersions by VersionsManager.versions.collectAsStateWithLifecycle()
    val latestReleaseVersion by MinecraftOfficialContentManager.latestReleaseVersion.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterReleases by remember { mutableStateOf(true) }
    var filterSnapshots by remember { mutableStateOf(true) }
    var filterModded by remember { mutableStateOf(true) }

    var showNewInstallationDialog by remember { mutableStateOf(false) }
    var createdInstallationName by remember { mutableStateOf<String?>(null) }
    var modSwitcherVersion by remember { mutableStateOf<Version?>(null) }
    var versionForIconChange by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header Action Bar: Stone 3D New Installation Button | Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Compact Rectangular "+ New..." Button (Exact 1:1 replica of Image 1)
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF222428))
                        .border(BorderStroke(1.dp, Color(0xFF3A3A3E)), RoundedCornerShape(3.dp))
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            showNewInstallationDialog = true
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color(0xFF4ADE80),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "New...",
                            color = Color(0xFFE2E2E2),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Direct "+ Modpack" Profile Button
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF222428))
                        .border(BorderStroke(1.dp, Color(0xFF3A3A3E)), RoundedCornerShape(3.dp))
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            backStackViewModel.navigateToDownload(backStackViewModel.downloadModPackScreen)
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color(0xFF38BDF8),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Modpack",
                            color = Color(0xFFE2E2E2),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Unclipped, Pixel-Aligned Search Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E1E20))
                        .border(
                            BorderStroke(
                                1.dp,
                                if (searchQuery.isNotEmpty()) Color(0xFF55FF55) else Color(0xFF383838)
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF55FF55)),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search installations...",
                                        color = Color(0xFF777777),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = Color(0xFF888888),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            // Filters Row: Checkboxes for Releases, Snapshots, Modded
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERSIONS:",
                    color = Color(0xFF888888),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))

                InstallationFilterCheckbox(
                    label = "Releases",
                    checked = filterReleases,
                    onCheckedChange = { filterReleases = it }
                )

                Spacer(modifier = Modifier.width(8.dp))

                InstallationFilterCheckbox(
                    label = "Snapshots",
                    checked = filterSnapshots,
                    onCheckedChange = { filterSnapshots = it }
                )

                Spacer(modifier = Modifier.width(8.dp))

                InstallationFilterCheckbox(
                    label = "Modded",
                    checked = filterModded,
                    onCheckedChange = { filterModded = it }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Installation List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filtered = allVersions.filter { ver ->
                    val name = ver.getVersionName()
                    val isModded = ver.getVersionInfo()?.loaderInfo?.loader?.isLoader == true
                    val isSnapshot = name.contains("snapshot", ignoreCase = true) || name.contains("w", ignoreCase = true)
                    val isRelease = !isModded && !isSnapshot

                    val matchesFilter = (filterModded && isModded) ||
                            (filterSnapshots && isSnapshot) ||
                            (filterReleases && isRelease) ||
                            (!filterModded && !filterSnapshots && !filterReleases)

                    val matchesQuery = searchQuery.isBlank() || name.contains(searchQuery.trim(), ignoreCase = true)
                    matchesFilter && matchesQuery
                }

                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E1E20))
                                .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NO INSTALLATIONS FOUND",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Click '+ New...' above to download and install any version of Minecraft!",
                                    color = Color(0xFFAAAAAA),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                items(filtered) { ver ->
                    val isSelected = currentVersion?.getVersionName() == ver.getVersionName()
                    val isModded = ver.getVersionInfo()?.loaderInfo?.loader?.isLoader == true
                    val customIconFile = ver.getVersionIconFile()
                    val blockIcon = when {
                        isModded -> R.drawable.img_diamond_block
                        ver.getVersionName().contains("snapshot", ignoreCase = true) -> R.drawable.img_command_block
                        else -> R.drawable.img_old_grass_block
                    }

                    val mcVer = ver.getVersionInfo()?.minecraftVersion
                    val loaderName = ver.getVersionInfo()?.loaderInfo?.loader?.displayName ?: "Vanilla"
                    val hasRealMc = mcVer != null && (mcVer != ver.getVersionName() || mcVer.contains("."))
                    val versionSubtitle = if (hasRealMc) {
                        "$loaderName • $mcVer"
                    } else {
                        loaderName
                    }

                    InstallationCardItem(
                        iconRes = blockIcon,
                        iconFile = if (customIconFile.exists()) customIconFile else null,
                        name = ver.getVersionName(),
                        versionTag = versionSubtitle,
                        isCurrent = isSelected,
                        onPlayClick = {
                            VersionsManager.saveVersion(ver)
                            onLaunchGame(ver)
                        },
                        onSelectClick = {
                            VersionsManager.saveVersion(ver)
                            Toast.makeText(context, "Active installation: ${ver.getVersionName()}", Toast.LENGTH_SHORT).show()
                        },
                        onManageClick = {
                            if (navigateToVersions != null) {
                                navigateToVersions(ver)
                            } else {
                                VersionsManager.saveVersion(ver)
                                backStackViewModel.mainScreen.navigateTo(NormalNavKey.VersionsManager)
                            }
                        },
                        onChangeIconClick = {
                            versionForIconChange = ver.getVersionName()
                        },
                        onToggleModsClick = {
                            modSwitcherVersion = ver
                        },
                        onDeleteClick = {
                            runCatching {
                                FileUtils.deleteQuietly(ver.getVersionPath())
                                VersionsManager.refresh("UserDelete")
                                Toast.makeText(context, "Deleted ${ver.getVersionName()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // New Installation Creation Dialog
        if (showNewInstallationDialog) {
            NewInstallationDialog(
                latestVersion = latestReleaseVersion,
                onDismiss = { showNewInstallationDialog = false },
                onAddModPackClick = {
                    showNewInstallationDialog = false
                    backStackViewModel.navigateToDownload(backStackViewModel.downloadModPackScreen)
                },
                onCreate = { name, version, loader, iconRes, iconId ->
                    showNewInstallationDialog = false
                    InstallationDownloadManager.startInstallation(
                        context = context,
                        name = name,
                        version = version,
                        loader = loader,
                        iconRes = iconRes,
                        onSuccess = { installedName ->
                            createdInstallationName = installedName
                        }
                    )
                }
            )
        }



        // Cubiq Installation Success Dialog
        createdInstallationName?.let { installedName ->
            AlertDialog(
                onDismissRequest = { createdInstallationName = null },
                containerColor = Color(0xFF1E1E20),
                title = {
                    Text(
                        text = "INSTALLATION READY",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                text = {
                    Text(
                        text = "Installation '$installedName' is configured and ready to play or download!",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = { createdInstallationName = null },
                        style = MinecraftButtonStyle.GREEN,
                        text = "OK",
                        fontSize = 12.sp,
                        modifier = Modifier.width(80.dp).height(36.dp)
                    )
                }
            )
        }

        // Direct In-Launcher Mod Switcher Dialog
        modSwitcherVersion?.let { ver ->
            InstallationModsDialog(
                version = ver,
                onDismiss = { modSwitcherVersion = null }
            )
        }

        versionForIconChange?.let { verName ->
            ChangeInstallationIconDialog(
                versionName = verName,
                onDismiss = { versionForIconChange = null }
            )
        }
    }
}

@Composable
private fun InstallationFilterCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF3C8527),
                checkmarkColor = Color.White,
                uncheckedColor = Color(0xFF666666)
            ),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (checked) Color.White else Color(0xFF888888),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun InstallationCardItem(
    iconRes: Int,
    iconFile: File? = null,
    name: String,
    versionTag: String,
    isCurrent: Boolean,
    onPlayClick: () -> Unit,
    onSelectClick: () -> Unit,
    onManageClick: () -> Unit = {},
    onChangeIconClick: () -> Unit = {},
    onToggleModsClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(if (isCurrent) Color(0xFF222428) else Color(0xFF1B1C1E))
            .border(
                BorderStroke(
                    1.dp,
                    if (isCurrent) Color(0xFF3C8527) else Color(0xFF2E3033)
                ),
                RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onSelectClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Block Icon
        if (iconFile != null && iconFile.exists()) {
            AsyncImage(
                model = iconFile,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        } else {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Version Tag
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF1E3A1E))
                            .border(BorderStroke(1.dp, Color(0xFF3C8527)), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = versionTag,
                color = Color(0xFF888888),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Green Play Button
        MinecraftButton(
            onClick = onPlayClick,
            style = MinecraftButtonStyle.GREEN,
            text = "Play",
            fontSize = 12.sp,
            modifier = Modifier
                .width(68.dp)
                .height(34.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Three Dots Options Button
        Box {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF28282A))
                    .border(BorderStroke(1.dp, Color(0xFF3C3C3E)), RoundedCornerShape(2.dp))
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "...",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color(0xFF1E1E20))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Play Installation",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onPlayClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Select as Active",
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onSelectClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Manage Installation",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onManageClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Change Icon",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onChangeIconClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Toggle / Manage Mods",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onToggleModsClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete Installation",
                            color = Color(0xFFFF5555),
                            fontFamily = MinecraftFontFamily
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}

/**
 * Direct In-Launcher Mod Switcher Dialog (Enables/Disables .jar <-> .jar.disabled on the fly)
 */
@Composable
fun InstallationModsDialog(
    version: Version,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val modsDir = remember(version) {
        VersionFolders.MOD.getDir(version.getGameDir())
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val modFiles = remember(refreshTrigger) {
        if (!modsDir.exists()) emptyList()
        else {
            modsDir.listFiles { file ->
                file.isFile && (file.name.endsWith(".jar", ignoreCase = true) || file.name.endsWith(".disabled", ignoreCase = true))
            }?.sortedBy { it.name.lowercase() }?.toList() ?: emptyList()
        }
    }

    val filteredMods = remember(modFiles, searchQuery) {
        if (searchQuery.isBlank()) modFiles
        else modFiles.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E20),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = "MANAGE MODS",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = version.getVersionName(),
                        color = Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val activeCount = modFiles.count { !it.name.endsWith(".disabled", ignoreCase = true) }
                Text(
                    text = "$activeCount/${modFiles.size} ACTIVE",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                if (modFiles.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF28282A))
                                .border(1.dp, Color(0xFF444444), RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 11.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF55FF55)),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search mods...",
                                        color = Color(0xFF777777),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 11.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Enable All
                        MinecraftButton(
                            onClick = {
                                modFiles.filter { it.name.endsWith(".disabled", ignoreCase = true) }.forEach { file ->
                                    val newName = file.name.removeSuffix(".disabled")
                                    file.renameTo(File(file.parentFile, newName))
                                }
                                refreshTrigger++
                                Toast.makeText(context, "Enabled all mods", Toast.LENGTH_SHORT).show()
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "ALL ON",
                            fontSize = 10.sp,
                            modifier = Modifier
                                .width(62.dp)
                                .height(32.dp)
                        )

                        // Disable All
                        MinecraftButton(
                            onClick = {
                                modFiles.filter { it.name.endsWith(".jar", ignoreCase = true) }.forEach { file ->
                                    val newName = file.name + ".disabled"
                                    file.renameTo(File(file.parentFile, newName))
                                }
                                refreshTrigger++
                                Toast.makeText(context, "Disabled all mods", Toast.LENGTH_SHORT).show()
                            },
                            style = MinecraftButtonStyle.STONE,
                            text = "ALL OFF",
                            fontSize = 10.sp,
                            modifier = Modifier
                                .width(66.dp)
                                .height(32.dp)
                        )
                    }
                }

                if (filteredMods.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (modFiles.isEmpty()) "No mods found in 'mods/' folder." else "No matching mods.",
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredMods, key = { it.absolutePath }) { file ->
                            val isEnabled = !file.name.endsWith(".disabled", ignoreCase = true)
                            val cleanName = file.name.removeSuffix(".disabled").removeSuffix(".jar")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isEnabled) Color(0xFF25272B) else Color(0xFF1B1B1C))
                                    .border(1.dp, if (isEnabled) Color(0xFF3C8527) else Color(0xFF333333), RoundedCornerShape(2.dp))
                                    .clickable {
                                        val newFile = if (isEnabled) {
                                            File(file.parentFile, file.name + ".disabled")
                                        } else {
                                            File(file.parentFile, file.name.removeSuffix(".disabled"))
                                        }
                                        file.renameTo(newFile)
                                        refreshTrigger++
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_mc_pc_mods),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                        if (isEnabled) Color(0xFF55FF55) else Color(0xFF777777)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cleanName,
                                        color = if (isEnabled) Color.White else Color(0xFF888888),
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isEnabled) ".jar (ACTIVE)" else ".jar.disabled (INACTIVE)",
                                        color = if (isEnabled) Color(0xFF55FF55) else Color(0xFFFF5555),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 10.sp
                                    )
                                }
                                Checkbox(
                                    checked = isEnabled,
                                    onCheckedChange = { checked ->
                                        val newFile = if (!checked) {
                                            File(file.parentFile, file.name + ".disabled")
                                        } else {
                                            File(file.parentFile, file.name.removeSuffix(".disabled"))
                                        }
                                        file.renameTo(newFile)
                                        refreshTrigger++
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF3C8527),
                                        checkmarkColor = Color.White,
                                        uncheckedColor = Color(0xFF555555)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.GREEN,
                text = "DONE",
                fontSize = 12.sp,
                modifier = Modifier
                    .width(80.dp)
                    .height(34.dp)
            )
        }
    )
}

private fun isVersionSupportedForLoader(version: String, loader: String): Boolean {
    val clean = version.trim().removePrefix("Minecraft ").split("-")[0]
    val parts = clean.split(".").mapNotNull { it.toIntOrNull() }
    val major = parts.getOrNull(0) ?: 1
    val minor = parts.getOrNull(1) ?: 0
    val patch = parts.getOrNull(2) ?: 0

    return when (loader) {
        "Vanilla" -> true
        "OptiFine" -> {
            // OptiFine is available for versions 1.7.2+
            (major == 1 && minor >= 7) || major > 1
        }
        "Forge" -> {
            // Forge is available for 1.1+
            (major == 1 && minor >= 1) || major > 1
        }
        "Fabric" -> {
            // Fabric requires 1.14+
            (major == 1 && minor >= 14) || major > 1
        }
        "Quilt" -> {
            // Quilt requires 1.14+
            (major == 1 && minor >= 14) || major > 1
        }
        "NeoForge" -> {
            // NeoForge was introduced in 1.20.2+
            major > 1 || (major == 1 && minor > 20) || (major == 1 && minor == 20 && patch >= 2)
        }
        else -> true
    }
}

@Composable
private fun NewInstallationDialog(
    latestVersion: String,
    onDismiss: () -> Unit,
    onAddModPackClick: () -> Unit,
    onCreate: (name: String, version: String, loader: String, iconRes: Int, iconId: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedVersion by remember { mutableStateOf(if (latestVersion.isNotBlank()) latestVersion else "1.21.4") }
    var selectedLoader by remember { mutableStateOf("Vanilla") }
    var selectedIconId by remember { mutableStateOf("grass") }
    val blockIcons = IsometricBlockIcons.ICONS
    val selectedIconRes = IsometricBlockIcons.getDrawableById(selectedIconId)
    var showVersionPicker by remember { mutableStateOf(false) }
    var versionSearchQuery by remember { mutableStateOf("") }
    var filterOnlyReleases by remember { mutableStateOf(true) }

    var allManifestVersions by remember {
        mutableStateOf<List<com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest.Version>>(emptyList())
    }

    LaunchedEffect(Unit) {
        runCatching {
            val manifest = MinecraftVersions.getVersionManifest(false)
            allManifestVersions = manifest.versions
        }.onFailure {
            // Offline fallback
            allManifestVersions = listOf(
                "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
                "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.20",
                "1.19.4", "1.19.2", "1.18.2", "1.16.5", "1.12.2", "1.10", "1.8.9", "1.7.10"
            ).map { id ->
                com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest.Version(
                    id = id,
                    type = "release",
                    url = "",
                    time = "",
                    releaseTime = ""
                )
            }
        }
    }

    val filteredVersions = remember(allManifestVersions, versionSearchQuery, filterOnlyReleases) {
        allManifestVersions.filter { ver ->
            val matchesType = !filterOnlyReleases || ver.type == "release"
            val matchesQuery = versionSearchQuery.isBlank() || ver.id.contains(versionSearchQuery.trim(), ignoreCase = true)
            matchesType && matchesQuery
        }
    }

    val allLoaders = listOf("Vanilla", "OptiFine", "Fabric", "Forge", "NeoForge", "Quilt")
    val availableLoaders = remember(selectedVersion) {
        allLoaders.filter { isVersionSupportedForLoader(selectedVersion, it) }
    }

    LaunchedEffect(availableLoaders) {
        if (selectedLoader !in availableLoaders) {
            selectedLoader = "Vanilla"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E20),
        title = {
            Text(
                text = "CREATE NEW INSTALLATION",
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
                    .verticalScrollWithBar(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Icon Selection Grid
                Text(
                    text = "INSTALLATION ICON",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    blockIcons.forEach { iconItem ->
                        val isSelected = selectedIconId == iconItem.id
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Color(0xFF2E4C22) else Color(0xFF262628))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedIconId = iconItem.id },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(iconItem.drawableRes),
                                contentDescription = iconItem.name,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // 2. Name
                Text(
                    text = "INSTALLATION NAME",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF28282A))
                        .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF55FF55)),
                        decorationBox = { innerTextField ->
                            if (name.isEmpty()) {
                                Text(
                                    text = "$selectedVersion $selectedLoader",
                                    color = Color(0xFF666666),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // 3. Version Picker Selector
                Text(
                    text = "MINECRAFT VERSION",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF28282A))
                        .border(BorderStroke(1.dp, Color(0xFF3C3C3E)), RoundedCornerShape(3.dp))
                        .clickable { showVersionPicker = !showVersionPicker }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minecraft $selectedVersion",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (showVersionPicker) "▲" else "▼",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                // If Version Picker Expanded: Show real versions list with search & release filter
                if (showVersionPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF18181A))
                            .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                            .padding(8.dp)
                    ) {
                        // Version Search Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF222224))
                                .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = versionSearchQuery,
                                onValueChange = { versionSearchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 11.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55)),
                                decorationBox = { innerTextField ->
                                    if (versionSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Search version (1.20, 1.16, 1.10...)",
                                            color = Color(0xFF777777),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Type Filter Chips: Releases vs Snapshots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (filterOnlyReleases) Color(0xFF3C8527) else Color(0xFF28282A))
                                    .clickable { filterOnlyReleases = true }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Releases", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (!filterOnlyReleases) Color(0xFF3C8527) else Color(0xFF28282A))
                                    .clickable { filterOnlyReleases = false }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("All (Snapshots)", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Scrollable Versions List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredVersions) { ver ->
                                val isSelected = selectedVersion == ver.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) Color(0xFF263820) else Color(0xFF202022))
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSelected) Color(0xFF55FF55) else Color(0xFF303030)
                                            ),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable {
                                            selectedVersion = ver.id
                                            showVersionPicker = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ver.id,
                                        color = if (isSelected) Color(0xFF55FF55) else Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    )
                                    Text(
                                        text = ver.type.uppercase(),
                                        color = Color(0xFF888888),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Mod Loader (Filtered dynamically by version)
                Text(
                    text = "MOD LOADER",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
                val loaderRows = listOf(
                    listOf("Vanilla", "Fabric", "Forge"),
                    listOf("NeoForge", "Quilt", "OptiFine")
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    loaderRows.forEach { rowLoaders ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowLoaders.forEach { loader ->
                                val isSupported = isVersionSupportedForLoader(selectedVersion, loader)
                                val isSelected = selectedLoader == loader && isSupported
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF3C8527)
                                                !isSupported -> Color(0xFF1B1B1D)
                                                else -> Color(0xFF28282A)
                                            }
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                when {
                                                    isSelected -> Color(0xFF55FF55)
                                                    !isSupported -> Color(0xFF252528)
                                                    else -> Color(0xFF3C3C3E)
                                                }
                                            ),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .then(
                                            if (isSupported) {
                                                Modifier.clickable { selectedLoader = loader }
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = loader,
                                        color = when {
                                            isSelected -> Color.White
                                            !isSupported -> Color(0xFF555555)
                                            else -> Color(0xFFE2E2E2)
                                        },
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Direct Option to Create/Add a Modpack Profile
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF132230))
                        .border(BorderStroke(1.dp, Color(0xFF1D4ED8)), RoundedCornerShape(3.dp))
                        .clickable {
                            onDismiss()
                            onAddModPackClick()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WANT A MODPACK PROFILE?",
                                color = Color(0xFF60A5FA),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Browse CurseForge & Modrinth modpacks directly",
                                color = Color(0xFFA1A1AA),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 9.5.sp
                            )
                        }
                        Text(
                            text = "BROWSE →",
                            color = Color(0xFF93C5FD),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    val finalName = if (name.isBlank()) "$selectedVersion $selectedLoader" else name
                    onCreate(finalName, selectedVersion, selectedLoader, selectedIconRes, selectedIconId)
                },
                style = MinecraftButtonStyle.GREEN,
                text = "CREATE",
                fontSize = 13.sp,
                modifier = Modifier
                    .width(100.dp)
                    .height(38.dp)
            )
        },
        dismissButton = {
            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.STONE,
                text = "CANCEL",
                fontSize = 13.sp,
                modifier = Modifier
                    .width(90.dp)
                    .height(38.dp)
            )
        }
    )
}

/**
 * Saves a chosen Minecraft block icon directly into the version's launcher folder as VersionIcon.png.
 */
fun saveIconToVersion(context: android.content.Context, versionName: String, iconRes: Int) {
    runCatching {
        val versionFolder = File(getVersionsHome(), versionName)
        val iconFolder = File(versionFolder, BuildKeys.LAUNCHER_IDENTIFIER)
        if (!iconFolder.exists()) iconFolder.mkdirs()
        val iconFile = File(iconFolder, "VersionIcon.png")
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, iconRes)
        if (bitmap != null) {
            val out = java.io.FileOutputStream(iconFile)
            try {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            } finally {
                out.close()
            }
        }
    }
}
