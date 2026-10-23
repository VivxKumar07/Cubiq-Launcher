/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.layer_controller.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.layer_controller.R
import com.movtery.layer_controller.data.TextAlignment
import com.movtery.layer_controller.event.EventHandler
import com.movtery.layer_controller.observable.DefaultObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.LocalButtonScale
import com.movtery.layer_controller.utils.buttonContentColorAsState
import com.movtery.layer_controller.utils.buttonFontSizeAsState
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.buttonStyle
import com.movtery.layer_controller.utils.editMode
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.SnapMode

private fun resolveIconDrawable(iconName: String?, isPressed: Boolean): Int? {
    if (iconName.isNullOrBlank()) return null
    val clean = iconName.trim().lowercase()
    return when {
        clean == "attack" || clean == "attack_button" || clean == "sword" -> {
            if (isPressed) R.drawable.attack_pressed else R.drawable.attack
        }
        clean == "interact_button" || clean == "interact" || clean == "use_item" || clean == "place" || clean == "use" -> {
            if (isPressed) R.drawable.interact_button_pressed else R.drawable.interact_button
        }
        clean == "pickaxe" -> {
            if (isPressed) R.drawable.use_item_pressed else R.drawable.use_item
        }
        clean == "jump" || clean == "jump_button" || clean == "jump_button_be2" -> {
            if (isPressed) R.drawable.jump_pressed else R.drawable.jump
        }
        clean == "sneak" || clean == "sneak_button" -> {
            if (isPressed) R.drawable.sneak_pressed else R.drawable.sneak
        }
        clean == "sprint" || clean == "sprint_button" -> {
            if (isPressed) R.drawable.sprint_pressed else R.drawable.sprint
        }
        clean == "inventory" || clean == "inv" || clean == "..." -> {
            if (isPressed) R.drawable.inventory_pressed else R.drawable.inventory
        }
        clean == "player_list" || clean == "tab" -> R.drawable.player_list
        clean == "f3" || clean == "f3_debug" -> {
            if (isPressed) R.drawable.f3_debug_pressed else R.drawable.f3_debug
        }
        clean == "perspective" || clean == "f5" || clean == "cam" -> {
            if (isPressed) R.drawable.perspective_pressed else R.drawable.perspective
        }
        clean == "keyboard" || clean == "key" -> {
            if (isPressed) R.drawable.keyboard_pressed else R.drawable.keyboard
        }
        clean == "gui" -> {
            if (isPressed) R.drawable.gui_pressed else R.drawable.gui
        }
        clean == "menu_button" || clean == "menu" -> {
            if (isPressed) R.drawable.menu_button_pressed else R.drawable.menu_button
        }
        clean == "chat" -> {
            if (isPressed) R.drawable.chat_pressed else R.drawable.chat
        }
        clean == "pause" || clean == "esc" -> {
            if (isPressed) R.drawable.pause_pressed else R.drawable.pause
        }
        clean == "swap_hands" || clean == "swap" -> {
            if (isPressed) R.drawable.swap_hands_pressed else R.drawable.swap_hands
        }
        clean == "drop_item" || clean == "drop" -> R.drawable.drop_item
        clean == "dismount" -> {
            if (isPressed) R.drawable.dismount_pressed else R.drawable.dismount
        }
        clean == "pick_block" || clean == "pick" -> {
            if (isPressed) R.drawable.pick_block_pressed else R.drawable.pick_block_button
        }
        clean == "forward" || clean == "forward_button_be2" || clean == "up" || clean == "dpad_up" -> {
            if (isPressed) R.drawable.forward_button_be2_pressed else R.drawable.forward_button_be2
        }
        clean == "backward" || clean == "backward_button_be2" || clean == "down" || clean == "dpad_down" -> {
            if (isPressed) R.drawable.backward_button_be2_pressed else R.drawable.backward_button_be2
        }
        clean == "left" || clean == "left_button_be2" || clean == "dpad_left" -> {
            if (isPressed) R.drawable.left_button_be2_pressed else R.drawable.left_button_be2
        }
        clean == "right" || clean == "right_button_be2" || clean == "dpad_right" -> {
            if (isPressed) R.drawable.right_button_be2_pressed else R.drawable.right_button_be2
        }
        clean == "sneak_button_be2" || clean == "dpad_center" -> {
            if (isPressed) R.drawable.sneak_button_be2_pressed else R.drawable.sneak_button_be2
        }
        clean == "flying_ascend_button" || clean == "water_ascend" -> {
            if (isPressed) R.drawable.water_ascend_button_pressed else R.drawable.water_ascend_button
        }
        clean == "flying_descend_button" || clean == "water_descend" -> {
            if (isPressed) R.drawable.water_descend_button_pressed else R.drawable.water_descend_button
        }
        clean == "shield" -> R.drawable.shield
        clean == "crosshair" -> R.drawable.crosshair
        clean == "arrow_up" -> R.drawable.arrow_up
        clean == "arrow_down" -> R.drawable.arrow_down
        clean == "arrow_left" -> R.drawable.arrow_left
        clean == "arrow_right" -> R.drawable.arrow_right
        else -> null
    }
}

private data class ButtonTextStyle(
    val text: ObservableTranslatableString,
    val textAlignment: TextAlignment,
    val textBold: Boolean,
    val textItalic: Boolean,
    val textUnderline: Boolean
)

/**
 * 基础文本控件
 * @param allStyles 当前控制布局所有的样式（用于加载控件的样式）
 * @param enableSnap 编辑模式下，是否开启吸附功能
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param getOtherWidgets 获取其他控件的信息，在编辑模式下，用于计算吸附位置
 * @param snapThresholdValue 吸附距离阈值
 * @param eventHandler 事件处理器
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
internal fun TextButton(
    isEditMode: Boolean,
    data: ObservableWidget,
    allStyles: List<ObservableButtonStyle>,
    screenSize: IntSize,
    isDark: Boolean = isSystemInDarkTheme(),
    visible: Boolean = true,
    enableSnap: Boolean = false,
    snapMode: SnapMode = SnapMode.FullScreen,
    localSnapRange: Dp = 50.dp,
    getOtherWidgets: () -> List<ObservableWidget>,
    snapThresholdValue: Dp,
    eventHandler: EventHandler? = null,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit = { _, _ -> },
    onLineCancel: (ObservableWidget) -> Unit = {},
    isPressed: Boolean,
    onTapInEditMode: () -> Unit = {}
) {
    if (visible && !data.isTemporarilyHidden) {
        val styleId = data.styleId
        val style = allStyles
            .takeIf { data.styleId != null }
            ?.find { it.uuid == styleId }
            ?: DefaultObservableButtonStyle

        val locale = LocalConfiguration.current.locales[0]

        val explicitIcon = (data as? ObservableNormalData)?.icon
        val resolvedIcon = resolveIconDrawable(explicitIcon, isPressed)
        val hasIcon = resolvedIcon != null
        val customOpacity = (data as? ObservableNormalData)?.opacity

        Box(
            modifier = Modifier
                .buttonSize(data, screenSize)
                .then(if (customOpacity != null) Modifier.alpha(customOpacity) else Modifier)
                .buttonStyle(
                    style = style,
                    isDark = isDark,
                    isPressed = if (hasIcon) false else isPressed
                )
                .editMode(
                    isEditMode = isEditMode,
                    data = data,
                    screenSize = screenSize,
                    enableSnap = enableSnap,
                    snapMode = snapMode,
                    localSnapRange = localSnapRange,
                    getOtherWidgets = getOtherWidgets,
                    snapThresholdValue = snapThresholdValue,
                    drawLine = drawLine,
                    onLineCancel = onLineCancel,
                    onTapInEditMode = onTapInEditMode
                ),
            contentAlignment = Alignment.Center
        ) {
            val color by buttonContentColorAsState(
                style = style,
                isDark = isDark,
                isPressed = isPressed
            )
            val fontSize by buttonFontSizeAsState(
                style = style,
                isDark = isDark,
                isPressed = isPressed
            )
            val buttonTextStyle = when (data) {
                is ObservableNormalData -> ButtonTextStyle(
                    text = data.text,
                    textAlignment = data.textAlignment,
                    textBold = data.textBold,
                    textItalic = data.textItalic,
                    textUnderline = data.textUnderline
                )
                is ObservableTextData -> ButtonTextStyle(
                    text = data.text,
                    textAlignment = data.textAlignment,
                    textBold = data.textBold,
                    textItalic = data.textItalic,
                    textUnderline = data.textUnderline
                )
                else -> error("Unknown widget type")
            }

            // Icon resolved above for double-press prevention

            if (resolvedIcon != null) {
                Image(
                    painter = painterResource(resolvedIcon),
                    contentDescription = buttonTextStyle.text.translate(locale),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                val scale = LocalButtonScale.current
                val scaledFontSize = (fontSize * scale).sp
                RtLText(
                    text = buttonTextStyle.text.translate(locale),
                    color = color,
                    fontSize = scaledFontSize,
                    textAlign = buttonTextStyle.textAlignment.textAlign,
                    fontWeight = if (buttonTextStyle.textBold) FontWeight.Bold else null,
                    fontStyle = if (buttonTextStyle.textItalic) FontStyle.Italic else null,
                    textDecoration = if (buttonTextStyle.textUnderline) TextDecoration.Underline else null,
                    style = LocalTextStyle.current.copy(
                        lineHeight = (fontSize * scale * 1.1).sp
                    )
                )
            }

            DisposableEffect(Unit) {
                data.onCompositionStart(eventHandler)
                onDispose {
                    data.onCompositionDispose(eventHandler)
                }
            }
        }
    } else {
        //虚假的控件，使用一个空的组件，只是让Layout有东西能测
        Spacer(
            modifier = Modifier.buttonSize(data, screenSize)
        )
    }
}

/**
 * 仅渲染控件外观的组件
 */
@Composable
fun RendererStyleBox(
    style: ObservableButtonStyle,
    modifier: Modifier = Modifier,
    text: String = "",
    isDark: Boolean,
    isPressed: Boolean
) {
    Box(
        modifier = modifier.buttonStyle(
            style = style,
            isDark = isDark,
            isPressed = isPressed
        ),
        contentAlignment = Alignment.Center
    ) {
        val color by buttonContentColorAsState(style = style, isDark = isDark, isPressed = isPressed)
        val fontSize by buttonFontSizeAsState(style = style, isDark = isDark, isPressed = isPressed)
        RtLText(
            text = text,
            color = color,
            fontSize = fontSize.sp
        )
    }
}