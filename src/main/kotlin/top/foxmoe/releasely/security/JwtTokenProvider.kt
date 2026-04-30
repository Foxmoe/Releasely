package top.foxmoe.releasely.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    private val key: SecretKey by lazy {
        val envKey = System.getenv(ENV_JWT_SECRET_KEY)
        when {
            envKey != null && envKey.length >= MIN_KEY_LENGTH -> {
                logger.info("JWT secret key loaded from environment variable")
                Keys.hmacShaKeyFor(envKey.toByteArray(Charsets.UTF_8))
            }
            envKey != null -> {
                logger.warn("JWT_SECRET_KEY from environment is shorter than $MIN_KEY_LENGTH characters, falling back to random key")
                Jwts.SIG.HS256.key().build()
            }
            else -> {
                logger.warn("JWT_SECRET_KEY environment variable not set, using random key (tokens will be invalidated on restart)")
                Jwts.SIG.HS256.key().build()
            }
        }
    }

    companion object {
        private const val MIN_KEY_LENGTH = 32
        private const val ENV_JWT_SECRET_KEY = "JWT_SECRET_KEY"
        private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

        fun hashToken(token: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(token.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }

    private val validityInMilliseconds: Long = 3600000 // 1h
    private val preAuthValidityInMilliseconds: Long = 300000 // 5min
    private val refreshTokenValidityInMilliseconds: Long = 604800000 // 7 days

    fun createToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + refreshTokenValidityInMilliseconds)

        return Jwts.builder()
            .subject(username)
            .claim("refresh", true)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.get("refresh", Boolean::class.java) == true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsernameFromRefreshToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun getRefreshTokenExpiration(): Long {
        return refreshTokenValidityInMilliseconds
    }

    fun createPreAuthToken(username: String): String {
        val now = Date()
        val validity = Date(now.time + preAuthValidityInMilliseconds)

        return Jwts.builder()
            .subject(username)
            .claim("pre_auth", true)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun isPreAuthToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.get("pre_auth", Boolean::class.java) == true
        } catch (e: Exception) {
            false
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsername(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }


}
