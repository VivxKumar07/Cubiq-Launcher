/*
 * Zalith Launcher 2 / Cubiq Launcher
 */

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.viewmodel.EventViewModel

@Composable
fun KeyboardMouseSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.KeyboardMouse, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            isVisible = isVisible
        ) { listScope ->
            // Physical Mouse Section
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
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_km_hide_cursor_title),
                        summary = stringResource(R.string.settings_km_hide_cursor_summary),
                        unit = AllSettings.hideMouse
                    )
                }
            }


        }
    }
}
