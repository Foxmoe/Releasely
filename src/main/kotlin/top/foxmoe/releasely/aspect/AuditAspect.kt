package top.foxmoe.releasely.aspect

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.service.AuditService

@Aspect
@Component
class AuditAspect(private val auditService: AuditService) {

    @Around("@annotation(auditLog)")
    fun aroundAuditLog(joinPoint: ProceedingJoinPoint, auditLog: AuditLog): Any? {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val ipAddress = getClientIp(request)
        val userAgent = request?.getHeader("User-Agent")

        val resourceId = try {
            extractResourceId(joinPoint)
        } catch (_: Exception) {
            null
        }

        val result = try {
            joinPoint.proceed()
        } catch (throwable: Throwable) {
            val userId = extractUserId(joinPoint, result = null)
            auditService.log(
                action = auditLog.action,
                userId = userId,
                resourceType = auditLog.resourceType.takeIf { it.isNotBlank() },
                resourceId = resourceId,
                details = "Exception: ${throwable.message}",
                success = false,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
            throw throwable
        }

        val userId = extractUserId(joinPoint, result)
        val success = isSuccessResponse(result)
        auditService.log(
            action = auditLog.action,
            userId = userId,
            resourceType = auditLog.resourceType.takeIf { it.isNotBlank() },
            resourceId = resourceId,
            details = null,
            success = success,
            ipAddress = ipAddress,
            userAgent = userAgent
        )

        return result
    }

    @Suppress("UNUSED_PARAMETER")
    private fun extractUserId(joinPoint: ProceedingJoinPoint, result: Any?): Long? {
        // Try to get from request args first
        for (arg in joinPoint.args) {
            val id = extractUserIdFromObject(arg)
            if (id != null) return id
        }

        // Try to get from authenticated principal
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated && authentication.principal != null) {
                val principal = authentication.principal
                if (principal is org.springframework.security.core.userdetails.User) {
                    // Can't get numeric userId from UserDetails username directly without DB lookup
                    // Return null and let caller resolve if needed
                }
            }
        } catch (_: Exception) {
            // ignore
        }

        return null
    }

    private fun extractUserIdFromObject(arg: Any?): Long? {
        if (arg == null) return null
        return when (arg) {
            is Long -> arg
            is Number -> arg.toLong()
            else -> {
                try {
                    val userIdField = arg::class.java.getDeclaredField("userId")
                    userIdField.isAccessible = true
                    val value = userIdField.get(arg)
                    if (value is Number) value.toLong() else null
                } catch (_: Exception) {
                    try {
                        val userIdGetter = arg::class.java.getMethod("getUserId")
                        val value = userIdGetter.invoke(arg)
                        if (value is Number) value.toLong() else null
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }
    }

    private fun getClientIp(request: HttpServletRequest?): String? {
        if (request == null) return null
        var ip = request.getHeader("X-Forwarded-For")
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip?.split(",")?.firstOrNull()?.trim()
    }

    private fun extractResourceId(joinPoint: ProceedingJoinPoint): String? {
        val args = joinPoint.args
        for (arg in args) {
            when (arg) {
                is Long -> return arg.toString()
                is String -> {
                    val num = arg.toLongOrNull()
                    if (num != null) return arg
                }
                else -> {
                    try {
                        val idField = arg::class.java.getDeclaredField("id")
                        idField.isAccessible = true
                        val idValue = idField.get(arg)
                        if (idValue != null) return idValue.toString()
                    } catch (_: Exception) {
                        // ignore
                    }
                    try {
                        val idGetter = arg::class.java.getMethod("getId")
                        val idValue = idGetter.invoke(arg)
                        if (idValue != null) return idValue.toString()
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }
        }
        return null
    }

    private fun isSuccessResponse(result: Any?): Boolean {
        if (result is top.foxmoe.releasely.dto.ApiResponse<*>) {
            return result.code == 200
        }
        return true
    }
}
