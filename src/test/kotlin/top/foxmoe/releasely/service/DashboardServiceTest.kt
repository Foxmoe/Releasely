package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import top.foxmoe.releasely.entity.ActivityRecord
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.entity.Medication
import top.foxmoe.releasely.entity.Partner
import top.foxmoe.releasely.mapper.ActivityRecordMapper
import top.foxmoe.releasely.mapper.CycleMapper
import top.foxmoe.releasely.mapper.MedicationMapper
import top.foxmoe.releasely.mapper.PartnerMapper
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class DashboardServiceTest {

    @Mock
    private lateinit var activityMapper: ActivityRecordMapper

    @Mock
    private lateinit var cycleMapper: CycleMapper

    @Mock
    private lateinit var medicationMapper: MedicationMapper

    @Mock
    private lateinit var partnerMapper: PartnerMapper

    @InjectMocks
    private lateinit var dashboardService: DashboardService

    private lateinit var testUserId: Long

    @BeforeEach
    fun setup() {
        testUserId = 1L
    }

    @Test
    fun `getStats should return correct counts`() {
        `when`(activityMapper.selectCount(any(QueryWrapper::class.java))).thenReturn(10L)
        `when`(cycleMapper.selectCount(any(QueryWrapper::class.java))).thenReturn(2L)
        `when`(medicationMapper.selectCount(any(QueryWrapper::class.java))).thenReturn(3L)
        `when`(partnerMapper.selectCount(any(QueryWrapper::class.java))).thenReturn(1L)

        val result = dashboardService.getStats(testUserId)

        assertEquals(10, result.totalActivities)
        assertEquals(2, result.totalCycles)
        assertEquals(3, result.activeMedications)
        assertEquals(1, result.partnerCount)
    }

    @Test
    fun `getRecentActivities should return limited activities`() {
        val activities = listOf(
            ActivityRecord(id = 1L, userId = testUserId),
            ActivityRecord(id = 2L, userId = testUserId),
            ActivityRecord(id = 3L, userId = testUserId)
        )
        `when`(activityMapper.selectList(any(QueryWrapper::class.java))).thenReturn(activities)

        val result = dashboardService.getRecentActivities(testUserId, 2)

        assertEquals(2, result.size)
    }

    @Test
    fun `getCyclePrediction should return null when less than 2 cycles`() {
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(
            Cycle(id = 1L, userId = testUserId, startDate = LocalDate.now())
        ))

        val result = dashboardService.getCyclePrediction(testUserId)

        assertNull(result)
    }

    @Test
    fun `getCyclePrediction should calculate prediction correctly`() {
        val now = LocalDate.now()
        val previousCycle = Cycle(
            id = 1L,
            userId = testUserId,
            startDate = now.minusDays(28)
        )
        val latestCycle = Cycle(
            id = 2L,
            userId = testUserId,
            startDate = now
        )
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(latestCycle, previousCycle))

        val result = dashboardService.getCyclePrediction(testUserId)

        assertNotNull(result)
        assertEquals(28, result.averageCycleLength)
        assertEquals(now.plusDays(28), result.predictedNext)
    }

    @Test
    fun `getCyclePrediction should handle null startDate`() {
        val previousCycle = Cycle(
            id = 1L,
            userId = testUserId,
            startDate = null
        )
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(previousCycle))

        val result = dashboardService.getCyclePrediction(testUserId)

        assertNull(result)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}