package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * Authentic Mojo Launcher In-Game Drawer and Quick Settings
 * Replaces fake custom menus with the exact drawer and options from authentic Mojo Launcher.
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
    onSelectDefaultLayout: () -> Unit,
    onLeaveEditor: () -> Unit,
    onButtonOpacityChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showQuickSettings by remember { mutableStateOf(false) }

    if (!visible && !showQuickSettings) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x55000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showQuickSettings = false
                onDismiss()
            }
    ) {
        // Quick Settings Side Dialog (matches Mojo Launcher QuickSettingSideDialog)
        AnimatedVisibility(
            visible = showQuickSettings,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            MojoQuickSettingsSidePanel(
                onClose = { showQuickSettings = false },
                onButtonOpacityChange = onButtonOpacityChange
            )
        }

        // In-Game / In-Editor Action Drawer (matches Mojo Launcher navDrawer)
        AnimatedVisibility(
            visible = visible && !showQuickSettings,
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
                    ) {} // Prevent click-through
                    .verticalScroll(rememberScrollState())
            ) {
                if (!isInEditor) {
                    // Authentic Mojo In-Game Options
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
                        showQuickSettings = true
                    }
                    MojoDivider()

                    MojoDrawerItem(text = "Custom controls") {
                        onDismiss()
                        onStartEditControls()
                    }
                } else {
                    // Authentic Mojo In-Editor Options
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
