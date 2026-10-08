/*
 * Cubiq Launcher
 * Minecraft PC Launcher Profile & Game Settings Screen (Integrated Real Zalith Settings & Accounts)
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.layer_controller.layout.EmptyControlLayout
import com.movtery.layer_controller.layout.EmptyLayoutInfo
import com.movtery.layer_controller.utils.newRandomFileName
import com.movtery.layer_controller.utils.saveToFile
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.control.ControlData
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GamepadInputMode
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.ui.activities.startEditorActivity
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.control.GamepadBindingKeyboard
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMap
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickMode
import com.movtery.zalithlauncher.ui.control.gamepad.getNameByGamepadEvent
import com.movtery.zalithlauncher.ui.control.gamepad.remapperMMKV
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class SettingsTab(val title: String) {
    GAME("GAME"),
    VIDEO("VIDEO"),
    CONTROLS("CONTROLS"),
    GAMEPAD("GAMEPAD"),
    LAUNCHER("LAUNCHER"),
    JAVA("JAVA"),
    LAYOUTS("LAYOUTS"),
    ABOUT("ABOUT")
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
    var showManageAccounts by remember { mutableStateOf(false) }
    var offlineUsername by remember { mutableStateOf("") }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    var isLoggingInMicrosoft by remember { mutableStateOf(false) }
    var msDeviceCodeText by remember { mutableStateOf<String?>(null) }
    var msVerificationUrl by remember { mutableStateOf<String?>(null) }

    var zinkPreferSystemDriver by remember { mutableStateOf(AllSettings.zinkPreferSystemDriver.state) }
    var dumpShaders by remember { mutableStateOf(AllSettings.dumpShaders.state) }
    var customResolutionWidth by remember { mutableIntStateOf(AllSettings.customResolutionWidth.state) }
    var customResolutionHeight by remember { mutableIntStateOf(AllSettings.customResolutionHeight.state) }

    val gamepadViewModel: GamepadViewModel = viewModel()
    val controlLayouts by ControlManager.dataList.collectAsStateWithLifecycle()
    var activeControlLayoutName by remember { mutableStateOf(AllSettings.controlLayout.state) }
    var gamepadInputMode by remember { mutableStateOf(AllSettings.gamepadInputMode.state) }
    var joystickMode by remember { mutableStateOf(AllSettings.joystickControlMode.state) }
    var editGamepadKeyInGame by remember { mutableStateOf(true) }
    var selectedGamepadMapForBinding by remember { mutableStateOf<GamepadMap?>(null) }
    var showCreateGamepadConfigDialog by remember { mutableStateOf(false) }
    var newGamepadConfigName by remember { mutableStateOf("") }
    var showCreateLayoutDialog by remember { mutableStateOf(false) }
    var newLayoutName by remember { mutableStateOf("") }
    var gamepadConfigRefreshed by remember { mutableStateOf(false) }
    var controlsOpacity by remember { mutableIntStateOf(AllSettings.controlsOpacity.state) }
    var gestureControl by remember { mutableStateOf(AllSettings.gestureControl.state) }
    var gestureTapAction by remember { mutableStateOf(AllSettings.gestureTapMouseAction.state) }
    var gestureLongPressAction by remember { mutableStateOf(AllSettings.gestureLongPressMouseAction.state) }
    var hotbarDoubleClick by remember { mutableStateOf(AllSettings.hotbarDoubleClick.state) }
    var hotbarLongClick by remember { mutableStateOf(AllSettings.hotbarLongClick.state) }
    var mouseSize by remember { mutableIntStateOf(AllSettings.mouseSize.state) }
    var hideMouse by remember { mutableStateOf(AllSettings.hideMouse.state) }

    var gamepadDeadZone by remember { mutableFloatStateOf(AllSettings.gamepadDeadZoneScale.state.toFloat()) }
    var gamepadCursorSens by remember { mutableFloatStateOf(AllSettings.gamepadCursorSensitivity.state.toFloat()) }
    var gamepadCameraSens by remember { mutableFloatStateOf(AllSettings.gamepadCameraSensitivity.state.toFloat()) }

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
            // Profile Card Header & Quick Switcher
            AccountSection(
                account = account,
                accountsList = accountsList,
                refreshWardrobe = refreshWardrobe,
                isLoggingInMicrosoft = isLoggingInMicrosoft,
                onMicrosoftLoginClick = {
                    if (isLoggingInMicrosoft) return@AccountSection
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
                            val str = text.toAndroidString(context)
                            Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                        },
                        submitError = { err ->
                            isLoggingInMicrosoft = false
                            msDeviceCodeText = null
                            Toast.makeText(context, "Microsoft Login Failed: ${err.message.toAndroidString(context)}", Toast.LENGTH_LONG).show()
                        },
                        onSuccess = {
                            isLoggingInMicrosoft = false
                            msDeviceCodeText = null
                            Toast.makeText(context, "Successfully logged into Microsoft Account!", Toast.LENGTH_SHORT).show()
                        },
                        onDeviceCode = { deviceCode ->
                            msDeviceCodeText = deviceCode.userCode
                            msVerificationUrl = deviceCode.verificationUrl
                        }
                    )
                },
                onAddOfflineClick = { showAddOfflineDialog = true },
                onSelectAccount = { acc ->
                    AllSettings.currentAccount.save(acc.uniqueUUID)
                    AccountsManager.reloadAccounts()
                    Toast.makeText(context, "Switched to ${acc.username}", Toast.LENGTH_SHORT).show()
                },
                onDeleteAccount = { acc -> accountToDelete = acc }
            )

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

            // Tab Bar: Horizontal Scrollable Row for all 8 categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SettingsTab.entries.forEach { tab ->
                    val isTabSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isTabSelected) Color(0xFF3C8527) else Color(0xFF262628))
                            .border(
                                BorderStroke(1.dp, if (isTabSelected) Color(0xFF55FF55) else Color(0xFF383838)),
                                RoundedCornerShape(2.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Content based on selectedTab
            when (selectedTab) {
                SettingsTab.GAME -> {
                    GameSettingsTab(
                        maxRam = maxRam,
                        ramValue = ramValue,
                        onRamChange = { ramValue = it },
                        autoPickJava = autoPickJava,
                        onAutoPickJavaChange = { autoPickJava = it },
                        selectedJava = selectedJava,
                        onSelectedJavaChange = { selectedJava = it },
                        versionIsolation = versionIsolation,
                        onVersionIsolationChange = { versionIsolation = it },
                        skipGameIntegrity = skipGameIntegrity,
                        onSkipGameIntegrityChange = { skipGameIntegrity = it },
                        jvmArgs = jvmArgs,
                        onJvmArgsChange = { jvmArgs = it },
                        versionCustomInfo = versionCustomInfo,
                        onVersionCustomInfoChange = { versionCustomInfo = it },
                        showLogAuto = showLogAuto,
                        onShowLogAutoChange = { showLogAuto = it },
                        logTextSize = logTextSize,
                        onLogTextSizeChange = { logTextSize = it }
                    )
                }

                SettingsTab.VIDEO -> {
                    VideoSettingsTab(
                        allRenderers = allRenderers,
                        selectedRendererId = selectedRendererId,
                        onRendererChange = { selectedRendererId = it },
                        selectedVulkanDriver = selectedVulkanDriver,
                        onVulkanDriverChange = { selectedVulkanDriver = it },
                        gameFullScreen = gameFullScreen,
                        onGameFullScreenChange = { gameFullScreen = it },
                        resolutionRatio = resolutionRatio,
                        onResolutionRatioChange = { resolutionRatio = it },
                        customResolutionWidth = customResolutionWidth,
                        onCustomResolutionWidthChange = { customResolutionWidth = it },
                        customResolutionHeight = customResolutionHeight,
                        onCustomResolutionHeightChange = { customResolutionHeight = it },
                        sustainedPerf = sustainedPerf,
                        onSustainedPerfChange = { sustainedPerf = it },
                        vsyncInZink = vsyncInZink,
                        onVsyncInZinkChange = { vsyncInZink = it },
                        zinkPreferSystemDriver = zinkPreferSystemDriver,
                        onZinkPreferSystemDriverChange = { zinkPreferSystemDriver = it },
                        dumpShaders = dumpShaders,
                        onDumpShadersChange = { dumpShaders = it },
                        useSurfaceView = useSurfaceView,
                        onUseSurfaceViewChange = { useSurfaceView = it }
                    )
                }

                SettingsTab.CONTROLS -> {
                    ControlsSettingsTab(
                        mouseMode = mouseMode,
                        onMouseModeChange = { mouseMode = it },
                        cursorSensitivity = cursorSensitivity,
                        onCursorSensitivityChange = { cursorSensitivity = it },
                        mouseSize = mouseSize,
                        onMouseSizeChange = { mouseSize = it },
                        enableMouseClick = enableMouseClick,
                        onEnableMouseClickChange = { enableMouseClick = it },
                        hideMouse = hideMouse,
                        onHideMouseChange = { hideMouse = it },
                        physicalMouseMode = physicalMouseMode,
                        onPhysicalMouseModeChange = { physicalMouseMode = it },
                        gestureControl = gestureControl,
                        onGestureControlChange = { gestureControl = it },
                        gestureTapAction = gestureTapAction,
                        onGestureTapActionChange = { gestureTapAction = it },
                        gestureLongPressAction = gestureLongPressAction,
                        onGestureLongPressActionChange = { gestureLongPressAction = it },
                        hotbarDoubleClick = hotbarDoubleClick,
                        onHotbarDoubleClickChange = { hotbarDoubleClick = it },
                        hotbarLongClick = hotbarLongClick,
                        onHotbarLongClickChange = { hotbarLongClick = it },
                        gyroscopeControl = gyroscopeControl,
                        onGyroscopeControlChange = { gyroscopeControl = it },
                        gyroscopeSensitivity = gyroscopeSensitivity,
                        onGyroscopeSensitivityChange = { gyroscopeSensitivity = it }
                    )
                }

                SettingsTab.LAUNCHER -> {
                    LauncherSettingsTab(
                        isFullScreen = isFullScreen,
                        onFullScreenChange = { isFullScreen = it },
                        backgroundBlurVal = backgroundBlurVal,
                        onBackgroundBlurChange = { backgroundBlurVal = it },
                        animateSpeedVal = animateSpeedVal,
                        onAnimateSpeedChange = { animateSpeedVal = it },
                        seasonalEffects = seasonalEffects,
                        onSeasonalEffectsChange = { seasonalEffects = it }
                    )
                }

                SettingsTab.GAMEPAD -> {
                    GamepadSettingsTab(
                        gamepadViewModel = gamepadViewModel,
                        gamepadControl = gamepadControl,
                        onGamepadControlChange = { gamepadControl = it },
                        gamepadInputMode = gamepadInputMode,
                        onGamepadInputModeChange = { gamepadInputMode = it },
                        joystickMode = joystickMode,
                        onJoystickModeChange = { joystickMode = it },
                        gamepadDeadZone = gamepadDeadZone,
                        onGamepadDeadZoneChange = { gamepadDeadZone = it },
                        gamepadCameraSens = gamepadCameraSens,
                        onGamepadCameraSensChange = { gamepadCameraSens = it },
                        gamepadCursorSens = gamepadCursorSens,
                        onGamepadCursorSensChange = { gamepadCursorSens = it },
                        editGamepadKeyInGame = editGamepadKeyInGame,
                        onEditGamepadKeyInGameChange = { editGamepadKeyInGame = it },
                        gamepadConfigRefreshed = gamepadConfigRefreshed,
                        onNewProfileClick = { showCreateGamepadConfigDialog = true },
                        onResetAllClick = {
                            scope.launch(Dispatchers.IO) {
                                remapperMMKV().clearAll()
                                gamepadViewModel.reloadAllMappings()
                                withContext(Dispatchers.Main) {
                                    gamepadConfigRefreshed = !gamepadConfigRefreshed
                                    Toast.makeText(context, "Mappings reset to defaults", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSelectBinding = { selectedGamepadMapForBinding = it }
                    )
                }

                SettingsTab.JAVA -> {
                    JavaSettingsTab(
                        selectedJava = selectedJava,
                        onSelectJava = { selectedJava = it }
                    )
                }

                SettingsTab.LAYOUTS -> {
                    LayoutsSettingsTab(
                        controlLayouts = controlLayouts,
                        activeControlLayoutName = activeControlLayoutName,
                        controlsOpacity = controlsOpacity,
                        onControlsOpacityChange = { controlsOpacity = it },
                        onSelectLayout = { name ->
                            activeControlLayoutName = name
                        },
                        onLaunchEditor = {
                            val activeControl = controlLayouts.find { it.file.name == activeControlLayoutName } ?: controlLayouts.firstOrNull()
                            val activeFile = activeControl?.file ?: File(PathManager.DIR_CONTROL_LAYOUTS, "default.json")
                            startEditorActivity(context, activeFile)
                        },
                        onCreateNewLayout = { showCreateLayoutDialog = true }
                    )
                }

                SettingsTab.ABOUT -> {
                    AboutSettingsTab()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Dialogs
        if (isLoggingInMicrosoft) {
            MicrosoftLoginDialog(
                msDeviceCodeText = msDeviceCodeText,
                msVerificationUrl = msVerificationUrl,
                onDismiss = {
                    com.movtery.zalithlauncher.coroutine.TaskSystem.cancelTask(com.movtery.zalithlauncher.game.account.MICROSOFT_LOGGING_TASK)
                    isLoggingInMicrosoft = false
                    msDeviceCodeText = null
                }
            )
        }

        if (showAddOfflineDialog) {
            AddOfflineAccountDialog(
                username = offlineUsername,
                onUsernameChange = { offlineUsername = it },
                onDismiss = { showAddOfflineDialog = false },
                onCreated = {
                    showAddOfflineDialog = false
                    offlineUsername = ""
                }
            )
        }

        accountToDelete?.let { acc ->
            DeleteAccountConfirmDialog(
                account = acc,
                onDismiss = { accountToDelete = null },
                onDeleted = { accountToDelete = null }
            )
        }

        selectedGamepadMapForBinding?.let { map ->
            GamepadBindingDialog(
                map = map,
                gamepadViewModel = gamepadViewModel,
                editGamepadKeyInGame = editGamepadKeyInGame,
                gamepadConfigRefreshed = gamepadConfigRefreshed,
                onRefresh = { gamepadConfigRefreshed = !gamepadConfigRefreshed },
                onDismiss = { selectedGamepadMapForBinding = null }
            )
        }

        if (showCreateGamepadConfigDialog) {
            CreateGamepadConfigDialog(
                gamepadViewModel = gamepadViewModel,
                configName = newGamepadConfigName,
                onConfigNameChange = { newGamepadConfigName = it },
                onDismiss = {
                    showCreateGamepadConfigDialog = false
                    newGamepadConfigName = ""
                },
                onCreated = {
                    gamepadConfigRefreshed = !gamepadConfigRefreshed
                    showCreateGamepadConfigDialog = false
                    newGamepadConfigName = ""
                }
            )
        }

        if (showCreateLayoutDialog) {
            CreateTouchLayoutDialog(
                layoutName = newLayoutName,
                onLayoutNameChange = { newLayoutName = it },
                onDismiss = {
                    showCreateLayoutDialog = false
                    newLayoutName = ""
                },
                onCreated = {
                    showCreateLayoutDialog = false
                    newLayoutName = ""
                }
            )
        }
    }
}

@Composable
private fun MicrosoftLoginDialog(
    msDeviceCodeText: String?,
    msVerificationUrl: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCCCCCC),
        title = {
            Text(
                text = "MICROSOFT LOGIN",
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF55FF55)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (msDeviceCodeText == null) {
                    CircularProgressIndicator(
                        color = Color(0xFF55FF55),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Connecting to Microsoft authentication service...",
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        text = "1. Open the activation page:\n${msVerificationUrl ?: "https://www.microsoft.com/link"}\n\n2. Enter this code to sign in:",
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFFCCCCCC)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF141416))
                            .border(BorderStroke(1.dp, Color(0xFF55FF55)), RoundedCornerShape(3.dp))
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = msDeviceCodeText,
                            color = Color(0xFF55FF55),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinecraftButton(
                            onClick = {
                                com.movtery.zalithlauncher.utils.copyText(
                                    "Microsoft Device Code",
                                    msDeviceCodeText,
                                    context,
                                    true
                                )
                            },
                            style = MinecraftButtonStyle.STONE,
                            text = "COPY CODE",
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        )

                        MinecraftButton(
                            onClick = {
                                val url = msVerificationUrl ?: "https://www.microsoft.com/link"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = "OPEN BROWSER",
                            fontSize = 10.5.sp,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        )
                    }

                    Text(
                        text = "Waiting for browser authorization...",
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.5.sp
                    )
                }
            }
        },
        confirmButton = {
            MinecraftButton(
                onClick = onDismiss,
                style = MinecraftButtonStyle.RED,
                text = "CANCEL",
                fontSize = 11.5.sp,
                modifier = Modifier
                    .width(84.dp)
                    .height(34.dp)
            )
        }
    )
}

@Composable
private fun AddOfflineAccountDialog(
    username: String,
    onUsernameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = {
            Text(
                text = "ADD OFFLINE ACCOUNT",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter username for local offline play:",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    placeholder = { Text("Username", color = Color(0xFF666666), fontSize = 12.sp) },
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
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    if (username.isNotBlank()) {
                        localLogin(userName = username.trim(), userUUID = null)
                        AccountsManager.reloadAccounts()
                        Toast.makeText(context, "Account '${username.trim()}' added!", Toast.LENGTH_SHORT).show()
                        onCreated()
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
                onClick = onDismiss,
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

@Composable
private fun DeleteAccountConfirmDialog(
    account: Account,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
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
                text = "Are you sure you want to remove account '${account.username}'?",
                color = Color(0xFFCCCCCC),
                fontFamily = MinecraftFontFamily,
                fontSize = 12.sp
            )
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        AccountsManager.deleteAccount(account)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                            onDeleted()
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
                onClick = onDismiss,
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

@Composable
private fun GamepadBindingDialog(
    map: GamepadMap,
    gamepadViewModel: GamepadViewModel,
    editGamepadKeyInGame: Boolean,
    gamepadConfigRefreshed: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedKeys = remember(map, editGamepadKeyInGame, gamepadConfigRefreshed) {
        (gamepadViewModel.currentMapping?.findByMap(map, inGame = editGamepadKeyInGame)?.toList() ?: emptyList()).toMutableList()
    }

    GamepadBindingKeyboard(
        selectedKeys = selectedKeys,
        onKeyAdd = { key ->
            selectedKeys.add(key)
            gamepadViewModel.currentMapping?.saveMapping(map, selectedKeys.toSet(), editGamepadKeyInGame)
            onRefresh()
        },
        onKeyRemove = { key ->
            selectedKeys.remove(key)
            gamepadViewModel.currentMapping?.saveMapping(map, selectedKeys.toSet(), editGamepadKeyInGame)
            onRefresh()
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun CreateGamepadConfigDialog(
    gamepadViewModel: GamepadViewModel,
    configName: String,
    onConfigNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = {
            Text(
                text = "NEW GAMEPAD PROFILE",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter profile name:",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = configName,
                    onValueChange = onConfigNameChange,
                    placeholder = { Text("Profile Name", color = Color(0xFF666666), fontSize = 12.sp) },
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
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    if (configName.isNotBlank()) {
                        gamepadViewModel.createNewConfig(
                            name = configName.trim(),
                            onContainsConfig = {
                                Toast.makeText(context, "Profile name already exists!", Toast.LENGTH_SHORT).show()
                            },
                            onFinished = {
                                Toast.makeText(context, "Gamepad Profile created!", Toast.LENGTH_SHORT).show()
                                onCreated()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
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
                onClick = onDismiss,
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

@Composable
private fun CreateTouchLayoutDialog(
    layoutName: String,
    onLayoutNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        title = {
            Text(
                text = "CREATE NEW TOUCH LAYOUT",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter layout name:",
                    color = Color(0xFFCCCCCC),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = layoutName,
                    onValueChange = onLayoutNameChange,
                    placeholder = { Text("Layout Name (e.g. PvP, Building)", color = Color(0xFF666666), fontSize = 12.sp) },
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
        },
        confirmButton = {
            MinecraftButton(
                onClick = {
                    if (layoutName.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            val file = File(PathManager.DIR_CONTROL_LAYOUTS, "${newRandomFileName()}.json")
                            try {
                                val layout = EmptyControlLayout.copy(
                                    info = EmptyLayoutInfo.copy(
                                        name = createTranslatable(default = layoutName.trim()),
                                        author = createTranslatable(default = "Player"),
                                        versionName = "1.0"
                                    )
                                )
                                layout.saveToFile(file)
                                ControlManager.refresh()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Layout '${layoutName.trim()}' created!", Toast.LENGTH_SHORT).show()
                                    onCreated()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to create layout: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Layout name cannot be empty", Toast.LENGTH_SHORT).show()
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
                onClick = onDismiss,
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

@Composable
private fun AccountSection(
    account: Account?,
    accountsList: List<Account>,
    refreshWardrobe: Boolean,
    isLoggingInMicrosoft: Boolean,
    onMicrosoftLoginClick: () -> Unit,
    onAddOfflineClick: () -> Unit,
    onSelectAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {
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
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(BorderStroke(2.5.dp, Color(0xFF1E88E5)), RoundedCornerShape(6.dp))
                    .background(Color(0xFF222222)),
                contentAlignment = Alignment.Center
            ) {
                if (account != null) {
                    PlayerFace(
                        modifier = Modifier.size(54.dp),
                        account = account,
                        avatarSize = 54.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_person_outlined),
                        contentDescription = "No Account",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = account?.username ?: "NO ACTIVE ACCOUNT",
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (account != null) "Type: ${getAccountTypeName(account)}" else "Add an account below to play Minecraft",
                color = if (account != null) Color(0xFF55FF55) else Color(0xFFFF5555),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinecraftButton(
                    onClick = onMicrosoftLoginClick,
                    style = MinecraftButtonStyle.GREEN,
                    text = if (isLoggingInMicrosoft) "LOGGING IN..." else "+ MICROSOFT",
                    fontSize = 9.5.sp,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                )

                MinecraftButton(
                    onClick = onAddOfflineClick,
                    style = MinecraftButtonStyle.STONE,
                    text = "+ OFFLINE",
                    fontSize = 9.5.sp,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(38.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "SAVED ACCOUNTS (${accountsList.size})",
        color = Color(0xFFAAAAAA),
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp
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
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) Color(0xFF263820) else Color(0xFF242426))
                        .border(
                            BorderStroke(1.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable { onSelectAccount(acc) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerFace(
                        modifier = Modifier.size(28.dp),
                        account = acc,
                        avatarSize = 28.dp
                    )

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

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .clickable { onDeleteAccount(acc) },
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
}

@Composable
private fun GameSettingsTab(
    maxRam: Float,
    ramValue: Float,
    onRamChange: (Float) -> Unit,
    autoPickJava: Boolean,
    onAutoPickJavaChange: (Boolean) -> Unit,
    selectedJava: String,
    onSelectedJavaChange: (String) -> Unit,
    versionIsolation: Boolean,
    onVersionIsolationChange: (Boolean) -> Unit,
    skipGameIntegrity: Boolean,
    onSkipGameIntegrityChange: (Boolean) -> Unit,
    jvmArgs: String,
    onJvmArgsChange: (String) -> Unit,
    versionCustomInfo: String,
    onVersionCustomInfoChange: (String) -> Unit,
    showLogAuto: Boolean,
    onShowLogAutoChange: (Boolean) -> Unit,
    logTextSize: Float,
    onLogTextSizeChange: (Float) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. RAM Allocation
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RAM Allocation", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${ramValue.toInt()} MB", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = ramValue,
                onValueChange = onRamChange,
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
                Text("Auto-Pick Java Runtime", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Automatically selects best Java 8/17/21 for the version", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = autoPickJava,
                onCheckedChange = {
                    onAutoPickJavaChange(it)
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

        // 3. Version Isolation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Version Isolation", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Isolate mods and configs per Minecraft version", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = versionIsolation,
                onCheckedChange = {
                    onVersionIsolationChange(it)
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

        // 4. Skip Game Integrity Check
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Skip Asset Verification", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Faster game launch by bypassing SHA1 file checks", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = skipGameIntegrity,
                onCheckedChange = {
                    onSkipGameIntegrityChange(it)
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

        // 5. Custom JVM Arguments
        Column {
            Text("Custom JVM Arguments", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = jvmArgs,
                onValueChange = {
                    onJvmArgsChange(it)
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

        // 6. Version Custom Subtitle
        Column {
            Text("Version Custom Subtitle", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = versionCustomInfo,
                onValueChange = {
                    onVersionCustomInfoChange(it)
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

        // 7. Auto Show Log
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show Console Log on Launch", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Automatically displays runtime log window on game start", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = showLogAuto,
                onCheckedChange = {
                    onShowLogAutoChange(it)
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

        // 8. Log Text Size
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
                onValueChange = onLogTextSizeChange,
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

@Composable
private fun VideoSettingsTab(
    allRenderers: List<RendererInterface>,
    selectedRendererId: String,
    onRendererChange: (String) -> Unit,
    selectedVulkanDriver: String,
    onVulkanDriverChange: (String) -> Unit,
    gameFullScreen: Boolean,
    onGameFullScreenChange: (Boolean) -> Unit,
    resolutionRatio: Float,
    onResolutionRatioChange: (Float) -> Unit,
    customResolutionWidth: Int,
    onCustomResolutionWidthChange: (Int) -> Unit,
    customResolutionHeight: Int,
    onCustomResolutionHeightChange: (Int) -> Unit,
    sustainedPerf: Boolean,
    onSustainedPerfChange: (Boolean) -> Unit,
    vsyncInZink: Boolean,
    onVsyncInZinkChange: (Boolean) -> Unit,
    zinkPreferSystemDriver: Boolean,
    onZinkPreferSystemDriverChange: (Boolean) -> Unit,
    dumpShaders: Boolean,
    onDumpShadersChange: (Boolean) -> Unit,
    useSurfaceView: Boolean,
    onUseSurfaceViewChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Renderer Selection
        Column {
            Text("Renderer Selection", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            Spacer(modifier = Modifier.height(6.dp))
            for (renderer in allRenderers) {
                val isSel = selectedRendererId == renderer.getUniqueIdentifier()
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
                            onRendererChange(renderer.getUniqueIdentifier())
                            AllSettings.renderer.save(renderer.getUniqueIdentifier())
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = renderer.getRendererName(),
                            color = if (isSel) Color(0xFF55FF55) else Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = renderer.getUniqueIdentifier(),
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                    RadioButton(
                        selected = isSel,
                        onClick = {
                            onRendererChange(renderer.getUniqueIdentifier())
                            AllSettings.renderer.save(renderer.getUniqueIdentifier())
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF55FF55),
                            unselectedColor = Color(0xFF666666)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // 2. Fullscreen Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Game Fullscreen", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Extend game view into notch/cutout area", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = gameFullScreen,
                onCheckedChange = {
                    onGameFullScreenChange(it)
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

        // 3. Resolution Scaling
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
                onValueChange = onResolutionRatioChange,
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

        // 4. Custom Resolution
        Column {
            Text("Custom Resolution (Width x Height)", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (customResolutionWidth > 0) customResolutionWidth.toString() else "",
                    onValueChange = {
                        val num = it.toIntOrNull() ?: 0
                        onCustomResolutionWidthChange(num)
                        AllSettings.customResolutionWidth.save(num)
                    },
                    placeholder = { Text("Width (e.g. 1920)", color = Color(0xFF666666), fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF242426),
                        unfocusedContainerColor = Color(0xFF242426),
                        focusedBorderColor = Color(0xFF55FF55),
                        unfocusedBorderColor = Color(0xFF383838),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = if (customResolutionHeight > 0) customResolutionHeight.toString() else "",
                    onValueChange = {
                        val num = it.toIntOrNull() ?: 0
                        onCustomResolutionHeightChange(num)
                        AllSettings.customResolutionHeight.save(num)
                    },
                    placeholder = { Text("Height (e.g. 1080)", color = Color(0xFF666666), fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
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
        }

        // 5. Sustained Performance Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sustained Performance", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Maintains stable FPS on thermal-throttling chips", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = sustainedPerf,
                onCheckedChange = {
                    onSustainedPerfChange(it)
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

        // 6. SurfaceView vs TextureView
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Use SurfaceView Rendering", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Improves frame pacing and reduces battery drain", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = useSurfaceView,
                onCheckedChange = {
                    onUseSurfaceViewChange(it)
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
    }
}

@Composable
private fun ControlsSettingsTab(
    mouseMode: MouseControlMode,
    onMouseModeChange: (MouseControlMode) -> Unit,
    cursorSensitivity: Float,
    onCursorSensitivityChange: (Float) -> Unit,
    mouseSize: Int,
    onMouseSizeChange: (Int) -> Unit,
    enableMouseClick: Boolean,
    onEnableMouseClickChange: (Boolean) -> Unit,
    hideMouse: Boolean,
    onHideMouseChange: (Boolean) -> Unit,
    physicalMouseMode: Boolean,
    onPhysicalMouseModeChange: (Boolean) -> Unit,
    gestureControl: Boolean,
    onGestureControlChange: (Boolean) -> Unit,
    gestureTapAction: GestureActionType,
    onGestureTapActionChange: (GestureActionType) -> Unit,
    gestureLongPressAction: GestureActionType,
    onGestureLongPressActionChange: (GestureActionType) -> Unit,
    hotbarDoubleClick: Boolean,
    onHotbarDoubleClickChange: (Boolean) -> Unit,
    hotbarLongClick: Boolean,
    onHotbarLongClickChange: (Boolean) -> Unit,
    gyroscopeControl: Boolean,
    onGyroscopeControlChange: (Boolean) -> Unit,
    gyroscopeSensitivity: Float,
    onGyroscopeSensitivityChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Virtual Mouse Control Mode", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)

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
                            onMouseModeChange(mode)
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
                onValueChange = onCursorSensitivityChange,
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

        // Pointer Size
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Virtual Mouse Pointer Size", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${mouseSize} dp", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = mouseSize.toFloat(),
                onValueChange = { onMouseSizeChange(it.toInt()) },
                onValueChangeFinished = {
                    AllSettings.mouseSize.save(mouseSize)
                },
                valueRange = 12f..48f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        // Tap to Click
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tap Screen to Click", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Single tap fires left mouse click", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = enableMouseClick,
                onCheckedChange = {
                    onEnableMouseClickChange(it)
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
                Text("Physical Mouse Capture", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Direct hardware mouse lock & cursor hiding", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = physicalMouseMode,
                onCheckedChange = {
                    onPhysicalMouseModeChange(it)
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

        // Gesture Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Touch Screen Gestures", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Swipe and pinch camera gestures", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = gestureControl,
                onCheckedChange = {
                    onGestureControlChange(it)
                    AllSettings.gestureControl.save(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF3C8527),
                    uncheckedThumbColor = Color(0xFF888888),
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }

        // Gyroscope
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Gyroscope Aiming", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Aim camera with device motion sensors", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = gyroscopeControl,
                onCheckedChange = {
                    onGyroscopeControlChange(it)
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
                    onValueChange = onGyroscopeSensitivityChange,
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

@Composable
private fun LauncherSettingsTab(
    isFullScreen: Boolean,
    onFullScreenChange: (Boolean) -> Unit,
    backgroundBlurVal: Float,
    onBackgroundBlurChange: (Float) -> Unit,
    animateSpeedVal: Float,
    onAnimateSpeedChange: (Float) -> Unit,
    seasonalEffects: Boolean,
    onSeasonalEffectsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Launcher Fullscreen", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Text("Hide Android system status & navigation bars", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = isFullScreen,
                onCheckedChange = {
                    onFullScreenChange(it)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Seasonal & Festival Effects", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Text("Show snowflake & holiday particle effects", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = seasonalEffects,
                onCheckedChange = {
                    onSeasonalEffectsChange(it)
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

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("UI Blur Intensity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${backgroundBlurVal.toInt()} px", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = backgroundBlurVal,
                onValueChange = onBackgroundBlurChange,
                onValueChangeFinished = {
                    AllSettings.backgroundBlur.save(backgroundBlurVal.toInt())
                },
                valueRange = 0f..25f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }
    }
}

@Composable
private fun GamepadSettingsTab(
    gamepadViewModel: GamepadViewModel,
    gamepadControl: Boolean,
    onGamepadControlChange: (Boolean) -> Unit,
    gamepadInputMode: GamepadInputMode,
    onGamepadInputModeChange: (GamepadInputMode) -> Unit,
    joystickMode: JoystickMode,
    onJoystickModeChange: (JoystickMode) -> Unit,
    gamepadDeadZone: Float,
    onGamepadDeadZoneChange: (Float) -> Unit,
    gamepadCameraSens: Float,
    onGamepadCameraSensChange: (Float) -> Unit,
    gamepadCursorSens: Float,
    onGamepadCursorSensChange: (Float) -> Unit,
    editGamepadKeyInGame: Boolean,
    onEditGamepadKeyInGameChange: (Boolean) -> Unit,
    gamepadConfigRefreshed: Boolean,
    onNewProfileClick: () -> Unit,
    onResetAllClick: () -> Unit,
    onSelectBinding: (GamepadMap) -> Unit
) {
    val currentMapping = gamepadViewModel.currentMapping

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Enable Gamepad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable Gamepad Support", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                Text("Connect external Xbox, PlayStation, Switch, or generic Bluetooth controllers", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = gamepadControl,
                onCheckedChange = {
                    onGamepadControlChange(it)
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

        // 2. Gamepad Input Mode
        Text("Gamepad Input Mode", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (mode in GamepadInputMode.entries) {
                val isSel = gamepadInputMode == mode
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
                            onGamepadInputModeChange(mode)
                            AllSettings.gamepadInputMode.save(mode)
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (mode == GamepadInputMode.Mapped) "MAPPED INPUT" else "DIRECT SDL PASS",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 3. Stick Deadzone
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Analog Stick Deadzone", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${gamepadDeadZone.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = gamepadDeadZone,
                onValueChange = onGamepadDeadZoneChange,
                onValueChangeFinished = {
                    AllSettings.gamepadDeadZoneScale.save(gamepadDeadZone.toInt())
                },
                valueRange = 50f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        // 4. Stick Sensitivities
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Right Stick Camera Sensitivity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${gamepadCameraSens.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = gamepadCameraSens,
                onValueChange = onGamepadCameraSensChange,
                onValueChangeFinished = {
                    AllSettings.gamepadCameraSensitivity.save(gamepadCameraSens.toInt())
                },
                valueRange = 25f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Menu Cursor Sensitivity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${gamepadCursorSens.toInt()}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = gamepadCursorSens,
                onValueChange = onGamepadCursorSensChange,
                onValueChangeFinished = {
                    AllSettings.gamepadCursorSensitivity.save(gamepadCursorSens.toInt())
                },
                valueRange = 25f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        // 5. Profile Switcher & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile: ${currentMapping?.name ?: "Default"}",
                color = Color(0xFF55FF55),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MinecraftButton(
                    onClick = onNewProfileClick,
                    style = MinecraftButtonStyle.STONE,
                    text = "+ NEW PROFILE",
                    fontSize = 9.sp,
                    modifier = Modifier.height(28.dp)
                )
                MinecraftButton(
                    onClick = onResetAllClick,
                    style = MinecraftButtonStyle.RED,
                    text = "RESET ALL",
                    fontSize = 9.sp,
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // 6. Mode Switcher (IN-GAME vs IN-MENU)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (editGamepadKeyInGame) Color(0xFF3C8527) else Color(0xFF262628))
                    .border(
                        BorderStroke(1.dp, if (editGamepadKeyInGame) Color(0xFF55FF55) else Color(0xFF383838)),
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onEditGamepadKeyInGameChange(true) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IN-GAME BINDINGS",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = if (editGamepadKeyInGame) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (!editGamepadKeyInGame) Color(0xFF3C8527) else Color(0xFF262628))
                    .border(
                        BorderStroke(1.dp, if (!editGamepadKeyInGame) Color(0xFF55FF55) else Color(0xFF383838)),
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onEditGamepadKeyInGameChange(false) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IN-MENU BINDINGS",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = if (!editGamepadKeyInGame) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.5.sp
                )
            }
        }

        // 7. Individual Button Mapping Cards
        Text(
            text = "INDIVIDUAL BUTTON MAPPINGS (${if (editGamepadKeyInGame) "IN-GAME" else "IN-MENU"})",
            color = Color(0xFFAAAAAA),
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
        )

        GamepadMap.entries.forEach { btn ->
            val boundCodes = remember(btn, editGamepadKeyInGame, gamepadConfigRefreshed) {
                currentMapping?.findByMap(btn, inGame = editGamepadKeyInGame)?.toList() ?: emptyList()
            }
            val boundNames = if (boundCodes.isNotEmpty()) {
                val names = boundCodes.map { getNameByGamepadEvent(it) }
                names.joinToString(", ")
            } else {
                "Unbound (Click to assign)"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF242426))
                    .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                    .clickable { onSelectBinding(btn) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(btn.getIconRes()),
                        contentDescription = btn.name,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = btn.name.replace("_", " "),
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = boundNames,
                            color = if (boundCodes.isNotEmpty()) Color(0xFF55FF55) else Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 9.5.sp
                        )
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = "Edit",
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun JavaSettingsTab(
    selectedJava: String,
    onSelectJava: (String) -> Unit
) {
    val context = LocalContext.current
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
            text = "INSTALLED JAVA RUNTIMES",
            color = Color.White,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        val runtimes = remember { RuntimesManager.getRuntimes() }
        if (runtimes.isEmpty()) {
            Text(
                text = "No custom Java runtimes installed. Internal runtime will be used automatically.",
                color = Color(0xFF888888),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.5.sp
            )
        } else {
            runtimes.forEach { rt ->
                val isSelected = selectedJava == rt.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isSelected) Color(0xFF263820) else Color(0xFF242426))
                        .border(
                            BorderStroke(1.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable {
                            onSelectJava(rt.name)
                            AllSettings.javaRuntime.save(rt.name)
                            Toast.makeText(context, "Java Runtime set to ${rt.name}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = rt.name,
                            color = if (isSelected) Color(0xFF55FF55) else Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Java Version: ${rt.versionString ?: rt.javaVersion} • ${rt.arch ?: "default"}",
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                    if (isSelected) {
                        Text("ACTIVE", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutsSettingsTab(
    controlLayouts: List<ControlData>,
    activeControlLayoutName: String,
    controlsOpacity: Int,
    onControlsOpacityChange: (Int) -> Unit,
    onSelectLayout: (String) -> Unit,
    onLaunchEditor: () -> Unit,
    onCreateNewLayout: () -> Unit
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]

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
            text = "TOUCH CONTROLS & LAYOUT MANAGEMENT",
            color = Color.White,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Launch Full Control Editor Activity
        MinecraftButton(
            onClick = onLaunchEditor,
            style = MinecraftButtonStyle.GREEN,
            text = "LAUNCH ON-SCREEN CONTROL EDITOR",
            fontSize = 11.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )

        // Overall Touch Opacity Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Virtual Controls Opacity", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${controlsOpacity}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }
            Slider(
                value = controlsOpacity.toFloat(),
                onValueChange = { onControlsOpacityChange(it.toInt()) },
                onValueChangeFinished = {
                    AllSettings.controlsOpacity.save(controlsOpacity)
                },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        // Create New Layout & Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAVED LAYOUTS (${controlLayouts.size})",
                color = Color(0xFFAAAAAA),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            )
            MinecraftButton(
                onClick = onCreateNewLayout,
                style = MinecraftButtonStyle.STONE,
                text = "+ NEW LAYOUT",
                fontSize = 9.5.sp,
                modifier = Modifier.height(28.dp)
            )
        }

        // Layout List
        if (controlLayouts.isEmpty()) {
            Text(
                text = "No custom layouts found. Default layout will be used.",
                color = Color(0xFF888888),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.sp
            )
        } else {
            controlLayouts.forEach { data ->
                val isSelected = activeControlLayoutName == data.file.name
                val layoutName = if (data.isSupport) data.controlLayout.info.name.translate(locale).ifBlank { data.file.name } else data.file.name
                val authorName = if (data.isSupport) data.controlLayout.info.author.translate(locale).ifBlank { "Cubiq" } else "Cubiq"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) Color(0xFF263820) else Color(0xFF242426))
                        .border(
                            BorderStroke(1.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF383838)),
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            onSelectLayout(data.file.name)
                            AllSettings.controlLayout.save(data.file.name)
                            Toast.makeText(context, "Active Layout: $layoutName", Toast.LENGTH_SHORT).show()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = layoutName,
                            color = if (isSelected) Color(0xFF55FF55) else Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Author: $authorName • File: ${data.file.name}",
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 9.5.sp
                        )
                    }
                    if (isSelected) {
                        Text("ACTIVE", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF1E1E20))
            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.img_cubiq_emblem),
            contentDescription = "Cubiq Emblem",
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "CUBIQ LAUNCHER",
            color = Color.White,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Text(
            text = "Version 2.6.1 (Build 200043)",
            color = Color(0xFF55FF55),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.5.sp
        )

        Text(
            text = "Architecture: ${BuildKeys.BUILD_ARCH}",
            color = Color(0xFF888888),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.sp
        )

        Text(
            text = "Cubiq is an open-source Minecraft Java Edition launcher for Android built with Jetpack Compose.",
            color = Color(0xFFAAAAAA),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
