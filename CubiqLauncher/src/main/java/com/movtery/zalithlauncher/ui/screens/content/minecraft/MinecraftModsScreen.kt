/*
 * Cubiq Launcher
 * Minecraft PC Launcher Content & Mods Hub (Integrated Real Zalith Downloaders)
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.screens.content.navigateToDownload
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

data class ContentCategory(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val badge: String,
    val onClick: () -> Unit
)

@Composable
fun MinecraftModsScreen(
    currentVersion: Version?,
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onBackClick: () -> Unit
) {
    val categories = listOf(
        ContentCategory(
            title = "Modpacks",
            subtitle = "Browse & install CurseForge and Modrinth modpacks",
            iconRes = R.drawable.ic_package_2_outlined,
            badge = "Modrinth / CF",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadModPackScreen)
            }
        ),
        ContentCategory(
            title = "Mods",
            subtitle = "Search, download and auto-install Fabric & Forge mods",
            iconRes = R.drawable.ic_extension_outlined,
            badge = "Mods Hub",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadModScreen)
            }
        ),
        ContentCategory(
            title = "Shader Packs",
            subtitle = "Enhance visuals with Iris, BSL, Complementary & more",
            iconRes = R.drawable.ic_lightbulb,
            badge = "Shaders",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadShadersScreen)
            }
        ),
        ContentCategory(
            title = "Resource Packs",
            subtitle = "Transform block textures, models, fonts and UI sounds",
            iconRes = R.drawable.ic_format_paint_outlined,
            badge = "Textures",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadResourcePackScreen)
            }
        ),
        ContentCategory(
            title = "Game Versions",
            subtitle = "Install official releases, snapshots, Fabric, Forge & Quilt",
            iconRes = R.drawable.ic_sports_esports_outlined,
            badge = "Installer",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadGameScreen)
            }
        ),
        ContentCategory(
            title = "Worlds & Saves",
            subtitle = "Download adventure maps, survival worlds and creations",
            iconRes = R.drawable.ic_public,
            badge = "Maps",
            onClick = {
                backStackViewModel.navigateToDownload(backStackViewModel.downloadSavesScreen)
            }
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E20))
                    .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(4.dp))
                .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONTENT & MODS HUB",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Download modpacks, shaders, mods & resource packs directly from official repositories",
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    MinecraftButton(
                        onClick = {
                            backStackViewModel.navigateToDownload()
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = "FULL BROWSER",
                        fontSize = 10.sp,
                        modifier = Modifier
                            .width(110.dp)
                            .height(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Version Manager Card (if version selected)
            if (currentVersion != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1A241C))
                        .border(BorderStroke(1.dp, Color(0xFF3C8527)), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE: ${currentVersion.getVersionName()}",
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Manage installed mods, shaders, and configs for this version",
                                color = Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        MinecraftButton(
                            onClick = {
                                navigateToVersions(currentVersion)
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "MANAGE",
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .width(84.dp)
                                .height(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Categories Section Title
            Text(
                text = "DOWNLOAD CATEGORIES",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of categories
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E1E20))
                            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                            .clickable(onClick = item.onClick)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF28282B))
                                    .border(BorderStroke(1.dp, Color(0xFF444444)), RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.title,
                                    tint = Color(0xFF55FF55),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF2E382E))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.badge,
                                            color = Color(0xFF55FF55),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.subtitle,
                                    color = Color(0xFF888888),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                painter = painterResource(R.drawable.ic_keyboard_arrow_right),
                                contentDescription = null,
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
