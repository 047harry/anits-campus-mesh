package `in`.anits.mesh.data.dao

import androidx.room.*
import `in`.anits.mesh.data.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeer(peer: PeerEntity)

    @Query("SELECT * FROM table_peers WHERE last_seen_epoch >= :activeThresholdMs ORDER BY rssi DESC")
    fun getActivePeersFlow(activeThresholdMs: Long): Flow<List<PeerEntity>>

    @Query("SELECT COUNT(*) FROM table_peers WHERE last_seen_epoch >= :activeThresholdMs")
    fun getActivePeerCountFlow(activeThresholdMs: Long): Flow<Int>

    @Query("DELETE FROM table_peers WHERE last_seen_epoch < :expiryThresholdMs")
    suspend fun pruneStalePeers(expiryThresholdMs: Long)
}
