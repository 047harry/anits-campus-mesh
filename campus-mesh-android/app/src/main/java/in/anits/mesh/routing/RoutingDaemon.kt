package `in`.anits.mesh.routing

import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.SeenCacheEntity
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.radio.RadioCoordinator
import kotlinx.coroutines.*
import java.util.Arrays

/**
 * Core Autonomous Mesh Routing Daemon.
 * Implements hop-to-hop forwarding, TTL decrementing, broadcast storm suppression,
 * and delivery dispatch across all campus nodes.
 */
class RoutingDaemon(
    private val localNodeId: ByteArray,
    private val database: MeshDatabase,
    private val queueManager: PriorityQueueManager,
    private val ackManager: AckManager,
    private val radioCoordinator: RadioCoordinator?,
    private val coroutineScope: CoroutineScope
) {
    private var routingJob: Job? = null
    var onEmergencySosReceived: ((MeshPacket) -> Unit)? = null
    var onGoldShieldNoticeReceived: ((MeshPacket) -> Unit)? = null
    var onAckReceived: ((MeshPacket) -> Unit)? = null

    fun start() {
        routingJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                val packet = queueManager.poll()
                if (packet != null) {
                    dispatchOutboundPacket(packet)
                } else {
                    delay(20L) // Yield thread when queue is idle
                }
            }
        }
    }

    fun stop() {
        routingJob?.cancel()
        routingJob = null
    }

    /**
     * Evaluates an incoming packet received from a radio neighbor.
     * Implements the 5-step routing pipeline:
     * 1. Storm Barrier (seen cache check)
     * 2. Destination check (local consumption vs. forward)
     * 3. TTL Decrement & Loop Guard
     * 4. Enqueueing to outbound priority queue
     */
    suspend fun processIncomingPacket(packet: MeshPacket): Boolean = withContext(Dispatchers.IO) {
        val uuidStr = packet.messageUuid.toString()

        // ---------------------------------------------------------
        // STEP 1: STORM BARRIER (Deduplication Check)
        // ---------------------------------------------------------
        if (database.seenCacheDao().isSeen(uuidStr)) {
            return@withContext false // Dropped in O(1) time
        }

        // Mark as seen in ring-buffer cache
        database.seenCacheDao().markSeen(
            SeenCacheEntity(
                messageUuid = uuidStr,
                receivedTimestamp = System.currentTimeMillis()
            )
        )

        val isForMe = Arrays.equals(packet.destinationNodeId, localNodeId)
        val isBroadcast = packet.isBroadcast

        // ---------------------------------------------------------
        // STEP 2: LOCAL CONSUMPTION & DATABASE PERSISTENCE
        // ---------------------------------------------------------
        if (isForMe || isBroadcast) {
            val entity = MessageEntity(
                messageUuid = uuidStr,
                packetType = packet.packetType.code,
                sourceId = packet.sourceNodeId.joinToString("") { "%02x".format(it) },
                destId = packet.destinationNodeId.joinToString("") { "%02x".format(it) },
                payloadBytes = packet.payload,
                isEncrypted = packet.isEncrypted,
                signatureBytes = packet.ed25519Signature,
                hopCount = (packet.initialTtl - packet.remainingTtl) + 1,
                remainingTtl = packet.remainingTtl.toInt(),
                deliveryStatus = "DELIVERED",
                createdEpoch = packet.timestampEpochMs
            )
            database.messageDao().insertMessage(entity)

            // Trigger UI callbacks
            when (packet.packetType) {
                PacketType.EMERGENCY_SOS -> {
                    onEmergencySosReceived?.invoke(packet)
                }
                PacketType.FACULTY_BROADCAST -> {
                    onGoldShieldNoticeReceived?.invoke(packet)
                }
                PacketType.DELIVERY_ACK -> {
                    onAckReceived?.invoke(packet)
                    ackManager.handleIncomingAck(packet)
                }
                else -> {}
            }
        }

        // ---------------------------------------------------------
        // STEP 3: TTL DECREMENT & MULTI-HOP FORWARDING
        // ---------------------------------------------------------
        // If not destined exclusively for me, and TTL > 1, decrement and forward
        if (!isForMe && packet.remainingTtl > 1) {
            val forwardedPacket = packet.copy(
                remainingTtl = (packet.remainingTtl - 1).toByte()
            )
            queueManager.enqueue(forwardedPacket)
            return@withContext true
        }

        return@withContext false
    }

    private suspend fun dispatchOutboundPacket(packet: MeshPacket) = withContext(Dispatchers.IO) {
        // Transmit via RadioCoordinator
        if (packet.packetType == PacketType.EMERGENCY_SOS) {
            radioCoordinator?.broadcastSosEmergency(packet)
        }
    }
}
