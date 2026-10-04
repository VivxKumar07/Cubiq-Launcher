/*
 * Cubiq Launcher
 * Minecraft PC Launcher Initial Unpack & Dependencies Screen
 */

package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.MinecraftBlockCard
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.MinecraftPixelDonutSpinner
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    screenViewModel: SplashBackStackViewModel,
    onAgreeClick: () -> Unit = {}
) {
    BaseScreen(
        screenKey = NormalNavKey.UnpackDeps,
        currentKey = screenViewModel.splashScreen.currentKey
    ) { _ ->
        var installing by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141414))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Centered Large Minecraft Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_mc_java_logo),
                        contentDescription = "Minecraft",
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .height(64.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = "CUBIQ SETUP",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                // Info banner explaining runtime dependencies
                MinecraftBlockCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 10.dp),
                    backgroundColor = Color(0xFF1C1C1E),
                    borderColor = Color(0xFF383838)
                ) {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = if (installing) {
                            "Extracting Minecraft Java runtimes, LWJGL libraries, and native assets..."
                        } else {
                            "Cubiq requires unpacking internal Java runtimes (Java 8, 17, 21), LWJGL libraries, and native assets to run Java Edition."
                        },
                        color = Color(0xFFC0C0C0),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }

                // Component List in Portrait Mode
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        MinecraftTaskItem(item = item)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Minecraft Green Action Button
                MinecraftButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    style = MinecraftButtonStyle.GREEN,
                    enabled = !installing,
                    isLoading = installing,
                    text = if (installing) "EXTRACTING..." else "INSTALL & PLAY",
                    onClick = {
                        installing = true
                        onAgreeClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun MinecraftTaskItem(
    item: InstallableItem,
    modifier: Modifier = Modifier
) {
    val state by item.state.collectAsStateWithLifecycle()
    val message by item.task.taskMessage.collectAsStateWithLifecycle()

    val borderColor = when (state) {
        InstallableItem.State.FINISHED -> Color(0xFF3C8527)
        InstallableItem.State.RUNNING -> Color(0xFF2EB5D6)
        else -> Color(0xFF333333)
    }

    // Pick appropriate icon based on the library name:
    // Java runtimes -> Java Coffee Cup
    // authlib-injector -> Security Shield
    // caciocavallo -> Monitor GUI
    // jna / native -> Microchip
    // assets / other -> Grass block
    val itemNameLower = item.name.lowercase()
    val iconRes = when {
        itemNameLower.contains("internal") || itemNameLower.contains("java") || itemNameLower.contains("jre") -> R.drawable.ic_java
        itemNameLower.contains("authlib") -> R.drawable.ic_shield_auth
        itemNameLower.contains("cacio") -> R.drawable.ic_display_gui
        itemNameLower.contains("jna") || itemNameLower.contains("native") -> R.drawable.ic_chip_native
        else -> R.drawable.img_minecraft
    }

    MinecraftBlockCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1F1F21),
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = item.name,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                item.summary?.let {
                    Text(
                        text = it,
                        color = Color(0xFF9E9E9E),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                if (state == InstallableItem.State.RUNNING) {
                    message?.let { taskMsg ->
                        Text(
                            text = taskMsg,
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF55FF55),
                        trackColor = Color(0xFF1E381A)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (state) {
                InstallableItem.State.NOT_STARTED -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_zip_outlined),
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(20.dp)
                    )
                }
                InstallableItem.State.PENDING -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_update),
                        contentDescription = null,
                        tint = Color(0xFFFFAA00),
                        modifier = Modifier.size(20.dp)
                    )
                }
                InstallableItem.State.RUNNING -> {
                    MinecraftPixelDonutSpinner(
                        size = 20.dp,
                        baseColor = Color(0xFF55FF55)
                    )
                }
                InstallableItem.State.FINISHED -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color(0xFF55FF55),
                        modifier = Modifier.size(22.dp)
                    )
                }
                else -> {}
            }
        }
    }
}