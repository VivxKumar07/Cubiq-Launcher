/*
 * Cubiq Launcher
 * Minecraft PC Launcher Skins Screen (Centered Full-Body Player & Skin + Cape Wardrobe)
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.graphics.BitmapFactory
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.SkinPreview3D
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream

data class CapePreset(
    val id: String,
    val name: String,
    val assetPath: String?
)

@Composable
fun MinecraftSkinsScreen(
    account: Account?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    // 0: Skins, 1: Capes
    var selectedWardrobeTab by remember { mutableIntStateOf(0) }

    var isSlimModel by remember { mutableStateOf(account?.skinModelType == SkinModelType.ALEX) }
    var selectedSkinPreset by remember { mutableStateOf("Current") }
    var selectedCapeId by remember { mutableStateOf("Current") }

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

    val presetCapes = remember {
        listOf(
            CapePreset("None", "None", null),
            CapePreset("Migrator", "Migrator", "capes/migrator.png"),
            CapePreset("15th_anniversary", "15th Anniv.", "capes/15th_anniversary.png"),
            CapePreset("cherry", "Cherry", "capes/cherry.png"),
            CapePreset("minecon_2011", "Minecon 11", "capes/minecon_2011.png"),
            CapePreset("minecon_2012", "Minecon 12", "capes/minecon_2012.png"),
            CapePreset("minecon_2013", "Minecon 13", "capes/minecon_2013.png"),
            CapePreset("minecon_2015", "Minecon 15", "capes/minecon_2015.png"),
            CapePreset("minecon_2016", "Minecon 16", "capes/minecon_2016.png"),
            CapePreset("vanilla", "Vanilla", "capes/vanilla.png")
        )
    }

    val activeSkinFile = remember(account, refreshWardrobe) {
        account?.getSkinFile()?.takeIf { it.exists() }
    }

    val activeCapeFile = remember(account, refreshWardrobe) {
        account?.getCapeFile()?.takeIf { it.exists() }
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
                        currentAcc.skinModelType = if (isSlimModel) SkinModelType.ALEX else SkinModelType.STEVE
                        AccountsManager.saveAccount(currentAcc)
                        AccountsManager.refreshWardrobe()
                    }
                    withContext(Dispatchers.Main) {
                        selectedSkinPreset = "Current"
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

    val capePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val currentAcc = AccountsManager.currentAccountFlow.value
                    if (currentAcc != null) {
                        val capeDir = PathManager.DIR_ACCOUNT_CAPE
                        if (!capeDir.exists()) capeDir.mkdirs()
                        val destFile = currentAcc.getCapeFile()
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        AccountsManager.refreshWardrobe()
                    }
                    withContext(Dispatchers.Main) {
                        selectedCapeId = "Custom"
                        Toast.makeText(context, "Custom cape applied successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to apply cape: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun equipCape(preset: CapePreset) {
        scope.launch(Dispatchers.IO) {
            val currentAcc = AccountsManager.currentAccountFlow.value ?: return@launch
            val targetFile = currentAcc.getCapeFile()
            if (preset.id == "None") {
                FileUtils.deleteQuietly(targetFile)
                AccountsManager.refreshWardrobe()
                withContext(Dispatchers.Main) {
                    selectedCapeId = "None"
                    Toast.makeText(context, "Cape removed", Toast.LENGTH_SHORT).show()
                }
            } else if (preset.assetPath != null) {
                runCatching {
                    val capeDir = PathManager.DIR_ACCOUNT_CAPE
                    if (!capeDir.exists()) capeDir.mkdirs()
                    context.assets.open(preset.assetPath).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    AccountsManager.refreshWardrobe()
                    withContext(Dispatchers.Main) {
                        selectedCapeId = preset.id
                        Toast.makeText(context, "${preset.name} Cape equipped!", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure { e ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to equip cape: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val activeModelType = if (isSlimModel) SkinModelType.ALEX else SkinModelType.STEVE
    val previewSkinFile = if (selectedSkinPreset == "Current") activeSkinFile else null
    val previewSkinUrl = when {
        selectedSkinPreset == "Current" && activeSkinFile == null && account != null && account.username.isNotBlank() -> "https://minotar.net/skin/${account.username}"
        selectedSkinPreset != "Current" && selectedSkinPreset != "Steve" && selectedSkinPreset != "Alex" -> "https://minotar.net/skin/$selectedSkinPreset"
        selectedSkinPreset == "Alex" -> "https://minotar.net/skin/MHF_Alex"
        selectedSkinPreset == "Steve" -> "https://minotar.net/skin/MHF_Steve"
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
            SkinPreview3D(
                skinFile = previewSkinFile,
                skinUrl = previewSkinUrl,
                capeFile = activeCapeFile,
                modelType = activeModelType,
                azimuth = if (selectedWardrobeTab == 1) 145 else -35,
                pitch = 10,
                modifier = Modifier.fillMaxSize(),
                interactionEnabled = true,
                refreshKey = remember(selectedSkinPreset, isSlimModel, activeSkinFile, activeCapeFile, previewSkinUrl, selectedCapeId, refreshWardrobe) {
                    "$selectedSkinPreset-$isSlimModel-${activeSkinFile?.path}-${activeCapeFile?.path}-$previewSkinUrl-$selectedCapeId-$refreshWardrobe"
                }
            )

            // Overlay Info Badge at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedWardrobeTab == 0) {
                        if (selectedSkinPreset == "Current") (account?.username ?: "Player (Active)") else selectedSkinPreset
                    } else {
                        if (activeCapeFile != null) "Active Cape Equipped" else "No Cape Equipped"
                    },
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )

                Text(
                    text = if (selectedWardrobeTab == 0) {
                        if (isSlimModel) "Slim Model (3px Arms) • Drag 360°" else "Classic Model (4px Arms) • Drag 360°"
                    } else {
                        "Waving Cape Physics • Drag 360°"
                    },
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
            // Sub-Tabs: [ SKINS ] | [ CAPES ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Skins Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selectedWardrobeTab == 0) Color(0xFF2A5520) else Color(0xFF222224))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (selectedWardrobeTab == 0) Color(0xFF55FF55) else Color(0xFF383838)
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            selectedWardrobeTab = 0
                        }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SKINS",
                        color = if (selectedWardrobeTab == 0) Color(0xFF55FF55) else Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Capes Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selectedWardrobeTab == 1) Color(0xFF2A5520) else Color(0xFF222224))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (selectedWardrobeTab == 1) Color(0xFF55FF55) else Color(0xFF383838)
                            ),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                            selectedWardrobeTab = 1
                        }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CAPES",
                        color = if (selectedWardrobeTab == 1) Color(0xFF55FF55) else Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (selectedWardrobeTab == 0) {
                // ==================== SKINS SUB-TAB ====================
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
                    // Classic (4px)
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
                                account?.let {
                                    it.skinModelType = SkinModelType.STEVE
                                    AccountsManager.saveAccount(it)
                                }
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

                    // Slim (3px)
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
                                account?.let {
                                    it.skinModelType = SkinModelType.ALEX
                                    AccountsManager.saveAccount(it)
                                }
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

                // Action: BROWSE CUSTOM SKIN (.PNG)
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

                // Official Character Library
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
                        val isSelected = selectedSkinPreset == label
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
                                    selectedSkinPreset = label
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
            } else {
                // ==================== CAPES SUB-TAB ====================
                Text(
                    text = "CUSTOM CAPES",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                // Action: BROWSE CUSTOM CAPE (.PNG)
                MinecraftButton(
                    onClick = {
                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                        capePickerLauncher.launch("image/png")
                    },
                    style = MinecraftButtonStyle.GREEN,
                    text = "+ BROWSE CUSTOM CAPE (.PNG)",
                    fontSize = 11.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )

                // Official Cape Library
                Text(
                    text = "OFFICIAL CAPES LIBRARY",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetCapes) { preset ->
                        val isSelected = (selectedCapeId == preset.id) || (selectedCapeId == "Current" && preset.id == "None" && activeCapeFile == null)
                        val capeBitmap = remember(preset.assetPath) {
                            preset.assetPath?.let { path ->
                                runCatching {
                                    context.assets.open(path).use { stream ->
                                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                    }
                                }.getOrNull()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(88.dp)
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
                                    equipCape(preset)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 48.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF28282A))
                                        .border(BorderStroke(1.dp, Color(0xFF3C3C3E)), RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (preset.id == "None") {
                                        Text(
                                            text = "NONE",
                                            color = Color(0xFFAAAAAA),
                                            fontFamily = MinecraftFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    } else if (capeBitmap != null) {
                                        Image(
                                            bitmap = capeBitmap,
                                            contentDescription = preset.name,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = preset.name,
                                    color = if (isSelected) Color(0xFF55FF55) else Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
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
                    text = "Full body 3D preview & official Minecraft skins & capes",
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
