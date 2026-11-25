package com.movtery.zalithlauncher.ui.screens.game.elements

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper
import com.movtery.zalithlauncher.utils.capture.CubiqScreenRecorder
import kotlinx.coroutines.delay
import org.lwjgl.glfw.CallbackBridge
import kotlin.math.roundToInt

@Composable
fun InGameCaptureHud(
    modifier: Modifier = Modifier,
    alpha: Float = 0.6f
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentCtrl = AllSettings.controlLayout.getValue()
    val isPocket = currentCtrl.contains("pocket", ignoreCase = true)
    val isZl1 = currentCtrl.contains("zl1", ignoreCase = true) ||
            currentCtrl.contains("zalith 1", ignoreCase = true) ||
            currentCtrl.contains("zalith1", ignoreCase = true)

    val posSetting = when {
        isPocket -> AllSettings.hudCapturePosPocket
        isZl1 -> AllSettings.hudCapturePosZl1
        else -> AllSettings.hudCapturePosZl2
    }

    val defaultOffset = when {
        isPocket -> Offset(80f, 14f)
        isZl1 -> Offset(340f, 14f)
        else -> Offset(140f, 14f)
    }

    val initialPos = remember(posSetting.state, defaultOffset) {
        if (posSetting.state != Offset.Zero) posSetting.state else defaultOffset
    }

    var offsetX by remember(posSetting) { mutableFloatStateOf(initialPos.x) }
    var offsetY by remember(posSetting) { mutableFloatStateOf(initialPos.y) }
    var isExpanded by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(CubiqScreenRecorder.isRecording) }
    var recordSeconds by remember { mutableIntStateOf(0) }

    val registryOwner = LocalActivityResultRegistryOwner.current
    val projectionLauncher = if (registryOwner != null) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val fpsVal = AllSettings.recordFps.state
                val bitVal = AllSettings.recordBitrate.state
                val started = CubiqScreenRecorder.startRecording(
                    context = context,
                    resultCode = result.resultCode,
                    data = result.data!!,
                    fps = fpsVal,
                    bitrate = bitVal
                )
                if (started) {
                    isRecording = true
                    Toast.makeText(context, R.string.cubiq_game_record_started, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.cubiq_game_record_failed, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, R.string.cubiq_game_record_permission_cancelled, Toast.LENGTH_SHORT).show()
            }
        }
    } else null

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
            .pointerInput(posSetting) {
                detectDragGestures(
                    onDragEnd = {
                        posSetting.updateState(Offset(offsetX, offsetY))
                        posSetting.save()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceAtLeast(0f)
                    offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
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
                    // Screenshot Button (F2 Keycode + Real PixelCopy -> Gallery)
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
                                if (activity != null) {
                                    CubiqScreenRecorder.takeScreenshot(activity) { file ->
                                        if (file != null) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.cubiq_game_screenshot_saved, "Pictures/Cubiq"),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                R.string.cubiq_game_screenshot_captured,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.cubiq_game_screenshot_captured,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📸", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.cubiq_game_screenshot),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Screen Recorder Button (Real MediaProjection -> MediaRecorder -> MP4)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRecording) Color(0x888B0000) else Color(0x552A2A34))
                            .border(0.5.dp, if (isRecording) Color(0xAAFF3333) else Color(0x28FFFFFF), RoundedCornerShape(10.dp))
                            .clickable {
                                if (AllSettings.hudCaptureSound.state) {
                                    MinecraftSoundHelper.playClickSound()
                                }
                                if (isRecording) {
                                    val savedFile = CubiqScreenRecorder.stopRecording(context)
                                    isRecording = false
                                    if (savedFile != null) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.cubiq_game_record_saved, "Movies/Cubiq", savedFile.name),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            R.string.cubiq_game_record_stopped,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                    if (projectionLauncher != null && projectionManager != null) {
                                        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
                                    } else {
                                        Toast.makeText(context, R.string.cubiq_game_record_unsupported, Toast.LENGTH_SHORT).show()
                                    }
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
                                text = if (AllSettings.hudShowRecordTimer.state) stringResource(R.string.cubiq_game_record_timer, m, s) else stringResource(R.string.cubiq_game_record_active),
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(text = "🎥", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.cubiq_game_record),
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
