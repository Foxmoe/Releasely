package top.foxmoe.releasely.services

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Cache
import org.json.JSONObject
import top.foxmoe.releasely.BuildConfig
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.concurrent.TimeUnit

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

/**
 * Token自动刷新拦截器
 *
 * 当API返回401时，自动使用refreshToken获取新token并重试原请求
 * 一次性使用：每次刷新后，旧refreshToken失效
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    companion object {
        private const val AUTH_PREFS = "auth"
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val API_BASE_URL = BuildConfig.API_BASE_URL
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 公开端点不需要token
        if (originalRequest.url.encodedPath.contains("/api/auth/")) {
            return chain.proceed(originalRequest)
        }

        // 先尝试原请求
        val response = chain.proceed(originalRequest)

        // 如果不是401，直接返回
        if (response.code != 401) {
            return response
        }

        response.close()

        // 尝试刷新token
        val refreshToken = getRefreshToken() ?: return chain.proceed(originalRequest)

        val newTokens = runBlocking { refreshAccessToken(refreshToken) }

        if (newTokens == null) {
            // 刷新失败，使用原token重试（可能token被其他设备刷新了）
            return chain.proceed(originalRequest)
        }

        // 保存新token
        saveTokens(newTokens.first, newTokens.second)

        // 使用新token重试原请求
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${newTokens.first}")
            .build()

        return chain.proceed(newRequest)
    }

    private fun getRefreshToken(): String? {
        val prefs = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    private fun saveTokens(token: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    private suspend fun refreshAccessToken(refreshToken: String): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("refreshToken", refreshToken)
                }

                val request = Request.Builder()
                    .url("$API_BASE_URL/api/auth/refresh")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val obj = JSONObject(body)
                    val code = obj.optInt("code", 500)
                    if (code == 200) {
                        val data = obj.optJSONObject("data")
                        val newToken = data?.optString("token", "") ?: ""
                        val newRefreshToken = data?.optString("refreshToken", "") ?: ""
                        if (newToken.isNotEmpty() && newRefreshToken.isNotEmpty()) {
                            return@withContext Pair(newToken, newRefreshToken)
                        }
                    }
                }
                null
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * API 请求签名拦截器
 *
 * 签名算法：HMAC-SHA256(method + path + timestamp + body_md5, secret_key)
 * 添加以下请求头：
 * - X-Signature: HMAC-SHA256 签名
 * - X-Timestamp: 当前时间戳（毫秒）
 *
 * 公开端点（/api/auth/**）不添加签名头
 */
class SignatureInterceptor(private val signatureKey: String) : Interceptor {

    companion object {
        private const val HEADER_SIGNATURE = "X-Signature"
        private const val HEADER_TIMESTAMP = "X-Timestamp"
        private const val HMAC_ALGORITHM = "HmacSHA256"
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // 公开端点不需要签名
        if (path.contains("/api/auth/")) {
            return chain.proceed(originalRequest)
        }

        val timestamp = System.currentTimeMillis().toString()
        val method = originalRequest.method
        val body = originalRequest.body?.let { bodyToString(it) } ?: ""

        val signature = computeSignature(method, path, timestamp, body, signatureKey)

        val signedRequest = originalRequest.newBuilder()
            .addHeader(HEADER_TIMESTAMP, timestamp)
            .addHeader(HEADER_SIGNATURE, signature)
            .build()

        return chain.proceed(signedRequest)
    }

    private fun bodyToString(body: okhttp3.RequestBody): String {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        return buffer.readString(StandardCharsets.UTF_8)
    }

    private fun computeSignature(
        method: String,
        path: String,
        timestamp: String,
        body: String,
        key: String
    ): String {
        val bodyMd5 = if (body.isNotEmpty()) {
            md5(body.toByteArray(StandardCharsets.UTF_8))
        } else {
            md5(ByteArray(0))
        }

        val dataToSign = "$method$path$timestamp$bodyMd5"

        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM)
        mac.init(secretKeySpec)

        val hmacBytes = mac.doFinal(dataToSign.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(hmacBytes)
    }

    private fun md5(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return bytesToHex(md.digest(data))
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class ApiService private constructor(
    context: Context,
    private val signatureKey: String
) {

    companion object {
        private const val PREFS_NAME = "api_settings"
        private const val KEY_SIGNATURE_KEY = "signature_key"
        // 开发环境默认密钥，生产环境应从安全存储获取
        private const val DEFAULT_DEV_SIGNATURE_KEY = "dev_signature_key_change_in_production"
        // HTTP 缓存配置（支持离线缓存 GET 请求）
        private const val CACHE_DIR_NAME = "http_cache"
        private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024 // 10 MB

        @Volatile
        private var instance: ApiService? = null

        fun getInstance(context: Context, signatureKey: String? = null): ApiService {
            return instance ?: synchronized(this) {
                instance ?: ApiService(
                    context.applicationContext,
                    signatureKey ?: getStoredSignatureKey(context) ?: DEFAULT_DEV_SIGNATURE_KEY
                ).also { instance = it }
            }
        }

        private fun getStoredSignatureKey(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_SIGNATURE_KEY, null)
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

    /**
     * 存储签名密钥到安全存储
     * 生产环境应使用 EncryptedSharedPreferences 或 Android Keystore
     */
    fun setSignatureKey(key: String) {
        prefs.edit().putString(KEY_SIGNATURE_KEY, key).apply()
    }

    /**
     * 证书固定配置（Certificate Pinning）
     *
     * 生产环境启用，防止中间人攻击。使用 OkHttp 的 CertificatePinner 固定服务端证书公钥。
     *
     * 如何生成真实指纹：
     * 1. 使用 openssl 从证书提取公钥并生成 SHA-256 指纹：
     *    openssl s_client -connect your-domain.com:443 -servername your-domain.com </dev/null 2>/dev/null | \
     *      openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | \
     *      openssl dgst -sha256 -binary | openssl enc -base64
     *
     * 2. 或者使用 okhttppins 工具：
     *    https://github.com/sparkle-project/okhttppins
     *
     * 3. 建议同时固定备用证书（中间证书或备份 CA），避免主证书轮换导致 App 无法连接。
     *
     * 注意：占位符 sha256/AAAAAAAA... 仅用于占位，发布前必须替换为真实指纹。
     */
    private fun createCertificatePinner(): CertificatePinner? {
        if (!BuildConfig.CERT_PINNING_ENABLED) return null
        if (BuildConfig.CERT_PIN.isBlank()) return null

        // 从 BuildConfig 读取生产域名和指纹
        // 生产域名应与 API_BASE_URL 的 host 保持一致
        val hostname = BuildConfig.API_BASE_URL
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore(":")

        return CertificatePinner.Builder()
            .add(hostname, BuildConfig.CERT_PIN)
            // TODO: 添加备用/中间证书指纹，防止主证书轮换导致断连
            // .add(hostname, "sha256/BACKUP_CERTIFICATE_PIN_HERE")
            .build()
    }

    // 创建签名拦截器
    private val signatureInterceptor = SignatureInterceptor(signatureKey)
    // 创建Token自动刷新拦截器
    private val authInterceptor = AuthInterceptor(appContext)
    // 缓存拦截器：支持离线时从缓存读取 GET 响应
    private val cacheInterceptor = Interceptor { chain ->
        val request = chain.request()
        // 仅对 GET 请求启用缓存
        if (request.method == "GET") {
            val response = chain.proceed(request)
            // 有网络时缓存响应，无网络时从缓存读取
            val cacheControl = if (isNetworkAvailable()) {
                okhttp3.CacheControl.Builder()
                    .maxAge(5, java.util.concurrent.TimeUnit.MINUTES)
                    .build()
            } else {
                okhttp3.CacheControl.Builder()
                    .maxStale(7, java.util.concurrent.TimeUnit.DAYS)
                    .onlyIfCached()
                    .build()
            }
            response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        } else {
            chain.proceed(request)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val clientBuilder = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(Cache(File(appContext.cacheDir, CACHE_DIR_NAME), CACHE_SIZE_BYTES))
        .apply {
            // 开发环境（debug）允许明文 HTTP（Android 9+ 默认禁止明文传输）
            if (BuildConfig.DEBUG) {
                // 已在 AndroidManifest 中配置 android:usesCleartextTraffic="true"
                // 或 network_security_config.xml 中配置域名校验
            }
            // 添加证书固定
            createCertificatePinner()?.let { certificatePinner ->
                certificatePinner(certificatePinner)
            }
            // 添加缓存拦截器
            addInterceptor(cacheInterceptor)
            // 添加签名拦截器
            addInterceptor(signatureInterceptor)
            // 添加Token自动刷新拦截器
            addInterceptor(authInterceptor)
        }

    private val client = clientBuilder.build()

    // baseUrl 通过 BuildConfig 根据 buildType 自动切换：
    //   debug   -> http://10.0.2.2:8080/api  (开发/模拟器)
    //   release -> https://api.releasely.example.com/api  (生产)
    private val baseUrl = BuildConfig.API_BASE_URL
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    suspend fun get(endpoint: String, token: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .apply {
                        token?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun post(endpoint: String, json: String, token: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .apply {
                        token?.let { addHeader("Authorization", "Bearer $it") }
                        addHeader("Content-Type", "application/json")
                    }
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun put(endpoint: String, json: String, token: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .apply {
                        token?.let { addHeader("Authorization", "Bearer $it") }
                        addHeader("Content-Type", "application/json")
                    }
                    .put(json.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun delete(endpoint: String, token: String? = null): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .apply {
                        token?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getBytes(endpoint: String, token: String? = null): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .apply {
                        token?.let { addHeader("Authorization", "Bearer $it") }
                    }
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Result.success(response.body?.bytes() ?: ByteArray(0))
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
