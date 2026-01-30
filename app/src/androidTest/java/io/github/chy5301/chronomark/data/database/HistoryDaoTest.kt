package io.github.chy5301.chronomark.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.chy5301.chronomark.data.database.dao.HistoryDao
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.SessionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * HistoryDao 仪器化测试
 *
 * 使用内存数据库测试 DAO 的 SQL 查询正确性
 */
@RunWith(AndroidJUnit4::class)
class HistoryDaoTest {

    private lateinit var historyDao: HistoryDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        historyDao = db.historyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    // ========== 插入和查询测试 ==========

    @Test
    fun insertSession_andRetrieve_returnsSession() = runTest {
        // Arrange
        val session = HistorySessionEntity(
            id = "session-1",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )

        // Act
        historyDao.insertSession(session)
        val result = historyDao.getEventSessionByDate("2025-01-15")

        // Assert
        assertNotNull(result)
        assertEquals("session-1", result?.id)
        assertEquals(SessionType.EVENT, result?.sessionType)
    }

    @Test
    fun getEventSessionByDate_returnsCorrectSession() = runTest {
        // Arrange - 插入两个不同类型的会话
        val eventSession = HistorySessionEntity(
            id = "event-session",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )
        val stopwatchSession = HistorySessionEntity(
            id = "stopwatch-session",
            date = "2025-01-15",
            sessionType = SessionType.STOPWATCH,
            title = "Test",
            createdAt = 1705280500000L
        )
        historyDao.insertSession(eventSession)
        historyDao.insertSession(stopwatchSession)

        // Act
        val result = historyDao.getEventSessionByDate("2025-01-15")

        // Assert - 应该只返回 EVENT 类型的会话
        assertNotNull(result)
        assertEquals("event-session", result?.id)
        assertEquals(SessionType.EVENT, result?.sessionType)
    }

    @Test
    fun getEventRecordsByDate_orderedByWallClockTime() = runTest {
        // Arrange
        val session = HistorySessionEntity(
            id = "session-1",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )
        historyDao.insertSession(session)

        // 插入乱序的记录
        val records = listOf(
            TimeRecordEntity(
                id = "r3",
                sessionId = "session-1",
                index = 3,
                wallClockTime = 1705280600000L,  // 最晚
                elapsedTimeNanos = 0L,
                splitTimeNanos = 100_000_000L,
                note = "third"
            ),
            TimeRecordEntity(
                id = "r1",
                sessionId = "session-1",
                index = 1,
                wallClockTime = 1705280400000L,  // 最早
                elapsedTimeNanos = 0L,
                splitTimeNanos = 0L,
                note = "first"
            ),
            TimeRecordEntity(
                id = "r2",
                sessionId = "session-1",
                index = 2,
                wallClockTime = 1705280500000L,  // 中间
                elapsedTimeNanos = 0L,
                splitTimeNanos = 100_000_000L,
                note = "second"
            )
        )
        historyDao.insertRecords(records)

        // Act
        val result = historyDao.getEventRecordsByDate("2025-01-15")

        // Assert - 应该按 wallClockTime 升序排列
        assertEquals(3, result.size)
        assertEquals("r1", result[0].id)  // wallClockTime 最早
        assertEquals("r2", result[1].id)
        assertEquals("r3", result[2].id)  // wallClockTime 最晚
    }

    // ========== 删除测试 ==========

    @Test
    fun deleteEventRecordsByDate_deletesOnlyTargetDate() = runTest {
        // Arrange - 插入两个不同日期的会话和记录
        val session1 = HistorySessionEntity(
            id = "session-1",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )
        val session2 = HistorySessionEntity(
            id = "session-2",
            date = "2025-01-16",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705366800000L
        )
        historyDao.insertSession(session1)
        historyDao.insertSession(session2)

        val record1 = TimeRecordEntity(
            id = "r1",
            sessionId = "session-1",
            index = 1,
            wallClockTime = 1705280400000L,
            elapsedTimeNanos = 0L,
            splitTimeNanos = 0L,
            note = ""
        )
        val record2 = TimeRecordEntity(
            id = "r2",
            sessionId = "session-2",
            index = 1,
            wallClockTime = 1705366800000L,
            elapsedTimeNanos = 0L,
            splitTimeNanos = 0L,
            note = ""
        )
        historyDao.insertRecord(record1)
        historyDao.insertRecord(record2)

        // Act - 删除 2025-01-15 的记录
        historyDao.deleteEventRecordsByDate("2025-01-15")

        // Assert
        val date15Records = historyDao.getEventRecordsByDate("2025-01-15")
        val date16Records = historyDao.getEventRecordsByDate("2025-01-16")
        assertTrue(date15Records.isEmpty())
        assertEquals(1, date16Records.size)
    }

    @Test
    fun deleteEmptyEventSessions_cleansUpOrphans() = runTest {
        // Arrange - 创建一个有记录的会话和一个空会话
        val sessionWithRecords = HistorySessionEntity(
            id = "session-with-records",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )
        val emptySession = HistorySessionEntity(
            id = "empty-session",
            date = "2025-01-16",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705366800000L
        )
        historyDao.insertSession(sessionWithRecords)
        historyDao.insertSession(emptySession)

        val record = TimeRecordEntity(
            id = "r1",
            sessionId = "session-with-records",
            index = 1,
            wallClockTime = 1705280400000L,
            elapsedTimeNanos = 0L,
            splitTimeNanos = 0L,
            note = ""
        )
        historyDao.insertRecord(record)

        // Act
        historyDao.deleteEmptyEventSessions()

        // Assert
        val withRecords = historyDao.getEventSessionByDate("2025-01-15")
        val empty = historyDao.getEventSessionByDate("2025-01-16")
        assertNotNull(withRecords)
        assertNull(empty)
    }

    @Test
    fun foreignKey_cascadeDelete_works() = runTest {
        // Arrange
        val session = HistorySessionEntity(
            id = "session-1",
            date = "2025-01-15",
            sessionType = SessionType.EVENT,
            title = "",
            createdAt = 1705280400000L
        )
        historyDao.insertSession(session)

        val records = listOf(
            TimeRecordEntity(
                id = "r1",
                sessionId = "session-1",
                index = 1,
                wallClockTime = 1705280400000L,
                elapsedTimeNanos = 0L,
                splitTimeNanos = 0L,
                note = "first"
            ),
            TimeRecordEntity(
                id = "r2",
                sessionId = "session-1",
                index = 2,
                wallClockTime = 1705280500000L,
                elapsedTimeNanos = 0L,
                splitTimeNanos = 100_000_000L,
                note = "second"
            )
        )
        historyDao.insertRecords(records)

        // Verify records exist
        val recordsBefore = historyDao.getEventRecordsByDate("2025-01-15")
        assertEquals(2, recordsBefore.size)

        // Act - 删除会话
        historyDao.deleteSession("session-1")

        // Assert - 记录应该被级联删除
        val recordsAfter = historyDao.getEventRecordsByDate("2025-01-15")
        assertTrue(recordsAfter.isEmpty())
    }
}
