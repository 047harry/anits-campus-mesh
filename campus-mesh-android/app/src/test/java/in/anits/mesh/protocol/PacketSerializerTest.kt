package `in`.anits.mesh.protocol

import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class PacketSerializerTest {

    @Test
    fun testSerializationAndDeserializationFidelity() {
        val keyPair = CryptoEngine.generateKeyPair()

        val payloadString = "SOS: CSE Block Floor 3, Medical Triage Needed"
        val payloadBytes = payloadString.toByteArray(Charsets.UTF_8)

        val srcId = ByteArray(16) { 0x01 }
        val destBroadcast = ByteArray(16) { 0xFF.toByte() }

        val signature = CryptoEngine.sign(payloadBytes, keyPair.privateKeyBytes)
        assertEquals(64, signature.size)

        val originalPacket = MeshPacket(
            magic = PacketSerializer.MAGIC_BYTES,
            protocolVersion = 1,
            packetType = PacketType.EMERGENCY_SOS,
            flags = 0x02,
            initialTtl = 7,
            remainingTtl = 7,
            reserved = 0x00,
            messageUuid = UUID.randomUUID(),
            sourceNodeId = srcId,
            destinationNodeId = destBroadcast,
            sequenceNumber = 42,
            timestampEpochMs = System.currentTimeMillis(),
            payloadLength = payloadBytes.size.toShort(),
            payload = payloadBytes,
            ed25519Signature = signature,
            crc32Checksum = 0L
        )

        val rawWireBytes = PacketSerializer.serialize(originalPacket)

        // 70 (Fixed Header) + payloadBytes.size + 64 (Sig) + 4 (CRC)
        val expectedSize = PacketSerializer.FIXED_HEADER_SIZE + payloadBytes.size + 64 + 4
        assertEquals(expectedSize, rawWireBytes.size)

        val decodedPacket = PacketSerializer.deserialize(rawWireBytes)

        assertEquals(originalPacket.magic, decodedPacket.magic)
        assertEquals(originalPacket.packetType, decodedPacket.packetType)
        assertEquals(originalPacket.messageUuid, decodedPacket.messageUuid)
        assertEquals(originalPacket.sequenceNumber, decodedPacket.sequenceNumber)
        assertArrayEquals(originalPacket.sourceNodeId, decodedPacket.sourceNodeId)
        assertArrayEquals(originalPacket.destinationNodeId, decodedPacket.destinationNodeId)
        assertArrayEquals(originalPacket.payload, decodedPacket.payload)
        assertArrayEquals(originalPacket.ed25519Signature, decodedPacket.ed25519Signature)

        val isSignatureValid = CryptoEngine.verify(
            decodedPacket.payload,
            decodedPacket.ed25519Signature,
            keyPair.publicKeyBytes
        )
        assertTrue("Ed25519 signature must verify offline successfully", isSignatureValid)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCorruptPacketFailsCrcCheck() {
        val keyPair = CryptoEngine.generateKeyPair()
        val payload = "Normal message".toByteArray()
        val signature = CryptoEngine.sign(payload, keyPair.privateKeyBytes)

        val packet = MeshPacket(
            packetType = PacketType.ROUTINE_CHAT,
            messageUuid = UUID.randomUUID(),
            sourceNodeId = ByteArray(16),
            destinationNodeId = ByteArray(16) { 0xFF.toByte() },
            sequenceNumber = 1,
            timestampEpochMs = System.currentTimeMillis(),
            payloadLength = payload.size.toShort(),
            payload = payload,
            ed25519Signature = signature,
            crc32Checksum = 0L
        )

        val rawBytes = PacketSerializer.serialize(packet)

        // Corrupt 1 byte in payload
        rawBytes[72] = (rawBytes[72].toInt() xor 0xFF).toByte()

        PacketSerializer.deserialize(rawBytes)
    }
}
