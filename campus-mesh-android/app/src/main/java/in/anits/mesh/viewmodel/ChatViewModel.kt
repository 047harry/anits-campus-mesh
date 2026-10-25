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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

data class ChatConversation(
    val conversationId: String, // "BROADCAST" or rollNumber / nodeId
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
    val deliveryStatus: String // 'RELAYING', 'DELIVERED'
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

    // All local in-memory messages mapped by conversationId
    private val _messages = MutableStateFlow<List<MeshChatMessage>>(emptyList())
    val messages: StateFlow<List<MeshChatMessage>> = _messages.asStateFlow()

    // WhatsApp-style Conversations List (Inbox)
    val conversations: StateFlow<List<ChatConversation>> = combine(
        activePeers,
        UserManager.campusDirectory,
        _messages
    ) { peers, directory, msgList ->
        buildConversationList(peers, directory, msgList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedInitialWelcomeIfEmpty()
    }

    fun openConversation(conv: ChatConversation) {
        _activeConversation.value = conv
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    fun sendMessage(target: ChatConversation, text: String) {
        if (text.isBlank()) return
        val currentProfile = UserManager.getProfile(getApplication())
        val sender = currentProfile?.fullName ?: "You"
        val senderRoll = currentProfile?.rollNumber ?: "A23126551000"
        val senderRole = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val now = timeFormat.format(Date())
        val msgUuid = UUID.randomUUID().toString()

        val newMsg = MeshChatMessage(
            id = msgUuid,
            conversationId = target.conversationId,
            senderName = sender,
            senderRoll = senderRoll,
            senderRole = senderRole,
            text = text.trim(),
            isOutgoing = true,
            timestamp = now,
            deliveryStatus = "RELAYING"
        )

        _messages.update { it + newMsg }

        // Persist to Room SQLite database
        viewModelScope.launch(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("text", text.trim())
                put("senderName", sender)
                put("senderRoll", senderRoll)
                put("senderRole", senderRole.name)
                put("destRoll", target.rollNumber)
            }

            val entity = MessageEntity(
                messageUuid = msgUuid,
                packetType = PacketType.ROUTINE_CHAT.code,
                sourceId = currentProfile?.nodeId ?: "node_local",
                destId = if (target.isBroadcast) "BROADCAST" else target.conversationId,
                payloadBytes = json.toString().toByteArray(StandardCharsets.UTF_8),
                isEncrypted = !target.isBroadcast,
                signatureBytes = ByteArray(64),
                hopCount = 0,
                remainingTtl = 5,
                deliveryStatus = "DELIVERED",
                createdEpoch = System.currentTimeMillis()
            )
            messageDao.insertMessage(entity)

            // Simulate multi-hop acknowledgment tick transition: RELAYING -> DELIVERED
            delay(800L)
            _messages.update { list ->
                list.map { if (it.id == msgUuid) it.copy(deliveryStatus = "DELIVERED") else it }
            }

            // If in 1-to-1 chat or General Chat, trigger contextual reply if communicating
            delay(2200L)
            generatePeerReply(target, currentProfile?.role ?: UserRole.STUDENT_OCCUPANT)
        }
    }

    private fun generatePeerReply(target: ChatConversation, myRole: UserRole) {
        val now = timeFormat.format(Date())
        val replyText: String
        val senderName: String
        val senderRoll: String
        val senderRole: UserRole

        if (target.isBroadcast) {
            if (myRole == UserRole.STUDENT_OCCUPANT) {
                senderName = "Dr. K. Venkata Lakshmi (HOD)"
                senderRoll = "FAC-CSD-01"
                senderRole = UserRole.HOD
                replyText = "All CSD students acknowledge. Proceed to north stairwell in orderly fashion."
            } else {
                senderName = "Rahul Sharma"
                senderRoll = "A23126551124"
                senderRole = UserRole.STUDENT_OCCUPANT
                replyText = "Received campus directive on Floor 3. 12 students moving safely."
            }
        } else {
            senderName = target.title
            senderRoll = target.rollNumber
            senderRole = target.role
            replyText = when (target.role) {
                UserRole.HOD, UserRole.PRINCIPAL_DEAN -> 
                    "Direct offline message received via 1-hop BLE. Stay stationed in your current block."
                UserRole.CLINIC_SECURITY -> 
                    "Campus Medical Responder standing by. Please share your current floor room number."
                else -> 
                    "Got your mesh message loud and clear! Radio relay is holding strong."
            }
        }

        val replyMsg = MeshChatMessage(
            id = UUID.randomUUID().toString(),
            conversationId = target.conversationId,
            senderName = senderName,
            senderRoll = senderRoll,
            senderRole = senderRole,
            text = replyText,
            isOutgoing = false,
            timestamp = now,
            deliveryStatus = "DELIVERED"
        )
        _messages.update { it + replyMsg }
    }

    private fun buildConversationList(
        peers: List<PeerEntity>,
        directory: Map<String, CampusUserSummary>,
        msgList: List<MeshChatMessage>
    ): List<ChatConversation> {
        val list = mutableListOf<ChatConversation>()

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

        // 2. DISCOVERED PEERS (Real physical peers in range)
        val peerIdsAdded = mutableSetOf<String>()
        peers.forEach { peer ->
            val summary = UserManager.resolvePeer(peer.rollNumber ?: peer.nodeId, peer.userRole)
            val convId = summary.rollNumber.ifBlank { peer.nodeId }
            peerIdsAdded.add(convId)
            peerIdsAdded.add(peer.nodeId)

            val pMsgs = msgList.filter { it.conversationId == convId || it.conversationId == peer.nodeId }
            val lastMsg = pMsgs.lastOrNull()

            list.add(
                ChatConversation(
                    conversationId = convId,
                    title = summary.fullName,
                    rollNumber = summary.rollNumber,
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

        // 3. CAMPUS ROSTER (Known registered users from Supabase directory)
        val knownRoster = listOf(
            Triple("FAC-CSD-01", "Dr. K. Venkata Lakshmi", UserRole.HOD),
            Triple("FAC-ADMIN-01", "Dr. K. Rama Krishna", UserRole.PRINCIPAL_DEAN),
            Triple("FAC-CLINIC-01", "Dr. B. Adinarayana", UserRole.CLINIC_SECURITY),
            Triple("FAC-WARDEN-01", "Prof. S. Ratan Kumar", UserRole.FACULTY_WARDEN),
            Triple("A23126551000", "P. Darakesu Hemanth", UserRole.STUDENT_OCCUPANT)
        )

        val myRoll = UserManager.getProfile(getApplication())?.rollNumber ?: ""

        knownRoster.forEach { (roll, name, role) ->
            if (roll != myRoll && !peerIdsAdded.contains(roll)) {
                val dMsgs = msgList.filter { it.conversationId == roll }
                val lastMsg = dMsgs.lastOrNull()

                list.add(
                    ChatConversation(
                        conversationId = roll,
                        title = name,
                        rollNumber = roll,
                        role = role,
                        department = if (role == UserRole.CLINIC_SECURITY) "Health Center" else "CSD",
                        isBroadcast = false,
                        isOnlineInRange = false,
                        rssi = 0,
                        lastMessageText = lastMsg?.text ?: "Tap to message via Store-and-Forward DTN",
                        lastMessageTimestamp = lastMsg?.timestamp ?: "",
                        lastMessageDeliveryStatus = lastMsg?.deliveryStatus ?: "DELIVERED"
                    )
                )
            }
        }

        return list
    }

    private fun seedInitialWelcomeIfEmpty() {
        if (_messages.value.isEmpty()) {
            val now = timeFormat.format(Date())
            _messages.value = listOf(
                MeshChatMessage(
                    id = "init_broadcast",
                    conversationId = "BROADCAST",
                    senderName = "Dr. K. Venkata Lakshmi (HOD)",
                    senderRoll = "FAC-CSD-01",
                    senderRole = UserRole.HOD,
                    text = "Welcome to ANITS Offline Campus Mesh. This channel broadcasts to all students and staff within radio range.",
                    isOutgoing = false,
                    timestamp = now,
                    deliveryStatus = "DELIVERED"
                )
            )
        }
    }
}
