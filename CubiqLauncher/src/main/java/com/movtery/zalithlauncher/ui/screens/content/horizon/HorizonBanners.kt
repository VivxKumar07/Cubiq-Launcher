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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

/**
 * Promotional banner for Horizon on Home / Play Screen (Panels A, B, C)
 * Exactly matches user requirements:
 * - Horizon logo
 * - Main title "Horizon"
 * - Subtext "Try Minecraft Rust Edition"
 * - Cubiq style > arrow
 * - Static background with no breathing/scaling glitch
 */
@Composable
fun HorizonHomeBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF14151B))
            .border(
                BorderStroke(1.2.dp, Color(0xFF2E323D)),
                RoundedCornerShape(6.dp)
            )
            .clickable {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onClick()
            }
    ) {
        // Crisp static sunset artwork on the right with smooth gradient overlay
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f)
            ) {
                Image(
                    painter = painterResource(R.drawable.art_horizon_sunset),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.CenterEnd
                )
                // Solid gradient fade into card background (static, no breathing)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF14151B),
                                    Color(0xEE14151B),
                                    Color(0x6614151B),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Content Row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Horizon Sunset Logo
                Image(
                    painter = painterResource(R.drawable.ic_horizon_logo),
                    contentDescription = "Horizon Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Horizon",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // Green "NEW" Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1B3D1B))
                                .border(
                                    BorderStroke(1.dp, Color(0xFF388E3C)),
                                    RoundedCornerShape(3.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "NEW",
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Text(
                        text = "Try Minecraft Rust Edition",
                        color = Color(0xFFBBBBBB),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp
                    )
                }
            }

            // Cubiq Style Arrow Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF222428))
                    .border(BorderStroke(1.dp, Color(0xFF3A3A3E)), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ">",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Larger promotional banner on Installations Screen (Panels D, E)
 * Fixes chipping/clipping issues by using flexible height.
 * Static background with no breathing glitch.
 */
@Composable
fun HorizonInstallationsBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF13141A))
            .border(
                BorderStroke(1.2.dp, Color(0xFF2D313D)),
                RoundedCornerShape(8.dp)
            )
            .clickable {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onClick()
            }
    ) {
        // Crisp static sunset background with gradient fade
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (isLandscape) 0.65f else 0.55f)
            ) {
                Image(
                    painter = painterResource(R.drawable.art_horizon_sunset),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.CenterEnd
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF13141A),
                                    Color(0xEE13141A),
                                    Color(0x6613141A),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        // Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Circular Horizon Logo
                Image(
                    painter = painterResource(R.drawable.ic_horizon_logo),
                    contentDescription = "Horizon Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Horizon",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1B3D1B))
                                .border(
                                    BorderStroke(1.dp, Color(0xFF388E3C)),
                                    RoundedCornerShape(3.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "NEW",
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Text(
                        text = "Minecraft 26.3, rewritten from scratch in Rust.",
                        color = Color(0xFFCCCCCC),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp
                    )

                    // Powered by Rust pill
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF222428))
                            .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Powered by Rust",
                            color = Color(0xFFE89A58),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Action Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF222428))
                    .border(
                        BorderStroke(1.dp, Color(0xFF3A3A3E)),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLandscape) "Try Minecraft Rust now →" else "Try now →",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
