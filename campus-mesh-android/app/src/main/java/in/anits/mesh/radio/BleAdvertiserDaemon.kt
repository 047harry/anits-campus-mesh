package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * Low-Power Bluetooth Low Energy Background Advertiser Daemon.
 * Broadcasts node presence, role badges, and immediate SOS beacon alerts in the 24-byte payload.
 */
class BleAdvertiserDaemon(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val localNodeId: ByteArray,
    private val localRole: UserRole,
    private val coroutineScope: CoroutineScope
) {
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    private var isSosActive = false
    private var sequenceNumber = 0

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (isAdvertising || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return

        broadcastBeacon()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (isAdvertising) {
            try {
                advertiser?.stopAdvertising(advertiseCallback)
            } catch (e: Exception) {
                // Handle adapter off
            }
            isAdvertising = false
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
                .setAdvertiseMode(
                    if (isSosActive) AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
                    else AdvertiseSettings.ADVERTISE_MODE_BALANCED
                )
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0)
                .build()

            // Construct 20-Byte Compact Payload (< 31-byte legacy BLE limit)
            val buffer = ByteBuffer.allocate(20)
            buffer.put(0x01.toByte()) // Version 1
            buffer.put(localRole.code) // User Role
            buffer.put(if (isSosActive) 0x01.toByte() else 0x00.toByte()) // Crisis Flag
            buffer.put(localNodeId.sliceArray(0 until 12)) // 12-Byte Node ID Prefix
            buffer.putInt(++sequenceNumber) // 4-Byte Sequence Counter
            buffer.put(84.toByte()) // Battery

            val data = AdvertiseData.Builder()
                .addManufacturerData(BleScannerDaemon.ANITS_MANUFACTURER_ID, buffer.array())
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()

            try {
                advertiser?.startAdvertising(settings, data, advertiseCallback)
            } catch (e: Exception) {
                // Catch hardware limits
            }
        }
    }
}
