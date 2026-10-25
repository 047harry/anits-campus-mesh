package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import `in`.anits.mesh.data.dao.PeerDao
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Low-Power Bluetooth Low Energy Background Scanner Daemon.
 * Scans for nearby campus mesh peers using duty-cycled BLE 5.0 scanning.
 * Extracts 24-byte mini-beacons and registers neighbors into the Room database.
 */
class BleScannerDaemon(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val peerDao: PeerDao,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        val MESH_SERVICE_UUID: UUID = UUID.fromString("0000FEF1-0000-1000-8000-00805F9B34FB")
        val MESH_PARCEL_UUID: ParcelUuid = ParcelUuid(MESH_SERVICE_UUID)
        const val ANITS_MANUFACTURER_ID = 0x02E5
        private const val SCAN_ACTIVE_WINDOW_MS = 2500L
        private const val SCAN_REST_WINDOW_MS = 7500L // 25% duty cycle in standby
    }

    private var scanner: BluetoothLeScanner? = null
    private var isScanning = false
    private var dutyCycleJob: Job? = null

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
            // Log scan failure and schedule restart
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isScanning || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        scanner = bluetoothAdapter.bluetoothLeScanner
        isScanning = true

        startDutyCycleLoop()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        dutyCycleJob?.cancel()
        dutyCycleJob = null
        if (isScanning) {
            try {
                scanner?.stopScan(scanCallback)
            } catch (e: Exception) {
                // Handle adapter off state
            }
            isScanning = false
        }
    }

    fun setCrisisMode(enabled: Boolean) {
        _isCrisisMode.value = enabled
        // Restart loop with zero-rest continuous low latency scanning
        dutyCycleJob?.cancel()
        startDutyCycleLoop()
    }

    @SuppressLint("MissingPermission")
    private fun startDutyCycleLoop() {
        dutyCycleJob = coroutineScope.launch(Dispatchers.IO) {
            while (isActive && isScanning) {
                try {
                    val settings = ScanSettings.Builder()
                        .setScanMode(
                            if (_isCrisisMode.value) ScanSettings.SCAN_MODE_LOW_LATENCY
                            else ScanSettings.SCAN_MODE_BALANCED
                        )
                        .setReportDelay(0)
                        .build()

                    val filter = ScanFilter.Builder()
                        .setManufacturerData(ANITS_MANUFACTURER_ID, byteArrayOf())
                        .build()

                    scanner?.startScan(listOf(filter), settings, scanCallback)

                    if (_isCrisisMode.value) {
                        // Crisis mode: continuous scanning
                        delay(60_000L)
                    } else {
                        // Standby mode: duty cycle
                        delay(SCAN_ACTIVE_WINDOW_MS)
                        scanner?.stopScan(scanCallback)
                        delay(SCAN_REST_WINDOW_MS)
                    }
                } catch (e: Exception) {
                    delay(5000L)
                }
            }
        }
    }

    private fun handleScanResult(result: ScanResult) {
        val record = result.scanRecord ?: return
        val mfgData = record.getManufacturerSpecificData(ANITS_MANUFACTURER_ID)
        val serviceData = mfgData ?: record.getServiceData(MESH_PARCEL_UUID) ?: return
        if (serviceData.size < 16) return

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val version = serviceData[0]
                val roleByte = serviceData[1]
                val crisisFlag = serviceData[2]

                val nodeIdLen = if (serviceData.size >= 19) 16 else (serviceData.size - 7)
                val nodeIdBytes = serviceData.sliceArray(3 until (3 + nodeIdLen))
                val nodeIdHex = nodeIdBytes.joinToString("") { "%02x".format(it) }

                val peer = PeerEntity(
                    nodeId = nodeIdHex,
                    macAddress = result.device.address,
                    rollNumber = null,
                    userRole = roleByte,
                    isTrusted = (roleByte <= UserRole.FACULTY_WARDEN.code),
                    rssi = result.rssi,
                    radioType = "BLE",
                    lastSeenEpoch = System.currentTimeMillis()
                )

                peerDao.upsertPeer(peer)

                // If peer has crisisFlag active, elevate local scanner to crisis mode immediately
                if (crisisFlag.toInt() == 0x01 && !_isCrisisMode.value) {
                    setCrisisMode(true)
                }
            } catch (e: Exception) {
                // Ignore malformed advertisements
            }
        }
    }
}
