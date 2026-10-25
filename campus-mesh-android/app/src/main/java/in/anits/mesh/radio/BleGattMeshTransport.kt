package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.*
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
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Offline Direct Bluetooth LE GATT Mesh Transport with Multi-Hop Relaying.
 */
@SuppressLint("MissingPermission")
class BleGattMeshTransport(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "BleGattMeshTransport"
        val MESH_SERVICE_UUID: UUID = BleScannerDaemon.MESH_SERVICE_UUID
        val MESH_CHAR_UUID: UUID = BleScannerDaemon.MESH_CHAR_UUID
    }

    private var bluetoothGattServer: BluetoothGattServer? = null
    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    fun start() {
        if (bluetoothGattServer != null || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return

        try {
            bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(MESH_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val characteristic = BluetoothGattCharacteristic(
                MESH_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
            )
            service.addCharacteristic(characteristic)
            bluetoothGattServer?.addService(service)
            Log.i(TAG, "✓ BLE GATT Server initialized with Service " + MESH_SERVICE_UUID)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BLE GATT Server: " + e.message)
        }
    }

    fun stop() {
        try {
            bluetoothGattServer?.close()
            bluetoothGattServer = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }

            if (characteristic?.uuid == MESH_CHAR_UUID && value != null) {
                val dataStr = String(value, StandardCharsets.UTF_8)
                coroutineScope.launch(Dispatchers.IO) {
                    processIncomingGattJson(dataStr)
                }
            }
        }
    }

    fun sendDirectGattMessage(peerMac: String, jsonString: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val device = bluetoothAdapter.getRemoteDevice(peerMac) ?: return@launch
                val bytes = jsonString.toByteArray(StandardCharsets.UTF_8)

                var gattClient: BluetoothGatt? = null
                val clientCallback = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt?.requestMtu(512)
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            try { gatt?.close() } catch (e: Exception) {}
                        }
                    }

                    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                        gatt?.discoverServices()
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            val service = gatt?.getService(MESH_SERVICE_UUID)
                            val characteristic = service?.getCharacteristic(MESH_CHAR_UUID)
                            if (characteristic != null) {
                                characteristic.value = bytes
                                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                                gatt.writeCharacteristic(characteristic)
                            }
                        }
                    }

                    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                        try { gatt?.disconnect(); gatt?.close() } catch (e: Exception) {}
                    }
                }

                gattClient = device.connectGatt(context, false, clientCallback, BluetoothDevice.TRANSPORT_LE)
            } catch (e: Exception) {
                // Ignore connection drops
            }
        }
    }

    private suspend fun processIncomingGattJson(jsonStr: String) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonStr)
            val uuid = json.optString("uuid", "")
            if (uuid.isBlank()) return@withContext

            val myProfile = UserManager.getProfile(context)
            val myRoll = myProfile?.rollNumber ?: ""
            val myNodeId = myProfile?.nodeId ?: ""
            val senderRoll = json.optString("senderRoll", "")

            // Loopback prevention
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

            // 1. LOCAL RECEPTION (Destination reached)
            if (isForMe) {
                val packetType = json.optInt("packetType", PacketType.ROUTINE_CHAT.code.toInt()).toByte()
                var rawText = json.optString("text", "")
                val senderName = json.optString("senderName", "Peer")

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
                Log.i(TAG, "✓ [GATT DESTINATION DELIVERED] Message from " + senderName + " (" + senderRoll + "): " + rawText + " [Hops: " + finalHopCount + ", via: " + relayedBy + "]")
            }

            // 2. MULTI-HOP RELAY FORWARDING
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
                Log.i(TAG, "🔀 [GATT MULTI-HOP RELAY] Relaying packet from " + senderRoll + " -> " + destRoll + " via " + myRoll + " (Hop " + nextHop + ", TTL: " + nextTtl + ")")

                // Re-broadcast over UDP
                UdpMeshTransport(context, database, coroutineScope).broadcastJson(relayedJsonStr)
                RadioCoordinator.instance?.forwardRelayedPacket(relayedJsonStr)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming BLE GATT packet: " + e.message)
        }
    }
}
