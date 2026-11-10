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

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import net.burningtnt.terracotta.TerracottaAndroidAPI

object FriendsManager {
    private const val PREFS_NAME = "cubiq_multiplayer_friends"
    private const val KEY_SAVED = "saved_friends"

    private fun getPrefs(): SharedPreferences {
        return GlobalContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSavedFriends(): List<Pair<String, String>> {
        val raw = getPrefs().getString(KEY_SAVED, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val idx = entry.indexOf('=')
            if (idx > 0) entry.substring(0, idx) to entry.substring(idx + 1) else null
        }
    }

    fun saveFriend(name: String, code: String) {
        val list = getSavedFriends().filter { it.second != code }.toMutableList()
        list.add(0, (name.ifBlank { "Friend" }) to code)
        val serialized = list.take(8).joinToString(";") { "${it.first}=${it.second}" }
        getPrefs().edit().putString(KEY_SAVED, serialized).apply()
    }

    fun removeFriend(code: String) {
        val list = getSavedFriends().filter { it.second != code }
        val serialized = list.joinToString(";") { "${it.first}=${it.second}" }
        getPrefs().edit().putString(KEY_SAVED, serialized).apply()
    }
}

/** 等待中：房客操作状态 */
sealed interface GuestWaitingOperation {
    data object None : GuestWaitingOperation
    /** 被点击，开始输入邀请码 */
    data object OnClick : GuestWaitingOperation
}

@Composable
fun GuestWaitingOperation(
    operation: GuestWaitingOperation,
    onChange: (GuestWaitingOperation) -> Unit,
    onPositive: (roomCode: String) -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    when (operation) {
        is GuestWaitingOperation.None -> {}
        is GuestWaitingOperation.OnClick -> {
            InviteCodeInputDialog(
                onPositive = onPositive,
                onDismiss = {
                    onChange(GuestWaitingOperation.None)
                },
                onShowToast = onShowToast
            )
        }
    }
}

/**
 * 房客输入邀请码对话框
 */
@Composable
private fun InviteCodeInputDialog(
    onPositive: (roomCode: String) -> Unit,
    onDismiss: () -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val savedFriends = remember { mutableStateOf(FriendsManager.getSavedFriends()) }

    /** 验证不通过时 */
    var isError by remember { mutableStateOf(false) }
    val cleanCode = remember(code) {
        if (code.startsWith("CQ-", ignoreCase = true)) code.substring(3).trim() else code.trim()
    }
    val supportingText: AndroidStringText? = remember(cleanCode) {
        if (cleanCode.isEmpty()) {
            //还未填写内容
            isError = false
            return@remember null
        }

        val type = Terracotta.parseRoomCode(cleanCode)
        when (type) {
            TerracottaAndroidAPI.RoomType.TERRACOTTA_LEGACY -> androidText(R.string.terracotta_status_waiting_guest_prompt_terracotta_legacy)
            TerracottaAndroidAPI.RoomType.PCL2CE -> androidText(R.string.terracotta_status_waiting_guest_prompt_pcl2ce)
            TerracottaAndroidAPI.RoomType.SCAFFOLDING -> androidText(R.string.terracotta_status_waiting_guest_prompt_scaffolding)
            else -> null
        }.also { text ->
            //根据是否检测出对应格式判断
            isError = text == null
        } ?: androidText(R.string.terracotta_status_waiting_guest_prompt_invalid)
    }

    SimpleEditDialog(
        title = stringResource(R.string.terracotta_status_waiting_guest_prompt_title),
        value = code,
        onValueChange = { value ->
            code = value
        },
        label = (@Composable { Text(text = "CQ-XXXXXX or U/XXXX-XXXX...") }).takeIf { code.isEmpty() },
        supportingText = supportingText?.let { text ->
            {
                AndroidStringText(text = text)
            }
        },
        singleLine = true,
        isError = isError,
        extraContent = if (savedFriends.value.isNotEmpty()) {
            {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Saved Friends & Recent Codes:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(savedFriends.value) { (friendName, friendCode) ->
                            SuggestionChip(
                                onClick = { code = friendCode },
                                label = { Text(text = "$friendName (${friendCode.take(8)}...)") }
                            )
                        }
                    }
                }
            }
        } else null,
        onConfirm = {
            val targetCode = if (code.startsWith("CQ-", ignoreCase = true)) code.substring(3).trim() else code.trim()
            if (isError || targetCode.isEmptyOrBlank() || Terracotta.parseRoomCode(targetCode) == null) {
                onShowToast(androidText(R.string.terracotta_status_waiting_guest_prompt_invalid))
            } else {
                FriendsManager.saveFriend("Friend ${savedFriends.value.size + 1}", targetCode)
                onPositive(targetCode)
                onDismiss()
            }
        },
        onDismissRequest = onDismiss
    )
}