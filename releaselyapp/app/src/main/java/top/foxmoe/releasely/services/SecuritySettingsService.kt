package top.foxmoe.releasely.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SecuritySettingsService(
    private val context: Context,
    private val apiService: ApiService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("security", Context.MODE_PRIVATE)
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    data class SecurityState(
        val decoyEnabled: Boolean,
        val appLockEnabled: Boolean,
        val pinHash: String,
        val disguiseType: String,
        val screenshotProtected: Boolean
    )

    suspend fun getSettings(userId: Long?): SecurityState = withContext(Dispatchers.IO) {
        if (userId != null && authToken != null) {
            try {
                val response = apiService.get("/security/settings?userId=$userId", authToken)
                response.getOrNull()?.let { jsonString ->
                    val jsonObj = JSONObject(jsonString)
                    val data = jsonObj.optJSONObject("data")
                    if (data != null) {
                        val state = SecurityState(
                            decoyEnabled = data.optBoolean("disguiseEnabled", false),
                            appLockEnabled = data.optBoolean("appLockEnabled", false),
                            pinHash = data.optString("pinHash", ""),
                            disguiseType = data.optString("disguiseType", "CALCULATOR"),
                            screenshotProtected = data.optBoolean("screenshotProtected", true)
                        )
                        saveToLocal(state)
                        return@withContext state
                    }
                }
            } catch (_: Exception) {
                // Fall back to local
            }
        }
        loadFromLocal()
    }

    suspend fun saveSettings(userId: Long?, state: SecurityState): Boolean = withContext(Dispatchers.IO) {
        saveToLocal(state)
        if (userId != null && authToken != null) {
            try {
                val json = JSONObject().apply {
                    put("userId", userId)
                    put("isAppLockEnabled", state.appLockEnabled)
                    put("isDisguiseEnabled", state.decoyEnabled)
                    put("disguiseType", state.disguiseType)
                    put("isScreenshotProtected", state.screenshotProtected)
                }
                val response = apiService.put("/security/settings", json.toString(), authToken)
                return@withContext response.isSuccess
            } catch (_: Exception) {
                return@withContext false
            }
        }
        true
    }

    suspend fun verifyPin(userId: Long?, pin: String): Boolean = withContext(Dispatchers.IO) {
        if (userId != null && authToken != null) {
            try {
                val json = JSONObject().apply {
                    put("userId", userId)
                    put("pin", pin)
                }
                val response = apiService.post("/security/pin/verify", json.toString(), authToken)
                response.getOrNull()?.let { jsonString ->
                    val jsonObj = JSONObject(jsonString)
                    val data = jsonObj.optJSONObject("data")
                    return@withContext data?.optBoolean("success", false) ?: false
                }
            } catch (_: Exception) {
                // Fall back to local
            }
        }
        val storedHash = prefs.getString("pin_hash", "") ?: ""
        hashPin(pin) == storedHash
    }

    fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveToLocal(state: SecurityState) {
        prefs.edit().apply {
            putBoolean("decoy_enabled", state.decoyEnabled)
            putBoolean("app_lock_enabled", state.appLockEnabled)
            putString("pin_hash", state.pinHash)
            putString("disguise_type", state.disguiseType)
            putBoolean("screenshot_protected", state.screenshotProtected)
            apply()
        }
    }

    private fun loadFromLocal(): SecurityState {
        return SecurityState(
            decoyEnabled = prefs.getBoolean("decoy_enabled", false),
            appLockEnabled = prefs.getBoolean("app_lock_enabled", false),
            pinHash = prefs.getString("pin_hash", "") ?: "",
            disguiseType = prefs.getString("disguise_type", "CALCULATOR") ?: "CALCULATOR",
            screenshotProtected = prefs.getBoolean("screenshot_protected", true)
        )
    }
}
