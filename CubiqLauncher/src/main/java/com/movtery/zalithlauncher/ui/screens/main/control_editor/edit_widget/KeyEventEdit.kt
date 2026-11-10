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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.inputmap.keycodes.ControlEventKeyName
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableClickEventsProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.control.Keyboard
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onItemColor

/**
 * 按键事件编辑 (4-Slot Quick Key Mapping / Mojo & Pojav Style)
 */
@Composable
fun KeyEventEdit(
    provider: ObservableClickEventsProvider,
    modifier: Modifier = Modifier,
    containerColor: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    var showKeyboard by remember { mutableStateOf(false) }
    var targetSlotIndex by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberLazyListState()

    val keyEvents = remember(provider.clickEvents) {
        provider.clickEvents.filter { it.type == ClickEvent.Type.Key }
    }

    LazyColumn(
        modifier = Modifier
            .nonInteractiveScrollbar(
                state = scrollState.scrollIndicatorState!!,
                orientation = Orientation.Vertical,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        state = scrollState,
    ) {
        // Section Header: 4 Quick Key Mappings (Suggestions 2 & 7)
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text(
                    text = "Key Mappings (4 Slots)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "Tap a slot to assign or swap keycodes. Supports attribute swaps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }

        // Row 1: Slots 1 & 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeySlotCard(
                    modifier = Modifier.weight(1f),
                    slotLabel = "Mapping 1",
                    keyEvent = keyEvents.getOrNull(0),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = {
                        targetSlotIndex = 0
                        showKeyboard = true
                    },
                    onDelete = {
                        keyEvents.getOrNull(0)?.let { provider.onRemoveEvent(it) }
                    }
                )
                KeySlotCard(
                    modifier = Modifier.weight(1f),
                    slotLabel = "Mapping 2",
                    keyEvent = keyEvents.getOrNull(1),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = {
                        targetSlotIndex = 1
                        showKeyboard = true
                    },
                    onDelete = {
                        keyEvents.getOrNull(1)?.let { provider.onRemoveEvent(it) }
                    }
                )
            }
        }

        // Row 2: Slots 3 & 4
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeySlotCard(
                    modifier = Modifier.weight(1f),
                    slotLabel = "Mapping 3",
                    keyEvent = keyEvents.getOrNull(2),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = {
                        targetSlotIndex = 2
                        showKeyboard = true
                    },
                    onDelete = {
                        keyEvents.getOrNull(2)?.let { provider.onRemoveEvent(it) }
                    }
                )
                KeySlotCard(
                    modifier = Modifier.weight(1f),
                    slotLabel = "Mapping 4",
                    keyEvent = keyEvents.getOrNull(3),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = {
                        targetSlotIndex = 3
                        showKeyboard = true
                    },
                    onDelete = {
                        keyEvents.getOrNull(3)?.let { provider.onRemoveEvent(it) }
                    }
                )
            }
        }

        // Section for extra keys if more than 4 keys are bound
        if (keyEvents.size > 4) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Additional Key Triggers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            itemsIndexed(keyEvents.drop(4)) { index, event ->
                EditKeyItem(
                    modifier = Modifier.fillMaxWidth(),
                    keyEvent = event,
                    onDelete = {
                        provider.onRemoveEvent(event)
                    },
                    color = containerColor,
                    contentColor = contentColor,
                )
            }
        }

        // Generic "Add Extra Key" button
        item {
            Spacer(modifier = Modifier.height(4.dp))
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_key_new),
                onClick = {
                    targetSlotIndex = null
                    showKeyboard = true
                },
                color = containerColor,
                contentColor = contentColor,
                showArrow = false
            )
        }
    }

    if (showKeyboard) {
        Keyboard(
            onDismissRequest = {
                showKeyboard = false
                targetSlotIndex = null
            },
            isTapMode = true,
            onTap = { selectedKey ->
                val slot = targetSlotIndex
                if (slot != null && slot < keyEvents.size) {
                    val oldEvent = keyEvents[slot]
                    provider.onRemoveEvent(oldEvent)
                }
                provider.onAddEvent(ClickEvent(type = ClickEvent.Type.Key, key = selectedKey))
                showKeyboard = false
                targetSlotIndex = null
            }
        )
    }
}

@Composable
private fun KeySlotCard(
    slotLabel: String,
    keyEvent: ClickEvent?,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAssigned = keyEvent != null
    val keyName = remember(keyEvent?.key) {
        keyEvent?.key?.let { ControlEventKeyName.getNameByKey(it) ?: it }
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                if (isAssigned) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.generic_delete),
                            tint = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isAssigned && keyName != null) {
                Text(
                    text = keyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
            } else {
                Text(
                    text = "+ Map Key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EditKeyItem(
    modifier: Modifier = Modifier,
    keyEvent: ClickEvent,
    onDelete: () -> Unit,
    color: Color = itemColor(false),
    contentColor: Color = onItemColor(),
) {
    val name = remember(keyEvent.key) { ControlEventKeyName.getNameByKey(keyEvent.key) }

    InfoLayoutItem(
        modifier = modifier,
        onClick = {},
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeText(
                text = stringResource(R.string.control_editor_edit_event_key_value, name ?: keyEvent.key),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = onDelete
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_outlined),
                contentDescription = stringResource(R.string.generic_delete)
            )
        }
    }
}
