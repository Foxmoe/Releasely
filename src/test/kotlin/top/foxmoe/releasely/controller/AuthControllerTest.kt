package top.foxmoe.releasely.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.SecuritySettings
import top.foxmoe.releasely.entity.User
import top.foxmoe.releasely.mapper.UserMapper
import top.foxmoe.releasely.security.JwtTokenProvider
import top.foxmoe.releasely.service.SecurityService
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var userMapper: UserMapper

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    private lateinit var securityService: SecurityService

    @Mock
    private lateinit var auditService: top.foxmoe.releasely.service.AuditService

    @Mock
    private lateinit var accountDeletionService: top.foxmoe.releasely.service.AccountDeletionService

    @Mock
    private lateinit var refreshTokenMapper: top.foxmoe.releasely.mapper.RefreshTokenMapper

    @Mock
    private lateinit var authentication: Authentication

    @InjectMocks
    private lateinit var authController: AuthController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L
    private val testUsername = "testuser"
    private val testPassword = "password123"

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `login should return token when credentials are valid`() {
        val request = LoginRequest(username = testUsername, password = testPassword)
        val user = User(id = testUserId, username = testUsername, passwordHash = "hashed")
        val settings = SecuritySettings(userId = testUserId, is2FAEnabled = false)

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(listOf(user))
        `when`(securityService.getOrCreateSettings(testUserId)).thenReturn(settings)
        `when`(jwtTokenProvider.createToken(testUsername)).thenReturn("jwt_token_123")
        `when`(jwtTokenProvider.createRefreshToken(testUsername)).thenReturn("refresh_token_123")
        `when`(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L)

        val result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `login should return pre-auth token when 2FA is enabled`() {
        val request = LoginRequest(username = testUsername, password = testPassword)
        val user = User(id = testUserId, username = testUsername, passwordHash = "hashed")
        val settings = SecuritySettings(userId = testUserId, is2FAEnabled = true)

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(listOf(user))
        `when`(securityService.getOrCreateSettings(testUserId)).thenReturn(settings)
        `when`(jwtTokenProvider.createPreAuthToken(testUsername)).thenReturn("pre_auth_token_123")

        val result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `login should return error when user not found`() {
        val request = LoginRequest(username = testUsername, password = testPassword)

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(authentication)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(emptyList())

        val result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.USER_NOT_FOUND.code, response.code)
    }

    @Test
    fun `login should return error when authentication fails`() {
        val request = LoginRequest(username = testUsername, password = testPassword)

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenThrow(RuntimeException("Bad credentials"))

        val result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.PASSWORD_ERROR.code, response.code)
    }

    @Test
    fun `register should create user when username is available`() {
        val request = RegisterRequest(username = testUsername, password = testPassword, email = "test@example.com")

        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(emptyList())
        `when`(passwordEncoder.encode(testPassword)).thenReturn("encoded_password")
        `when`(userMapper.insert(any(User::class.java))).thenAnswer { invocation ->
            val user = invocation.getArgument<User>(0)
            user.id = testUserId
            1
        }
        `when`(securityService.createDefaultSettings(testUserId)).thenReturn(SecuritySettings())

        val result = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `register should return error when username already exists`() {
        val request = RegisterRequest(username = testUsername, password = testPassword)
        val existingUser = User(id = 2L, username = testUsername)

        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(listOf(existingUser))

        val result = mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.USERNAME_EXISTS.code, response.code)
    }

    @Test
    fun `verify2FA should return token when pre-auth token and code are valid`() {
        val request = TwoFactorLoginRequest(preAuthToken = "pre_auth_token", totpCode = "123456")
        val user = User(id = testUserId, username = testUsername)
        val settings = SecuritySettings(userId = testUserId, is2FAEnabled = true)

        `when`(jwtTokenProvider.validateToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.isPreAuthToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUsername(request.preAuthToken)).thenReturn(testUsername)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(listOf(user))
        `when`(securityService.getOrCreateSettings(testUserId)).thenReturn(settings)
        `when`(securityService.verify2FA(testUserId, request.totpCode)).thenReturn(true)
        `when`(jwtTokenProvider.createToken(testUsername)).thenReturn("final_jwt_token")
        `when`(jwtTokenProvider.createRefreshToken(testUsername)).thenReturn("final_refresh_token")
        `when`(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L)
        `when`(refreshTokenMapper.insert(any(top.foxmoe.releasely.entity.RefreshToken::class.java))).thenReturn(1)

        val result = mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `verify2FA should return error when token is invalid`() {
        val request = TwoFactorLoginRequest(preAuthToken = "invalid_token", totpCode = "123456")

        `when`(jwtTokenProvider.validateToken(request.preAuthToken)).thenReturn(false)

        val result = mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.TOKEN_INVALID.code, response.code)
    }

    @Test
    fun `verify2FA should return error when token is not pre-auth token`() {
        val request = TwoFactorLoginRequest(preAuthToken = "regular_token", totpCode = "123456")

        `when`(jwtTokenProvider.validateToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.isPreAuthToken(request.preAuthToken)).thenReturn(false)

        val result = mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.TOKEN_INVALID.code, response.code)
    }

    @Test
    fun `verify2FA should return error when user not found`() {
        val request = TwoFactorLoginRequest(preAuthToken = "pre_auth_token", totpCode = "123456")

        `when`(jwtTokenProvider.validateToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.isPreAuthToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUsername(request.preAuthToken)).thenReturn(testUsername)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(emptyList())

        val result = mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.USER_NOT_FOUND.code, response.code)
    }

    @Test
    fun `verify2FA should return error when 2FA code is incorrect`() {
        val request = TwoFactorLoginRequest(preAuthToken = "pre_auth_token", totpCode = "wrong_code")
        val user = User(id = testUserId, username = testUsername)
        val settings = SecuritySettings(userId = testUserId, is2FAEnabled = true)

        `when`(jwtTokenProvider.validateToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.isPreAuthToken(request.preAuthToken)).thenReturn(true)
        `when`(jwtTokenProvider.getUsername(request.preAuthToken)).thenReturn(testUsername)
        `when`(userMapper.selectByMap(mapOf("username" to testUsername))).thenReturn(listOf(user))
        `when`(securityService.getOrCreateSettings(testUserId)).thenReturn(settings)
        `when`(securityService.verify2FA(testUserId, request.totpCode)).thenReturn(false)

        val result = mockMvc.perform(post("/api/auth/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.`2FA_REQUIRED`.code, response.code)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
