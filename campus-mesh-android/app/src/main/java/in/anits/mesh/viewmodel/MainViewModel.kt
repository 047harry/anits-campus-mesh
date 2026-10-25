package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.radio.MeshRadioService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusMeshApp).database
    private val peerDao = db.peerDao()

    val peerCount: StateFlow<Int> = peerDao.getActivePeerCountFlow(0L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activePeers: StateFlow<List<PeerEntity>> = peerDao.getActivePeersFlow(0L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isBlackoutActive = MutableStateFlow(true)
    val isBlackoutActive: StateFlow<Boolean> = _isBlackoutActive.asStateFlow()

    private val _batteryLevel = MutableStateFlow(84)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _radioLogs = MutableStateFlow(listOf(
        "🟢 BLE 5.0 Advertiser active • ANITS UUID: 0000FEF1",
        "📡 BLE Scanner active • 25% Duty Cycle listening",
        "⚡ Wi-Fi Aware NAN state initialized",
        "🔒 Room SQLite Mesh Database mounted (4 tables)"
    ))
    val radioLogs: StateFlow<List<String>> = _radioLogs.asStateFlow()

    private var heartbeatCounter = 0

    init {
        MeshRadioService.startService(application)
        startHeartbeatTicker()
    }

    private fun startHeartbeatTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(4000L)
                heartbeatCounter++
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val log = "[$time] 📡 BLE Heartbeat #${heartbeatCounter} broadcasted (24B Mini-Beacon)"
                _radioLogs.update { (listOf(log) + it).take(8) }
            }
        }
    }

    fun simulatePeerBeacon() {
        viewModelScope.launch(Dispatchers.IO) {
            val randomSuffix = (1000..9999).random()
            val mockPeer = PeerEntity(
                nodeId = "anits_peer_" + randomSuffix,
                macAddress = "D4:3A:2B:11:" + (10..99).random() + ":FE",
                rollNumber = "A2312655" + randomSuffix,
                userRole = 0x03, // Student Occupant
                isTrusted = true,
                rssi = -(50..75).random(),
                radioType = "BLE 5.0",
                lastSeenEpoch = System.currentTimeMillis()
            )
            peerDao.upsertPeer(mockPeer)
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _radioLogs.update {
                (listOf("[$time] 🟢 Peer Discovered: Node ${mockPeer.nodeId} (${mockPeer.rssi} dBm)") + it).take(8)
            }
        }
    }
}
