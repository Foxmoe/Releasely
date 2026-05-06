package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class LoginResult(
    val success: Boolean,
    val token: String? = null,
    val refreshToken: String? = null,
    val preAuthToken: String? = null,
    val requires2FA: Boolean = false,
    val username: String = "",
    val userId: Long? = null,
    val error: String? = null
)

data class RegisterResult(
    val success: Boolean,
    val message: String = "",
    val error: String? = null
)

data class DeleteAccountResult(
    val success: Boolean,
    val message: String = "",
    val error: String? = null
)

data class RefreshResult(
    val success: Boolean,
    val token: String? = null,
    val refreshToken: String? = null,
    val username: String? = null,
    val userId: Long? = null,
    val error: String? = null
)

class AuthService(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            val response = apiService.post("/auth/login", json.toString())
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    val data = obj.optJSONObject("data")
                    val requires2FA = data?.optBoolean("requires2FA", false) ?: false
                    val userId = data?.optLong("userId", -1L)?.takeIf { it != -1L }
                    if (requires2FA) {
                        LoginResult(
                            success = true,
                            preAuthToken = data?.optString("preAuthToken", ""),
                            requires2FA = true,
                            username = data?.optString("username", username) ?: username,
                            userId = userId
                        )
                    } else {
                        LoginResult(
                            success = true,
                            token = data?.optString("token", ""),
                            refreshToken = data?.optString("refreshToken", ""),
                            username = data?.optString("username", username) ?: username,
                            userId = userId
                        )
                    }
                } else {
                    LoginResult(success = false, error = obj.optString("message", "登录失败"))
                }
            } ?: LoginResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            LoginResult(success = false, error = e.message ?: "未知错误")
        }
    }

    suspend fun verify2FA(preAuthToken: String, totpCode: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("preAuthToken", preAuthToken)
                put("totpCode", totpCode)
            }
            val response = apiService.post("/auth/2fa/verify", json.toString())
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    val data = obj.optJSONObject("data")
                    val userId = data?.optLong("userId", -1L)?.takeIf { it != -1L }
                    LoginResult(
                        success = true,
                        token = data?.optString("token", ""),
                        username = data?.optString("username", "") ?: "",
                        userId = userId
                    )
                } else {
                    LoginResult(success = false, error = obj.optString("message", "验证失败"))
                }
            } ?: LoginResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            LoginResult(success = false, error = e.message ?: "未知错误")
        }
    }

    suspend fun register(username: String, password: String, email: String? = null): RegisterResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("username", username)
                put("password", password)
                email?.let { put("email", it) }
            }
            val response = apiService.post("/auth/register", json.toString())
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    RegisterResult(success = true, message = obj.optString("data", "注册成功"))
                } else {
                    RegisterResult(success = false, error = obj.optString("message", "注册失败"))
                }
            } ?: RegisterResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            RegisterResult(success = false, error = e.message ?: "未知错误")
        }
    }

    suspend fun deleteAccount(token: String): DeleteAccountResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.delete("/auth/account", token)
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    DeleteAccountResult(success = true, message = obj.optString("data", "账户注销成功"))
                } else {
                    DeleteAccountResult(success = false, error = obj.optString("message", "账户注销失败"))
                }
            } ?: DeleteAccountResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            DeleteAccountResult(success = false, error = e.message ?: "未知错误")
        }
    }

    suspend fun refreshToken(refreshToken: String): RefreshResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("refreshToken", refreshToken)
            }
            val response = apiService.post("/auth/refresh", json.toString())
            response.getOrNull()?.let { jsonString ->
                val obj = JSONObject(jsonString)
                val code = obj.optInt("code", 500)
                if (code == 200) {
                    val data = obj.optJSONObject("data")
                    val userId = data?.optLong("userId", -1L)?.takeIf { it != -1L }
                    RefreshResult(
                        success = true,
                        token = data?.optString("token", ""),
                        refreshToken = data?.optString("refreshToken", ""),
                        username = data?.optString("username", ""),
                        userId = userId
                    )
                } else {
                    RefreshResult(success = false, error = obj.optString("message", "刷新令牌失败"))
                }
            } ?: RefreshResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            RefreshResult(success = false, error = e.message ?: "未知错误")
        }
    }

    suspend fun exportData(token: String): ExportResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBytes("/auth/export/file", token)
            response.getOrNull()?.let { bytes ->
                val timestamp = System.currentTimeMillis()
                val file = java.io.File(
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                    "releasely_cloud_backup_$timestamp.json"
                )
                file.writeBytes(bytes)
                ExportResult(success = true, filePath = file.absolutePath)
            } ?: ExportResult(success = false, error = "网络错误")
        } catch (e: Exception) {
            ExportResult(success = false, error = e.message ?: "未知错误")
        }
    }
}

data class ExportResult(
    val success: Boolean,
    val filePath: String? = null,
    val error: String? = null
)
