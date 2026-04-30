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
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.service.DashboardService
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class DashboardControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var dashboardService: DashboardService

    @InjectMocks
    private lateinit var dashboardController: DashboardController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `getDashboard should return dashboard data for user`() {
        val stats = DashboardService.DashboardStats(
            totalActivities = 10,
            totalCycles = 5,
            activeMedications = 2,
            partnerCount = 1
        )
        val activities = listOf(
            ActivityRecord(
                id = 1L,
                userId = testUserId,
                type = "MASTURBATION",
                protection = "NONE",
                pleasureRating = 5,
                healthStatus = "GOOD",
                occurredAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now()
            )
        )
        val prediction = DashboardService.CyclePrediction(
            predictedNext = LocalDate.of(2024, 2, 1),
            averageCycleLength = 28,
            daysUntilNext = 14
        )

        `when`(dashboardService.getStats(testUserId)).thenReturn(stats)
        `when`(dashboardService.getRecentActivities(testUserId)).thenReturn(activities)
        `when`(dashboardService.getCyclePrediction(testUserId)).thenReturn(prediction)

        val result = mockMvc.perform(get("/api/dashboard")
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
    fun `getDashboard should return empty recent activities when no data`() {
        val stats = DashboardService.DashboardStats(
            totalActivities = 0,
            totalCycles = 0,
            activeMedications = 0,
            partnerCount = 0
        )

        `when`(dashboardService.getStats(testUserId)).thenReturn(stats)
        `when`(dashboardService.getRecentActivities(testUserId)).thenReturn(emptyList())
        `when`(dashboardService.getCyclePrediction(testUserId)).thenReturn(null)

        val result = mockMvc.perform(get("/api/dashboard")
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
    fun `getStats should return dashboard stats for user`() {
        val stats = DashboardService.DashboardStats(
            totalActivities = 20,
            totalCycles = 10,
            activeMedications = 3,
            partnerCount = 2
        )

        `when`(dashboardService.getStats(testUserId)).thenReturn(stats)
        `when`(dashboardService.getRecentActivities(testUserId)).thenReturn(emptyList())
        `when`(dashboardService.getCyclePrediction(testUserId)).thenReturn(null)

        val result = mockMvc.perform(get("/api/dashboard/stats")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
