package `in`.anits.mesh.model

/**
 * 8-Bit Packet Type and Quality-of-Service (QoS) Level.
 * Strictly mirrors STAGE_2_ARCHITECTURE.md Section 2.1.
 */
enum class PacketType(val code: Byte, val priority: Int) {
    EMERGENCY_SOS(0x01.toByte(), priority = 1),       // Priority 1: Instant broadcast; preempts all queues
    FACULTY_BROADCAST(0x02.toByte(), priority = 2),   // Priority 2: Institutional directive (Gold Shield)
    ROUTINE_CHAT(0x03.toByte(), priority = 3),        // Priority 3: P2P text / coordination
    DELIVERY_ACK(0x04.toByte(), priority = 1),        // Closed-loop receipt from clinic/authority
    HEARTBEAT_DISCOVERY(0x05.toByte(), priority = 4); // Ambient neighbor sensing

    companion object {
        fun fromByte(code: Byte): PacketType =
            entries.firstOrNull { it.code == code } ?: ROUTINE_CHAT
    }
}
