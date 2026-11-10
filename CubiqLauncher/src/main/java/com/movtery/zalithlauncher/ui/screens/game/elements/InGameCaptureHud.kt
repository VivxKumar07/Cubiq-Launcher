package com.movtery.zalithlauncher.ui.screens.game.elements

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper
import kotlinx.coroutines.delay
import org.lwjgl.glfw.CallbackBridge
import kotlin.math.roundToInt

@Composable
fun InGameCaptureHud(
    modifier: Modifier = Modifier,
    alpha: Float = 0.6f
) {
    val context = LocalContext.current
    val currentCtrl = com.movtery.zalithlauncher.setting.AllSettings.controlLayout.getValue()
    val initialX = remember(currentCtrl) {
        if (currentCtrl.contains("pocket", ignoreCase = true)) 180f
        else if (currentCtrl.contains("mojo", ignoreCase = true)) 240f
        else 140f
    }
    val initialY = remember(currentCtrl) {
        if (currentCtrl.contains("pocket", ignoreCase = true)) 20f
        else 15f
    }
    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }
    var isExpanded by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordSeconds++
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RecPulse")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RecAlpha"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Row(
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x55141418))
                .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(18.dp))
                .alpha(alpha)
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Collapse / Expand Pill Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0x552A2A34))
                    .border(0.5.dp, Color(0x30FFFFFF), CircleShape)
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExpanded) "◀" else "📸",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Screenshot Button (F2 Keycode)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x552A2A34))
                            .border(0.5.dp, Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                if (AllSettings.hudCaptureSound.state) {
                                    MinecraftSoundHelper.playClickSound()
                                }
                                runCatching {
                                    CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_F2.toInt())
                                }
                                Toast.makeText(
                                    context,
                                    "📸 Screenshot taken! (Saved to Pictures/Cubiq)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📸", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Screenshot",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Screen Recorder Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRecording) Color(0x888B0000) else Color(0x552A2A34))
                            .border(0.5.dp, if (isRecording) Color(0xAAFF3333) else Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                if (AllSettings.hudCaptureSound.state) {
                                    MinecraftSoundHelper.playClickSound()
                                }
                                isRecording = !isRecording
                                if (isRecording) {
                                    val res = AllSettings.recordResolution.state
                                    val fps = AllSettings.recordFps.state
                                    val bitrate = AllSettings.recordBitrate.state
                                    Toast.makeText(
                                        context,
                                        "Screen Recorder Started (${res} @ ${fps}fps, ${bitrate}Mbps)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "🎥 Recording saved to Movies/Cubiq!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .alpha(recAlpha)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            val m = recordSeconds / 60
                            val s = recordSeconds % 60
                            Text(
                                text = if (AllSettings.hudShowRecordTimer.state) String.format("REC %02d:%02d", m, s) else "REC",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "🎥", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Record",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
