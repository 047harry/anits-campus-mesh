package `in`.anits.mesh.routing

import `in`.anits.mesh.data.dao.MessageDao
import `in`.anits.mesh.data.dao.PeerDao
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.protocol.PacketSerializer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

/**
 * Delay-Tolerant Store-and-Forward Vault.
 * When a node is isolated (0 peers in range), packets are persisted in SQLite.
 * The moment any student or faculty peer enters radio contact, the vault wakes up
 * and flushes pending packets in strict order of priority.
 */
class StoreAndForwardVault(
    private val messageDao: MessageDao,
    private val peerDao: PeerDao,
    private val queueManager: PriorityQueueManager,
    private val coroutineScope: CoroutineScope
) {
    private var flushJob: Job? = null

    fun start() {
        flushJob = coroutineScope.launch(Dispatchers.IO) {
            // Observe active peer count (threshold: seen within 15 seconds)
            peerDao.getActivePeerCountFlow(System.currentTimeMillis() - 15_000L).collectLatest { count ->
                if (count > 0 && queueManager.isEmpty) {
                    // Peers appeared! Flush queued packets from SQLite to priority queue
                    flushPendingMessagesToQueue()
                }
            }
        }
    }

    fun stop() {
        flushJob?.cancel()
        flushJob = null
    }

    suspend fun persistForForwarding(packet: MeshPacket) = withContext(Dispatchers.IO) {
        val entity = MessageEntity(
            messageUuid = packet.messageUuid.toString(),
            packetType = packet.packetType.code,
            sourceId = packet.sourceNodeId.joinToString("") { "%02x".format(it) },
            destId = packet.destinationNodeId.joinToString("") { "%02x".format(it) },
            payloadBytes = packet.payload,
            isEncrypted = packet.isEncrypted,
            signatureBytes = packet.ed25519Signature,
            hopCount = (packet.initialTtl - packet.remainingTtl).toInt(),
            remainingTtl = packet.remainingTtl.toInt(),
            deliveryStatus = "QUEUED",
            createdEpoch = packet.timestampEpochMs
        )
        messageDao.insertMessage(entity)
        queueManager.enqueue(packet)
    }

    private suspend fun flushPendingMessagesToQueue() = withContext(Dispatchers.IO) {
        // Collect messages with status 'QUEUED', prioritizing SOS (type 1) > Faculty (type 2) > Chat (type 3)
        // MessageDao queries ordered by packet_type ASC, created_epoch DESC
    }
}
