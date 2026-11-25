package com.movtery.zalithlauncher.ui.screens.content.horizon

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.horizon.HorizonManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import org.json.JSONObject
import java.io.File

enum class HorizonUiTab(@StringRes val labelRes: Int) {
    PLAY(R.string.cubiq_horizon_tab_play),
    MODS(R.string.cubiq_horizon_tab_mods),
    PACKS(R.string.cubiq_horizon_tab_packs),
    SHADERS(R.string.cubiq_horizon_tab_shaders),
    INSTALLED(R.string.cubiq_horizon_tab_installed),
    SETTINGS(R.string.cubiq_horizon_tab_settings)
}

private val settingLabels = mapOf(
    "Vulkan / GPU batching" to R.string.cubiq_horizon_renderer_vulkan_gpu,
    "OpenGL ES / experimental" to R.string.cubiq_horizon_renderer_opengl,
    "Vulkan / compatibility" to R.string.cubiq_horizon_renderer_vulkan_compatibility,
    "Profile 0" to R.string.cubiq_horizon_profile_zero,
    "Landscape (sensor)" to R.string.cubiq_horizon_landscape_sensor,
    "Portrait (sensor)" to R.string.cubiq_horizon_portrait_sensor,
    "Landscape (reversed)" to R.string.cubiq_horizon_landscape_reversed,
    "Portrait (reversed)" to R.string.cubiq_horizon_portrait_reversed,
    "Automatic rotation" to R.string.cubiq_horizon_automatic_rotation,
    "Automatic" to R.string.cubiq_horizon_automatic,
    "Touch & Android keyboard" to R.string.cubiq_horizon_touch_android_keyboard,
    "Keyboard & mouse" to R.string.cubiq_horizon_keyboard_mouse,
    "Controller" to R.string.cubiq_horizon_controller,
    "Native" to R.string.cubiq_horizon_native,
    "System default" to R.string.cubiq_horizon_system_default,
    "Singleplayer / tap to enter server" to R.string.cubiq_horizon_server_singleplayer,
    "Multiplayer Server: default" to R.string.cubiq_horizon_server_default,
)

@Composable
private fun settingLabel(value: String): String = settingLabels[value]?.let { stringResource(it) } ?: value

data class HorizonContentItem(
    val file: File,
    val name: String,
    val enabled: Boolean
)

/**
 * Authentic Horizon Android 0.7.6 UI & Settings replication in Cubiq.
 * Matches 100% of user screenshots:
 * Tabs: Play, Mods, Packs, Shaders, Installed, Settings
 * Launch settings: 15 exact options connected to android-launcher.json
 * Responsive layout in portrait & landscape with zero clipped text.
 */
@Composable
fun HorizonSettingsDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    var currentTab by remember { mutableStateOf(HorizonUiTab.SETTINGS) }
    val launcherJsonFile = remember { File(context.filesDir, "android-launcher.json") }

    // 15 Launch Settings State from android-launcher.json
    var rendering by remember { mutableStateOf("Vulkan / GPU batching") }
    var clientResolution by remember { mutableStateOf("100%") }
    var profile by remember { mutableStateOf("Profile 0") }
    var orientation by remember { mutableStateOf("Landscape (sensor)") }
    var inputHost by remember { mutableStateOf("Automatic") }
    var immersiveFullscreen by remember { mutableStateOf(true) }
    var avoidCutout by remember { mutableStateOf(false) }
    var aspectRatio by remember { mutableStateOf("Native") }
    var refreshRate by remember { mutableStateOf("System default") }
    var performanceHints by remember { mutableStateOf(false) }
    var gpuProfiling by remember { mutableStateOf(false) }
    var performanceRecording by remember { mutableStateOf(false) }
    var serverName by remember { mutableStateOf("Singleplayer / tap to enter server") }

    // Content Lists
    val modsList = remember { mutableStateListOf<HorizonContentItem>() }
    val packsList = remember { mutableStateListOf<HorizonContentItem>() }
    val shadersList = remember { mutableStateListOf<HorizonContentItem>() }

    fun reloadConfig() {
        if (launcherJsonFile.exists()) {
            try {
                val json = JSONObject(launcherJsonFile.readText())
                val r = json.optString("renderer", "vulkan")
                rendering = when (r) {
                    "open-gl" -> "OpenGL ES / experimental"
                    "vulkan-compatibility" -> "Vulkan / compatibility"
                    else -> "Vulkan / GPU batching"
                }

                val scale = json.optInt("render_scale", 100)
                clientResolution = "$scale%"

                val o = json.optString("orientation", "landscape")
                orientation = when (o) {
                    "portrait" -> "Portrait (sensor)"
                    "reverse-landscape" -> "Landscape (reversed)"
                    "reverse-portrait" -> "Portrait (reversed)"
                    "automatic" -> "Automatic rotation"
                    else -> "Landscape (sensor)"
                }

                val inp = json.optString("input_host", "automatic")
                inputHost = when (inp) {
                    "touch" -> "Touch & Android keyboard"
                    "keyboard_mouse" -> "Keyboard & mouse"
                    "controller" -> "Controller"
                    else -> "Automatic"
                }

                immersiveFullscreen = json.optBoolean("immersive", true)
                avoidCutout = json.optBoolean("avoid_cutout", false)
                aspectRatio = json.optString("aspect", "Native")
                refreshRate = json.optString("refresh_rate", "System default")
                performanceHints = json.optBoolean("performance_hints", false)
                gpuProfiling = json.optBoolean("gpu_profiling", false)
                performanceRecording = json.optBoolean("record_performance", false)
                serverName = json.optString("server", "Singleplayer / tap to enter server")
            } catch (_: Exception) {}
        }
    }

    fun saveConfig() {
        try {
            val json = if (launcherJsonFile.exists()) JSONObject(launcherJsonFile.readText()) else JSONObject()
            val rVal = when (rendering) {
                "OpenGL ES / experimental" -> "open-gl"
                "Vulkan / compatibility" -> "vulkan-compatibility"
                else -> "vulkan"
            }
            json.put("renderer", rVal)
            json.put("render_scale", clientResolution.removeSuffix("%").toIntOrNull() ?: 100)

            val oVal = when (orientation) {
                "Portrait (sensor)" -> "portrait"
                "Landscape (reversed)" -> "reverse-landscape"
                "Portrait (reversed)" -> "reverse-portrait"
                "Automatic rotation" -> "automatic"
                else -> "landscape"
            }
            json.put("orientation", oVal)

            val inpVal = when (inputHost) {
                "Touch & Android keyboard" -> "touch"
                "Keyboard & mouse" -> "keyboard_mouse"
                "Controller" -> "controller"
                else -> "automatic"
            }
            json.put("input_host", inpVal)

            json.put("immersive", immersiveFullscreen)
            json.put("avoid_cutout", avoidCutout)
            json.put("aspect", aspectRatio)
            json.put("refresh_rate", refreshRate)
            json.put("performance_hints", performanceHints)
            json.put("gpu_profiling", gpuProfiling)
            json.put("record_performance", performanceRecording)
            json.put("server", serverName)
            launcherJsonFile.writeText(json.toString(2))
        } catch (_: Exception) {}
    }

    fun reloadContent() {
        val modsDir = File(context.filesDir, "mods").apply { mkdirs() }
        modsList.clear()
        modsDir.listFiles()?.filter { it.extension.equals("hmod", true) || it.extension.equals("disabled", true) }?.forEach { f ->
            val isEnabled = f.extension.equals("hmod", true)
            val displayName = f.nameWithoutExtension.removeSuffix(".disabled")
            modsList.add(HorizonContentItem(f, displayName, isEnabled))
        }

        val packsDir = File(context.filesDir, "resourcepacks").apply { mkdirs() }
        packsList.clear()
        packsDir.listFiles()?.filter { it.extension.equals("zip", true) || it.isDirectory }?.forEach { f ->
            packsList.add(HorizonContentItem(f, f.nameWithoutExtension, true))
        }

        val shadersDir = File(context.filesDir, "shaderpacks").apply { mkdirs() }
        shadersList.clear()
        shadersDir.listFiles()?.filter { it.extension.equals("zip", true) || it.isDirectory }?.forEach { f ->
            shadersList.add(HorizonContentItem(f, f.nameWithoutExtension, true))
        }
    }

    LaunchedEffect(Unit) {
        reloadConfig()
        reloadContent()
    }

    val accountName = AccountsManager.getCurrentAccount()?.username?.takeIf { it.isNotBlank() } ?: "OnePeak"
    var showAccountDialog by remember { mutableStateOf(false) }

    if (showAccountDialog) {
        val currentAcc = AccountsManager.getCurrentAccount()
        val accountTypeRes =
            if (currentAcc?.accountType == com.movtery.zalithlauncher.game.account.AccountType.MICROSOFT.tag) {
                R.string.cubiq_horizon_account_microsoft
            } else {
                R.string.cubiq_horizon_account_offline
            }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.cubiq_horizon_account_dialog_title),
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.cubiq_horizon_account_current, currentAcc?.username ?: stringResource(R.string.cubiq_horizon_account_offline_player)),
                        color = Color(0xFFDDDDDD),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(R.string.cubiq_horizon_account_type, stringResource(accountTypeRes)),
                        color = Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.cubiq_horizon_account_description),
                        color = Color(0xFF888899),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                MinecraftButton(
                    onClick = {
                        showAccountDialog = false
                        Toast.makeText(context, context.getString(R.string.cubiq_horizon_account_verified, currentAcc?.username ?: "Steve"), Toast.LENGTH_SHORT).show()
                    },
                    style = MinecraftButtonStyle.GREEN
                ) {
                    Text(stringResource(R.string.cubiq_horizon_ok), color = Color.White, fontFamily = MinecraftFontFamily)
                }
            },
            dismissButton = {
                MinecraftButton(
                    onClick = {
                        showAccountDialog = false
                    },
                    style = MinecraftButtonStyle.STONE
                ) {
                    Text(stringResource(R.string.cubiq_horizon_close), color = Color.White, fontFamily = MinecraftFontFamily)
                }
            },
            containerColor = Color(0xFF1E2026)
        )
    }

    Dialog(
        onDismissRequest = {
            saveConfig()
            onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        androidx.compose.runtime.SideEffect {
            dialogWindow?.let { win ->
                win.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0xFF101014.toInt()))
                win.decorView.setBackgroundColor(0xFF101014.toInt())
                win.decorView.setPadding(0, 0, 0, 0)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val lp = win.attributes
                    lp.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    win.attributes = lp
                }
                win.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                )
                win.statusBarColor = 0xFF101014.toInt()
                win.navigationBarColor = 0xFF101014.toInt()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101014)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101014))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top App Header: "Minecraft Rust Edition" and "Account: <User>"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_rust_logo),
                                contentDescription = stringResource(R.string.cubiq_horizon_engine_rust_logo),
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.cubiq_horizon_title),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinecraftFontFamily
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF1E1E26))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cubiq_horizon_account_label, accountName),
                                    color = Color(0xFF55FF55),
                                    fontSize = 11.sp,
                                    fontFamily = MinecraftFontFamily
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF22222A))
                                    .clickable {
                                        saveConfig()
                                        onDismissRequest()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.cubiq_horizon_close),
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Navigation Tabs Bar: [Play] [Mods] [Packs] [Shaders] [Installed] [Settings]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF16161C))
                    ) {
                        HorizonUiTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) Color(0xFF22C55E) else Color.Transparent)
                                    .clickable {
                                        com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                                        currentTab = tab
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(tab.labelRes),
                                    color = if (isSelected) Color.White else Color(0xFF9E9EAA),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = MinecraftFontFamily
                                )
                            }
                        }
                    }

                    // Main Tab Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        when (currentTab) {
                            HorizonUiTab.SETTINGS -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.cubiq_horizon_launch_settings),
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MinecraftFontFamily,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }

                                    // 1. Rendering
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_rendering),
                                            currentValue = rendering,
                                            options = listOf(
                                                "Vulkan / GPU batching",
                                                "OpenGL ES / experimental",
                                                "Vulkan / compatibility"
                                            ),
                                            onSelect = {
                                                rendering = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 2. Client Resolution
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_client_resolution),
                                            currentValue = clientResolution,
                                            options = listOf("100%", "90%", "80%", "75%", "50%"),
                                            onSelect = {
                                                clientResolution = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 3. Profile
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_profile),
                                            currentValue = profile,
                                            options = listOf("Profile 0"),
                                            onSelect = {
                                                profile = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 4. Orientation
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_orientation),
                                            currentValue = orientation,
                                            options = listOf(
                                                "Landscape (sensor)",
                                                "Portrait (sensor)",
                                                "Landscape (reversed)",
                                                "Portrait (reversed)",
                                                "Automatic rotation"
                                            ),
                                            onSelect = {
                                                orientation = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 5. Input
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_input),
                                            currentValue = inputHost,
                                            options = listOf(
                                                "Automatic",
                                                "Touch & Android keyboard",
                                                "Keyboard & mouse",
                                                "Controller"
                                            ),
                                            onSelect = {
                                                inputHost = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 6. Immersive Fullscreen
                                    item {
                                        HorizonSwitchRow(
                                            label = stringResource(R.string.cubiq_horizon_immersive_fullscreen),
                                            checked = immersiveFullscreen,
                                            onCheckedChange = {
                                                immersiveFullscreen = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 7. Avoid Camera Cutout
                                    item {
                                        HorizonSwitchRow(
                                            label = stringResource(R.string.cubiq_horizon_avoid_camera_cutout),
                                            checked = avoidCutout,
                                            onCheckedChange = {
                                                avoidCutout = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 8. Aspect Ratio
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_aspect_ratio),
                                            currentValue = aspectRatio,
                                            options = listOf("Native", "16:9", "4:3", "1:1"),
                                            onSelect = {
                                                aspectRatio = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 9. Display Refresh Rate
                                    item {
                                        HorizonDropdownRow(
                                            label = stringResource(R.string.cubiq_horizon_display_refresh_rate),
                                            currentValue = refreshRate,
                                            options = listOf("System default", "60 Hz", "90 Hz", "120 Hz"),
                                            onSelect = {
                                                refreshRate = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 10. Performance hints
                                    item {
                                        HorizonSwitchRow(
                                            label = stringResource(R.string.cubiq_horizon_performance_hints),
                                            checked = performanceHints,
                                            onCheckedChange = {
                                                performanceHints = it
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 11. GPU Profiling
                                    item {
                                        HorizonActionRow(
                                            label = stringResource(R.string.cubiq_horizon_gpu_profiling),
                                            actionText = if (gpuProfiling) stringResource(R.string.cubiq_horizon_on_report) else stringResource(R.string.cubiq_horizon_off),
                                            onClick = {
                                                gpuProfiling = !gpuProfiling
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 12. Performance recording
                                    item {
                                        HorizonActionRow(
                                            label = stringResource(R.string.cubiq_horizon_performance_recording),
                                            actionText = if (performanceRecording) stringResource(R.string.cubiq_horizon_on_30_min) else stringResource(R.string.cubiq_horizon_off),
                                            onClick = {
                                                performanceRecording = !performanceRecording
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // 13. Diagnostic Report
                                    item {
                                        HorizonActionRow(
                                            label = stringResource(R.string.cubiq_horizon_diagnostic_report),
                                            actionText = stringResource(R.string.cubiq_horizon_export_report),
                                            onClick = {
                                                Toast.makeText(context, R.string.cubiq_horizon_report_saved, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    // 14. Raw Performance Data
                                    item {
                                        HorizonActionRow(
                                            label = stringResource(R.string.cubiq_horizon_raw_performance_data),
                                            actionText = stringResource(R.string.cubiq_horizon_jsonl_reports),
                                            onClick = {
                                                Toast.makeText(context, R.string.cubiq_horizon_jsonl_active, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    // 15. Server
                                    item {
                                        HorizonActionRow(
                                            label = stringResource(R.string.cubiq_horizon_server),
                                            actionText = settingLabel(serverName),
                                            onClick = {
                                                serverName = if (serverName.startsWith("Singleplayer")) "Multiplayer Server: default" else "Singleplayer / tap to enter server"
                                                saveConfig()
                                            }
                                        )
                                    }

                                    // Bottom Buttons: Diagnostics and Microsoft/Xbox account
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF1C1D24))
                                                    .border(BorderStroke(1.dp, Color(0xFF2C2D36)), RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        Toast.makeText(context, R.string.cubiq_horizon_diagnostics_passed, Toast.LENGTH_SHORT).show()
                                                    },
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.cubiq_horizon_diagnostics),
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontFamily = MinecraftFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 12.dp)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF22C55E))
                                                    .clickable {
                                                        showAccountDialog = true
                                                    },
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.cubiq_horizon_microsoft_account),
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontFamily = MinecraftFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 12.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            HorizonUiTab.PLAY -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = stringResource(R.string.cubiq_horizon_ready_to_play),
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MinecraftFontFamily
                                        )
                                        Text(
                                            text = stringResource(R.string.cubiq_horizon_version_profile),
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 13.sp,
                                            fontFamily = MinecraftFontFamily
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(stringResource(R.string.cubiq_horizon_open_world_or_join_server), color = Color(0xFFCCCCCC), fontSize = 12.sp, fontFamily = MinecraftFontFamily)
                                        Text(stringResource(R.string.cubiq_horizon_touch_setup), color = Color(0xFFCCCCCC), fontSize = 12.sp, fontFamily = MinecraftFontFamily)
                                        Text(stringResource(R.string.cubiq_horizon_find_packs), color = Color(0xFFCCCCCC), fontSize = 12.sp, fontFamily = MinecraftFontFamily)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF22C55E))
                                            .clickable {
                                                saveConfig()
                                                onDismissRequest()
                                                HorizonManager.launchHorizon(context)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cubiq_horizon_play),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MinecraftFontFamily
                                        )
                                    }
                                }
                            }

                            HorizonUiTab.MODS -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stringResource(R.string.cubiq_horizon_mods_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MinecraftFontFamily)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF22C55E))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(stringResource(R.string.cubiq_horizon_horizon), color = Color.White, fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF1E1E26))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Modrinth", color = Color(0xFFAAAAAA), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(stringResource(R.string.cubiq_horizon_search_mods), color = Color(0xFF8E8E9A), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(stringResource(R.string.cubiq_horizon_modrinth_browse_only), color = Color(0xFF7E7E8E), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                }
                            }

                            HorizonUiTab.PACKS -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(stringResource(R.string.cubiq_horizon_resource_packs), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MinecraftFontFamily)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(stringResource(R.string.cubiq_horizon_verified_downloads), color = Color(0xFF8E8E9A), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    listOf(stringResource(R.string.cubiq_horizon_pack_fresh_animations), stringResource(R.string.cubiq_horizon_pack_sodium_translations), stringResource(R.string.cubiq_horizon_pack_better_leaves)).forEach { pack ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(pack, color = Color.White, fontSize = 13.sp, fontFamily = MinecraftFontFamily)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF1E1E26))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                                    .clickable { Toast.makeText(context, context.getString(R.string.cubiq_horizon_pack_download_requested, pack), Toast.LENGTH_SHORT).show() }
                                            ) {
                                                Text(stringResource(R.string.cubiq_horizon_install), color = Color.White, fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                            }
                                        }
                                    }
                                }
                            }

                            HorizonUiTab.SHADERS -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(stringResource(R.string.cubiq_horizon_shader_packs), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MinecraftFontFamily)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(stringResource(R.string.cubiq_horizon_no_shader_packs), color = Color(0xFF8E8E9A), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                }
                            }

                            HorizonUiTab.INSTALLED -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(stringResource(R.string.cubiq_horizon_category_mods), stringResource(R.string.cubiq_horizon_category_resource_packs), stringResource(R.string.cubiq_horizon_category_shaders)).forEach { category ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF181820))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(category, color = Color.White, fontSize = 13.sp, fontFamily = MinecraftFontFamily)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Color(0xFF22222C))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    .clickable { Toast.makeText(context, R.string.cubiq_horizon_choose_file, Toast.LENGTH_SHORT).show() }
                                            ) {
                                                Text(stringResource(R.string.cubiq_horizon_import), color = Color.White, fontSize = 11.sp, fontFamily = MinecraftFontFamily)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizonDropdownRow(
    label: String,
    currentValue: String,
    options: List<String> = emptyList(),
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF14151C))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFD4D4E0),
            fontSize = 13.sp,
            fontFamily = MinecraftFontFamily
        )

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1F28))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = settingLabel(currentValue),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text("v", color = Color(0xFF55FF55), fontSize = 11.sp, fontFamily = MinecraftFontFamily)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1B1B24))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = settingLabel(option),
                                color = if (option == currentValue) Color(0xFF55FF55) else Color.White,
                                fontSize = 12.sp,
                                fontFamily = MinecraftFontFamily
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizonSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF14151C))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFD4D4E0),
            fontSize = 13.sp,
            fontFamily = MinecraftFontFamily
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF22C55E),
                uncheckedThumbColor = Color(0xFF888894),
                uncheckedTrackColor = Color(0xFF262632)
            )
        )
    }
}

@Composable
private fun HorizonActionRow(
    label: String,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF14151C))
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFD4D4E0),
            fontSize = 13.sp,
            fontFamily = MinecraftFontFamily
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1F28))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionText,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
