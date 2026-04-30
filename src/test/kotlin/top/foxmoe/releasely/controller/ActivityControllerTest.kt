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
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.service.EncryptionService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class ActivityControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var activityMapper: ActivityRecordMapper

    @Mock
    private lateinit var encryptionService: EncryptionService

    @InjectMocks
    private lateinit var activityController: ActivityController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L
    private val testActivityId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `list should return activities for user`() {
        val records = listOf(
            ActivityRecord(
                id = 1L,
                userId = testUserId,
                type = "MASTURBATION",
                protection = "NONE",
                pleasureRating = 5,
                healthStatus = "GOOD",
                occurredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
                encryptedNotes = "encrypted_notes_1",
                isDeleted = false,
                createdAt = LocalDateTime.now()
            ),
            ActivityRecord(
                id = 2L,
                userId = testUserId,
                type = "PARTNER_SEX",
                protection = "CONDOM",
                pleasureRating = 4,
                healthStatus = "GOOD",
                occurredAt = LocalDateTime.of(2024, 1, 2, 20, 0),
                encryptedNotes = null,
                isDeleted = false,
                createdAt = LocalDateTime.now()
            )
        )

        `when`(activityMapper.selectByMap(mapOf("user_id" to testUserId, "is_deleted" to false))).thenReturn(records)
        `when`(encryptionService.decrypt("encrypted_notes_1")).thenReturn("some notes")

        val result = mockMvc.perform(get("/api/activity/list")
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
    fun `list should return empty list when no activities`() {
        `when`(activityMapper.selectByMap(mapOf("user_id" to testUserId, "is_deleted" to false))).thenReturn(emptyList())

        val result = mockMvc.perform(get("/api/activity/list")
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
    fun `getById should return activity when exists`() {
        val record = ActivityRecord(
            id = testActivityId,
            userId = testUserId,
            type = "MASTURBATION",
            protection = "NONE",
            pleasureRating = 5,
            healthStatus = "GOOD",
            occurredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            encryptedNotes = "encrypted_notes",
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )

        `when`(activityMapper.selectById(testActivityId)).thenReturn(record)
        `when`(encryptionService.decrypt("encrypted_notes")).thenReturn("decrypted notes")

        val result = mockMvc.perform(get("/api/activity/{id}", testActivityId))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `getById should return NOT_FOUND when activity does not exist`() {
        `when`(activityMapper.selectById(999L)).thenReturn(null)

        val result = mockMvc.perform(get("/api/activity/{id}", 999L))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.NOT_FOUND.code, response.code)
    }

    @Test
    fun `create should return created activity`() {
        val request = CreateActivityRequest(
            userId = testUserId,
            type = "MASTURBATION",
            protection = "NONE",
            pleasureRating = 5,
            healthStatus = "GOOD",
            occurredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            notes = "my notes"
        )

        `when`(encryptionService.encrypt("my notes")).thenReturn("encrypted_notes")
        `when`(activityMapper.insert(any(ActivityRecord::class.java))).thenAnswer { invocation ->
            val record = invocation.getArgument<ActivityRecord>(0)
            record.id = testActivityId
            1
        }
        `when`(encryptionService.decrypt("encrypted_notes")).thenReturn("my notes")

        val result = mockMvc.perform(post("/api/activity")
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
    fun `create should handle null notes`() {
        val request = CreateActivityRequest(
            userId = testUserId,
            type = "EDGING",
            protection = null,
            pleasureRating = null,
            healthStatus = null,
            occurredAt = null,
            notes = null
        )

        `when`(activityMapper.insert(any(ActivityRecord::class.java))).thenAnswer { invocation ->
            val record = invocation.getArgument<ActivityRecord>(0)
            record.id = testActivityId
            1
        }

        val result = mockMvc.perform(post("/api/activity")
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
    fun `update should return updated activity when exists`() {
        val existingRecord = ActivityRecord(
            id = testActivityId,
            userId = testUserId,
            type = "MASTURBATION",
            protection = "NONE",
            pleasureRating = 5,
            healthStatus = "GOOD",
            occurredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            encryptedNotes = "old_encrypted_notes",
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )

        val request = UpdateActivityRequest(
            id = testActivityId,
            type = "PARTNER_SEX",
            protection = "CONDOM",
            pleasureRating = 4,
            notes = "updated notes"
        )

        `when`(activityMapper.selectById(testActivityId)).thenReturn(existingRecord)
        `when`(encryptionService.encrypt("updated notes")).thenReturn("new_encrypted_notes")
        `when`(activityMapper.updateById(any(ActivityRecord::class.java))).thenReturn(1)
        `when`(encryptionService.decrypt("new_encrypted_notes")).thenReturn("updated notes")

        val result = mockMvc.perform(put("/api/activity")
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
    fun `update should return NOT_FOUND when activity does not exist`() {
        val request = UpdateActivityRequest(
            id = 999L,
            type = "MASTURBATION"
        )

        `when`(activityMapper.selectById(999L)).thenReturn(null)

        val result = mockMvc.perform(put("/api/activity")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.NOT_FOUND.code, response.code)
    }

    @Test
    fun `update should handle partial update`() {
        val existingRecord = ActivityRecord(
            id = testActivityId,
            userId = testUserId,
            type = "MASTURBATION",
            protection = "NONE",
            pleasureRating = 5,
            healthStatus = "GOOD",
            occurredAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            encryptedNotes = null,
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )

        val request = UpdateActivityRequest(
            id = testActivityId,
            pleasureRating = 3
        )

        `when`(activityMapper.selectById(testActivityId)).thenReturn(existingRecord)
        `when`(activityMapper.updateById(any(ActivityRecord::class.java))).thenReturn(1)

        val result = mockMvc.perform(put("/api/activity")
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
    fun `delete should return success when activity exists`() {
        val existingRecord = ActivityRecord(
            id = testActivityId,
            userId = testUserId,
            type = "MASTURBATION",
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )

        `when`(activityMapper.selectById(testActivityId)).thenReturn(existingRecord)
        `when`(activityMapper.updateById(any(ActivityRecord::class.java))).thenReturn(1)

        val result = mockMvc.perform(delete("/api/activity/{id}", testActivityId))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    @Test
    fun `delete should return NOT_FOUND when activity does not exist`() {
        `when`(activityMapper.selectById(999L)).thenReturn(null)

        val result = mockMvc.perform(delete("/api/activity/{id}", 999L))
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
