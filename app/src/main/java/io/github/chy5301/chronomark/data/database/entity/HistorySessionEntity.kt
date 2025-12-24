package io.github.chy5301.chronomark.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.chy5301.chronomark.data.model.SessionType

/**
 * 历史会话实体类
 * 存储历史记录的会话元数据
 *
 * @property id 会话唯一标识符（UUID）
 * @property date 会话日期（格式：yyyy-MM-dd）
 * @property sessionType 会话类型（EVENT 或 STOPWATCH）
 * @property title 会话标题（秒表模式专用，事件模式为空字符串）
 * @property createdAt 创建时间戳（毫秒）
 * @property totalElapsedNanos 总用时（纳秒，秒表模式专用）
 * @property startTime 会话开始时间（毫秒时间戳，秒表模式专用）
 * @property endTime 会话结束时间（毫秒时间戳，秒表模式专用）
 */
@Entity(
    tableName = "history_sessions",
    indices = [
        Index(value = ["date"]),           // 按日期查询索引
        Index(value = ["session_type"])    // 按类型筛选索引
    ]
)
data class HistorySessionEntity(
    @PrimaryKey
    val id: String,

    val date: String,                      // "yyyy-MM-dd"

    @ColumnInfo(name = "session_type")
    val sessionType: SessionType,          // 通过 TypeConverter 转换

    val title: String,                     // 会话标题（秒表专用）

    @ColumnInfo(name = "created_at")
    val createdAt: Long,                   // 创建时间戳

    // 秒表专用字段
    @ColumnInfo(name = "total_elapsed_nanos")
    val totalElapsedNanos: Long = 0L,      // 总用时（纳秒）

    @ColumnInfo(name = "start_time")
    val startTime: Long = 0L,              // 会话开始时间

    @ColumnInfo(name = "end_time")
    val endTime: Long = 0L                 // 会话结束时间
)
