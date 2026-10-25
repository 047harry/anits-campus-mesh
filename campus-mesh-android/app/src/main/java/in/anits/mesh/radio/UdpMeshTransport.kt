package `in`.anits.mesh.radio

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import `in`.anits.mesh.crypto.CryptoEngine
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
 * Offline Local Wi-Fi / Hotspot LAN UDP Mesh Transport with Autonomous Multi-Hop Hopping.
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

            // Loopback prevention: Ignore packets originating from this device
            if (senderRoll.isNotBlank() && (senderRoll.equals(myRoll, ignoreCase = true) || senderRoll.equals(myNodeId, ignoreCase = true))) {
                return@withContext
            }

            // Deduplication Barrier: Drop packets already seen in the ring-buffer
            if (database.seenCacheDao().isSeen(uuid)) {
                return@withContext
            }
            database.seenCacheDao().markSeen(
                SeenCacheEntity(messageUuid = uuid, receivedTimestamp = System.currentTimeMillis())
            )

            val destRoll = json.optString("destRoll", "ALL PEERS")
            val isBroadcast = destRoll == "ALL PEERS" || destRoll == "BROADCAST"

            val isForMe = isBroadcast
                    || destRoll.equals(myRoll, ignoreCase = true)
                    || destRoll.equals(myNodeId, ignoreCase = true)
                    || (myRoll.isNotBlank() && destRoll.contains(myRoll, ignoreCase = true))
                    || (myNodeId.isNotBlank() && destRoll.contains(myNodeId, ignoreCase = true))

            val ttl = json.optInt("ttl", 5)
            val hopCount = json.optInt("hopCount", 0)
            val relayedBy = json.optString("relayedBy", "")
            val isEncrypted = json.optBoolean("isEncrypted", false)

            // -------------------------------------------------------------
            // 1. LOCAL DESTINATION RECEIPT (Only when meant for me or broadcast)
            // -------------------------------------------------------------
            if (isForMe) {
                val packetType = json.optInt("packetType", PacketType.ROUTINE_CHAT.code.toInt()).toByte()
                var rawText = json.optString("text", "")
                val senderName = json.optString("senderName", "Peer")
                if (senderRoll.isNotBlank() && senderName.isNotBlank() && !senderName.startsWith("Roll No:", ignoreCase = true) && !senderName.equals("User", ignoreCase = true) && !senderName.equals("Peer", ignoreCase = true)) {
                    UserManager.registerKnownPeer(senderRoll, senderName)
                }

                // Decrypt payload if end-to-end encrypted for 1-to-1 private chat
                if (isEncrypted && !isBroadcast) {
                    val keySeed = CryptoEngine.derivePairwiseKey(senderRoll, myRoll)
                    if (rawText.isNotBlank()) {
                        rawText = CryptoEngine.decryptAesGcm(rawText, keySeed)
                        json.put("text", rawText) // Store decrypted content locally
                    }
                    val encImage = json.optString("image", "")
                    if (encImage.isNotBlank()) {
                        val decImage = CryptoEngine.decryptAesGcm(encImage, keySeed)
                        json.put("image", decImage)
                    }
                }

                val finalHopCount = if (hopCount == 0) 1 else (hopCount + 1)
                json.put("hopCount", finalHopCount)
                json.put("relayedBy", relayedBy)

                val entity = MessageEntity(
                    messageUuid = uuid,
                    packetType = packetType,
                    sourceId = senderRoll.ifBlank { "peer_node" },
                    destId = if (isBroadcast) "BROADCAST" else destRoll,
                    payloadBytes = json.toString().toByteArray(StandardCharsets.UTF_8),
                    isEncrypted = isEncrypted,
                    signatureBytes = ByteArray(64),
                    hopCount = finalHopCount,
                    remainingTtl = ttl,
                    deliveryStatus = "DELIVERED",
                    createdEpoch = json.optLong("timestampEpoch", System.currentTimeMillis())
                )

                database.messageDao().insertMessage(entity)
                Log.i(TAG, "✓ [DESTINATION DELIVERED] Message from " + senderName + " (" + senderRoll + "): " + rawText + " [Hops: " + finalHopCount + ", via: " + relayedBy + "]")
            }

            // -------------------------------------------------------------
            // 2. MULTI-HOP AUTONOMOUS RELAY FORWARDING
            // If not exclusively for me (or broadcast across campus), and TTL > 1:
            // Forward packet without revealing content to intermediate nodes!
            // -------------------------------------------------------------
            if ((!isForMe || isBroadcast) && ttl > 1) {
                val nextTtl = ttl - 1
                val nextHop = hopCount + 1
                val relayPath = json.optString("relayPath", "")
                val updatedRelayPath = if (relayPath.isBlank()) myRoll else "$relayPath -> $myRoll"

                json.put("ttl", nextTtl)
                json.put("hopCount", nextHop)
                json.put("relayedBy", myRoll)
                json.put("relayPath", updatedRelayPath)

                val relayedJsonStr = json.toString()
                Log.i(TAG, "🔀 [MULTI-HOP RELAY] Forwarding packet from " + senderRoll + " -> " + destRoll + " via " + myRoll + " (Hop " + nextHop + ", TTL: " + nextTtl + ")")

                // Re-broadcast over LAN UDP
                broadcastJson(relayedJsonStr)

                // Re-forward over Bluetooth LE GATT to other physical peers
                RadioCoordinator.instance?.forwardRelayedPacket(relayedJsonStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming UDP packet: " + e.message)
        }
    }
}
