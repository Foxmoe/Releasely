package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.RefreshToken
import top.foxmoe.releasely.entity.User
import top.foxmoe.releasely.mapper.RefreshTokenMapper
import top.foxmoe.releasely.mapper.UserMapper
import top.foxmoe.releasely.security.JwtTokenProvider
import top.foxmoe.releasely.service.AccountDeletionService
import top.foxmoe.releasely.service.AuditService
import top.foxmoe.releasely.service.SecurityService
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val securityService: SecurityService,
    private val auditService: AuditService,
    private val accountDeletionService: AccountDeletionService,
    private val refreshTokenMapper: RefreshTokenMapper
) {

    private fun createRefreshTokenRecord(userId: Long, refreshToken: String) {
        val expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
        val refreshTokenRecord = RefreshToken(
            userId = userId,
            tokenHash = JwtTokenProvider.hashToken(refreshToken),
            expiresAt = expiresAt,
            revoked = false,
            createdAt = LocalDateTime.now()
        )
        refreshTokenMapper.insert(refreshTokenRecord)
    }

    private fun revokeAllUserRefreshTokens(userId: Long) {
        val tokens = refreshTokenMapper.selectList(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefreshToken>()
                .eq("user_id", userId)
                .eq("revoked", false)
        )
        tokens.forEach { token ->
            token.revoked = true
            refreshTokenMapper.updateById(token)
        }
    }

    @AuditLog(action = "LOGIN", resourceType = "AUTH")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
            SecurityContextHolder.getContext().authentication = authentication

            val user = userMapper.selectByMap(mapOf("username" to request.username)).firstOrNull()
                ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

            val settings = securityService.getOrCreateSettings(user.id!!)
            if (settings.is2FAEnabled) {
                val preAuthToken = jwtTokenProvider.createPreAuthToken(request.username)
                return ResponseEntity.ok(ApiResponse.success(
                    AuthResponse(token = preAuthToken, username = request.username, requires2FA = true)
                ))
            }

            val token = jwtTokenProvider.createToken(request.username)
            val refreshToken = jwtTokenProvider.createRefreshToken(request.username)
            createRefreshTokenRecord(user.id!!, refreshToken)

            ResponseEntity.ok(ApiResponse.success(AuthResponse(token = token, username = user.username ?: request.username, userId = user.id, refreshToken = refreshToken)))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse.error(ResultCode.PASSWORD_ERROR.code, ResultCode.PASSWORD_ERROR.message))
        }
    }

    @AuditLog(action = "2FA_VERIFY", resourceType = "AUTH")
    @PostMapping("/2fa/verify")
    fun verify2FA(@RequestBody request: TwoFactorLoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        if (!jwtTokenProvider.validateToken(request.preAuthToken)) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_INVALID.code, ResultCode.TOKEN_INVALID.message))
        }
        if (!jwtTokenProvider.isPreAuthToken(request.preAuthToken)) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_INVALID.code, ResultCode.TOKEN_INVALID.message))
        }

        val username = jwtTokenProvider.getUsername(request.preAuthToken)
        val user = userMapper.selectByMap(mapOf("username" to username)).firstOrNull()
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val settings = securityService.getOrCreateSettings(user.id!!)
        if (!settings.is2FAEnabled || securityService.verify2FA(user.id!!, request.totpCode)) {
            val token = jwtTokenProvider.createToken(username)
            val refreshToken = jwtTokenProvider.createRefreshToken(username)
            createRefreshTokenRecord(user.id!!, refreshToken)
            return ResponseEntity.ok(ApiResponse.success(AuthResponse(token = token, username = username, userId = user.id, refreshToken = refreshToken)))
        }

        return ResponseEntity.ok(ApiResponse.error(ResultCode.`2FA_REQUIRED`))
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        // Validate refresh token format
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_INVALID.code, ResultCode.TOKEN_INVALID.message))
        }
        if (!jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_INVALID.code, ResultCode.TOKEN_INVALID.message))
        }

        val username = jwtTokenProvider.getUsernameFromRefreshToken(request.refreshToken)
        val user = userMapper.selectByMap(mapOf("username" to username)).firstOrNull()
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        // Verify refresh token exists and is not revoked
        val tokenHash = JwtTokenProvider.hashToken(request.refreshToken)
        val storedToken = refreshTokenMapper.selectList(
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RefreshToken>()
                .eq("user_id", user.id)
                .eq("token_hash", tokenHash)
                .eq("revoked", false)
        ).firstOrNull()

        if (storedToken == null) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_INVALID.code, ResultCode.TOKEN_INVALID.message))
        }

        // Check if token is expired
        if (storedToken.expiresAt?.isBefore(LocalDateTime.now()) == true) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.TOKEN_EXPIRED.code, ResultCode.TOKEN_EXPIRED.message))
        }

        // Revoke the old refresh token (one-time use)
        storedToken.revoked = true
        refreshTokenMapper.updateById(storedToken)

        // Issue new tokens
        val newAccessToken = jwtTokenProvider.createToken(username)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(username)
        createRefreshTokenRecord(user.id!!, newRefreshToken)

        return ResponseEntity.ok(ApiResponse.success(AuthResponse(token = newAccessToken, username = username, userId = user.id, refreshToken = newRefreshToken)))
    }

    @AuditLog(action = "REGISTER", resourceType = "AUTH")
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<String>> {
        if (userMapper.selectByMap(mapOf("username" to request.username)).isNotEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.USERNAME_EXISTS.code, ResultCode.USERNAME_EXISTS.message))
        }

        val user = User(
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            email = request.email,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userMapper.insert(user)
        securityService.createDefaultSettings(user.id!!)
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"))
    }

    @AuditLog(action = "DELETE_ACCOUNT", resourceType = "AUTH")
    @DeleteMapping("/account")
    fun deleteAccount(): ResponseEntity<ApiResponse<String>> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.UNAUTHORIZED.code, ResultCode.UNAUTHORIZED.message))
        }

        val username = authentication.name
        val user = userMapper.selectByMap(mapOf("username" to username)).firstOrNull()
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val result = accountDeletionService.deleteAccount(user.id!!)
        return if (result.success) {
            ResponseEntity.ok(ApiResponse.success(result.message))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR.code, result.message))
        }
    }
}
