package `in`.anits.mesh.radio

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.PacketType
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets

/**
 * Offline Local Wi-Fi / Hotspot LAN UDP Mesh Transport.
 * Broadcasts packets on UDP 38884 with zero internet required.
 */
class UdpMeshTransport(
    private val context: Context,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "UdpMeshTransport"
        const val MESH_UDP_PORT = 38884
    }

    private var socket: DatagramSocket? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isListening = false

    fun start() {
        if (isListening) return
        isListening = true

        coroutineScope.launch(Dispatchers.IO) {
            // 1. Try acquiring multicast lock (non-fatal if rejected)
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                multicastLock = wifiManager?.createMulticastLock("CampusMeshMulticastLock")?.apply {
                    setReferenceCounted(true)
                    acquire()
                }
                Log.i(TAG, "✓ MulticastLock acquired successfully")
            } catch (e: Exception) {
                Log.w(TAG, "MulticastLock note: " + e.message)
            }

            // 2. Open UDP receiver socket
            try {
                socket = DatagramSocket(MESH_UDP_PORT).apply {
                    broadcast = true
                    reuseAddress = true
                }
                Log.i(TAG, "✓ UDP Mesh Transport listening on port " + MESH_UDP_PORT)

                val buffer = ByteArray(4096)
                while (isActive && isListening) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        val dataStr = String(packet.data, 0, packet.length, StandardCharsets.UTF_8)
                        processIncomingJson(dataStr)
                    } catch (e: Exception) {
                        if (!isListening) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDP Socket listen error: " + e.message)
            }
        }
    }

    fun stop() {
        isListening = false
        try {
            socket?.close()
            socket = null
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun broadcastJson(jsonString: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val bytes = jsonString.toByteArray(StandardCharsets.UTF_8)
                val sendSocket = DatagramSocket().apply { broadcast = true }

                val targets = mutableSetOf<InetAddress>()
                try {
                    targets.add(InetAddress.getByName("255.255.255.255"))
                } catch (e: Exception) {
                    // Safe
                }

                // Broadcast on all local network interfaces
                try {
                    val interfaces = NetworkInterface.getNetworkInterfaces()
                    while (interfaces.hasMoreElements()) {
                        val iface = interfaces.nextElement()
                        if (iface.isLoopback || !iface.isUp) continue
                        for (addr in iface.interfaceAddresses) {
                            addr.broadcast?.let { targets.add(it) }
                        }
                    }
                } catch (e: Exception) {
                    // Safe
                }

                for (target in targets) {
                    try {
                        val packet = DatagramPacket(bytes, bytes.size, target, MESH_UDP_PORT)
                        sendSocket.send(packet)
                    } catch (e: Exception) {
                        // Safe continue
                    }
                }
                sendSocket.close()
                Log.i(TAG, "Sent UDP mesh packet (" + bytes.size + " bytes) to " + targets.size + " endpoints")
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting UDP mesh packet: " + e.message)
            }
        }
    }

    private suspend fun processIncomingJson(jsonStr: String) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonStr)
            val uuid = json.optString("uuid", "")
            if (uuid.isBlank()) return@withContext

            val myProfile = UserManager.getProfile(context)
            val myRoll = myProfile?.rollNumber ?: ""
            val myNodeId = myProfile?.nodeId ?: ""
            val senderRoll = json.optString("senderRoll", "")

            // Loopback prevention: Ignore messages sent by this local device
            if (senderRoll.isNotBlank() && (senderRoll.equals(myRoll, ignoreCase = true) || senderRoll.equals(myNodeId, ignoreCase = true))) {
                return@withContext
            }

            // Deduplication barrier
            if (database.seenCacheDao().isSeen(uuid)) {
                return@withContext
            }
            database.seenCacheDao().markSeen(
                SeenCacheEntity(messageUuid = uuid, receivedTimestamp = System.currentTimeMillis())
            )

            val destRoll = json.optString("destRoll", "ALL PEERS")
            // Accept if broadcast or targeted to me
            val isForMe = destRoll == "ALL PEERS" || destRoll == "BROADCAST"
                    || destRoll.equals(myRoll, ignoreCase = true)
                    || destRoll.equals(myNodeId, ignoreCase = true)
                    || (myRoll.isNotBlank() && destRoll.contains(myRoll, ignoreCase = true))
                    || (myNodeId.isNotBlank() && destRoll.contains(myNodeId, ignoreCase = true))

            if (!isForMe) {
                Log.d(TAG, "Packet not for me (dest=" + destRoll + ", myRoll=" + myRoll + ")")
                return@withContext
            }

            val packetType = json.optInt("packetType", PacketType.ROUTINE_CHAT.code.toInt()).toByte()
            val text = json.optString("text", "")
            val senderName = json.optString("senderName", "Peer")

            val entity = MessageEntity(
                messageUuid = uuid,
                packetType = packetType,
                sourceId = senderRoll.ifBlank { "peer_node" },
                destId = if (destRoll == "ALL PEERS" || destRoll == "BROADCAST") "BROADCAST" else destRoll,
                payloadBytes = jsonStr.toByteArray(StandardCharsets.UTF_8),
                isEncrypted = false,
                signatureBytes = ByteArray(64),
                hopCount = 1,
                remainingTtl = 5,
                deliveryStatus = "DELIVERED",
                createdEpoch = json.optLong("timestampEpoch", System.currentTimeMillis())
            )

            database.messageDao().insertMessage(entity)
            Log.i(TAG, "✓ Received genuine mesh message from " + senderName + " (" + senderRoll + "): " + text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming UDP packet: " + e.message)
        }
    }
}
