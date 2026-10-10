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

package com.movtery.zalithlauncher.ui.screens.content.versions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.contract.MediaPickerContract
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.DeleteVersionDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportFileButton
import com.movtery.zalithlauncher.ui.screens.content.elements.RenameVersionDialog
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardDir
import com.movtery.zalithlauncher.ui.screens.content.home.version.VersionCardManager
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionChunkBackground
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionOverviewItem
import com.movtery.zalithlauncher.utils.file.ensureDirectory
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "VersionOverView"

@Composable
fun VersionOverViewScreen(
    mainScreenKey: TitledNavKey?,
    versionsScreenKey: TitledNavKey?,
    backToMainScreen: () -> Unit,
    onExport: () -> Unit,
    eventViewModel: EventViewModel,
    version: Version,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.OverView, versionsScreenKey, false)
    ) { isVisible ->
        var versionSummary by remember { mutableStateOf(version.getVersionSummary()) }
        var refreshVersionIcon by remember { mutableIntStateOf(0) }

        var iconFileExists by remember { mutableStateOf(version.getVersionIconFile().exists()) }

        var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
        VersionsOperation(
            versionsOperation = versionsOperation,
            updateOperation = { versionsOperation = it },
            submitError = submitError,
            resetIcon = {
                val iconFile = version.getVersionIconFile()
                FileUtils.deleteQuietly(iconFile)
                refreshVersionIcon++
                iconFileExists = iconFile.exists()
            },
            setVersionSummary = { value ->
                version.getVersionConfig().apply {
                    this.versionSummary = value
                    save()
                }
                versionSummary = version.getVersionSummary()
            }
        )

        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                VersionInfoLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    version = version,
                    versionSummary = versionSummary,
                    iconFileExists = iconFileExists,
                    submitError = submitError,
                    refreshKey = refreshVersionIcon,
                    onIconPicked = {
                        iconFileExists = version.getVersionIconFile().exists()
                        versionsOperation = VersionsOperation.None
                        refreshVersionIcon++
                    },
                    resetIcon = { versionsOperation = VersionsOperation.ResetIconAlert }
                )
            }

            AnimatedItem(scope) { yOffset ->
                VersionManagementLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    version = version,
                    onShareLog = { logFile ->
                        eventViewModel.sendEvent(
                            EventViewModel.Event.LogShare.ShareGameLog(logFile)
                        )
                    },
                    onEditSummary = { versionsOperation = VersionsOperation.EditSummary(version) },
                    onRename = { versionsOperation = VersionsOperation.Rename(version) },
                    onExport = onExport,
                    onDelete = { versionsOperation = VersionsOperation.Delete(version) }
                )
            }

            AnimatedItem(scope) { yOffset ->
                VersionQuickActions(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    accessFolder = { path ->
                        val folder = if (path.isEmpty()) {
                            version.getGameDir()
                        } else {
                            File(version.getGameDir(), path)
                        }
                        runCatching {
                            folder.ensureDirectory()
                        }.onFailure { e ->
                            submitError(
                                ErrorViewModel.ThrowableMessage(
                                    title = androidText(R.string.error_create_dir, folder.absolutePath),
                                    message = androidText(e.getMessageOrToString())
                                )
                            )
                            return@VersionQuickActions
                        }
                        eventViewModel.sendEvent(
                            EventViewModel.Event.OpenFileManager(
                                rootPath = version.getGameHome(),
                                currentPath = folder.absolutePath,
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun VersionInfoLayout(
    modifier: Modifier = Modifier,
    version: Version,
    versionSummary: String,
    iconFileExists: Boolean,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    refreshKey: Any? = null,
    onIconPicked: () -> Unit = {},
    resetIcon: () -> Unit = {}
) {
    VersionChunkBackground(
        modifier = modifier,
        paddingValues = PaddingValues(all = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionOverviewItem(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .fillMaxWidth(),
                version = version,
                versionSummary = versionSummary,
                refreshKey = refreshKey
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VersionManagementLayout(
    modifier: Modifier = Modifier,
    version: Version,
    onShareLog: (File) -> Unit,
    onEditSummary: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    val logFile = remember(version) {
        version.getLatestLog()
    }
    val logExists = remember(logFile) {
        logFile.exists()
    }

    VersionChunkBackground(
        modifier = modifier,
        paddingValues = PaddingValues(all = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                text = stringResource(R.string.versions_settings_overview_management),
                style = MaterialTheme.typography.labelLarge
            )

            FlowRow {
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_edit_version_summary),
                    onClick = onEditSummary
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_manage_rename_version),
                    onClick = onRename
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_export),
                    onClick = onExport
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_log),
                    enabled = logExists,
                    onClick = { onShareLog(logFile) }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_manage_delete_version),
                    onClick = onDelete,
                    isDestructive = true
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VersionQuickActions(
    modifier: Modifier = Modifier,
    accessFolder: (folderName: String) -> Unit = {}
) {
    VersionChunkBackground(
        modifier = modifier,
        paddingValues = PaddingValues(all = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                text = "GAME FILES & DIRECTORIES",
                style = MaterialTheme.typography.labelLarge
            )

            // Direct Primary Action: OPEN GAME FILES (.minecraft)
            com.movtery.zalithlauncher.ui.components.MinecraftButton(
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    accessFolder("")
                },
                style = com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle.GREEN,
                text = "📁 OPEN GAME FILES (.MINECRAFT)",
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(bottom = 8.dp)
            )

            FlowRow {
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_saves_folder),
                    onClick = { accessFolder(VersionFolders.SAVES.folderName) }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_resource_pack_folder),
                    onClick = { accessFolder(VersionFolders.RESOURCE_PACK.folderName) }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_shaders_pack_folder),
                    onClick = { accessFolder(VersionFolders.SHADERS.folderName) }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_mod_folder),
                    onClick = { accessFolder(VersionFolders.MOD.folderName) }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_screenshot_folder),
                    onClick = { accessFolder("screenshots") }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_logs_folder),
                    onClick = { accessFolder("logs") }
                )
                CubiqActionButton(
                    text = stringResource(R.string.versions_overview_crash_report_folder),
                    onClick = { accessFolder("crash-reports") }
                )
            }
        }
    }
}

@Composable
private fun CubiqActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    enabled: Boolean = true
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
            .background(
                if (!enabled) androidx.compose.ui.graphics.Color(0xFF1C1D1F)
                else if (isDestructive) androidx.compose.ui.graphics.Color(0xFF2A1B1D)
                else androidx.compose.ui.graphics.Color(0xFF24262A)
            )
            .border(
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (!enabled) androidx.compose.ui.graphics.Color(0xFF2C2C2E)
                    else if (isDestructive) androidx.compose.ui.graphics.Color(0xFF882222)
                    else androidx.compose.ui.graphics.Color(0xFF383838)
                ),
                androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
            )
            .clickable(enabled = enabled) {
                com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (!enabled) androidx.compose.ui.graphics.Color(0xFF666666)
            else if (isDestructive) androidx.compose.ui.graphics.Color(0xFFFF6666)
            else androidx.compose.ui.graphics.Color.White,
            fontFamily = com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 11.5.sp
        )
    }
}

/**
 * 版本概览操作
 */
sealed interface VersionsOperation {
    data object None: VersionsOperation
    data object ResetIconAlert: VersionsOperation
    data class EditSummary(val version: Version): VersionsOperation
    data class Rename(val version: Version): VersionsOperation
    data class Delete(val version: Version): VersionsOperation
    data class RunTask(val title: Int, val task: suspend () -> Unit): VersionsOperation
}

@Composable
private fun VersionsOperation(
    versionsOperation: VersionsOperation,
    updateOperation: (VersionsOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    resetIcon: () -> Unit = {},
    setVersionSummary: (String) -> Unit = {}
) {
    when(versionsOperation) {
        is VersionsOperation.None -> {}
        is VersionsOperation.ResetIconAlert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.versions_overview_reset_version_icon_message),
                onDismiss = { updateOperation(VersionsOperation.None) },
                onConfirm = {
                    resetIcon()
                    updateOperation(VersionsOperation.None)
                }
            )
        }
        is VersionsOperation.Rename -> {
            RenameVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateOperation(VersionsOperation.None) },
                onConfirm = {
                    updateOperation(
                        VersionsOperation.RunTask(
                            title = R.string.versions_manage_rename_version,
                            task = {
                                VersionsManager.renameVersion(versionsOperation.version, it)
                                VersionCardManager.onVersionRenamed(
                                    gameHome = versionsOperation.version.getGameHome(),
                                    oldName = versionsOperation.version.getVersionName(),
                                    newName = it
                                )
                            }
                        )
                    )
                }
            )
        }
        is VersionsOperation.Delete -> {
            val version = versionsOperation.version

            DeleteVersionDialog(
                version = version,
                onDismissRequest = { updateOperation(VersionsOperation.None) },
                onConfirm = { title, task ->
                    updateOperation(
                        VersionsOperation.RunTask(
                            title = title,
                            task = task
                        )
                    )
                }
            )
        }
        is VersionsOperation.EditSummary -> {
            val version = versionsOperation.version
            var value by remember { mutableStateOf(version.getVersionConfig().versionSummary) }

            SimpleEditDialog(
                title = stringResource(R.string.versions_overview_edit_version_summary),
                value = value,
                onValueChange = { value = it },
                label = {
                    Text(text = stringResource(R.string.versions_overview_edit_version_summary_label))
                },
                singleLine = true,
                onDismissRequest = { updateOperation(VersionsOperation.None) },
                onConfirm = {
                    setVersionSummary(value)
                    updateOperation(VersionsOperation.None)
                }
            )
        }
        is VersionsOperation.RunTask -> {
            val errorMessage = stringResource(R.string.versions_manage_task_error)
            SimpleTaskDialog(
                title = stringResource(versionsOperation.title),
                task = versionsOperation.task,
                context = Dispatchers.IO,
                onDismiss = { updateOperation(VersionsOperation.None) },
                onError = { e ->
                    Logger.error(TAG, "Failed to run task.", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(errorMessage),
                            message = androidText(e.getMessageOrToString())
                        )
                    )
                }
            )
        }
    }
}

