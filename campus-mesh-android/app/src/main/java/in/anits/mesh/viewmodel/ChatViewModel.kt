package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.protocol.PacketSerializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String,
    val deliveryStatus: String // 'RELAYED', 'DELIVERED'
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage("1", "Rahul (CSE Stairwell)", "Are you stuck on Floor 3? The northern fire stairwell is clear of rubble.", false, "10:18 AM", "DELIVERED"),
            ChatMessage("2", "You", "Yes, 3 of us here in Lab 4. One student has sprained ankle. Moving towards the landing now!", true, "10:19 AM", "RELAYED"),
            ChatMessage("3", "Rahul", "Standing by Floor 2 door with splint and first-aid kit.", false, "10:20 AM", "DELIVERED")
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = "You",
            text = text,
            isOutgoing = true,
            timestamp = "Just now",
            deliveryStatus = "RELAYING"
        )

        _messages.value = _messages.value + newMsg

        viewModelScope.launch {
            // Simulate mesh hop & delivery confirmation
            delay(1200L)
            _messages.value = _messages.value.map {
                if (it.id == newMsg.id) it.copy(deliveryStatus = "DELIVERED") else it
            }

            // Simulate automatic peer reply
            delay(2000L)
            val replyMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = "Rahul",
                text = "Understood! Relaying your location to ground rescue volunteers.",
                isOutgoing = false,
                timestamp = "Just now",
                deliveryStatus = "DELIVERED"
            )
            _messages.value = _messages.value + replyMsg
        }
    }
}
