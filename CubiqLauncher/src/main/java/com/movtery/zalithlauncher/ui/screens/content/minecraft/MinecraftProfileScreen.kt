/*

 * Cubiq Launcher

 * Minecraft PC Launcher Profile & Game Settings Screen (Integrated Real Zalith Settings & Accounts)

 */



package com.movtery.zalithlauncher.ui.screens.content.minecraft



import android.content.Intent

import android.net.Uri

import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts

import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog

import com.movtery.zalithlauncher.utils.file.shareFile

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Image

import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.horizontalScroll

import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.interaction.collectIsPressedAsState

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.ui.draw.drawWithContent

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Shadow

import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.unit.IntOffset

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.layout.widthIn

import androidx.compose.foundation.rememberScrollState

import com.movtery.zalithlauncher.ui.toAndroidString

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
import androidx.compose.ui.graphics.SolidColor

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

import com.movtery.layer_controller.layout.loadLayoutFromFile

import com.movtery.layer_controller.layout.loadLayoutFromString

import com.movtery.layer_controller.utils.newRandomFileName

import com.movtery.layer_controller.utils.saveToFile

import com.movtery.zalithlauncher.BuildKeys

import com.movtery.zalithlauncher.R

import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.BasicTextField

import com.movtery.zalithlauncher.game.account.AccountsManager

import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper

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

import com.movtery.zalithlauncher.ui.theme.FontManager

import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager

import com.movtery.zalithlauncher.context.copyAssetFile

import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings

import com.movtery.zalithlauncher.viewmodel.GamepadViewModel

import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext

import java.io.File



private enum class SettingsTab(val title: String) {

    RENDERER("RENDERER"),

    GAME("GAME"),

    CONTROLS("CONTROLS"),

    GAMEPAD("GAMEPAD"),

    LAUNCHER("LAUNCHER"),

    JAVA("JAVA"),

    LAYOUTS("CONTROLS (LAYOUTS)"),

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

    var showAddElybyDialog by remember { mutableStateOf(false) }

    var showManageAccounts by remember { mutableStateOf(false) }

    var offlineUsername by remember { mutableStateOf("") }

    var accountToDelete by remember { mutableStateOf<Account?>(null) }

    var offlineAccountToManage by remember { mutableStateOf<Account?>(null) }

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

    var controlsButtonScale by remember { mutableIntStateOf(AllSettings.controlsButtonScale.state) }

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



    var selectedTab by remember { mutableStateOf(SettingsTab.RENDERER) }



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

    val allRenderers = remember {

        Renderers.init()

        Renderers.getRenderers()

    }

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



    val importControlsLauncher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.GetContent()

    ) { uri: Uri? ->

        if (uri != null) {

            scope.launch(Dispatchers.IO) {

                val stream = runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()

                if (stream != null) {

                    ControlManager.importControl(

                        inputStream = stream,

                        onSerializationError = {

                            scope.launch(Dispatchers.Main) {

                                Toast.makeText(context, "Invalid control layout JSON", Toast.LENGTH_SHORT).show()

                            }

                        },

                        catchedError = { e ->

                            scope.launch(Dispatchers.Main) {

                                Toast.makeText(context, "Failed to import controls: ${e.message}", Toast.LENGTH_SHORT).show()

                            }

                        },

                        onFinished = {

                            ControlManager.refresh()

                            scope.launch(Dispatchers.Main) {

                                Toast.makeText(context, "Control layout imported successfully!", Toast.LENGTH_SHORT).show()

                            }

                        }

                    )

                }

            }

        }

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

                onAddElybyClick = { showAddElybyDialog = true },

                onAddOfflineClick = { showAddOfflineDialog = true },

                onSelectAccount = { acc ->

                    AllSettings.currentAccount.save(acc.uniqueUUID)

                    AccountsManager.reloadAccounts()

                    Toast.makeText(context, "Switched to ${acc.username}", Toast.LENGTH_SHORT).show()

                },

                onDeleteAccount = { acc -> accountToDelete = acc },

                onManageOfflineAccount = { acc -> offlineAccountToManage = acc }

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

            val tabsScrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxWidth()) {

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .horizontalScroll(tabsScrollState),

                    horizontalArrangement = Arrangement.spacedBy(6.dp)

                ) {

                SettingsTab.entries.forEach { tab ->

                    val isTabSelected = selectedTab == tab

                    val interactionSource = remember { MutableInteractionSource() }

                    val isPressed by interactionSource.collectIsPressedAsState()



                    val outerBorder = if (isTabSelected) Color(0xFF1B4E12) else Color(0xFF38383C)

                    val topHighlight = if (isTabSelected) {

                        if (isPressed) Color(0xFF194411) else Color(0xFF5AC636)

                    } else {

                        if (isPressed) Color(0xFF1E1E22) else Color(0xFF3E3E44)

                    }

                    val bottomShadow = if (isTabSelected) {

                        if (isPressed) Color(0xFF5AC636) else Color(0xFF194411)

                    } else {

                        if (isPressed) Color(0xFF3E3E44) else Color(0xFF141416)

                    }

                    val bgGradient = if (isTabSelected) {

                        if (isPressed) listOf(Color(0xFF286D19), Color(0xFF205814))

                        else listOf(Color(0xFF388E23), Color(0xFF2C741B))

                    } else {

                        if (isPressed) listOf(Color(0xFF1A1A1E), Color(0xFF161618))

                        else listOf(Color(0xFF2A2A2E), Color(0xFF202024))

                    }



                    Box(

                        modifier = Modifier

                            .offset { IntOffset(0, if (isPressed) 2 else 0) }

                            .clip(RoundedCornerShape(3.dp))

                            .border(

                                BorderStroke(1.dp, outerBorder),

                                RoundedCornerShape(3.dp)

                            )

                            .background(Brush.verticalGradient(bgGradient))

                            .drawWithContent {

                                drawContent()

                                val highlightThick = 2.dp.toPx()

                                val shadowThick = 2.5.dp.toPx()

                                // Top highlight

                                drawRect(

                                    color = topHighlight,

                                    topLeft = Offset(0.5f, 0.5f),

                                    size = Size(size.width - 1f, highlightThick)

                                )

                                // Bottom shadow

                                drawRect(

                                    color = bottomShadow,

                                    topLeft = Offset(0.5f, size.height - shadowThick),

                                    size = Size(size.width - 1f, shadowThick)

                                )

                            }

                            .clickable(

                                interactionSource = interactionSource,

                                indication = null,

                                onClick = {

                                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                                    selectedTab = tab

                                }

                            )

                            .padding(horizontal = 14.dp, vertical = 7.dp),

                        contentAlignment = Alignment.Center

                    ) {

                        Text(

                            text = tab.title,

                            color = if (isTabSelected) Color.White else Color(0xFFCCCCCC),

                            fontFamily = MinecraftFontFamily,

                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.SemiBold,

                            fontSize = 11.5.sp,

                            maxLines = 1,

                            style = TextStyle(

                                shadow = Shadow(

                                    color = if (isTabSelected) Color(0xFF0F2B0A) else Color.Black,

                                    offset = Offset(1.5f, 1.5f),

                                    blurRadius = 0f

                                )

                            )

                        )

                    }

                }

            }



            if (tabsScrollState.canScrollForward) {

                    Box(

                        modifier = Modifier

                            .matchParentSize()

                            .background(

                                Brush.horizontalGradient(

                                    0.88f to Color.Transparent,

                                    1.0f to Color(0xFF141414)

                                )

                            )

                    )

                }

            }



            Spacer(modifier = Modifier.height(12.dp))



            // Settings Content based on selectedTab

            when (selectedTab) {

                SettingsTab.RENDERER -> {

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

                        onFullScreenChange = { isFullScreen = it }

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

                        controlsButtonScale = controlsButtonScale,

                        onControlsButtonScaleChange = { controlsButtonScale = it },

                        onSelectLayout = { name ->

                            activeControlLayoutName = name

                        },

                        onLaunchEditor = {

                            val defaultFile = File(PathManager.DIR_CONTROL_LAYOUTS, "mojo_layout.json")

                            val shouldOverwriteDefault = runCatching {

                                if (!defaultFile.exists()) true

                                else {

                                    val existing = loadLayoutFromFile(defaultFile)

                                    existing.info.versionCode < 10

                                }

                            }.getOrDefault(true)

                            if (shouldOverwriteDefault) {

                                runCatching {

                                    context.copyAssetFile("mojo_layout.json", defaultFile, true)

                                }

                            }



                            val activeControl = controlLayouts.find { it.file.name == activeControlLayoutName }

                                ?: controlLayouts.find { it.file.name == "mojo_layout.json" }

                                ?: controlLayouts.firstOrNull()

                            val activeFile = activeControl?.file

                                ?: defaultFile

                            startEditorActivity(context, activeFile)

                        },

                        onCreateNewLayout = { showCreateLayoutDialog = true },

                        onImportControls = { importControlsLauncher.launch("application/json") },

                        onExportLayout = { data -> shareFile(context, data.file) }

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



        if (showAddElybyDialog) {

            ElybyLoginDialog(

                onDismiss = { showAddElybyDialog = false },

                onSuccess = { showAddElybyDialog = false }

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



        offlineAccountToManage?.let { acc ->

            ManageOfflineAccountDialog(

                account = acc,

                onDismiss = { offlineAccountToManage = null },

                onNavigateToSkins = {

                    offlineAccountToManage = null

                    com.movtery.zalithlauncher.ui.screens.content.LauncherNavState.selectedBottomNav = 1

                    com.movtery.zalithlauncher.ui.screens.content.LauncherNavState.activeTab = 1

                }

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

fun MicrosoftLoginDialog(

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

fun ElybyLoginDialog(

    onDismiss: () -> Unit,

    onSuccess: () -> Unit

) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var emailOrUsername by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }



    AlertDialog(

        onDismissRequest = { if (!isLoading) onDismiss() },

        containerColor = Color(0xFF1C1C1E),

        title = {

            Row(

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                Box(

                    modifier = Modifier

                        .size(24.dp)

                        .clip(RoundedCornerShape(4.dp))

                        .background(Color(0xFF0284C7)),

                    contentAlignment = Alignment.Center

                ) {

                    Text("E", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                }

                Text(

                    text = "ADD ELY.BY ACCOUNT",

                    color = Color.White,

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 15.sp

                )

            }

        },

        text = {

            Column(

                modifier = Modifier.fillMaxWidth(),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                Text(

                    text = "Sign in with your Ely.by username/email and password for custom skins, capes, and authlib integration:",

                    color = Color(0xFFCCCCCC),

                    fontFamily = MinecraftFontFamily,

                    fontSize = 11.5.sp

                )



                OutlinedTextField(

                    value = emailOrUsername,

                    onValueChange = { emailOrUsername = it; errorMessage = null },

                    placeholder = { Text("Username or Email", color = Color(0xFF666666), fontSize = 12.sp) },

                    singleLine = true,

                    enabled = !isLoading,

                    modifier = Modifier.fillMaxWidth(),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedContainerColor = Color(0xFF242426),

                        unfocusedContainerColor = Color(0xFF242426),

                        focusedBorderColor = Color(0xFF0284C7),

                        unfocusedBorderColor = Color(0xFF383838),

                        focusedTextColor = Color.White,

                        unfocusedTextColor = Color.White

                    )

                )



                OutlinedTextField(

                    value = password,

                    onValueChange = { password = it; errorMessage = null },

                    placeholder = { Text("Password", color = Color(0xFF666666), fontSize = 12.sp) },

                    singleLine = true,

                    enabled = !isLoading,

                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),

                    trailingIcon = {

                        Text(

                            text = if (passwordVisible) "HIDE" else "SHOW",

                            color = Color(0xFF0284C7),

                            fontFamily = MinecraftFontFamily,

                            fontSize = 10.sp,

                            modifier = Modifier

                                .clickable { passwordVisible = !passwordVisible }

                                .padding(end = 8.dp)

                        )

                    },

                    modifier = Modifier.fillMaxWidth(),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedContainerColor = Color(0xFF242426),

                        unfocusedContainerColor = Color(0xFF242426),

                        focusedBorderColor = Color(0xFF0284C7),

                        unfocusedBorderColor = Color(0xFF383838),

                        focusedTextColor = Color.White,

                        unfocusedTextColor = Color.White

                    )

                )



                if (errorMessage != null) {

                    Text(

                        text = errorMessage ?: "",

                        color = Color(0xFFFF5555),

                        fontFamily = MinecraftFontFamily,

                        fontSize = 11.sp

                    )

                }



                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Text(

                        text = "Don't have an account?",

                        color = Color(0xFF888888),

                        fontFamily = MinecraftFontFamily,

                        fontSize = 10.5.sp

                    )

                    Text(

                        text = "Register at Ely.by ↗",

                        color = Color(0xFF38BDF8),

                        fontFamily = MinecraftFontFamily,

                        fontSize = 10.5.sp,

                        fontWeight = FontWeight.Bold,

                        modifier = Modifier.clickable {

                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ely.by/registration")).apply {

                                flags = Intent.FLAG_ACTIVITY_NEW_TASK

                            }

                            context.startActivity(intent)

                        }

                    )

                }



                if (isLoading) {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.Center,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        CircularProgressIndicator(

                            color = Color(0xFF0284C7),

                            strokeWidth = 2.dp,

                            modifier = Modifier.size(20.dp)

                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(

                            text = "Authenticating with Ely.by...",

                            color = Color(0xFFCCCCCC),

                            fontFamily = MinecraftFontFamily,

                            fontSize = 11.sp

                        )

                    }

                }

            }

        },

        confirmButton = {

            MinecraftButton(

                onClick = {

                    val trimmedUser = emailOrUsername.trim()

                    if (trimmedUser.isBlank() || password.isBlank()) {

                        errorMessage = "Please enter username/email and password"

                        return@MinecraftButton

                    }

                    isLoading = true

                    errorMessage = null

                    val helper = AuthServerHelper(

                        baseUrl = "https://authserver.ely.by/api/authlib-injector",

                        serverName = "Ely.by",

                        email = trimmedUser,

                        password = password,

                        onSuccess = { acc, task ->

                            task.updateMessage(com.movtery.zalithlauncher.ui.androidText(R.string.account_logging_in_saving))

                            acc.downloadYggdrasil()

                            AccountsManager.suspendSaveAccount(acc)

                            AccountsManager.setCurrentAccount(acc)

                            withContext(Dispatchers.Main) {

                                isLoading = false

                                Toast.makeText(context, "Logged in as ${acc.username}!", Toast.LENGTH_SHORT).show()

                                onSuccess()

                            }

                        },

                        onFailed = { th ->

                            scope.launch(Dispatchers.Main) {

                                isLoading = false

                                errorMessage = th.localizedMessage ?: "Authentication failed"

                            }

                        }

                    )

                    helper.createNewAccount(context) { profiles, select ->

                        if (profiles.isNotEmpty()) {

                            select(profiles.first())

                        }

                    }

                },

                style = MinecraftButtonStyle.GREEN,

                text = if (isLoading) "WAIT..." else "LOGIN",

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

private fun AddOfflineAccountDialog(

    username: String,

    onUsernameChange: (String) -> Unit,

    onDismiss: () -> Unit,

    onCreated: () -> Unit

) {

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

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

                    val trimmed = username.trim()

                    if (trimmed.isNotBlank()) {

                        coroutineScope.launch {

                            val account = Account(

                                username = trimmed,

                                accountType = com.movtery.zalithlauncher.game.account.AccountType.LOCAL.tag

                            )

                            withContext(Dispatchers.IO) {

                                AccountsManager.suspendSaveAccount(account)

                            }

                            AccountsManager.setCurrentAccount(account)

                            Toast.makeText(context, "Account '$trimmed' added!", Toast.LENGTH_SHORT).show()

                            onCreated()

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
private fun ManageOfflineAccountDialog(

    account: Account,

    onDismiss: () -> Unit,

    onNavigateToSkins: () -> Unit

) {

    val context = LocalContext.current

    var username by remember { mutableStateOf(account.username) }



    Dialog(onDismissRequest = onDismiss) {

        Column(

            modifier = Modifier

                .fillMaxWidth(0.92f)

                .clip(RoundedCornerShape(4.dp))

                .background(Color(0xFF1E1E20))

                .border(BorderStroke(2.dp, Color(0xFF3C8527)), RoundedCornerShape(4.dp))

                .padding(18.dp)

        ) {

            Text(

                text = "MANAGE OFFLINE ACCOUNT",

                color = Color(0xFF55FF55),

                fontFamily = MinecraftFontFamily,

                fontWeight = FontWeight.Bold,

                fontSize = 15.sp

            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(

                text = "Edit username or customize skins and capes for '${account.username}':",

                color = Color(0xFFAAAAAA),

                fontFamily = MinecraftFontFamily,

                fontSize = 11.sp

            )



            Spacer(modifier = Modifier.height(14.dp))



            Text(

                text = "PLAYER USERNAME",

                color = Color.White,

                fontFamily = MinecraftFontFamily,

                fontWeight = FontWeight.Bold,

                fontSize = 11.sp

            )

            Spacer(modifier = Modifier.height(4.dp))

            BasicTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(Color(0xFF55FF55)),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(Color(0xFF141416), RoundedCornerShape(2.dp))
                            .border(BorderStroke(1.dp, Color(0xFF4A4A50)), RoundedCornerShape(2.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (username.isEmpty()) {
                            Text("Enter username", color = Color(0xFF666666), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                }
            )



            Spacer(modifier = Modifier.height(14.dp))



            // Action: CUSTOMIZE YOUR SKINS AND CAPES

            MinecraftButton(

                onClick = {

                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                    onNavigateToSkins()

                },

                style = MinecraftButtonStyle.GREEN,

                text = "CUSTOMIZE SKINS & CAPES",

                modifier = Modifier

                    .fillMaxWidth()

                    .height(38.dp)

            )



            Spacer(modifier = Modifier.height(12.dp))



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                MinecraftButton(

                    onClick = onDismiss,

                    style = MinecraftButtonStyle.STONE,

                    text = "CANCEL",

                    modifier = Modifier

                        .weight(1f)

                        .height(36.dp)

                )



                MinecraftButton(

                    onClick = {

                        val trimmed = username.trim()

                        if (trimmed.isNotBlank()) {

                            account.username = trimmed

                            account.profileId = com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel(

                                trimmed,

                                account.skinModelType

                            )

                            AccountsManager.saveAccount(account)

                            AccountsManager.refreshWardrobe()

                            Toast.makeText(context, "Username updated to $trimmed", Toast.LENGTH_SHORT).show()

                            onDismiss()

                        } else {

                            Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()

                        }

                    },

                    style = MinecraftButtonStyle.GREEN,

                    text = "SAVE",

                    modifier = Modifier

                        .weight(1f)

                        .height(36.dp)

                )

            }

        }

    }

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

                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                    scope.launch(Dispatchers.IO) {

                        val isCurrent = AllSettings.currentAccount.getValue() == account.uniqueUUID

                        AccountsManager.deleteAccount(account)

                        if (isCurrent) {

                            val remaining = AccountsManager.accountsFlow.value.filter { it.uniqueUUID != account.uniqueUUID }

                            val next = remaining.firstOrNull()

                            if (next != null) {

                                AccountsManager.setCurrentAccount(next)

                            } else {

                                AllSettings.currentAccount.save("")

                            }

                        }

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

                onClick = {

                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                    onDismiss()

                },

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

    var selectedTemplate by remember { mutableStateOf("Plain Java Controls") }

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

                    placeholder = { Text("Layout Name (e.g. PvP, Bedrock Touch)", color = Color(0xFF666666), fontSize = 12.sp) },

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



                Spacer(modifier = Modifier.height(4.dp))

                Text(

                    text = "Base Layout Template:",

                    color = Color(0xFFCCCCCC),

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 11.5.sp

                )



                listOf("Plain Java Controls", "Pocket Edition Controls").forEach { template ->

                    val isSel = selectedTemplate == template

                    Row(

                        modifier = Modifier

                            .fillMaxWidth()

                            .clip(RoundedCornerShape(2.dp))

                            .background(if (isSel) Color(0xFF263820) else Color(0xFF242426))

                            .border(

                                BorderStroke(1.dp, if (isSel) Color(0xFF55FF55) else Color(0xFF383838)),

                                RoundedCornerShape(2.dp)

                            )

                            .clickable { selectedTemplate = template }

                            .padding(horizontal = 10.dp, vertical = 6.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(

                                text = template,

                                color = if (isSel) Color(0xFF55FF55) else Color.White,

                                fontFamily = MinecraftFontFamily,

                                fontWeight = FontWeight.Bold,

                                fontSize = 11.sp

                            )

                            Text(

                                text = if (template == "Pocket Edition Controls") "Bedrock mobile controls layout (~65% opacity)" else "Standard PC keyboard on-screen buttons",

                                color = Color(0xFF888888),

                                fontFamily = MinecraftFontFamily,

                                fontSize = 9.sp

                            )

                        }

                        RadioButton(

                            selected = isSel,

                            onClick = { selectedTemplate = template },

                            colors = RadioButtonDefaults.colors(

                                selectedColor = Color(0xFF55FF55),

                                unselectedColor = Color(0xFF666666)

                            )

                        )

                    }

                }

            }

        },

        confirmButton = {

            MinecraftButton(

                onClick = {

                    if (layoutName.isNotBlank()) {

                        scope.launch(Dispatchers.IO) {

                            val file = File(PathManager.DIR_CONTROL_LAYOUTS, "${newRandomFileName()}.json")

                            try {

                                val assetName = if (selectedTemplate == "Pocket Edition Controls") "pocket_edition_layout.json" else "mojo_layout.json"

                                val layout = runCatching {

                                    val jsonStr = context.assets.open(assetName).bufferedReader().use { it.readText() }

                                    val base = loadLayoutFromString(jsonStr)

                                    base.copy(

                                        info = base.info.copy(

                                            name = createTranslatable(default = layoutName.trim()),

                                            author = createTranslatable(default = "Player"),

                                            versionName = "1.0"

                                        )

                                    )

                                }.getOrElse {

                                    EmptyControlLayout.copy(

                                        info = EmptyLayoutInfo.copy(

                                            name = createTranslatable(default = layoutName.trim()),

                                            author = createTranslatable(default = "Player"),

                                            versionName = "1.0"

                                        )

                                    )

                                }

                                layout.saveToFile(file)

                                ControlManager.refresh()

                                withContext(Dispatchers.Main) {

                                    Toast.makeText(context, "Layout '${layoutName.trim()}' created from $selectedTemplate!", Toast.LENGTH_SHORT).show()

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

    onAddElybyClick: () -> Unit,

    onAddOfflineClick: () -> Unit,

    onSelectAccount: (Account) -> Unit,

    onDeleteAccount: (Account) -> Unit,

    onManageOfflineAccount: (Account) -> Unit = {}

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

                horizontalArrangement = Arrangement.spacedBy(6.dp)

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

                    onClick = onAddElybyClick,

                    style = MinecraftButtonStyle.STONE,

                    text = "+ ELY.BY",

                    fontSize = 9.5.sp,

                    modifier = Modifier

                        .weight(0.9f)

                        .height(38.dp)

                )



                MinecraftButton(

                    onClick = onAddOfflineClick,

                    style = MinecraftButtonStyle.STONE,

                    text = "+ OFFLINE",

                    fontSize = 9.5.sp,

                    modifier = Modifier

                        .weight(0.9f)

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



                    Row(

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.spacedBy(6.dp)

                    ) {

                        val context = LocalContext.current
                        val isMs = acc.isMicrosoftAccount()

                        val isEly = acc.otherBaseUrl?.contains("ely.by", ignoreCase = true) == true || acc.accountType?.contains("Ely.by", ignoreCase = true) == true



                        // Manage Button

                        Box(

                            modifier = Modifier

                                .height(26.dp)

                                .clip(RoundedCornerShape(2.dp))

                                .background(Color(0xFF2C2C30))

                                .border(BorderStroke(1.dp, Color(0xFF4A4A50)), RoundedCornerShape(2.dp))

                                .clickable {

                                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                                    when {

                                        isMs -> {

                                            runCatching {

                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://account.microsoft.com/")).apply {

                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                                }

                                                context.startActivity(intent)

                                            }

                                        }

                                        isEly -> {

                                            runCatching {

                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ely.by/")).apply {

                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                                }

                                                context.startActivity(intent)

                                            }

                                        }

                                        else -> {

                                            onManageOfflineAccount(acc)

                                        }

                                    }

                                }

                                .padding(horizontal = 7.dp),

                            contentAlignment = Alignment.Center

                        ) {

                            Text(

                                text = "MANAGE",

                                color = Color.White,

                                fontFamily = MinecraftFontFamily,

                                fontWeight = FontWeight.Bold,

                                fontSize = 10.sp

                            )

                        }



                        // Logo on the left of red bin

                        if (isMs) {

                            Image(

                                painter = painterResource(R.drawable.ic_microsoft_logo),

                                contentDescription = "Microsoft Account",

                                modifier = Modifier.size(18.dp)

                            )

                        } else if (isEly) {

                            Image(

                                painter = painterResource(R.drawable.ic_elyby_logo),

                                contentDescription = "Ely.by Account",

                                modifier = Modifier.size(18.dp)

                            )

                        }



                        // Red Delete Bin

                        Box(

                            modifier = Modifier

                                .size(32.dp)

                                .clip(RoundedCornerShape(2.dp))

                                .clickable {

                                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                                    onDeleteAccount(acc)

                                },

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

    var showAdvancedGameOptions by remember { mutableStateOf(false) }



    Column(

        modifier = Modifier

            .fillMaxWidth()

            .clip(RoundedCornerShape(3.dp))

            .background(Color(0xFF1E1E20))

            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))

            .padding(14.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)

    ) {

        // 1. RAM Allocation (Presets + Fine Slider)

        Column {

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Text("RAM Allocation", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Text("${ramValue.toInt()} MB (${String.format(java.util.Locale.ROOT, "%.1f", ramValue / 1024f)} GB)", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            }

            Spacer(modifier = Modifier.height(8.dp))



            // Quick RAM Presets (Beginner Friendly)

            val ramPresets = listOf(1536f, 2048f, 3072f, 4096f, 6144f).filter { it <= maxRam }

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.spacedBy(6.dp)

            ) {

                ramPresets.forEach { presetMb ->

                    val isPresetSel = kotlin.math.abs(ramValue - presetMb) < 64f

                    Box(

                        modifier = Modifier

                            .weight(1f)

                            .clip(RoundedCornerShape(2.dp))

                            .background(if (isPresetSel) Color(0xFF2E6930) else Color(0xFF28282A))

                            .border(BorderStroke(1.dp, if (isPresetSel) Color(0xFF55FF55) else Color(0xFF404040)), RoundedCornerShape(2.dp))

                            .clickable {

                                onRamChange(presetMb)

                                AllSettings.ramAllocation.save(presetMb.toInt())

                            }

                            .padding(vertical = 6.dp),

                        contentAlignment = Alignment.Center

                    ) {

                        Text(

                            text = if (presetMb >= 1024f) "${(presetMb / 1024f).toInt()} GB" else "${presetMb.toInt()} MB",

                            color = if (isPresetSel) Color(0xFF55FF55) else Color.White,

                            fontFamily = MinecraftFontFamily,

                            fontWeight = FontWeight.Bold,

                            fontSize = 10.sp

                        )

                    }

                }

            }



            Spacer(modifier = Modifier.height(8.dp))

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



        // Show Startup Loading Dialog
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Show Startup Loading Dialog", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Display a Minecraft-styled loading popup while the game initializes", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)
            }
            Switch(
                checked = AllSettings.showGameLoadingDialog.state,
                onCheckedChange = {
                    AllSettings.showGameLoadingDialog.save(it)
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

                Text("Isolate mods, resource packs and configs per version", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)

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

                Text("Fast Launch (Skip Verification)", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Text("Dramatically speeds up startup by skipping SHA-1 hash checks", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 10.sp)

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



        // Collapsible Advanced JVM & Debug Settings (Accordion)

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .clip(RoundedCornerShape(2.dp))

                .background(Color(0xFF262629))

                .border(BorderStroke(1.dp, Color(0xFF444448)), RoundedCornerShape(2.dp))

                .clickable { showAdvancedGameOptions = !showAdvancedGameOptions }

                .padding(horizontal = 10.dp, vertical = 9.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(

                    text = if (showAdvancedGameOptions) "▼" else "▶",

                    color = Color(0xFF55FF55),

                    fontFamily = MinecraftFontFamily,

                    fontSize = 10.sp

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    text = "ADVANCED JVM & CONSOLE OPTIONS",

                    color = Color.White,

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 11.5.sp

                )

            }

            Text(

                text = if (showAdvancedGameOptions) "Hide" else "Show",

                color = Color(0xFF888888),

                fontFamily = MinecraftFontFamily,

                fontSize = 10.sp

            )

        }



        if (showAdvancedGameOptions) {

            Column(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(top = 4.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                // Custom MB Manual Entry

                var manualMbText by remember(ramValue) { mutableStateOf(ramValue.toInt().toString()) }

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(8.dp),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    OutlinedTextField(

                        value = manualMbText,

                        onValueChange = { str ->

                            manualMbText = str.filter { it.isDigit() }

                        },

                        label = { Text("Exact Memory (MB)", color = Color(0xFF888888), fontSize = 10.sp) },

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

                    MinecraftButton(

                        onClick = {

                            val num = manualMbText.toIntOrNull()

                            if (num != null) {

                                val clamped = num.toFloat().coerceIn(256f, maxRam)

                                onRamChange(clamped)

                                AllSettings.ramAllocation.save(clamped.toInt())

                                Toast.makeText(context, "Memory set to ${clamped.toInt()} MB", Toast.LENGTH_SHORT).show()

                            }

                        },

                        style = MinecraftButtonStyle.GREEN,

                        text = "SET MB",

                        fontSize = 10.5.sp,

                        modifier = Modifier

                            .width(76.dp)

                            .height(38.dp)

                    )

                }



                // Custom JVM Arguments

                Column {

                    Text("Custom JVM Arguments", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

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



                // Version Custom Subtitle

                Column {

                    Text("Version Custom Subtitle", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

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



                // Auto Show Log

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Show Console Log on Launch", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Text("Displays runtime log window during game execution", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)

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



                // Log Text Size

                Column {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Text("Console Log Text Size", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 11.5.sp)

                        Text("${logTextSize.toInt()} sp", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 11.5.sp)

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



                // Log Buffer Flush Interval

                var flushInterval by remember { mutableFloatStateOf(AllSettings.logBufferFlushInterval.state.toFloat()) }

                Column {

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Text("Log Buffer Flush Interval", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 11.5.sp)

                        Text("${flushInterval.toInt()} ms", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 11.5.sp)

                    }

                    Slider(

                        value = flushInterval,

                        onValueChange = { flushInterval = it },

                        onValueChangeFinished = {

                            AllSettings.logBufferFlushInterval.save(flushInterval.toInt())

                        },

                        valueRange = 100f..1000f,

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

    val context = LocalContext.current

    var showAdvancedVideoOptions by remember { mutableStateOf(false) }



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

            Text("Graphics Renderer", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)

            Spacer(modifier = Modifier.height(6.dp))

            var showCustomRendererDialog by remember { mutableStateOf(false) }

            if (showCustomRendererDialog) {
                SimpleAlertDialog(
                    title = "Download Custom Renderers",
                    text = "How to Install Custom Renderers:\n\n" +
                            "1. Tap 'Go to GitHub' below to open the FCL Renderer Plugin releases page.\n\n" +
                            "2. Download the renderer APK you need (such as LTW, Zink, MobileGlues, Angle, VirGL).\n\n" +
                            "3. Install the APK on your device and launch it once to complete setup if needed.\n\n" +
                            "4. Close and reopen Cubiq Launcher. The new renderer will automatically appear in your renderers list!",
                    confirmText = "Go to GitHub",
                    dismissText = androidx.compose.ui.res.stringResource(com.movtery.zalithlauncher.R.string.generic_cancel),
                    onConfirm = {
                        showCustomRendererDialog = false
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ShirosakiMio/FCLRendererPlugin/releases")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    },
                    onDismiss = {
                        showCustomRendererDialog = false
                    }
                )
            }

            MinecraftButton(
                onClick = { showCustomRendererDialog = true },
                style = MinecraftButtonStyle.STONE,
                text = "DOWNLOAD CUSTOM RENDERERS",
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                            com.movtery.zalithlauncher.game.renderer.Renderers.setCurrentRenderer(renderer.getUniqueIdentifier())
                            com.movtery.zalithlauncher.game.version.installed.VersionsManager.currentVersion.value?.let { curVer ->
                                runCatching {
                                    curVer.getVersionConfig().apply {
                                        this.renderer = renderer.getUniqueIdentifier()
                                        saveWithThrowable()
                                    }
                                }
                            }
                        }

                        .padding(horizontal = 10.dp, vertical = 8.dp),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(

                                text = renderer.getRendererName(),

                                color = if (isSel) Color(0xFF55FF55) else Color.White,

                                fontFamily = MinecraftFontFamily,

                                fontWeight = FontWeight.Bold,

                                fontSize = 12.sp

                            )

                            if (renderer.isRecommended()) {

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(

                                    modifier = Modifier

                                        .clip(RoundedCornerShape(3.dp))

                                        .background(Color(0xFF2E6930))

                                        .padding(horizontal = 5.dp, vertical = 1.dp)

                                ) {

                                    Text(

                                        text = "RECOMMENDED",

                                        color = Color(0xFF55FF55),

                                        fontFamily = MinecraftFontFamily,

                                        fontWeight = FontWeight.Bold,

                                        fontSize = 8.sp

                                    )

                                }

                            }

                        }

                        Text(

                            text = renderer.getCompatibilityDescription(),

                            color = if (isSel) Color(0xFFCCCCCC) else Color(0xFF888888),

                            fontFamily = MinecraftFontFamily,

                            fontSize = 10.sp,

                            lineHeight = 14.sp

                        )

                    }

                    RadioButton(
                        selected = isSel,
                        onClick = {
                            onRendererChange(renderer.getUniqueIdentifier())
                            AllSettings.renderer.save(renderer.getUniqueIdentifier())
                            com.movtery.zalithlauncher.game.renderer.Renderers.setCurrentRenderer(renderer.getUniqueIdentifier())
                            com.movtery.zalithlauncher.game.version.installed.VersionsManager.currentVersion.value?.let { curVer ->
                                runCatching {
                                    curVer.getVersionConfig().apply {
                                        this.renderer = renderer.getUniqueIdentifier()
                                        saveWithThrowable()
                                    }
                                }
                            }
                        },

                        colors = RadioButtonDefaults.colors(

                            selectedColor = Color(0xFF55FF55),

                            unselectedColor = Color(0xFF666666)

                        )

                    )

                }

                Spacer(modifier = Modifier.height(4.dp))

            }



            if (selectedRendererId == com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.getUniqueIdentifier()) {
                val isInstalled = com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.isInstalled(context)
                MinecraftButton(
                    onClick = {
                        runCatching {
                            if (isInstalled) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer.PACKAGE_NAME)
                                if (launchIntent != null) {
                                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(launchIntent)
                                }
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(com.movtery.zalithlauncher.path.URL_GITHUB_MOBILEGLUES_RELEASES)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            }
                        }
                    },
                    style = if (isInstalled) MinecraftButtonStyle.GREEN else MinecraftButtonStyle.STONE,
                    text = if (isInstalled) "MOBILEGLUES INSTALLED (OPEN SETTINGS)" else "DOWNLOAD MOBILEGLUES COMPANION APP",
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                )
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



        // 4. Sustained Performance Mode

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



        // Collapsible Advanced Video & Driver Settings (Accordion)

        Row(

            modifier = Modifier

                .fillMaxWidth()

                .clip(RoundedCornerShape(2.dp))

                .background(Color(0xFF262629))

                .border(BorderStroke(1.dp, Color(0xFF444448)), RoundedCornerShape(2.dp))

                .clickable { showAdvancedVideoOptions = !showAdvancedVideoOptions }

                .padding(horizontal = 10.dp, vertical = 9.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(

                    text = if (showAdvancedVideoOptions) "▼" else "▶",

                    color = Color(0xFF55FF55),

                    fontFamily = MinecraftFontFamily,

                    fontSize = 10.sp

                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(

                    text = "ADVANCED GRAPHICS & DRIVERS",

                    color = Color.White,

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 11.5.sp

                )

            }

            Text(

                text = if (showAdvancedVideoOptions) "Hide" else "Show",

                color = Color(0xFF888888),

                fontFamily = MinecraftFontFamily,

                fontSize = 10.sp

            )

        }



        if (showAdvancedVideoOptions) {

            Column(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(top = 4.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                // Custom Resolution

                Column {

                    Text("Custom Resolution (Width x Height)", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

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



                // SurfaceView vs TextureView

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Use SurfaceView Rendering", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Text("Improves frame pacing and reduces battery drain", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)

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



                // Vulkan Driver & Turnip Selection

                Column {

                    val context = LocalContext.current

                    val importDriverLauncher = rememberLauncherForActivityResult(

                        contract = ActivityResultContracts.GetContent()

                    ) { uri: Uri? ->

                        if (uri != null) {

                            val customDir = File(context.filesDir, "custom_drivers").apply { mkdirs() }

                            val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "custom_turnip.so"

                            val target = File(customDir, name)

                            runCatching {

                                context.contentResolver.openInputStream(uri)?.use { input ->

                                    target.outputStream().use { output -> input.copyTo(output) }

                                }

                                DriverPluginManager.initDriver(context)

                                onVulkanDriverChange(name)

                                AllSettings.vulkanDriver.save(name)

                                Toast.makeText(context, "Loaded custom driver: $name", Toast.LENGTH_SHORT).show()

                            }.onFailure { e ->

                                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()

                            }

                        }

                    }



                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Text("Turnip Vulkan Driver", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Box(

                            modifier = Modifier

                                .clip(RoundedCornerShape(3.dp))

                                .background(Color(0xFF2E6930))

                                .clickable { importDriverLauncher.launch("*/*") }

                                .padding(horizontal = 8.dp, vertical = 4.dp)

                        ) {

                            Text("+ IMPORT TURNIP", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp)

                        }

                    }

                    Spacer(modifier = Modifier.height(6.dp))



                    val drivers = remember {

                        DriverPluginManager.initDriver(context)

                        DriverPluginManager.getDriverList()

                    }

                    for (driver in drivers) {

                        val isDriverSel = selectedVulkanDriver == driver.id || (selectedVulkanDriver.isBlank() && driver.isLauncher)

                        Row(

                            modifier = Modifier

                                .fillMaxWidth()

                                .clip(RoundedCornerShape(2.dp))

                                .background(if (isDriverSel) Color(0xFF263820) else Color(0xFF242426))

                                .border(

                                    BorderStroke(1.dp, if (isDriverSel) Color(0xFF55FF55) else Color(0xFF383838)),

                                    RoundedCornerShape(2.dp)

                                )

                                .clickable {

                                    onVulkanDriverChange(driver.id)

                                    AllSettings.vulkanDriver.save(driver.id)

                                }

                                .padding(horizontal = 10.dp, vertical = 7.dp),

                            horizontalArrangement = Arrangement.SpaceBetween,

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Column(modifier = Modifier.weight(1f)) {

                                Text(

                                    text = driver.name.ifBlank { driver.id },

                                    color = if (isDriverSel) Color(0xFF55FF55) else Color.White,

                                    fontFamily = MinecraftFontFamily,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 11.5.sp

                                )

                                Text(

                                    text = driver.summary ?: if (driver.isLauncher) "Bundled Turnip driver for Adreno GPUs" else "Custom driver: ${driver.path}",

                                    color = if (isDriverSel) Color(0xFFCCCCCC) else Color(0xFF888888),

                                    fontFamily = MinecraftFontFamily,

                                    fontSize = 9.5.sp

                                )

                            }

                            RadioButton(

                                selected = isDriverSel,

                                onClick = {

                                    onVulkanDriverChange(driver.id)

                                    AllSettings.vulkanDriver.save(driver.id)

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



                // Use System Vulkan Driver

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Use System Vulkan Driver", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Text("Bypasses Turnip and uses Qualcomm/Mali system driver", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)

                    }

                    Switch(

                        checked = zinkPreferSystemDriver,

                        onCheckedChange = {

                            onZinkPreferSystemDriverChange(it)

                            AllSettings.zinkPreferSystemDriver.save(it)

                        },

                        colors = SwitchDefaults.colors(

                            checkedThumbColor = Color.White,

                            checkedTrackColor = Color(0xFF3C8527),

                            uncheckedThumbColor = Color(0xFF888888),

                            uncheckedTrackColor = Color(0xFF333333)

                        )

                    )

                }



                // Vsync in Zink

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Vsync (Zink)", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Text("Syncs frame rate to screen refresh rate", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)

                    }

                    Switch(

                        checked = vsyncInZink,

                        onCheckedChange = {

                            onVsyncInZinkChange(it)

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



                // Shader Logging / Dump

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text("Shader Logging & Dump", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)

                        Text("Dumps compiled shaders to storage for debugging", color = Color(0xFF777777), fontFamily = MinecraftFontFamily, fontSize = 9.5.sp)

                    }

                    Switch(

                        checked = dumpShaders,

                        onCheckedChange = {

                            onDumpShadersChange(it)

                            AllSettings.dumpShaders.save(it)

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

    onFullScreenChange: (Boolean) -> Unit

) {

    Column(

        modifier = Modifier

            .fillMaxWidth()

            .clip(RoundedCornerShape(3.dp))

            .background(Color(0xFF1E1E20))

            .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(3.dp))

            .padding(14.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)

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



        Box(

            modifier = Modifier

                .fillMaxWidth()

                .height(1.dp)

                .background(Color(0xFF2E2E30))

        )



        // Launcher Font Customizer

        Column {

            val context = LocalContext.current

            val importFontLauncher = rememberLauncherForActivityResult(

                contract = ActivityResultContracts.GetContent()

            ) { uri: Uri? ->

                if (uri != null) {

                    val fontDir = File(context.filesDir, "fonts").apply { mkdirs() }

                    val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':') ?: "custom_font.ttf"

                    val target = File(fontDir, name)

                    runCatching {

                        context.contentResolver.openInputStream(uri)?.use { input ->

                            target.outputStream().use { output -> input.copyTo(output) }

                        }

                        FontManager.setFont(FontManager.FONT_CUSTOM, target.absolutePath)

                        Toast.makeText(context, "Loaded custom font: $name", Toast.LENGTH_SHORT).show()

                    }.onFailure { e ->

                        Toast.makeText(context, "Failed to load font: ${e.message}", Toast.LENGTH_SHORT).show()

                    }

                }

            }



            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically

            ) {

                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {

                    Text("Launcher Font Style", color = Color.White, fontFamily = MinecraftFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)

                    Text("Select a preset typography or load custom font", color = Color(0xFF888888), fontFamily = MinecraftFontFamily, fontSize = 10.sp, lineHeight = 14.sp)

                }

                Box(

                    modifier = Modifier

                        .clip(RoundedCornerShape(3.dp))

                        .background(Color(0xFF2E6930))

                        .clickable { importFontLauncher.launch("*/*") }

                        .padding(horizontal = 8.dp, vertical = 4.dp)

                ) {

                    Text(

                        "+ BROWSE FONT",

                        color = Color(0xFF55FF55),

                        fontFamily = MinecraftFontFamily,

                        fontWeight = FontWeight.Bold,

                        fontSize = 9.sp,

                        maxLines = 1,

                        softWrap = false

                    )

                }

            }

            Spacer(modifier = Modifier.height(8.dp))



            val fontOptions = listOf(

                Triple(FontManager.FONT_MINECRAFT, "Minecraft Pixel", "Authentic retro Minecraft pixel font"),

                Triple(FontManager.FONT_SANS, "Modern Sans", "Clean, high-legibility modern sans-serif font"),

                Triple(FontManager.FONT_MONOSPACE, "Monospace Code", "Developer monospaced terminal font"),

                Triple(FontManager.FONT_SERIF, "Serif Classical", "Book-style elegant serif typeface"),

                Triple(FontManager.FONT_SYSTEM, "System Default", "Native Android operating system font"),

                Triple(FontManager.FONT_CUSTOM, "Custom Font", FontManager.customFontPath.ifBlank { "Tap to choose a .ttf or .otf file from storage" })

            )



            fontOptions.forEach { (type, name, desc) ->

                val isFontSel = FontManager.currentFontType == type

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .clip(RoundedCornerShape(2.dp))

                        .background(if (isFontSel) Color(0xFF263820) else Color(0xFF242426))

                        .border(

                            BorderStroke(1.dp, if (isFontSel) Color(0xFF55FF55) else Color(0xFF383838)),

                            RoundedCornerShape(2.dp)

                        )

                        .clickable {

                            if (type == FontManager.FONT_CUSTOM) {

                                importFontLauncher.launch("*/*")

                            } else {

                                FontManager.setFont(type, "")

                                Toast.makeText(context, "Switched font to $name", Toast.LENGTH_SHORT).show()

                            }

                        }

                        .padding(horizontal = 10.dp, vertical = 7.dp),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(

                            text = name,

                            color = if (isFontSel) Color(0xFF55FF55) else Color.White,

                            fontFamily = MinecraftFontFamily,

                            fontWeight = FontWeight.Bold,

                            fontSize = 11.5.sp

                        )

                        Text(

                            text = desc,

                            color = if (isFontSel) Color(0xFFCCCCCC) else Color(0xFF888888),

                            fontFamily = MinecraftFontFamily,

                            fontSize = 10.sp,

                            lineHeight = 14.sp

                        )

                    }

                    RadioButton(

                        selected = isFontSel,

                        onClick = {

                            if (type == FontManager.FONT_CUSTOM) {

                                importFontLauncher.launch("*/*")

                            } else {

                                FontManager.setFont(type, "")

                                Toast.makeText(context, "Switched font to $name", Toast.LENGTH_SHORT).show()

                            }

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

                    fontSize = 10.sp,

                    modifier = Modifier

                        .height(36.dp)

                        .widthIn(min = 92.dp)

                )

                MinecraftButton(

                    onClick = onResetAllClick,

                    style = MinecraftButtonStyle.RED,

                    text = "RESET ALL",

                    fontSize = 10.sp,

                    modifier = Modifier

                        .height(36.dp)

                        .widthIn(min = 78.dp)

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

    val scope = rememberCoroutineScope()

    var isImporting by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableIntStateOf(0) }



    val importJreLauncher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.GetContent()

    ) { uri: Uri? ->

        if (uri != null) {

            val rawName = context.getFileName(uri)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
                ?: "custom_jre"

            val cleanName = rawName
                .removeSuffix(".tar.xz")
                .removeSuffix(".tar.gz")
                .removeSuffix(".tgz")
                .removeSuffix(".zip")
                .removeSuffix(".tar")
                .removeSuffix(".xz")
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .ifBlank { "custom_jre" }

            scope.launch(Dispatchers.IO) {

                isImporting = true

                val success = com.movtery.zalithlauncher.game.multirt.JreDownloadManager.importCustomJre(context, uri, cleanName)

                withContext(Dispatchers.Main) {

                    isImporting = false

                    if (success) {

                        refreshKey++

                        onSelectJava(cleanName)

                        AllSettings.javaRuntime.save(cleanName)

                        Toast.makeText(context, "Successfully imported $cleanName!", Toast.LENGTH_SHORT).show()

                    } else {

                        Toast.makeText(context, "Failed to import custom JRE archive", Toast.LENGTH_LONG).show()

                    }

                }

            }

        }

    }



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

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {

                Text(

                    text = "INSTALLED JAVA RUNTIMES",

                    color = Color.White,

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 13.sp

                )

                Text(

                    text = "Import archives (.tar.xz, .zip, .tar.gz) or pick runtime",

                    color = Color(0xFF888888),

                    fontFamily = MinecraftFontFamily,

                    fontSize = 10.sp,

                    lineHeight = 14.sp

                )

            }

            Box(

                modifier = Modifier

                    .clip(RoundedCornerShape(3.dp))

                    .background(Color(0xFF2E6930))

                    .clickable { importJreLauncher.launch("*/*") }

                    .padding(horizontal = 8.dp, vertical = 4.dp)

            ) {

                Text(

                    "+ IMPORT JRE",

                    color = Color(0xFF55FF55),

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 9.sp,

                    maxLines = 1,

                    softWrap = false

                )

            }

        }



        if (isImporting) {

            Text(

                text = "Unpacking and registering runtime archive...",

                color = Color(0xFF55FF55),

                fontFamily = MinecraftFontFamily,

                fontSize = 10.sp,

                lineHeight = 14.sp

            )

        }



        val runtimes = remember(refreshKey) { RuntimesManager.getRuntimes(forceLoad = true) }

        if (runtimes.isEmpty()) {

            Text(

                text = "No custom Java runtimes installed. Internal runtime will be used automatically.",

                color = Color(0xFF888888),

                fontFamily = MinecraftFontFamily,

                fontSize = 11.5.sp,

                lineHeight = 15.sp

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

                    Column(modifier = Modifier.weight(1f)) {

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

                            fontSize = 10.sp,

                            lineHeight = 14.sp

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

    controlsButtonScale: Int,

    onControlsButtonScaleChange: (Int) -> Unit,

    onSelectLayout: (String) -> Unit,

    onLaunchEditor: () -> Unit,

    onCreateNewLayout: () -> Unit,

    onImportControls: () -> Unit,

    onExportLayout: (ControlData) -> Unit

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

        // Overall Controls Button Size Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Overall Controls Button Size", color = Color.White, fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                Text("${controlsButtonScale}%", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
            }

            Slider(
                value = controlsButtonScale.toFloat(),
                onValueChange = { onControlsButtonScaleChange(it.toInt()) },
                onValueChangeFinished = {
                    AllSettings.controlsButtonScale.save(controlsButtonScale)
                },
                valueRange = 50f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF3C8527),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }

        // Layout Actions Header: + NEW LAYOUT | IMPORT CONTROLS | EXPORT ACTIVE

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            MinecraftButton(

                onClick = onCreateNewLayout,

                style = MinecraftButtonStyle.STONE,

                text = "+ NEW",

                fontSize = 10.sp,

                modifier = Modifier

                    .weight(1f)

                    .height(34.dp)

            )

            MinecraftButton(

                onClick = onImportControls,

                style = MinecraftButtonStyle.STONE,

                text = "IMPORT",

                fontSize = 10.sp,

                modifier = Modifier

                    .weight(1f)

                    .height(34.dp)

            )

            MinecraftButton(

                onClick = {

                    val active = controlLayouts.find { it.file.name == activeControlLayoutName }

                        ?: controlLayouts.find { it.file.name == "mojo_layout.json" }

                        ?: controlLayouts.firstOrNull()

                    if (active != null) {

                        onExportLayout(active)

                    } else {

                        Toast.makeText(context, "No active layout to export", Toast.LENGTH_SHORT).show()

                    }

                },

                style = MinecraftButtonStyle.STONE,

                text = "EXPORT",

                fontSize = 10.sp,

                modifier = Modifier

                    .weight(1f)

                    .height(34.dp)

            )

        }



        Text(

            text = "SAVED LAYOUTS (${controlLayouts.size})",

            color = Color(0xFFAAAAAA),

            fontFamily = MinecraftFontFamily,

            fontWeight = FontWeight.Bold,

            fontSize = 11.5.sp

        )



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



                    Row(

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.spacedBy(6.dp)

                    ) {

                        if (isSelected) {

                            Text("ACTIVE", color = Color(0xFF55FF55), fontFamily = MinecraftFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        }



                        MinecraftButton(

                            onClick = { onExportLayout(data) },

                            style = MinecraftButtonStyle.STONE,

                            text = "SHARE",

                            fontSize = 9.sp,

                            modifier = Modifier.height(26.dp)

                        )



                        if (data.file.name != "mojo_layout.json") {

                            MinecraftButton(

                                onClick = {

                                    ControlManager.deleteControl(data)

                                    Toast.makeText(context, "Deleted ${data.file.name}", Toast.LENGTH_SHORT).show()

                                },

                                style = MinecraftButtonStyle.RED,

                                text = "DEL",

                                fontSize = 9.sp,

                                modifier = Modifier.height(26.dp)

                            )

                        }

                    }

                }

            }

        }

    }

}



@Composable

private fun AboutSettingsTab() {

    val context = LocalContext.current



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

            text = "v1.1.0 (Release)",

            color = Color(0xFF55FF55),

            fontFamily = MinecraftFontFamily,

            fontSize = 13.sp,

            fontWeight = FontWeight.Bold

        )



        Text(

            text = "Architecture: ${BuildKeys.BUILD_ARCH}",

            color = Color(0xFF888888),

            fontFamily = MinecraftFontFamily,

            fontSize = 11.sp

        )



        // Zalith Launcher 2 Fork Attribution Notice

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .clip(RoundedCornerShape(3.dp))

                .background(Color(0xFF161618))

                .border(BorderStroke(1.dp, Color(0xFF333336)), RoundedCornerShape(3.dp))

                .padding(12.dp)

        ) {

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                Text(

                    text = "GPLv3 FORK ATTRIBUTION",

                    color = Color(0xFFFFAA00),

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 11.sp

                )

                Text(

                    text = "Cubiq Launcher is an independent, unofficial fork of Zalith Launcher 2 created by MovTery & contributors, licensed under the GNU General Public License v3.0 (GPLv3).\nAll modifications, improvements, and custom modules are published under GPLv3.",

                    color = Color(0xFFAAAAAA),

                    fontFamily = MinecraftFontFamily,

                    fontSize = 10.sp,

                    lineHeight = 14.sp

                )

            }

        }



        // v1.1.0 Changelog & Features

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .clip(RoundedCornerShape(3.dp))

                .background(Color(0xFF161618))

                .border(BorderStroke(1.dp, Color(0xFF223322)), RoundedCornerShape(3.dp))

                .padding(12.dp)

        ) {

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                Text(

                    text = "WHAT'S NEW IN v1.1.0",

                    color = Color(0xFF55FF55),

                    fontFamily = MinecraftFontFamily,

                    fontWeight = FontWeight.Bold,

                    fontSize = 11.5.sp

                )

                val changes = listOf(

                    "Global Mojang Official CDN default download source (Amazon CloudFront edge servers for 50+ MB/s speeds)",

                    "Full PC-Style Landscape UI with desktop navigation sidebar & hero banner",

                    "Tablets & Android Emulators auto-default landscape layout optimization",

                    "Dynamic File Manager matching launcher orientation (portrait/landscape)",

                    "Background downloading service with dedicated Terminate action button & continuous task execution",

                    "Vastly decreased APK download size with dynamic on-demand runtime management",

                    "Real-time JRE & runtime download progress display with live extraction status & spacious speed readout",

                    "4 Built-in Pre-made Control Presets (Mojo default, Zalith 1, Zalith 2, Pocket Edition)",

                    "Bedrock Pocket Edition touch controls with analog joystick, D-Pad & custom textures",

                    "Dedicated in-game Virtual Mouse toggle mapping via mouse button",

                    "Zero-latency UI button click audio (537ms muted silence delay eliminated)",

                    "Controls always visible across menus & inventory; fixed GUI button toggle bug",

                    "Clean native Minecraft HUD (removed duplicate overlay crosshair)",

                    "Skins & Capes wardrobe hub with 9 authentic Mojang capes, custom cape upload, and 3D animated player preview",

                    "Resolved 3D skin glitch on Steve and Alex arm overlay textures",

                    "Account management shortcuts: Official Microsoft & Ely.by logos and direct Manage links, plus offline account manager",

                    "Custom installation isometric block icon selector (38+ high-res icons)",

                    "Reordered renderers: Krypton Wrapper (NGGL4ES) on top, followed by LTW (Large Thin Wrapper)",

                    "LTW renderer integration for modern Minecraft 1.20+ and Sodium/Iris",

                    "Custom Turnip / Vulkan driver manager and Typography customization hub",

                    "Persistent crash recovery system with 1-click log sharing",

                    "Official Cubiq GitHub news feed and automated release update dialog"

                )

                changes.forEach { item ->

                    Row(

                        horizontalArrangement = Arrangement.spacedBy(6.dp),

                        verticalAlignment = Alignment.Top

                    ) {

                        Text("•", color = Color(0xFF55FF55), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Text(item, color = Color(0xFFCCCCCC), fontFamily = MinecraftFontFamily, fontSize = 10.sp, lineHeight = 13.5.sp)

                    }

                }

            }

        }



        Spacer(modifier = Modifier.height(4.dp))



        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(10.dp)

        ) {

            MinecraftButton(

                onClick = {

                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(com.movtery.zalithlauncher.path.URL_PROJECT))

                    context.startActivity(intent)

                },

                style = MinecraftButtonStyle.STONE,

                text = "GITHUB",

                fontSize = 11.5.sp,

                modifier = Modifier

                    .weight(1f)

                    .height(38.dp)

            )



            MinecraftButton(

                onClick = {

                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(com.movtery.zalithlauncher.path.URL_DISCORD))

                    context.startActivity(intent)

                },

                style = MinecraftButtonStyle.GREEN,

                text = "DISCORD",

                fontSize = 11.5.sp,

                modifier = Modifier

                    .weight(1f)

                    .height(38.dp)

            )

        }

    }

}

