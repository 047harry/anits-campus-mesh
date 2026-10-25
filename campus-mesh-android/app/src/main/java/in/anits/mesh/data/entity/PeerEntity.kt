package `in`.anits.mesh.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Active and ambient neighbors discovered via BLE and Wi-Fi Aware.
 */
@Entity(tableName = "table_peers")
data class PeerEntity(
    @PrimaryKey
    @ColumnInfo(name = "node_id")
    val nodeId: String,

    @ColumnInfo(name = "mac_address")
    val macAddress: String,

    @ColumnInfo(name = "roll_number")
    val rollNumber: String?,

    @ColumnInfo(name = "user_role")
    val userRole: Byte, // 0..5 UserRole code

    @ColumnInfo(name = "is_trusted")
    val isTrusted: Boolean,

    @ColumnInfo(name = "rssi")
    val rssi: Int,

    @ColumnInfo(name = "radio_type")
    val radioType: String, // 'BLE', 'WIFI_AWARE', 'HYBRID'

    @ColumnInfo(name = "last_seen_epoch")
    val lastSeenEpoch: Long
)
