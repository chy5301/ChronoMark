package io.github.chy5301.chronomark.data.repository

import android.util.Log
import io.github.chy5301.chronomark.data.database.dao.HistoryDao
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * HistoryRepository 单元测试
 *
 * 使用 MockK 模拟 HistoryDao，测试 Repository 层的业务逻辑
 * 注意：需要 Mock android.util.Log，因为在 JVM 单元测试中不可用
 */
class HistoryRepositoryTest {

    private lateinit var repository: HistoryRepository
    private lateinit var mockDao: HistoryDao

    @Before
    fun setup() {
        // Mock android.util.Log（在 JVM 单元测试中不可用）
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockDao = mockk(relaxed = true)
        repository = HistoryRepository(mockDao)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ========== insertEventRecord 测试 ==========

    @Test
    fun `insertEventRecord - no existing session - creates session and record`() = runTest {
        // Arrange
        val date = "2025-01-15"
        val record = TimeRecord(
            id = "test-id-1",
            index = 1,
            wallClockTime = 1705280400000L,
            elapsedTimeNanos = 0L,
            splitTimeNanos = 0L,
            note = "test note"
        )
        val sessionSlot = slot<HistorySessionEntity>()
        val recordSlot = slot<TimeRecordEntity>()

        coEvery { mockDao.getEventSessionByDate(date) } returns null
        coEvery { mockDao.insertSession(capture(sessionSlot)) } just Runs
        coEvery { mockDao.insertRecord(capture(recordSlot)) } just Runs

        // Act
        val result = repository.insertEventRecord(date, record)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.insertSession(any()) }
        coVerify(exactly = 1) { mockDao.insertRecord(any()) }

        // 验证创建的会话
        assertEquals(date, sessionSlot.captured.date)
        assertEquals(SessionType.EVENT, sessionSlot.captured.sessionType)
        assertEquals("", sessionSlot.captured.title)

        // 验证创建的记录
        assertEquals(record.id, recordSlot.captured.id)
        assertEquals(record.index, recordSlot.captured.index)
        assertEquals(record.wallClockTime, recordSlot.captured.wallClockTime)
        assertEquals(record.note, recordSlot.captured.note)
    }

    @Test
    fun `insertEventRecord - existing session - reuses session`() = runTest {
        // Arrange
        val date = "2025-01-15"
        val existingSession = HistorySessionEntity(
            id = "existing-session-id",
            date = date,
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280000000L
        )
        val record = TimeRecord(
            id = "test-id-2",
            index = 2,
            wallClockTime = 1705280500000L,
            elapsedTimeNanos = 0L,
            splitTimeNanos = 100_000_000L,
            note = ""
        )
        val recordSlot = slot<TimeRecordEntity>()

        coEvery { mockDao.getEventSessionByDate(date) } returns existingSession
        coEvery { mockDao.insertRecord(capture(recordSlot)) } just Runs

        // Act
        val result = repository.insertEventRecord(date, record)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { mockDao.insertSession(any()) }
        coVerify(exactly = 1) { mockDao.insertRecord(any()) }
        assertEquals(existingSession.id, recordSlot.captured.sessionId)
    }

    // ========== insertEventRecords 测试 ==========

    @Test
    fun `insertEventRecords - empty list - returns success without db operation`() = runTest {
        // Arrange
        val date = "2025-01-15"
        val emptyRecords = emptyList<TimeRecord>()

        // Act
        val result = repository.insertEventRecords(date, emptyRecords)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { mockDao.getEventSessionByDate(any()) }
        coVerify(exactly = 0) { mockDao.insertSession(any()) }
        coVerify(exactly = 0) { mockDao.insertRecords(any()) }
    }

    @Test
    fun `insertEventRecords - multiple records - batch inserts all`() = runTest {
        // Arrange
        val date = "2025-01-15"
        val records = listOf(
            TimeRecord(id = "id-1", index = 1, wallClockTime = 1705280400000L, elapsedTimeNanos = 0L, splitTimeNanos = 0L),
            TimeRecord(id = "id-2", index = 2, wallClockTime = 1705280500000L, elapsedTimeNanos = 0L, splitTimeNanos = 100_000_000L),
            TimeRecord(id = "id-3", index = 3, wallClockTime = 1705280600000L, elapsedTimeNanos = 0L, splitTimeNanos = 100_000_000L)
        )
        val recordsSlot = slot<List<TimeRecordEntity>>()

        coEvery { mockDao.getEventSessionByDate(date) } returns null
        coEvery { mockDao.insertSession(any()) } just Runs
        coEvery { mockDao.insertRecords(capture(recordsSlot)) } just Runs

        // Act
        val result = repository.insertEventRecords(date, records)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.insertRecords(any()) }
        assertEquals(3, recordsSlot.captured.size)
    }

    // ========== getEventRecordsByDate 测试 ==========

    @Test
    fun `getEventRecordsByDate - existing date - returns records`() = runTest {
        // Arrange
        val date = "2025-01-15"
        val entities = listOf(
            TimeRecordEntity(
                id = "id-1",
                sessionId = "session-1",
                index = 1,
                wallClockTime = 1705280400000L,
                elapsedTimeNanos = 0L,
                splitTimeNanos = 0L,
                note = "note 1"
            ),
            TimeRecordEntity(
                id = "id-2",
                sessionId = "session-1",
                index = 2,
                wallClockTime = 1705280500000L,
                elapsedTimeNanos = 0L,
                splitTimeNanos = 100_000_000L,
                note = "note 2"
            )
        )
        coEvery { mockDao.getEventRecordsByDate(date) } returns entities

        // Act
        val result = repository.getEventRecordsByDate(date)

        // Assert
        assertEquals(2, result.size)
        assertEquals("id-1", result[0].id)
        assertEquals("id-2", result[1].id)
        assertEquals("note 1", result[0].note)
        assertEquals("note 2", result[1].note)
    }

    @Test
    fun `getEventRecordsByDate - non existing date - returns empty list`() = runTest {
        // Arrange
        val date = "2025-01-15"
        coEvery { mockDao.getEventRecordsByDate(date) } returns emptyList()

        // Act
        val result = repository.getEventRecordsByDate(date)

        // Assert
        assertTrue(result.isEmpty())
    }

    // ========== deleteEventRecordsByDate 测试 ==========

    @Test
    fun `deleteEventRecordsByDate - cleans up empty sessions`() = runTest {
        // Arrange
        val date = "2025-01-15"
        coEvery { mockDao.deleteEventRecordsByDate(date) } just Runs
        coEvery { mockDao.deleteEmptyEventSessions() } just Runs

        // Act
        val result = repository.deleteEventRecordsByDate(date)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.deleteEventRecordsByDate(date) }
        coVerify(exactly = 1) { mockDao.deleteEmptyEventSessions() }
    }

    // ========== updateEventRecordNote 测试 ==========

    @Test
    fun `updateEventRecordNote - delegates to dao`() = runTest {
        // Arrange
        val recordId = "test-record-id"
        val newNote = "updated note"
        coEvery { mockDao.updateRecordNote(recordId, newNote) } just Runs

        // Act
        val result = repository.updateEventRecordNote(recordId, newNote)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.updateRecordNote(recordId, newNote) }
    }

    // ========== deleteEventRecord 测试 ==========

    @Test
    fun `deleteEventRecord - delegates to dao`() = runTest {
        // Arrange
        val recordId = "test-record-id"
        coEvery { mockDao.deleteRecord(recordId) } just Runs

        // Act
        val result = repository.deleteEventRecord(recordId)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockDao.deleteRecord(recordId) }
    }
}
