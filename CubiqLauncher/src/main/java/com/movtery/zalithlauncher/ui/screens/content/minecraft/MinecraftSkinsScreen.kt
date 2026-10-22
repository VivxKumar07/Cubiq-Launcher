/*
 * Cubiq Launcher
 * Minecraft PC Launcher Skins Screen (Centered Full-Body Player & Skin Library)
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun MinecraftSkinsScreen(
    account: Account?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    var isSlimModel by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf("Current") }

    val presetCharacters = listOf(
        "Current" to (account?.username ?: "Steve"),
        "Steve" to "Steve",
        "Alex" to "Alex",
        "Ari" to "Ari",
        "Efe" to "Efe",
        "Makena" to "Makena",
        "Noor" to "Noor",
        "Sunny" to "Sunny",
        "Zuri" to "Zuri",
        "Kai" to "Kai"
    )

    val activeSkinFile = remember(account, refreshWardrobe) {
        account?.getSkinFile()?.takeIf { it.exists() }
    }

    val skinPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val currentAcc = AccountsManager.currentAccountFlow.value
                    if (currentAcc != null) {
                        val skinDir = PathManager.DIR_ACCOUNT_SKIN
                        if (!skinDir.exists()) skinDir.mkdirs()
                        val destFile = File(skinDir, "${currentAcc.uniqueUUID}.png")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        AccountsManager.refreshWardrobe()
                    }
                    withContext(Dispatchers.Main) {
                        selectedPreset = "Current"
                        Toast.makeText(context, "Custom skin applied successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to apply skin: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val displaySkinModel: Any = remember(selectedPreset, account, activeSkinFile, refreshWardrobe, isSlimModel) {
        when {
            selectedPreset != "Current" -> {
                if (selectedPreset == "Steve" && isSlimModel) "https://mc-heads.net/body/Alex/400.png"
                else if (selectedPreset == "Alex" && !isSlimModel) "https://mc-heads.net/body/Steve/400.png"
                else "https://mc-heads.net/body/$selectedPreset/400.png"
            }
            activeSkinFile != null && activeSkinFile.exists() -> activeSkinFile
            account != null && account.username.isNotBlank() -> "https://mc-heads.net/body/${account.username}/400.png"
            isSlimModel -> "https://mc-heads.net/body/Alex/400.png"
            else -> "https://mc-heads.net/body/Steve/400.png"
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val activeModelType = if (isSlimModel) com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType.ALEX else com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType.STEVE
    val previewSkinFile = if (selectedPreset == "Current") activeSkinFile else null
    val previewSkinUrl = when {
        selectedPreset == "Current" && activeSkinFile == null && account != null && account.username.isNotBlank() -> "https://minotar.net/skin/${account.username}"
        selectedPreset != "Current" && selectedPreset != "Steve" && selectedPreset != "Alex" -> "https://minotar.net/skin/$selectedPreset"
        selectedPreset == "Alex" -> "https://minotar.net/skin/MHF_Alex"
        selectedPreset == "Steve" -> "https://minotar.net/skin/MHF_Steve"
        else -> null
    }

    @Composable
    fun Stage3DPreview(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF24272B),
                            Color(0xFF181A1C),
                            Color(0xFF101112)
                        )
                    )
                )
                .border(BorderStroke(1.5.dp, Color(0xFF383838)), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            com.movtery.zalithlauncher.ui.components.SkinPreview3D(
                skinFile = previewSkinFile,
                skinUrl = previewSkinUrl,
                capeFile = null,
                modelType = activeModelType,
                modifier = Modifier.fillMaxSize(),
                interactionEnabled = true,
                refreshKey = remember(selectedPreset, isSlimModel, activeSkinFile, previewSkinUrl) { "$selectedPreset-$isSlimModel-${activeSkinFile?.path}-$previewSkinUrl" }
            )

            // Overlay Info Badge at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedPreset == "Current") (account?.username ?: "Player (Active)") else selectedPreset,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )

                Text(
                    text = if (isSlimModel) "Slim Model (3px Arms) • Drag 360°" else "Classic Model (4px Arms) • Drag 360°",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 9.5.sp
                )
            }
        }
    }

    @Composable
    fun WardrobeControls(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Player Model Selector (Classic vs Slim)
            Text(
                text = "PLAYER MODEL",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Classic
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (!isSlimModel) Color(0xFF3C8527) else Color(0xFF222224))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (!isSlimModel) Color(0xFF55FF55) else Color(0xFF383838)
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            isSlimModel = false
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CLASSIC (4px)",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }

                // Slim
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSlimModel) Color(0xFF3C8527) else Color(0xFF222224))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isSlimModel) Color(0xFF55FF55) else Color(0xFF383838)
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            isSlimModel = true
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SLIM (3px)",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Action Buttons: BROWSE SKIN FILE (.PNG)
            MinecraftButton(
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    skinPickerLauncher.launch("image/png")
                },
                style = MinecraftButtonStyle.GREEN,
                text = "+ BROWSE CUSTOM SKIN (.PNG)",
                fontSize = 11.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )

            // Preset Character Skins Library
            Text(
                text = "OFFICIAL CHARACTERS LIBRARY",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presetCharacters) { (label, username) ->
                    val isSelected = selectedPreset == label
                    val isCurrentSkin = label == "Current"
                    Box(
                        modifier = Modifier
                            .width(84.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) Color(0xFF2A3A2A)
                                else Color(0xFF1E1E20)
                            )
                            .border(
                                BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF55FF55)
                                    else Color(0xFF383838)
                                ),
                                RoundedCornerShape(3.dp)
                            )
                            .clickable {
                                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                selectedPreset = label
                                if (label != "Current") {
                                    Toast.makeText(context, "$label skin selected", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF28282A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrentSkin) {
                                    if (account != null) {
                                        PlayerFace(
                                            account = account,
                                            avatarSize = 34.dp,
                                            refreshKey = refreshWardrobe
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(R.drawable.img_minecraft),
                                            contentDescription = label,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = "https://mc-heads.net/avatar/$username/64.png",
                                        contentDescription = label,
                                        modifier = Modifier.size(36.dp),
                                        fallback = painterResource(R.drawable.img_minecraft),
                                        error = painterResource(R.drawable.img_minecraft)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isCurrentSkin) "CURRENT" else label,
                                color = if (isSelected) Color(0xFF55FF55) else Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF55FF55),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (isLandscape) {
            // Landscape 2-Column Layout: Left Stage (Interactive), Right Controls (Scrollable)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left 3D Stage (Fixed width, full height)
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "SKINS & WARDROBE",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Stage3DPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                // Right Scrollable Controls
                WardrobeControls(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScrollWithBar(scrollState)
                )
            }
        } else {
            // Portrait Layout: Single Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollWithBar(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "SKINS & WARDROBE",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Full body 3D preview & official Minecraft skin library",
                    color = Color(0xFF888888),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Stage3DPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                WardrobeControls(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
