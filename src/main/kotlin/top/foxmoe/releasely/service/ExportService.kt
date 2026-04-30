package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.*
import top.foxmoe.releasely.mapper.*
import java.time.LocalDateTime

@Service
class ExportService(
    private val userMapper: UserMapper,
    private val activityRecordMapper: ActivityRecordMapper,
    private val cycleMapper: CycleMapper,
    private val medicationMapper: MedicationMapper,
    private val healthReportMapper: HealthReportMapper,
    private val partnerMapper: PartnerMapper,
    private val securitySettingsMapper: SecuritySettingsMapper
) {

    fun exportUserData(userId: Long): ExportData {
        val exportDate = LocalDateTime.now()

        // Get user info
        val user = userMapper.selectById(userId)
        val userDto = UserExportDto(
            id = userId,
            username = user?.username,
            email = user?.email,
            createdAt = user?.createdAt
        )

        // Get activity records
        val activityWrapper = QueryWrapper<ActivityRecord>()
            .eq("user_id", userId)
            .eq("is_deleted", false)
        val activities = activityRecordMapper.selectList(activityWrapper)
        val activityDtos = activities.map { a ->
            ActivityRecordDto(
                id = a.id,
                type = a.type,
                protection = a.protection,
                pleasureRating = a.pleasureRating,
                healthStatus = a.healthStatus,
                occurredAt = a.occurredAt,
                encryptedNotes = a.encryptedNotes,
                createdAt = a.createdAt
            )
        }

        // Get cycles
        val cycleWrapper = QueryWrapper<Cycle>()
            .eq("user_id", userId)
            .eq("is_deleted", false)
        val cycles = cycleMapper.selectList(cycleWrapper)
        val cycleDtos = cycles.map { c ->
            CycleExportDto(
                id = c.id,
                startDate = c.startDate?.toString(),
                duration = c.duration,
                predictedNext = c.predictedNext?.toString(),
                createdAt = c.createdAt
            )
        }

        // Get medications
        val medWrapper = QueryWrapper<Medication>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        val medications = medicationMapper.selectList(medWrapper)
        val medDtos = medications.map { m ->
            MedicationExportDto(
                id = m.id,
                name = m.name,
                dosage = m.dosage,
                reminderTime = m.reminderTime?.toString(),
                lastTaken = m.lastTaken?.toString(),
                isActive = m.isActive,
                createdAt = m.createdAt
            )
        }

        // Get health reports
        val healthWrapper = QueryWrapper<HealthReport>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        val healthReports = healthReportMapper.selectList(healthWrapper)
        val healthDtos = healthReports.map { h ->
            HealthReportExportDto(
                id = h.id,
                period = h.period,
                frequencyData = h.frequencyData,
                protectionRate = h.protectionRate,
                createdAt = h.createdAt
            )
        }

        // Get partners
        val partnerWrapper = QueryWrapper<Partner>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        val partners = partnerMapper.selectList(partnerWrapper)
        val partnerDtos = partners.map { p ->
            PartnerExportDto(
                id = p.id,
                partnerId = p.partnerId,
                inviteCode = p.inviteCode,
                status = p.status,
                sharedPermissions = p.sharedPermissions,
                calendarSharingEnabled = p.calendarSharingEnabled,
                createdAt = p.createdAt
            )
        }

        // Get security settings
        val securityWrapper = QueryWrapper<SecuritySettings>()
            .eq("user_id", userId)
            .last("LIMIT 1")
        val securitySettings = securitySettingsMapper.selectList(securityWrapper).firstOrNull()
        val securityDto = SecuritySettingsExportDto(
            lockType = securitySettings?.lockType,
            isAppLockEnabled = securitySettings?.isAppLockEnabled,
            isDisguiseEnabled = securitySettings?.isDisguiseEnabled,
            disguiseType = securitySettings?.disguiseType,
            isScreenshotProtected = securitySettings?.isScreenshotProtected,
            is2FAEnabled = securitySettings?.is2FAEnabled,
            createdAt = securitySettings?.createdAt
        )

        return ExportData(
            exportDate = exportDate,
            user = userDto,
            activityRecords = activityDtos,
            cycles = cycleDtos,
            medications = medDtos,
            healthReports = healthDtos,
            partners = partnerDtos,
            securitySettings = securityDto
        )
    }
}