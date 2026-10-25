package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import `in`.anits.mesh.data.dao.PeerDao
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Real Hardware BLE Background Scanner Daemon.
 * Scans continuous low-latency for nearby peer devices broadcasting ANITS manufacturer data.
 */
class BleScannerDaemon(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val peerDao: PeerDao,
    private var localNodeIdHex: String,
    private val coroutineScope: CoroutineScope,
    private val onPeerDiscovered: ((PeerEntity, String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "BleScanner"
        val MESH_SERVICE_UUID: UUID = UUID.fromString("0000FEF1-0000-1000-8000-00805F9B34FB")
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

    fun setLocalNodeIdHex(nodeIdHex: String) {
        localNodeIdHex = nodeIdHex
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
        if (mfgData.size < 15) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val version = mfgData[0]
                val roleByte = mfgData[1]
                val crisisFlag = mfgData[2]

                // Extract fixed 12-byte node ID
                val nodeIdBytes = mfgData.sliceArray(3 until 15)
                val nodeIdHex = nodeIdBytes.joinToString("") { "%02x".format(it) }

                // Ignore packets originating from this local device!
                if (nodeIdHex.equals(localNodeIdHex, ignoreCase = true)) {
                    return@launch
                }

                val role = UserRole.fromByte(roleByte)
                val peer = PeerEntity(
                    nodeId = nodeIdHex,
                    macAddress = result.device.address ?: "00:00:00:00:00:00",
                    rollNumber = null,
                    userRole = roleByte,
                    isTrusted = (roleByte <= UserRole.FACULTY_WARDEN.code),
                    rssi = result.rssi,
                    radioType = "BLE 5.0",
                    lastSeenEpoch = System.currentTimeMillis()
                )

                peerDao.upsertPeer(peer)
                Log.i(TAG, "🎉 FOUND PHYSICAL PEER! Node=$nodeIdHex, Role=${role.title}, RSSI=${result.rssi}dBm")

                onPeerDiscovered?.invoke(peer, "Discovered ${role.title} (${result.rssi} dBm)")

                if (crisisFlag.toInt() == 0x01 && !_isCrisisMode.value) {
                    setCrisisMode(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling scan result: ${e.message}")
            }
        }
    }
}
