package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.CampusUserSummary
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.radio.RadioCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

data class ChatConversation(
    val conversationId: String, // "BROADCAST" or rollNumber
    val title: String,
    val rollNumber: String,
    val role: UserRole,
    val department: String,
    val isBroadcast: Boolean,
    val isOnlineInRange: Boolean,
    val rssi: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTimestamp: String = "",
    val lastMessageDeliveryStatus: String = "DELIVERED"
)

data class MeshChatMessage(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val senderRoll: String,
    val senderRole: UserRole,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val deliveryStatus: String // 'DELIVERED', 'RELAYING'
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MeshDatabase.getDatabase(application)
    private val peerDao = db.peerDao()
    private val messageDao = db.messageDao()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Active open conversation (null = Inbox list view, not null = inside chat room)
    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    // Discovered physical peers in BLE range
    val activePeers: StateFlow<List<PeerEntity>> = peerDao.getActivePeersFlow(0L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe genuine messages from Room database (Single Source of Truth)
    val messages: StateFlow<List<MeshChatMessage>> = messageDao.getChatMessagesFlow()
        .map { entities ->
            val profile = UserManager.getProfile(getApplication())
            val myRoll = profile?.rollNumber ?: ""
            val myNodeId = profile?.nodeId ?: ""
            entities.mapNotNull { parseChatMessageEntity(it, myRoll, myNodeId) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // WhatsApp-style Conversations List (Inbox)
    val conversations: StateFlow<List<ChatConversation>> = combine(
        activePeers,
        UserManager.campusDirectory,
        messages
    ) { peers, directory, msgList ->
        buildConversationList(peers, directory, msgList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // One-time purge of any old mock/bot messages from previous test runs
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageDao.purgeBotMessages()
            } catch (e: Exception) {
                // Safe
            }
        }
    }

    fun openConversation(conv: ChatConversation) {
        _activeConversation.value = conv
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    /**
     * Sends a genuine user message to the destination or broadcast channel.
     * Persists locally to Room and transmits over radio & LAN mesh.
     */
    fun sendMessage(target: ChatConversation, text: String) {
        if (text.isBlank()) return
        val currentProfile = UserManager.getProfile(getApplication())
        val sender = currentProfile?.fullName ?: "You"
        val senderRoll = currentProfile?.rollNumber ?: "A23126551000"
        val senderRole = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val nowEpoch = System.currentTimeMillis()
        val msgUuid = UUID.randomUUID().toString()

        val json = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.ROUTINE_CHAT.code)
            put("text", text.trim())
            put("senderName", sender)
            put("senderRoll", senderRoll)
            put("senderRole", senderRole.name)
            put("destRoll", if (target.isBroadcast) "ALL PEERS" else target.rollNumber)
            put("timestampEpoch", nowEpoch)
        }
        val jsonStr = json.toString()

        viewModelScope.launch(Dispatchers.IO) {
            val entity = MessageEntity(
                messageUuid = msgUuid,
                packetType = PacketType.ROUTINE_CHAT.code,
                sourceId = senderRoll,
                destId = if (target.isBroadcast) "BROADCAST" else target.conversationId,
                payloadBytes = jsonStr.toByteArray(StandardCharsets.UTF_8),
                isEncrypted = false,
                signatureBytes = ByteArray(64),
                hopCount = 0,
                remainingTtl = 5,
                deliveryStatus = "DELIVERED",
                createdEpoch = nowEpoch
            )
            messageDao.insertMessage(entity)

            // Transmit over real mesh radio and local Wi-Fi / hotspot subnet
            RadioCoordinator.instance?.transmitChatMessage(entity, jsonStr)
        }
    }

    private fun parseChatMessageEntity(entity: MessageEntity, myRoll: String, myNodeId: String): MeshChatMessage? {
        return try {
            val jsonStr = String(entity.payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonStr)
            val text = json.optString("text", "")
            if (text.isBlank()) return null

            val senderName = json.optString("senderName", "User")
            val senderRoll = json.optString("senderRoll", entity.sourceId)
            val roleStr = json.optString("senderRole", UserRole.STUDENT_OCCUPANT.name)
            val senderRole = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT_OCCUPANT }
            val destRoll = json.optString("destRoll", entity.destId)

            val isOutgoing = (senderRoll.isNotBlank() && senderRoll.equals(myRoll, ignoreCase = true))
                    || entity.sourceId.equals(myNodeId, ignoreCase = true)

            val conversationId = if (entity.destId == "BROADCAST" || destRoll == "ALL PEERS" || destRoll == "BROADCAST") {
                "BROADCAST"
            } else if (isOutgoing) {
                if (destRoll.isNotBlank()) destRoll else entity.destId
            } else {
                if (senderRoll.isNotBlank()) senderRoll else entity.sourceId
            }

            val timeStr = timeFormat.format(Date(entity.createdEpoch))

            MeshChatMessage(
                id = entity.messageUuid,
                conversationId = conversationId,
                senderName = if (isOutgoing) "You" else senderName,
                senderRoll = senderRoll,
                senderRole = senderRole,
                text = text,
                isOutgoing = isOutgoing,
                timestamp = timeStr,
                deliveryStatus = entity.deliveryStatus
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun buildConversationList(
        peers: List<PeerEntity>,
        directory: Map<String, CampusUserSummary>,
        msgList: List<MeshChatMessage>
    ): List<ChatConversation> {
        val list = mutableListOf<ChatConversation>()
        val myRoll = UserManager.getProfile(getApplication())?.rollNumber ?: ""

        // 1. PINNED: Campus General Broadcast (All Nodes)
        val broadcastMsgs = msgList.filter { it.conversationId == "BROADCAST" }
        val lastBroadcast = broadcastMsgs.lastOrNull()
        list.add(
            ChatConversation(
                conversationId = "BROADCAST",
                title = "Campus General Broadcast",
                rollNumber = "ALL PEERS",
                role = UserRole.SYSTEM_ADMIN,
                department = "CAMPUS-WIDE",
                isBroadcast = true,
                isOnlineInRange = true,
                lastMessageText = lastBroadcast?.text ?: "Broadcast to all reachable student & faculty nodes...",
                lastMessageTimestamp = lastBroadcast?.timestamp ?: "Live",
                lastMessageDeliveryStatus = lastBroadcast?.deliveryStatus ?: "DELIVERED"
            )
        )

        // 2. DISCOVERED PEERS (Real physical peers discovered in the mesh network)
        val handledConversations = mutableSetOf<String>()
        peers.forEach { peer ->
            val roll = peer.rollNumber?.ifBlank { null } ?: peer.nodeId
            if (roll.equals(myRoll, ignoreCase = true)) return@forEach

            handledConversations.add(roll)
            val summary = UserManager.resolvePeer(roll, peer.userRole)
            val pMsgs = msgList.filter { it.conversationId == roll || it.conversationId == summary.rollNumber }
            val lastMsg = pMsgs.lastOrNull()

            list.add(
                ChatConversation(
                    conversationId = summary.rollNumber.ifBlank { roll },
                    title = summary.fullName,
                    rollNumber = summary.rollNumber.ifBlank { roll },
                    role = summary.role,
                    department = summary.department,
                    isBroadcast = false,
                    isOnlineInRange = true,
                    rssi = peer.rssi,
                    lastMessageText = lastMsg?.text ?: "In direct Bluetooth radio range",
                    lastMessageTimestamp = lastMsg?.timestamp ?: "Now",
                    lastMessageDeliveryStatus = lastMsg?.deliveryStatus ?: "DELIVERED"
                )
            )
        }

        // 3. EXISTING CONVERSATIONS WITH MESSAGE HISTORY (When peer moves out of radio range)
        val conversationIdsWithHistory = msgList.map { it.conversationId }.distinct()
        for (convId in conversationIdsWithHistory) {
            if (convId == "BROADCAST" || handledConversations.contains(convId)) continue
            val summary = UserManager.resolvePeer(convId, UserRole.STUDENT_OCCUPANT.code)
            val pMsgs = msgList.filter { it.conversationId == convId }
            val lastMsg = pMsgs.lastOrNull() ?: continue

            list.add(
                ChatConversation(
                    conversationId = convId,
                    title = summary.fullName,
                    rollNumber = summary.rollNumber.ifBlank { convId },
                    role = summary.role,
                    department = summary.department,
                    isBroadcast = false,
                    isOnlineInRange = false,
                    rssi = 0,
                    lastMessageText = lastMsg.text,
                    lastMessageTimestamp = lastMsg.timestamp,
                    lastMessageDeliveryStatus = lastMsg.deliveryStatus
                )
            )
        }

        return list
    }
}
