package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.util.Log
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * Real Hardware BLE Background Advertiser Daemon.
 * Broadcasts node presence, role badges, crisis alerts, and ROLL NUMBER in 20-byte manufacturer data.
 */
class BleAdvertiserDaemon(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private var localRole: UserRole,
    private var localRollNumber: String,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "BleAdvertiser"
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private var isSosActive = false
    private var sequenceNumber = 0

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            Log.i(TAG, "✓ BLE Advertising ACTIVE: Roll=$localRollNumber, Role=${localRole.title}")
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            Log.e(TAG, "✗ BLE Advertising FAILED with code $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isAdvertising) return
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Cannot start advertising: Bluetooth is disabled or unavailable")
            return
        }
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.e(TAG, "BluetoothLeAdvertiser is NULL on this device hardware")
            return
        }

        broadcastBeacon()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (isAdvertising) {
            try {
                advertiser?.stopAdvertising(advertiseCallback)
                Log.i(TAG, "BLE Advertising stopped")
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping advertising: ${e.message}")
            }
            isAdvertising = false
        }
    }

    @SuppressLint("MissingPermission")
    fun updateIdentity(newRole: UserRole, newRollNumber: String) {
        localRole = newRole
        localRollNumber = newRollNumber
        Log.i(TAG, "Updating advertising identity to Roll=$newRollNumber, Role=${newRole.title}")
        if (isAdvertising) {
            stop()
            start()
        }
    }

    @SuppressLint("MissingPermission")
    fun triggerSosBeacon() {
        isSosActive = true
        stop()
        start()
    }

    @SuppressLint("MissingPermission")
    fun clearSosBeacon() {
        isSosActive = false
        stop()
        start()
    }

    @SuppressLint("MissingPermission")
    private fun broadcastBeacon() {
        coroutineScope.launch(Dispatchers.IO) {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0)
                .build()

            // 20-Byte Compact Payload (< 31-byte legacy BLE limit)
            val buffer = ByteBuffer.allocate(20)
            buffer.put(0x01.toByte()) // Byte 0: Protocol Version 1
            buffer.put(localRole.code) // Byte 1: Stakeholder Role Code (0..5)
            buffer.put(if (isSosActive) 0x01.toByte() else 0x00.toByte()) // Byte 2: Crisis / SOS Flag

            // Bytes 3..17: Roll Number payload (Length byte + up to 14 ASCII characters)
            val rollBytes = localRollNumber.toByteArray(StandardCharsets.UTF_8)
            val rollLen = minOf(rollBytes.size, 14)
            buffer.put(rollLen.toByte()) // Byte 3: Roll number length
            buffer.put(rollBytes, 0, rollLen) // Bytes 4..(4+rollLen-1)

            // Pad remaining bytes up to index 18
            val remainingPad = 18 - buffer.position()
            for (i in 0 until remainingPad) {
                buffer.put(0.toByte())
            }

            buffer.put(84.toByte()) // Byte 18: Battery level
            buffer.put((++sequenceNumber % 256).toByte()) // Byte 19: Sequence counter

            val data = AdvertiseData.Builder()
                .addManufacturerData(BleScannerDaemon.ANITS_MANUFACTURER_ID, buffer.array())
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()

            try {
                advertiser?.startAdvertising(settings, data, advertiseCallback)
            } catch (e: Exception) {
                Log.e(TAG, "Exception in startAdvertising: ${e.message}", e)
            }
        }
    }
}
