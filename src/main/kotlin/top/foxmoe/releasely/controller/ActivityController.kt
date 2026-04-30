package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.service.EncryptionService
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/activity")
class ActivityController(
    private val activityMapper: ActivityRecordMapper,
    private val encryptionService: EncryptionService
) {

    @GetMapping("/list")
    fun list(@RequestParam userId: Long): ResponseEntity<ApiResponse<ActivityListResponse>> {
        val map = mapOf("user_id" to userId, "is_deleted" to false)
        val records = activityMapper.selectByMap(map)
        val dtos = records.map { it.toDto() }
        return ResponseEntity.ok(ApiResponse.success(ActivityListResponse(dtos, dtos.size)))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<ActivityDto>> {
        val record = activityMapper.selectById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(record.toDto()))
    }

    @Suppress("NewApi")
    @AuditLog(action = "ACTIVITY_CREATE", resourceType = "ACTIVITY")
    @PostMapping
    fun create(@RequestBody request: CreateActivityRequest): ResponseEntity<ApiResponse<ActivityDto>> {
        val record = ActivityRecord(
            userId = request.userId,
            type = request.type,
            protection = request.protection,
            pleasureRating = request.pleasureRating,
            healthStatus = request.healthStatus,
            occurredAt = request.occurredAt ?: LocalDateTime.now(),
            encryptedNotes = request.notes?.let { encryptionService.encrypt(it) },
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )
        activityMapper.insert(record)
        return ResponseEntity.ok(ApiResponse.success(record.toDto()))
    }

    @AuditLog(action = "ACTIVITY_UPDATE", resourceType = "ACTIVITY")
    @PutMapping
    fun update(@RequestBody request: UpdateActivityRequest): ResponseEntity<ApiResponse<ActivityDto>> {
        val record = activityMapper.selectById(request.id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        request.type?.let { record.type = it }
        request.protection?.let { record.protection = it }
        request.pleasureRating?.let { record.pleasureRating = it }
        request.healthStatus?.let { record.healthStatus = it }
        request.occurredAt?.let { record.occurredAt = it }
        request.notes?.let { record.encryptedNotes = encryptionService.encrypt(it) }

        activityMapper.updateById(record)
        return ResponseEntity.ok(ApiResponse.success(record.toDto()))
    }

    @AuditLog(action = "ACTIVITY_DELETE", resourceType = "ACTIVITY")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        val record = activityMapper.selectById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        record.isDeleted = true
        activityMapper.updateById(record)
        return ResponseEntity.ok(ApiResponse.success("Activity deleted"))
    }

    private fun ActivityRecord.toDto() = ActivityDto(
        id = id,
        userId = userId,
        type = type,
        protection = protection,
        pleasureRating = pleasureRating,
        healthStatus = healthStatus,
        occurredAt = occurredAt,
        notes = encryptedNotes?.let { encryptionService.decrypt(it) },
        createdAt = createdAt
    )
}
