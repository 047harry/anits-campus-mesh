package `in`.anits.mesh.routing

import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.PriorityBlockingQueue

/**
 * 3-Tier Preemptive Quality of Service (QoS) Queue Manager.
 * Enforces strict prioritization:
 * Tier 1: EMERGENCY_SOS & DELIVERY_ACK (Preempts all)
 * Tier 2: FACULTY_BROADCAST (Gold Shield notices)
 * Tier 3: ROUTINE_CHAT (FIFO, dropped first under buffer pressure)
 */
class PriorityQueueManager(
    private val maxChatCapacity: Int = 500
) {
    data class QueuedItem(
        val packet: MeshPacket,
        val queuedEpochMs: Long = System.currentTimeMillis()
    ) : Comparable<QueuedItem> {
        override fun compareTo(other: QueuedItem): Int {
            // Compare priority first (lower number = higher priority: 1 < 2 < 3)
            val pComp = this.packet.packetType.priority.compareTo(other.packet.packetType.priority)
            if (pComp != 0) return pComp
            // If same priority, FIFO (earlier timestamp first)
            return this.queuedEpochMs.compareTo(other.queuedEpochMs)
        }
    }

    private val queue = PriorityBlockingQueue<QueuedItem>(100)
    private val mutex = Mutex()
    private var chatCount = 0

    suspend fun enqueue(packet: MeshPacket): Boolean = mutex.withLock {
        // Drop protection: if chat queue exceeds limit, drop oldest chat to preserve emergency bandwidth
        if (packet.packetType == PacketType.ROUTINE_CHAT) {
            if (chatCount >= maxChatCapacity) {
                return false // Drop routine chat under buffer saturation
            }
            chatCount++
        }

        queue.offer(QueuedItem(packet))
        return true
    }

    suspend fun poll(): MeshPacket? = mutex.withLock {
        val item = queue.poll() ?: return null
        if (item.packet.packetType == PacketType.ROUTINE_CHAT) {
            chatCount = maxOf(0, chatCount - 1)
        }
        return item.packet
    }

    fun peek(): MeshPacket? = queue.peek()?.packet

    val size: Int
        get() = queue.size

    val isEmpty: Boolean
        get() = queue.isEmpty()

    fun clear() {
        queue.clear()
        chatCount = 0
    }
}
