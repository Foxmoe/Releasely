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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.SecuritySettings
import top.foxmoe.releasely.service.SecurityService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class SecurityControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var securityService: SecurityService

    @InjectMocks
    private lateinit var securityController: SecurityController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(securityController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `getSettings should return security settings for user`() {
        val settings = SecuritySettings(
            id = 1L,
            userId = testUserId,
            lockType = "PIN",
            isAppLockEnabled = true,
            isDisguiseEnabled = false,
            isScreenshotProtected = true,
            is2FAEnabled = false
        )
        `when`(securityService.getOrCreateSettings(testUserId)).thenReturn(settings)

        val result = mockMvc.perform(get("/api/security/settings")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `setPin should return success when pin is set`() {
        `when`(securityService.setPin(testUserId, "1234")).thenReturn(true)

        val request = SetPinRequest(userId = testUserId, pin = "1234")
        val result = mockMvc.perform(post("/api/security/pin/set")
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
    fun `setPin should return error when setting pin fails`() {
        `when`(securityService.setPin(testUserId, "1234")).thenReturn(false)

        val request = SetPinRequest(userId = testUserId, pin = "1234")
        val result = mockMvc.perform(post("/api/security/pin/set")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.INTERNAL_ERROR.code, response.code)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
