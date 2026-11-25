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

package com.movtery.zalithlauncher.ui.vulkan_checker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.device.McVersionSpan
import com.movtery.zalithlauncher.utils.device.VulkanDependency
import com.movtery.zalithlauncher.utils.device.VulkanRequirements
import com.movtery.zalithlauncher.utils.device.profileSupport
import com.movtery.zalithlauncher.utils.device.supports

@Composable
fun VulkanChecker(
    operation: VCOperation,
    onChange: (VCOperation) -> Unit,
    startCheck: (Version) -> Unit,
    confirmResult: () -> Unit,
) {
    when (operation) {
        is VCOperation.None -> {}
        is VCOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.game_vulkan_check_title),
                text = stringResource(R.string.game_vulkan_check_text),
                dismissByDialog = false,
                onDismiss = {
                    startCheck(operation.version)
                }
            )
        }
        is VCOperation.Result -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false,
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 480.dp)
                        .heightIn(max = rememberDialogMaxHeight())
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(all = 6.dp)
                            .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF1E1E20),
                        contentColor = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFF383838)),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(all = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cubiq_game_vulkan_compatibility_title),
                                fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )

                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .fillMaxWidth()
                                    .fadeEdge(scrollState)
                                    .verticalScrollWithBar(scrollState),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val data = operation.data
                                if (data == null) {
                                    Text(
                                        text = stringResource(R.string.game_vulkan_check_failed),
                                        fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                        color = Color(0xFFFF5555),
                                        fontSize = 12.sp
                                    )
                                } else {
                                    val profiles = data.profileSupport()

                                    val summary = when {
                                        profiles.all { it.supported } ->
                                            stringResource(R.string.game_vulkan_check_supp, profiles.first().since)
                                        profiles.none { it.supported } ->
                                            stringResource(R.string.game_vulkan_check_unsupp)
                                        else ->
                                            stringResource(R.string.game_vulkan_check_partial)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF28282A))
                                            .border(BorderStroke(1.dp, Color(0xFF3A3A3C)), RoundedCornerShape(2.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = summary,
                                            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                            fontSize = 12.sp,
                                            color = if (profiles.any { it.supported }) Color(0xFF55FF55) else Color(0xFFFF9999),
                                            lineHeight = 16.sp
                                        )
                                    }

                                    // Vulkan Version & Driver
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(R.string.game_vulkan_check_version, data.versionString),
                                            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFAAAAAA)
                                        )
                                        Text(
                                            text = stringResource(
                                                R.string.game_vulkan_check_turnip,
                                                stringResource(if (operation.useTurnip) R.string.generic_enabled else R.string.generic_disabled)
                                            ),
                                            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFAAAAAA)
                                        )
                                    }

                                    // Informative note about OpenGL / Krypton Wrapper
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF1E281E))
                                            .border(BorderStroke(1.dp, Color(0xFF2E4C22)), RoundedCornerShape(2.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cubiq_game_vulkan_compatibility_note),
                                            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFAADD99),
                                            lineHeight = 15.sp
                                        )
                                    }

                                    // Extensions & features
                                    TextGroup(text = stringResource(R.string.game_vulkan_check_extensions)) {
                                        VulkanRequirements.EXTENSIONS.forEach {
                                            DependencyText(
                                                dependency = it,
                                                supported = data.supports(it)
                                            )
                                        }
                                    }
                                    TextGroup(text = stringResource(R.string.game_vulkan_check_features)) {
                                        VulkanRequirements.FEATURES.forEach {
                                            DependencyText(
                                                dependency = it,
                                                supported = data.supports(it)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                com.movtery.zalithlauncher.ui.components.MinecraftButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp),
                                    onClick = {
                                        confirmResult()
                                        onChange(VCOperation.None)
                                    },
                                    style = com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle.GREEN,
                                    text = stringResource(R.string.cubiq_game_continue),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextGroup(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            content = content
        )
    }
}

@Composable
private fun DependencyText(
    dependency: VulkanDependency,
    supported: Boolean,
    unsupportedColor: Color = MaterialTheme.colorScheme.error,
) {
    Column {
        Row {
            val color = if (supported) LocalContentColor.current else unsupportedColor
            Text(
                text = dependency.name,
                color = color
            )
            if (!supported) {
                Text(
                    text = stringResource(R.string.game_vulkan_check_item_missing),
                    color = color
                )
            }
        }

        @Composable
        fun VersionSupp(
            text: String,
            spans: List<McVersionSpan>,
            modifier: Modifier = Modifier
        ) {
            Row(modifier) {
                Text(text = text)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    spans.forEach { span ->
                        Text(text = span.displayText)
                    }
                }
            }
        }

        if (dependency.requiredIn.isNotEmpty()) {
            VersionSupp(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.game_vulkan_check_dep_required),
                spans = dependency.requiredIn
            )
        }
        if (dependency.optionalIn.isNotEmpty()) {
            VersionSupp(
                modifier = Modifier.padding(start = 12.dp),
                text = stringResource(R.string.game_vulkan_check_dep_optional),
                spans = dependency.optionalIn
            )
        }
    }
}