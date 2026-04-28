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
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.service.CycleService
import java.time.LocalDate
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class CycleControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var cycleService: CycleService

    @InjectMocks
    private lateinit var cycleController: CycleController

    private lateinit var objectMapper: ObjectMapper
    private lateinit var testCycle: Cycle
    private var testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(cycleController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        testUserId = 1L
        testCycle = Cycle(
            id = 1L,
            userId = testUserId,
            startDate = LocalDate.of(2024, 1, 1),
            duration = 5
        )
    }

    @Test
    fun `getCycles should return cycles for user`() {
        val cycles = listOf(testCycle)
        `when`(cycleService.getCyclesByUserId(testUserId)).thenReturn(cycles)

        val result = mockMvc.perform(get("/api/cycles")
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
    fun `getCycleById should return cycle when exists`() {
        `when`(cycleService.getCycleById(1L)).thenReturn(testCycle)

        val result = mockMvc.perform(get("/api/cycles/1"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `getCycleById should return NOT_FOUND when cycle does not exist`() {
        `when`(cycleService.getCycleById(999L)).thenReturn(null)

        val result = mockMvc.perform(get("/api/cycles/999"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(404, response.code)
    }

    @Test
    fun `createCycle should return created cycle`() {
        val request = CreateCycleRequest(
            userId = testUserId,
            startDate = LocalDate.of(2024, 1, 1),
            duration = 5
        )
        `when`(cycleService.createCycle(any(Cycle::class.java))).thenReturn(1L)
        `when`(cycleService.getCycleById(1L)).thenReturn(testCycle)

        val result = mockMvc.perform(post("/api/cycles")
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
    fun `deleteCycle should return success when deleted`() {
        `when`(cycleService.deleteCycle(1L)).thenReturn(true)

        val result = mockMvc.perform(delete("/api/cycles/1"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `getPrediction should return prediction data`() {
        `when`(cycleService.predictNextPeriod(testUserId)).thenReturn(LocalDate.of(2024, 2, 1))
        `when`(cycleService.calculateAverageCycleLength(testUserId)).thenReturn(28)

        val result = mockMvc.perform(get("/api/cycles/prediction")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}