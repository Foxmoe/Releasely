package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.mapper.CycleMapper
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CycleServiceTest {

    @Mock
    private lateinit var cycleMapper: CycleMapper

    @InjectMocks
    private lateinit var cycleService: CycleService

    private lateinit var testCycle: Cycle
    private lateinit var testUserId: Long

    @BeforeEach
    fun setup() {
        testUserId = 1L
        testCycle = Cycle(
            id = 1L,
            userId = testUserId,
            startDate = LocalDate.now().minusDays(28),
            duration = 5,
            isDeleted = false,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `getCyclesByUserId should return cycles for user`() {
        val cycles = listOf(testCycle)
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(cycles)

        val result = cycleService.getCyclesByUserId(testUserId)

        assertEquals(1, result.size)
        assertEquals(testCycle.id, result[0].id)
    }

    @Test
    fun `getCycleById should return cycle when exists`() {
        `when`(cycleMapper.selectById(testCycle.id)).thenReturn(testCycle)

        val result = cycleService.getCycleById(testCycle.id!!)

        assertNotNull(result)
        assertEquals(testCycle.id, result.id)
    }

    @Test
    fun `getCycleById should return null when not exists`() {
        `when`(cycleMapper.selectById(999L)).thenReturn(null)

        val result = cycleService.getCycleById(999L)

        assertNull(result)
    }

    @Test
    fun `createCycle should insert and return id`() {
        `when`(cycleMapper.insert(any(Cycle::class.java))).thenAnswer { invocation ->
            val cycle = invocation.getArgument<Cycle>(0)
            cycle.id = 1L
            1
        }

        val result = cycleService.createCycle(testCycle)

        assertEquals(1L, result)
    }

    @Test
    fun `deleteCycle should set isDeleted to true`() {
        `when`(cycleMapper.selectById(testCycle.id)).thenReturn(testCycle)
        `when`(cycleMapper.updateById(any(Cycle::class.java))).thenReturn(1)

        val result = cycleService.deleteCycle(testCycle.id!!)

        assertTrue(result)
    }

    @Test
    fun `deleteCycle should return false when cycle not found`() {
        `when`(cycleMapper.selectById(999L)).thenReturn(null)

        val result = cycleService.deleteCycle(999L)

        assertFalse(result)
    }

    @Test
    fun `predictNextPeriod should return null when less than 2 cycles`() {
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(testCycle))

        val result = cycleService.predictNextPeriod(testUserId)

        assertNull(result)
    }

    @Test
    fun `predictNextPeriod should calculate correctly with 2 cycles`() {
        val previousCycle = testCycle.copy(
            id = 2L,
            startDate = testCycle.startDate?.minusDays(28)
        )
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(testCycle, previousCycle))
        `when`(cycleMapper.updateById(any(Cycle::class.java))).thenReturn(1)

        val result = cycleService.predictNextPeriod(testUserId)

        assertNotNull(result)
        assertEquals(testCycle.startDate?.plusDays(28), result)
    }

    @Test
    fun `calculateAverageCycleLength should return duration when only one cycle`() {
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(testCycle))

        val result = cycleService.calculateAverageCycleLength(testUserId)

        assertEquals(testCycle.duration, result)
    }

    @Test
    fun `calculateAverageCycleLength should return average when multiple cycles`() {
        val previousCycle = testCycle.copy(
            id = 2L,
            startDate = testCycle.startDate?.minusDays(28)
        )
        `when`(cycleMapper.selectList(any(QueryWrapper::class.java))).thenReturn(listOf(testCycle, previousCycle))

        val result = cycleService.calculateAverageCycleLength(testUserId)

        assertEquals(28, result)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}