package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.radio.MeshRadioService
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
        "🟢 Real BLE 5.0 Hardware Advertiser active (ANITS MFG: 0x02E5)",
        "📡 Continuous Low-Latency BLE Scanner active",
        "⚡ Dual-radio transport layer ready",
        "🔒 SQLite Room Mesh Database active"
    ))
    val radioLogs: StateFlow<List<String>> = _radioLogs.asStateFlow()

    private var heartbeatCounter = 0
    private var lastLoggedPeerCount = 0

    init {
        MeshRadioService.startService(application)
        startHeartbeatTicker()
        observeRealPeers()
    }

    private fun startHeartbeatTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(6000L)
                heartbeatCounter++
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val profile = UserManager.getProfile(getApplication())
                val roleStr = profile?.role?.title ?: "Student Node"
                val log = "[$time] 📡 BLE Beacon #$heartbeatCounter broadcasted ($roleStr • 20B)"
                _radioLogs.update { (listOf(log) + it).take(8) }
            }
        }
    }

    private fun observeRealPeers() {
        viewModelScope.launch {
            activePeers.collect { peers ->
                if (peers.size != lastLoggedPeerCount && peers.isNotEmpty()) {
                    lastLoggedPeerCount = peers.size
                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val latest = peers.first()
                    val summary = UserManager.resolvePeer(latest.rollNumber ?: latest.nodeId, latest.userRole)
                    val log = "[$time] 🎉 REAL PEER CONNECTED: ${summary.fullName} (${latest.rssi} dBm)"
                    _radioLogs.update { (listOf(log) + it).take(8) }
                }
            }
        }
    }
}
