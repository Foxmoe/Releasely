package top.foxmoe.releasely.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider {

    private val key: SecretKey = Jwts.SIG.HS256.key().build()
    private val validityInMilliseconds: Long = 3600000 // 1h
    private val preAuthValidityInMilliseconds: Long = 300000 // 5min

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
