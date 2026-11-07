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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var offsetX by remember { mutableStateOf(20f) }
    var offsetY by remember { mutableStateOf(80f) }
    var isExpanded by remember { mutableStateOf(true) }
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
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xDD18181C))
                .border(1.dp, Color(0xFF388E23), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Collapse / Expand Pill Icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A30))
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A30))
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
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📸", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Screenshot",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Screen Recorder Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRecording) Color(0xFF4A1010) else Color(0xFF2A2A30))
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
                            .padding(horizontal = 10.dp, vertical = 4.dp),
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "🎥", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Record",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
