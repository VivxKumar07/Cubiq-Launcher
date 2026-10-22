/*
 * Cubiq Launcher
 * News Screen: Official Mojang Minecraft News & Cubiq GitHub Releases / Announcements
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.CubiqGitHubManager
import com.movtery.zalithlauncher.game.versioninfo.CubiqReleaseItem
import com.movtery.zalithlauncher.game.versioninfo.MinecraftOfficialContentManager
import com.movtery.zalithlauncher.game.versioninfo.OfficialNewsItem
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@Composable
fun MinecraftNewsScreen(
    onOpenLink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Minecraft News, 1: Cubiq News

    val liveNews by MinecraftOfficialContentManager.news.collectAsStateWithLifecycle()
    val cubiqReleases by CubiqGitHubManager.releases.collectAsStateWithLifecycle()
    val isGitHubLoading by CubiqGitHubManager.isLoading.collectAsStateWithLifecycle()

    val fallbackNews = remember {
        listOf(
            OfficialNewsItem(
                title = "Minecraft Java Edition Updates & Features",
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
    val scrollState = rememberScrollState()

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
            // Top Tabs: [ MINECRAFT NEWS ] | [ CUBIQ NEWS ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minecraft News Tab Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selectedTab == 0) Color(0xFF2E6930) else Color(0xFF222428))
                        .border(
                            BorderStroke(1.dp, if (selectedTab == 0) Color(0xFF55FF55) else Color(0xFF3A3A3E)),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            selectedTab = 0
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MINECRAFT NEWS",
                        color = if (selectedTab == 0) Color.White else Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Cubiq News Tab Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selectedTab == 1) Color(0xFF1D4ED8) else Color(0xFF222428))
                        .border(
                            BorderStroke(1.dp, if (selectedTab == 1) Color(0xFF60A5FA) else Color(0xFF3A3A3E)),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            selectedTab = 1
                            if (cubiqReleases.isEmpty()) {
                                CubiqGitHubManager.fetchReleases()
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CUBIQ NEWS & UPDATES",
                            color = if (selectedTab == 1) Color.White else Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        if (CubiqGitHubManager.newUpdate.value != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF55FF55))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable News Content Area with Vertical Slider
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScrollWithBar(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    // Minecraft Official News List
                    if (isLandscape) {
                        // 2-Column Responsive Layout in Landscape
                        val chunked = displayNews.chunked(2)
                        for (rowItems in chunked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (item in rowItems) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        NewsArticleCard(
                                            news = item,
                                            isLandscape = true,
                                            onClick = { onOpenLink(item.readMoreUrl) }
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        // 1-Column Portrait Layout
                        displayNews.forEach { item ->
                            NewsArticleCard(
                                news = item,
                                isLandscape = false,
                                onClick = { onOpenLink(item.readMoreUrl) }
                            )
                        }
                    }
                } else {
                    // Cubiq GitHub News & Announcements
                    if (cubiqReleases.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isGitHubLoading) "Fetching announcements from GitHub..." else "No announcements found. Check back later!",
                                color = Color(0xFF888888),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        if (isLandscape) {
                            val chunkedReleases = cubiqReleases.chunked(2)
                            for (rowItems in chunkedReleases) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (release in rowItems) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            CubiqReleaseCard(
                                                release = release,
                                                onClick = { onOpenLink(release.downloadUrl ?: release.htmlUrl) }
                                            )
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            cubiqReleases.forEach { release ->
                                CubiqReleaseCard(
                                    release = release,
                                    onClick = { onOpenLink(release.downloadUrl ?: release.htmlUrl) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsArticleCard(
    news: OfficialNewsItem,
    isLandscape: Boolean,
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
        val aspect = if (isLandscape) 16f / 8.5f else 16f / 9f
        if (news.imageUrl.isNotBlank()) {
            AsyncImage(
                model = news.imageUrl,
                contentDescription = news.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.img_mc_news_cave),
                contentDescription = "News Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
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

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = news.title,
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Read More Button
            MinecraftButton(
                onClick = onClick,
                style = MinecraftButtonStyle.GREEN,
                text = "READ ON MINECRAFT.NET",
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            )
        }
    }
}

@Composable
private fun CubiqReleaseCard(
    release: CubiqReleaseItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(4.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tag / Version Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1E3A5F))
                    .border(BorderStroke(1.dp, Color(0xFF3B82F6)), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = release.tagName.uppercase(),
                    color = Color(0xFF93C5FD),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            if (release.publishedAt.isNotBlank()) {
                Text(
                    text = release.publishedAt,
                    color = Color(0xFF888888),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = release.title,
            color = Color.White,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Changelog preview
        Text(
            text = release.body.take(280) + if (release.body.length > 280) "..." else "",
            color = Color(0xFFCCCCCC),
            fontFamily = MinecraftFontFamily,
            fontSize = 10.5.sp,
            lineHeight = 15.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Button
        MinecraftButton(
            onClick = onClick,
            style = MinecraftButtonStyle.GREEN,
            text = "VIEW ON GITHUB / DOWNLOAD",
            fontSize = 10.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        )
    }
}
