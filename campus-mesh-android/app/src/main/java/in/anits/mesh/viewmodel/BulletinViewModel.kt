package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

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

    private val _bulletins = MutableStateFlow(
        listOf(
            GoldShieldNotice(
                id = "notif_1",
                senderName = "Dr. K. Venkata Lakshmi (HOD — CSD)",
                senderRole = "Tier 1 Institutional Broadcast",
                timestampText = "10:14 AM",
                hopCount = 2,
                directiveText = "IMMEDIATE EVACUATION ORDER: Minor tremor aftershock detected. DO NOT use central lifts. Evacuate via North Fire Stairwells directly to the Central Cricket Ground Muster Point. Medical triage tent 1 active."
            ),
            GoldShieldNotice(
                id = "notif_2",
                senderName = "Health Center Emergency Staff",
                senderRole = "Campus Clinic Advisory",
                timestampText = "10:10 AM",
                hopCount = 1,
                directiveText = "First-aid stations set up at Cricket Ground. Stretcher volunteer teams dispatched to CSE & Mechanical blocks."
            )
        )
    )
    val bulletins: StateFlow<List<GoldShieldNotice>> = _bulletins.asStateFlow()

    fun publishDirective(directive: String) {
        if (directive.isBlank()) return
        val profile = UserManager.getProfile(getApplication()) ?: return

        val newNotice = GoldShieldNotice(
            id = UUID.randomUUID().toString(),
            senderName = "${profile.fullName} (${profile.rollNumber})",
            senderRole = "${profile.role.title} (${profile.department})",
            timestampText = "Just now",
            hopCount = 0,
            directiveText = directive.trim()
        )

        _bulletins.value = listOf(newNotice) + _bulletins.value

        // Asynchronously sync to Supabase Cloud
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("${UserManager.SUPABASE_URL}/rest/v1/official_bulletins")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", UserManager.SUPABASE_ANON_KEY)
                conn.setRequestProperty("Authorization", "Bearer ${UserManager.SUPABASE_ANON_KEY}")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
                conn.doOutput = true

                val body = JSONObject().apply {
                    put("bulletin_id", newNotice.id)
                    put("sender_name", newNotice.senderName)
                    put("sender_role", newNotice.senderRole)
                    put("directive_text", newNotice.directiveText)
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
