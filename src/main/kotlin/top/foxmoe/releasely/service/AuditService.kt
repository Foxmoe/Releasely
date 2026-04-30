package top.foxmoe.releasely.service

import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.AuditLog
import top.foxmoe.releasely.mapper.AuditLogMapper
import java.time.LocalDateTime

@Service
class AuditService(private val auditLogMapper: AuditLogMapper) {

    fun log(
        action: String,
        userId: Long? = null,
        resourceType: String? = null,
        resourceId: String? = null,
        details: String? = null,
        success: Boolean = true,
        ipAddress: String? = null,
        userAgent: String? = null
    ) {
        val auditLog = AuditLog(
            userId = userId,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            timestamp = LocalDateTime.now(),
            details = details,
            success = success
        )
        auditLogMapper.insert(auditLog)
    }
}
