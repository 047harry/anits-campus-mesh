package `in`.anits.mesh.routing

import `in`.anits.mesh.data.dao.MessageDao
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.protocol.PacketSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Closed-Loop ACK Return Engine.
 * Generates high-priority delivery receipts when emergency alerts reach the clinic
 * and routes them back to the sender to turn their screen GREEN ("Help is En Route!").
 */
class AckManager(
    private val localNodeId: ByteArray,
    private val messageDao: MessageDao,
    private val queueManager: PriorityQueueManager,
    private val coroutineScope: CoroutineScope
) {
    /**
     * Generates a return ACK packet targeting the original sender node.
     */
    fun sendDeliveryReceipt(originalPacket: MeshPacket, responderNote: String) {
        val ackPayload = responderNote.toByteArray(Charsets.UTF_8)
        val ackPacket = MeshPacket(
            magic = PacketSerializer.MAGIC_BYTES,
            protocolVersion = 1,
            packetType = PacketType.DELIVERY_ACK,
            flags = 0x00,
            initialTtl = 7,
            remainingTtl = 7,
            reserved = 0x00,
            messageUuid = UUID.randomUUID(),
            sourceNodeId = localNodeId,
            destinationNodeId = originalPacket.sourceNodeId, // Return directly to sender
            sequenceNumber = 1,
            timestampEpochMs = System.currentTimeMillis(),
            payloadLength = ackPayload.size.toShort(),
            payload = ackPayload,
            ed25519Signature = ByteArray(64), // Dummy/signed by responder
            crc32Checksum = 0L
        )

        coroutineScope.launch(Dispatchers.IO) {
            // Push ACK to Priority 1 queue
            queueManager.enqueue(ackPacket)
        }
    }

    /**
     * Processes an incoming ACK that has arrived back at the sender phone.
     */
    suspend fun handleIncomingAck(ackPacket: MeshPacket) {
        // Target UUID or sender matches local node
        val responderMsg = String(ackPacket.payload, Charsets.UTF_8)
        // Update database delivery status to ACKNOWLEDGED
    }
}
