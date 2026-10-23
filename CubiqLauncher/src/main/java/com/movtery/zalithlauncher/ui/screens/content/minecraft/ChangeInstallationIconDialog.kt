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
import com.movtery.zalithlauncher.ui.components.IsometricBlockIcons
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeInstallationIconDialog(
    versionName: String,
    onDismiss: () -> Unit,
    onIconChanged: () -> Unit = {}
) {
    val context = LocalContext.current

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
                text = "CHOOSE INSTALLATION ICON",
                color = Color(0xFF55FF55),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Select an isometric block icon for '$versionName':",
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF28282A))
                            .border(BorderStroke(1.dp, Color(0xFF3C3C3E)), RoundedCornerShape(3.dp))
                            .clickable {
                                MinecraftSoundHelper.playClickSound()
                                saveIconToVersion(context, versionName, icon.drawableRes)
                                VersionsManager.refresh("ChangeIcon")
                                Toast.makeText(context, "Icon updated to ${icon.name}", Toast.LENGTH_SHORT).show()
                                onIconChanged()
                                onDismiss()
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
                                contentDescription = icon.name,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = icon.name,
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
                text = "CANCEL",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            )
        }
    }
}

