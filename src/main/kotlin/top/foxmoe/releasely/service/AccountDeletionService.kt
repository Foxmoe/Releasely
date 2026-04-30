package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxmoe.releasely.entity.*
import top.foxmoe.releasely.mapper.*

@Service
class AccountDeletionService(
    private val userMapper: UserMapper,
    private val syncRecordMapper: SyncRecordMapper,
    private val activityRecordMapper: ActivityRecordMapper,
    private val cycleMapper: CycleMapper,
    private val medicationMapper: MedicationMapper,
    private val healthReportMapper: HealthReportMapper,
    private val partnerMapper: PartnerMapper,
    private val securitySettingsMapper: SecuritySettingsMapper
) {

    @Transactional(rollbackFor = [Exception::class])
    fun deleteAccount(userId: Long): AccountDeletionResult {
        return try {
            // 1. Delete sync_records
            syncRecordMapper.delete(QueryWrapper<SyncRecord>().eq("user_id", userId))

            // 2. Delete activity_records
            activityRecordMapper.delete(QueryWrapper<ActivityRecord>().eq("user_id", userId))

            // 3. Delete cycles
            cycleMapper.delete(QueryWrapper<Cycle>().eq("user_id", userId))

            // 4. Delete medications
            medicationMapper.delete(QueryWrapper<Medication>().eq("user_id", userId))

            // 5. Delete health_reports
            healthReportMapper.delete(QueryWrapper<HealthReport>().eq("user_id", userId))

            // 6. Delete partners
            partnerMapper.delete(QueryWrapper<Partner>().eq("user_id", userId))

            // 7. Delete security_settings
            securitySettingsMapper.delete(QueryWrapper<SecuritySettings>().eq("user_id", userId))

            // 8. Delete user
            userMapper.deleteById(userId)

            AccountDeletionResult(success = true, message = "Account and all associated data deleted successfully")
        } catch (e: Exception) {
            AccountDeletionResult(success = false, message = "Failed to delete account: ${e.message}")
        }
    }

    data class AccountDeletionResult(
        val success: Boolean,
        val message: String
    )
}
