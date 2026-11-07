/*
 * Zalith Launcher 2 / Cubiq Launcher
 */

package com.movtery.zalithlauncher.ui.screens.content.settings

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.movtery.zalithlauncher.ui.components.MinecraftPixelBorderContainer


import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.movtery.inputmap.keycodes.ControlEventKeyName
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.components.MinecraftPixelBorderContainer
import com.movtery.zalithlauncher.ui.control.Keyboard
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.viewmodel.EventViewModel

enum class RemapCategory {
    MOUSE, KEYBOARD
}

data class MouseBinding(
    val id: String,
    val name: String,
    val defaultMapping: String
)

data class KeyboardBinding(
    val action: String,
    val defaultKey: String
)

@Composable
fun KeyboardMouseSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(RemapCategory.KEYBOARD) }
    val activePack = AllSettings.customCursorPack.state
    var selectedCursorPack by remember(activePack) { mutableStateOf(activePack) }
    var cursorScale by remember { mutableFloatStateOf(AllSettings.mouseSize.state * 4f) }

    val cursorPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val dest = com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile
                    dest.parentFile?.mkdirs()
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                AllSettings.customCursorUri.save(uri.toString())
                AllSettings.customCursorPack.save("Custom Imported")
                selectedCursorPack = "Custom Imported"
                Toast.makeText(context, "Custom cursor imported successfully!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Failed to import cursor: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dialog state for key picking
    var activeRemapTarget by remember { mutableStateOf<String?>(null) }
    var showExtraButtonDetector by remember { mutableStateOf(false) }

    // Persistent key mapping store
    val mouseMappings = remember {
        mutableStateMapOf(
            "btn_left" to "Attack / Destroy (Btn 1)",
            "btn_right" to "Use Item / Place Block (Btn 2)",
            "btn_middle" to "Pick Block (Btn 3)",
            "btn_side_1" to "Sprint (Key L-CTRL)",
            "btn_side_2" to "Sneak (Key L-SHIFT)"
        ).apply {
            val saved = AllSettings.mouseRemapBindings.getValue()
            if (saved.isNotEmpty()) {
                saved.split(";").forEach { pair ->
                    val parts = pair.split("=")
                    if (parts.size == 2) put(parts[0], parts[1])
                }
            }
        }
    }

    val keyboardMappings = remember {
        mutableStateMapOf(
            "Move Forward" to "W",
            "Move Backward" to "S",
            "Strafe Left" to "A",
            "Strafe Right" to "D",
            "Jump" to "SPACE",
            "Sneak" to "L-SHIFT",
            "Sprint" to "L-CTRL",
            "Inventory" to "E",
            "Drop Item" to "Q",
            "Open Chat" to "T",
            "Player List" to "TAB",
            "Perspective" to "F5",
            "Fullscreen" to "F11",
            "Zoom" to "C"
        ).apply {
            val saved = AllSettings.keyboardRemapBindings.getValue()
            if (saved.isNotEmpty()) {
                saved.split(";").forEach { pair ->
                    val parts = pair.split("=")
                    if (parts.size == 2) put(parts[0], parts[1])
                }
            }
        }
    }

    // Interactive Remap & Key/Button Detector Dialog
    if (activeRemapTarget != null) {
        RemapBindingDialog(
            initialTarget = activeRemapTarget!!,
            mouseMappings = mouseMappings,
            keyboardMappings = keyboardMappings,
            onDismissRequest = { activeRemapTarget = null },
            onSaveBinding = { target, newBinding ->
                if (mouseMappings.containsKey(target) || target.startsWith("btn_")) {
                    mouseMappings[target] = newBinding
                } else {
                    keyboardMappings[target] = newBinding
                }
                val mouseJson = mouseMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.mouseRemapBindings.save(mouseJson)
                val kbJson = keyboardMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.keyboardRemapBindings.save(kbJson)
                Toast.makeText(context, "Bound $target to $newBinding", Toast.LENGTH_SHORT).show()
            },
            onResetTarget = { target ->
                val defaultVal = when (target) {
                    "btn_left" -> "Attack / Destroy (Btn 1)"
                    "btn_right" -> "Use Item / Place Block (Btn 2)"
                    "btn_middle" -> "Pick Block (Btn 3)"
                    "btn_side_1" -> "Sprint (Key L-CTRL)"
                    "btn_side_2" -> "Sneak (Key L-SHIFT)"
                    "Move Forward" -> "W"
                    "Move Backward" -> "S"
                    "Strafe Left" -> "A"
                    "Strafe Right" -> "D"
                    "Jump" -> "SPACE"
                    "Sneak" -> "L-SHIFT"
                    "Sprint" -> "L-CTRL"
                    "Inventory" -> "E"
                    "Drop Item" -> "Q"
                    "Swap Hand" -> "F"
                    "Open Chat" -> "T"
                    "Player List" -> "TAB"
                    "Perspective" -> "F5"
                    "Fullscreen" -> "F11"
                    "Zoom" -> "C"
                    else -> "Default"
                }
                if (mouseMappings.containsKey(target)) {
                    mouseMappings[target] = defaultVal
                } else {
                    keyboardMappings[target] = defaultVal
                }
                val mouseJson = mouseMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.mouseRemapBindings.save(mouseJson)
                val kbJson = keyboardMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.keyboardRemapBindings.save(kbJson)
                Toast.makeText(context, "Reset $target to default", Toast.LENGTH_SHORT).show()
            },
            onResetAll = {
                mouseMappings.clear()
                mouseMappings.putAll(mapOf(
                    "btn_left" to "Attack / Destroy (Btn 1)",
                    "btn_right" to "Use Item / Place Block (Btn 2)",
                    "btn_middle" to "Pick Block (Btn 3)",
                    "btn_side_1" to "Sprint (Key L-CTRL)",
                    "btn_side_2" to "Sneak (Key L-SHIFT)"
                ))
                keyboardMappings.clear()
                keyboardMappings.putAll(mapOf(
                    "Move Forward" to "W",
                    "Move Backward" to "S",
                    "Strafe Left" to "A",
                    "Strafe Right" to "D",
                    "Jump" to "SPACE",
                    "Sneak" to "L-SHIFT",
                    "Sprint" to "L-CTRL",
                    "Inventory" to "E",
                    "Drop Item" to "Q",
                    "Swap Hand" to "F",
                    "Open Chat" to "T",
                    "Player List" to "TAB",
                    "Perspective" to "F5",
                    "Fullscreen" to "F11",
                    "Zoom" to "C"
                ))
                val mouseJson = mouseMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.mouseRemapBindings.save(mouseJson)
                val kbJson = keyboardMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.keyboardRemapBindings.save(kbJson)
                Toast.makeText(context, "Reset all mappings to default", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showExtraButtonDetector) {
        RemapBindingDialog(
            initialTarget = "btn_side_1",
            mouseMappings = mouseMappings,
            keyboardMappings = keyboardMappings,
            onDismissRequest = { showExtraButtonDetector = false },
            onSaveBinding = { target, newBinding ->
                showExtraButtonDetector = false
                val key = "btn_extra_${System.currentTimeMillis() % 1000}"
                mouseMappings[key] = newBinding
                val mouseJson = mouseMappings.entries.joinToString(";") { "${it.key}=${it.value}" }
                AllSettings.mouseRemapBindings.save(mouseJson)
                Toast.makeText(context, "Added extra button: $newBinding", Toast.LENGTH_SHORT).show()
            },
            onResetTarget = { showExtraButtonDetector = false },
            onResetAll = { showExtraButtonDetector = false }
        )
    }

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.KeyboardMouse, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            isVisible = isVisible
        ) { listScope ->
            // Physical Mouse General Settings
            animatedItem(listScope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsCard(
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_km_mouse_capture_title),
                        summary = stringResource(R.string.settings_km_mouse_capture_summary),
                        unit = AllSettings.physicalMouseMode
                    )
                    IntSliderSettingsCard(
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_km_sensitivity_title),
                        summary = stringResource(R.string.settings_km_sensitivity_summary),
                        unit = AllSettings.mouseCaptureSensitivity,
                        valueRange = AllSettings.mouseCaptureSensitivity.floatRange,
                        suffix = "%",
                        fineTuningControl = true
                    )
                    SwitchSettingsCard(
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_km_hide_cursor_title),
                        summary = stringResource(R.string.settings_km_hide_cursor_summary),
                        unit = AllSettings.hideMouse
                    )
                    SettingsCard(
                        position = CardPosition.Bottom,
                        title = "\ud83c\udfae Remap Buttons & Keys",
                        summary = "Open remap popup to select buttons or press a key/mouse button to identify",
                        onClick = {
                            activeRemapTarget = if (selectedCategory == RemapCategory.MOUSE) "btn_left" else "Move Forward"
                        }
                    )
                }
            }

            // Remap Category Toggle: [Keyboard Keys] | [Mouse Buttons]
            animatedItem(listScope) { yOffset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CheckChip(
                        selected = selectedCategory == RemapCategory.KEYBOARD,
                        label = {
                            Text("⌨️ Keyboard Keys", fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                        },
                        onClick = { selectedCategory = RemapCategory.KEYBOARD }
                    )

                    CheckChip(
                        selected = selectedCategory == RemapCategory.MOUSE,
                        label = {
                            Text("🖱️ Mouse Buttons", fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                        },
                        onClick = { selectedCategory = RemapCategory.MOUSE }
                    )
                }
            }

            // Category: Mouse Buttons Remapping
            if (selectedCategory == RemapCategory.MOUSE) {
                animatedItem(listScope) { yOffset ->
                    SettingsCardColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                    ) {
                        SettingsCard(
                            position = CardPosition.Top,
                            title = "Left Mouse Button (Primary)",
                            summary = "Action: ${mouseMappings["btn_left"]}",
                            onClick = { activeRemapTarget = "btn_left" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = "Right Mouse Button (Secondary)",
                            summary = "Action: ${mouseMappings["btn_right"]}",
                            onClick = { activeRemapTarget = "btn_right" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = "Middle Click (Scroll Wheel)",
                            summary = "Action: ${mouseMappings["btn_middle"]}",
                            onClick = { activeRemapTarget = "btn_middle" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = "Mouse Side Button 1 (Back / X1)",
                            summary = "Action: ${mouseMappings["btn_side_1"]}",
                            onClick = { activeRemapTarget = "btn_side_1" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = "Mouse Side Button 2 (Forward / X2)",
                            summary = "Action: ${mouseMappings["btn_side_2"]}",
                            onClick = { activeRemapTarget = "btn_side_2" }
                        )

                        // Button to detect custom hardware side buttons
                        SettingsCard(
                            position = CardPosition.Bottom,
                            title = "+ Detect & Map Extra Mouse Button",
                            summary = "Click here and press any extra side/DPI button on your mouse to detect and bind it",
                            onClick = { showExtraButtonDetector = true }
                        )
                    }
                }
            }

            // Category: Keyboard Keys Remapping
            if (selectedCategory == RemapCategory.KEYBOARD) {
                animatedItem(listScope) { yOffset ->
                    SettingsCardColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                    ) {
                        val keys = keyboardMappings.entries.toList()
                        keys.forEachIndexed { index, entry ->
                            val position = when {
                                keys.size == 1 -> CardPosition.Single
                                index == 0 -> CardPosition.Top
                                index == keys.size - 1 -> CardPosition.Bottom
                                else -> CardPosition.Middle
                            }

                            SettingsCard(
                                position = position,
                                title = entry.key,
                                summary = "Assigned Key: [ ${entry.value} ]",
                                onClick = { activeRemapTarget = entry.key }
                            )
                        }
                    }
                }
            }

            // Mouse Cursors & Cursor Packs Section
            animatedItem(listScope) { yOffset ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    Text(
                        text = "MOUSE CURSOR & PACKS",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    SettingsCardColumn(modifier = Modifier.fillMaxWidth()) {
                        val cursorPacks = listOf(
                            "Default Classic",
                            "Modern Obsidian",
                            "Precision Crosshair",
                            "Bedrock Arrow",
                            "Chroma Emerald"
                        )

                        cursorPacks.forEachIndexed { idx, pack ->
                            val isSelected = selectedCursorPack == pack
                            val pos = when {
                                idx == 0 -> CardPosition.Top
                                idx == cursorPacks.size - 1 -> CardPosition.Middle
                                else -> CardPosition.Middle
                            }
                            SettingsCard(
                                position = pos,
                                title = pack + if (isSelected) "  (Active)" else "",
                                summary = if (isSelected) "Currently selected cursor pack" else "Click to activate pack",
                                onClick = {
                                    selectedCursorPack = pack
                                    AllSettings.customCursorPack.save(pack)
                                    if (pack == "Default Classic") {
                                        com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile.delete()
                                    }
                                    Toast.makeText(context, "Cursor pack: $pack activated", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Custom Cursor Pack Import
                        SettingsCard(
                            position = CardPosition.Middle,
                            title = "+ Import Custom Cursor (.png / .zip)",
                            summary = "Select an image or cursor zip from your storage to use as mouse pointer",
                            onClick = {
                                cursorPickerLauncher.launch("image/*")
                            }
                        )

                        // Cursor Size Slider
                        IntSliderSettingsCard(
                            position = CardPosition.Bottom,
                            title = "Cursor Scale: ${cursorScale.toInt()}%",
                            summary = "Adjust on-screen virtual pointer scale",
                            valueRange = 50f..200f,
                            unit = AllSettings.mouseCaptureSensitivity,
                            suffix = "%",
                            fineTuningControl = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Remap & Key/Button Detector Dialog
 * Supports both hardware detection (keyboard & mouse) and list selection
 */
data class RemapActionItem(val id: String, val category: String, val name: String, val defaultVal: String)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun RemapBindingDialog(
    initialTarget: String,
    mouseMappings: MutableMap<String, String>,
    keyboardMappings: MutableMap<String, String>,
    onDismissRequest: () -> Unit,
    onSaveBinding: (target: String, newBinding: String) -> Unit,
    onResetTarget: (target: String) -> Unit,
    onResetAll: () -> Unit
) {
    val allActionItems = remember {
        listOf(
            RemapActionItem("btn_left", "Mouse Buttons", "Left Mouse Button (Attack / Destroy)", "Attack / Destroy (Btn 1)"),
            RemapActionItem("btn_right", "Mouse Buttons", "Right Mouse Button (Use / Place Block)", "Use Item / Place Block (Btn 2)"),
            RemapActionItem("btn_middle", "Mouse Buttons", "Middle Click (Pick Block)", "Pick Block (Btn 3)"),
            RemapActionItem("btn_side_1", "Mouse Buttons", "Mouse Side Button 1 (Back / X1)", "Sprint (Key L-CTRL)"),
            RemapActionItem("btn_side_2", "Mouse Buttons", "Mouse Side Button 2 (Forward / X2)", "Sneak (Key L-SHIFT)"),
            RemapActionItem("Move Forward", "Movement", "Move Forward", "W"),
            RemapActionItem("Move Backward", "Movement", "Move Backward", "S"),
            RemapActionItem("Strafe Left", "Movement", "Strafe Left", "A"),
            RemapActionItem("Strafe Right", "Movement", "Strafe Right", "D"),
            RemapActionItem("Jump", "Movement", "Jump", "SPACE"),
            RemapActionItem("Sneak", "Movement", "Sneak", "L-SHIFT"),
            RemapActionItem("Sprint", "Movement", "Sprint", "L-CTRL"),
            RemapActionItem("Inventory", "Gameplay", "Inventory", "E"),
            RemapActionItem("Drop Item", "Gameplay", "Drop Item", "Q"),
            RemapActionItem("Swap Hand", "Gameplay", "Swap Hand (Offhand)", "F"),
            RemapActionItem("Open Chat", "Gameplay", "Open Chat", "T"),
            RemapActionItem("Player List", "Gameplay", "Player List", "TAB"),
            RemapActionItem("Perspective", "Gameplay", "Perspective", "F5"),
            RemapActionItem("Fullscreen", "Gameplay", "Fullscreen", "F11"),
            RemapActionItem("Zoom", "Gameplay", "Zoom", "C")
        )
    }

    var currentTarget by remember { mutableStateOf(initialTarget) }
    var isActionDropdownOpen by remember { mutableStateOf(false) }

    val currentItem = allActionItems.find { it.id == currentTarget } ?: allActionItems[0]
    val isMouse = currentItem.category == "Mouse Buttons"

    var selectedBinding by remember(currentTarget) {
        mutableStateOf(
            if (mouseMappings.containsKey(currentTarget)) mouseMappings[currentTarget] ?: currentItem.defaultVal
            else keyboardMappings[currentTarget] ?: currentItem.defaultVal
        )
    }
    var isDetecting by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(if (isMouse) 0 else 1) }

    val view = LocalView.current
    DisposableEffect(isDetecting) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (isDetecting && window != null) {
            val oldCallback = window.callback
            window.callback = object : Window.Callback by oldCallback {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val keyName = when (event.keyCode) {
                            KeyEvent.KEYCODE_W -> "W"
                            KeyEvent.KEYCODE_A -> "A"
                            KeyEvent.KEYCODE_S -> "S"
                            KeyEvent.KEYCODE_D -> "D"
                            KeyEvent.KEYCODE_SPACE -> "SPACE"
                            KeyEvent.KEYCODE_SHIFT_LEFT -> "L-SHIFT"
                            KeyEvent.KEYCODE_SHIFT_RIGHT -> "R-SHIFT"
                            KeyEvent.KEYCODE_CTRL_LEFT -> "L-CTRL"
                            KeyEvent.KEYCODE_CTRL_RIGHT -> "R-CTRL"
                            KeyEvent.KEYCODE_ALT_LEFT -> "L-ALT"
                            KeyEvent.KEYCODE_ALT_RIGHT -> "R-ALT"
                            KeyEvent.KEYCODE_TAB -> "TAB"
                            KeyEvent.KEYCODE_ESCAPE -> "ESCAPE"
                            KeyEvent.KEYCODE_ENTER -> "ENTER"
                            KeyEvent.KEYCODE_DEL -> "BACKSPACE"
                            KeyEvent.KEYCODE_E -> "E"
                            KeyEvent.KEYCODE_Q -> "Q"
                            KeyEvent.KEYCODE_F -> "F"
                            KeyEvent.KEYCODE_R -> "R"
                            KeyEvent.KEYCODE_C -> "C"
                            KeyEvent.KEYCODE_T -> "T"
                            KeyEvent.KEYCODE_Z -> "Z"
                            KeyEvent.KEYCODE_X -> "X"
                            KeyEvent.KEYCODE_V -> "V"
                            KeyEvent.KEYCODE_B -> "B"
                            KeyEvent.KEYCODE_G -> "G"
                            KeyEvent.KEYCODE_H -> "H"
                            KeyEvent.KEYCODE_M -> "M"
                            KeyEvent.KEYCODE_1 -> "1"
                            KeyEvent.KEYCODE_2 -> "2"
                            KeyEvent.KEYCODE_3 -> "3"
                            KeyEvent.KEYCODE_4 -> "4"
                            KeyEvent.KEYCODE_5 -> "5"
                            KeyEvent.KEYCODE_6 -> "6"
                            KeyEvent.KEYCODE_7 -> "7"
                            KeyEvent.KEYCODE_8 -> "8"
                            KeyEvent.KEYCODE_9 -> "9"
                            KeyEvent.KEYCODE_0 -> "0"
                            KeyEvent.KEYCODE_F1 -> "F1"
                            KeyEvent.KEYCODE_F2 -> "F2"
                            KeyEvent.KEYCODE_F3 -> "F3"
                            KeyEvent.KEYCODE_F4 -> "F4"
                            KeyEvent.KEYCODE_F5 -> "F5"
                            KeyEvent.KEYCODE_F11 -> "F11"
                            else -> KeyEvent.keyCodeToString(event.keyCode).removePrefix("KEYCODE_")
                        }
                        if (keyName.isNotBlank()) {
                            selectedBinding = keyName
                            onSaveBinding(currentTarget, keyName)
                            isDetecting = false
                            return true
                        }
                    }
                    return oldCallback.dispatchKeyEvent(event)
                }

                override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
                    if (event.actionMasked == MotionEvent.ACTION_BUTTON_PRESS || event.actionMasked == MotionEvent.ACTION_DOWN) {
                        val bs = event.buttonState
                        val mouseName = when {
                            (bs and MotionEvent.BUTTON_PRIMARY) != 0 -> "Mouse Left Click"
                            (bs and MotionEvent.BUTTON_SECONDARY) != 0 -> "Mouse Right Click"
                            (bs and MotionEvent.BUTTON_TERTIARY) != 0 -> "Mouse Middle Click"
                            (bs and MotionEvent.BUTTON_BACK) != 0 -> "Mouse Button 4 (Back)"
                            (bs and MotionEvent.BUTTON_FORWARD) != 0 -> "Mouse Button 5 (Forward)"
                            else -> null
                        }
                        if (mouseName != null) {
                            selectedBinding = mouseName
                            onSaveBinding(currentTarget, mouseName)
                            isDetecting = false
                            return true
                        }
                    }
                    if (event.actionMasked == MotionEvent.ACTION_SCROLL) {
                        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                        if (vScroll > 0) {
                            selectedBinding = "Mouse Wheel Up"
                            onSaveBinding(currentTarget, "Mouse Wheel Up")
                            isDetecting = false
                            return true
                        } else if (vScroll < 0) {
                            selectedBinding = "Mouse Wheel Down"
                            onSaveBinding(currentTarget, "Mouse Wheel Down")
                            isDetecting = false
                            return true
                        }
                    }
                    return oldCallback.dispatchGenericMotionEvent(event)
                }
            }
            onDispose {
                window.callback = oldCallback
            }
        } else {
            onDispose {}
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = !isDetecting
        )
    ) {
        val scrollState = rememberScrollState()
        MinecraftPixelBorderContainer(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 12.dp),
            borderColor = if (isDetecting) Color(0xFF55FF55) else Color(0xFF4A4A50)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REMAP BUTTONS & KEYS",
                        color = Color(0xFF55FF55),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Action Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E2024))
                            .border(BorderStroke(1.dp, Color(0xFF383C48)), RoundedCornerShape(4.dp))
                            .clickable { isActionDropdownOpen = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SELECT ACTION TO REMAP:",
                                color = Color(0xFF888888),
                                fontSize = 10.sp,
                                fontFamily = MinecraftFontFamily
                            )
                            Text(
                                text = currentItem.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = MinecraftFontFamily
                            )
                        }
                        Text("▼", color = Color(0xFF55FF55), fontSize = 12.sp, fontFamily = MinecraftFontFamily)
                    }

                    DropdownMenu(
                        expanded = isActionDropdownOpen,
                        onDismissRequest = { isActionDropdownOpen = false },
                        modifier = Modifier.background(Color(0xFF1B1B1E))
                    ) {
                        allActionItems.forEach { item ->
                            val bound = if (mouseMappings.containsKey(item.id)) mouseMappings[item.id] else keyboardMappings[item.id]
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.name,
                                            color = if (item.id == currentTarget) Color(0xFF55FF55) else Color.White,
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "[ ${bound ?: item.defaultVal} ]",
                                            color = Color(0xFFAAAAAA),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    currentTarget = item.id
                                    selectedTab = if (item.category == "Mouse Buttons") 0 else 1
                                    isActionDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Currently Selected Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF141416))
                        .border(BorderStroke(1.dp, Color(0xFF2E2E32)), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Currently Bound To:",
                            color = Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF233B26))
                                .border(BorderStroke(1.dp, Color(0xFF55FF55)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedBinding.ifEmpty { "None" },
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // DETECT KEY / MOUSE BUTTON BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDetecting) Color(0xFF1C3A24) else Color(0xFF222226))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isDetecting) Color(0xFF55FF55) else Color(0xFF3E3E44)
                            ),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { isDetecting = !isDetecting }
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mouse_filled),
                                contentDescription = null,
                                tint = if (isDetecting) Color(0xFF55FF55) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isDetecting) "🎯 LISTENING... PRESS ANY KEY OR MOUSE BUTTON" else "🔘 AUTO DETECT WHICH BUTTON PRESSED",
                                color = if (isDetecting) Color(0xFF55FF55) else Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (isDetecting) "Press any keyboard key or click any mouse button (including side buttons) now!" else "Click here to identify which button is pressed and map it automatically",
                            color = if (isDetecting) Color(0xFFAAFFAA) else Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                // Category Selector Tabs for Manual Selection
                Text(
                    text = "OR SELECT FROM LIST:",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("Mouse Buttons", "Actions & Movement", "Numbers & F-Keys")
                    tabs.forEachIndexed { idx, tabTitle ->
                        val isTabSelected = selectedTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isTabSelected) Color(0xFF2D4B34) else Color(0xFF1E1E22))
                                .border(
                                    BorderStroke(1.dp, if (isTabSelected) Color(0xFF55FF55) else Color(0xFF38383E)),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { selectedTab = idx }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                color = if (isTabSelected) Color(0xFF55FF55) else Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // List of Chips for chosen Tab
                val itemsForTab = when (selectedTab) {
                    0 -> listOf(
                        "Mouse Left Click",
                        "Mouse Right Click",
                        "Mouse Middle Click",
                        "Mouse Button 4 (Back)",
                        "Mouse Button 5 (Forward)",
                        "Mouse Wheel Up",
                        "Mouse Wheel Down"
                    )
                    1 -> listOf(
                        "W", "A", "S", "D", "SPACE", "L-SHIFT", "L-CTRL", "L-ALT",
                        "E", "Q", "F", "R", "C", "T", "TAB", "ESCAPE", "ENTER",
                        "BACKSPACE", "Z", "X", "V", "B", "G", "H", "M"
                    )
                    else -> listOf(
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                        "F1", "F2", "F3", "F4", "F5", "F11"
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsForTab.forEach { item ->
                        val isSelected = selectedBinding.equals(item, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) Color(0xFF1C3A24) else Color(0xFF26262B))
                                .border(
                                    BorderStroke(1.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF3E3E44)),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable {
                                    selectedBinding = item
                                    onSaveBinding(currentTarget, item)
                                    isDetecting = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item,
                                color = if (isSelected) Color(0xFF55FF55) else Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Action Buttons Row (Reset All, Reset Action, Done)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MinecraftButton(
                        onClick = onResetAll,
                        style = MinecraftButtonStyle.STONE,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Reset All", fontSize = 10.5.sp, fontFamily = MinecraftFontFamily, color = Color(0xFFFF6666))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MinecraftButton(
                            onClick = { onResetTarget(currentTarget) },
                            style = MinecraftButtonStyle.STONE,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Reset Action", fontSize = 10.5.sp, fontFamily = MinecraftFontFamily)
                        }

                        MinecraftButton(
                            onClick = onDismissRequest,
                            style = MinecraftButtonStyle.GREEN,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Done", fontSize = 10.5.sp, fontFamily = MinecraftFontFamily)
                        }
                    }
                }
            }
        }
    }
}
