package `in`.anits.mesh.data

import android.content.Context
import android.content.SharedPreferences
import `in`.anits.mesh.crypto.CryptoEngine
import `in`.anits.mesh.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

data class UserProfile(
    val fullName: String,
    val rollNumber: String,
    val department: String,
    val role: UserRole,
    val nodeId: String,
    val publicKeyHex: String,
    val privateKeyHex: String
)

data class CampusUserSummary(
    val rollNumber: String,
    val fullName: String,
    val department: String,
    val role: UserRole,
    val nodeId: String,
    val publicKeyHex: String
)

object UserManager {
    private const val PREF_NAME = "anits_mesh_user_profile"
    private const val KEY_IS_REGISTERED = "is_registered"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_ROLL_NUMBER = "roll_number"
    private const val KEY_DEPARTMENT = "department"
    private const val KEY_ROLE_CODE = "role_code"
    private const val KEY_NODE_ID = "node_id"
    private const val KEY_PUB_KEY = "pub_key"
    private const val KEY_PRIV_KEY = "priv_key"
    private const val KEY_PASS_HASH = "pass_hash"

    // Supabase FinalDance Project Config
    const val SUPABASE_URL = "https://ilpmxbeuhdtobwpgzgcf.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlscG14YmV1aGR0b2J3cGd6Z2NmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTI4Njk0ODcsImV4cCI6MjEwODQ0NTQ4N30.GiElIludoDDXDbMQakJqCqP8l3sKH5_EWNRaEC46Xns"

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // In-memory directory of registered campus nodes
    private val _campusDirectory = MutableStateFlow<Map<String, CampusUserSummary>>(emptyMap())
    val campusDirectory: StateFlow<Map<String, CampusUserSummary>> = _campusDirectory.asStateFlow()

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.trim().toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun isRegistered(context: Context): Boolean {
        val prefs = getPrefs(context)
        val registered = prefs.getBoolean(KEY_IS_REGISTERED, false)
        if (registered) {
            if (_currentUser.value == null) {
                _currentUser.value = loadProfile(context)
            }
            // Auto-sync profile and directory in background on start
            CoroutineScope(Dispatchers.IO).launch {
                val prof = _currentUser.value ?: loadProfile(context)
                val passHash = prefs.getString(KEY_PASS_HASH, "") ?: ""
                if (prof != null) {
                    syncUserToCloud(prof, passHash)
                }
                syncCampusDirectory()
            }
        }
        return registered
    }

    fun getProfile(context: Context): UserProfile? {
        if (_currentUser.value == null) {
            _currentUser.value = loadProfile(context)
        }
        return _currentUser.value
    }

    suspend fun signUpUser(
        context: Context,
        fullName: String,
        rollNumber: String,
        department: String,
        role: UserRole,
        password: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val cleanRoll = rollNumber.trim().uppercase()
            val cleanName = fullName.trim()
            val passHash = hashPassword(password)

            // Generate unique Node ID and Ed25519 Keypair
            val nodeId = "node_" + UUID.randomUUID().toString().replace("-", "").take(11)
            val keyPair = CryptoEngine.generateKeyPair()
            val pubHex = keyPair.publicKeyBytes.joinToString("") { "%02x".format(it) }
            val privHex = keyPair.privateKeyBytes.joinToString("") { "%02x".format(it) }

            val profile = UserProfile(
                fullName = cleanName,
                rollNumber = cleanRoll,
                department = department,
                role = role,
                nodeId = nodeId,
                publicKeyHex = pubHex,
                privateKeyHex = privHex
            )

            // Persist locally
            getPrefs(context).edit().apply {
                putBoolean(KEY_IS_REGISTERED, true)
                putString(KEY_FULL_NAME, profile.fullName)
                putString(KEY_ROLL_NUMBER, profile.rollNumber)
                putString(KEY_DEPARTMENT, profile.department)
                putInt(KEY_ROLE_CODE, profile.role.code.toInt())
                putString(KEY_NODE_ID, profile.nodeId)
                putString(KEY_PUB_KEY, profile.publicKeyHex)
                putString(KEY_PRIV_KEY, profile.privateKeyHex)
                putString(KEY_PASS_HASH, passHash)
                apply()
            }

            _currentUser.value = profile

            // Push to Supabase Cloud
            syncUserToCloud(profile, passHash)
            syncCampusDirectory()

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(
        context: Context,
        rollNumber: String,
        password: String
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val cleanRoll = rollNumber.trim().uppercase()
        val passHash = hashPassword(password)
        val prefs = getPrefs(context)

        // 1. Try local cache login first (for disaster offline resilience)
        val cachedRoll = prefs.getString(KEY_ROLL_NUMBER, "") ?: ""
        val cachedPassHash = prefs.getString(KEY_PASS_HASH, "") ?: ""

        if (cachedRoll == cleanRoll && cachedPassHash == passHash && prefs.getBoolean(KEY_IS_REGISTERED, false)) {
            val profile = loadProfile(context)
            if (profile != null) {
                _currentUser.value = profile
                // Background refresh directory if online
                launch { syncCampusDirectory() }
                return@withContext Result.success(profile)
            }
        }

        // 2. Query Supabase Cloud for user
        try {
            val url = URL("$SUPABASE_URL/rest/v1/campus_users?roll_number=eq.$cleanRoll")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.readText()
                reader.close()
                conn.disconnect()

                val jsonArray = JSONArray(responseText)
                if (jsonArray.length() == 0) {
                    return@withContext Result.failure(Exception("Account not found for $cleanRoll. Please Sign Up."))
                }

                val userObj = jsonArray.getJSONObject(0)
                val cloudPassHash = userObj.optString("password_hash", "")

                if (cloudPassHash.isNotBlank() && cloudPassHash != passHash) {
                    return@withContext Result.failure(Exception("Invalid credentials. Incorrect password."))
                }

                val roleName = userObj.optString("user_role", "STUDENT_OCCUPANT")
                val role = try { UserRole.valueOf(roleName) } catch (e: Exception) { UserRole.STUDENT_OCCUPANT }
                val fullName = userObj.optString("full_name", "ANITS User")
                val department = userObj.optString("department", "CSD")
                val nodeId = userObj.optString("node_id", "node_" + UUID.randomUUID().toString().take(8))
                var pubKey = userObj.optString("ed25519_public_key", "")
                var privKey = userObj.optString("encrypted_private_key", "")

                if (privKey.isBlank() || pubKey.isBlank()) {
                    val keyPair = CryptoEngine.generateKeyPair()
                    pubKey = keyPair.publicKeyBytes.joinToString("") { "%02x".format(it) }
                    privKey = keyPair.privateKeyBytes.joinToString("") { "%02x".format(it) }
                }

                val profile = UserProfile(
                    fullName = fullName,
                    rollNumber = cleanRoll,
                    department = department,
                    role = role,
                    nodeId = nodeId,
                    publicKeyHex = pubKey,
                    privateKeyHex = privKey
                )

                // Cache locally
                prefs.edit().apply {
                    putBoolean(KEY_IS_REGISTERED, true)
                    putString(KEY_FULL_NAME, profile.fullName)
                    putString(KEY_ROLL_NUMBER, profile.rollNumber)
                    putString(KEY_DEPARTMENT, profile.department)
                    putInt(KEY_ROLE_CODE, profile.role.code.toInt())
                    putString(KEY_NODE_ID, profile.nodeId)
                    putString(KEY_PUB_KEY, profile.publicKeyHex)
                    putString(KEY_PRIV_KEY, profile.privateKeyHex)
                    putString(KEY_PASS_HASH, passHash)
                    apply()
                }

                _currentUser.value = profile
                syncCampusDirectory()
                return@withContext Result.success(profile)
            } else {
                conn.disconnect()
                return@withContext Result.failure(Exception("Authentication server error (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Network offline and no cached session found. (${e.localizedMessage})"))
        }
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
        _currentUser.value = null
    }

    suspend fun syncCampusDirectory() = withContext(Dispatchers.IO) {
        try {
            val url = URL("$SUPABASE_URL/rest/v1/campus_users?select=roll_number,full_name,department,user_role,node_id,ed25519_public_key")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 6000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonArray = JSONArray(reader.readText())
                reader.close()
                conn.disconnect()

                val dirMap = mutableMapOf<String, CampusUserSummary>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val roll = obj.optString("roll_number", "")
                    val name = obj.optString("full_name", "")
                    val dept = obj.optString("department", "")
                    val roleStr = obj.optString("user_role", "STUDENT_OCCUPANT")
                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT_OCCUPANT }
                    val nId = obj.optString("node_id", "")
                    val pub = obj.optString("ed25519_public_key", "")

                    val summary = CampusUserSummary(roll, name, dept, role, nId, pub)
                    if (nId.isNotBlank()) dirMap[nId] = summary
                    if (roll.isNotBlank()) dirMap[roll] = summary
                }
                _campusDirectory.value = dirMap
            }
        } catch (e: Exception) {
            // Ignore offline errors
        }
    }

    fun resolvePeer(nodeIdOrRoll: String, fallbackRoleByte: Byte): CampusUserSummary {
        val cached = _campusDirectory.value[nodeIdOrRoll]
        if (cached != null) return cached

        val fallbackRole = UserRole.fromByte(fallbackRoleByte)
        return CampusUserSummary(
            rollNumber = nodeIdOrRoll.take(12),
            fullName = when (fallbackRole) {
                UserRole.SYSTEM_ADMIN -> "Campus Root Administrator"
                UserRole.PRINCIPAL_DEAN -> "Principal / Dean's Office"
                UserRole.HOD -> "Head of Department"
                UserRole.FACULTY_WARDEN -> "Faculty / Hostel Warden"
                UserRole.CLINIC_SECURITY -> "Clinic Triage / Security"
                UserRole.STUDENT_OCCUPANT -> "Student Peer (${nodeIdOrRoll.take(6)})"
            },
            department = "ANITS",
            role = fallbackRole,
            nodeId = nodeIdOrRoll,
            publicKeyHex = ""
        )
    }

    private fun loadProfile(context: Context): UserProfile? {
        val prefs = getPrefs(context)
        if (!prefs.getBoolean(KEY_IS_REGISTERED, false)) return null

        val roleCode = prefs.getInt(KEY_ROLE_CODE, UserRole.STUDENT_OCCUPANT.code.toInt()).toByte()
        return UserProfile(
            fullName = prefs.getString(KEY_FULL_NAME, "ANITS User") ?: "ANITS User",
            rollNumber = prefs.getString(KEY_ROLL_NUMBER, "A23126551000") ?: "A23126551000",
            department = prefs.getString(KEY_DEPARTMENT, "CSD") ?: "CSD",
            role = UserRole.fromByte(roleCode),
            nodeId = prefs.getString(KEY_NODE_ID, "node_00000000") ?: "node_00000000",
            publicKeyHex = prefs.getString(KEY_PUB_KEY, "") ?: "",
            privateKeyHex = prefs.getString(KEY_PRIV_KEY, "") ?: ""
        )
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun syncUserToCloud(profile: UserProfile, passHash: String) {
        try {
            android.util.Log.d("UserManager", "Attempting cloud sync for ${profile.rollNumber}...")
            val url = URL("$SUPABASE_URL/rest/v1/campus_users?on_conflict=roll_number")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            conn.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true

            val body = JSONObject().apply {
                put("roll_number", profile.rollNumber)
                put("full_name", profile.fullName)
                put("department", profile.department)
                put("user_role", profile.role.name)
                put("node_id", profile.nodeId)
                put("ed25519_public_key", profile.publicKeyHex)
                put("encrypted_private_key", profile.privateKeyHex)
                put("password_hash", passHash)
            }

            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            android.util.Log.d("UserManager", "Cloud sync response code: $code")
            if (code in 200..299) {
                android.util.Log.i("UserManager", "Successfully synced ${profile.rollNumber} to Supabase!")
            } else {
                val err = conn.errorStream?.bufferedReader()?.readText()
                android.util.Log.e("UserManager", "Supabase error HTTP $code: $err")
            }
            conn.disconnect()
        } catch (e: Exception) {
            android.util.Log.e("UserManager", "Exception during cloud sync: ${e.message}", e)
        }
    }
}
