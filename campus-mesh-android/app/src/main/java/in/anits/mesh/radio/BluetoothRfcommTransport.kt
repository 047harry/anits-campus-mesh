package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.PacketType
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Direct Offline Bluetooth RFCOMM Stream Transport.
 * Enables 100% offline peer-to-peer message and photo exchange over physical Bluetooth radio
 * without requiring Wi-Fi, routers, internet, or Bluetooth PIN pairing.
 */
@SuppressLint("MissingPermission")
class BluetoothRfcommTransport(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "BluetoothRfcomm"
        val MESH_RFCOMM_UUID: UUID = UUID.fromString("0000FEF0-0000-1000-8000-00805F9B34FB")
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var isListening = false

    fun start() {
        if (isListening || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        isListening = true

        coroutineScope.launch(Dispatchers.IO) {
            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    "CampusMesh",
                    MESH_RFCOMM_UUID
                )
                Log.i(TAG, "✓ Bluetooth Insecure RFCOMM Server listening on $MESH_RFCOMM_UUID")

                while (isListening && serverSocket != null) {
                    val socket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        null
                    } ?: break

                    launch(Dispatchers.IO) {
                        handleIncomingConnection(socket)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Bluetooth RFCOMM server stopped: ${e.message}")
            }
        }
    }

    fun stop() {
        isListening = false
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {}
    }

    private suspend fun handleIncomingConnection(socket: BluetoothSocket) {
        withContext(Dispatchers.IO) {
            try {
                val input = socket.inputStream
                val buffer = ByteArray(1024 * 64)
                val bytesRead = input.read(buffer)
                if (bytesRead > 0) {
                    val jsonStr = String(buffer, 0, bytesRead, StandardCharsets.UTF_8)
                    processIncomingPacket(jsonStr)
                }
                Unit
            } catch (e: Exception) {
                Log.w(TAG, "Error reading from Bluetooth socket: ${e.message}")
            } finally {
                try { socket.close() } catch (e: Exception) {}
            }
        }
    }

    fun sendDirectMessage(peerMac: String, jsonString: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val device = bluetoothAdapter.getRemoteDevice(peerMac) ?: return@launch
                val socket = device.createInsecureRfcommSocketToServiceRecord(MESH_RFCOMM_UUID)
                socket.connect()
                val out = socket.outputStream
                out.write(jsonString.toByteArray(StandardCharsets.UTF_8))
                out.flush()
                socket.close()
                Log.i(TAG, "✓ Sent packet directly over Bluetooth RFCOMM to $peerMac")
            } catch (e: Exception) {
                Log.w(TAG, "Direct Bluetooth RFCOMM send failed to $peerMac: ${e.message}")
            }
        }
    }

    private suspend fun processIncomingPacket(jsonStr: String) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(jsonStr)
                val uuid = json.optString("uuid", "")
                if (uuid.isBlank()) return@withContext

                val seenDao = database.seenCacheDao()
                if (seenDao.isSeen(uuid)) return@withContext
                seenDao.markSeen(SeenCacheEntity(messageUuid = uuid, receivedTimestamp = System.currentTimeMillis()))

                val myRoll = UserManager.getProfile(context)?.rollNumber ?: ""
                val destRoll = json.optString("destRoll", "")
                val isBroadcast = destRoll == "ALL PEERS" || destRoll == "BROADCAST"
                val isForMe = isBroadcast || (destRoll.isNotBlank() && destRoll.equals(myRoll, ignoreCase = true))

                val remainingTtl = json.optInt("ttl", 7)
                val hopCount = json.optInt("hopCount", 0)

                if (!isForMe && remainingTtl > 1) {
                    val forwardJson = JSONObject(jsonStr)
                    forwardJson.put("ttl", remainingTtl - 1)
                    forwardJson.put("hopCount", hopCount + 1)
                    forwardJson.put("relayedBy", myRoll)
                    val forwardStr = forwardJson.toString()

                    RadioCoordinator.instance?.forwardRelayedPacket(forwardStr)
                    return@withContext
                }

                if (isForMe) {
                    val packetType = json.optInt("packetType", PacketType.ROUTINE_CHAT.code.toInt()).toByte()
                    val isEncrypted = json.optBoolean("isEncrypted", false)
                    val senderRoll = json.optString("senderRoll", "")
                    var rawText = json.optString("text", "")

                    if (isEncrypted && !isBroadcast) {
                        val keySeed = CryptoEngine.derivePairwiseKey(senderRoll, myRoll)
                        if (rawText.isNotBlank()) {
                            rawText = CryptoEngine.decryptAesGcm(rawText, keySeed)
                            json.put("text", rawText)
                        }
                        val encImage = json.optString("image", "")
                        if (encImage.isNotBlank()) {
                            val decImage = CryptoEngine.decryptAesGcm(encImage, keySeed)
                            json.put("image", decImage)
                        }
                    }

                    val finalHopCount = if (hopCount == 0) 1 else (hopCount + 1)
                    json.put("hopCount", finalHopCount)

                    val entity = MessageEntity(
                        messageUuid = uuid,
                        packetType = packetType,
                        sourceId = senderRoll.ifBlank { "peer_node" },
                        destId = if (isBroadcast) "BROADCAST" else destRoll,
                        payloadBytes = json.toString().toByteArray(StandardCharsets.UTF_8),
                        isEncrypted = isEncrypted,
                        signatureBytes = ByteArray(64),
                        hopCount = finalHopCount,
                        remainingTtl = remainingTtl,
                        deliveryStatus = "DELIVERED",
                        createdEpoch = json.optLong("timestampEpoch", System.currentTimeMillis())
                    )
                    database.messageDao().insertMessage(entity)
                    Log.i(TAG, "✓ [BLUETOOTH RFCOMM DELIVERED] Offline message received from $senderRoll")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming RFCOMM JSON: ${e.message}")
            }
        }
    }
}
