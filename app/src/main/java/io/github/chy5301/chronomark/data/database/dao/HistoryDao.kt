package io.github.chy5301.chronomark.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.SessionType
import kotlinx.coroutines.flow.Flow

/**
 * 历史记录数据访问对象（DAO）
 * 定义所有数据库操作方法
 */
@Dao
interface HistoryDao {

    // ========== 查询操作 ==========

    /**
     * 查询指定日期的所有会话（用于历史页面）
     *
     * @param date 日期字符串（格式：yyyy-MM-dd）
     * @param sessionType 会话类型
     * @return Flow 包装的会话列表（响应式）
     */
    @Transaction
    @Query(
        """
        SELECT * FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
        ORDER BY created_at ASC
    """
    )
    fun getSessionsByDate(date: String, sessionType: SessionType): Flow<List<HistorySessionEntity>>

    /**
     * 查询指定会话的所有记录
     *
     * @param sessionId 会话 ID
     * @return Flow 包装的记录列表（响应式）
     */
    @Query(
        """
        SELECT * FROM time_records
        WHERE session_id = :sessionId
        ORDER BY `index` ASC
    """
    )
    fun getRecordsBySessionId(sessionId: String): Flow<List<TimeRecordEntity>>

    /**
     * 查询包含记录的日期列表（用于日历标记）
     *
     * @param sessionType 会话类型
     * @return Flow 包装的日期字符串列表
     */
    @Query(
        """
        SELECT DISTINCT date FROM history_sessions
        WHERE session_type = :sessionType
        ORDER BY date DESC
    """
    )
    fun getDatesWithRecords(sessionType: SessionType): Flow<List<String>>

    /**
     * 查询指定日期的会话数量
     *
     * @param date 日期字符串
     * @param sessionType 会话类型
     * @return 会话数量
     */
    @Query(
        """
        SELECT COUNT(*) FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
    """
    )
    suspend fun getSessionCountByDate(date: String, sessionType: SessionType): Int

    /**
     * 查询指定会话的记录数量
     *
     * @param sessionId 会话 ID
     * @return 记录数量
     */
    @Query(
        """
        SELECT COUNT(*) FROM time_records
        WHERE session_id = :sessionId
    """
    )
    suspend fun getRecordCountBySessionId(sessionId: String): Int

    // ========== 插入操作 ==========

    /**
     * 插入会话（归档时使用）
     *
     * @param session 会话实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HistorySessionEntity)

    /**
     * 批量插入记录（归档时使用）
     *
     * @param records 记录实体列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TimeRecordEntity>)

    /**
     * 归档会话（事务操作，保证原子性）
     * 同时插入会话和记录，要么全部成功，要么全部失败
     *
     * @param session 会话实体
     * @param records 记录实体列表
     */
    @Transaction
    suspend fun archiveSession(session: HistorySessionEntity, records: List<TimeRecordEntity>) {
        insertSession(session)
        insertRecords(records)
    }

    // ========== 更新操作 ==========

    /**
     * 更新会话标题（秒表模式编辑标题）
     *
     * @param sessionId 会话 ID
     * @param title 新标题
     */
    @Query("UPDATE history_sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String)

    /**
     * 更新记录备注（编辑单条记录）
     *
     * @param recordId 记录 ID
     * @param note 新备注
     */
    @Query("UPDATE time_records SET note = :note WHERE id = :recordId")
    suspend fun updateRecordNote(recordId: String, note: String)

    // ========== 删除操作 ==========

    /**
     * 删除指定会话（级联删除所有记录）
     *
     * @param sessionId 会话 ID
     */
    @Query("DELETE FROM history_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    /**
     * 删除指定日期和类型的会话（通用方法）
     * 用于事件模式删除当天所有记录
     *
     * @param date 日期字符串
     * @param sessionType 会话类型
     */
    @Query(
        """
        DELETE FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
    """
    )
    suspend fun deleteSessionsByDateAndType(date: String, sessionType: SessionType)

    /**
     * 删除单条记录
     *
     * @param recordId 记录 ID
     */
    @Query("DELETE FROM time_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String)

    /**
     * 清空所有历史记录（设置页面危险操作）
     */
    @Query("DELETE FROM history_sessions")
    suspend fun deleteAllSessions()

    /**
     * 删除指定日期之前的旧数据（自动清理）
     *
     * @param beforeDate 截止日期（该日期之前的数据将被删除）
     */
    @Query("DELETE FROM history_sessions WHERE date < :beforeDate")
    suspend fun deleteSessionsBeforeDate(beforeDate: String)
}
