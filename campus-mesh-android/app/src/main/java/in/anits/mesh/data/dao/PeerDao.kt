package `in`.anits.mesh.data.dao

import androidx.room.*
import `in`.anits.mesh.data.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPeer(peer: PeerEntity)

    @Query("SELECT * FROM table_peers WHERE last_seen_epoch >= :activeThresholdMs ORDER BY roll_number ASC")
    fun getActivePeersFlow(activeThresholdMs: Long): Flow<List<PeerEntity>>

    @Query("SELECT * FROM table_peers WHERE last_seen_epoch >= :activeThresholdMs ORDER BY roll_number ASC")
    suspend fun getActivePeers(activeThresholdMs: Long = 0L): List<PeerEntity>

    @Query("SELECT * FROM table_peers ORDER BY last_seen_epoch DESC")
    fun getAllPeersFlow(): Flow<List<PeerEntity>>

    @Query("SELECT COUNT(*) FROM table_peers WHERE last_seen_epoch >= :activeThresholdMs")
    fun getActivePeerCountFlow(activeThresholdMs: Long): Flow<Int>

    @Query("DELETE FROM table_peers WHERE last_seen_epoch < :expiryThresholdMs")
    suspend fun pruneStalePeers(expiryThresholdMs: Long)

    @Query("DELETE FROM table_peers WHERE mac_address = :macAddress AND roll_number != :newRollNumber")
    suspend fun deleteOtherPeersWithMac(macAddress: String, newRollNumber: String)

    @Query("DELETE FROM table_peers")
    suspend fun clearAllPeers()

    @Query("DELETE FROM table_peers WHERE roll_number = :rollNumber OR node_id = :rollNumber")
    suspend fun deletePeerByRoll(rollNumber: String)

    @Query("DELETE FROM table_peers WHERE roll_number = 'A23126551000' OR roll_number LIKE '%1000%' OR node_id = 'A23126551000'")
    suspend fun purgeDefaultPlaceholder()
}
