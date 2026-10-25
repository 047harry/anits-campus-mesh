package `in`.anits.mesh.data.dao

import androidx.room.*
import `in`.anits.mesh.data.entity.KeyRingEntity

@Dao
interface KeyRingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyRing(keys: List<KeyRingEntity>)

    @Query("SELECT * FROM table_keyring WHERE roll_number = :rollNo LIMIT 1")
    suspend fun getKeyByRollNumber(rollNo: String): KeyRingEntity?

    @Query("SELECT * FROM table_keyring WHERE ed25519_public_key = :publicKeyBytes LIMIT 1")
    suspend fun getKeyByPublicKey(publicKeyBytes: ByteArray): KeyRingEntity?
}
