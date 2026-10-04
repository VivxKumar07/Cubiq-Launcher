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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.navigateToDownload
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import org.apache.commons.io.FileUtils

@Composable
fun MinecraftInstallationsScreen(
    currentVersion: Version?,
    backStackViewModel: ScreenBackStackViewModel,
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
            // Header Action Bar: + New Installation | Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // + New... Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF28282A))
                        .border(BorderStroke(1.5.dp, Color(0xFF444444)), RoundedCornerShape(3.dp))
                        .clickable { showNewInstallationDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New...",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Search Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        placeholder = {
                            Text(
                                text = "Search installations...",
                                color = Color(0xFF888888),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E20),
                            unfocusedContainerColor = Color(0xFF1E1E20),
                            focusedBorderColor = Color(0xFF55FF55),
                            unfocusedBorderColor = Color(0xFF383838),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
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
                // Installed custom/modded versions
                val filtered = allVersions.filter { ver ->
                    searchQuery.isBlank() || ver.getVersionName().contains(searchQuery, ignoreCase = true)
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
                    val blockIcon = when {
                        isModded -> R.drawable.img_diamond_block
                        ver.getVersionName().contains("snapshot", ignoreCase = true) -> R.drawable.img_command_block
                        else -> R.drawable.img_minecraft
                    }

                    InstallationCardItem(
                        iconRes = blockIcon,
                        name = ver.getVersionName(),
                        versionTag = if (isModded) "${ver.getVersionName()} • Modded" else "${ver.getVersionName()} • Official Release",
                        isCurrent = isSelected,
                        onPlayClick = {
                            VersionsManager.saveVersion(ver)
                            onLaunchGame(ver)
                        },
                        onSelectClick = {
                            VersionsManager.saveVersion(ver)
                            Toast.makeText(context, "Active installation: ${ver.getVersionName()}", Toast.LENGTH_SHORT).show()
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
                onCreate = { name, version, loader ->
                    showNewInstallationDialog = false
                    // Look for existing installed version
                    val existing = allVersions.find { it.getVersionName().equals(name, ignoreCase = true) || it.getVersionName().equals(version, ignoreCase = true) }
                    if (existing != null) {
                        VersionsManager.saveVersion(existing)
                        Toast.makeText(context, "Selected installation: ${existing.getVersionName()}", Toast.LENGTH_SHORT).show()
                    } else {
                        // Launch downloader directly for this version
                        backStackViewModel.downloadGameScreen.navigateTo(
                            NormalNavKey.DownloadGame.Addons(version)
                        )
                        backStackViewModel.navigateToDownload(backStackViewModel.downloadGameScreen)
                        Toast.makeText(context, "Opening installer for $version ($loader)", Toast.LENGTH_SHORT).show()
                    }
                }
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
    name: String,
    versionTag: String,
    isCurrent: Boolean,
    onPlayClick: () -> Unit,
    onSelectClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(if (isCurrent) Color(0xFF222426) else Color(0xFF1A1A1C))
            .border(
                BorderStroke(
                    1.dp,
                    if (isCurrent) Color(0xFF55FF55) else Color(0xFF2A2A2C)
                ),
                RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onSelectClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Block Icon
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Version Tag
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
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
                            text = "Select as Default",
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

@Composable
private fun NewInstallationDialog(
    latestVersion: String,
    onDismiss: () -> Unit,
    onCreate: (name: String, version: String, loader: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedVersion by remember { mutableStateOf(if (latestVersion.isNotBlank()) latestVersion else "1.21.4") }
    var selectedLoader by remember { mutableStateOf("Vanilla") }
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
                "1.19.4", "1.19.2", "1.18.2", "1.16.5", "1.12.2", "1.8.9", "1.7.10"
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

    val loaders = listOf("Vanilla", "Fabric", "Forge", "NeoForge", "Quilt")

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
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name
                Text(
                    text = "INSTALLATION NAME",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "$selectedVersion $selectedLoader",
                            color = Color(0xFF666666),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF28282A),
                        unfocusedContainerColor = Color(0xFF28282A),
                        focusedBorderColor = Color(0xFF55FF55),
                        unfocusedBorderColor = Color(0xFF383838),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Version Picker Selector
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
                        OutlinedTextField(
                            value = versionSearchQuery,
                            onValueChange = { versionSearchQuery = it },
                            placeholder = {
                                Text("Search version (1.20, 1.16...)", color = Color(0xFF777777), fontSize = 11.sp)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF222224),
                                unfocusedContainerColor = Color(0xFF222224),
                                focusedBorderColor = Color(0xFF55FF55),
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

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

                // Mod Loader
                Text(
                    text = "MOD LOADER",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    loaders.forEach { loader ->
                        val isSelected = selectedLoader == loader
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) Color(0xFF3C8527) else Color(0xFF28282A))
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF55FF55) else Color(0xFF3C3C3E)
                                    ),
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable { selectedLoader = loader }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loader,
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    val finalName = if (name.isBlank()) "$selectedVersion $selectedLoader" else name
                    onCreate(finalName, selectedVersion, selectedLoader)
                },
                style = MinecraftButtonStyle.GREEN,
                text = "INSTALL",
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
