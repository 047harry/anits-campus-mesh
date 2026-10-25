package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.data.MeshDatabase
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderRole: UserRole,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val deliveryStatus: String // 'RELAYING', 'DELIVERED'
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MeshDatabase.getDatabase(application)
    val activePeers: StateFlow<List<PeerEntity>> = db.peerDao().getAllPeersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeer = MutableStateFlow<PeerEntity?>(null)
    val selectedPeer: StateFlow<PeerEntity?> = _selectedPeer.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    init {
        // Automatically select first discovered peer if available
        viewModelScope.launch {
            activePeers.collect { peers ->
                if (_selectedPeer.value == null && peers.isNotEmpty()) {
                    _selectedPeer.value = peers.first()
                    initPeerChat(peers.first())
                }
            }
        }
    }

    fun selectPeer(peer: PeerEntity) {
        _selectedPeer.value = peer
        initPeerChat(peer)
    }

    private fun initPeerChat(peer: PeerEntity) {
        val currentProfile = UserManager.getProfile(getApplication())
        val myName = currentProfile?.fullName ?: "You"
        val myRole = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val peerSummary = UserManager.resolvePeer(peer.rollNumber ?: peer.nodeId, peer.userRole)

        // Seed with realistic contextual welcome if empty
        if (_messages.value.isEmpty()) {
            val peerGreeting = when (peerSummary.role) {
                UserRole.HOD, UserRole.PRINCIPAL_DEAN -> 
                    "This is ${peerSummary.fullName} (${peerSummary.role.title}). Keep this channel clear for critical status reports."
                UserRole.CLINIC_SECURITY -> 
                    "Campus Medical Responder online. Report any injured occupants or immediate triage needs."
                else -> 
                    "Direct offline mesh connection established. Are you at a safe floor location?"
            }

            _messages.value = listOf(
                ChatMessage(
                    id = "welcome_1",
                    senderName = peerSummary.fullName,
                    senderRole = peerSummary.role,
                    text = peerGreeting,
                    isOutgoing = false,
                    timestamp = timeFormat.format(Date()),
                    deliveryStatus = "DELIVERED"
                )
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentProfile = UserManager.getProfile(getApplication())
        val sender = currentProfile?.fullName ?: "You"
        val role = currentProfile?.role ?: UserRole.STUDENT_OCCUPANT
        val now = timeFormat.format(Date())

        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = sender,
            senderRole = role,
            text = text.trim(),
            isOutgoing = true,
            timestamp = now,
            deliveryStatus = "RELAYING"
        )

        _messages.value = _messages.value + newMsg

        viewModelScope.launch {
            // Simulate multi-hop confirmation
            delay(1000L)
            _messages.value = _messages.value.map {
                if (it.id == newMsg.id) it.copy(deliveryStatus = "DELIVERED") else it
            }

            // Simulate realistic peer reply if communicating with an authority or responder
            val targetPeer = _selectedPeer.value
            val targetSummary = if (targetPeer != null) {
                UserManager.resolvePeer(targetPeer.rollNumber ?: targetPeer.nodeId, targetPeer.userRole)
            } else null

            delay(2500L)
            val replyText = if (role == UserRole.STUDENT_OCCUPANT) {
                "Acknowledged via 1-hop BLE relay. Responders updated. Stay in current protected corridor."
            } else {
                "Student node standing by. Relaying evacuation telemetry."
            }

            val replyMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = targetSummary?.fullName ?: if (role == UserRole.STUDENT_OCCUPANT) "Dr. K. Venkata Lakshmi (HOD CSD)" else "Student Node (CSD Lab)",
                senderRole = targetSummary?.role ?: if (role == UserRole.STUDENT_OCCUPANT) UserRole.HOD else UserRole.STUDENT_OCCUPANT,
                text = replyText,
                isOutgoing = false,
                timestamp = timeFormat.format(Date()),
                deliveryStatus = "DELIVERED"
            )
            _messages.value = _messages.value + replyMsg
        }
    }
}
