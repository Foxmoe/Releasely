package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.SyncRecord
import top.foxmoe.releasely.service.SyncService
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/sync")
class SyncController(private val syncService: SyncService) {

    @GetMapping("/status")
    fun getSyncStatus(@RequestParam userId: Long): ResponseEntity<ApiResponse<SyncStatusResponse>> {
        val status = syncService.getSyncStatus(userId)
        return ResponseEntity.ok(ApiResponse.success(status))
    }

    @GetMapping("/pending")
    fun getPendingRecords(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<SyncRecordDto>>> {
        val records = syncService.getPendingRecords(userId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse.success(records))
    }

    @GetMapping("/conflicts")
    fun getConflictRecords(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<SyncRecordDto>>> {
        val records = syncService.getConflictRecords(userId).map { it.toDto() }
        return ResponseEntity.ok(ApiResponse.success(records))
    }

    @PostMapping("/push")
    fun pushChanges(@RequestBody request: SyncRequest): ResponseEntity<ApiResponse<SyncResponse>> {
        val response = syncService.sync(request.userId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/resolve")
    fun resolveConflict(@RequestBody request: ResolveConflictRequest): ResponseEntity<ApiResponse<String>> {
        val success = syncService.resolveConflict(request.userId, request)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Conflict resolved"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @PostMapping("/record")
    fun createSyncRecord(@RequestBody request: CreateSyncRecordRequest): ResponseEntity<ApiResponse<SyncRecordDto>> {
        val record = SyncRecord(
            userId = request.userId,
            entityType = request.entityType,
            entityId = request.entityId,
            action = request.action,
            localTimestamp = request.localTimestamp ?: LocalDateTime.now(),
            syncStatus = SyncRecord.STATUS_PENDING,
            payload = request.payload
        )
        val id = syncService.createSyncRecord(record)
        val created = syncService.getPendingRecords(request.userId)
            .find { it.id == id }

        return if (created != null) {
            ResponseEntity.ok(ApiResponse.success(created.toDto()))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR))
        }
    }
}

data class CreateSyncRecordRequest(
    val userId: Long,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val localTimestamp: LocalDateTime? = null,
    val payload: String? = null
)