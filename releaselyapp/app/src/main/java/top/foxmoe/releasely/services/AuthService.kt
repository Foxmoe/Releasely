package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class LoginResult(
    val success: Boolean,
    val token: String? = null,
    val preAuthToken: String? = null,
    val requires2FA: Boolean = false,
    val username: String = "",
    val error: String? = null
)

data class RegisterResult(
    val success: Boolean,
    val message: String = "",
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
                    if (requires2FA) {
                        LoginResult(
                            success = true,
                            preAuthToken = data?.optString("preAuthToken", ""),
                            requires2FA = true,
                            username = data?.optString("username", username) ?: username
                        )
                    } else {
                        LoginResult(
                            success = true,
                            token = data?.optString("token", ""),
                            username = data?.optString("username", username) ?: username
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
                    LoginResult(
                        success = true,
                        token = data?.optString("token", ""),
                        username = data?.optString("username", "") ?: ""
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
}
