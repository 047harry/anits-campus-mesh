package `in`.anits.mesh.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Rolling ring-buffer deduplication cache table.
 * If UUID is present, packet is discarded in O(1) time to kill broadcast storms.
 */
@Entity(
    tableName = "table_seen_cache",
    indices = [Index(value = ["received_timestamp"])]
)
data class SeenCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "message_uuid")
    val messageUuid: String,

    @ColumnInfo(name = "received_timestamp")
    val receivedTimestamp: Long
)
