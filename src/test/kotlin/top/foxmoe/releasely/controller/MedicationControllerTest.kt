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
import top.foxmoe.releasely.entity.Medication
import top.foxmoe.releasely.service.MedicationService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class MedicationControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var medicationService: MedicationService

    @InjectMocks
    private lateinit var medicationController: MedicationController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L
    private val testMedicationId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(medicationController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `getMedications should return medications for user`() {
        val medications = listOf(
            Medication(
                id = testMedicationId,
                userId = testUserId,
                name = "Test Med",
                dosage = "100mg",
                reminderTime = LocalDateTime.of(2024, 1, 1, 9, 0),
                isActive = true
            )
        )
        `when`(medicationService.getMedicationsByUserId(testUserId)).thenReturn(medications)

        val result = mockMvc.perform(get("/api/medications")
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
    fun `getMedicationById should return medication when exists`() {
        val medication = Medication(
            id = testMedicationId,
            userId = testUserId,
            name = "Test Med",
            dosage = "100mg",
            reminderTime = LocalDateTime.of(2024, 1, 1, 9, 0),
            isActive = true
        )
        `when`(medicationService.getMedicationById(testMedicationId)).thenReturn(medication)

        val result = mockMvc.perform(get("/api/medications/{id}", testMedicationId))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `getMedicationById should return NOT_FOUND when medication does not exist`() {
        `when`(medicationService.getMedicationById(999L)).thenReturn(null)

        val result = mockMvc.perform(get("/api/medications/999"))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.NOT_FOUND.code, response.code)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
