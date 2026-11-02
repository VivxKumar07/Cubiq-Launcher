package com.movtery.zalithlauncher.ui.screens.content.horizon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
    val scope = rememberCoroutineScope()
    val installState by HorizonManager.installState.collectAsState()

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
            when (installState) {
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

                        // Artwork Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, Color(0xFF2E323D)), RoundedCornerShape(6.dp))
                        ) {
                            Image(
                                painter = painterResource(R.drawable.art_horizon_sunset),
                                contentDescription = "Horizon Sunset",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        // Description
                        Text(
                            text = "Cubiq will set up Minecraft Rust (Horizon) on your device. This will download the required official resources (~ 500 MB) directly from Mojang.",
                            color = Color(0xFFD1D5DB),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MinecraftButton(
                                onClick = onDismiss,
                                text = "Cancel",
                                style = MinecraftButtonStyle.STONE,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )

                            MinecraftButton(
                                onClick = {
                                    HorizonManager.startSetup(context, scope)
                                },
                                text = "Set Up",
                                style = MinecraftButtonStyle.GREEN,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                        }
                    }
                }

                is HorizonManager.InstallState.Preparing,
                is HorizonManager.InstallState.Downloading,
                is HorizonManager.InstallState.Configuring,
                is HorizonManager.InstallState.Finalizing -> {
                    // Progress Flow
                    val state = installState
                    val downloadPercent = if (state is HorizonManager.InstallState.Downloading) state.percent else 0f
                    val currentDoneMB = if (state is HorizonManager.InstallState.Downloading) state.currentBytes / 1048576 else 0
                    val totalMB = if (state is HorizonManager.InstallState.Downloading) state.totalBytes / 1048576 else 500

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            // Step 1: Preparing environment
                            val step1Done = state !is HorizonManager.InstallState.Preparing
                            ProgressStepRow(
                                stepTitle = "Preparing environment",
                                isDone = step1Done,
                                isActive = state is HorizonManager.InstallState.Preparing
                            )

                            // Step 2: Downloading files
                            val step2Active = state is HorizonManager.InstallState.Downloading
                            val step2Done = state is HorizonManager.InstallState.Configuring || state is HorizonManager.InstallState.Finalizing
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProgressStepRow(
                                        stepTitle = "Downloading files (${currentDoneMB} MB / ${totalMB} MB)",
                                        isDone = step2Done,
                                        isActive = step2Active
                                    )
                                    if (step2Active) {
                                        Text(
                                            text = "${(downloadPercent * 100).toInt()}%",
                                            color = Color(0xFF22C55E),
                                            fontFamily = MinecraftFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (step2Active) {
                                    LinearProgressIndicator(
                                        progress = { downloadPercent },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF22C55E),
                                        trackColor = Color(0xFF232630)
                                    )
                                }
                            }

                            // Step 3: Configuring runtime
                            val step3Active = state is HorizonManager.InstallState.Configuring
                            val step3Done = state is HorizonManager.InstallState.Finalizing
                            ProgressStepRow(
                                stepTitle = "Configuring runtime",
                                isDone = step3Done,
                                isActive = step3Active
                            )

                            // Step 4: Finalizing setup
                            ProgressStepRow(
                                stepTitle = "Finalizing setup",
                                isDone = false,
                                isActive = state is HorizonManager.InstallState.Finalizing
                            )
                        }
                    }
                }

                is HorizonManager.InstallState.Success -> {
                    // Setup Complete
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
                                modifier = Modifier.weight(1f).height(40.dp)
                            )

                            MinecraftButton(
                                onClick = {
                                    HorizonManager.resetState()
                                    onSetupComplete()
                                    onDismiss()
                                },
                                text = "Done",
                                style = MinecraftButtonStyle.STONE,
                                modifier = Modifier.weight(1f).height(40.dp)
                            )
                        }
                    }
                }

                is HorizonManager.InstallState.Failed -> {
                    // Failed
                    val error = (installState as HorizonManager.InstallState.Failed).error
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Setup Interrupted",
                            color = Color(0xFFEF4444),
                            fontFamily = MinecraftFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = error,
                            color = Color(0xFFD1D5DB),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MinecraftButton(
                                onClick = {
                                    HorizonManager.resetState()
                                    onDismiss()
                                },
                                text = "Close",
                                style = MinecraftButtonStyle.STONE,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )

                            MinecraftButton(
                                onClick = {
                                    HorizonManager.startSetup(context, scope)
                                },
                                text = "Retry",
                                style = MinecraftButtonStyle.GREEN,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressStepRow(
    stepTitle: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> Color(0xFF22C55E)
                        isActive -> Color(0xFF1E293B)
                        else -> Color(0xFF181A20)
                    }
                )
                .border(
                    BorderStroke(
                        1.2.dp,
                        when {
                            isDone -> Color(0xFF22C55E)
                            isActive -> Color(0xFF22C55E)
                            else -> Color(0xFF374151)
                        }
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDone -> Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Done",
                    tint = Color.Black,
                    modifier = Modifier.size(13.dp)
                )
                isActive -> CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF22C55E)
                )
                else -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4B5563))
                )
            }
        }

        Text(
            text = stepTitle,
            color = if (isDone || isActive) Color.White else Color(0xFF6B7280),
            fontFamily = MinecraftFontFamily,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}
