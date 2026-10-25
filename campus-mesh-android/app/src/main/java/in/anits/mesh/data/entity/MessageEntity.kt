package `in`.anits.mesh.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores local and relayed messages.
 * Maps directly to table_messages from STAGE_2_ARCHITECTURE.md Section 2.4.
 */
@Entity(
    tableName = "table_messages",
    indices = [
        Index(value = ["packet_type", "delivery_status"]),
        Index(value = ["created_epoch"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "message_uuid")
    val messageUuid: String,

    @ColumnInfo(name = "packet_type")
    val packetType: Byte, // 1=SOS, 2=Faculty, 3=Chat, 4=ACK

    @ColumnInfo(name = "source_id")
    val sourceId: String,

    @ColumnInfo(name = "dest_id")
    val destId: String,

    @ColumnInfo(name = "payload_bytes")
    val payloadBytes: ByteArray,

    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean,

    @ColumnInfo(name = "signature_bytes")
    val signatureBytes: ByteArray,

    @ColumnInfo(name = "hop_count")
    val hopCount: Int,

    @ColumnInfo(name = "remaining_ttl")
    val remainingTtl: Int,

    @ColumnInfo(name = "delivery_status")
    val deliveryStatus: String, // 'QUEUED', 'RELAYED', 'ACKNOWLEDGED'

    @ColumnInfo(name = "is_read", defaultValue = "0")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_epoch")
    val createdEpoch: Long
)
