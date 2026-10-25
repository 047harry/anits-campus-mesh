package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.model.MeshPacket
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.protocol.PacketSerializer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class SosState {
    STANDBY,
    COUNTDOWN,
    BROADCASTING,
    CONFIRMED_ACK
}

class SosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusMeshApp).database
    private val messageDao = db.messageDao()

    private val _sosState = MutableStateFlow(SosState.STANDBY)
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(3)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _selectedHazard = MutableStateFlow("Medical Trauma")
    val selectedHazard: StateFlow<String> = _selectedHazard.asStateFlow()

    private val _currentLocation = MutableStateFlow("CSE Block — Floor 3 (Corridor A)")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private var countdownJob: Job? = null

    fun selectHazard(hazard: String) {
        _selectedHazard.value = hazard
    }

    fun startSosCountdown() {
        if (_sosState.value == SosState.CONFIRMED_ACK) {
            // Reset to standby
            _sosState.value = SosState.STANDBY
            return
        }

        _countdownSeconds.value = 3
        _sosState.value = SosState.COUNTDOWN

        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _countdownSeconds.value = i
                delay(1000L)
            }
            // Trigger SOS Broadcast
            broadcastSos()
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _sosState.value = SosState.STANDBY
    }

    private fun broadcastSos() {
        _sosState.value = SosState.BROADCASTING

        viewModelScope.launch {
            val keyPair = CryptoEngine.generateKeyPair()
            val payloadText = "SOS: ${_selectedHazard.value} at ${_currentLocation.value}"
            val payloadBytes = payloadText.toByteArray(Charsets.UTF_8)
            val signature = CryptoEngine.sign(payloadBytes, keyPair.privateKeyBytes)

            val sosPacket = MeshPacket(
                magic = PacketSerializer.MAGIC_BYTES,
                protocolVersion = 1,
                packetType = PacketType.EMERGENCY_SOS,
                flags = 0x02, // Requires ACK
                initialTtl = 7,
                remainingTtl = 7,
                reserved = 0x00,
                messageUuid = UUID.randomUUID(),
                sourceNodeId = ByteArray(16) { 0x01 },
                destinationNodeId = ByteArray(16) { 0xFF.toByte() }, // Broadcast
                sequenceNumber = 1,
                timestampEpochMs = System.currentTimeMillis(),
                payloadLength = payloadBytes.size.toShort(),
                payload = payloadBytes,
                ed25519Signature = signature,
                crc32Checksum = 0L
            )

            // Simulate clinic ACK reception after 1.5 seconds
            delay(1500L)
            _sosState.value = SosState.CONFIRMED_ACK
        }
    }
}
