/*
 * Cubiq Launcher
 * Minecraft Official Realms & Multiplayer Servers Screen
 */

package com.movtery.zalithlauncher.ui.screens.content.minecraft

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.MinecraftButton
import com.movtery.zalithlauncher.ui.components.MinecraftButtonStyle
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily

data class MinecraftServerEntry(
    val name: String,
    val address: String,
    val motd: Int,
    val pingMs: Int = 32
)

data class CubiqFriend(
    val username: String,
    val status: String = "Online",
    val worldIp: String = "",
    val isOnline: Boolean = true
)

@Composable
fun MinecraftRealmsScreen(
    account: Account?,
    currentVersion: Version?,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("cubiq_friends_data", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Realms & Servers, 1: Friends System
    var showAddServerDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    val servers = remember {
        mutableStateListOf(
            MinecraftServerEntry("Skillora SMP", "paid51.skilloraclouds.com:20015", R.string.cubiq_realms_server_skillora, 24),
            MinecraftServerEntry("Hypixel Network", "mc.hypixel.net", R.string.cubiq_realms_server_hypixel, 28),
            MinecraftServerEntry("CubeCraft Games", "play.cubecraft.net", R.string.cubiq_realms_server_cubecraft, 35),
            MinecraftServerEntry("Complex Gaming", "hub.mc-complex.com", R.string.cubiq_realms_server_complex, 42),
            MinecraftServerEntry("Wynncraft", "play.wynncraft.com", R.string.cubiq_realms_server_wynncraft, 31)
        )
    }

    val friends = remember {
        val saved = prefs.getString("friends_json", null)
        val initialList = if (!saved.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<CubiqFriend>>() {}.type
                gson.fromJson<List<CubiqFriend>>(saved, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            listOf(
                CubiqFriend("Steve_Pro", "Playing on Skillora SMP", "paid51.skilloraclouds.com:20015", true),
                CubiqFriend("Alex_Craft", "In Local World (LAN)", "192.168.1.105:25565", true),
                CubiqFriend("Notch", "Online", "", true),
                CubiqFriend("Jeb_", "Offline (Last seen 2h ago)", "", false)
            )
        }
        mutableStateListOf<CubiqFriend>().apply { addAll(initialList) }
    }

    fun saveFriends() {
        val json = gson.toJson(friends.toList())
        prefs.edit().putString("friends_json", json).apply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Tab Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinecraftButton(
                    onClick = { selectedTab = 0 },
                    style = if (selectedTab == 0) MinecraftButtonStyle.GREEN else MinecraftButtonStyle.DARK,
                    text = stringResource(R.string.cubiq_realms_servers_realms),
                    fontSize = 10.5.sp,
                    modifier = Modifier.weight(1f).height(32.dp)
                )
                MinecraftButton(
                    onClick = { selectedTab = 1 },
                    style = if (selectedTab == 1) MinecraftButtonStyle.GREEN else MinecraftButtonStyle.DARK,
                    text = stringResource(R.string.cubiq_realms_friends_system),
                    fontSize = 10.5.sp,
                    modifier = Modifier.weight(1f).height(32.dp)
                )
            }

            if (selectedTab == 0) {
                // Realms Promo Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_mc_hero_latest),
                        contentDescription = stringResource(R.string.cubiq_realms_realms_banner),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE6101012), Color(0x9918181A), Color(0x33000000))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.cubiq_realms_realms_multiplayer),
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.cubiq_realms_realms_multiplayer_desc),
                                color = Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.5.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MinecraftButton(
                                onClick = {
                                    onOpenLink("https://www.minecraft.net/realms")
                                },
                                style = MinecraftButtonStyle.GREEN,
                                text = stringResource(R.string.cubiq_realms_try_plus),
                                fontSize = 10.sp,
                                modifier = Modifier.height(30.dp)
                            )
                            MinecraftButton(
                                onClick = { showAddServerDialog = true },
                                style = MinecraftButtonStyle.STONE,
                                text = stringResource(R.string.cubiq_realms_add_server_short),
                                fontSize = 10.sp,
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Servers List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.cubiq_realms_featured),
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.cubiq_realms_server_count, servers.size),
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Server Cards
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(servers) { srv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(3.dp))
                                .background(Color(0xFF1A1A1C))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.img_minecraft),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = srv.name,
                                        color = Color.White,
                                        fontFamily = MinecraftFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = srv.address,
                                        color = Color(0xFF55FF55),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 10.5.sp
                                    )
                                    Text(
                                        text = stringResource(srv.motd),
                                        color = Color(0xFF888888),
                                        fontFamily = MinecraftFontFamily,
                                        fontSize = 9.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            MinecraftButton(
                                onClick = {
                                    onLaunchGame(currentVersion)
                                },
                                style = MinecraftButtonStyle.GREEN,
                                text = stringResource(R.string.cubiq_realms_join),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .width(65.dp)
                                    .height(32.dp)
                            )
                        }
                    }
                }
            } else {
                // Friends System View
                // Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_mc_hero_latest),
                        contentDescription = stringResource(R.string.cubiq_realms_friends_banner),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE6101012), Color(0x9918181A), Color(0x33000000))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.cubiq_realms_adventure),
                                color = Color(0xFF55FF55),
                                fontFamily = MinecraftFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stringResource(R.string.cubiq_realms_adventure_desc),
                                color = Color(0xFFCCCCCC),
                                fontFamily = MinecraftFontFamily,
                                fontSize = 10.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MinecraftButton(
                                onClick = { showAddFriendDialog = true },
                                style = MinecraftButtonStyle.GREEN,
                                text = stringResource(R.string.cubiq_realms_add_friend_plus),
                                fontSize = 10.sp,
                                modifier = Modifier.height(30.dp)
                            )
                            MinecraftButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(context.getString(R.string.cubiq_realms_clip_lan_ip), "cubiq://connect/${account?.username ?: "Player"}")
                                    cm.setPrimaryClip(clip)
                                    Toast.makeText(context, context.getString(R.string.cubiq_realms_friend_link_copied), Toast.LENGTH_SHORT).show()
                                },
                                style = MinecraftButtonStyle.STONE,
                                text = stringResource(R.string.cubiq_realms_share_invite),
                                fontSize = 10.sp,
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Friends List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.cubiq_realms_friends_list),
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(R.string.cubiq_realms_friend_count, friends.size),
                        color = Color(0xFF888888),
                        fontFamily = MinecraftFontFamily,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Friends List
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cubiq_realms_friends_empty),
                            color = Color(0xFF777777),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF2C2C2E)), RoundedCornerShape(3.dp))
                                    .background(Color(0xFF1A1A1C))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF242426))
                                            .border(1.dp, Color(0xFF444444), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = "https://mc-heads.net/avatar/${friend.username}/64.png",
                                            contentDescription = friend.username,
                                            modifier = Modifier.size(32.dp),
                                            fallback = painterResource(R.drawable.img_minecraft),
                                            error = painterResource(R.drawable.img_minecraft)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(if (friend.isOnline) Color(0xFF55FF55) else Color(0xFF888888))
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = friend.username,
                                                color = Color.White,
                                                fontFamily = MinecraftFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Text(
                                            text = when (friend.status) {
                                                "Playing on Skillora SMP" -> stringResource(R.string.cubiq_realms_status_skillora)
                                                "In Local World (LAN)" -> stringResource(R.string.cubiq_realms_status_lan)
                                                "Online" -> stringResource(R.string.cubiq_realms_status_online)
                                                "Offline (Last seen 2h ago)" -> stringResource(R.string.cubiq_realms_status_offline)
                                                "Playing on World" -> stringResource(R.string.cubiq_realms_status_playing)
                                                else -> friend.status
                                            },
                                            color = if (friend.isOnline) Color(0xFF55FF55) else Color(0xFF888888),
                                            fontFamily = MinecraftFontFamily,
                                            fontSize = 10.sp
                                        )
                                        if (friend.worldIp.isNotBlank()) {
                                            Text(
                                                text = stringResource(R.string.cubiq_realms_friend_server, friend.worldIp),
                                                color = Color(0xFFAAAAAA),
                                                fontFamily = MinecraftFontFamily,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (friend.worldIp.isNotBlank()) {
                                        MinecraftButton(
                                            onClick = {
                                                Toast.makeText(context, context.getString(R.string.cubiq_realms_joining_world, friend.username, friend.worldIp), Toast.LENGTH_SHORT).show()
                                                onLaunchGame(currentVersion)
                                            },
                                            style = MinecraftButtonStyle.GREEN,
                                            text = stringResource(R.string.cubiq_realms_join),
                                            fontSize = 10.sp,
                                            modifier = Modifier.width(60.dp).height(30.dp)
                                        )
                                    } else {
                                        MinecraftButton(
                                            onClick = {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText(context.getString(R.string.cubiq_realms_clip_world_invite), context.getString(R.string.cubiq_realms_world_invite_msg))
                                                cm.setPrimaryClip(clip)
                                                Toast.makeText(context, context.getString(R.string.cubiq_realms_world_invite_copied, friend.username), Toast.LENGTH_SHORT).show()
                                            },
                                            style = MinecraftButtonStyle.STONE,
                                            text = stringResource(R.string.cubiq_realms_invite),
                                            fontSize = 10.sp,
                                            modifier = Modifier.width(62.dp).height(30.dp)
                                        )
                                    }

                                    MinecraftButton(
                                        onClick = {
                                            friends.remove(friend)
                                            saveFriends()
                                            Toast.makeText(context, context.getString(R.string.cubiq_realms_friend_removed, friend.username), Toast.LENGTH_SHORT).show()
                                        },
                                        style = MinecraftButtonStyle.RED,
                                        text = "✖",
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(32.dp).height(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Friend Dialog
        if (showAddFriendDialog) {
            var friendName by remember { mutableStateOf("") }
            var friendIp by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddFriendDialog = false },
                containerColor = Color(0xFF1E1E20),
                title = {
                    Text(
                        text = stringResource(R.string.cubiq_realms_add_minecraft_friend),
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cubiq_realms_friend_username),
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = friendName,
                                onValueChange = { friendName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }

                        Text(
                            text = stringResource(R.string.cubiq_realms_world_ip_optional),
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = friendIp,
                                onValueChange = { friendIp = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }
                    }
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            if (friendName.isNotBlank()) {
                                val newFriend = CubiqFriend(
                                    username = friendName.trim(),
                                    status = if (friendIp.isNotBlank()) "Playing on World" else "Online",
                                    worldIp = friendIp.trim(),
                                    isOnline = true
                                )
                                friends.add(0, newFriend)
                                saveFriends()
                                showAddFriendDialog = false
                                Toast.makeText(context, context.getString(R.string.cubiq_realms_friend_added, friendName.trim()), Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = stringResource(R.string.cubiq_realms_add_friend),
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                },
                dismissButton = {
                    MinecraftButton(
                        onClick = { showAddFriendDialog = false },
                        style = MinecraftButtonStyle.STONE,
                        text = stringResource(R.string.cubiq_realms_cancel),
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                }
            )
        }

        // Add Server Dialog
        if (showAddServerDialog) {
            var srvName by remember { mutableStateOf("") }
            var srvAddr by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddServerDialog = false },
                containerColor = Color(0xFF1E1E20),
                title = {
                    Text(
                        text = stringResource(R.string.cubiq_realms_add_multiplayer),
                        color = Color.White,
                        fontFamily = MinecraftFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cubiq_realms_server_name),
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = srvName,
                                onValueChange = { srvName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }

                        Text(
                            text = stringResource(R.string.cubiq_realms_server_address),
                            color = Color(0xFFAAAAAA),
                            fontFamily = MinecraftFontFamily,
                            fontSize = 10.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF28282A))
                                .border(BorderStroke(1.dp, Color(0xFF383838)), RoundedCornerShape(3.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = srvAddr,
                                onValueChange = { srvAddr = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = MinecraftFontFamily,
                                    fontSize = 12.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF55FF55))
                            )
                        }
                    }
                },
                confirmButton = {
                    MinecraftButton(
                        onClick = {
                            if (srvName.isNotBlank() && srvAddr.isNotBlank()) {
                                servers.add(MinecraftServerEntry(srvName.trim(), srvAddr.trim(), R.string.cubiq_realms_custom_server))
                                showAddServerDialog = false
                                Toast.makeText(context, context.getString(R.string.cubiq_realms_server_added, srvName), Toast.LENGTH_SHORT).show()
                            }
                        },
                        style = MinecraftButtonStyle.GREEN,
                        text = stringResource(R.string.cubiq_realms_add),
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                },
                dismissButton = {
                    MinecraftButton(
                        onClick = { showAddServerDialog = false },
                        style = MinecraftButtonStyle.STONE,
                        text = stringResource(R.string.cubiq_realms_cancel),
                        fontSize = 11.5.sp,
                        modifier = Modifier.height(34.dp)
                    )
                }
            )
        }
    }
}
