package com.movtery.zalithlauncher.ui.screens.game.elements

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@Composable
fun LegacyInGameDrawer(
    visible: Boolean,
    isMojo: Boolean,
    onDismiss: () -> Unit,
    onStartEditControls: () -> Unit,
    onSendKeycode: () -> Unit,
    onViewLogs: () -> Unit,
    onForceClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .background(Color(0xF8141416))
                .border(1.dp, Color(0xFF383838))
                .padding(14.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {} // Prevent click-through
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isMojo) "MOJO LAUNCHER MENU" else "ZALITH 1 MENU",
                        color = Color(0xFF68B7FF),
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = "Native In-Game Quick Settings",
                        color = Color(0xFFAAAAAA),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.5.sp
                    )
                }
                MinecraftButton(
                    onClick = onDismiss,
                    style = MinecraftButtonStyle.STONE,
                    text = "✕",
                    fontSize = 11.sp,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Controls Editor Button
            Text(
                text = "ON-SCREEN CONTROLS",
                color = Color(0xFF888888),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            MinecraftButton(
                onClick = onStartEditControls,
                style = MinecraftButtonStyle.GREEN,
                text = "✏️ CUSTOM CONTROLS (EDIT)",
                fontSize = 10.5.sp,
                modifier = Modifier.fillMaxWidth().height(36.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Resolution Scale Slider
            var resScale by remember { mutableFloatStateOf(AllSettings.resolutionRatio.state.toFloat()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RESOLUTION SCALER",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 10.5.sp
                )
                Text(
                    text = "${resScale.toInt()}%",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            }
            Slider(
                value = resScale,
                onValueChange = {
                    resScale = it
                    AllSettings.resolutionRatio.updateState(it.toInt())
                },
                onValueChangeFinished = {
                    AllSettings.resolutionRatio.save()
                },
                valueRange = 25f..120f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF55FF55),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Button Opacity Slider
            var opacity by remember { mutableFloatStateOf(AllSettings.controlsOpacity.state.toFloat()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BUTTON OPACITY",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 10.5.sp
                )
                Text(
                    text = "${opacity.toInt()}%",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            }
            Slider(
                value = opacity,
                onValueChange = {
                    opacity = it
                    AllSettings.controlsOpacity.updateState(it.toInt())
                },
                onValueChangeFinished = {
                    AllSettings.controlsOpacity.save()
                },
                valueRange = 10f..100f,
                steps = 17,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF55FF55),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mouse Speed Slider
            var mouseSpeed by remember { mutableFloatStateOf(AllSettings.mouseCaptureSensitivity.state.toFloat()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MOUSE SENSITIVITY",
                    color = Color.White,
                    fontFamily = MinecraftFontFamily,
                    fontSize = 10.5.sp
                )
                Text(
                    text = "${mouseSpeed.toInt()}%",
                    color = Color(0xFF55FF55),
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                )
            }
            Slider(
                value = mouseSpeed,
                onValueChange = {
                    mouseSpeed = it
                    AllSettings.mouseCaptureSensitivity.updateState(it.toInt())
                },
                onValueChangeFinished = {
                    AllSettings.mouseCaptureSensitivity.save()
                },
                valueRange = 25f..300f,
                steps = 54,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF55FF55),
                    activeTrackColor = Color(0xFF55FF55),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // In-Game Operations
            Text(
                text = "OPERATIONS",
                color = Color(0xFF888888),
                fontFamily = MinecraftFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            MinecraftButton(
                onClick = onSendKeycode,
                style = MinecraftButtonStyle.DARK,
                text = "⌨️ SEND CUSTOM KEY",
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().height(34.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            MinecraftButton(
                onClick = onViewLogs,
                style = MinecraftButtonStyle.DARK,
                text = "📜 VIEW LOG OUTPUT",
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().height(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MinecraftButton(
                onClick = onForceClose,
                style = MinecraftButtonStyle.RED,
                text = "⚠️ FORCE CLOSE GAME",
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth().height(34.dp)
            )
        }
    }
}

