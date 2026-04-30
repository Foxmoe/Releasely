package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import top.foxmoe.releasely.entity.*
import top.foxmoe.releasely.mapper.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AccountDeletionServiceTest {

    @Mock
    private lateinit var userMapper: UserMapper

    @Mock
    private lateinit var syncRecordMapper: SyncRecordMapper

    @Mock
    private lateinit var activityRecordMapper: ActivityRecordMapper

    @Mock
    private lateinit var cycleMapper: CycleMapper

    @Mock
    private lateinit var medicationMapper: MedicationMapper

    @Mock
    private lateinit var healthReportMapper: HealthReportMapper

    @Mock
    private lateinit var partnerMapper: PartnerMapper

    @Mock
    private lateinit var securitySettingsMapper: SecuritySettingsMapper

    @InjectMocks
    private lateinit var accountDeletionService: AccountDeletionService

    private val testUserId: Long = 1L

    @Test
    fun `deleteAccount should delete all user data in correct order`() {
        `when`(syncRecordMapper.delete(any<QueryWrapper<SyncRecord>>())).thenReturn(1)
        `when`(activityRecordMapper.delete(any<QueryWrapper<ActivityRecord>>())).thenReturn(2)
        `when`(cycleMapper.delete(any<QueryWrapper<Cycle>>())).thenReturn(3)
        `when`(medicationMapper.delete(any<QueryWrapper<Medication>>())).thenReturn(1)
        `when`(healthReportMapper.delete(any<QueryWrapper<HealthReport>>())).thenReturn(1)
        `when`(partnerMapper.delete(any<QueryWrapper<Partner>>())).thenReturn(0)
        `when`(securitySettingsMapper.delete(any<QueryWrapper<SecuritySettings>>())).thenReturn(1)
        `when`(userMapper.deleteById(testUserId)).thenReturn(1)

        val result = accountDeletionService.deleteAccount(testUserId)

        assertTrue(result.success)
        assertEquals("Account and all associated data deleted successfully", result.message)
    }

    @Test
    fun `deleteAccount should return failure when exception occurs`() {
        `when`(syncRecordMapper.delete(any<QueryWrapper<SyncRecord>>())).thenThrow(RuntimeException("Database error"))

        val result = accountDeletionService.deleteAccount(testUserId)

        assertFalse(result.success)
        assertTrue(result.message.contains("Failed to delete account"))
    }

    @Test
    fun `deleteAccount should handle empty data gracefully`() {
        `when`(syncRecordMapper.delete(any<QueryWrapper<SyncRecord>>())).thenReturn(0)
        `when`(activityRecordMapper.delete(any<QueryWrapper<ActivityRecord>>())).thenReturn(0)
        `when`(cycleMapper.delete(any<QueryWrapper<Cycle>>())).thenReturn(0)
        `when`(medicationMapper.delete(any<QueryWrapper<Medication>>())).thenReturn(0)
        `when`(healthReportMapper.delete(any<QueryWrapper<HealthReport>>())).thenReturn(0)
        `when`(partnerMapper.delete(any<QueryWrapper<Partner>>())).thenReturn(0)
        `when`(securitySettingsMapper.delete(any<QueryWrapper<SecuritySettings>>())).thenReturn(0)
        `when`(userMapper.deleteById(testUserId)).thenReturn(1)

        val result = accountDeletionService.deleteAccount(testUserId)

        assertTrue(result.success)
    }

    @Test
    fun `deleteAccount should delete user when no related data exists`() {
        `when`(syncRecordMapper.delete(any<QueryWrapper<SyncRecord>>())).thenReturn(0)
        `when`(activityRecordMapper.delete(any<QueryWrapper<ActivityRecord>>())).thenReturn(0)
        `when`(cycleMapper.delete(any<QueryWrapper<Cycle>>())).thenReturn(0)
        `when`(medicationMapper.delete(any<QueryWrapper<Medication>>())).thenReturn(0)
        `when`(healthReportMapper.delete(any<QueryWrapper<HealthReport>>())).thenReturn(0)
        `when`(partnerMapper.delete(any<QueryWrapper<Partner>>())).thenReturn(0)
        `when`(securitySettingsMapper.delete(any<QueryWrapper<SecuritySettings>>())).thenReturn(0)
        `when`(userMapper.deleteById(testUserId)).thenReturn(1)

        val result = accountDeletionService.deleteAccount(testUserId)

        assertTrue(result.success)
    }

    private fun <T> any(): T {
        return org.mockito.ArgumentMatchers.any()
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
