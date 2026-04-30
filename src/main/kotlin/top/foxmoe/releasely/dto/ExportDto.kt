package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class ExportData(
    val exportDate: LocalDateTime,
    val user: UserExportDto,
    val activityRecords: List<ActivityRecordDto>,
    val cycles: List<CycleExportDto>,
    val medications: List<MedicationExportDto>,
    val healthReports: List<HealthReportExportDto>,
    val partners: List<PartnerExportDto>,
    val securitySettings: SecuritySettingsExportDto
)

data class UserExportDto(
    val id: Long,
    val username: String?,
    val email: String?,
    val createdAt: LocalDateTime?
)

data class ActivityRecordDto(
    val id: Long?,
    val type: String?,
    val protection: String?,
    val pleasureRating: Int?,
    val healthStatus: String?,
    val occurredAt: LocalDateTime?,
    val encryptedNotes: String?,
    val createdAt: LocalDateTime?
)

data class CycleExportDto(
    val id: Long?,
    val startDate: String?,
    val duration: Int?,
    val predictedNext: String?,
    val createdAt: LocalDateTime?
)

data class MedicationExportDto(
    val id: Long?,
    val name: String?,
    val dosage: String?,
    val reminderTime: String?,
    val lastTaken: String?,
    val isActive: Boolean?,
    val createdAt: LocalDateTime?
)

data class HealthReportExportDto(
    val id: Long?,
    val period: String?,
    val frequencyData: String?,
    val protectionRate: Double?,
    val createdAt: LocalDateTime?
)

data class PartnerExportDto(
    val id: Long?,
    val partnerId: Long?,
    val inviteCode: String?,
    val status: String?,
    val sharedPermissions: String?,
    val calendarSharingEnabled: Boolean?,
    val createdAt: LocalDateTime?
)

data class SecuritySettingsExportDto(
    val lockType: String?,
    val isAppLockEnabled: Boolean?,
    val isDisguiseEnabled: Boolean?,
    val disguiseType: String?,
    val isScreenshotProtected: Boolean?,
    val is2FAEnabled: Boolean?,
    val createdAt: LocalDateTime?
)