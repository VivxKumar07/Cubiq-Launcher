package com.movtery.zalithlauncher.ui.screens.content.horizon

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.horizon.HorizonManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

/**
 * Java / Rust Engine Selector Header (Panel F)
 * Uses official Oracle Java logo and official Rust gear/R logo instead of emojis.
 */
@Composable
fun JavaRustEngineSelector(
    selectedEngine: String, // "Java" or "Rust"
    onSelectEngine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Java Button with official Oracle Java logo
        val isJava = selectedEngine == "Java"
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isJava) Color(0xFF1E3A19) else Color(0xFF181A20))
                .border(
                    BorderStroke(
                        1.5.dp,
                        if (isJava) Color(0xFF55FF55) else Color(0xFF2C303B)
                    ),
                    RoundedCornerShape(4.dp)
                )
                .clickable {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onSelectEngine("Java")
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_java),
                    contentDescription = "Java Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Java",
                    color = if (isJava) Color(0xFF55FF55) else Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Rust Button with official Rust logo
        val isRust = selectedEngine == "Rust"
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isRust) Color(0xFF1E3A19) else Color(0xFF181A20))
                .border(
                    BorderStroke(
                        1.5.dp,
                        if (isRust) Color(0xFF55FF55) else Color(0xFF2C303B)
                    ),
                    RoundedCornerShape(4.dp)
                )
                .clickable {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onSelectEngine("Rust")
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_rust_logo),
                    contentDescription = "Rust Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Rust",
                    color = if (isRust) Color(0xFF55FF55) else Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Rust Installation Card (Panel F)
 */
@Composable
fun RustInstallationCard(
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E2024))
            .border(BorderStroke(1.dp, Color(0xFF33363F)), RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_horizon_logo),
                    contentDescription = "Horizon Icon",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Minecraft Rust (26.3)",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "Horizon · Rust Edition",
                        color = Color(0xFF9E9E9E),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Play Button & Overflow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play Button
                MinecraftButton(
                    onClick = onLaunch,
                    style = MinecraftButtonStyle.GREEN,
                    modifier = Modifier
                        .height(34.dp)
                        .width(72.dp)
                ) {
                    Text(
                        text = "Play",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Options Menu Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF2B2D33))
                            .border(BorderStroke(1.dp, Color(0xFF3E414A)), RoundedCornerShape(3.dp))
                            .clickable { menuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color(0xFF222428))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Re-check files",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                HorizonManager.setupHorizon(context)
                            }
                        )
                    }
                }
            }
        }
    }
}
