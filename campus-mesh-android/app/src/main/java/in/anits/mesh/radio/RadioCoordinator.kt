package `in`.anits.mesh.radio

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.PeerEntity
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
 * Controls real physical BLE scanning and advertising between devices.
 */
class RadioCoordinator(
    private val context: Context,
    private val database: MeshDatabase,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "RadioCoordinator"
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private val _peerEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val peerEvents: SharedFlow<String> = _peerEvents.asSharedFlow()

    private var localNodeIdBytes: ByteArray = getCanonicalNodeId()
    private var localRole: UserRole = UserManager.getProfile(context)?.role ?: UserRole.STUDENT_OCCUPANT

    private val bleScanner: BleScannerDaemon = BleScannerDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        peerDao = database.peerDao(),
        localNodeIdHex = localNodeIdBytes.take(12).toByteArray().joinToString("") { "%02x".format(it) },
        coroutineScope = coroutineScope,
        onPeerDiscovered = { peer, logMessage ->
            _peerEvents.tryEmit(logMessage)
        }
    )

    private val bleAdvertiser: BleAdvertiserDaemon = BleAdvertiserDaemon(
        context = context,
        bluetoothAdapter = bluetoothAdapter,
        localNodeId = localNodeIdBytes,
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

    fun updateProfile() {
        val profile = UserManager.getProfile(context)
        localRole = profile?.role ?: UserRole.STUDENT_OCCUPANT
        localNodeIdBytes = getCanonicalNodeId()

        val idHex = localNodeIdBytes.take(12).toByteArray().joinToString("") { "%02x".format(it) }
        bleScanner.setLocalNodeIdHex(idHex)
        bleAdvertiser.updateIdentity(localNodeIdBytes, localRole)
        Log.i(TAG, "Coordinator updated profile: Role=${localRole.title}, ID=$idHex")
    }

    fun startMeshServices() {
        if (_isMeshActive.value) return
        _isMeshActive.value = true

        updateProfile()
        bleScanner.start()
        bleAdvertiser.start()

        if (wifiAwareManager.isSupported) {
            wifiAwareManager.start()
        }
        Log.i(TAG, "Hardware Mesh Services started successfully")
    }

    fun stopMeshServices() {
        if (!_isMeshActive.value) return
        _isMeshActive.value = false

        bleScanner.stop()
        bleAdvertiser.stop()
        wifiAwareManager.stop()
        Log.i(TAG, "Hardware Mesh Services stopped")
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

    private fun processIncomingPacket(packet: MeshPacket) {
        coroutineScope.launch(Dispatchers.IO) {
            val uuidStr = packet.messageUuid.toString()
            if (database.seenCacheDao().isSeen(uuidStr)) return@launch

            database.seenCacheDao().markSeen(
                SeenCacheEntity(
                    messageUuid = uuidStr,
                    receivedTimestamp = System.currentTimeMillis()
                )
            )

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
        }
    }

    private fun getCanonicalNodeId(): ByteArray {
        val profile = UserManager.getProfile(context)
        val nodeIdStr = profile?.nodeId ?: "node_default00"
        val bytes = ByteArray(16)
        val strBytes = nodeIdStr.toByteArray(StandardCharsets.UTF_8)
        System.arraycopy(strBytes, 0, bytes, 0, minOf(strBytes.size, 16))
        return bytes
    }
}
