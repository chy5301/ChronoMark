package io.github.chy5301.chronomark.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间记录实体类
 * 存储历史会话中的时间记录详情
 *
 * @property id 记录唯一标识符（UUID）
 * @property sessionId 关联的会话 ID（外键）
 * @property index 记录序号（从 1 开始）
 * @property wallClockTime 标记时刻（墙上时钟时间戳，毫秒）
 * @property elapsedTimeNanos 累计经过时间（纳秒，秒表模式专用）
 * @property splitTimeNanos 与上一条记录的时间差（纳秒，秒表模式专用）
 * @property note 备注文字
 */
@Entity(
    tableName = "time_records",
    foreignKeys = [
        ForeignKey(
            entity = HistorySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE  // 级联删除：删除会话时自动删除所有记录
        )
    ],
    indices = [Index("session_id")]        // 加速关联查询
)
data class TimeRecordEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,                 // 关联会话 ID

    val index: Int,                        // 记录序号

    @ColumnInfo(name = "wall_clock_time")
    val wallClockTime: Long,               // 标记时刻（毫秒）

    @ColumnInfo(name = "elapsed_time_nanos")
    val elapsedTimeNanos: Long,            // 累计时间（纳秒）

    @ColumnInfo(name = "split_time_nanos")
    val splitTimeNanos: Long,              // 时间差（纳秒）

    val note: String                       // 备注
)
