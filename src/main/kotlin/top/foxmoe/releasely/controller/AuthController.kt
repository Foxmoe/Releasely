package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.User
import top.foxmoe.releasely.mapper.UserMapper
import top.foxmoe.releasely.security.JwtTokenProvider
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            val token = jwtTokenProvider.createToken(request.username)
            val user = userMapper.selectByMap(mapOf("username" to request.username)).firstOrNull()
            ResponseEntity.ok(ApiResponse.success(AuthResponse(token, user?.username ?: request.username)))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse.error(ResultCode.PASSWORD_ERROR))
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<String>> {
        if (userMapper.selectByMap(mapOf("username" to request.username)).isNotEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(ResultCode.USERNAME_EXISTS))
        }

        val user = User(
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            email = request.email,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userMapper.insert(user)
        return ResponseEntity.ok(ApiResponse.success("User registered successfully"))
    }
}

