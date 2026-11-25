/*
 * Zalith Launcher 2 / Cubiq Launcher
 */

package com.movtery.zalithlauncher.ui.screens.content.settings

import android.widget.Toast
import android.content.Context


import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.mutableIntStateOf
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
                Toast.makeText(context, context.getString(R.string.cubiq_km_cursor_imported), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, context.getString(R.string.cubiq_km_cursor_import_failed, it.localizedMessage), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context.getString(R.string.cubiq_km_binding_saved, context.remapDisplayLabel(target), context.remapDisplayLabel(newBinding)), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context.getString(R.string.cubiq_km_binding_reset, context.remapDisplayLabel(target)), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context.getString(R.string.cubiq_km_all_reset), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, context.getString(R.string.cubiq_km_extra_added, context.remapDisplayLabel(newBinding)), Toast.LENGTH_SHORT).show()
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
                        title = stringResource(R.string.cubiq_km_remap_card),
                        summary = stringResource(R.string.cubiq_km_remap_card_desc),
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
                            Text(stringResource(R.string.cubiq_km_keyboard_keys), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
                        },
                        onClick = { selectedCategory = RemapCategory.KEYBOARD }
                    )

                    CheckChip(
                        selected = selectedCategory == RemapCategory.MOUSE,
                        label = {
                            Text(stringResource(R.string.cubiq_km_mouse_buttons), fontFamily = MinecraftFontFamily, fontSize = 12.sp)
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
                            title = stringResource(R.string.cubiq_km_left_mouse),
                            summary = stringResource(R.string.cubiq_km_binding_action, mouseMappings["btn_left"]?.let { context.remapDisplayLabel(it) } ?: stringResource(R.string.cubiq_km_label_none)),
                            onClick = { activeRemapTarget = "btn_left" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = stringResource(R.string.cubiq_km_right_mouse),
                            summary = stringResource(R.string.cubiq_km_binding_action, mouseMappings["btn_right"]?.let { context.remapDisplayLabel(it) } ?: stringResource(R.string.cubiq_km_label_none)),
                            onClick = { activeRemapTarget = "btn_right" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = stringResource(R.string.cubiq_km_middle_mouse),
                            summary = stringResource(R.string.cubiq_km_binding_action, mouseMappings["btn_middle"]?.let { context.remapDisplayLabel(it) } ?: stringResource(R.string.cubiq_km_label_none)),
                            onClick = { activeRemapTarget = "btn_middle" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = stringResource(R.string.cubiq_km_side_x1),
                            summary = stringResource(R.string.cubiq_km_binding_action, mouseMappings["btn_side_1"]?.let { context.remapDisplayLabel(it) } ?: stringResource(R.string.cubiq_km_label_none)),
                            onClick = { activeRemapTarget = "btn_side_1" }
                        )

                        SettingsCard(
                            position = CardPosition.Middle,
                            title = stringResource(R.string.cubiq_km_side_x2),
                            summary = stringResource(R.string.cubiq_km_binding_action, mouseMappings["btn_side_2"]?.let { context.remapDisplayLabel(it) } ?: stringResource(R.string.cubiq_km_label_none)),
                            onClick = { activeRemapTarget = "btn_side_2" }
                        )

                        // Button to detect custom hardware side buttons
                        SettingsCard(
                            position = CardPosition.Bottom,
                            title = stringResource(R.string.cubiq_km_detect_extra),
                            summary = stringResource(R.string.cubiq_km_detect_extra_desc),
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
                                title = context.remapDisplayLabel(entry.key),
                                summary = stringResource(R.string.cubiq_km_assigned_key, context.remapDisplayLabel(entry.value)),
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
                        text = stringResource(R.string.cubiq_km_cursor_packs),
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
                                title = if (isSelected) stringResource(R.string.cubiq_km_pack_active_label, context.remapDisplayLabel(pack)) else context.remapDisplayLabel(pack),
                                summary = if (isSelected) stringResource(R.string.cubiq_km_cursor_pack_active) else stringResource(R.string.cubiq_km_cursor_pack_select),
                                onClick = {
                                    selectedCursorPack = pack
                                    AllSettings.customCursorPack.save(pack)
                                    if (pack == "Default Classic") {
                                        com.movtery.zalithlauncher.ui.control.mouse.arrowPointerFile.delete()
                                    }
                                    Toast.makeText(context, context.getString(R.string.cubiq_km_pack_activated, context.remapDisplayLabel(pack)), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Custom Cursor Pack Import
                        SettingsCard(
                            position = CardPosition.Middle,
                            title = stringResource(R.string.cubiq_km_cursor_import),
                            summary = stringResource(R.string.cubiq_km_cursor_import_desc),
                            onClick = {
                                cursorPickerLauncher.launch("image/*")
                            }
                        )

                        // Cursor Size Slider
                        IntSliderSettingsCard(
                            position = CardPosition.Bottom,
                            title = stringResource(R.string.cubiq_km_cursor_scale, cursorScale.toInt()),
                            summary = stringResource(R.string.cubiq_km_cursor_scale_desc),
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
    val context = LocalContext.current
    val allActionItems = run {
        listOf(
            RemapActionItem("btn_left", "Mouse Buttons", stringResource(R.string.cubiq_km_action_left_mouse), "Attack / Destroy (Btn 1)"),
            RemapActionItem("btn_right", "Mouse Buttons", stringResource(R.string.cubiq_km_action_right_mouse), "Use Item / Place Block (Btn 2)"),
            RemapActionItem("btn_middle", "Mouse Buttons", stringResource(R.string.cubiq_km_action_middle_mouse), "Pick Block (Btn 3)"),
            RemapActionItem("btn_side_1", "Mouse Buttons", stringResource(R.string.cubiq_km_side_x1), "Sprint (Key L-CTRL)"),
            RemapActionItem("btn_side_2", "Mouse Buttons", stringResource(R.string.cubiq_km_side_x2), "Sneak (Key L-SHIFT)"),
            RemapActionItem("Move Forward", "Movement", context.remapDisplayLabel("Move Forward"), "W"),
            RemapActionItem("Move Backward", "Movement", context.remapDisplayLabel("Move Backward"), "S"),
            RemapActionItem("Strafe Left", "Movement", context.remapDisplayLabel("Strafe Left"), "A"),
            RemapActionItem("Strafe Right", "Movement", context.remapDisplayLabel("Strafe Right"), "D"),
            RemapActionItem("Jump", "Movement", context.remapDisplayLabel("Jump"), "SPACE"),
            RemapActionItem("Sneak", "Movement", context.remapDisplayLabel("Sneak"), "L-SHIFT"),
            RemapActionItem("Sprint", "Movement", context.remapDisplayLabel("Sprint"), "L-CTRL"),
            RemapActionItem("Inventory", "Gameplay", context.remapDisplayLabel("Inventory"), "E"),
            RemapActionItem("Drop Item", "Gameplay", context.remapDisplayLabel("Drop Item"), "Q"),
            RemapActionItem("Swap Hand", "Gameplay", stringResource(R.string.cubiq_km_swap_offhand), "F"),
            RemapActionItem("Open Chat", "Gameplay", context.remapDisplayLabel("Open Chat"), "T"),
            RemapActionItem("Player List", "Gameplay", context.remapDisplayLabel("Player List"), "TAB"),
            RemapActionItem("Perspective", "Gameplay", context.remapDisplayLabel("Perspective"), "F5"),
            RemapActionItem("Fullscreen", "Gameplay", context.remapDisplayLabel("Fullscreen"), "F11"),
            RemapActionItem("Zoom", "Gameplay", context.remapDisplayLabel("Zoom"), "C")
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
                        text = stringResource(R.string.cubiq_km_remap_title),
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
                            contentDescription = stringResource(R.string.generic_close),
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
                                text = stringResource(R.string.cubiq_km_select_action),
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
                                            text = "[ ${context.remapDisplayLabel(bound ?: item.defaultVal)} ]",
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
                            text = stringResource(R.string.cubiq_km_currently_bound),
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
                                text = context.remapDisplayLabel(selectedBinding.ifEmpty { "None" }),
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
                                text = if (isDetecting) stringResource(R.string.cubiq_km_detect_listening) else stringResource(R.string.cubiq_km_detect_auto),
                                color = if (isDetecting) Color(0xFF55FF55) else Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (isDetecting) stringResource(R.string.cubiq_km_detect_listening_desc) else stringResource(R.string.cubiq_km_detect_auto_desc),
                            color = if (isDetecting) Color(0xFFAAFFAA) else Color(0xFF888888),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.sp
                        )
                    }
                }

                // Mapping Radio List (Pojav / ZL1 Style in Cubiq Theme)
                Text(
                    text = stringResource(R.string.cubiq_km_select_key),
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                val allKeysList = remember {
                    listOf(
                        // Mouse Buttons
                        "Mouse Left Click", "Mouse Right Click", "Mouse Middle Click",
                        "Mouse Button 4 (Back)", "Mouse Button 5 (Forward)",
                        "Mouse Wheel Up", "Mouse Wheel Down",
                        // D-Pad / Controller
                        "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT",
                        // Movement & Keys
                        "SPACE", "L-SHIFT", "R-SHIFT", "L-CTRL", "R-CTRL", "L-ALT", "R-ALT",
                        "TAB", "ESCAPE", "ENTER", "BACKSPACE",
                        // Letters
                        "W", "A", "S", "D", "E", "Q", "F", "R", "C", "T", "Z", "X", "V", "B", "G", "H", "M",
                        "I", "J", "K", "L", "O", "P", "U", "Y", "N",
                        // Numbers
                        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                        // Function Keys
                        "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
                    )
                }

                val categories = listOf("All", "Mouse", "D-Pad", "Move", "Letters", "Numbers", "F-Keys")
                var selectedCategoryIndex by remember { mutableIntStateOf(0) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEachIndexed { idx, catTitle ->
                        val isTabSelected = selectedCategoryIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isTabSelected) Color(0xFF2E7D32) else Color(0xFF1E1E22))
                                .border(
                                    BorderStroke(1.dp, if (isTabSelected) Color(0xFF55FF55) else Color(0xFF38383E)),
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { selectedCategoryIndex = idx }
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = context.remapDisplayLabel(catTitle),
                                color = if (isTabSelected) Color.White else Color(0xFFAAAAAA),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                val filteredKeys = remember(selectedCategoryIndex) {
                    allKeysList.filter { key ->
                        when (selectedCategoryIndex) {
                            1 -> key.startsWith("Mouse")
                            2 -> key.startsWith("DPAD_")
                            3 -> key in listOf("SPACE", "L-SHIFT", "R-SHIFT", "L-CTRL", "R-CTRL", "L-ALT", "R-ALT", "TAB", "ESCAPE", "ENTER", "BACKSPACE")
                            4 -> key.length == 1 && key[0] in 'A'..'Z'
                            5 -> key.length == 1 && key[0] in '0'..'9'
                            6 -> key.startsWith("F") && key.length in 2..3
                            else -> true
                        }
                    }
                }

                // Scrollable Column with Pojav/ZL1 radio items in Cubiq style
                val listScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 145.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF16171B))
                        .border(BorderStroke(1.dp, Color(0xFF2E303A)), RoundedCornerShape(4.dp))
                        .verticalScroll(listScrollState)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    filteredKeys.forEach { item ->
                        val isSelected = selectedBinding.equals(item, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) Color(0xFF1E3825) else Color.Transparent)
                                .clickable {
                                    selectedBinding = item
                                    onSaveBinding(currentTarget, item)
                                    isDetecting = false
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = context.remapDisplayLabel(item),
                                color = if (isSelected) Color(0xFF55FF55) else Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                            // Circular Radio Button Indicator
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .border(
                                        BorderStroke(1.5.dp, if (isSelected) Color(0xFF55FF55) else Color(0xFF666670)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF55FF55))
                                    )
                                }
                            }
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
                        Text(stringResource(R.string.cubiq_km_reset_all), fontSize = 10.5.sp, fontFamily = MinecraftFontFamily, color = Color(0xFFFF6666))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MinecraftButton(
                            onClick = { onResetTarget(currentTarget) },
                            style = MinecraftButtonStyle.STONE,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(stringResource(R.string.cubiq_km_reset_action), fontSize = 10.5.sp, fontFamily = MinecraftFontFamily)
                        }

                        MinecraftButton(
                            onClick = onDismissRequest,
                            style = MinecraftButtonStyle.GREEN,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(stringResource(R.string.generic_done), fontSize = 10.5.sp, fontFamily = MinecraftFontFamily)
                        }
                    }
                }
            }
        }
    }
}

internal fun Context.remapDisplayLabel(id: String): String = when (id) {
    "All" -> getString(R.string.cubiq_km_label_all)
    "Mouse" -> getString(R.string.cubiq_km_label_mouse)
    "D-Pad" -> getString(R.string.cubiq_km_label_dpad)
    "Move" -> getString(R.string.cubiq_km_label_move)
    "Letters" -> getString(R.string.cubiq_km_label_letters)
    "Numbers" -> getString(R.string.cubiq_km_label_numbers)
    "F-Keys" -> getString(R.string.cubiq_km_label_function_keys)
    "Primary Action (Attack / Mine)" -> getString(R.string.cubiq_km_label_primary)
    "Secondary Action (Use / Place)" -> getString(R.string.cubiq_km_label_secondary)
    "Pick Block" -> getString(R.string.cubiq_km_label_pick_block)
    "Scroll Next Hotbar Slot" -> getString(R.string.cubiq_km_label_hotbar_next)
    "Scroll Prev Hotbar Slot" -> getString(R.string.cubiq_km_label_hotbar_previous)
    "Walk Forward" -> getString(R.string.cubiq_km_label_walk_forward)
    "Walk Backward" -> getString(R.string.cubiq_km_label_walk_backward)
    "Move Forward" -> getString(R.string.cubiq_km_label_forward)
    "Move Backward" -> getString(R.string.cubiq_km_label_backward)
    "Strafe Left" -> getString(R.string.cubiq_km_label_strafe_left)
    "Strafe Right" -> getString(R.string.cubiq_km_label_strafe_right)
    "Jump" -> getString(R.string.cubiq_km_label_jump)
    "Sneak" -> getString(R.string.cubiq_km_label_sneak)
    "Sprint" -> getString(R.string.cubiq_km_label_sprint)
    "Inventory" -> getString(R.string.cubiq_km_label_inventory)
    "Drop Item" -> getString(R.string.cubiq_km_label_drop)
    "Swap Hand" -> getString(R.string.cubiq_km_label_swap)
    "Chat" -> getString(R.string.cubiq_km_label_chat)
    "Open Chat" -> getString(R.string.cubiq_km_label_open_chat)
    "Player List" -> getString(R.string.cubiq_km_label_players)
    "Perspective" -> getString(R.string.cubiq_km_label_perspective)
    "Change Perspective" -> getString(R.string.cubiq_km_label_change_perspective)
    "Debug Screen" -> getString(R.string.cubiq_km_label_debug_screen)
    "Fullscreen" -> getString(R.string.cubiq_km_label_fullscreen)
    "Zoom" -> getString(R.string.cubiq_km_label_zoom)
    "Left Click (Button 1)" -> getString(R.string.cubiq_km_label_click_left)
    "Right Click (Button 2)" -> getString(R.string.cubiq_km_label_click_right)
    "Middle Click (Button 3)" -> getString(R.string.cubiq_km_label_click_middle)
    "Mouse 4 (Back)" -> getString(R.string.cubiq_km_label_mouse4)
    "Mouse 5 (Forward)" -> getString(R.string.cubiq_km_label_mouse5)
    "Mouse Wheel Up" -> getString(R.string.cubiq_km_label_wheel_up)
    "Mouse Wheel Down" -> getString(R.string.cubiq_km_label_wheel_down)
    "DPAD_UP" -> getString(R.string.cubiq_km_label_dpad_up)
    "DPAD_DOWN" -> getString(R.string.cubiq_km_label_dpad_down)
    "DPAD_LEFT" -> getString(R.string.cubiq_km_label_dpad_left)
    "DPAD_RIGHT" -> getString(R.string.cubiq_km_label_dpad_right)
    "Up Arrow" -> getString(R.string.cubiq_km_label_arrow_up)
    "Down Arrow" -> getString(R.string.cubiq_km_label_arrow_down)
    "Left Arrow" -> getString(R.string.cubiq_km_label_arrow_left)
    "Right Arrow" -> getString(R.string.cubiq_km_label_arrow_right)
    "Space" -> getString(R.string.cubiq_km_label_space)
    "Left Shift" -> getString(R.string.cubiq_km_label_left_shift)
    "Right Shift" -> getString(R.string.cubiq_km_label_right_shift)
    "Left Control" -> getString(R.string.cubiq_km_label_left_control)
    "Right Control" -> getString(R.string.cubiq_km_label_right_control)
    "Left Alt" -> getString(R.string.cubiq_km_label_left_alt)
    "Right Alt" -> getString(R.string.cubiq_km_label_right_alt)
    "Enter" -> getString(R.string.cubiq_km_label_enter)
    "Backspace" -> getString(R.string.cubiq_km_label_backspace)
    "Attack / Destroy (Btn 1)" -> getString(R.string.cubiq_km_label_attack)
    "Use Item / Place Block (Btn 2)" -> getString(R.string.cubiq_km_label_use)
    "Pick Block (Btn 3)" -> getString(R.string.cubiq_km_label_pick)
    "Sprint (Key L-CTRL)" -> getString(R.string.cubiq_km_label_sprint_ctrl)
    "Sneak (Key L-SHIFT)" -> getString(R.string.cubiq_km_label_sneak_shift)
    "Mouse Left Click" -> getString(R.string.cubiq_km_label_mouse_left)
    "Mouse Right Click" -> getString(R.string.cubiq_km_label_mouse_right)
    "Mouse Middle Click" -> getString(R.string.cubiq_km_label_mouse_middle)
    "Mouse Button 4 (Back)" -> getString(R.string.cubiq_km_label_mouse_button_4)
    "Mouse Button 5 (Forward)" -> getString(R.string.cubiq_km_label_mouse_button_5)
    "SPACE" -> getString(R.string.cubiq_km_label_space_caps)
    "L-SHIFT" -> getString(R.string.cubiq_km_label_l_shift)
    "R-SHIFT" -> getString(R.string.cubiq_km_label_r_shift)
    "L-CTRL" -> getString(R.string.cubiq_km_label_l_ctrl)
    "R-CTRL" -> getString(R.string.cubiq_km_label_r_ctrl)
    "L-ALT" -> getString(R.string.cubiq_km_label_l_alt)
    "R-ALT" -> getString(R.string.cubiq_km_label_r_alt)
    "ENTER" -> getString(R.string.cubiq_km_label_enter_caps)
    "BACKSPACE" -> getString(R.string.cubiq_km_label_backspace_caps)
    "None" -> getString(R.string.cubiq_km_label_none)
    "Default" -> getString(R.string.cubiq_km_label_default)
    "Default Classic" -> getString(R.string.cubiq_km_label_cursor_classic)
    "Modern Obsidian" -> getString(R.string.cubiq_km_label_cursor_obsidian)
    "Precision Crosshair" -> getString(R.string.cubiq_km_label_cursor_crosshair)
    "Bedrock Arrow" -> getString(R.string.cubiq_km_label_cursor_bedrock)
    "Chroma Emerald" -> getString(R.string.cubiq_km_label_cursor_emerald)
    "Custom Imported" -> getString(R.string.cubiq_km_label_cursor_custom)
    "btn_left" -> getString(R.string.cubiq_km_left_mouse)
    "btn_right" -> getString(R.string.cubiq_km_right_mouse)
    "btn_middle" -> getString(R.string.cubiq_km_middle_mouse)
    "btn_side_1" -> getString(R.string.cubiq_km_side_x1)
    "btn_side_2" -> getString(R.string.cubiq_km_side_x2)
    else -> if (id.startsWith("Key ") && id.length == 5) getString(R.string.cubiq_km_letter_key, id.last().toString()) else id
}
