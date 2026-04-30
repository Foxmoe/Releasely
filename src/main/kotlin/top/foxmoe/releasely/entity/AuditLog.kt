package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("audit_log")
data class AuditLog(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var action: String? = null,
    var resourceType: String? = null,
    var resourceId: String? = null,
    var ipAddress: String? = null,
    var userAgent: String? = null,
    var timestamp: LocalDateTime? = null,
    var details: String? = null,
    var success: Boolean? = true
)
