package `in`.anits.mesh.radio

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import `in`.anits.mesh.protocol.PacketSerializer
import `in`.anits.mesh.model.MeshPacket
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Wi-Fi Aware (Neighbor Awareness Networking - NAN) Session Controller.
 * Manages peer publish/subscribe discovery and negotiates direct high-speed
 * data sockets for large packet/bundle payloads.
 */
class WifiAwareSessionManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPacketReceived: (MeshPacket) -> Unit
) {
    companion object {
        const val SERVICE_NAME = "in.anits.mesh.campus"
        const val DATA_PORT = 38884
    }

    private val wifiAwareManager: WifiAwareManager? =
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            context.getSystemService(Context.WIFI_AWARE_SERVICE) as? WifiAwareManager
        } else null

    private var currentSession: WifiAwareSession? = null
    private var publishDiscoverySession: PublishDiscoverySession? = null
    private var subscribeDiscoverySession: SubscribeDiscoverySession? = null
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    val isSupported: Boolean
        get() = wifiAwareManager != null && wifiAwareManager.isAvailable

    fun start() {
        if (!isSupported || isRunning) return
        isRunning = true

        wifiAwareManager?.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession?) {
                currentSession = session
                startPublishSession()
                startSubscribeSession()
                startSocketListener()
            }

            override fun onAttachFailed() {
                isRunning = false
            }
        }, null)
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            publishDiscoverySession?.close()
            subscribeDiscoverySession?.close()
            currentSession?.close()
        } catch (e: Exception) {
            // Safe close
        }
    }

    private fun startPublishSession() {
        val config = PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()

        currentSession?.publish(config, object : DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                publishDiscoverySession = session
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Short control message received over Aware L2 channel
            }
        }, null)
    }

    private fun startSubscribeSession() {
        val config = SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()

        currentSession?.subscribe(config, object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                subscribeDiscoverySession = session
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle,
                serviceSpecificInfo: ByteArray?,
                matchFilter: MutableList<ByteArray>?
            ) {
                // Peer found via Wi-Fi Aware. Available for direct high-speed socket escalation.
            }
        }, null)
    }

    /**
     * Listens on a direct local socket for incoming multi-hop binary wire packets.
     */
    private fun startSocketListener() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(DATA_PORT)
                while (isActive && isRunning) {
                    val socket = serverSocket?.accept() ?: continue
                    launch(Dispatchers.IO) {
                        handleIncomingConnection(socket)
                    }
                }
            } catch (e: Exception) {
                // Socket stopped
            }
        }
    }

    private fun handleIncomingConnection(socket: Socket) {
        try {
            socket.use { s ->
                val input: InputStream = s.getInputStream()
                val rawBytes = input.readBytes()
                if (rawBytes.isNotEmpty()) {
                    val packet = PacketSerializer.deserialize(rawBytes)
                    onPacketReceived(packet)
                }
            }
        } catch (e: Exception) {
            // Malformed or interrupted socket transmission
        }
    }

    /**
     * Sends a MeshPacket over a direct TCP socket channel to a peer IP.
     */
    suspend fun sendPacketDirect(hostIp: String, packet: MeshPacket): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Socket(hostIp, DATA_PORT).use { s ->
                val out: OutputStream = s.getOutputStream()
                val wireBytes = PacketSerializer.serialize(packet)
                out.write(wireBytes)
                out.flush()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
