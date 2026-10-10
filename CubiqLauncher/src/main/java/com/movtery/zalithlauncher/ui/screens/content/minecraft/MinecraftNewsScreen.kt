/*
 * Cubiq Launcher
 * Minecraft PC Launcher Official News Screen (Fetched from minecraft.net)
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.movtery.zalithlauncher.game.versioninfo.OfficialNewsItem
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@Composable
fun MinecraftNewsScreen(
    onOpenLink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val liveNews by MinecraftOfficialContentManager.news.collectAsStateWithLifecycle()

    val fallbackNews = remember {
        listOf(
            OfficialNewsItem(
                title = "Minecraft Java Edition Latest Updates & Features",
                category = "MINECRAFT JAVA EDITION",
                imageUrl = "https://launchercontent.mojang.com/v2/images/1-21-patchnotes.jpg",
                readMoreUrl = "https://www.minecraft.net/en-us/article"
            ),
            OfficialNewsItem(
                title = "Security Vulnerability Advisory in Java Edition",
                category = "SECURITY ADVISORY",
                imageUrl = "",
                readMoreUrl = "https://www.minecraft.net/en-us/article/important-message--security-vulnerability-java-edition"
            ),
            OfficialNewsItem(
                title = "Tricky Trials Drop: Discover Trial Chambers and the Mace",
                category = "GAME UPDATE",
                imageUrl = "",
                readMoreUrl = "https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21"
            )
        )
    }

    val displayNews = if (liveNews.isNotEmpty()) liveNews else fallbackNews

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OFFICIAL MINECRAFT NEWS",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // News List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayNews) { item ->
                    NewsArticleCard(
                        news = item,
                        onClick = { onOpenLink(item.readMoreUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsArticleCard(
    news: OfficialNewsItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        // High-res Image Banner (Zoomed out with proper 16:9 aspect ratio)
        if (news.imageUrl.isNotBlank()) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = news.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(Color(0xFF141416)),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(R.drawable.img_mc_news_cave),
                contentDescription = "News Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(14.dp)) {
            // Category Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF283824))
                    .border(BorderStroke(1.dp, Color(0xFF3C8527)), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = news.category.uppercase(),
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = news.title,
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Read More Button
            MinecraftButton(
                onClick = onClick,
                style = MinecraftButtonStyle.GREEN,
                text = "READ ON MINECRAFT.NET",
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            )
        }
    }
}
