package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import top.foxmoe.releasely.dto.ExportData
import top.foxmoe.releasely.entity.*
import top.foxmoe.releasely.mapper.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ExportServiceTest {

    @Mock
    private lateinit var userMapper: UserMapper

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
    private lateinit var exportService: ExportService

    private lateinit var testUser: User
    private var testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        testUserId = 1L
        testUser = User(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `exportUserData should return all user data`() {
        // Mock user
        `when`(userMapper.selectById(testUserId)).thenReturn(testUser)

        // Mock activity records
        val activities = listOf(
            ActivityRecord(
                id = 1L,
                userId = testUserId,
                type = "MASTURBATION",
                protection = "NONE",
                pleasureRating = 5,
                healthStatus = "GOOD",
                occurredAt = LocalDateTime.now(),
                isDeleted = false,
                createdAt = LocalDateTime.now()
            )
        )
        `when`(activityRecordMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<ActivityRecord>>()))
            .thenReturn(activities)

        // Mock cycles
        val cycles = listOf(
            Cycle(
                id = 1L,
                userId = testUserId,
                startDate = LocalDate.now().minusDays(28),
                duration = 5,
                isDeleted = false,
                createdAt = LocalDateTime.now()
            )
        )
        `when`(cycleMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Cycle>>()))
            .thenReturn(cycles)

        // Mock medications
        val medications = listOf(
            Medication(
                id = 1L,
                userId = testUserId,
                name = "Vitamin D",
                dosage = "1000IU",
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )
        `when`(medicationMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Medication>>()))
            .thenReturn(medications)

        // Mock health reports
        val healthReports = listOf(
            HealthReport(
                id = 1L,
                userId = testUserId,
                period = "weekly",
                frequencyData = "{\"count\": 3}",
                protectionRate = 0.66,
                createdAt = LocalDateTime.now()
            )
        )
        `when`(healthReportMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<HealthReport>>()))
            .thenReturn(healthReports)

        // Mock partners
        val partners = emptyList<Partner>()
        `when`(partnerMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Partner>>()))
            .thenReturn(partners)

        // Mock security settings
        val securitySettings = SecuritySettings(
            id = 1L,
            userId = testUserId,
            lockType = "PIN",
            isAppLockEnabled = true,
            is2FAEnabled = false,
            createdAt = LocalDateTime.now()
        )
        `when`(securitySettingsMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<SecuritySettings>>()))
            .thenReturn(listOf(securitySettings))

        val result = exportService.exportUserData(testUserId)

        assertNotNull(result)
        assertNotNull(result.exportDate)
        assertEquals(testUserId, result.user.id)
        assertEquals("testuser", result.user.username)
        assertEquals("test@example.com", result.user.email)
        assertEquals(1, result.activityRecords.size)
        assertEquals(1, result.cycles.size)
        assertEquals(1, result.medications.size)
        assertEquals(1, result.healthReports.size)
        assertEquals(0, result.partners.size)
        assertEquals("PIN", result.securitySettings.lockType)
    }

    @Test
    fun `exportUserData should handle empty data`() {
        `when`(userMapper.selectById(testUserId)).thenReturn(testUser)
        `when`(activityRecordMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<ActivityRecord>>()))
            .thenReturn(emptyList())
        `when`(cycleMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Cycle>>()))
            .thenReturn(emptyList())
        `when`(medicationMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Medication>>()))
            .thenReturn(emptyList())
        `when`(healthReportMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<HealthReport>>()))
            .thenReturn(emptyList())
        `when`(partnerMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<Partner>>()))
            .thenReturn(emptyList())
        `when`(securitySettingsMapper.selectList(org.mockito.ArgumentMatchers.any<QueryWrapper<SecuritySettings>>()))
            .thenReturn(emptyList())

        val result = exportService.exportUserData(testUserId)

        assertNotNull(result)
        assertEquals(testUserId, result.user.id)
        assertEquals(0, result.activityRecords.size)
        assertEquals(0, result.cycles.size)
        assertEquals(0, result.medications.size)
        assertEquals(0, result.healthReports.size)
        assertEquals(0, result.partners.size)
        assertEquals(null, result.securitySettings.lockType)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}