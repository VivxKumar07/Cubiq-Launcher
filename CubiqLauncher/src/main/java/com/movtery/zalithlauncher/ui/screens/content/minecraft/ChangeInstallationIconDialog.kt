package com.movtery.zalithlauncher.ui.screens.content.minecraft

import com.movtery.zalithlauncher.R
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.IsometricBlockIcons
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.utils.string.getMessageOrToString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeInstallationIconDialog(
    version: Version,
    onDismiss: () -> Unit,
    onIconChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val versionName = version.getVersionName()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(2.dp, Color(0xFF3C8527)), RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.cubiq_secondary_icon_choose),
                color = Color(0xFF55FF55),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.cubiq_secondary_icon_choose_description, versionName),
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 64.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(2.dp)
            ) {
                items(IsometricBlockIcons.ICONS) { icon ->
                    val iconName = when (icon.id) {
                        "grass" -> stringResource(R.string.cubiq_secondary_icon_block_grass)
                        "crafting_table" -> stringResource(R.string.cubiq_secondary_icon_block_crafting_table)
                        "furnace" -> stringResource(R.string.cubiq_secondary_icon_block_furnace)
                        "diamond_block" -> stringResource(R.string.cubiq_secondary_icon_block_diamond_block)
                        "crafter" -> stringResource(R.string.cubiq_secondary_icon_block_crafter)
                        "trial_spawner" -> stringResource(R.string.cubiq_secondary_icon_block_trial_spawner)
                        "vault" -> stringResource(R.string.cubiq_secondary_icon_block_vault)
                        "tnt" -> stringResource(R.string.cubiq_secondary_icon_block_tnt)
                        "obsidian" -> stringResource(R.string.cubiq_secondary_icon_block_obsidian)
                        "crying_obsidian" -> stringResource(R.string.cubiq_secondary_icon_block_crying_obsidian)
                        "netherite" -> stringResource(R.string.cubiq_secondary_icon_block_netherite)
                        "ancient_debris" -> stringResource(R.string.cubiq_secondary_icon_block_ancient_debris)
                        "command_block" -> stringResource(R.string.cubiq_secondary_icon_block_command_block)
                        "beacon" -> stringResource(R.string.cubiq_secondary_icon_block_beacon)
                        "bookshelf" -> stringResource(R.string.cubiq_secondary_icon_block_bookshelf)
                        "cartography_table" -> stringResource(R.string.cubiq_secondary_icon_block_cartography_table)
                        "smithing_table" -> stringResource(R.string.cubiq_secondary_icon_block_smithing_table)
                        "bedrock" -> stringResource(R.string.cubiq_secondary_icon_block_bedrock)
                        "emerald" -> stringResource(R.string.cubiq_secondary_icon_block_emerald)
                        "gold" -> stringResource(R.string.cubiq_secondary_icon_block_gold)
                        "iron" -> stringResource(R.string.cubiq_secondary_icon_block_iron)
                        "lapis" -> stringResource(R.string.cubiq_secondary_icon_block_lapis)
                        "redstone" -> stringResource(R.string.cubiq_secondary_icon_block_redstone)
                        "glowstone" -> stringResource(R.string.cubiq_secondary_icon_block_glowstone)
                        "sea_lantern" -> stringResource(R.string.cubiq_secondary_icon_block_sea_lantern)
                        "shulker_box" -> stringResource(R.string.cubiq_secondary_icon_block_shulker_box)
                        "stone" -> stringResource(R.string.cubiq_secondary_icon_block_stone)
                        "ore_diamond" -> stringResource(R.string.cubiq_secondary_icon_block_ore_diamond)
                        "oak_planks" -> stringResource(R.string.cubiq_secondary_icon_block_oak_planks)
                        "target" -> stringResource(R.string.cubiq_secondary_icon_block_target)
                        "barrel" -> stringResource(R.string.cubiq_secondary_icon_block_barrel)
                        "dirt" -> stringResource(R.string.cubiq_secondary_icon_block_dirt)
                        "glass" -> stringResource(R.string.cubiq_secondary_icon_block_glass)
                        "melon" -> stringResource(R.string.cubiq_secondary_icon_block_melon)
                        "pumpkin" -> stringResource(R.string.cubiq_secondary_icon_block_pumpkin)
                        "note_block" -> stringResource(R.string.cubiq_secondary_icon_block_note_block)
                        "packed_ice" -> stringResource(R.string.cubiq_secondary_icon_block_packed_ice)
                        "amethyst" -> stringResource(R.string.cubiq_secondary_icon_block_amethyst)
                        else -> icon.name
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF28282A))
                            .border(BorderStroke(1.dp, Color(0xFF3C3C3E)), RoundedCornerShape(3.dp))
                            .clickable {
                                MinecraftSoundHelper.playClickSound()
                                runCatching {
                                    saveIconToVersion(context, version, icon.drawableRes)
                                }.onSuccess {
                                    VersionsManager.refresh("ChangeIcon")
                                    Toast.makeText(context, context.getString(R.string.cubiq_secondary_icon_updated, iconName), Toast.LENGTH_SHORT).show()
                                    onIconChanged()
                                    onDismiss()
                                }.onFailure { error ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.cubiq_secondary_icon_failed, error.getMessageOrToString()),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(icon.drawableRes),
                                contentDescription = iconName,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = iconName,
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.STONE,
                text = stringResource(R.string.cubiq_secondary_icon_cancel),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            )
        }
    }
}
