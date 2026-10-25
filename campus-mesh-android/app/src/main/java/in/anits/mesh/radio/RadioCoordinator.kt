package `in`.anits.mesh.radio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.protocol.PacketSerializer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom

/**
 * Unified Dual-Radio Hardware Transport Coordinator.
 * Manages the energy synergy between passive BLE discovery and high-speed Wi-Fi Aware.
 */
class RadioCoordinator(
    private val context: Context,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val localNodeId: ByteArray = generateOrLoadNodeId()
    private val localRole: UserRole = `in`.anits.mesh.data.UserManager.getProfile(context)?.role ?: UserRole.STUDENT_OCCUPANT

    private val bleScanner: BleScannerDaemon = BleScannerDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        peerDao = database.peerDao(),
        coroutineScope = coroutineScope
    )

    private val bleAdvertiser: BleAdvertiserDaemon = BleAdvertiserDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        localNodeId = localNodeId,
        localRole = localRole,
        coroutineScope = coroutineScope
    )

    private val wifiAwareManager: WifiAwareSessionManager = WifiAwareSessionManager(
        context = context,
        coroutineScope = coroutineScope,
        onPacketReceived = { packet -> processIncomingPacket(packet) }
    )

    private val _isMeshActive = MutableStateFlow(false)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive.asStateFlow()

    fun startMeshServices() {
        if (_isMeshActive.value) return
        _isMeshActive.value = true

        bleScanner.start()
        bleAdvertiser.start()

        if (wifiAwareManager.isSupported) {
            wifiAwareManager.start()
        }
    }

    fun stopMeshServices() {
        if (!_isMeshActive.value) return
        _isMeshActive.value = false

        bleScanner.stop()
        bleAdvertiser.stop()
        wifiAwareManager.stop()
    }

    /**
     * Broadcasts a high-priority Crisis SOS alert.
     * Escalates BLE to continuous low-latency mode and broadcasts SOS mini-beacon.
     */
    fun broadcastSosEmergency(packet: MeshPacket) {
        bleAdvertiser.triggerSosBeacon()
        bleScanner.setCrisisMode(true)

        coroutineScope.launch(Dispatchers.IO) {
            // Store into local message table with Priority 1
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

    private fun processIncomingPacket(packet: MeshPacket) {
        coroutineScope.launch(Dispatchers.IO) {
            val uuidStr = packet.messageUuid.toString()

            // 1. Storm Barrier: Check seen cache
            if (database.seenCacheDao().isSeen(uuidStr)) {
                return@launch // Silently drop duplicate packet in O(1)
            }

            // 2. Mark as seen
            database.seenCacheDao().markSeen(
                SeenCacheEntity(
                    messageUuid = uuidStr,
                    receivedTimestamp = System.currentTimeMillis()
                )
            )

            // 3. Store into local database
            val entity = MessageEntity(
                messageUuid = uuidStr,
                packetType = packet.packetType.code,
                sourceId = packet.sourceNodeId.joinToString("") { "%02x".format(it) },
                destId = packet.destinationNodeId.joinToString("") { "%02x".format(it) },
                payloadBytes = packet.payload,
                isEncrypted = packet.isEncrypted,
                signatureBytes = packet.ed25519Signature,
                hopCount = (packet.initialTtl - packet.remainingTtl) + 1,
                remainingTtl = packet.remainingTtl - 1,
                deliveryStatus = "DELIVERED",
                createdEpoch = packet.timestampEpochMs
            )
            database.messageDao().insertMessage(entity)

            // 4. Relay logic: decrement TTL and forward if TTL > 0
            if (packet.remainingTtl > 1) {
                packet.remainingTtl = (packet.remainingTtl - 1).toByte()
                // Forward via active radio
            }
        }
    }

    private fun generateOrLoadNodeId(): ByteArray {
        val id = ByteArray(16)
        SecureRandom().nextBytes(id)
        return id
    }
}
