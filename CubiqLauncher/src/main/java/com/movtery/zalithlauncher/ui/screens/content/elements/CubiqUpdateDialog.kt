/*
 * Cubiq Launcher
 * Update Available Notification Dialog
 * Prompts user on app launch when GitHub has a new release with changelogs, [ DOWNLOAD NOW ] and [ NOT NOW ].
 */

package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.game.versioninfo.CubiqReleaseItem
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CubiqUpdateDialog(
    release: CubiqReleaseItem,
    onDownloadNow: (String) -> Unit,
    onNotNow: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onNotNow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1E20))
                .border(BorderStroke(2.dp, Color(0xFF3C8527)), RoundedCornerShape(4.dp))
                .padding(18.dp)
        ) {
            // Header: Update Available
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "NEW VERSION AVAILABLE",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = release.title,
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Changelog container with slider
            Text(
                text = "CHANGELOG & RELEASE NOTES:",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF151517))
                    .border(BorderStroke(1.dp, Color(0xFF2E2E30)), RoundedCornerShape(3.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScrollWithBar(rememberScrollState())
                ) {
                    Text(
                        text = release.body.ifBlank { "New version released with performance optimizations and bug fixes." },
                        color = Color(0xFFE2E2E2),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons: [ DOWNLOAD NOW ] and [ NOT NOW ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinecraftButton(
                    onClick = {
                        onDownloadNow(release.downloadUrl ?: release.htmlUrl)
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = "DOWNLOAD NOW",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                )

                MinecraftButton(
                    onClick = onNotNow,
                    style = MinecraftButtonStyle.STONE,
                    text = "NOT NOW",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .width(100.dp)
                        .height(38.dp)
                )
            }
        }
    }
}
