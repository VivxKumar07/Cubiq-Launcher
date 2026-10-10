/*
 * Cubiq Launcher
 * Minecraft Official Realms & Multiplayer Servers Screen
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

data class MinecraftServerEntry(
    val name: String,
    val address: String,
    val motd: String,
    val pingMs: Int = 32
)

@Composable
fun MinecraftRealmsScreen(
    account: Account?,
    currentVersion: Version?,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showAddServerDialog by remember { mutableStateOf(false) }

    val servers = remember {
        mutableStateListOf(
            MinecraftServerEntry("Hypixel Network", "mc.hypixel.net", "The #1 Minecraft Minigame Server", 28),
            MinecraftServerEntry("CubeCraft Games", "play.cubecraft.net", "Skyblock, EggWars, Lucky Islands", 35),
            MinecraftServerEntry("Complex Gaming", "hub.mc-complex.com", "Pixelmon, Skyblock, Factions, Survival", 42),
            MinecraftServerEntry("Wynncraft", "play.wynncraft.com", "The premier Minecraft MMORPG Experience", 31)
        )
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
            // Realms Promo Hero Banner (Image 2 style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_mc_hero_latest),
                    contentDescription = "Realms Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xE6101012), Color(0x9918181A), Color(0x33000000))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MINECRAFT REALMS & MULTIPLAYER",
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Play always-online worlds with friends on PC and Mobile!",
                            color = Color(0xFFCCCCCC),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinecraftButton(
                            onClick = {
                                onOpenLink("https://www.minecraft.net/realms")
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "TRY REALMS PLUS",
                            fontSize = 10.sp,
                            modifier = Modifier.height(32.dp)
                        )
                        MinecraftButton(
                            onClick = { showAddServerDialog = true },
                            style = MinecraftButtonStyle.STONE,
                            text = "+ ADD SERVER",
                            fontSize = 10.sp,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Servers List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FEATURED MULTIPLAYER SERVERS",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${servers.size} SERVERS",
                    color = Color(0xFF888888),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Server Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(servers) { srv ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(3.dp))
                            .background(Color(0xFF1A1A1C))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_minecraft),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = srv.name,
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = srv.address,
                                    color = Color(0xFF55FF55),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    text = srv.motd,
                                    color = Color(0xFF888888),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 9.5.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        MinecraftButton(
                            onClick = {
                                onLaunchGame(currentVersion)
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "JOIN",
                            fontSize = 11.sp,
                            modifier = Modifier
                                .width(65.dp)
                                .height(32.dp)
                        )
                    }
                }
            }
        }

        // Add Server Dialog
        if (showAddServerDialog) {
            var srvName by remember { mutableStateOf("") }
            var srvAddr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddServerDialog = false },
                containerColor = Color(0xFF1E1E20),
                title = {
                    Text(
                        text = "ADD MULTIPLAYER SERVER",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SERVER NAME",
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = srvName,
                                onValueChange = { srvName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }

                        Text(
                            text = "SERVER ADDRESS (IP / DOMAIN)",
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = srvAddr,
                                onValueChange = { srvAddr = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }
                    }
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            if (srvName.isNotBlank() && srvAddr.isNotBlank()) {
                                servers.add(MinecraftServerEntry(srvName.trim(), srvAddr.trim(), "Custom Multiplayer Server"))
                                showAddServerDialog = false
                                Toast.makeText(context, "Added $srvName", Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = "ADD",
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                },
                dismissButton = {
                    MinecraftButton(
                        onClick = { showAddServerDialog = false },
                        style = MinecraftButtonStyle.STONE,
                        text = "CANCEL",
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                }
            )
        }
    }
}
