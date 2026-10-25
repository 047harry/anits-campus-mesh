package `in`.anits.mesh.data.dao

import androidx.room.*
import `in`.anits.mesh.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM table_messages ORDER BY packet_type ASC, created_epoch DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM table_messages WHERE packet_type = 1 ORDER BY created_epoch DESC")
    fun getSosMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM table_messages WHERE packet_type = 2 ORDER BY created_epoch DESC")
    fun getAuthorityBulletinsFlow(): Flow<List<MessageEntity>>

    @Query("UPDATE table_messages SET delivery_status = :status WHERE message_uuid = :uuid")
    suspend fun updateDeliveryStatus(uuid: String, status: String)

    @Query("SELECT * FROM table_messages WHERE message_uuid = :uuid LIMIT 1")
    suspend fun getMessageByUuid(uuid: String): MessageEntity?
}
