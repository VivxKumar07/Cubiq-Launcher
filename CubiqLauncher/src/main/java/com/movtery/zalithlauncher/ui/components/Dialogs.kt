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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowManager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Authentic Minecraft PC Launcher Dialog Container
 */
@Composable
fun MinecraftDialogContainer(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val isImeVisible = WindowInsets.isImeVisible
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            dismissOnBackPress = !isImeVisible && dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        val view = LocalView.current
        androidx.compose.runtime.DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            onDispose {}
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .widthIn(min = 310.dp, max = 480.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFF1E1E20),
                contentColor = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFF3C3B3B)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 返回对话框内容可用的最大高度，以窗口容器高度为上界
 *
 * 输入法动画期间窗口高度可能被系统短暂压缩，这里记录最大值，避免对话框高度随之抖动
 */
@Composable
fun rememberDialogMaxHeight(): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    var maxHeight by remember(windowInfo) { mutableStateOf(windowInfo.containerSize.height) }
    maxHeight = maxOf(maxHeight, windowInfo.containerSize.height)
    return with(density) { maxHeight.toDp() }
}

/**
 * 展示警告对话框
 *
 * @param title 对话框标题
 * @param text 对话框内容
 * @param dismissByDialog 是否允许Dialog自己请求关闭
 * @param onConfirm 点击确认按钮的回调
 * @param onDismiss 点击取消或对话框外部的回调
 */
@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissText: String = stringResource(R.string.generic_cancel),
    dismissByDialog: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MinecraftDialogContainer(
        onDismissRequest = {
            if (dismissByDialog) onDismiss()
        }
    ) {
        Text(
            text = title,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .verticalScrollWithBar(state = scrollState)
                .weight(1f, fill = false)
        ) {
            Text(
                text = text,
                color = Color(0xFFD0D0D0),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MinecraftButton(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                onClick = onDismiss,
                style = MinecraftButtonStyle.STONE,
                text = dismissText.uppercase(),
                fontSize = 13.sp
            )
            MinecraftButton(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                onClick = onConfirm,
                style = MinecraftButtonStyle.GREEN,
                text = confirmText.uppercase(),
                fontSize = 13.sp
            )
        }
    }
}

/**
 * 展示警告对话框
 *
 * @param title 对话框标题
 * @param text 对话框内容
 * @param dismissByDialog 是否允许Dialog自己请求关闭
 * @param onDismiss 点击确认或对话框外部的回调
 */
@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissByDialog: Boolean = true,
    onDismiss: () -> Unit
) {
    MinecraftDialogContainer(
        onDismissRequest = {
            if (dismissByDialog) onDismiss()
        }
    ) {
        Text(
            text = title,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .verticalScrollWithBar(state = scrollState)
                .weight(1f, fill = false)
        ) {
            Text(
                text = text,
                color = Color(0xFFD0D0D0),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        MinecraftButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            onClick = onDismiss,
            style = MinecraftButtonStyle.GREEN,
            text = confirmText.uppercase(),
            fontSize = 13.sp
        )
    }
}

@Composable
fun SimpleAlertDialog(
    title: String,
    text: @Composable () -> Unit = {},
    confirmText: String = stringResource(R.string.generic_confirm),
    dismissText: String = stringResource(R.string.generic_cancel),
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit = {}
) {
    MinecraftDialogContainer(onDismissRequest = onDismissRequest) {
        Text(
            text = title,
            fontFamily = MinecraftFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fadeEdge(state = scrollState)
                .verticalScrollWithBar(state = scrollState)
                .weight(1f, fill = false)
        ) {
            text()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MinecraftButton(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                onClick = onCancel,
                style = MinecraftButtonStyle.STONE,
                text = dismissText.uppercase(),
                fontSize = 13.sp
            )
            MinecraftButton(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                onClick = onConfirm,
                style = MinecraftButtonStyle.GREEN,
                text = confirmText.uppercase(),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SimpleEditDialog(
    title: String,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    onCancel: () -> Unit = onDismissRequest,
    onConfirm: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        ImePanContainer(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .widthIn(min = 310.dp, max = 460.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFF1E1E20),
                contentColor = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFF3C3B3B)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    simpleEditDialogBody(
                        title = title,
                        value = value,
                        onValueChange = onValueChange,
                        label = label,
                        isError = isError,
                        supportingText = supportingText,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        keyboardOptions = keyboardOptions,
                        extraBody = extraBody,
                        extraContent = extraContent
                    ).invoke(this)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MinecraftButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            onClick = onCancel,
                            style = MinecraftButtonStyle.STONE,
                            text = stringResource(R.string.generic_cancel).uppercase(),
                            fontSize = 13.sp
                        )
                        MinecraftButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            onClick = onConfirm,
                            style = MinecraftButtonStyle.GREEN,
                            text = stringResource(R.string.generic_confirm).uppercase(),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleEditDialog(
    title: String,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        ImePanContainer(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .widthIn(min = 310.dp, max = 460.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFF1E1E20),
                contentColor = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFF3C3B3B)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    simpleEditDialogBody(
                        title = title,
                        value = value,
                        onValueChange = onValueChange,
                        label = label,
                        isError = isError,
                        supportingText = supportingText,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        keyboardOptions = keyboardOptions,
                        extraBody = extraBody,
                        extraContent = extraContent
                    ).invoke(this)

                    MinecraftButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        onClick = onConfirm,
                        style = MinecraftButtonStyle.GREEN,
                        text = stringResource(R.string.generic_confirm).uppercase(),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun simpleEditDialogBody(
    title: String,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit = {}
): @Composable ColumnScope.() -> Unit = {
    Text(
        text = title,
        fontFamily = MinecraftFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.White
    )

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fadeEdge(state = scrollState)
            .weight(1f, fill = false)
            .verticalScrollWithBar(state = scrollState)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        extraBody?.let {
            it.invoke()
            Spacer(modifier = Modifier.size(8.dp))
        }

        val focusManager = LocalFocusManager.current

        if (singleLine) {
            SingleLineTextCheck(
                text = value,
                onSingleLined = onValueChange
            )
        }

        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = isError,
            supportingText = supportingText,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(true)
                    onConfirm()
                }
            ),
            shape = MaterialTheme.shapes.large
        )
        extraContent?.invoke()
    }
}

@Composable
fun SimpleCheckEditDialog(
    title: String,
    text: String,
    value: String,
    checked: Boolean,
    checkBoxText: String? = null,
    onValueChange: (newValue: String) -> Unit,
    onCheckedChange: (newValue: Boolean) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    SimpleEditDialog(
        title = title,
        value = value,
        onValueChange = onValueChange,
        label = label,
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        extraBody = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        extraContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    checkBoxText?.let{ Text(text = it, style = MaterialTheme.typography.labelMedium) }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = onCheckedChange
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm
    )
}

/**
 * 一个很简单的列表Dialog
 * @param items 需要列出的items
 * @param onItemSelected item被点击的回调
 * @param onDismissRequest dialog被关闭的回调
 * @param showConfirm 是否通过确认按钮来触发item的点击回调
 */
@Composable
fun <T> SimpleListDialog(
    title: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onDismissRequest: (selected: Boolean) -> Unit,
    current: T? = null,
    itemLayout: @Composable (item: T, isCurrent: Boolean, onClick: () -> Unit) -> Unit,
    showConfirm: Boolean = false,
    confirmText: @Composable RowScope.() -> Unit = {
        MarqueeText(text = stringResource(R.string.generic_confirm))
    }
) {
    var selectedItem: T? by remember { mutableStateOf(current) }

    Dialog(
        onDismissRequest = {
            onDismissRequest(false)
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .widthIn(min = 310.dp, max = 460.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = RoundedCornerShape(3.dp),
                color = Color(0xFF1E1E20),
                contentColor = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFF3C3B3B)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(14.dp))

                    val state = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = state)
                            .weight(1f, fill = false),
                        state = state
                    ) {
                        items(items) { item ->
                            val isCurrent = selectedItem == item
                            itemLayout(item, isCurrent) {
                                selectedItem = item
                                if (!showConfirm && !isCurrent) {
                                    onItemSelected(item)
                                    onDismissRequest(true)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))

                    if (showConfirm) {
                        MinecraftButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            onClick = {
                                if (selectedItem != null) {
                                    onItemSelected(selectedItem!!)
                                    onDismissRequest(true)
                                }
                            },
                            style = MinecraftButtonStyle.GREEN,
                            text = stringResource(R.string.generic_confirm).uppercase(),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTaskDialog(
    title: String,
    text: String? = null,
    task: suspend () -> Unit,
    context: CoroutineContext = Dispatchers.Default,
    onDismiss: () -> Unit,
    onError: (Throwable) -> Unit = {}
) {
    var inProgress by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (inProgress) {
        ProgressDialog(
            title = title,
            text = text
        )
    } else {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        inProgress = true
        scope.launch(context) {
            try {
                task()
            } catch (e: Throwable) {
                onError(e)
            } finally {
                inProgress = false
            }
        }
    }
}

@Composable
fun ProgressDialog(
    title: String = stringResource(R.string.generic_in_progress),
    text: String? = null
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier.widthIn(min = 280.dp, max = 420.dp),
            shape = RoundedCornerShape(3.dp),
            color = Color(0xFF1E1E20),
            contentColor = Color.White,
            border = BorderStroke(1.5.dp, Color(0xFF3C3B3B)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = MinecraftFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                text?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = it,
                        color = Color(0xFFD0D0D0),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF42A62C),
                    trackColor = Color(0xFF2C2C2E)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}