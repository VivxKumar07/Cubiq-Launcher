/*
 * Cubiq Launcher
 * Minecraft PC Launcher Profile & Game Settings Screen (Integrated Real Zalith Settings & Accounts)
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SettingsTab(val title: String) {
    GAME("GAME"),
    RENDERER("VIDEO / RENDERER"),
    CONTROLS("CONTROLS"),
    LAUNCHER("LAUNCHER")
}

@Composable
fun MinecraftProfileScreen(
    account: Account?,
    backStackViewModel: ScreenBackStackViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accountsList by AccountsManager.accountsFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    var showAddOfflineDialog by remember { mutableStateOf(false) }
    var offlineUsername by remember { mutableStateOf("") }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    var isLoggingInMicrosoft by remember { mutableStateOf(false) }
    var msDeviceCodeText by remember { mutableStateOf<String?>(null) }
    var msVerificationUrl by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(SettingsTab.GAME) }

    // REAL Zalith Game Settings
    val maxRam = remember { getMaxMemoryForSettings(context).toFloat().coerceAtLeast(1024f) }
    var ramValue by remember {
        mutableFloatStateOf((AllSettings.ramAllocation.state ?: 2048).toFloat().coerceIn(256f, maxRam))
    }
    var selectedJava by remember {
        mutableStateOf(AllSettings.javaRuntime.state.ifBlank { "Auto" })
    }
    var autoPickJava by remember {
        mutableStateOf(AllSettings.autoPickJavaRuntime.state)
    }
    var versionIsolation by remember {
        mutableStateOf(AllSettings.versionIsolation.state)
    }
    var skipGameIntegrity by remember {
        mutableStateOf(AllSettings.skipGameIntegrityCheck.state)
    }
    var jvmArgs by remember {
        mutableStateOf(AllSettings.jvmArgs.state)
    }

    // REAL Zalith Renderer Settings
    val allRenderers = remember { Renderers.getRenderers() }
    var selectedRendererId by remember {
        mutableStateOf(AllSettings.renderer.state.ifBlank { allRenderers.firstOrNull()?.getUniqueIdentifier() ?: "" })
    }
    var selectedVulkanDriver by remember {
        mutableStateOf(AllSettings.vulkanDriver.state)
    }
    var gameFullScreen by remember {
        mutableStateOf(AllSettings.gameFullScreen.state)
    }
    var sustainedPerf by remember {
        mutableStateOf(AllSettings.sustainedPerformance.state)
    }
    var resolutionRatio by remember {
        mutableFloatStateOf(AllSettings.resolutionRatio.state.toFloat())
    }

    // REAL Zalith Controls Settings
    var mouseMode by remember {
        mutableStateOf(AllSettings.mouseControlMode.state)
    }
    var cursorSensitivity by remember {
        mutableFloatStateOf(AllSettings.cursorSensitivity.state.toFloat())
    }
    var enableMouseClick by remember {
        mutableStateOf(AllSettings.enableMouseClick.state)
    }
    var physicalMouseMode by remember {
        mutableStateOf(AllSettings.physicalMouseMode.state)
    }
    var gamepadControl by remember {
        mutableStateOf(AllSettings.gamepadControl.state)
    }
    var gyroscopeControl by remember {
        mutableStateOf(AllSettings.gyroscopeControl.state)
    }
    var gyroscopeSensitivity by remember {
        mutableFloatStateOf(AllSettings.gyroscopeSensitivity.state.toFloat())
    }

    // Additional Game Settings
    var versionCustomInfo by remember {
        mutableStateOf(AllSettings.versionCustomInfo.state)
    }
    var showLogAuto by remember {
        mutableStateOf(AllSettings.showLogAutomatic.state)
    }
    var logTextSize by remember {
        mutableFloatStateOf(AllSettings.logTextSize.state.toFloat())
    }

    // Additional Renderer Settings
    var vsyncInZink by remember {
        mutableStateOf(AllSettings.vsyncInZink.state)
    }
    var useSurfaceView by remember {
        mutableStateOf(AllSettings.useSurfaceView.state)
    }

    // Additional Launcher Settings
    var backgroundBlurVal by remember {
        mutableFloatStateOf(AllSettings.backgroundBlur.state.toFloat())
    }
    var animateSpeedVal by remember {
        mutableFloatStateOf(AllSettings.launcherAnimateSpeed.state.toFloat())
    }

    // REAL Zalith Launcher Settings
    var isFullScreen by remember {
        mutableStateOf(AllSettings.launcherFullScreen.state)
    }
    var seasonalEffects by remember {
        mutableStateOf(AllSettings.launcherFestivalEffects.state)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E20))
                    .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(4.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar Box with requested Blue Rounded Square Border
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(BorderStroke(2.5.dp, Color(0xFF1E88E5)), RoundedCornerShape(6.dp))
                            .background(Color(0xFF222222)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (account != null) {
                            PlayerFace(account = account, avatarSize = 60.dp, refreshKey = refreshWardrobe)
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_mc_pc_profile),
                                contentDescription = "Steve Face",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = account?.username ?: "Steve",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = account?.let { getAccountTypeName(it) } ?: "No Active Account",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            onClick = {
                                if (isLoggingInMicrosoft) return@MinecraftButton
                                isLoggingInMicrosoft = true
                                microsoftLogin(
                                    context = context,
                                    toWeb = { url ->
                                        msVerificationUrl = url
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    },
                                    backToMain = {
                                        isLoggingInMicrosoft = false
                                        msDeviceCodeText = null
                                    },
                                    checkIfInWebScreen = { true },
                                    updateOperation = {},
                                    showToast = { text, _ ->
                                        val str = text.getString(context)
                                        if (str.contains("code", ignoreCase = true) || str.contains("代码", ignoreCase = true)) {
                                            // Extract code if possible
                                            val parts = str.split(" ")
                                            val foundCode = parts.lastOrNull { it.length in 6..10 }
                                            if (foundCode != null) {
                                                msDeviceCodeText = foundCode
                                            }
                                        }
                                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                                    },
                                    submitError = { err ->
                                        isLoggingInMicrosoft = false
                                        msDeviceCodeText = null
                                        Toast.makeText(context, "Microsoft Login: ${err.message}", Toast.LENGTH_LONG).show()
                                    },
                                    onSuccess = {
                                        isLoggingInMicrosoft = false
                                        msDeviceCodeText = null
                                        Toast.makeText(context, "Successfully logged into Microsoft Account!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = if (isLoggingInMicrosoft) "LOGGING IN..." else "MICROSOFT LOGIN",
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        )

                        MinecraftButton(
                            onClick = { showAddOfflineDialog = true },
                            style = MinecraftButtonStyle.STONE,
                            text = "+ OFFLINE",
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .weight(0.7f)
                                .height(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Account Switcher Section
            Text(
                text = "SAVED ACCOUNTS (${accountsList.size})",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (accountsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E1E20))
                        .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved accounts yet. Click '+ ADD NEW ACCOUNT' above.",
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E1E20))
                        .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    accountsList.forEach { acc ->
                        val isSelected = account?.uniqueUUID == acc.uniqueUUID
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) Color(0xFF28282B) else Color.Transparent)
                                .clickable {
                                    AccountsManager.setCurrentAccount(acc)
                                    Toast.makeText(context, "Switched to ${acc.username}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    AccountsManager.setCurrentAccount(acc)
                                    Toast.makeText(context, "Switched to ${acc.username}", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF55FF55),
                                    unselectedColor = Color(0xFF666666)
                                ),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            PlayerFace(account = acc, avatarSize = 28.dp, refreshKey = refreshWardrobe)

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.username,
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = getAccountTypeName(acc),
                                    color = Color(0xFF888888),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.5.sp
                                )
                            }

                            // Delete account button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .clickable { accountToDelete = acc },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete_outlined),
                                    contentDescription = "Delete Account",
                                    tint = Color(0xFFFF5555),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // REAL ZALITH LAUNCHER SETTINGS TABS IN MINECRAFT STYLE
            Text(
                text = "CUBIQ LAUNCHER SETTINGS",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E1E20))
                    .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingsTab.entries.forEach { tab ->
                    val isTabSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isTabSelected) Color(0xFF3C8527) else Color(0xFF262628))
                            .border(
                                BorderStroke(1.dp, if (isTabSelected) Color(0xFF55FF55) else Color(0xFF383838)),
                                RoundedCornerShape(2.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Content based on selectedTab
            when (selectedTab) {
                SettingsTab.GAME -> {
                    // GAME SETTINGS SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E1E20))
                            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. RAM Allocation (Real Zalith Setting)
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Memory Allocation",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = "${ramValue.toInt()} MB (${String.format("%.1f", ramValue / 1024f)} GB)",
                                    color = Color(0xFF55FF55),
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = ramValue,
                                onValueChange = { ramValue = it },
                                onValueChangeFinished = {
                                    AllSettings.ramAllocation.save(ramValue.toInt())
                                    Toast.makeText(context, "Memory set to ${ramValue.toInt()} MB", Toast.LENGTH_SHORT).show()
                                },
                                valueRange = 512f..maxRam,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("512 MB", color = Color(0xFF666666), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)
                                Text("Max: ${maxRam.toInt()} MB", color = Color(0xFF666666), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)
                            }
                        }

                        // 2. Auto Pick Java Runtime
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Pick Java Runtime",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Automatically selects best Java 8/17/21 for the version",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = autoPickJava,
                                onCheckedChange = {
                                    autoPickJava = it
                                    AllSettings.autoPickJavaRuntime.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 3. Real Java Runtimes Selection (from RuntimesManager)
                        val runtimes = remember { RuntimesManager.getRuntimes().filter { it.isCompatible() } }
                        Column {
                            Text(
                                text = "Manual Java Runtime",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (runtimes.isEmpty()) {
                                Text(
                                    text = "Using internal bundled runtimes",
                                    color = Color(0xFF888888),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.5.sp
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    runtimes.forEach { rt ->
                                        val isSel = selectedJava == rt.name
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (isSel) Color(0xFF3C8527) else Color(0xFF262628))
                                                .border(
                                                    BorderStroke(1.dp, if (isSel) Color(0xFF55FF55) else Color(0xFF383838)),
                                                    RoundedCornerShape(2.dp)
                                                )
                                                .clickable {
                                                    selectedJava = rt.name
                                                    AllSettings.javaRuntime.save(rt.name)
                                                    Toast.makeText(context, "Runtime: ${rt.name}", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = rt.name,
                                                color = Color.White,
                                                fontFamily = MinecraftFontFamily,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Version Isolation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Version Isolation",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Isolate mods and configs per Minecraft version",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = versionIsolation,
                                onCheckedChange = {
                                    versionIsolation = it
                                    AllSettings.versionIsolation.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 5. Skip Game Integrity Check
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Skip Integrity Check",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Faster game startup by skipping sha1 verification",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = skipGameIntegrity,
                                onCheckedChange = {
                                    skipGameIntegrity = it
                                    AllSettings.skipGameIntegrityCheck.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 6. Custom JVM Arguments
                        Column {
                            Text(
                                text = "Custom JVM Arguments",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = jvmArgs,
                                onValueChange = {
                                    jvmArgs = it
                                    AllSettings.jvmArgs.save(it)
                                },
                                placeholder = {
                                    Text("-XX:+UseG1GC -Dminecraft.applet.TargetDirectory=...", color = Color(0xFF666666), fontSize = 11.sp)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF242426),
                                    unfocusedContainerColor = Color(0xFF242426),
                                    focusedBorderColor = Color(0xFF55FF55),
                                    unfocusedBorderColor = Color(0xFF383838),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // 7. Version Subtitle / Custom Info
                        Column {
                            Text(
                                text = "Version Custom Subtitle",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = versionCustomInfo,
                                onValueChange = {
                                    versionCustomInfo = it
                                    AllSettings.versionCustomInfo.save(it)
                                },
                                placeholder = {
                                    Text("Cubiq[zl_version]", color = Color(0xFF666666), fontSize = 11.sp)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF242426),
                                    unfocusedContainerColor = Color(0xFF242426),
                                    focusedBorderColor = Color(0xFF55FF55),
                                    unfocusedBorderColor = Color(0xFF383838),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // 8. Show Log Automatically
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Log On Launch",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Display game console log automatically while launching",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = showLogAuto,
                                onCheckedChange = {
                                    showLogAuto = it
                                    AllSettings.showLogAutomatic.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 9. Log Text Size
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Console Log Text Size", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                Text("${logTextSize.toInt()} sp", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                            }
                            Slider(
                                value = logTextSize,
                                onValueChange = { logTextSize = it },
                                onValueChangeFinished = {
                                    AllSettings.logTextSize.save(logTextSize.toInt())
                                },
                                valueRange = 8f..20f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }

                SettingsTab.RENDERER -> {
                    // RENDERER & VIDEO SETTINGS SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E1E20))
                            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Renderer Picker
                        Column {
                            Text(
                                text = "Renderer Selection",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            allRenderers.take(4).forEach { rend ->
                                val rendId = rend.getUniqueIdentifier()
                                val rendName = rend.getRendererName()
                                val isSel = selectedRendererId == rendId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSel) Color(0xFF263820) else Color(0xFF242426))
                                        .border(
                                            BorderStroke(1.dp, if (isSel) Color(0xFF55FF55) else Color(0xFF383838)),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable {
                                            selectedRendererId = rendId
                                            AllSettings.renderer.save(rendId)
                                            Toast.makeText(context, "Renderer: $rendName", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rendName,
                                        color = if (isSel) Color(0xFF55FF55) else Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp
                                    )
                                    if (isSel) {
                                        Text("ACTIVE", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 9.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        // 2. Game Fullscreen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Game Fullscreen Mode",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Immersive fullscreen when Minecraft starts",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = gameFullScreen,
                                onCheckedChange = {
                                    gameFullScreen = it
                                    AllSettings.gameFullScreen.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 3. Resolution Scaling Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Resolution Scaling", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${resolutionRatio.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                            }
                            Slider(
                                value = resolutionRatio,
                                onValueChange = { resolutionRatio = it },
                                onValueChangeFinished = {
                                    AllSettings.resolutionRatio.save(resolutionRatio.toInt())
                                },
                                valueRange = 50f..150f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 4. Sustained Performance Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sustained Performance",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Maintains stable clock speeds on supported devices",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = sustainedPerf,
                                onCheckedChange = {
                                    sustainedPerf = it
                                    AllSettings.sustainedPerformance.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 5. Zink VSync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Zink VSync",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Enable vertical synchronization in Zink / Vulkan",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = vsyncInZink,
                                onCheckedChange = {
                                    vsyncInZink = it
                                    AllSettings.vsyncInZink.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 6. SurfaceView Rendering
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use SurfaceView",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Direct hardware surface rendering for lower latency",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = useSurfaceView,
                                onCheckedChange = {
                                    useSurfaceView = it
                                    AllSettings.useSurfaceView.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // 7. Vulkan Driver Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vulkan Driver",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = selectedVulkanDriver.ifBlank { "System default Turnip / Adreno" },
                                    color = Color(0xFF55FF55),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                SettingsTab.CONTROLS -> {
                    // CONTROLS SETTINGS SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E1E20))
                            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Virtual Mouse Control Mode",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(MouseControlMode.SLIDE to "Slide Mode", MouseControlMode.CLICK to "Click Mode")
                            modes.forEach { (mode, label) ->
                                val isSel = mouseMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSel) Color(0xFF3C8527) else Color(0xFF262628))
                                        .border(
                                            BorderStroke(1.dp, if (isSel) Color(0xFF55FF55) else Color(0xFF383838)),
                                            RoundedCornerShape(2.dp)
                                        )
                                        .clickable {
                                            mouseMode = mode
                                            AllSettings.mouseControlMode.save(mode)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Cursor Sensitivity
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Virtual Mouse Sensitivity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                Text("${cursorSensitivity.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                            }
                            Slider(
                                value = cursorSensitivity,
                                onValueChange = { cursorSensitivity = it },
                                onValueChangeFinished = {
                                    AllSettings.cursorSensitivity.save(cursorSensitivity.toInt())
                                },
                                valueRange = 25f..300f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Enable Mouse Click
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Virtual Mouse Left-Click On Tap",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Tap anywhere to simulate left mouse button click",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = enableMouseClick,
                                onCheckedChange = {
                                    enableMouseClick = it
                                    AllSettings.enableMouseClick.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Physical Mouse Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Physical Mouse Capture",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Capture external USB / Bluetooth mouse directly in game",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = physicalMouseMode,
                                onCheckedChange = {
                                    physicalMouseMode = it
                                    AllSettings.physicalMouseMode.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Gamepad Controller Support
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Physical Gamepad Support",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Enable external Xbox / PlayStation controller input",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = gamepadControl,
                                onCheckedChange = {
                                    gamepadControl = it
                                    AllSettings.gamepadControl.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Gyroscope Aiming Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gyroscope Aiming",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Use device motion sensor for camera look and aiming",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = gyroscopeControl,
                                onCheckedChange = {
                                    gyroscopeControl = it
                                    AllSettings.gyroscopeControl.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        if (gyroscopeControl) {
                            // Gyroscope Sensitivity Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gyroscope Sensitivity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                    Text("${gyroscopeSensitivity.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                }
                                Slider(
                                    value = gyroscopeSensitivity,
                                    onValueChange = { gyroscopeSensitivity = it },
                                    onValueChangeFinished = {
                                        AllSettings.gyroscopeSensitivity.save(gyroscopeSensitivity.toInt())
                                    },
                                    valueRange = 25f..300f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF55FF55),
                                        activeTrackColor = Color(0xFF3C8527),
                                        inactiveTrackColor = Color(0xFF333333)
                                    )
                                )
                            }
                        }
                    }
                }

                SettingsTab.LAUNCHER -> {
                    // LAUNCHER SETTINGS SECTION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E1E20))
                            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Launcher Fullscreen
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Launcher Fullscreen",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = "Hide Android system status & navigation bars",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isFullScreen,
                                onCheckedChange = {
                                    isFullScreen = it
                                    AllSettings.launcherFullScreen.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Launcher Seasonal Effects
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seasonal Theme Effects",
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Text(
                                    text = "Enable festive themes on special dates",
                                    color = Color(0xFF777777),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = seasonalEffects,
                                onCheckedChange = {
                                    seasonalEffects = it
                                    AllSettings.launcherFestivalEffects.save(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3C8527),
                                    uncheckedThumbColor = Color(0xFF888888),
                                    uncheckedTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Background Blur Effect
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Background Blur Effect", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                Text("${backgroundBlurVal.toInt()} dp", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                            }
                            Slider(
                                value = backgroundBlurVal,
                                onValueChange = { backgroundBlurVal = it },
                                onValueChangeFinished = {
                                    AllSettings.backgroundBlur.save(backgroundBlurVal.toInt())
                                },
                                valueRange = 0f..40f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }

                        // Animation Speed
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Animation Speed Multiplier", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                                Text("${animateSpeedVal.toInt()}x", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                            }
                            Slider(
                                value = animateSpeedVal,
                                onValueChange = { animateSpeedVal = it },
                                onValueChangeFinished = {
                                    AllSettings.launcherAnimateSpeed.save(animateSpeedVal.toInt())
                                },
                                valueRange = 0f..10f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF55FF55),
                                    activeTrackColor = Color(0xFF3C8527),
                                    inactiveTrackColor = Color(0xFF333333)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Microsoft Login Status Dialog
        if (isLoggingInMicrosoft) {
            AlertDialog(
                onDismissRequest = {
                    isLoggingInMicrosoft = false
                    msDeviceCodeText = null
                },
                containerColor = Color(0xFF1C1C1E),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCCCCCC),
                title = {
                    Text(
                        text = "MICROSOFT LOGIN",
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF55FF55),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = if (msDeviceCodeText != null) {
                                "Code copied: $msDeviceCodeText\nBrowser opened to enter code!"
                            } else {
                                "Connecting to Microsoft authentication service..."
                            },
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (msVerificationUrl != null) {
                            MinecraftButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(msVerificationUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                },
                                style = MinecraftButtonStyle.STONE,
                                text = "RE-OPEN BROWSER",
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(34.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            isLoggingInMicrosoft = false
                            msDeviceCodeText = null
                        },
                        style = MinecraftButtonStyle.STONE,
                        text = "DISMISS",
                        fontSize = 11.5.sp,
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                    )
                }
            )
        }

        // Add Offline Account Dialog
        if (showAddOfflineDialog) {
            AlertDialog(
                onDismissRequest = { showAddOfflineDialog = false },
                containerColor = Color(0xFF1C1C1E),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFCCCCCC),
                title = {
                    Text(
                        text = "ADD ACCOUNT",
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter a username for local offline play:",
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFAAAAAA)
                        )

                        OutlinedTextField(
                            value = offlineUsername,
                            onValueChange = { offlineUsername = it },
                            placeholder = {
                                Text(
                                    text = "e.g. Steve, Alex",
                                    fontFamily = MinecraftFontFamily,
                                    color = Color(0xFF666666)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2A2A2E),
                                unfocusedContainerColor = Color(0xFF2A2A2E),
                                focusedBorderColor = Color(0xFF55FF55),
                                unfocusedBorderColor = Color(0xFF444444),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            val name = offlineUsername.trim()
                            if (name.isNotEmpty()) {
                                scope.launch(Dispatchers.IO) {
                                    localLogin(userName = name, userUUID = null)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Account $name created and selected!", Toast.LENGTH_SHORT).show()
                                        offlineUsername = ""
                                        showAddOfflineDialog = false
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = "CREATE",
                        fontSize = 11.5.sp,
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                    )
                },
                dismissButton = {
                    MinecraftButton(
                        onClick = { showAddOfflineDialog = false },
                        style = MinecraftButtonStyle.STONE,
                        text = "CANCEL",
                        fontSize = 11.5.sp,
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                    )
                }
            )
        }

        // Delete Account Confirmation Dialog
        accountToDelete?.let { acc ->
            AlertDialog(
                onDismissRequest = { accountToDelete = null },
                containerColor = Color(0xFF1C1C1E),
                title = {
                    Text(
                        text = "DELETE ACCOUNT?",
                        color = Color(0xFFFF5555),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove account '${acc.username}'?",
                        color = Color(0xFFCCCCCC),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                AccountsManager.deleteAccount(acc)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                    accountToDelete = null
                                }
                            }
                        },
                        style = MinecraftButtonStyle.RED,
                        text = "DELETE",
                        fontSize = 11.5.sp,
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                    )
                },
                dismissButton = {
                    MinecraftButton(
                        onClick = { accountToDelete = null },
                        style = MinecraftButtonStyle.STONE,
                        text = "CANCEL",
                        fontSize = 11.5.sp,
                        modifier = Modifier
                            .width(84.dp)
                            .height(34.dp)
                    )
                }
            )
        }
    }
}
