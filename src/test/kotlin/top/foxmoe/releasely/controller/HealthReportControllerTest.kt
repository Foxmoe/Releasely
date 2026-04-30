package top.foxmoe.releasely.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import top.foxmoe.releasely.dto.ApiResponse
import top.foxmoe.releasely.dto.ResultCode
import top.foxmoe.releasely.entity.HealthReport
import top.foxmoe.releasely.service.HealthReportService
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class HealthReportControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var healthReportService: HealthReportService

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @InjectMocks
    private lateinit var healthReportController: HealthReportController

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthReportController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `getReports should return list of reports for user`() {
        val testUserId = 1L
        val reports = listOf(
            HealthReport(id = 1L, userId = testUserId, period = "weekly")
        )
        `when`(healthReportService.getReportsByUserId(testUserId)).thenReturn(reports)

        val result = mockMvc.perform(get("/api/health-reports")
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
    fun `getReportById should return report when exists`() {
        val testUserId = 1L
        val report = HealthReport(id = 1L, userId = testUserId, period = "weekly")
        `when`(healthReportService.getReportById(1L)).thenReturn(report)

        val result = mockMvc.perform(get("/api/health-reports/1"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `getReportById should return NOT_FOUND when report does not exist`() {
        `when`(healthReportService.getReportById(999L)).thenReturn(null)

        val result = mockMvc.perform(get("/api/health-reports/999"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.NOT_FOUND.code, response.code)
    }

    @Test
    fun `deleteReport should return success when report is deleted`() {
        `when`(healthReportService.deleteReport(1L)).thenReturn(true)

        val result = mockMvc.perform(delete("/api/health-reports/1"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `deleteReport should return NOT_FOUND when report does not exist`() {
        `when`(healthReportService.deleteReport(999L)).thenReturn(false)

        val result = mockMvc.perform(delete("/api/health-reports/999"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.NOT_FOUND.code, response.code)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
