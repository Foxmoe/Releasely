package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/activity")
class ActivityController(private val activityMapper: ActivityRecordMapper) {

    @GetMapping("/list")
    fun list(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<ActivityRecord>>> {
        val map = mapOf("user_id" to userId, "is_deleted" to false)
        val records = activityMapper.selectByMap(map)
        return ResponseEntity.ok(ApiResponse.success(records))
    }

    @Suppress("NewApi")
    @PostMapping("/add")
    fun add(@RequestBody record: ActivityRecord): ResponseEntity<ApiResponse<ActivityRecord>> {
        record.createdAt = LocalDateTime.now()
        if (record.occurredAt == null) {
            record.occurredAt = LocalDateTime.now()
        }
        activityMapper.insert(record)
        return ResponseEntity.ok(ApiResponse.success(record))
    }
}
