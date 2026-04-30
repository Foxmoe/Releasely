package top.foxmoe.releasely.services

import app.cash.sqldelight.Query
import app.cash.sqldelight.SqlDriver
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import top.foxmoe.releasely.database.AppDatabase
import top.foxmoe.releasely.database.Cycle
import top.foxmoe.releasely.database.CycleQueries
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for CycleService.
 * Uses MockK to mock the database layer.
 */
class CycleServiceTest {

    private lateinit var mockDriver: SqlDriver
    private lateinit var mockCycleQueries: CycleQueries
    private lateinit var mockDatabase: AppDatabase
    private lateinit var cycleService: CycleService

    // Helper to create a mock Cycle row
    private fun createMockCycle(
        id: String = "test-id-1",
        serverId: String? = null,
        startDate: Long = System.currentTimeMillis() / 1000,
        duration: Long? = 5,
        predictedNext: Long? = null,
        isDeleted: Long = 0L,
        createdAt: Long = System.currentTimeMillis() / 1000,
        updatedAt: Long = System.currentTimeMillis() / 1000
    ): Cycle {
        return Cycle(
            id = id,
            server_id = serverId,
            start_date = startDate,
            duration = duration,
            predicted_next = predictedNext,
            is_deleted = isDeleted,
            created_at = createdAt,
            updated_at = updatedAt
        )
    }

    @BeforeEach
    fun setUp() {
        mockDriver = mockk(relaxed = true)
        mockCycleQueries = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        // Make database return the mock cycleQueries
        every { mockDatabase.cycleQueries } returns mockCycleQueries

        cycleService = CycleService(mockDatabase)
    }

    @Test
    fun `getAllCycles returns list of CycleRecord`() = runTest {
        val now = System.currentTimeMillis() / 1000
        val cycles = listOf(
            createMockCycle(id = "1", startDate = now - 86400 * 30),
            createMockCycle(id = "2", startDate = now - 86400 * 60)
        )

        // Mock the async query execution
        coEvery { mockCycleQueries.getAllCycles().awaitAsList() } returns cycles

        val result = cycleService.getAllCycles()

        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
        coVerify { mockCycleQueries.getAllCycles() }
    }

    @Test
    fun `getCycleById returns CycleRecord when found`() = runTest {
        val cycle = createMockCycle(id = "test-id")
        coEvery { mockCycleQueries.getCycleById("test-id").awaitAsOneOrNull() } returns cycle

        val result = cycleService.getCycleById("test-id")

        assertTrue(result != null)
        assertEquals("test-id", result?.id)
    }

    @Test
    fun `getCycleById returns null when not found`() = runTest {
        coEvery { mockCycleQueries.getCycleById("non-existent").awaitAsOneOrNull() } returns null

        val result = cycleService.getCycleById("non-existent")

        assertNull(result)
    }

    @Test
    fun `insertCycle generates UUID and inserts into database`() = runTest {
        coEvery { mockCycleQueries.insertCycle(any(), any(), any(), any(), any()) } just Runs

        val startDate = System.currentTimeMillis() / 1000
        val duration = 5
        val predictedNext = startDate + 86400 * 28

        val resultId = cycleService.insertCycle(startDate, duration, predictedNext)

        assertTrue(resultId.isNotBlank())
        coVerify {
            mockCycleQueries.insertCycle(
                id = resultId,
                server_id = null,
                start_date = startDate,
                duration = duration.toLong(),
                predicted_next = predictedNext
            )
        }
    }

    @Test
    fun `deleteCycle marks cycle as deleted`() = runTest {
        coEvery { mockCycleQueries.markCycleDeleted("test-id") } just Runs

        cycleService.deleteCycle("test-id")

        coVerify { mockCycleQueries.markCycleDeleted("test-id") }
    }

    @Test
    fun `predictNextPeriod returns null when fewer than 2 cycles`() = runTest {
        val cycles = listOf(createMockCycle(id = "1"))
        coEvery { mockCycleQueries.getAllCycles().awaitAsList() } returns cycles

        val result = cycleService.predictNextPeriod()

        assertNull(result)
    }

    @Test
    fun `predictNextPeriod calculates correctly with 2 cycles`() = runTest {
        val now = System.currentTimeMillis() / 1000
        val cycles = listOf(
            createMockCycle(id = "1", startDate = now),           // latest
            createMockCycle(id = "2", startDate = now - 86400 * 30)  // 30 days before
        )

        coEvery { mockCycleQueries.getAllCycles().awaitAsList() } returns cycles
        coEvery { mockCycleQueries.getLatestCycle().awaitAsOneOrNull() } returns cycles[0]
        coEvery { mockCycleQueries.updateCycle(any(), any(), any(), any()) } just Runs

        val result = cycleService.predictNextPeriod()

        // Should predict ~30 days after the latest cycle start date
        assertTrue(result != null)
        // The predicted next should be approximately startDate + 30 days
        val expectedDiff = 30 * 24 * 60 * 60L
        val actualDiff = result!! - now
        // Allow some tolerance since we sorted
        assertTrue((actualDiff - expectedDiff).let { it < 10 && it > -10 },
            "Expected ~30 days (${expectedDiff}s) but got ${actualDiff}s")
    }

    @Test
    fun `predictNextPeriod uses average of multiple cycles`() = runTest {
        val now = System.currentTimeMillis() / 1000
        val cycles = listOf(
            createMockCycle(id = "1", startDate = now),
            createMockCycle(id = "2", startDate = now - 86400 * 28),  // 28 days apart
            createMockCycle(id = "3", startDate = now - 86400 * 56)   // 28 days apart from previous
        )

        coEvery { mockCycleQueries.getAllCycles().awaitAsList() } returns cycles
        coEvery { mockCycleQueries.getLatestCycle().awaitAsOneOrNull() } returns cycles[0]
        coEvery { mockCycleQueries.updateCycle(any(), any(), any(), any()) } just Runs

        val result = cycleService.predictNextPeriod()

        // Should predict 28 days after (average of 28)
        assertTrue(result != null)
        val expectedDiff = 28 * 24 * 60 * 60L
        val actualDiff = result!! - now
        assertTrue((actualDiff - expectedDiff).let { it < 10 && it > -10 },
            "Expected ~28 days (${expectedDiff}s) but got ${actualDiff}s")
    }

    @Test
    fun `getLatestCycle returns most recent cycle`() = runTest {
        val latestCycle = createMockCycle(id = "latest-id", startDate = System.currentTimeMillis() / 1000)
        coEvery { mockCycleQueries.getLatestCycle().awaitAsOneOrNull() } returns latestCycle

        val result = cycleService.getLatestCycle()

        assertTrue(result != null)
        assertEquals("latest-id", result?.id)
    }

    @Test
    fun `setServerId updates cycle with server id`() = runTest {
        coEvery { mockCycleQueries.setServerId("server-123", "local-id") } just Runs

        cycleService.setServerId("local-id", "server-123")

        coVerify { mockCycleQueries.setServerId("server-123", "local-id") }
    }

    @Test
    fun `getCycleCount returns count of non-deleted cycles`() = runTest {
        coEvery { mockCycleQueries.countCycles().awaitAsOne() } returns 42L

        val result = cycleService.getCycleCount()

        assertEquals(42L, result)
    }
}