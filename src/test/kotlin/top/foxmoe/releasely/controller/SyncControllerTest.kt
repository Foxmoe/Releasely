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
import top.foxmoe.releasely.entity.SyncRecord
import top.foxmoe.releasely.service.SyncService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class SyncControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var syncService: SyncService

    @InjectMocks
    private lateinit var syncController: SyncController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(syncController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    // ========== GET /api/sync/status ==========

    @Test
    fun `getSyncStatus should return status for user`() {
        val status = SyncStatusResponse(
            pendingCount = 3,
            conflictCount = 1,
            lastSyncTime = LocalDateTime.of(2024, 1, 15, 10, 0)
        )
        `when`(syncService.getSyncStatus(testUserId)).thenReturn(status)

        val result = mockMvc.perform(get("/api/sync/status")
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
    fun `getSyncStatus should return zero counts when no records`() {
        val status = SyncStatusResponse(
            pendingCount = 0,
            conflictCount = 0,
            lastSyncTime = null
        )
        `when`(syncService.getSyncStatus(testUserId)).thenReturn(status)

        val result = mockMvc.perform(get("/api/sync/status")
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
    fun `getSyncStatus should handle user with only conflicts`() {
        val status = SyncStatusResponse(
            pendingCount = 0,
            conflictCount = 5,
            lastSyncTime = LocalDateTime.of(2024, 1, 10, 8, 30)
        )
        `when`(syncService.getSyncStatus(testUserId)).thenReturn(status)

        val result = mockMvc.perform(get("/api/sync/status")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    // ========== GET /api/sync/pending ==========

    @Test
    fun `getPendingRecords should return pending records for user`() {
        val records = listOf(
            SyncRecordDto(
                id = 1L,
                userId = testUserId,
                entityType = "activity",
                entityId = 100L,
                action = "CREATE",
                localTimestamp = LocalDateTime.of(2024, 1, 1, 10, 0),
                syncStatus = SyncRecord.STATUS_PENDING
            ),
            SyncRecordDto(
                id = 2L,
                userId = testUserId,
                entityType = "cycle",
                entityId = 200L,
                action = "UPDATE",
                localTimestamp = LocalDateTime.of(2024, 1, 2, 15, 30),
                syncStatus = SyncRecord.STATUS_PENDING
            )
        )
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(records)

        val result = mockMvc.perform(get("/api/sync/pending")
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
    fun `getPendingRecords should return empty list when no pending records`() {
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(emptyList())

        val result = mockMvc.perform(get("/api/sync/pending")
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
    fun `getPendingRecords should return records with different entity types`() {
        val records = listOf(
            SyncRecordDto(
                id = 1L,
                userId = testUserId,
                entityType = "activity",
                entityId = 1L,
                action = "CREATE",
                localTimestamp = LocalDateTime.now(),
                syncStatus = SyncRecord.STATUS_PENDING
            ),
            SyncRecordDto(
                id = 2L,
                userId = testUserId,
                entityType = "medication",
                entityId = 2L,
                action = "DELETE",
                localTimestamp = LocalDateTime.now(),
                syncStatus = SyncRecord.STATUS_PENDING
            ),
            SyncRecordDto(
                id = 3L,
                userId = testUserId,
                entityType = "partner",
                entityId = 3L,
                action = "UPDATE",
                localTimestamp = LocalDateTime.now(),
                syncStatus = SyncRecord.STATUS_PENDING
            )
        )
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(records)

        val result = mockMvc.perform(get("/api/sync/pending")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    // ========== GET /api/sync/conflicts ==========

    @Test
    fun `getConflictRecords should return conflict records for user`() {
        val records = listOf(
            SyncRecordDto(
                id = 1L,
                userId = testUserId,
                entityType = "activity",
                entityId = 100L,
                action = "UPDATE",
                localTimestamp = LocalDateTime.of(2024, 1, 1, 10, 0),
                syncStatus = SyncRecord.STATUS_CONFLICT,
                payload = "{\"server\": \"data\"}"
            )
        )
        `when`(syncService.getConflictRecordDtos(testUserId)).thenReturn(records)

        val result = mockMvc.perform(get("/api/sync/conflicts")
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
    fun `getConflictRecords should return empty list when no conflicts`() {
        `when`(syncService.getConflictRecordDtos(testUserId)).thenReturn(emptyList())

        val result = mockMvc.perform(get("/api/sync/conflicts")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    // ========== POST /api/sync/push ==========

    @Test
    fun `pushChanges should return sync response with pending records`() {
        val request = SyncRequest(
            userId = testUserId,
            lastSyncTimestamp = LocalDateTime.of(2024, 1, 1, 0, 0),
            pendingRecords = listOf(
                SyncRecordDto(
                    entityType = "activity",
                    entityId = 100L,
                    action = "CREATE",
                    localTimestamp = LocalDateTime.of(2024, 1, 2, 10, 0)
                )
            )
        )

        val response = SyncResponse(
            syncedRecords = emptyList(),
            pendingRecords = listOf(
                SyncRecordDto(
                    id = 1L,
                    userId = testUserId,
                    entityType = "activity",
                    entityId = 100L,
                    action = "CREATE",
                    localTimestamp = LocalDateTime.of(2024, 1, 2, 10, 0),
                    syncStatus = SyncRecord.STATUS_PENDING
                )
            ),
            conflicts = emptyList(),
            serverTimestamp = LocalDateTime.now()
        )
        `when`(syncService.sync(testUserId, request)).thenReturn(response)

        val result = mockMvc.perform(post("/api/sync/push")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val apiResponse = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, apiResponse.code)
    }

    @Test
    fun `pushChanges should handle empty pending records`() {
        val request = SyncRequest(
            userId = testUserId,
            lastSyncTimestamp = LocalDateTime.of(2024, 1, 1, 0, 0),
            pendingRecords = null
        )

        val response = SyncResponse(
            syncedRecords = emptyList(),
            pendingRecords = emptyList(),
            conflicts = emptyList(),
            serverTimestamp = LocalDateTime.now()
        )
        `when`(syncService.sync(testUserId, request)).thenReturn(response)

        val result = mockMvc.perform(post("/api/sync/push")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val apiResponse = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, apiResponse.code)
    }

    @Test
    fun `pushChanges should return conflicts when server has newer data`() {
        val request = SyncRequest(
            userId = testUserId,
            lastSyncTimestamp = LocalDateTime.of(2024, 1, 1, 0, 0),
            pendingRecords = listOf(
                SyncRecordDto(
                    entityType = "activity",
                    entityId = 100L,
                    action = "UPDATE",
                    localTimestamp = LocalDateTime.of(2024, 1, 2, 10, 0)
                )
            )
        )

        val response = SyncResponse(
            syncedRecords = emptyList(),
            pendingRecords = emptyList(),
            conflicts = listOf(
                SyncConflictDto(
                    entityType = "activity",
                    entityId = 100L,
                    localData = "{\"local\": \"data\"}",
                    serverData = "{\"server\": \"data\"}",
                    conflictTimestamp = LocalDateTime.of(2024, 1, 2, 12, 0)
                )
            ),
            serverTimestamp = LocalDateTime.now()
        )
        `when`(syncService.sync(testUserId, request)).thenReturn(response)

        val result = mockMvc.perform(post("/api/sync/push")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val apiResponse = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, apiResponse.code)
    }

    @Test
    fun `pushChanges should handle multiple pending records`() {
        val request = SyncRequest(
            userId = testUserId,
            pendingRecords = listOf(
                SyncRecordDto(entityType = "activity", entityId = 1L, action = "CREATE"),
                SyncRecordDto(entityType = "cycle", entityId = 2L, action = "UPDATE"),
                SyncRecordDto(entityType = "medication", entityId = 3L, action = "DELETE")
            )
        )

        val response = SyncResponse(
            syncedRecords = emptyList(),
            pendingRecords = listOf(
                SyncRecordDto(id = 1L, entityType = "activity", entityId = 1L, action = "CREATE"),
                SyncRecordDto(id = 2L, entityType = "cycle", entityId = 2L, action = "UPDATE"),
                SyncRecordDto(id = 3L, entityType = "medication", entityId = 3L, action = "DELETE")
            ),
            conflicts = emptyList(),
            serverTimestamp = LocalDateTime.now()
        )
        `when`(syncService.sync(testUserId, request)).thenReturn(response)

        val result = mockMvc.perform(post("/api/sync/push")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andReturn()

        val apiResponse = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, apiResponse.code)
    }

    // ========== POST /api/sync/resolve ==========

    @Test
    fun `resolveConflict should return success when conflict resolved`() {
        val request = ResolveConflictRequest(
            userId = testUserId,
            entityType = "activity",
            entityId = 100L,
            resolution = "LOCAL"
        )
        `when`(syncService.resolveConflict(testUserId, request)).thenReturn(true)

        val result = mockMvc.perform(post("/api/sync/resolve")
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
    fun `resolveConflict should return NOT_FOUND when conflict not found`() {
        val request = ResolveConflictRequest(
            userId = testUserId,
            entityType = "activity",
            entityId = 999L,
            resolution = "SERVER"
        )
        `when`(syncService.resolveConflict(testUserId, request)).thenReturn(false)

        val result = mockMvc.perform(post("/api/sync/resolve")
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
    fun `resolveConflict should handle merged resolution`() {
        val request = ResolveConflictRequest(
            userId = testUserId,
            entityType = "activity",
            entityId = 100L,
            resolution = "MERGED",
            mergedData = "{\"merged\": \"data\"}"
        )
        `when`(syncService.resolveConflict(testUserId, request)).thenReturn(true)

        val result = mockMvc.perform(post("/api/sync/resolve")
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
    fun `resolveConflict should handle different entity types`() {
        val request = ResolveConflictRequest(
            userId = testUserId,
            entityType = "cycle",
            entityId = 200L,
            resolution = "LOCAL"
        )
        `when`(syncService.resolveConflict(testUserId, request)).thenReturn(true)

        val result = mockMvc.perform(post("/api/sync/resolve")
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

    // ========== POST /api/sync/record ==========

    @Test
    fun `createSyncRecord should return created record`() {
        val request = CreateSyncRecordRequest(
            userId = testUserId,
            entityType = "activity",
            entityId = 100L,
            action = "CREATE",
            localTimestamp = LocalDateTime.of(2024, 1, 1, 10, 0),
            payload = "{\"data\": \"value\"}"
        )

        val createdDto = SyncRecordDto(
            id = 1L,
            userId = testUserId,
            entityType = "activity",
            entityId = 100L,
            action = "CREATE",
            localTimestamp = LocalDateTime.of(2024, 1, 1, 10, 0),
            syncStatus = SyncRecord.STATUS_PENDING,
            payload = "{\"data\": \"value\"}"
        )
        `when`(syncService.createSyncRecord(any(SyncRecord::class.java))).thenReturn(1L)
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(listOf(createdDto))

        val result = mockMvc.perform(post("/api/sync/record")
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
    fun `createSyncRecord should return error when record not found after creation`() {
        val request = CreateSyncRecordRequest(
            userId = testUserId,
            entityType = "activity",
            entityId = 100L,
            action = "CREATE"
        )
        `when`(syncService.createSyncRecord(any(SyncRecord::class.java))).thenReturn(1L)
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(emptyList())

        val result = mockMvc.perform(post("/api/sync/record")
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

    @Test
    fun `createSyncRecord should handle null localTimestamp`() {
        val request = CreateSyncRecordRequest(
            userId = testUserId,
            entityType = "cycle",
            entityId = 200L,
            action = "UPDATE",
            localTimestamp = null,
            payload = null
        )

        val createdDto = SyncRecordDto(
            id = 2L,
            userId = testUserId,
            entityType = "cycle",
            entityId = 200L,
            action = "UPDATE",
            syncStatus = SyncRecord.STATUS_PENDING
        )
        `when`(syncService.createSyncRecord(any(SyncRecord::class.java))).thenReturn(2L)
        `when`(syncService.getPendingRecordDtos(testUserId)).thenReturn(listOf(createdDto))

        val result = mockMvc.perform(post("/api/sync/record")
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

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
