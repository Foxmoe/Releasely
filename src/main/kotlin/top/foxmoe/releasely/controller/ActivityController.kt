package top.foxmoe.releasely.controller

import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import java.time.LocalDateTime

// 简单封装结果对象，实际项目中建议使用统一Result类
data class Result(val code: Int, val msg: String, val data: Any?)

@RestController
@RequestMapping("/api/v1/activity")
class ActivityController(private val activityMapper: ActivityRecordMapper) {

    @GetMapping("/list")
    fun list(@RequestParam userId: Long): Result {
        // 实际应从SecurityContext获取当前用户ID，此处演示直接传参
        // 在启用了Spring Security后，可以通过SecurityContextHolder获取当前用户信息
        // val authentication = SecurityContextHolder.getContext().authentication
        // val username = authentication.name
        // // 根据username查询userId (实际项目中建议封装UserContext)
        val map = mapOf("user_id" to userId, "is_deleted" to false)
        return Result(200, "OK", activityMapper.selectByMap(map))
    }

    @PostMapping("/add")
    fun add(@RequestBody record: ActivityRecord): Result {
        record.createdAt = LocalDateTime.now()
        // 确保发生时间不为空
        if (record.occurredAt == null) {
            record.occurredAt = LocalDateTime.now()
        }
        activityMapper.insert(record)
        return Result(200, "Success", record.id)
    }
}
