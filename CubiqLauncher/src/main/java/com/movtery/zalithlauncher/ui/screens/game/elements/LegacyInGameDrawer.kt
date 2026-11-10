package com.movtery.zalithlauncher.ui.screens.game.elements

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * Authentic In-Game Drawers for Legacy Control Schemes (Mojo Launcher & Zalith Launcher 1)
 */
@Composable
fun LegacyInGameDrawer(
    visible: Boolean,
    isMojo: Boolean,
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
    onButtonOpacityChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMojoQuickSettings by remember { mutableStateOf(false) }

    if (!visible && !showMojoQuickSettings) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x55000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showMojoQuickSettings = false
                onDismiss()
            }
    ) {
        if (isMojo) {
            // Mojo Launcher Quick Settings Side Panel
            AnimatedVisibility(
                visible = showMojoQuickSettings,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                MojoQuickSettingsSidePanel(
                    onClose = { showMojoQuickSettings = false },
                    onButtonOpacityChange = onButtonOpacityChange
                )
            }

            // Mojo Launcher In-Game / In-Editor Action Drawer
            AnimatedVisibility(
                visible = visible && !showMojoQuickSettings,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(200.dp)
                        .background(Color(0xFA1E1E22))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!isInEditor) {
                        MojoDrawerItem(text = "Force close") {
                            onDismiss()
                            onForceClose()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Log output") {
                            onDismiss()
                            onViewLogs()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Send custom keycode") {
                            onDismiss()
                            onSendKeycode()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Quick settings") {
                            showMojoQuickSettings = true
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Custom controls") {
                            onDismiss()
                            onStartEditControls()
                        }
                    } else {
                        MojoDrawerItem(text = "Add button") {
                            onDismiss()
                            onAddButton()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Add button drawer") {
                            onDismiss()
                            onAddDrawer()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Add joystick") {
                            onDismiss()
                            onAddJoystick()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Load") {
                            onDismiss()
                            onLoadLayout()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Save") {
                            onDismiss()
                            onSaveLayout()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Select default controlmap") {
                            onDismiss()
                            onSelectDefaultLayout()
                        }
                        MojoDivider()

                        MojoDrawerItem(text = "Leave editor") {
                            onDismiss()
                            onLeaveEditor()
                        }
                    }
                }
            }
        } else {
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
                    onReplaceControl = onReplaceControl,
                    onStartEditControls = onStartEditControls,
                    onAddButton = onAddButton,
                    onAddDrawer = onAddDrawer,
                    onAddJoystick = onAddJoystick,
                    onLoadLayout = onLoadLayout,
                    onSaveLayout = onSaveLayout,
                    onSaveAndExit = onSaveAndExit,
                    onSelectDefaultLayout = onSelectDefaultLayout,
                    onLeaveEditor = onLeaveEditor
                )
            }
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
    onReplaceControl: () -> Unit,
    onStartEditControls: () -> Unit,
    onAddButton: () -> Unit,
    onAddDrawer: () -> Unit,
    onAddJoystick: () -> Unit,
    onLoadLayout: () -> Unit,
    onSaveLayout: () -> Unit,
    onSaveAndExit: () -> Unit,
    onSelectDefaultLayout: () -> Unit,
    onLeaveEditor: () -> Unit
) {
    var resRatio by remember { mutableFloatStateOf(AllSettings.resolutionRatio.state.toFloat()) }
    var mouseSpeed by remember { mutableFloatStateOf(AllSettings.mouseCaptureSensitivity.state.toFloat()) }
    var gestureDelay by remember { mutableFloatStateOf(AllSettings.mouseLongPressDelay.state.toFloat()) }
    var gesturesDisabled by remember { mutableStateOf(false) }
    var doubleTapDisabled by remember { mutableStateOf(false) }
    var showMemory by remember { mutableStateOf(AllSettings.showMemory.state) }
    var showFps by remember { mutableStateOf(AllSettings.showFPS.state) }
    var gyroEnabled by remember { mutableStateOf(AllSettings.gyroscopeControl.state) }
    var gyroSensitivity by remember { mutableFloatStateOf(AllSettings.gyroscopeSensitivity.state.toFloat()) }
    var buttonSnapping by remember { mutableStateOf(AllSettings.buttonSnapping.state) }
    var snappingDist by remember { mutableFloatStateOf(AllSettings.buttonSnappingDistance.state.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
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
                text = if (isInEditor) "Edit Control Layout" else "Game Menu",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

        if (!isInEditor) {
            // Debug section buttons
            Zl1ButtonItem(text = "Force close") {
                onDismiss()
                onForceClose()
            }
            Zl1ButtonItem(text = "Log output") {
                onDismiss()
                onViewLogs()
            }
            Zl1ButtonItem(text = "Send custom keycode") {
                onDismiss()
                onSendKeycode()
            }

            // Real-time info switches
            Zl1SwitchItem(
                title = "Real-time Memory Info Display",
                checked = showMemory
            ) {
                showMemory = it
                AllSettings.showMemory.updateState(it)
                AllSettings.showMemory.save()
            }

            Zl1SwitchItem(
                title = "Show real-time FPS",
                checked = showFps
            ) {
                showFps = it
                AllSettings.showFPS.updateState(it)
                AllSettings.showFPS.save()
            }

            // Resolution Scaling
            Zl1SliderItem(
                title = "Resolution Scaling",
                value = resRatio,
                range = 25f..300f,
                unit = "%",
                step = 5f
            ) {
                resRatio = it
                AllSettings.resolutionRatio.updateState(it.toInt())
                AllSettings.resolutionRatio.save()
            }

            // Control Category
            Zl1CategoryHeader(title = "Control")

            Zl1SwitchItem(
                title = "Disable Gestures",
                checked = gesturesDisabled
            ) {
                gesturesDisabled = it
            }

            Zl1SwitchItem(
                title = "Disable Double Tap to Swap Items",
                checked = doubleTapDisabled
            ) {
                doubleTapDisabled = it
            }

            Zl1SliderItem(
                title = "Long Press Trigger Delay",
                value = gestureDelay,
                range = 100f..1000f,
                unit = " ms",
                step = 25f
            ) {
                gestureDelay = it
                AllSettings.mouseLongPressDelay.updateState(it.toInt())
                AllSettings.mouseLongPressDelay.save()
            }

            Zl1SliderItem(
                title = "Virtual Mouse Speed",
                value = mouseSpeed,
                range = 25f..300f,
                unit = " %",
                step = 5f
            ) {
                mouseSpeed = it
                AllSettings.mouseCaptureSensitivity.updateState(it.toInt())
                AllSettings.mouseCaptureSensitivity.save()
            }

            Zl1ButtonItem(text = "Replace Control Layout") {
                onDismiss()
                onReplaceControl()
            }

            Zl1ButtonItem(text = "Edit Control Layout") {
                onDismiss()
                onStartEditControls()
            }

            // Gyro controls Category
            Zl1CategoryHeader(title = "Gyro controls")

            Zl1SwitchItem(
                title = "Enable Gyroscope",
                checked = gyroEnabled
            ) {
                gyroEnabled = it
                AllSettings.gyroscopeControl.updateState(it)
                AllSettings.gyroscopeControl.save()
            }

            Zl1SliderItem(
                title = "Gyroscope Sensitivity",
                value = gyroSensitivity,
                range = 25f..300f,
                unit = " %",
                step = 5f
            ) {
                gyroSensitivity = it
                AllSettings.gyroscopeSensitivity.updateState(it.toInt())
                AllSettings.gyroscopeSensitivity.save()
            }
        } else {
            // In-Editor Actions
            Zl1CategoryHeader(title = "Edit Control Layout")

            Zl1ButtonItem(text = "Add Button") {
                onAddButton()
            }
            Zl1ButtonItem(text = "Add Drawer") {
                onAddDrawer()
            }
            Zl1ButtonItem(text = "Add Joystick") {
                onAddJoystick()
            }

            Zl1CategoryHeader(title = "Save")

            Zl1ButtonItem(text = "Load") {
                onLoadLayout()
            }
            Zl1ButtonItem(text = "Save") {
                onSaveLayout()
            }
            Zl1ButtonItem(text = "Save and Exit") {
                onSaveAndExit()
            }

            Zl1CategoryHeader(title = "Snapping")

            Zl1SwitchItem(
                title = "Button Snapping",
                checked = buttonSnapping
            ) {
                buttonSnapping = it
                AllSettings.buttonSnapping.updateState(it)
                AllSettings.buttonSnapping.save()
            }

            Zl1SliderItem(
                title = "Snapping Distance",
                value = snappingDist,
                range = 2f..64f,
                unit = " dp",
                step = 1f
            ) {
                snappingDist = it
                AllSettings.buttonSnappingDistance.updateState(it.toInt())
                AllSettings.buttonSnappingDistance.save()
            }

            Zl1ButtonItem(text = "Select Default Control Layout") {
                onSelectDefaultLayout()
            }

            Zl1ButtonItem(text = "Exit") {
                onLeaveEditor()
            }
        }
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

@Composable
private fun MojoDrawerItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun MojoDivider() {
    HorizontalDivider(
        color = Color(0x1FFFFFFF),
        thickness = 1.dp
    )
}

/**
 * Authentic Quick Settings Side Panel from Mojo Launcher
 */
@Composable
private fun MojoQuickSettingsSidePanel(
    onClose: () -> Unit,
    onButtonOpacityChange: ((Float) -> Unit)? = null
) {
    var resRatio by remember { mutableFloatStateOf(AllSettings.resolutionRatio.state.toFloat()) }
    var gyroEnabled by remember { mutableStateOf<Boolean>(AllSettings.gyroscopeControl.state) }
    var mouseSpeed by remember { mutableFloatStateOf(AllSettings.mouseCaptureSensitivity.state.toFloat()) }
    var gesturesDisabled by remember { mutableStateOf(false) }
    var gestureDelay by remember { mutableFloatStateOf(300f) }
    var buttonsOpacity by remember { mutableFloatStateOf(AllSettings.controlsOpacity.state.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFA1E1E22))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Quick settings",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

        // Resolution Scaler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Resolution Scaler", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Text("${resRatio.toInt()}%", color = Color(0xFF888888), fontSize = 12.sp)
        }
        Slider(
            value = resRatio,
            onValueChange = { resRatio = it },
            valueRange = 25f..100f,
            steps = 14,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF26A69A),
                activeTrackColor = Color(0xFF26A69A),
                inactiveTrackColor = Color(0xFF444448)
            ),
            modifier = Modifier.height(28.dp)
        )

        // Enable gyroscope controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable gyroscope controls", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Switch(
                checked = gyroEnabled,
                onCheckedChange = { gyroEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF26A69A),
                    checkedTrackColor = Color(0xFF1E524D)
                )
            )
        }

        // Mouse Speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mouse Speed", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Text("${mouseSpeed.toInt()}%", color = Color(0xFF888888), fontSize = 12.sp)
        }
        Slider(
            value = mouseSpeed,
            onValueChange = { mouseSpeed = it },
            valueRange = 25f..300f,
            steps = 54,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF26A69A),
                activeTrackColor = Color(0xFF26A69A),
                inactiveTrackColor = Color(0xFF444448)
            ),
            modifier = Modifier.height(28.dp)
        )

        // Disable gestures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Disable gestures", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Switch(
                checked = gesturesDisabled,
                onCheckedChange = { gesturesDisabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF26A69A),
                    checkedTrackColor = Color(0xFF1E524D)
                )
            )
        }

        // Trigger long press delay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trigger long press delay", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Text("${gestureDelay.toInt()} ms", color = Color(0xFF888888), fontSize = 12.sp)
        }
        Slider(
            value = gestureDelay,
            onValueChange = { gestureDelay = it },
            valueRange = 100f..1000f,
            steps = 89,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF26A69A),
                activeTrackColor = Color(0xFF26A69A),
                inactiveTrackColor = Color(0xFF444448)
            ),
            modifier = Modifier.height(28.dp)
        )

        // Buttons transparency
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Buttons transparency", color = Color(0xFFDDDDDD), fontSize = 12.sp)
            Text("${buttonsOpacity.toInt()}%", color = Color(0xFF888888), fontSize = 12.sp)
        }
        Slider(
            value = buttonsOpacity,
            onValueChange = {
                buttonsOpacity = it
                onButtonOpacityChange?.invoke(it)
            },
            valueRange = 0f..100f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF26A69A),
                activeTrackColor = Color(0xFF26A69A),
                inactiveTrackColor = Color(0xFF444448)
            ),
            modifier = Modifier.height(28.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // CANCEL and OK Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) {
                Text("CANCEL", color = Color(0xFF26A69A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            TextButton(
                onClick = {
                    AllSettings.resolutionRatio.updateState(resRatio.toInt())
                    AllSettings.resolutionRatio.save()
                    AllSettings.gyroscopeControl.updateState(gyroEnabled)
                    AllSettings.gyroscopeControl.save()
                    AllSettings.mouseCaptureSensitivity.updateState(mouseSpeed.toInt())
                    AllSettings.mouseCaptureSensitivity.save()
                    AllSettings.controlsOpacity.updateState(buttonsOpacity.toInt())
                    AllSettings.controlsOpacity.save()
                    onClose()
                }
            ) {
                Text("OK", color = Color(0xFF26A69A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
