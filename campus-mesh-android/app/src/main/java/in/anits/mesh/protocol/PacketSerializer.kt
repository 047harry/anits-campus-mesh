package `in`.anits.mesh.protocol

import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.zip.CRC32

/**
 * Strict Big-Endian Packed Binary Serializer and Deserializer.
 * Enforces the exact 70-Byte Header + Variable Payload + 68-Byte Verification Block.
 * Total Minimum Wire Size = 138 Bytes.
 */
object PacketSerializer {

    // 2 (Magic) + 1 (Ver) + 1 (Type) + 1 (Flags) + 1 (InitTTL) + 1 (RemTTL) + 1 (Reserved)
    // + 16 (UUID) + 16 (SrcID) + 16 (DestID) + 4 (SeqNo) + 8 (Timestamp) + 2 (PayloadLen) = 70 Bytes
    const val FIXED_HEADER_SIZE = 70
    const val SIGNATURE_SIZE = 64
    const val CRC32_SIZE = 4
    const val MINIMUM_PACKET_SIZE = FIXED_HEADER_SIZE + SIGNATURE_SIZE + CRC32_SIZE // 138 Bytes
    const val MAGIC_BYTES: Short = 0x4D53.toShort() // 'MS' in Big-Endian

    /**
     * Serializes a MeshPacket into a contiguous raw byte array.
     */
    fun serialize(packet: MeshPacket): ByteArray {
        val payloadLen = packet.payload.size
        val totalSize = FIXED_HEADER_SIZE + payloadLen + SIGNATURE_SIZE + CRC32_SIZE
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.BIG_ENDIAN)

        // 1. Fixed Header (70 Bytes)
        buffer.putShort(packet.magic)
        buffer.put(packet.protocolVersion)
        buffer.put(packet.packetType.code)
        buffer.put(packet.flags)
        buffer.put(packet.initialTtl)
        buffer.put(packet.remainingTtl)
        buffer.put(packet.reserved)

        // Message UUID (16 Bytes: Most Sig Bits + Least Sig Bits)
        buffer.putLong(packet.messageUuid.mostSignificantBits)
        buffer.putLong(packet.messageUuid.leastSignificantBits)

        // Source & Destination Node IDs (16 Bytes each)
        buffer.put(packet.sourceNodeId)
        buffer.put(packet.destinationNodeId)

        // Sequence Number (uint32) & Timestamp (uint64)
        buffer.putInt(packet.sequenceNumber)
        buffer.putLong(packet.timestampEpochMs)

        // Payload Length (uint16)
        buffer.putShort(payloadLen.toShort())

        // 2. Variable Payload
        if (payloadLen > 0) {
            buffer.put(packet.payload)
        }

        // 3. Ed25519 Signature (64 Bytes)
        buffer.put(packet.ed25519Signature)

        // 4. Compute CRC-32 Checksum over all preceding bytes
        val checksumRegion = buffer.array()
        val crc = CRC32()
        crc.update(checksumRegion, 0, FIXED_HEADER_SIZE + payloadLen + SIGNATURE_SIZE)
        val computedCrc = crc.value

        // Append CRC-32 (uint32)
        buffer.putInt(computedCrc.toInt())

        return buffer.array()
    }

    /**
     * Deserializes a raw byte array into a verified MeshPacket.
     * Throws IllegalArgumentException if magic bytes mismatch, size is invalid, or CRC fails.
     */
    fun deserialize(rawBytes: ByteArray): MeshPacket {
        require(rawBytes.size >= MINIMUM_PACKET_SIZE) {
            "Packet size (${rawBytes.size}) is smaller than minimum wire frame ($MINIMUM_PACKET_SIZE)"
        }

        val buffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.BIG_ENDIAN)

        // 1. Verify Magic Bytes
        val magic = buffer.short
        require(magic == MAGIC_BYTES) {
            "Invalid magic bytes: 0x${Integer.toHexString(magic.toInt() and 0xFFFF)}. Expected 0x4D53."
        }

        val version = buffer.get()
        val typeByte = buffer.get()
        val flags = buffer.get()
        val initialTtl = buffer.get()
        val remainingTtl = buffer.get()
        val reserved = buffer.get()

        val uuidMost = buffer.long
        val uuidLeast = buffer.long
        val messageUuid = UUID(uuidMost, uuidLeast)

        val sourceId = ByteArray(16)
        buffer.get(sourceId)

        val destId = ByteArray(16)
        buffer.get(destId)

        val seqNo = buffer.int
        val timestamp = buffer.long
        val payloadLen = buffer.short.toInt() and 0xFFFF

        require(buffer.remaining() == payloadLen + SIGNATURE_SIZE + CRC32_SIZE) {
            "Payload length mismatch: declared $payloadLen, available ${buffer.remaining() - SIGNATURE_SIZE - CRC32_SIZE}"
        }

        val payload = ByteArray(payloadLen)
        if (payloadLen > 0) {
            buffer.get(payload)
        }

        val signature = ByteArray(SIGNATURE_SIZE)
        buffer.get(signature)

        val receivedCrcInt = buffer.int
        val receivedCrc = receivedCrcInt.toLong() and 0xFFFFFFFFL

        // 2. Validate CRC-32 Checksum
        val crc = CRC32()
        crc.update(rawBytes, 0, FIXED_HEADER_SIZE + payloadLen + SIGNATURE_SIZE)
        val expectedCrc = crc.value

        require(receivedCrc == expectedCrc) {
            "CRC-32 Checksum failure: received $receivedCrc, computed $expectedCrc (Corrupt Frame)"
        }

        return MeshPacket(
            magic = magic,
            protocolVersion = version,
            packetType = PacketType.fromByte(typeByte),
            flags = flags,
            initialTtl = initialTtl,
            remainingTtl = remainingTtl,
            reserved = reserved,
            messageUuid = messageUuid,
            sourceNodeId = sourceId,
            destinationNodeId = destId,
            sequenceNumber = seqNo,
            timestampEpochMs = timestamp,
            payloadLength = payloadLen.toShort(),
            payload = payload,
            ed25519Signature = signature,
            crc32Checksum = receivedCrc
        )
    }
}
