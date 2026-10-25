package `in`.anits.mesh.radio

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Persistent Android Foreground Service.
 * Ensures BLE and Wi-Fi Aware mesh daemons survive Android OS background task killing
 * and doze modes during campus crisis events.
 */
class MeshRadioService : Service() {

    companion object {
        const val CHANNEL_ID = "anits_mesh_radio_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, MeshRadioService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MeshRadioService::class.java)
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var radioCoordinator: RadioCoordinator

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val app = application as CampusMeshApp
        radioCoordinator = RadioCoordinator(
            context = applicationContext,
            database = app.database,
            coroutineScope = serviceScope
        )

        acquireWakeLock()
        startForeground(NOTIFICATION_ID, buildNotification())
        radioCoordinator.startMeshServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (::radioCoordinator.isInitialized) {
            radioCoordinator.updateProfile()
            radioCoordinator.startMeshServices()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        radioCoordinator.stopMeshServices()
        releaseWakeLock()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CampusMesh::RadioWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // 10 minutes sliding window
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Safe release
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ANITS Campus Mesh Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps peer-to-peer disaster routing active offline"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ANITS Campus Mesh Active")
            .setContentText("Autonomous peer-to-peer radio relay active offline")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
