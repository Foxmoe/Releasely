package top.foxmoe.releasely.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

/**
 * API 请求签名验证过滤器
 *
 * 签名算法：HMAC-SHA256(method + path + timestamp + body_md5, secret_key)
 *
 * 防篡改机制：
 * - HMAC-SHA256 签名确保请求内容完整性
 * - Timestamp 字段防止重放攻击（允许 5 分钟误差）
 * - Body MD5 确保请求体未被修改
 */
@Component
@Order(1)
class RequestSignatureFilter(
    @Value("\${api.signature.key}") private val signatureKey: String
) : OncePerRequestFilter() {

    companion object {
        private const val HEADER_SIGNATURE = "X-Signature"
        private const val HEADER_TIMESTAMP = "X-Timestamp"
        private const val TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes
        private const val HMAC_ALGORITHM = "HmacSHA256"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        // 公开端点不需要签名验证（登录、注册等）
        return path.startsWith("/api/auth/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val signature = request.getHeader(HEADER_SIGNATURE)
        val timestampStr = request.getHeader(HEADER_TIMESTAMP)

        if (signature.isNullOrBlank() || timestampStr.isNullOrBlank()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing signature headers")
            return
        }

        // 验证时间戳防止重放攻击
        val timestamp = timestampStr.toLongOrNull()
        if (timestamp == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid timestamp format")
            return
        }

        val currentTime = System.currentTimeMillis()
        if (abs(currentTime - timestamp) > TIMESTAMP_TOLERANCE_MS) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Request timestamp expired")
            return
        }

        // 包装请求以便多次读取 body
        val wrappedRequest = CachedBodyHttpServletRequest(request)

        // 计算期望的签名
        val expectedSignature = computeSignature(
            method = wrappedRequest.method,
            path = wrappedRequest.requestURI,
            timestamp = timestampStr,
            body = wrappedRequest.getBody(),
            key = signatureKey
        )

        // 验证签名
        if (!secureCompare(signature, expectedSignature)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature")
            return
        }

        filterChain.doFilter(wrappedRequest, response)
    }

    /**
     * 计算请求签名
     * 格式：HMAC-SHA256(method + path + timestamp + body_md5, secret_key)
     */
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

    /**
     * 计算 MD5 哈希
     */
    private fun md5(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return bytesToHex(md.digest(data))
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 常数时间比较，防止时序攻击
     */
    private fun secureCompare(a: String, b: String): Boolean {
        if (a.length != b.length) return false

        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    private fun sendError(response: HttpServletResponse, status: Int, message: String) {
        response.status = status
        response.contentType = "application/json"
        response.writer.write("""{"code":$status,"message":"$message"}""")
    }
}

/**
 * 可缓存 body 的 HttpServletRequest 包装器
 * 允许在 filter 中多次读取请求体
 */
class CachedBodyHttpServletRequest(request: HttpServletRequest) :
    jakarta.servlet.http.HttpServletRequestWrapper(request) {

    private var cachedBody: String? = null

    override fun getInputStream(): jakarta.servlet.ServletInputStream {
        val body = getBody()
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        return CachedBodyServletInputStream(bytes)
    }

    override fun getReader(): java.io.BufferedReader {
        return java.io.BufferedReader(
            java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )
    }

    fun getBody(): String {
        if (cachedBody == null) {
            cachedBody = request.inputStream.bufferedReader().readText()
        }
        return cachedBody ?: ""
    }
}

/**
 * 可缓存的 ServletInputStream 实现
 */
class CachedBodyServletInputStream(private val bytes: ByteArray) :
    jakarta.servlet.ServletInputStream() {

    private val byteArrayInputStream = java.io.ByteArrayInputStream(bytes)

    override fun read(): Int = byteArrayInputStream.read()

    override fun isFinished(): Boolean = byteArrayInputStream.available() == 0

    override fun isReady(): Boolean = true

    override fun setReadListener(listener: jakarta.servlet.ReadListener?) {}
}
