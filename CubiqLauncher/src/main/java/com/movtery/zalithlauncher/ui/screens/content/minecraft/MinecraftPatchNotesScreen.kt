/*
 * Cubiq Launcher
 * Minecraft PC Launcher Official Patch Notes Screen
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    // Fallback notes if offline
    val defaultNotes = remember {
        listOf(
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.3",
                version = "26.3",
                body = "Welcome to the latest release of Minecraft Java Edition 26.3!\n\n" +
                        "• New biome features, pale garden ambiance, and creaking mob adjustments\n" +
                        "• Resin blocks and brick crafting recipes\n" +
                        "• Full rendering and rendering pipeline optimizations\n" +
                        "• Bug fixes, memory optimizations, and security patches for Java Edition.",
                imageUrl = "https://launchercontent.mojang.com/v2/images/dappledcamp540x540.jpg"
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 26.2",
                version = "26.2",
                body = "Release 26.2 introduces major stability improvements, redstone fixes, and enhanced multiplayer packet compression.",
                imageUrl = null
            ),
            OfficialPatchNote(
                title = "Minecraft: Java Edition 1.21.4 - Tricky Trials",
                version = "1.21.4",
                body = "• Trial Chambers: Explore sprawling underground structures filled with perilous challenges, trial spawners, and vaults.\n" +
                        "• The Breeze: A hostile mob that leaps circles around opponents and fires wind charges.\n" +
                        "• The Crafter: A new Redstone block that enables automated crafting of items.\n" +
                        "• The Mace: A powerful new weapon delivering devastating smash attacks from high falls.\n" +
                        "• Performance and security enhancements for Java Edition.",
                imageUrl = null
            )
        )
    }

    val displayNotes = if (livePatchNotes.isNotEmpty()) livePatchNotes else defaultNotes
    var selectedNote by remember(displayNotes) { mutableStateOf(displayNotes.first()) }

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
            // Version Selector Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayNotes.take(12)) { note ->
                    val isSelected = selectedNote.version == note.version
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) Color(0xFF3C8527) else Color(0xFF222224))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)
                                ),
                                RoundedCornerShape(3.dp)
                            )
                            .clickable {
                                selectedNote = note
                                if (!note.contentPath.isNullOrBlank()) {
                                    MinecraftOfficialContentManager.fetchDetailedPatchNote(note)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = note.version,
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Patch Note Content Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1E20))
                    .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                // Update Banner Image
                if (!selectedNote.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = selectedNote.imageUrl,
                        contentDescription = selectedNote.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Image(
                        painter = painterResource(R.drawable.img_mc_hero_latest),
                        contentDescription = "Minecraft Art",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.img_minecraft),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedNote.title.ifBlank { "Minecraft ${selectedNote.version}" },
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Release details body
                Text(
                    text = cleanPatchNoteHtml(selectedNote.body),
                    color = Color(0xFFD0D0D0),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Changelog Button
                MinecraftButton(
                    onClick = {
                        onOpenLink("https://www.minecraft.net/en-us/article/minecraft-java-edition-${selectedNote.version.replace('.', '-')}")
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = "VIEW FULL CHANGELOG ON MINECRAFT.NET",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
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
