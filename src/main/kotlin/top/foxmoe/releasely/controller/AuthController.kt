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
import top.foxmoe.releasely.dto.AuthResponse
import top.foxmoe.releasely.dto.LoginRequest
import top.foxmoe.releasely.dto.RegisterRequest
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
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        val token = jwtTokenProvider.createToken(request.username)
        return ResponseEntity.ok(AuthResponse(token, request.username))
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<String> {
        if (userMapper.selectByMap(mapOf("username" to request.username)).isNotEmpty()) {
            return ResponseEntity.badRequest().body("Username already exists")
        }

        val user = User(
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            email = request.email,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        userMapper.insert(user)
        return ResponseEntity.ok("User registered successfully")
    }
}

