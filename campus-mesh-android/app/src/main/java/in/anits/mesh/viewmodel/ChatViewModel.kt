package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.util.ImageCompressor
import `in`.anits.mesh.data.CampusUserSummary
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.radio.RadioCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    val lastMessageDeliveryStatus: String = "DELIVERED",
    val unreadCount: Int = 0,            // Badge count like WhatsApp green circle
    val lastMessageIsRead: Boolean = true // false = white ticks, true = blue ticks
)

data class MeshChatMessage(
    val id: String,
    val conversationId: String,
    val senderName: String,
    val senderRoll: String,
    val senderRole: UserRole,
    val text: String,
    val imageBase64: String? = null,
    val isOutgoing: Boolean,
    val timestamp: String,
    val deliveryStatus: String, // 'DELIVERED', 'RELAYING'
    val isRead: Boolean = true,
    val hopCount: Int = 1,
    val relayedBy: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MeshDatabase.getDatabase(application)
    private val peerDao = db.peerDao()
    private val messageDao = db.messageDao()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Active open conversation (null = Inbox list view, not null = inside chat room)
    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    // Manually created custom private chats (e.g., student in MECH selected via Directory Picker)
    private val _customConversations = MutableStateFlow<Map<String, ChatConversation>>(emptyMap())

    // Discovered physical peers currently active in BLE range (last 45 seconds sliding window)
    val activePeers: StateFlow<List<PeerEntity>> = peerDao.getAllPeersFlow()
        .map { peers ->
            val activeThreshold = System.currentTimeMillis() - 75_000L
            peers.filter { it.lastSeenEpoch >= activeThreshold && it.rollNumber != "A23126551000" }
        }
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
        messages,
        _customConversations
    ) { peers, directory, msgList, customMap ->
        buildConversationList(peers, directory, msgList, customMap)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // One-time purge of any old mock/bot messages from previous test runs
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageDao.purgeBotMessages()
                peerDao.purgeDefaultPlaceholder()
            } catch (e: Exception) {
                // Safe
            }
            // Auto-pruner loop: evict peers not seen in the last 45 seconds
            while (isActive) {
                try {
                    val threshold = System.currentTimeMillis() - 75_000L
                    peerDao.pruneStalePeers(threshold)
                } catch (e: Exception) {}
                delay(15_000L)
            }
        }
    }

    fun openConversation(conv: ChatConversation) {
        _activeConversation.value = conv
        markConversationRead(conv.conversationId)
        if (conv.rollNumber.isNotBlank() && conv.rollNumber != conv.conversationId) {
            markConversationRead(conv.rollNumber)
        }
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    /**
     * Opens a private 1-on-1 chat targeting any student/faculty across campus blocks
     * by department and roll number.
     */
    fun startNewPrivateChat(department: String, rollNumber: String, customName: String? = null) {
        val cleanRoll = rollNumber.trim().uppercase()
        if (cleanRoll.isBlank()) return

        val summary = UserManager.resolvePeer(cleanRoll, UserRole.STUDENT_OCCUPANT.code)
        val title = customName?.ifBlank { null } ?: summary.fullName.ifBlank { "Student ($cleanRoll)" }

        val newConv = ChatConversation(
            conversationId = cleanRoll,
            title = title,
            rollNumber = cleanRoll,
            role = summary.role,
            department = department.ifBlank { summary.department },
            isBroadcast = false,
            isOnlineInRange = false,
            rssi = 0,
            lastMessageText = "Tap to send encrypted multi-hop message",
            lastMessageTimestamp = "New",
            lastMessageDeliveryStatus = "DELIVERED"
        )

        _customConversations.update { it + (cleanRoll to newConv) }
        _activeConversation.value = newConv
    }

    /**
     * Compresses and transmits a photo across the campus mesh network.
     * Encrypts both caption and photo for private 1-to-1 conversations.
     */
    fun sendPhotoMessage(target: ChatConversation, imageUri: Uri, caption: String = "") {
        val base64 = ImageCompressor.compressUriToBase64(getApplication(), imageUri) ?: return
        val currentProfile = UserManager.getProfile(getApplication())
        val sender = currentProfile?.fullName ?: "You"
        val senderRoll = currentProfile?.rollNumber ?: "A23126551000"
        val senderRole = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val nowEpoch = System.currentTimeMillis()
        val msgUuid = UUID.randomUUID().toString()

        val isBroadcast = target.isBroadcast
        val destRoll = if (isBroadcast) "ALL PEERS" else target.rollNumber

        val (wireText, wireImage) = if (!isBroadcast) {
            val keySeed = CryptoEngine.derivePairwiseKey(senderRoll, target.rollNumber)
            val encText = if (caption.isNotBlank()) CryptoEngine.encryptAesGcm(caption.trim(), keySeed) else ""
            val encImg = CryptoEngine.encryptAesGcm(base64, keySeed)
            Pair(encText, encImg)
        } else {
            Pair(caption.trim(), base64)
        }

        val wireJson = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.ROUTINE_CHAT.code)
            put("text", wireText)
            put("image", wireImage)
            put("mediaType", "IMAGE")
            put("senderName", sender)
            put("senderRoll", senderRoll)
            put("senderRole", senderRole.name)
            put("destRoll", destRoll)
            put("isEncrypted", !isBroadcast)
            put("ttl", 7)
            put("hopCount", 0)
            put("relayedBy", "")
            put("timestampEpoch", nowEpoch)
        }
        val wireJsonStr = wireJson.toString()

        val localJson = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.ROUTINE_CHAT.code)
            put("text", caption.trim())
            put("image", base64)
            put("mediaType", "IMAGE")
            put("senderName", sender)
            put("senderRoll", senderRoll)
            put("senderRole", senderRole.name)
            put("destRoll", destRoll)
            put("isEncrypted", !isBroadcast)
            put("ttl", 7)
            put("hopCount", 0)
            put("relayedBy", "")
            put("timestampEpoch", nowEpoch)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = MessageEntity(
                messageUuid = msgUuid,
                packetType = PacketType.ROUTINE_CHAT.code,
                sourceId = senderRoll,
                destId = if (isBroadcast) "BROADCAST" else target.conversationId,
                payloadBytes = localJson.toString().toByteArray(StandardCharsets.UTF_8),
                isEncrypted = !isBroadcast,
                signatureBytes = ByteArray(64),
                hopCount = 0,
                remainingTtl = 7,
                deliveryStatus = "DELIVERED",
                createdEpoch = nowEpoch
            )
            messageDao.insertMessage(entity)
            RadioCoordinator.instance?.transmitChatMessage(entity, wireJsonStr)
        }
    }

    /**
     * Sends a genuine user message across the multi-hop campus mesh.
     * Encrypts private 1-to-1 chats with AES-256-GCM.
     */
    fun sendMessage(target: ChatConversation, text: String) {
        if (text.isBlank()) return
        val currentProfile = UserManager.getProfile(getApplication())
        val sender = currentProfile?.fullName ?: "You"
        val senderRoll = currentProfile?.rollNumber ?: "A23126551000"
        val senderRole = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val nowEpoch = System.currentTimeMillis()
        val msgUuid = UUID.randomUUID().toString()

        val isBroadcast = target.isBroadcast
        val destRoll = if (isBroadcast) "ALL PEERS" else target.rollNumber

        // Zero-Knowledge Encryption for Private Chats
        val wireText = if (!isBroadcast) {
            val keySeed = CryptoEngine.derivePairwiseKey(senderRoll, target.rollNumber)
            CryptoEngine.encryptAesGcm(text.trim(), keySeed)
        } else {
            text.trim()
        }

        // Outbound Wire JSON (Relay nodes see only this encrypted packet)
        val wireJson = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.ROUTINE_CHAT.code)
            put("text", wireText)
            put("senderName", sender)
            put("senderRoll", senderRoll)
            put("senderRole", senderRole.name)
            put("destRoll", destRoll)
            put("isEncrypted", !isBroadcast)
            put("ttl", 7) // Multi-hop budget to span CSE -> MECH -> Clinic
            put("hopCount", 0)
            put("relayedBy", "")
            put("timestampEpoch", nowEpoch)
        }
        val wireJsonStr = wireJson.toString()

        // Local Entity JSON (stores decrypted text for the local sender)
        val localJson = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.ROUTINE_CHAT.code)
            put("text", text.trim())
            put("senderName", sender)
            put("senderRoll", senderRoll)
            put("senderRole", senderRole.name)
            put("destRoll", destRoll)
            put("isEncrypted", !isBroadcast)
            put("ttl", 7)
            put("hopCount", 0)
            put("relayedBy", "")
            put("timestampEpoch", nowEpoch)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = MessageEntity(
                messageUuid = msgUuid,
                packetType = PacketType.ROUTINE_CHAT.code,
                sourceId = senderRoll,
                destId = if (isBroadcast) "BROADCAST" else target.conversationId,
                payloadBytes = localJson.toString().toByteArray(StandardCharsets.UTF_8),
                isEncrypted = !isBroadcast,
                signatureBytes = ByteArray(64),
                hopCount = 0,
                remainingTtl = 7,
                deliveryStatus = "DELIVERED",
                createdEpoch = nowEpoch
            )
            messageDao.insertMessage(entity)

            // Transmit over real mesh radio and local Wi-Fi / hotspot subnet
            RadioCoordinator.instance?.transmitChatMessage(entity, wireJsonStr)
        }
    }

    /**
     * Called when the user opens a conversation - marks all its messages as read
     * (clears the unread badge and flips ticks to blue).
     */
    fun markConversationRead(conversationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (conversationId == "BROADCAST") {
                messageDao.markBroadcastRead()
            } else {
                messageDao.markConversationRead(conversationId)
            }
        }
    }

    private fun parseChatMessageEntity(entity: MessageEntity, myRoll: String, myNodeId: String): MeshChatMessage? {
        return try {
            val jsonStr = String(entity.payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonStr)
            val text = json.optString("text", "")
            val imageBase64 = json.optString("image", "").ifBlank { null }
            if (text.isBlank() && imageBase64 == null) return null

            val senderName = json.optString("senderName", "User")
            val senderRoll = json.optString("senderRoll", entity.sourceId)
            val roleStr = json.optString("senderRole", UserRole.STUDENT_OCCUPANT.name)
            val senderRole = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT_OCCUPANT }

            if (senderRoll.isNotBlank() && senderName.isNotBlank() && !senderName.equals("User", ignoreCase = true) && !senderName.startsWith("Roll No:", ignoreCase = true)) {
                UserManager.registerKnownPeer(senderRoll, senderName, role = senderRole)
            }
            val destRoll = json.optString("destRoll", entity.destId)

            val hopCount = json.optInt("hopCount", entity.hopCount)
            val relayedBy = json.optString("relayedBy", "")

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
                imageBase64 = imageBase64,
                isOutgoing = isOutgoing,
                timestamp = timeStr,
                deliveryStatus = entity.deliveryStatus,
                isRead = entity.isRead,
                hopCount = if (hopCount == 0 && !isOutgoing) 1 else hopCount,
                relayedBy = relayedBy
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun buildConversationList(
        peers: List<PeerEntity>,
        directory: Map<String, CampusUserSummary>,
        msgList: List<MeshChatMessage>,
        customMap: Map<String, ChatConversation>
    ): List<ChatConversation> {
        val list = mutableListOf<ChatConversation>()
        val myRoll = UserManager.getProfile(getApplication())?.rollNumber ?: ""

        // 1. PINNED: Campus General Broadcast (All Nodes)
        val broadcastMsgs = msgList.filter { it.conversationId == "BROADCAST" }
        val lastBroadcast = broadcastMsgs.lastOrNull()
        val unreadBc = broadcastMsgs.count { !it.isOutgoing && !it.isRead }
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
                lastMessageDeliveryStatus = lastBroadcast?.deliveryStatus ?: "DELIVERED",
            unreadCount = unreadBc,
            lastMessageIsRead = lastBroadcast?.isRead ?: true
            )
        )

        val handledConversations = mutableSetOf<String>()

        // 2. DISCOVERED PEERS (Physical Direct Peers in BLE/Wi-Fi range)
        peers.forEach { peer ->
            val roll = peer.rollNumber?.ifBlank { null } ?: peer.nodeId
            if (roll.equals(myRoll, ignoreCase = true) || roll.equals("A23126551000", ignoreCase = true)) return@forEach

            handledConversations.add(roll)
            val summary = UserManager.resolvePeer(roll, peer.userRole)
            val pMsgs = msgList.filter { it.conversationId == roll || it.conversationId == summary.rollNumber }
            val lastMsg = pMsgs.lastOrNull()

            val unreadCount = pMsgs.count { !it.isOutgoing && !it.isRead }

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
                    lastMessageText = lastMsg?.text ?: "In direct Bluetooth radio range (~3m)",
                    lastMessageTimestamp = lastMsg?.timestamp ?: "Now",
                    lastMessageDeliveryStatus = lastMsg?.deliveryStatus ?: "DELIVERED",
                unreadCount = unreadCount,
                lastMessageIsRead = lastMsg?.isRead ?: true
                )
            )
        }

        // 3. CUSTOM USER-INITIATED MULTI-HOP PRIVATE CHATS
        customMap.values.forEach { customConv ->
            if (!handledConversations.contains(customConv.conversationId)) {
                handledConversations.add(customConv.conversationId)
                val pMsgs = msgList.filter { it.conversationId == customConv.conversationId }
                val lastMsg = pMsgs.lastOrNull()

                val unreadCount = pMsgs.count { !it.isOutgoing && !it.isRead }

                list.add(
                    customConv.copy(
                        lastMessageText = lastMsg?.text ?: customConv.lastMessageText,
                        lastMessageTimestamp = lastMsg?.timestamp ?: customConv.lastMessageTimestamp,
                    unreadCount = unreadCount,
                    lastMessageIsRead = lastMsg?.isRead ?: true
                    )
                )
            }
        }

        // Inactive peers from past logins / out-of-range historical messages are excluded
        // so the inbox only contains currently live mesh peers in range!
        return list
    }
}
