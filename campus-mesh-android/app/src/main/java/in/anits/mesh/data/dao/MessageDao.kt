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

    @Query("SELECT * FROM table_messages WHERE packet_type = 3 ORDER BY created_epoch ASC")
    fun getChatMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM table_messages WHERE packet_type = 1 ORDER BY created_epoch DESC")
    fun getSosMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM table_messages WHERE packet_type = 2 ORDER BY created_epoch DESC")
    fun getAuthorityBulletinsFlow(): Flow<List<MessageEntity>>

    @Query("UPDATE table_messages SET delivery_status = :status WHERE message_uuid = :uuid")
    suspend fun updateDeliveryStatus(uuid: String, status: String)

    @Query("SELECT * FROM table_messages WHERE message_uuid = :uuid LIMIT 1")
    suspend fun getMessageByUuid(uuid: String): MessageEntity?

    @Query("DELETE FROM table_messages WHERE message_uuid LIKE 'init_%' OR payload_bytes LIKE '%stairwell%' OR payload_bytes LIKE '%loud and clear%' OR payload_bytes LIKE '%Welcome to ANITS%'")
    suspend fun purgeBotMessages()

    /**
     * Count unread incoming messages for a specific conversation (dest_id or source_id match).
     * Used for the WhatsApp-style unread badge on inbox rows.
     */
    @Query("""
        SELECT COUNT(*) FROM table_messages
        WHERE packet_type = 3
          AND is_read = 0
          AND (
              (source_id = :conversationId)
              OR (dest_id = :conversationId AND dest_id != 'BROADCAST')
          )
    """)
    fun getUnreadCountFlow(conversationId: String): Flow<Int>

    /**
     * Count unread broadcast messages (dest_id = BROADCAST and is_read = 0).
     */
    @Query("SELECT COUNT(*) FROM table_messages WHERE packet_type = 3 AND dest_id = 'BROADCAST' AND is_read = 0")
    fun getBroadcastUnreadCountFlow(): Flow<Int>

    /**
     * Mark all messages in a conversation as read (both directions).
     */
    @Query("""
        UPDATE table_messages SET is_read = 1
        WHERE packet_type = 3
          AND (source_id = :conversationId OR dest_id = :conversationId)
    """)
    suspend fun markConversationRead(conversationId: String)

    /**
     * Mark all broadcast messages as read.
     */
    @Query("UPDATE table_messages SET is_read = 1 WHERE packet_type = 3 AND dest_id = 'BROADCAST'")
    suspend fun markBroadcastRead()

    /**
     * Update is_read flag for a specific message uuid.
     */
    @Query("UPDATE table_messages SET is_read = 1 WHERE message_uuid = :uuid")
    suspend fun markMessageRead(uuid: String)
}