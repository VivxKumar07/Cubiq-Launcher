package com.movtery.zalithlauncher.ui.screens.game.elements

import android.app.ActivityManager
import android.content.Context
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.control.HotbarRule

/**
 * Authentic In-Game Drawers for Legacy Control Schemes (Mojo Launcher & Zalith Launcher 1)
 */
@Composable
fun LegacyInGameDrawer(
    visible: Boolean,
    isInEditor: Boolean,
    onDismiss: () -> Unit,
    onForceClose: () -> Unit,
    onViewLogs: () -> Unit,
    onSendKeycode: () -> Unit,
    onStartEditControls: () -> Unit,
    onAddButton: () -> Unit,
    onAddDrawer: () -> Unit,
    onAddJoystick: () -> Unit,
    onLoadLayout: () -> Unit,
    onSaveLayout: () -> Unit,
    onSaveAndExit: () -> Unit = {},
    onSelectDefaultLayout: () -> Unit,
    onLeaveEditor: () -> Unit,
    onReplaceControl: () -> Unit = {},
    onSelectCustomMouse: () -> Unit = {},
    onButtonOpacityChange: ((Float) -> Unit)? = null,
    onRefreshWindowSize: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x55000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onDismiss()
            }
    ) {
        // Authentic Zalith Launcher 1 In-Game / In-Editor Drawer
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Zl1InGameDrawerContent(
                isInEditor = isInEditor,
                onDismiss = onDismiss,
                onForceClose = onForceClose,
                onViewLogs = onViewLogs,
                onSendKeycode = onSendKeycode,
                onSelectCustomMouse = onSelectCustomMouse,
                onReplaceControl = onReplaceControl,
                onStartEditControls = onStartEditControls,
                onAddButton = onAddButton,
                onAddDrawer = onAddDrawer,
                onAddJoystick = onAddJoystick,
                onLoadLayout = onLoadLayout,
                onSaveLayout = onSaveLayout,
                onSaveAndExit = onSaveAndExit,
                onSelectDefaultLayout = onSelectDefaultLayout,
                onLeaveEditor = onLeaveEditor,
                onRefreshWindowSize = onRefreshWindowSize
            )
        }
    }
}

/**
 * Authentic Zalith Launcher 1 Drawer Content
 */
@Composable
private fun Zl1InGameDrawerContent(
    isInEditor: Boolean,
    onDismiss: () -> Unit,
    onForceClose: () -> Unit,
    onViewLogs: () -> Unit,
    onSendKeycode: () -> Unit,
    onSelectCustomMouse: () -> Unit,
    onReplaceControl: () -> Unit,
    onStartEditControls: () -> Unit,
    onAddButton: () -> Unit,
    onAddDrawer: () -> Unit,
    onAddJoystick: () -> Unit,
    onLoadLayout: () -> Unit,
    onSaveLayout: () -> Unit,
    onSaveAndExit: () -> Unit,
    onSelectDefaultLayout: () -> Unit,
    onLeaveEditor: () -> Unit,
    onRefreshWindowSize: () -> Unit = {}
) {
    val context = LocalContext.current
    val dm = context.resources.displayMetrics
    val baseW = maxOf(dm.widthPixels, dm.heightPixels)
    val baseH = minOf(dm.widthPixels, dm.heightPixels)

    var resRatio by remember { mutableFloatStateOf(AllSettings.resolutionRatio.state.toFloat()) }
    var mouseSpeed by remember { mutableFloatStateOf(AllSettings.mouseCaptureSensitivity.state.toFloat()) }
    var gestureDelay by remember { mutableFloatStateOf(AllSettings.mouseLongPressDelay.state.toFloat()) }
    var gesturesDisabled by remember { mutableStateOf(!AllSettings.gestureControl.state) }
    var doubleTapDisabled by remember { mutableStateOf(AllSettings.disableDoubleTap.state) }
    var showMemory by remember { mutableStateOf(AllSettings.showMemory.state) }
    var showFps by remember { mutableStateOf(AllSettings.showFPS.state) }
    var gyroEnabled by remember { mutableStateOf(AllSettings.gyroscopeControl.state) }
    var gyroSensitivity by remember { mutableFloatStateOf(AllSettings.gyroscopeSensitivity.state.toFloat()) }
    var gyroInvertX by remember { mutableStateOf(AllSettings.gyroscopeInvertX.state) }
    var gyroInvertY by remember { mutableStateOf(AllSettings.gyroscopeInvertY.state) }
    var hotbarRule by remember { mutableStateOf(AllSettings.hotbarRule.state) }
    var hotbarWidth by remember { mutableFloatStateOf(AllSettings.hotbarWidth.state.toFloat()) }
    var hotbarHeight by remember { mutableFloatStateOf(AllSettings.hotbarHeight.state.toFloat()) }
    var buttonSnapping by remember { mutableStateOf(AllSettings.buttonSnapping.state) }
    var snappingDist by remember { mutableFloatStateOf(AllSettings.buttonSnappingDistance.state.toFloat().coerceIn(2f, 30f)) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(272.dp)
            .background(Color(0xFA1D1E22), RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header with gear icon and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sharp_settings_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(if (isInEditor) R.string.cubiq_game_editor_title else R.string.game_menu_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

        if (!isInEditor) {
            // Debug section
            Zl1CategoryHeader(title = stringResource(R.string.cubiq_game_debug))

            Zl1ButtonItem(text = stringResource(R.string.game_button_force_close)) {
                onDismiss()
                onForceClose()
            }
            Zl1ButtonItem(text = stringResource(R.string.game_button_log_output)) {
                onDismiss()
                onViewLogs()
            }
            Zl1ButtonItem(text = stringResource(R.string.game_menu_option_send_keycode)) {
                onDismiss()
                onSendKeycode()
            }

            // Real-time info switches
            Zl1SwitchItem(
                title = stringResource(R.string.game_menu_option_switch_memory),
                checked = showMemory
            ) {
                showMemory = it
                AllSettings.showMemory.save(it)
            }

            Zl1SwitchItem(
                title = stringResource(R.string.game_menu_option_switch_fps),
                checked = showFps
            ) {
                showFps = it
                AllSettings.showFPS.save(it)
            }

            // Resolution Scaling with live pixel preview
            val scaledW = (baseW * resRatio / 100f).toInt().coerceAtLeast(1)
            val scaledH = (baseH * resRatio / 100f).toInt().coerceAtLeast(1)
            Zl1SliderItem(
                title = stringResource(R.string.cubiq_game_resolution_preview, scaledW, scaledH),
                value = resRatio,
                range = 25f..300f,
                unit = "%",
                step = 5f
            ) {
                resRatio = it
                AllSettings.resolutionRatio.save(it.toInt())
                onRefreshWindowSize()
            }

            // Control Category
            Zl1CategoryHeader(title = stringResource(R.string.cubiq_game_control))

            Zl1SwitchItem(
                title = stringResource(R.string.cubiq_game_disable_gestures),
                checked = gesturesDisabled
            ) {
                gesturesDisabled = it
                AllSettings.gestureControl.save(!it)
            }

            Zl1SwitchItem(
                title = stringResource(R.string.cubiq_game_disable_double_tap),
                checked = doubleTapDisabled
            ) {
                doubleTapDisabled = it
                AllSettings.disableDoubleTap.save(it)
            }

            if (!gesturesDisabled) {
                Zl1SliderItem(
                    title = stringResource(R.string.settings_control_gesture_long_press_delay_title),
                    value = gestureDelay,
                    range = 100f..1000f,
                    unit = " ms",
                    step = 25f
                ) {
                    gestureDelay = it
                    AllSettings.mouseLongPressDelay.save(it.toInt())
                }
            }

            Zl1SliderItem(
                title = stringResource(R.string.cubiq_game_virtual_mouse_speed),
                value = mouseSpeed,
                range = 25f..300f,
                unit = " %",
                step = 5f
            ) {
                mouseSpeed = it
                AllSettings.mouseCaptureSensitivity.save(it.toInt())
            }

            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_custom_mouse)) {
                onDismiss()
                onSelectCustomMouse()
            }

            Zl1ButtonItem(text = stringResource(R.string.game_menu_option_replacement_control)) {
                onDismiss()
                onReplaceControl()
            }

            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_editor_title)) {
                onDismiss()
                onStartEditControls()
            }

            // Gyro controls Category
            Zl1CategoryHeader(title = stringResource(R.string.settings_control_gyroscope_title))

            Zl1SwitchItem(
                title = stringResource(R.string.cubiq_game_enable_gyro),
                checked = gyroEnabled
            ) {
                gyroEnabled = it
                AllSettings.gyroscopeControl.save(it)
            }

            if (gyroEnabled) {
                Zl1SliderItem(
                    title = stringResource(R.string.settings_control_gyroscope_sensitivity_title),
                    value = gyroSensitivity,
                    range = 25f..300f,
                    unit = " %",
                    step = 5f
                ) {
                    gyroSensitivity = it
                    AllSettings.gyroscopeSensitivity.save(it.toInt())
                }

                Zl1SwitchItem(
                    title = stringResource(R.string.settings_control_gyroscope_invert_x_title),
                    checked = gyroInvertX
                ) {
                    gyroInvertX = it
                    AllSettings.gyroscopeInvertX.save(it)
                }

                Zl1SwitchItem(
                    title = stringResource(R.string.settings_control_gyroscope_invert_y_title),
                    checked = gyroInvertY
                ) {
                    gyroInvertY = it
                    AllSettings.gyroscopeInvertY.save(it)
                }
            }

            // Hotbar Detection Box Category
            Zl1CategoryHeader(title = stringResource(R.string.cubiq_game_hotbar_detection_box))

            Text(
                text = stringResource(R.string.cubiq_game_hotbar_detection_mode),
                color = Color(0xFFB8BCC6),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
            )
            Zl1ModeOptionItem(
                label = stringResource(R.string.game_menu_option_hotbar_rule_auto),
                selected = hotbarRule == HotbarRule.Auto
            ) {
                hotbarRule = HotbarRule.Auto
                AllSettings.hotbarRule.save(HotbarRule.Auto)
            }
            Zl1ModeOptionItem(
                label = stringResource(R.string.cubiq_game_manual_adjustment),
                selected = hotbarRule == HotbarRule.Custom
            ) {
                hotbarRule = HotbarRule.Custom
                AllSettings.hotbarRule.save(HotbarRule.Custom)
            }

            if (hotbarRule == HotbarRule.Custom) {
                Zl1SliderItem(
                    title = stringResource(R.string.game_menu_option_hotbar_width),
                    value = hotbarWidth,
                    range = 0f..1000f,
                    unit = " px",
                    step = 10f
                ) {
                    hotbarWidth = it
                    AllSettings.hotbarWidth.save(it.toInt())
                }
                Zl1SliderItem(
                    title = stringResource(R.string.game_menu_option_hotbar_height),
                    value = hotbarHeight,
                    range = 0f..1000f,
                    unit = " px",
                    step = 10f
                ) {
                    hotbarHeight = it
                    AllSettings.hotbarHeight.save(it.toInt())
                }
            }
        } else {
            // In-Editor Actions
            Zl1CategoryHeader(title = stringResource(R.string.cubiq_game_editor_title))

            Zl1ButtonItem(text = stringResource(R.string.control_editor_menu_new_widget_button)) {
                onDismiss()
                onAddButton()
            }
            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_editor_add_drawer)) {
                onDismiss()
                onAddDrawer()
            }
            Zl1ButtonItem(text = stringResource(R.string.control_editor_menu_new_widget_joystick)) {
                onDismiss()
                onAddJoystick()
            }

            Zl1CategoryHeader(title = stringResource(R.string.generic_save))

            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_editor_load)) {
                onDismiss()
                onLoadLayout()
            }
            Zl1ButtonItem(text = stringResource(R.string.generic_save)) {
                onDismiss()
                onSaveLayout()
            }
            Zl1ButtonItem(text = stringResource(R.string.control_editor_menu_save_and_exit)) {
                onDismiss()
                onSaveAndExit()
            }

            Zl1CategoryHeader(title = stringResource(R.string.cubiq_game_editor_layout))

            Zl1SwitchItem(
                title = stringResource(R.string.cubiq_game_editor_snapping),
                checked = buttonSnapping
            ) {
                buttonSnapping = it
                AllSettings.buttonSnapping.save(it)
            }

            Zl1SliderItem(
                title = stringResource(R.string.cubiq_game_editor_snapping_distance),
                value = snappingDist,
                range = 2f..30f,
                unit = " dp",
                step = 1f
            ) {
                snappingDist = it
                AllSettings.buttonSnappingDistance.save(it.toInt().coerceIn(2, 64))
            }

            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_editor_default_layout)) {
                onDismiss()
                onSelectDefaultLayout()
            }

            Zl1ButtonItem(text = stringResource(R.string.cubiq_game_editor_exit)) {
                onDismiss()
                onLeaveEditor()
            }
        }
    }
}

@Composable
private fun Zl1ModeOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if (selected) Color(0x3326A69A) else Color(0xFF2B2C32),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF26A69A) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFFD0D3DC),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (selected) "✓" else "",
            color = Color(0xFF26A69A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Zl1CategoryHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            color = Color(0x33FFFFFF),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun Zl1ButtonItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(38.dp)
            .background(Color(0xFF2B2C32), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun Zl1SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF26A69A),
                checkedTrackColor = Color(0xFF1E524D),
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = Color(0xFF3E4048)
            )
        )
    }
}

@Composable
private fun Zl1SliderItem(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    step: Float = 1f,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.White, fontSize = 12.sp)
            Text("${value.toInt()}$unit", color = Color(0xFF26A69A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onValueChange((value - step).coerceIn(range.start, range.endInclusive)) },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF26A69A),
                    activeTrackColor = Color(0xFF26A69A),
                    inactiveTrackColor = Color(0xFF3E4048)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onValueChange((value + step).coerceIn(range.start, range.endInclusive)) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


private fun formatSystemMemorySummary(context: Context): String {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (am != null) {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        if (info.totalMem > 0L) {
            val usedBytes = (info.totalMem - info.availMem).coerceAtLeast(0L)
            return "${Formatter.formatFileSize(context, usedBytes)}/${Formatter.formatFileSize(context, info.totalMem)}"
        }
    }
    val rt = Runtime.getRuntime()
    val used = (rt.totalMemory() - rt.freeMemory()) / 1048576L
    val max = rt.maxMemory() / 1048576L
    return "${used}MB/${max}MB"
}

/**
 * Horizontal ZL1 Floating Game Menu Pill matching view_game_menu_window.xml
 */
@Composable
fun Zl1FloatingMenuPill(
    gameFps: Int?,
    showFps: Boolean,
    showMemory: Boolean
) {
    val context = LocalContext.current
    var memLabel by remember { mutableStateOf("") }
    if (showMemory) {
        LaunchedEffect(Unit) {
            while (true) {
                memLabel = formatSystemMemorySummary(context)
                kotlinx.coroutines.delay(1000L)
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xEE16161A), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sharp_settings_24),
                contentDescription = stringResource(R.string.cubiq_game_zl1_menu),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showFps) {
            Text(
                text = stringResource(R.string.cubiq_game_fps, gameFps ?: 0),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp, end = 4.dp)
            )
        }
        if (showMemory && memLabel.isNotEmpty()) {
            Text(
                text = stringResource(R.string.cubiq_game_memory, memLabel),
                color = Color(0xFFB8BCC6),
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            )
        }
    }
}

/**
 * Shared ZL1 Custom Virtual Mouse Pack Picker Dialog
 */
@Composable
fun Zl1CustomMouseDialog(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return
    val context = LocalContext.current
    val packs = remember {
        listOf(
            "Default Classic" to R.string.cubiq_game_cursor_default,
            "Precision Crosshair" to R.string.cubiq_game_cursor_crosshair,
            "Bedrock Arrow" to R.string.cubiq_game_cursor_arrow,
            "Chroma Emerald" to R.string.cubiq_game_cursor_emerald,
            "Modern Obsidian" to R.string.cubiq_game_cursor_obsidian
        )
    }
    var selected by remember(visible) { mutableStateOf(AllSettings.customCursorPack.state) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(290.dp)
                .background(Color(0xFA1D1E22), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.cubiq_game_custom_mouse),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)
            packs.forEach { (pack, labelRes) ->
                val packLabel = stringResource(labelRes)
                Zl1ModeOptionItem(
                    label = packLabel,
                    selected = selected == pack
                ) {
                    selected = pack
                    AllSettings.customCursorPack.save(pack)
                    if (pack == "Default Classic") {
                        runCatching { com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile.delete() }
                    }
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.cubiq_game_cursor_selected, packLabel),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.generic_close), color = Color(0xFF26A69A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
