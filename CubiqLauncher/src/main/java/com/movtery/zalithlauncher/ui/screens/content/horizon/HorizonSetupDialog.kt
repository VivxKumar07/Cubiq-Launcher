package com.movtery.zalithlauncher.ui.screens.content.horizon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.horizon.HorizonManager
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

@Composable
fun HorizonSetupDialog(
    onDismiss: () -> Unit,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val installState by HorizonManager.installState.collectAsState()
    val isAlreadyInstalled = remember { HorizonManager.isInstalled(context) }
    var forceReinstall by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        if (installState !is HorizonManager.InstallState.Downloading && installState !is HorizonManager.InstallState.Configuring) {
            onDismiss()
        }
    }) {
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF14151B))
                .border(BorderStroke(1.5.dp, Color(0xFF2E323D)), RoundedCornerShape(8.dp))
                .padding(18.dp)
        ) {
            if ((isAlreadyInstalled && !forceReinstall && installState is HorizonManager.InstallState.Idle) ||
                installState is HorizonManager.InstallState.Success
            ) {
                // Setup Complete / Direct Play Option
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A20))
                            .border(BorderStroke(2.dp, Color(0xFF22C55E)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Complete",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Setup Complete",
                            color = Color.White,
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Minecraft Rust (26.3) is ready to play.",
                            color = Color(0xFF9CA3AF),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MinecraftButton(
                            onClick = {
                                HorizonManager.resetState()
                                onSetupComplete()
                                onDismiss()
                                HorizonManager.launchHorizon(context)
                            },
                            text = "Play Now",
                            style = MinecraftButtonStyle.GREEN,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        )

                        MinecraftButton(
                            onClick = {
                                HorizonManager.resetState()
                                onSetupComplete()
                                onDismiss()
                            },
                            text = "Done",
                            style = MinecraftButtonStyle.STONE,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        )
                    }

                    Text(
                        text = "Re-check files",
                        color = Color(0xFF71717A),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable {
                                forceReinstall = true
                                HorizonManager.setupHorizon(context)
                            }
                            .padding(top = 4.dp)
                    )
                }
            } else {
                when (val state = installState) {
                    is HorizonManager.InstallState.Idle -> {
                        // Initial Setup Introduction
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header with Close
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_horizon_logo),
                                        contentDescription = "Horizon",
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text = "Horizon",
                                            color = Color.White,
                                            fontFamily = MinecraftFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Set up Minecraft Rust Edition",
                                            color = Color(0xFF9CA3AF),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1F222B))
                                        .clickable { onDismiss() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = "Close",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Sunset artwork banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.art_horizon_sunset),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = "Cubiq will set up Minecraft Rust (Horizon) on your device. This will download the required official files (~500 MB).",
                                color = Color(0xFFD1D5DB),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MinecraftButton(
                                    onClick = onDismiss,
                                    text = "Cancel",
                                    style = MinecraftButtonStyle.STONE,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                )

                                MinecraftButton(
                                    onClick = {
                                        HorizonManager.setupHorizon(context)
                                    },
                                    text = "Set Up",
                                    style = MinecraftButtonStyle.GREEN,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                )
                            }
                        }
                    }

                    is HorizonManager.InstallState.Preparing,
                    is HorizonManager.InstallState.Downloading,
                    is HorizonManager.InstallState.Configuring,
                    is HorizonManager.InstallState.Finalizing -> {
                        // In Progress Screen with 4 Steps
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_horizon_logo),
                                        contentDescription = "Horizon",
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text = "Horizon",
                                            color = Color.White,
                                            fontFamily = MinecraftFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Setting up Minecraft Rust...",
                                            color = Color(0xFF9CA3AF),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1F222B))
                                        .clickable {
                                            HorizonManager.cancelSetup()
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = "Close",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Steps Progress List
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                val step1Done = state !is HorizonManager.InstallState.Preparing
                                ProgressStepRow(
                                    label = "Preparing environment",
                                    isDone = step1Done,
                                    isActive = state is HorizonManager.InstallState.Preparing
                                )

                                val step2Active = state is HorizonManager.InstallState.Downloading
                                val step2Done = state is HorizonManager.InstallState.Configuring || state is HorizonManager.InstallState.Finalizing
                                val dlBytes = (state as? HorizonManager.InstallState.Downloading)?.let {
                                    val mbCurrent = (it.currentBytes / (1024 * 1024)).toInt()
                                    val mbTotal = (it.totalBytes / (1024 * 1024)).toInt().coerceAtLeast(1)
                                    " ($mbCurrent MB / $mbTotal MB)"
                                } ?: ""

                                ProgressStepRow(
                                    label = "Downloading files$dlBytes",
                                    isDone = step2Done,
                                    isActive = step2Active,
                                    percentText = (state as? HorizonManager.InstallState.Downloading)?.let {
                                        "${(it.percent * 100).toInt()}%"
                                    }
                                )

                                if (step2Active) {
                                    val pct = (state as? HorizonManager.InstallState.Downloading)?.percent ?: 0f
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 28.dp, end = 4.dp)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF22C55E),
                                        trackColor = Color(0xFF2E323D)
                                    )
                                }

                                val step3Active = state is HorizonManager.InstallState.Configuring
                                val step3Done = state is HorizonManager.InstallState.Finalizing
                                ProgressStepRow(
                                    label = "Configuring runtime",
                                    isDone = step3Done,
                                    isActive = step3Active
                                )

                                val step4Active = state is HorizonManager.InstallState.Finalizing
                                ProgressStepRow(
                                    label = "Finalizing setup",
                                    isDone = false,
                                    isActive = step4Active
                                )
                            }
                        }
                    }

                    is HorizonManager.InstallState.Failed -> {
                        // Error Screen
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Setup Failed",
                                color = Color(0xFFEF4444),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = state.error,
                                color = Color(0xFFD1D5DB),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MinecraftButton(
                                    onClick = onDismiss,
                                    text = "Close",
                                    style = MinecraftButtonStyle.STONE,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                )

                                MinecraftButton(
                                    onClick = {
                                        HorizonManager.setupHorizon(context)
                                    },
                                    text = "Retry",
                                    style = MinecraftButtonStyle.RED,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun ProgressStepRow(
    label: String,
    isDone: Boolean,
    isActive: Boolean,
    percentText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> Color(0xFF22C55E)
                            isActive -> Color(0xFF1E3A20)
                            else -> Color(0xFF272932)
                        }
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            when {
                                isDone -> Color(0xFF22C55E)
                                isActive -> Color(0xFF4ADE80)
                                else -> Color(0xFF3F424E)
                            }
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Text(
                text = label,
                color = when {
                    isDone -> Color(0xFFE5E7EB)
                    isActive -> Color.White
                    else -> Color(0xFF6B7280)
                },
                fontFamily = MinecraftFontFamily,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }

        if (percentText != null) {
            Text(
                text = percentText,
                color = Color(0xFF9CA3AF),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.sp
            )
        }
    }
}
