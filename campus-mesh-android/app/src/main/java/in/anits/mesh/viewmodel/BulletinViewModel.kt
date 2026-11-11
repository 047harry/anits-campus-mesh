package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.entity.MessageEntity
import `in`.anits.mesh.model.PacketType
import `in`.anits.mesh.radio.RadioCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

data class GoldShieldNotice(
    val id: String,
    val senderName: String,
    val senderRole: String,
    val timestampText: String,
    val hopCount: Int,
    val directiveText: String,
    val isVerifiedOffline: Boolean = true
)

class BulletinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusMeshApp).database
    private val messageDao = db.messageDao()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Live Flow connecting to Room SQLite table_messages (packet_type = 2)
    val bulletins: StateFlow<List<GoldShieldNotice>> = messageDao.getAuthorityBulletinsFlow()
        .map { entities ->
            val list = mutableListOf<GoldShieldNotice>()
            entities.forEach { entity ->
                try {
                    val jsonStr = String(entity.payloadBytes, StandardCharsets.UTF_8)
                    val json = JSONObject(jsonStr)
                    val text = json.optString("text", "").ifBlank { json.optString("directive_text", "") }
                    if (text.isNotBlank()) {
                        val senderName = json.optString("senderName", entity.sourceId)
                        val senderRole = json.optString("senderRole", "Authority Directive")
                        val timeStr = timeFormat.format(Date(entity.createdEpoch))
                        list.add(
                            GoldShieldNotice(
                                id = entity.messageUuid,
                                senderName = senderName,
                                senderRole = senderRole,
                                timestampText = timeStr,
                                hopCount = entity.hopCount,
                                directiveText = text,
                                isVerifiedOffline = true
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Safe continue
                }
            }
            if (list.isEmpty()) {
                // Default starter bulletins if none received yet
                listOf(
                    GoldShieldNotice(
                        id = "notif_default_1",
                        senderName = "Dr. K. Venkata Lakshmi (HOD — CSD)",
                        senderRole = "Tier 1 Institutional Broadcast",
                        timestampText = "Live System Ready",
                        hopCount = 0,
                        directiveText = "CAMPUS MESH RADIO ACTIVE: All official faculty & authority emergency directives will be broadcast here across student devices."
                    )
                )
            } else {
                list
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun publishDirective(directive: String) {
        if (directive.isBlank()) return
        val profile = UserManager.getProfile(getApplication()) ?: return
        val msgUuid = UUID.randomUUID().toString()

        val noticeJson = JSONObject().apply {
            put("uuid", msgUuid)
            put("packetType", PacketType.FACULTY_BROADCAST.code.toInt())
            put("senderRoll", profile.rollNumber)
            put("senderName", profile.fullName)
            put("senderRole", "${profile.role.title} (${profile.department})")
            put("destRoll", "ALL PEERS")
            put("text", directive.trim())
            put("directive_text", directive.trim())
            put("hopCount", 0)
            put("ttl", 7) // High 7-hop reach for emergency bulletins
            put("timestampEpoch", System.currentTimeMillis())
        }

        val jsonString = noticeJson.toString()

        // 1. Insert into local SQLite database
        val entity = MessageEntity(
            messageUuid = msgUuid,
            packetType = PacketType.FACULTY_BROADCAST.code,
            sourceId = profile.rollNumber,
            destId = "BROADCAST",
            payloadBytes = jsonString.toByteArray(StandardCharsets.UTF_8),
            isEncrypted = false,
            signatureBytes = ByteArray(64),
            hopCount = 0,
            remainingTtl = 7,
            deliveryStatus = "DELIVERED",
            createdEpoch = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            messageDao.insertMessage(entity)

            // 2. Transmit across Hardware Mesh Radios (BLE GATT + Wi-Fi LAN UDP)
            RadioCoordinator.instance?.transmitChatMessage(entity, jsonString)

            // 3. Best-effort Supabase cloud backup if internet is present
            try {
                val url = URL("${UserManager.SUPABASE_URL}/rest/v1/official_bulletins")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", UserManager.SUPABASE_ANON_KEY)
                conn.setRequestProperty("Authorization", "Bearer ${UserManager.SUPABASE_ANON_KEY}")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
                conn.connectTimeout = 4000
                conn.doOutput = true

                val body = JSONObject().apply {
                    put("bulletin_id", msgUuid)
                    put("sender_name", "${profile.fullName} (${profile.rollNumber})")
                    put("sender_role", "${profile.role.title} (${profile.department})")
                    put("directive_text", directive.trim())
                    put("ed25519_signature", profile.publicKeyHex)
                    put("hop_count", 0)
                }

                conn.outputStream.use { os ->
                    os.write(body.toString().toByteArray(Charsets.UTF_8))
                }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }
}
