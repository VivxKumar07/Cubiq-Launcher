package com.movtery.zalithlauncher.friends.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.friends.CubiqChatMessage
import com.movtery.zalithlauncher.friends.CubiqFriend
import com.movtery.zalithlauncher.friends.CubiqFriendsManager
import com.movtery.zalithlauncher.friends.FriendStatus
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import kotlinx.coroutines.delay

@Composable
fun MinecraftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    fontSize: Int = 13,
    textColor: Color = if (enabled) Color.White else Color(0xFFAAAAAA)
) {
    val bgColor = when {
        !enabled -> Color(0x66222222)
        isSelected -> Color(0xDD3A3A4A)
        else -> Color(0xCC2A2A35)
    }
    val borderColor = if (isSelected) Color(0xFFFFFFFF) else Color(0x88555566)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = MinecraftFontFamily,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CubiqFriendsDialog(
    onDismissRequest: () -> Unit,
    onJoinFriendWorld: (CubiqFriend) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.90f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xE614141A))
                    .border(2.dp, Color(0xFF444455), RoundedCornerShape(6.dp))
                    .shadow(16.dp)
                    .padding(14.dp)
            ) {
                CubiqFriendsContent(
                    modifier = Modifier.fillMaxSize(),
                    onClose = onDismissRequest,
                    onJoinFriendWorld = onJoinFriendWorld
                )
            }
        }
    }
}

@Composable
fun CubiqFriendsContent(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    onJoinFriendWorld: (CubiqFriend) -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        CubiqFriendsManager.initialize(context)
    }

    val friends by CubiqFriendsManager.friends.collectAsStateWithLifecycle()
    val chatMessages by CubiqFriendsManager.messages.collectAsStateWithLifecycle()
    val isHosting by CubiqFriendsManager.isHostingMyWorld.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Friends, 1: Chat, 2: Host
    var inputFriendCode by remember { mutableStateOf("") }
    var chatInputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
                    // Header Area
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Cubiq Friends",
                                color = Color.White,
                                fontFamily = MinecraftFontFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚙",
                                color = Color(0xFFAAAAAA),
                                fontSize = 15.sp,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Cubiq Friends v1.2.0 • Decentralized P2P Mesh", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subheader: Username + Code + Copy Code
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${CubiqFriendsManager.myUsername} · your code: ",
                                color = Color(0xFFDDDDDD),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 12.sp
                            )
                            Text(
                                text = CubiqFriendsManager.myFriendCode,
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            MinecraftButton(
                                text = "Copy Code",
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("Cubiq Friend Code", CubiqFriendsManager.myFriendCode))
                                    Toast.makeText(context, "Copied code: ${CubiqFriendsManager.myFriendCode}", Toast.LENGTH_SHORT).show()
                                },
                                fontSize = 11,
                                modifier = Modifier.height(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Navigation Tabs Row: [Friends (N)] [Chat] [Host]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MinecraftButton(
                                text = "Friends (${friends.size})",
                                onClick = { selectedTab = 0 },
                                isSelected = selectedTab == 0,
                                modifier = Modifier.weight(1f).height(34.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftButton(
                                text = "Chat",
                                onClick = { selectedTab = 1 },
                                isSelected = selectedTab == 1,
                                modifier = Modifier.weight(1f).height(34.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MinecraftButton(
                                text = if (isHosting) "Host (Active)" else "Host",
                                onClick = { selectedTab = 2 },
                                isSelected = selectedTab == 2,
                                textColor = if (isHosting) Color(0xFF55FF55) else Color.White,
                                modifier = Modifier.weight(1f).height(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Content Area (Switch by Tab)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x77000000))
                            .border(1.dp, Color(0xFF333344), RoundedCornerShape(3.dp))
                            .padding(10.dp)
                    ) {
                        when (selectedTab) {
                            0 -> FriendsTabContent(
                                friends = friends,
                                inputFriendCode = inputFriendCode,
                                onFriendCodeChange = { inputFriendCode = it },
                                onAddFriend = {
                                    if (CubiqFriendsManager.addFriend(inputFriendCode, context = context)) {
                                        inputFriendCode = ""
                                    }
                                },
                                onJoinWorld = onJoinFriendWorld,
                                onOpenChat = { selectedTab = 1 }
                            )
                            1 -> ChatTabContent(
                                messages = chatMessages,
                                chatInputText = chatInputText,
                                onChatInputChange = { chatInputText = it },
                                onSendMessage = {
                                    CubiqFriendsManager.sendMessage(chatInputText)
                                    chatInputText = ""
                                }
                            )
                            2 -> HostTabContent(
                                isHosting = isHosting,
                                onToggleHost = {
                                    if (isHosting) {
                                        CubiqFriendsManager.stopHosting(context)
                                    } else {
                                        CubiqFriendsManager.hostWorld(port = 25565, context = context)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

        if (onClose != null) {
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Area: [Done] Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                MinecraftButton(
                    text = "Done",
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(36.dp)
                )
            }
        }
    }
}

@Composable
private fun FriendsTabContent(
    friends: List<CubiqFriend>,
    inputFriendCode: String,
    onFriendCodeChange: (String) -> Unit,
    onAddFriend: () -> Unit,
    onJoinWorld: (CubiqFriend) -> Unit,
    onOpenChat: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Friend code input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF000000))
                    .border(1.5.dp, Color(0xFF666677), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputFriendCode.isEmpty()) {
                    Text(
                        text = "Friend code (CQ-XXXXXX)",
                        color = Color(0xFF777788),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                }
                BasicTextField(
                    value = inputFriendCode,
                    onValueChange = onFriendCodeChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MinecraftButton(
                text = "Add",
                onClick = onAddFriend,
                modifier = Modifier.height(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Friends list
        if (friends.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No friends added yet.\nShare your friend code to play together!",
                    color = Color(0xFFAAAAAA),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(friends, key = { it.id }) { friend ->
                    FriendItemRow(
                        friend = friend,
                        onJoin = { onJoinWorld(friend) },
                        onChat = onOpenChat,
                        onRemove = { CubiqFriendsManager.removeFriend(friend.id) },
                        onAccept = { CubiqFriendsManager.acceptFriendRequest(friend.id, context) },
                        onDecline = { CubiqFriendsManager.declineFriendRequest(friend.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendItemRow(
    friend: CubiqFriend,
    onJoin: () -> Unit,
    onChat: () -> Unit,
    onRemove: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0x4422222E))
            .border(0.8.dp, Color(0x44555566), RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (friend.isPendingRequest) {
            // Pending request row: name (code) wants to... [Accept] [Decline]
            Text(
                text = "${friend.name} (${friend.code}) wants to connect...",
                color = Color(0xFFFFAA00),
                fontFamily = MinecraftFontFamily,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MinecraftButton(
                    text = "Accept",
                    onClick = onAccept,
                    textColor = Color(0xFF55FF55),
                    fontSize = 10,
                    modifier = Modifier.height(26.dp)
                )
                MinecraftButton(
                    text = "Decline",
                    onClick = onDecline,
                    textColor = Color(0xFFFF5555),
                    fontSize = 10,
                    modifier = Modifier.height(26.dp)
                )
            }
        } else {
            // Normal friend row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Online/Hosting status dot
                val dotColor = when (friend.status) {
                    FriendStatus.HOSTING -> Color(0xFF55FFFF)
                    FriendStatus.ONLINE -> Color(0xFF55FF55)
                    FriendStatus.OFFLINE -> Color(0xFF777777)
                }
                Text(
                    text = "•",
                    color = dotColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = friend.name,
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (friend.status) {
                            FriendStatus.HOSTING -> "Hosting world on ${friend.hostAddress ?: "LAN"}:${friend.hostPort ?: 25565}"
                            FriendStatus.ONLINE -> "Online"
                            FriendStatus.OFFLINE -> "Offline"
                        },
                        color = when (friend.status) {
                            FriendStatus.HOSTING -> Color(0xFF55FFFF)
                            FriendStatus.ONLINE -> Color(0xFF55FF55)
                            FriendStatus.OFFLINE -> Color(0xFF888888)
                        },
                        fontFamily = MinecraftFontFamily,
                        fontSize = 9.5.sp
                    )
                }
            }

            // Action Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (friend.status == FriendStatus.HOSTING) {
                    MinecraftButton(
                        text = "Join",
                        onClick = onJoin,
                        textColor = Color(0xFF55FF55),
                        fontSize = 11,
                        modifier = Modifier.height(28.dp)
                    )
                } else {
                    MinecraftButton(
                        text = "Invite",
                        onClick = {
                            Toast.makeText(context, "Invited ${friend.name} to your game!", Toast.LENGTH_SHORT).show()
                        },
                        fontSize = 11,
                        modifier = Modifier.height(28.dp)
                    )
                }

                MinecraftButton(
                    text = "Chat",
                    onClick = onChat,
                    fontSize = 11,
                    modifier = Modifier.height(28.dp)
                )

                MinecraftButton(
                    text = "X",
                    onClick = onRemove,
                    textColor = Color(0xFFFF5555),
                    fontSize = 11,
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatTabContent(
    messages: List<CubiqChatMessage>,
    chatInputText: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet.\nChat with your Cubiq Friends in real time!",
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (msg.isFromMe) Color(0xFF1E3A5F) else Color(0xFF2C2C38))
                                .border(1.dp, if (msg.isFromMe) Color(0xFF3B6B9B) else Color(0xFF4A4A5A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (msg.isFromMe) "You" else msg.senderName,
                                    color = if (msg.isFromMe) Color(0xFF88CCFF) else Color(0xFFFFAA00),
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.message,
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF000000))
                    .border(1.5.dp, Color(0xFF666677), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (chatInputText.isEmpty()) {
                    Text(
                        text = "Type message...",
                        color = Color(0xFF777788),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.sp
                    )
                }
                BasicTextField(
                    value = chatInputText,
                    onValueChange = onChatInputChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 12.5.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            MinecraftButton(
                text = "Send",
                onClick = onSendMessage,
                modifier = Modifier.height(34.dp)
            )
        }
    }
}

@Composable
private fun HostTabContent(
    isHosting: Boolean,
    onToggleHost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Host Multiplayer World",
            color = Color.White,
            fontFamily = MinecraftFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Open your singleplayer world to your Cubiq Friends list.\nFriends can discover and join your world with a single tap!",
            color = Color(0xFFCCCCCC),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isHosting) Color(0x3300FF00) else Color(0x33FFFFFF))
                .border(1.dp, if (isHosting) Color(0xFF55FF55) else Color(0xFF666677), RoundedCornerShape(4.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isHosting) "STATUS: WORLD HOSTED & ONLINE" else "STATUS: NOT HOSTING",
                    color = if (isHosting) Color(0xFF55FF55) else Color(0xFFFFAA00),
                    fontFamily = MinecraftFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isHosting) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Port: ${CubiqFriendsManager.myHostPort} • Broadcasting to Friends",
                        color = Color(0xFFDDDDDD),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        MinecraftButton(
            text = if (isHosting) "Stop Hosting" else "Host World (Open to Friends)",
            onClick = onToggleHost,
            textColor = if (isHosting) Color(0xFFFF5555) else Color(0xFF55FF55),
            fontSize = 13,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(40.dp)
        )
    }
}

/**
 * Authentic In-Game Toast Card (Matches CS Launcher Plus top-right toast)
 */
@Composable
fun CubiqFriendsInGameToast(
    modifier: Modifier = Modifier,
    onJoin: (CubiqFriend) -> Unit
) {
    val activeHost by CubiqFriendsManager.activeWorldHost.collectAsStateWithLifecycle()
    var visibleHost by remember { mutableStateOf<CubiqFriend?>(null) }

    LaunchedEffect(activeHost) {
        if (activeHost != null && activeHost?.status == FriendStatus.HOSTING) {
            visibleHost = activeHost
            delay(8000L)
            visibleHost = null
        }
    }

    AnimatedVisibility(
        visible = visibleHost != null,
        enter = fadeIn() + slideInHorizontally { it },
        exit = fadeOut() + slideOutHorizontally { it },
        modifier = modifier
    ) {
        val host = visibleHost ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .padding(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xE60D1B2A))
                .border(1.5.dp, Color(0xFF415A77), RoundedCornerShape(3.dp))
                .shadow(8.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[!] Cubiq Friends",
                            color = Color(0xFF55FFFF),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${host.name} is hosting a world",
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontSize = 11.5.sp
                    )
                }

                MinecraftButton(
                    text = "Join",
                    onClick = {
                        onJoin(host)
                        visibleHost = null
                    },
                    textColor = Color(0xFF55FF55),
                    fontSize = 11,
                    modifier = Modifier.height(28.dp)
                )

                Text(
                    text = "✕",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { visibleHost = null }
                )
            }
        }
    }
}
