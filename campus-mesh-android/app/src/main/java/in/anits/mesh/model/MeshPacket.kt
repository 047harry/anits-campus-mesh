package `in`.anits.mesh.model

import java.util.UUID

/**
 * High-Level Representation of an Autonomous Mesh Packet.
 * Total Minimum Binary Wire Frame = 126 Bytes (58B Header + 0B Payload + 68B Verification).
 */
data class MeshPacket(
    // 58-Byte Fixed Header Fields
    val magic: Short = 0x4D53.toShort(), // 'MS' in Big-Endian
    val protocolVersion: Byte = 0x01.toByte(),
    val packetType: PacketType,
    val flags: Byte = 0x00.toByte(),
    val initialTtl: Byte = 7.toByte(),
    var remainingTtl: Byte = 7.toByte(),
    val reserved: Byte = 0x00.toByte(),
    val messageUuid: UUID,
    val sourceNodeId: ByteArray,      // 16 Bytes
    val destinationNodeId: ByteArray, // 16 Bytes (0xFF..FF for Broadcast)
    val sequenceNumber: Int,          // 32-Bit unsigned counter
    val timestampEpochMs: Long,       // 64-Bit Unix epoch milliseconds
    val payloadLength: Short,         // 16-Bit unsigned length

    // Variable Payload
    val payload: ByteArray,

    // 68-Byte Trailing Verification Block
    val ed25519Signature: ByteArray,  // 64 Bytes raw signature
    val crc32Checksum: Long           // 32-Bit unsigned CRC (stored as Long)
) {
    init {
        require(sourceNodeId.size == 16) { "sourceNodeId must be precisely 16 bytes" }
        require(destinationNodeId.size == 16) { "destinationNodeId must be precisely 16 bytes" }
        require(ed25519Signature.size == 64) { "ed25519Signature must be precisely 64 bytes" }
    }

    val isBroadcast: Boolean
        get() = destinationNodeId.all { it == 0xFF.toByte() }

    val isEncrypted: Boolean
        get() = (flags.toInt() and 0x01) != 0

    val requiresAck: Boolean
        get() = (flags.toInt() and 0x02) != 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MeshPacket
        return messageUuid == other.messageUuid
    }

    override fun hashCode(): Int = messageUuid.hashCode()
}
