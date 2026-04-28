package top.foxmoe.releasely.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class TotpService {

    companion object {
        private const val TIME_STEP = 30L
        private const val CODE_DIGITS = 6
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private const val HMAC_ALGORITHM = "HmacSHA1"
        private const val SECRET_LENGTH = 20
    }

    fun generateSecret(): String {
        val random = SecureRandom()
        val bytes = ByteArray(SECRET_LENGTH)
        random.nextBytes(bytes)
        return base32Encode(bytes)
    }

    fun verifyCode(secret: String, code: String, window: Int = 1): Boolean {
        if (code.length != CODE_DIGITS) return false
        val currentTime = System.currentTimeMillis() / 1000
        for (i in -window..window) {
            val counter = (currentTime / TIME_STEP) + i
            if (generateTotp(secret, counter) == code) {
                return true
            }
        }
        return false
    }

    fun generateQrCodeUrl(username: String, secret: String, issuer: String = "Releasely"): String {
        val encodedIssuer = java.net.URLEncoder.encode(issuer, "UTF-8")
        val encodedAccount = java.net.URLEncoder.encode(username, "UTF-8")
        return "otpauth://totp/$encodedIssuer:$encodedAccount?secret=$secret&issuer=$encodedIssuer"
    }

    private fun generateTotp(secret: String, counter: Long): String {
        val key = base32Decode(secret)
        val data = ByteArray(8) { i ->
            ((counter shr (56 - i * 8)) and 0xFF).toByte()
        }
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        val otp = binary % 1_000_000
        return otp.toString().padStart(CODE_DIGITS, '0')
    }

    private fun base32Encode(data: ByteArray): String {
        val result = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (byte in data) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                bitsLeft -= 5
                result.append(ALPHABET[(buffer shr bitsLeft) and 0x1F])
            }
        }
        if (bitsLeft > 0) {
            result.append(ALPHABET[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        return result.toString()
    }

    private fun base32Decode(input: String): ByteArray {
        val cleaned = input.uppercase().filter { it != '=' }
        var buffer = 0
        var bitsLeft = 0
        val result = mutableListOf<Byte>()
        for (char in cleaned) {
            val value = ALPHABET.indexOf(char)
            if (value == -1) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                result.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return result.toByteArray()
    }
}
