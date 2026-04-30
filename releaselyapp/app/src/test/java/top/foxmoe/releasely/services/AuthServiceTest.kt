package top.foxmoe.releasely.services

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AuthService.
 * Uses MockK to mock the ApiService dependency.
 */
class AuthServiceTest {

    private lateinit var mockApiService: ApiService
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        mockApiService = mockk(relaxed = true)
        authService = AuthService(mockApiService)
    }

    @Test
    fun `login returns success with token when credentials are valid`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "登录成功",
                "data": {
                    "token": "test-jwt-token-123",
                    "username": "testuser",
                    "userId": 42
                }
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/login", any()) } returns Result.success(jsonResponse)

        val result = authService.login("testuser", "password123")

        assertTrue(result.success)
        assertEquals("test-jwt-token-123", result.token)
        assertEquals("testuser", result.username)
        assertEquals(42L, result.userId)
        assertFalse(result.requires2FA)
        coVerify { mockApiService.post("/auth/login", any()) }
    }

    @Test
    fun `login returns requires2FA when 2FA is enabled`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "需要两步验证",
                "data": {
                    "requires2FA": true,
                    "preAuthToken": "pre-auth-token-456",
                    "username": "testuser",
                    "userId": 42
                }
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/login", any()) } returns Result.success(jsonResponse)

        val result = authService.login("testuser", "password123")

        assertTrue(result.success)
        assertTrue(result.requires2FA)
        assertEquals("pre-auth-token-456", result.preAuthToken)
        assertNull(result.token)
        assertEquals("testuser", result.username)
    }

    @Test
    fun `login returns error when credentials are invalid`() = runTest {
        val jsonResponse = """
            {
                "code": 401,
                "message": "用户名或密码错误",
                "data": null
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/login", any()) } returns Result.success(jsonResponse)

        val result = authService.login("wronguser", "wrongpassword")

        assertFalse(result.success)
        assertEquals("用户名或密码错误", result.error)
        assertNull(result.token)
    }

    @Test
    fun `login returns error on network failure`() = runTest {
        coEvery { mockApiService.post("/auth/login", any()) } returns Result.failure(Exception("Network error"))

        val result = authService.login("testuser", "password123")

        assertFalse(result.success)
        assertEquals("Network error", result.error)
    }

    @Test
    fun `login returns error on malformed response`() = runTest {
        coEvery { mockApiService.post("/auth/login", any()) } returns Result.success("not json")

        val result = authService.login("testuser", "password123")

        assertFalse(result.success)
        assertTrue(result.error != null)
    }

    @Test
    fun `register returns success when registration is successful`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "注册成功",
                "data": "注册成功"
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/register", any()) } returns Result.success(jsonResponse)

        val result = authService.register("newuser", "newpassword123", "newuser@example.com")

        assertTrue(result.success)
        assertEquals("注册成功", result.message)
        coVerify { mockApiService.post("/auth/register", any()) }
    }

    @Test
    fun `register returns error when username exists`() = runTest {
        val jsonResponse = """
            {
                "code": 409,
                "message": "用户名已存在",
                "data": null
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/register", any()) } returns Result.success(jsonResponse)

        val result = authService.register("existinguser", "password123")

        assertFalse(result.success)
        assertEquals("用户名已存在", result.error)
    }

    @Test
    fun `register handles missing email`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "注册成功",
                "data": "注册成功"
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/register", any()) } returns Result.success(jsonResponse)

        val result = authService.register("newuser", "password123")

        assertTrue(result.success)
        // Verify email is not in the request body when null
        val capturedJson = slot<String>()
        coVerify { mockApiService.post("/auth/register", capture(capturedJson)) }
        assertFalse(capturedJson.captured.contains("\"email\""))
    }

    @Test
    fun `verify2FA returns success with token on valid code`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "验证成功",
                "data": {
                    "token": "post-2fa-token-789",
                    "username": "testuser",
                    "userId": 42
                }
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/2fa/verify", any()) } returns Result.success(jsonResponse)

        val result = authService.verify2FA("pre-auth-token", "123456")

        assertTrue(result.success)
        assertEquals("post-2fa-token-789", result.token)
        assertEquals("testuser", result.username)
        assertEquals(42L, result.userId)
    }

    @Test
    fun `verify2FA returns error on invalid code`() = runTest {
        val jsonResponse = """
            {
                "code": 401,
                "message": "验证码错误或已过期",
                "data": null
            }
        """.trimIndent()

        coEvery { mockApiService.post("/auth/2fa/verify", any()) } returns Result.success(jsonResponse)

        val result = authService.verify2FA("pre-auth-token", "000000")

        assertFalse(result.success)
        assertEquals("验证码错误或已过期", result.error)
    }

    @Test
    fun `deleteAccount returns success when account is deleted`() = runTest {
        val jsonResponse = """
            {
                "code": 200,
                "message": "账户注销成功",
                "data": "账户注销成功"
            }
        """.trimIndent()

        coEvery { mockApiService.delete("/auth/account", "valid-token") } returns Result.success(jsonResponse)

        val result = authService.deleteAccount("valid-token")

        assertTrue(result.success)
        assertEquals("账户注销成功", result.message)
        coVerify { mockApiService.delete("/auth/account", "valid-token") }
    }

    @Test
    fun `deleteAccount returns error on unauthorized`() = runTest {
        val jsonResponse = """
            {
                "code": 401,
                "message": "无效的认证令牌",
                "data": null
            }
        """.trimIndent()

        coEvery { mockApiService.delete("/auth/account", "invalid-token") } returns Result.success(jsonResponse)

        val result = authService.deleteAccount("invalid-token")

        assertFalse(result.success)
        assertEquals("无效的认证令牌", result.error)
    }

    @Test
    fun `deleteAccount returns error on network failure`() = runTest {
        coEvery { mockApiService.delete("/auth/account", any()) } returns Result.failure(Exception("Connection refused"))

        val result = authService.deleteAccount("some-token")

        assertFalse(result.success)
        assertEquals("Connection refused", result.error)
    }
}