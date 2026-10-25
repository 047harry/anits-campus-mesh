package `in`.anits.mesh.radio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets

/**
 * Unified Hardware Transport Coordinator.
 * Controls real physical BLE scanning, advertising, BLE GATT messaging, and LAN UDP transport between devices.
 * Supports autonomous multi-hop message relaying across campus blocks.
 */
class RadioCoordinator(
    private val context: Context,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "RadioCoordinator"
        @Volatile
        var instance: RadioCoordinator? = null
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val _peerEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val peerEvents: SharedFlow<String> = _peerEvents.asSharedFlow()

    private var localRole: UserRole = UserManager.getProfile(context)?.role ?: UserRole.STUDENT_OCCUPANT
    private var localRollNumber: String = UserManager.getProfile(context)?.rollNumber ?: "A23126551000"
    private var localName: String = UserManager.getProfile(context)?.fullName ?: "Student"

    private val bleScanner: BleScannerDaemon = BleScannerDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        peerDao = database.peerDao(),
        localRollNumber = localRollNumber,
        coroutineScope = coroutineScope,
        onPeerDiscovered = { peer, logMessage ->
            _peerEvents.tryEmit(logMessage)
        }
    )

    private val bleAdvertiser: BleAdvertiserDaemon = BleAdvertiserDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        localRole = localRole,
        localRollNumber = localRollNumber,
        localName = localName,
        coroutineScope = coroutineScope
    )

    private val udpTransport: UdpMeshTransport = UdpMeshTransport(
        context = context,
        database = database,
        coroutineScope = coroutineScope
    )

    private val bleGattTransport: BleGattMeshTransport = BleGattMeshTransport(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        database = database,
        coroutineScope = coroutineScope
    )

    private val rfcommTransport: BluetoothRfcommTransport = BluetoothRfcommTransport(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        database = database,
        coroutineScope = coroutineScope
    )

    private val _isMeshActive = MutableStateFlow(false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive.asStateFlow()

    init {
        instance = this
    }

    fun updateProfile() {
        val profile = UserManager.getProfile(context)
        localRole = profile?.role ?: UserRole.STUDENT_OCCUPANT
        localRollNumber = profile?.rollNumber ?: "A23126551000"
        localName = profile?.fullName ?: "Student"

        bleScanner.setLocalRollNumber(localRollNumber)
        bleAdvertiser.updateIdentity(localRole, localRollNumber, localName)
        Log.i(TAG, "Coordinator updated profile: Roll=" + localRollNumber + ", Role=" + localRole.title)
    }

    fun startMeshServices() {
        instance = this
        if (_isMeshActive.value) return
        _isMeshActive.value = true

        updateProfile()
        bleScanner.start()
        bleAdvertiser.start()
        udpTransport.start()
        bleGattTransport.start()
        rfcommTransport.start()

        Log.i(TAG, "✓ Hardware Mesh Services (BLE GATT + BLE Beacon + LAN UDP) started successfully")
    }

    fun stopMeshServices() {
        if (!_isMeshActive.value) return
        _isMeshActive.value = false

        bleScanner.stop()
        bleAdvertiser.stop()
        udpTransport.stop()
        bleGattTransport.stop()
        rfcommTransport.stop()
        Log.i(TAG, "Hardware Mesh Services stopped")
    }

    /**
     * Transmits a genuine chat message originating from this device.
     */
    fun transmitChatMessage(entity: MessageEntity, jsonPayload: String) {
        coroutineScope.launch(Dispatchers.IO) {
            // Layer 1: Instant local Wi-Fi / Hotspot LAN UDP Broadcast (< 5ms)
            udpTransport.broadcastJson(jsonPayload)

            // Layer 2: Direct offline Bluetooth RFCOMM & BLE GATT delivery to active peers
            try {
                val peers = database.peerDao().getActivePeers(0L)
                for (peer in peers) {
                    if (peer.macAddress.isNotBlank() && peer.macAddress != "00:00:00:00:00:00") {
                        rfcommTransport.sendDirectMessage(peer.macAddress, jsonPayload)
                        bleGattTransport.sendDirectGattMessage(peer.macAddress, jsonPayload)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Bluetooth dispatch note: " + e.message)
            }
        }
    }

    /**
     * Forwards a relayed packet over BLE GATT to active physical peers.
     */
    fun forwardRelayedPacket(jsonPayload: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val peers = database.peerDao().getActivePeers(0L)
                for (peer in peers) {
                    if (peer.macAddress.isNotBlank() && peer.macAddress != "00:00:00:00:00:00") {
                        rfcommTransport.sendDirectMessage(peer.macAddress, jsonPayload)
                        bleGattTransport.sendDirectGattMessage(peer.macAddress, jsonPayload)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun broadcastSosEmergency(packet: MeshPacket) {
        bleAdvertiser.triggerSosBeacon()
        bleScanner.setCrisisMode(true)

        coroutineScope.launch(Dispatchers.IO) {
            val entity = MessageEntity(
                messageUuid = packet.messageUuid.toString(),
                packetType = PacketType.EMERGENCY_SOS.code,
                sourceId = packet.sourceNodeId.joinToString("") { "%02x".format(it) },
                destId = packet.destinationNodeId.joinToString("") { "%02x".format(it) },
                payloadBytes = packet.payload,
                isEncrypted = packet.isEncrypted,
                signatureBytes = packet.ed25519Signature,
                hopCount = 0,
                remainingTtl = packet.remainingTtl.toInt(),
                deliveryStatus = "RELAYED",
                createdEpoch = packet.timestampEpochMs
            )
            database.messageDao().insertMessage(entity)
        }
    }
}
