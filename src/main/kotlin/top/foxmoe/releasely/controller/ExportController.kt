package top.foxmoe.releasely.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.ApiResponse
import top.foxmoe.releasely.dto.ExportData
import top.foxmoe.releasely.dto.ResultCode
import top.foxmoe.releasely.mapper.UserMapper
import top.foxmoe.releasely.service.ExportService
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
class ExportController(
    private val exportService: ExportService,
    private val userMapper: UserMapper
) {

    @GetMapping("/export")
    fun exportUserData(authentication: Authentication): ResponseEntity<ApiResponse<ExportData>> {
        val username = authentication.principal.toString()
        val user = com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<top.foxmoe.releasely.entity.User>()
            .eq("username", username)
            .last("LIMIT 1")
            .let { userMapper.selectOne(it) }
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND.code, ResultCode.USER_NOT_FOUND.message))

        val exportData = exportService.exportUserData(user.id!!)
        return ResponseEntity.ok(ApiResponse.success(exportData))
    }

    @GetMapping("/export/file")
    fun exportUserDataAsFile(authentication: Authentication): ResponseEntity<ByteArray> {
        val username = authentication.principal.toString()
        val user = com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<top.foxmoe.releasely.entity.User>()
            .eq("username", username)
            .last("LIMIT 1")
            .let { userMapper.selectOne(it) }
            ?: return ResponseEntity.badRequest().build()

        val exportData = exportService.exportUserData(user.id!!)

        val timestamp = Instant.now().toString().replace(":", "-")
        val filename = "releasely_data_${user.id}_$timestamp.json"

        val jsonBytes = exportData.toJson().toByteArray(Charsets.UTF_8)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_JSON)
            .contentLength(jsonBytes.size.toLong())
            .body(jsonBytes)
    }

    private fun ExportData.toJson(): String {
        return buildString {
            appendLine("{")
            appendLine("  \"exportDate\": \"$exportDate\",")
            appendLine("  \"user\": {")
            appendLine("    \"id\": ${user.id},")
            appendLine("    \"username\": \"${user.username ?: ""}\",")
            appendLine("    \"email\": \"${user.email ?: ""}\",")
            appendLine("    \"createdAt\": \"${user.createdAt}\"")
            appendLine("  },")
            appendLine("  \"activityRecords\": [")
            activityRecords.forEachIndexed { index, record ->
                appendLine("    {")
                appendLine("      \"id\": ${record.id},")
                appendLine("      \"type\": \"${record.type ?: ""}\",")
                appendLine("      \"protection\": \"${record.protection ?: ""}\",")
                appendLine("      \"pleasureRating\": ${record.pleasureRating},")
                appendLine("      \"healthStatus\": \"${record.healthStatus ?: ""}\",")
                appendLine("      \"occurredAt\": \"${record.occurredAt}\",")
                appendLine("      \"encryptedNotes\": \"${record.encryptedNotes ?: ""}\",")
                appendLine("      \"createdAt\": \"${record.createdAt}\"")
                append("    }${if (index < activityRecords.size - 1) "," else ""}")
                appendLine()
            }
            appendLine("  ],")
            appendLine("  \"cycles\": [")
            cycles.forEachIndexed { index, cycle ->
                appendLine("    {")
                appendLine("      \"id\": ${cycle.id},")
                appendLine("      \"startDate\": \"${cycle.startDate ?: ""}\",")
                appendLine("      \"duration\": ${cycle.duration},")
                appendLine("      \"predictedNext\": \"${cycle.predictedNext ?: ""}\",")
                appendLine("      \"createdAt\": \"${cycle.createdAt}\"")
                append("    }${if (index < cycles.size - 1) "," else ""}")
                appendLine()
            }
            appendLine("  ],")
            appendLine("  \"medications\": [")
            medications.forEachIndexed { index, med ->
                appendLine("    {")
                appendLine("      \"id\": ${med.id},")
                appendLine("      \"name\": \"${med.name ?: ""}\",")
                appendLine("      \"dosage\": \"${med.dosage ?: ""}\",")
                appendLine("      \"reminderTime\": \"${med.reminderTime ?: ""}\",")
                appendLine("      \"lastTaken\": \"${med.lastTaken ?: ""}\",")
                appendLine("      \"isActive\": ${med.isActive},")
                appendLine("      \"createdAt\": \"${med.createdAt}\"")
                append("    }${if (index < medications.size - 1) "," else ""}")
                appendLine()
            }
            appendLine("  ],")
            appendLine("  \"healthReports\": [")
            healthReports.forEachIndexed { index, report ->
                appendLine("    {")
                appendLine("      \"id\": ${report.id},")
                appendLine("      \"period\": \"${report.period ?: ""}\",")
                appendLine("      \"frequencyData\": \"${report.frequencyData ?: ""}\",")
                appendLine("      \"protectionRate\": ${report.protectionRate},")
                appendLine("      \"createdAt\": \"${report.createdAt}\"")
                append("    }${if (index < healthReports.size - 1) "," else ""}")
                appendLine()
            }
            appendLine("  ],")
            appendLine("  \"partners\": [")
            partners.forEachIndexed { index, partner ->
                appendLine("    {")
                appendLine("      \"id\": ${partner.id},")
                appendLine("      \"partnerId\": ${partner.partnerId},")
                appendLine("      \"inviteCode\": \"${partner.inviteCode ?: ""}\",")
                appendLine("      \"status\": \"${partner.status ?: ""}\",")
                appendLine("      \"sharedPermissions\": \"${partner.sharedPermissions ?: ""}\",")
                appendLine("      \"calendarSharingEnabled\": ${partner.calendarSharingEnabled},")
                appendLine("      \"createdAt\": \"${partner.createdAt}\"")
                append("    }${if (index < partners.size - 1) "," else ""}")
                appendLine()
            }
            appendLine("  ],")
            appendLine("  \"securitySettings\": {")
            appendLine("    \"lockType\": \"${securitySettings.lockType ?: ""}\",")
            appendLine("    \"isAppLockEnabled\": ${securitySettings.isAppLockEnabled},")
            appendLine("    \"isDisguiseEnabled\": ${securitySettings.isDisguiseEnabled},")
            appendLine("    \"disguiseType\": \"${securitySettings.disguiseType ?: ""}\",")
            appendLine("    \"isScreenshotProtected\": ${securitySettings.isScreenshotProtected},")
            appendLine("    \"is2FAEnabled\": ${securitySettings.is2FAEnabled},")
            appendLine("    \"createdAt\": \"${securitySettings.createdAt}\"")
            appendLine("  }")
            appendLine("}")
        }
    }
}