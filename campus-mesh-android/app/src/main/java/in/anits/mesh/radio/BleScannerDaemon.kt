package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.dao.PeerDao
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Real Hardware BLE Background Scanner Daemon.
 * Scans continuous low-latency for nearby peer devices broadcasting ANITS manufacturer data.
 */
class BleScannerDaemon(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val peerDao: PeerDao,
    private var localRollNumber: String,
    private val coroutineScope: CoroutineScope,
    private val onPeerDiscovered: ((PeerEntity, String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "BleScanner"
        val MESH_SERVICE_UUID: UUID = UUID.fromString("0000FEF1-0000-1000-8000-00805F9B34FB")
        val MESH_CHAR_UUID: UUID = UUID.fromString("0000FEF2-0000-1000-8000-00805F9B34FB")
        val MESH_PARCEL_UUID: ParcelUuid = ParcelUuid(MESH_SERVICE_UUID)
        const val ANITS_MANUFACTURER_ID = 0x02E5
    }

    private var scanner: BluetoothLeScanner? = null
    private var isScanning = false

    private val _isCrisisMode = MutableStateFlow(false)
    val isCrisisMode: StateFlow<Boolean> = _isCrisisMode.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { handleScanResult(it) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { handleScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE Scan Failed with error code: $errorCode")
            isScanning = false
        }
    }

    fun setLocalRollNumber(rollNumber: String) {
        localRollNumber = rollNumber
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isScanning) return
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Cannot start scanner: Bluetooth is off or unavailable")
            return
        }
        scanner = bluetoothAdapter.bluetoothLeScanner
        if (scanner == null) {
            Log.e(TAG, "BluetoothLeScanner is NULL on this hardware")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        try {
            // Scan without restrictive hardware filters so all advertisements are received
            scanner?.startScan(null, settings, scanCallback)
            isScanning = true
            Log.i(TAG, "✓ Real Hardware BLE Scanner started in LOW_LATENCY mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start BLE scan: ${e.message}", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (isScanning) {
            try {
                scanner?.stopScan(scanCallback)
                Log.i(TAG, "BLE Scanner stopped")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping scanner: ${e.message}")
            }
            isScanning = false
        }
    }

    fun setCrisisMode(enabled: Boolean) {
        _isCrisisMode.value = enabled
    }

    private fun handleScanResult(result: ScanResult) {
        val record = result.scanRecord ?: return
        val mfgData = record.getManufacturerSpecificData(ANITS_MANUFACTURER_ID) ?: return
        if (mfgData.size < 5) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val version = mfgData[0]
                val roleByte = mfgData[1]
                val crisisFlag = mfgData[2]

                // Extract Roll Number directly from beacon payload!
                val rollLen = mfgData[3].toInt().coerceIn(1, minOf(8, mfgData.size - 4))
                val rollNumber = String(mfgData, 4, rollLen, StandardCharsets.UTF_8).trim()

                if (rollNumber.isBlank()) return@launch

                // Ignore packets originating from this local device!
                if (rollNumber.equals(localRollNumber, ignoreCase = true)) {
                    return@launch
                }

                val role = UserRole.fromByte(roleByte)

                // Extract Name from beacon payload!
                var peerName = ""
                val nameOffset = 4 + rollLen
                if (mfgData.size > nameOffset) {
                    val nameLen = mfgData[nameOffset].toInt().coerceIn(0, minOf(8, mfgData.size - nameOffset - 1))
                    if (nameLen > 0) {
                        peerName = String(mfgData, nameOffset + 1, nameLen, StandardCharsets.UTF_8).trim()
                    }
                }

                if (peerName.isNotBlank() && !peerName.startsWith("Roll No:", ignoreCase = true) && !peerName.equals("User", ignoreCase = true)) {
                    UserManager.registerKnownPeer(rollNumber, peerName, role = role)
                }

                val summary = UserManager.resolvePeer(rollNumber, roleByte)

                val mac = result.device.address ?: "00:00:00:00:00:00"
                if (mac != "00:00:00:00:00:00") {
                    peerDao.deleteOtherPeersWithMac(mac, rollNumber)
                }

                val peer = PeerEntity(
                    nodeId = rollNumber,
                    macAddress = result.device.address ?: "00:00:00:00:00:00",
                    rollNumber = rollNumber,
                    userRole = roleByte,
                    isTrusted = (roleByte <= UserRole.FACULTY_WARDEN.code),
                    rssi = result.rssi,
                    radioType = "BLE 5.0",
                    lastSeenEpoch = System.currentTimeMillis()
                )

                peerDao.upsertPeer(peer)
                Log.i(TAG, "🎉 FOUND PHYSICAL PEER! Roll=${summary.rollNumber}, Name=${summary.fullName}, RSSI=${result.rssi}dBm")

                onPeerDiscovered?.invoke(peer, "Discovered ${summary.fullName} (${summary.rollNumber})")

                if (crisisFlag.toInt() == 0x01 && !_isCrisisMode.value) {
                    setCrisisMode(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling scan result: ${e.message}")
            }
        }
    }
}
