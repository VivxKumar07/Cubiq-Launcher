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

package com.movtery.zalithlauncher.game.download.jvm_server

import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.MessageDigest

private const val TAG = "JVMSocketServer"

/**
 * [Reference FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCLCore/src/main/java/com/tungsten/fclcore/util/SocketServer.java)
 */
object JVMSocketServer {
    private var scope: CoroutineScope? = null
    private var socket: DatagramSocket? = null

    @Volatile
    var receiveMsg: String? = null
        private set

    /** Binds before returning so the service can safely use the assigned callback port. */
    fun start(
        sessionToken: String,
        ip: String = "127.0.0.1",
        port: Int = 0,
        onReceive: suspend (receiveMsg: String) -> Unit
    ): Int {
        require(sessionToken.isNotBlank()) { "A JVM session token is required" }
        stop()
        receiveMsg = null
        val receiver = DatagramSocket(port, InetAddress.getByName(ip))
        socket = receiver
        val expectedToken = sessionToken.toByteArray(Charsets.UTF_8)
        val receiverScope = CoroutineScope(Dispatchers.IO)
        scope = receiverScope

        receiverScope.launch {
            val bytes = ByteArray(1024)
            val packet = DatagramPacket(bytes, bytes.size)
            try {
                while (true) {
                    ensureActive()
                    // Receiving a short datagram must not truncate subsequent replies.
                    packet.length = bytes.size
                    receiver.receive(packet)
                    val message = String(packet.data, packet.offset, packet.length, Charsets.UTF_8)
                    val separator = message.indexOf(':')
                    if (separator <= 0) continue
                    val token = message.substring(0, separator).toByteArray(Charsets.UTF_8)
                    if (!MessageDigest.isEqual(expectedToken, token)) continue
                    val code = message.substring(separator + 1).toIntOrNull() ?: continue
                    val result = code.toString()
                    receiveMsg = result
                    Logger.info(TAG, "Received JVM exit code $result")
                    onReceive(result)
                    return@launch
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (!receiver.isClosed) Logger.warning(TAG, "JVM callback receiver failed", e)
            } finally {
                receiver.close()
            }
        }
        return receiver.localPort
    }

    fun stop() {
        socket?.close()
        scope?.cancel()
        scope = null
        socket = null
    }
}
