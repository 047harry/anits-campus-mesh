package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.PacketType
import kotlinx.coroutines.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Direct Offline Peer-to-Peer BLE GATT Transport.
 * Transmits and receives full JSON packets directly over Bluetooth LE without any Wi-Fi connection.
 */
class BleGattMeshTransport(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "BleGattTransport"
        val MESH_SERVICE_UUID: UUID = UUID.fromString("0000FEF1-0000-1000-8000-00805F9B34FB")
        val MESH_CHAR_UUID: UUID = UUID.fromString("0000FEF2-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothManager: BluetoothManager? = null
    private var gattServer: BluetoothGattServer? = null
    private var isRunning = false

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (responseNeeded && device != null) {
                try {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                } catch (e: Exception) {
                    // Safe
                }
            }
            if (value != null && value.isNotEmpty()) {
                val jsonStr = String(value, StandardCharsets.UTF_8)
                coroutineScope.launch(Dispatchers.IO) {
                    processIncomingJson(jsonStr)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRunning) return
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        if (bluetoothManager == null || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return

        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(MESH_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val char = BluetoothGattCharacteristic(
                MESH_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(char)
            gattServer?.addService(service)
            isRunning = true
            Log.i(TAG, "✓ BLE GATT Server listening on $MESH_SERVICE_UUID")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BLE GATT Server: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        isRunning = false
        try {
            gattServer?.close()
            gattServer = null
        } catch (e: Exception) {
            // Safe
        }
    }

    @SuppressLint("MissingPermission")
    fun sendDirectGattMessage(targetMac: String, jsonString: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (targetMac == "00:00:00:00:00:00" || targetMac.isBlank()) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val device = bluetoothAdapter.getRemoteDevice(targetMac) ?: return@launch
                val bytes = jsonString.toByteArray(StandardCharsets.UTF_8)

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
                        val service = gatt?.getService(MESH_SERVICE_UUID)
                        val char = service?.getCharacteristic(MESH_CHAR_UUID)
                        if (char != null) {
                            char.value = bytes
                            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            gatt.writeCharacteristic(char)
                            Log.i(TAG, "Writing BLE GATT packet (" + bytes.size + " bytes) to " + targetMac)
                        } else {
                            gatt?.disconnect()
                        }
                    }

                    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                        Log.i(TAG, "✓ BLE GATT packet delivered to " + targetMac)
                        gatt?.disconnect()
                    }
                }

                device.connectGatt(context, false, clientCallback, BluetoothDevice.TRANSPORT_LE)
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendDirectGattMessage: " + e.message)
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
            val isForMe = destRoll == "ALL PEERS" || destRoll == "BROADCAST"
                    || destRoll.equals(myRoll, ignoreCase = true)
                    || destRoll.equals(myNodeId, ignoreCase = true)
                    || (myRoll.isNotBlank() && destRoll.contains(myRoll, ignoreCase = true))
                    || (myNodeId.isNotBlank() && destRoll.contains(myNodeId, ignoreCase = true))

            if (!isForMe) return@withContext

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
            Log.i(TAG, "✓ Received genuine mesh message via BLE GATT from " + senderName + " (" + senderRoll + "): " + text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse incoming BLE GATT packet: " + e.message)
        }
    }
}
