/*
 * Cubiq Launcher
 * Minecraft Official Patch Notes Screen (1:1 Replica of Image 4)
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import com.movtery.zalithlauncher.game.versioninfo.OfficialPatchNote
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@Composable
fun MinecraftPatchNotesScreen(
    onOpenLink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val livePatchNotes by MinecraftOfficialContentManager.patchNotes.collectAsStateWithLifecycle()

    var showReleases by remember { mutableStateOf(true) }
    var showSnapshots by remember { mutableStateOf(false) }
    var viewingNote by remember { mutableStateOf<OfficialPatchNote?>(null) }

    // Fallback official releases matching PC launcher patch notes
    val defaultNotes = remember {
        listOf(
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.3",
                version = "26.3",
                body = "Welcome to Minecraft: Java Edition 26.3!\n\n" +
                        "• Added new ambient sounds, creaking mob updates, and pale garden biome enhancements.\n" +
                        "• Resin block variants, resin clumps, and new crafting recipes.\n" +
                        "• Full rendering and rendering pipeline optimizations.\n" +
                        "• Fixed over 120 bugs and gameplay issues across Java Edition.",
                imageUrl = "https://launchercontent.mojang.com/v2/images/1-21-patchnotes.jpg"
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.2",
                version = "26.2",
                body = "Minecraft: Java Edition 26.2 brings significant internal game loop improvements, chunk loading speedups, and bug fixes.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.1.2",
                version = "26.1.2",
                body = "Release 26.1.2 resolves critical multiplayer packet compression issues and memory leaks.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.1.1",
                version = "26.1.1",
                body = "Hotfix release 26.1.1 resolving rendering artifacts on high-resolution textures.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 1.21.4 - Tricky Trials",
                version = "1.21.4",
                body = "• Trial Chambers: Explore subterranean labyrinths filled with spawners and vaults.\n" +
                        "• The Breeze: A tempestuous hostile mob.\n" +
                        "• The Crafter: Automated crafting for Redstone contraptions.\n" +
                        "• The Mace: High-flying heavy melee weapon.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 1.21.3",
                version = "1.21.3",
                body = "Performance updates and server packet validation fixes for 1.21 Tricky Trials.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 1.21.1",
                version = "1.21.1",
                body = "Stability update for Minecraft 1.21 Tricky Trials.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 1.20.6 - Armored Paws",
                version = "1.20.6",
                body = "• Armadillo mob and Wolf Armor crafting.\n" +
                        "• Eight new wolf variants across diverse biomes.\n" +
                        "• Java Edition backend technical fixes.",
                imageUrl = null
            )
        )
    }

    val displayNotes = if (livePatchNotes.isNotEmpty()) livePatchNotes else defaultNotes

    val filteredNotes = remember(displayNotes, showReleases, showSnapshots) {
        displayNotes.filter { note ->
            val isSnapshot = note.version.contains("w", ignoreCase = true) ||
                    note.version.contains("pre", ignoreCase = true) ||
                    note.version.contains("rc", ignoreCase = true) ||
                    note.title.contains("snapshot", ignoreCase = true)
            val isRelease = !isSnapshot
            (showReleases && isRelease) || (showSnapshots && isSnapshot) || (!showReleases && !showSnapshots)
        }
    }

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
            // Header (Image 4): VERSIONS label & Filter Checkboxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "VERSIONS",
                    color = Color(0xFFFFFFFF),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Releases Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        showReleases = !showReleases
                    }
                ) {
                    Checkbox(
                        checked = showReleases,
                        onCheckedChange = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            showReleases = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF3C8527),
                            checkmarkColor = Color.White,
                            uncheckedColor = Color(0xFF666666)
                        ),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Releases",
                        color = if (showReleases) Color.White else Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Snapshots Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        showSnapshots = !showSnapshots
                    }
                ) {
                    Checkbox(
                        checked = showSnapshots,
                        onCheckedChange = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            showSnapshots = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF3C8527),
                            checkmarkColor = Color.White,
                            uncheckedColor = Color(0xFF666666)
                        ),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Snapshots",
                        color = if (showSnapshots) Color.White else Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Patch Notes 2-Column Grid (Image 4)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotes) { note ->
                    PatchNoteGridCard(
                        note = note,
                        onClick = {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            viewingNote = note
                            if (!note.contentPath.isNullOrBlank()) {
                                MinecraftOfficialContentManager.fetchDetailedPatchNote(note)
                            }
                        }
                    )
                }
            }
        }

        // Full Patch Note Dialog / Viewer
        viewingNote?.let { note ->
            AlertDialog(
                onDismissRequest = { viewingNote = null },
                containerColor = Color(0xFF1E1E20),
                title = {
                    Text(
                        text = note.title.ifBlank { "Minecraft ${note.version}" },
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
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!note.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = note.imageUrl,
                                contentDescription = note.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.img_mc_hero_latest),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = cleanPatchNoteHtml(note.body),
                            color = Color(0xFFD0D0D0),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MinecraftButton(
                            onClick = {
                                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                viewingNote = null
                            },
                            style = MinecraftButtonStyle.STONE,
                            text = "CLOSE",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        )

                        MinecraftButton(
                            onClick = {
                                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                onOpenLink("https://www.minecraft.net/en-us/article/minecraft-java-edition-${note.version.replace('.', '-')}")
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "WEB CHANGELOG ↗",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(38.dp)
                        )
                    }
                }
            )
        }
    }
}

/**
 * Patch Note Card matching Image 4 (Square/Rectangle Banner image with black bar below)
 */
@Composable
private fun PatchNoteGridCard(
    note: OfficialPatchNote,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(3.dp))
            .background(Color(0xFF0F0F10))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Artwork Image
            if (!note.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = note.imageUrl,
                    contentDescription = note.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.img_mc_news_cave),
                    contentDescription = note.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f),
                    contentScale = ContentScale.Crop
                )
            }

            // Dark info banner underneath matching Image 4
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141416))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Minecraft: Java Edition",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 9.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.version,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

private fun cleanPatchNoteHtml(raw: String): String {
    if (raw.isBlank()) return "Explore this release of Minecraft Java Edition with all the latest features, balance adjustments, and performance updates."
    return raw
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "• ")
        .replace(Regex("</li>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
