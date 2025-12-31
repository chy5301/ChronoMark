package io.github.chy5301.chronomark.data.database.repository

import android.util.Log
import io.github.chy5301.chronomark.data.database.dao.HistoryDao
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.data.model.TimeRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

/**
 * 历史记录数据仓库
 * 封装历史数据的业务逻辑，提供归档、查询、清理等功能
 *
 * @property historyDao 历史记录 DAO
 */
class HistoryRepository(
    private val historyDao: HistoryDao
) {

    companion object {
        private const val TAG = "HistoryRepository"
    }

    // ========== 归档操作 ==========

    /**
     * 归档事件模式记录到指定日期
     *
     * @param date 归档日期（格式：yyyy-MM-dd）
     * @param records 时间记录列表
     * @return Result<Unit> 成功或失败
     */
    suspend fun archiveEventRecordsByDate(date: String, records: List<TimeRecord>): Result<Unit> {
        return try {
            if (records.isEmpty()) {
                return Result.failure(Exception("No records to archive"))
            }

            val session = HistorySessionEntity(
                id = UUID.randomUUID().toString(),
                date = date,
                sessionType = SessionType.EVENT,
                title = "",  // 事件模式无标题
                createdAt = System.currentTimeMillis()
            )

            val recordEntities = records.map { record ->
                TimeRecordEntity(
                    id = record.id,
                    sessionId = session.id,
                    index = record.index,
                    wallClockTime = record.wallClockTime,
                    elapsedTimeNanos = record.elapsedTimeNanos,
                    splitTimeNanos = record.splitTimeNanos,
                    note = record.note
                )
            }

            historyDao.archiveSession(session, recordEntities)
            Log.i(TAG, "Archived ${records.size} event records for $date")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive event records", e)
            Result.failure(e)
        }
    }

    /**
     * 归档事件模式记录（旧版本，已废弃）
     *
     * @param records 时间记录列表
     * @return Result<Unit> 成功或失败
     * @deprecated 使用 archiveEventRecordsByDate() 替代，该方法假设所有记录都归档到昨天
     */
    @Deprecated(
        message = "使用 archiveEventRecordsByDate() 替代，该方法假设所有记录都归档到昨天",
        replaceWith = ReplaceWith("archiveEventRecordsByDate(date, records)")
    )
    suspend fun archiveEventRecords(records: List<TimeRecord>): Result<Unit> {
        return try {
            if (records.isEmpty()) {
                return Result.failure(Exception("No records to archive"))
            }

            // 归档到昨天的日期
            val yesterday = LocalDate.now().minusDays(1).toString()
            val session = HistorySessionEntity(
                id = UUID.randomUUID().toString(),
                date = yesterday,
                sessionType = SessionType.EVENT,
                title = "",  // 事件模式无标题
                createdAt = System.currentTimeMillis()
            )

            val recordEntities = records.map { record ->
                TimeRecordEntity(
                    id = record.id,
                    sessionId = session.id,
                    index = record.index,
                    wallClockTime = record.wallClockTime,
                    elapsedTimeNanos = record.elapsedTimeNanos,
                    splitTimeNanos = record.splitTimeNanos,
                    note = record.note
                )
            }

            historyDao.archiveSession(session, recordEntities)
            Log.i(TAG, "Archived ${records.size} event records for $yesterday")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive event records", e)
            Result.failure(e)
        }
    }

    /**
     * 归档秒表模式记录
     *
     * @param records 时间记录列表
     * @param title 会话标题
     * @param startTime 会话开始时间（毫秒时间戳）
     * @param totalElapsedNanos 总用时（纳秒）
     * @return Result<Unit> 成功或失败
     */
    suspend fun archiveStopwatchRecords(
        records: List<TimeRecord>,
        title: String,
        startTime: Long,
        totalElapsedNanos: Long
    ): Result<Unit> {
        return try {
            if (records.isEmpty()) {
                return Result.failure(Exception("No records to archive"))
            }

            // 归档到今天的日期
            val today = LocalDate.now().toString()
            val session = HistorySessionEntity(
                id = UUID.randomUUID().toString(),
                date = today,
                sessionType = SessionType.STOPWATCH,
                title = title,
                createdAt = startTime,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                totalElapsedNanos = totalElapsedNanos
            )

            val recordEntities = records.map { record ->
                TimeRecordEntity(
                    id = record.id,
                    sessionId = session.id,
                    index = record.index,
                    wallClockTime = record.wallClockTime,
                    elapsedTimeNanos = record.elapsedTimeNanos,
                    splitTimeNanos = record.splitTimeNanos,
                    note = record.note
                )
            }

            historyDao.archiveSession(session, recordEntities)
            Log.i(TAG, "Archived stopwatch session \"$title\" with ${records.size} records")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive stopwatch records", e)
            Result.failure(e)
        }
    }

    // ========== 查询操作 ==========

    /**
     * 查询指定日期的会话列表
     *
     * @param date 日期字符串（格式：yyyy-MM-dd）
     * @param sessionType 会话类型
     * @return Flow 包装的会话列表
     */
    fun getSessionsByDate(
        date: String,
        sessionType: SessionType
    ): Flow<List<HistorySessionEntity>> {
        return historyDao.getSessionsByDate(date, sessionType)
    }

    /**
     * 查询指定会话的记录列表
     *
     * @param sessionId 会话 ID
     * @return Flow 包装的记录列表
     */
    fun getRecordsBySessionId(sessionId: String): Flow<List<TimeRecordEntity>> {
        return historyDao.getRecordsBySessionId(sessionId)
    }

    /**
     * 查询包含记录的日期列表
     *
     * @param sessionType 会话类型
     * @return Flow 包装的日期字符串列表
     */
    fun getDatesWithRecords(sessionType: SessionType): Flow<List<String>> {
        return historyDao.getDatesWithRecords(sessionType)
    }

    // ========== 更新操作 ==========

    /**
     * 更新会话标题
     *
     * @param sessionId 会话 ID
     * @param title 新标题
     */
    suspend fun updateSessionTitle(sessionId: String, title: String): Result<Unit> {
        return try {
            historyDao.updateSessionTitle(sessionId, title)
            Log.i(TAG, "Updated session title: $sessionId -> \"$title\"")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update session title", e)
            Result.failure(e)
        }
    }

    /**
     * 更新记录备注
     *
     * @param recordId 记录 ID
     * @param note 新备注
     */
    suspend fun updateRecordNote(recordId: String, note: String): Result<Unit> {
        return try {
            historyDao.updateRecordNote(recordId, note)
            Log.i(TAG, "Updated record note: $recordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update record note", e)
            Result.failure(e)
        }
    }

    // ========== 删除操作 ==========

    /**
     * 删除指定会话（级联删除所有记录）
     *
     * @param sessionId 会话 ID
     */
    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            historyDao.deleteSession(sessionId)
            Log.i(TAG, "Deleted session: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session", e)
            Result.failure(e)
        }
    }

    /**
     * 删除指定日期和类型的所有会话
     *
     * @param date 日期字符串
     * @param sessionType 会话类型
     */
    suspend fun deleteSessionsByDateAndType(date: String, sessionType: SessionType): Result<Unit> {
        return try {
            historyDao.deleteSessionsByDateAndType(date, sessionType)
            Log.i(TAG, "Deleted $sessionType sessions for $date")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete sessions", e)
            Result.failure(e)
        }
    }

    /**
     * 删除单条记录
     *
     * @param recordId 记录 ID
     */
    suspend fun deleteRecord(recordId: String): Result<Unit> {
        return try {
            historyDao.deleteRecord(recordId)
            Log.i(TAG, "Deleted record: $recordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete record", e)
            Result.failure(e)
        }
    }

    /**
     * 清空所有历史记录
     */
    suspend fun deleteAllSessions(): Result<Unit> {
        return try {
            historyDao.deleteAllSessions()
            Log.i(TAG, "Deleted all sessions")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete all sessions", e)
            Result.failure(e)
        }
    }

    // ========== 自动清理 ==========

    /**
     * 自动清理旧数据（根据用户设置的保留天数）
     *
     * @param retentionDays 保留天数（-1 表示永久保留，不执行清理）
     * @return Result<Unit> 成功或失败
     */
    suspend fun cleanupOldData(retentionDays: Int): Result<Unit> {
        return try {
            // 永久保留或无效值（防止整型溢出）
            if (retentionDays !in 0..36500) {
                return Result.success(Unit)
            }

            // 至少保留 2 天的数据（今天 + 昨天），避免删除刚归档的数据
            // 即使用户设置为 0 天或 1 天，也要保护刚归档的数据
            val actualRetentionDays = maxOf(retentionDays, 2)

            val cutoffDate = LocalDate.now()
                .minusDays(actualRetentionDays.toLong())
                .toString()

            historyDao.deleteSessionsBeforeDate(cutoffDate)
            Log.i(TAG, "Cleaned up data before $cutoffDate (retention: $retentionDays days, actual: $actualRetentionDays days)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old data", e)
            Result.failure(e)
        }
    }
}
