package `in`.anits.mesh.data.dao

import androidx.room.*
import `in`.anits.mesh.data.entity.SeenCacheEntity

@Dao
interface SeenCacheDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markSeen(entry: SeenCacheEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM table_seen_cache WHERE message_uuid = :uuid)")
    suspend fun isSeen(uuid: String): Boolean

    @Query("DELETE FROM table_seen_cache WHERE received_timestamp < :purgeThresholdMs")
    suspend fun purgeOldSeenCache(purgeThresholdMs: Long)
}
