package `in`.anits.mesh.routing

import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class RoutingDaemonTest {

    private lateinit var queueManager: PriorityQueueManager

    @Before
    fun setup() {
        queueManager = PriorityQueueManager(maxChatCapacity = 10)
    }

    private fun createDummyPacket(type: PacketType, ttl: Byte = 7): MeshPacket {
        return MeshPacket(
            packetType = type,
            messageUuid = UUID.randomUUID(),
            sourceNodeId = ByteArray(16) { 0x01 },
            destinationNodeId = ByteArray(16) { 0xFF.toByte() },
            sequenceNumber = 1,
            timestampEpochMs = System.currentTimeMillis(),
            payloadLength = 4,
            payload = "TEST".toByteArray(),
            ed25519Signature = ByteArray(64),
            crc32Checksum = 0L,
            initialTtl = ttl,
            remainingTtl = ttl
        )
    }

    @Test
    fun testPriorityPreemption() = runBlocking {
        // Enqueue 3 routine chats first
        val chat1 = createDummyPacket(PacketType.ROUTINE_CHAT)
        val chat2 = createDummyPacket(PacketType.ROUTINE_CHAT)
        val chat3 = createDummyPacket(PacketType.ROUTINE_CHAT)

        queueManager.enqueue(chat1)
        queueManager.enqueue(chat2)
        queueManager.enqueue(chat3)

        // Enqueue 1 faculty broadcast (Priority 2)
        val faculty = createDummyPacket(PacketType.FACULTY_BROADCAST)
        queueManager.enqueue(faculty)

        // Enqueue 1 emergency SOS (Priority 1)
        val sos = createDummyPacket(PacketType.EMERGENCY_SOS)
        queueManager.enqueue(sos)

        // Polling MUST return SOS first!
        val firstOut = queueManager.poll()
        assertNotNull(firstOut)
        assertEquals(PacketType.EMERGENCY_SOS, firstOut?.packetType)

        // Second MUST be Faculty Broadcast!
        val secondOut = queueManager.poll()
        assertNotNull(secondOut)
        assertEquals(PacketType.FACULTY_BROADCAST, secondOut?.packetType)

        // Subsequent items are chats
        val thirdOut = queueManager.poll()
        assertEquals(PacketType.ROUTINE_CHAT, thirdOut?.packetType)
    }

    @Test
    fun testChatBufferDropUnderSaturation() = runBlocking {
        // Queue capacity is 10
        for (i in 1..10) {
            val accepted = queueManager.enqueue(createDummyPacket(PacketType.ROUTINE_CHAT))
            assertTrue("Chat packet $i must be accepted", accepted)
        }

        // 11th chat packet must be dropped to protect emergency bandwidth
        val eleventhChat = queueManager.enqueue(createDummyPacket(PacketType.ROUTINE_CHAT))
        assertFalse("11th chat packet must be dropped under buffer saturation", eleventhChat)

        // Emergency SOS must STILL be accepted even when chat buffer is full!
        val sos = queueManager.enqueue(createDummyPacket(PacketType.EMERGENCY_SOS))
        assertTrue("Emergency SOS must never be dropped", sos)
    }
}
