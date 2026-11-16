package com.movtery.zalithlauncher.friends

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.SecureRandom
import java.util.UUID

enum class FriendStatus {
    OFFLINE,
    ONLINE,
    HOSTING
}

data class CubiqFriend(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val code: String,
    val status: FriendStatus = FriendStatus.ONLINE,
    val hostAddress: String? = null,
    val hostPort: Int? = null,
    val isPendingRequest: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

data class CubiqChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val senderCode: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean
)

sealed interface CubiqFriendNotification {
    data class WorldHosted(val friend: CubiqFriend) : CubiqFriendNotification
    data class FriendRequest(val friend: CubiqFriend) : CubiqFriendNotification
    data class ChatReceived(val message: CubiqChatMessage) : CubiqFriendNotification
}

object CubiqFriendsManager {
    private const val BROADCAST_PORT = 25564
    private const val NOTIFICATION_CHANNEL_ID = "cubiq_friends_channel"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _friends = MutableStateFlow<List<CubiqFriend>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _messages = MutableStateFlow<List<CubiqChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _notifications = MutableSharedFlow<CubiqFriendNotification>(extraBufferCapacity = 16)
    val notifications = _notifications.asSharedFlow()

    private val _activeWorldHost = MutableStateFlow<CubiqFriend?>(null)
    val activeWorldHost = _activeWorldHost.asStateFlow()

    private val _isHostingMyWorld = MutableStateFlow(false)
    val isHostingMyWorld = _isHostingMyWorld.asStateFlow()
    var myHostPort: Int = 25565
        private set

    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private var isInitialized = false

    var myFriendCode: String = ""
        private set

    var myUsername: String = "Player"
        private set

    fun initialize(context: Context, username: String? = null) {
        if (isInitialized) {
            if (!username.isNullOrBlank()) myUsername = username
            return
        }
        isInitialized = true
        if (!username.isNullOrBlank()) {
            myUsername = username
        } else {
            val savedUser = com.movtery.zalithlauncher.game.account.AccountsManager.currentAccountFlow.value?.username
                ?: com.movtery.zalithlauncher.game.account.AccountsManager.accountsFlow.value.firstOrNull()?.username
            if (!savedUser.isNullOrBlank()) myUsername = savedUser
        }

        // Load or generate persistent friend code CQ-XXXXXX
        myFriendCode = loadOrGenerateFriendCode(context)

        createNotificationChannel(context)
        loadFriendsFromDisk()
        startDiscoveryListener(context)
    }

    private fun loadOrGenerateFriendCode(context: Context): String {
        val prefs = context.getSharedPreferences("cubiq_social_prefs", Context.MODE_PRIVATE)
        var code = prefs.getString("my_friend_code", null)
        if (code.isNullOrBlank()) {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val rand = SecureRandom()
            val sb = StringBuilder("CQ-")
            for (i in 0 until 6) {
                sb.append(chars[rand.nextInt(chars.length)])
            }
            code = sb.toString()
            prefs.edit().putString("my_friend_code", code).apply()
        }
        return code
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Cubiq Friends",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Cubiq Friends world invites and requests"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun getFriendsFile(): File {
        return File(PathManager.DIR_FILES_PRIVATE, "cubiq_friends.json")
    }

    private fun loadFriendsFromDisk() {
        runCatching {
            val file = getFriendsFile()
            if (!file.exists()) {
                // Initialize default sample friend for discovery testing if empty
                val sample = listOf(
                    CubiqFriend(
                        id = "z4hir_exe_sample",
                        name = "z4hir_exe",
                        code = "CQ-JQKFLN",
                        status = FriendStatus.ONLINE,
                        isPendingRequest = false
                    )
                )
                _friends.value = sample
                saveFriendsToDisk()
                return
            }
            val raw = file.readText()
            val array = JSONArray(raw)
            val list = mutableListOf<CubiqFriend>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", "ONLINE")
                val status = runCatching { FriendStatus.valueOf(statusStr) }.getOrDefault(FriendStatus.ONLINE)
                list.add(
                    CubiqFriend(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Friend"),
                        code = obj.optString("code", "CQ-000000"),
                        status = status,
                        hostAddress = obj.optString("hostAddress").takeIf { !it.isNullOrBlank() },
                        hostPort = if (obj.has("hostPort")) obj.optInt("hostPort") else null,
                        isPendingRequest = obj.optBoolean("isPendingRequest", false),
                        lastSeen = obj.optLong("lastSeen", System.currentTimeMillis())
                    )
                )
            }
            _friends.value = list
        }
    }

    private fun saveFriendsToDisk() {
        runCatching {
            val array = JSONArray()
            for (f in _friends.value) {
                val obj = JSONObject().apply {
                    put("id", f.id)
                    put("name", f.name)
                    put("code", f.code)
                    put("status", f.status.name)
                    if (f.hostAddress != null) put("hostAddress", f.hostAddress)
                    if (f.hostPort != null) put("hostPort", f.hostPort)
                    put("isPendingRequest", f.isPendingRequest)
                    put("lastSeen", f.lastSeen)
                }
                array.put(obj)
            }
            val file = getFriendsFile()
            file.parentFile?.mkdirs()
            file.writeText(array.toString(2))
        }
    }

    fun addFriend(codeRaw: String, customName: String? = null, context: Context? = null): Boolean {
        val trimmed = codeRaw.trim().uppercase()
        val formattedCode = if (!trimmed.startsWith("CQ-") && trimmed.length == 6) "CQ-$trimmed" else trimmed
        if (formattedCode == myFriendCode) {
            context?.let { Toast.makeText(it, "Cannot add yourself as a friend", Toast.LENGTH_SHORT).show() }
            return false
        }
        val existing = _friends.value.find { it.code.equals(formattedCode, ignoreCase = true) }
        if (existing != null) {
            context?.let { Toast.makeText(it, "${existing.name} is already in your friends list", Toast.LENGTH_SHORT).show() }
            return false
        }
        val defaultName = customName?.takeIf { it.isNotBlank() } ?: "Player_${formattedCode.takeLast(4)}"
        val newFriend = CubiqFriend(
            name = defaultName,
            code = formattedCode,
            status = FriendStatus.ONLINE,
            isPendingRequest = false
        )
        _friends.update { it + newFriend }
        saveFriendsToDisk()
        context?.let { Toast.makeText(it, "Added friend: $defaultName ($formattedCode)", Toast.LENGTH_SHORT).show() }
        return true
    }

    fun removeFriend(id: String) {
        _friends.update { list -> list.filterNot { it.id == id } }
        saveFriendsToDisk()
    }

    fun acceptFriendRequest(id: String, context: Context? = null) {
        _friends.update { list ->
            list.map { f ->
                if (f.id == id) f.copy(isPendingRequest = false, status = FriendStatus.ONLINE) else f
            }
        }
        saveFriendsToDisk()
        context?.let { Toast.makeText(it, "Friend request accepted!", Toast.LENGTH_SHORT).show() }
    }

    fun declineFriendRequest(id: String) {
        removeFriend(id)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val msg = CubiqChatMessage(
            senderName = myUsername,
            senderCode = myFriendCode,
            message = text,
            isFromMe = true
        )
        _messages.update { it + msg }

        // Broadcast over local mesh/socket to peers
        scope.launch {
            broadcastRaw("CQ_CHAT:${myFriendCode}:${myUsername}:${text}")
        }
    }

    fun hostWorld(port: Int = 25565, context: Context? = null) {
        myHostPort = port
        _isHostingMyWorld.value = true
        broadcastJob?.cancel()
        broadcastJob = scope.launch {
            while (isActive && _isHostingMyWorld.value) {
                broadcastRaw("CQ_HOST:${myFriendCode}:${myUsername}:${port}")
                delay(3000L)
            }
        }
        context?.let {
            Toast.makeText(it, "Hosting world on port $port for Cubiq Friends!", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopHosting(context: Context? = null) {
        _isHostingMyWorld.value = false
        broadcastJob?.cancel()
        broadcastJob = null
        scope.launch {
            broadcastRaw("CQ_STOP_HOST:${myFriendCode}:${myUsername}")
        }
        context?.let {
            Toast.makeText(it, "Stopped hosting world", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun broadcastRaw(payload: String) {
        runCatching {
            val socket = DatagramSocket()
            socket.broadcast = true
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val broadcastAddresses = listOf(
                InetAddress.getByName("255.255.255.255"),
                InetAddress.getByName("127.0.0.1")
            )
            for (addr in broadcastAddresses) {
                val packet = DatagramPacket(bytes, bytes.size, addr, BROADCAST_PORT)
                socket.send(packet)
            }
            socket.close()
        }
    }

    private fun startDiscoveryListener(context: Context) {
        listenJob?.cancel()
        listenJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(BROADCAST_PORT)
                val buffer = ByteArray(2048)
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val rawStr = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    val senderIp = packet.address.hostAddress ?: "127.0.0.1"
                    handleReceivedPacket(context, rawStr, senderIp)
                }
            } catch (t: Throwable) {
                // Ignore socket closed / restart after brief delay
            } finally {
                socket?.close()
            }
        }
    }

    private fun handleReceivedPacket(context: Context, data: String, senderIp: String) {
        val parts = data.split(":")
        if (parts.isEmpty()) return
        val command = parts[0]
        when (command) {
            "CQ_HOST" -> {
                if (parts.size >= 4) {
                    val code = parts[1]
                    val name = parts[2]
                    val port = parts[3].toIntOrNull() ?: 25565
                    if (code == myFriendCode) return // My own packet

                    var matchedFriend = _friends.value.find { it.code.equals(code, ignoreCase = true) }
                    if (matchedFriend == null) {
                        // Discovered player on LAN/Mesh
                        matchedFriend = CubiqFriend(
                            name = name,
                            code = code,
                            status = FriendStatus.HOSTING,
                            hostAddress = senderIp,
                            hostPort = port
                        )
                        _friends.update { it + matchedFriend }
                        saveFriendsToDisk()
                    } else {
                        matchedFriend = matchedFriend.copy(
                            status = FriendStatus.HOSTING,
                            hostAddress = senderIp,
                            hostPort = port,
                            lastSeen = System.currentTimeMillis()
                        )
                        _friends.update { list ->
                            list.map { if (it.id == matchedFriend.id) matchedFriend else it }
                        }
                    }

                    _activeWorldHost.value = matchedFriend
                    _notifications.tryEmit(CubiqFriendNotification.WorldHosted(matchedFriend))
                    showSystemNotification(context, "Cubiq Friends", "${matchedFriend.name} is hosting a world! Tap to join.")
                }
            }
            "CQ_STOP_HOST" -> {
                if (parts.size >= 2) {
                    val code = parts[1]
                    _friends.update { list ->
                        list.map { f ->
                            if (f.code.equals(code, ignoreCase = true)) {
                                f.copy(status = FriendStatus.ONLINE, hostAddress = null, hostPort = null)
                            } else f
                        }
                    }
                    if (_activeWorldHost.value?.code.equals(code, ignoreCase = true)) {
                        _activeWorldHost.value = null
                    }
                }
            }
            "CQ_CHAT" -> {
                if (parts.size >= 4) {
                    val code = parts[1]
                    val name = parts[2]
                    val msgText = parts.subList(3, parts.size).joinToString(":")
                    if (code == myFriendCode) return
                    val chat = CubiqChatMessage(
                        senderName = name,
                        senderCode = code,
                        message = msgText,
                        isFromMe = false
                    )
                    _messages.update { it + chat }
                    _notifications.tryEmit(CubiqFriendNotification.ChatReceived(chat))
                    showSystemNotification(context, "Cubiq Friends: $name", msgText)
                }
            }
        }
    }

    private fun showSystemNotification(context: Context, title: String, content: String) {
        runCatching {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            nm.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    /**
     * Deploys the bundled Cubiq Friends Fabric client mod into the game instance mods directory
     * so that the in-game "Cubiq Friends" button and overlay appear natively inside Minecraft.
     */
    fun installInGameMod(context: Context, gameDir: File, mcVersion: String): Boolean {
        return runCatching {
            val modsDir = File(gameDir, "mods")
            if (!modsDir.exists()) modsDir.mkdirs()

            val assetNames = context.assets.list("components/cs_client") ?: emptyArray()
            val candidateJars = assetNames.filter { it.endsWith(".jar") }
            if (candidateJars.isEmpty()) return false

            // Match best version (e.g. 1.21.11, 26.1, 26.2 or default)
            val selectedJar = candidateJars.firstOrNull { jar ->
                val v = jar.substringAfter("+").substringBefore(".jar")
                mcVersion.contains(v)
            } ?: candidateJars.firstOrNull { it.contains("1.21") } ?: candidateJars.first()

            val targetFile = File(modsDir, selectedJar)
            if (!targetFile.exists() || targetFile.length() == 0L) {
                context.assets.open("components/cs_client/$selectedJar").use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            true
        }.getOrDefault(false)
    }
}
