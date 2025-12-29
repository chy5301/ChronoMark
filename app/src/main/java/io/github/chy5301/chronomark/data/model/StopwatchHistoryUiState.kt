package io.github.chy5301.chronomark.data.model

import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import java.time.LocalDate

/**
 * 秒表模式历史记录 UI 状态
 *
 * 秒表模式一天可能有多个会话（手动保存），因此需要 currentSessionIndex。
 */
data class StopwatchHistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),     // 当前选中日期
    val sessions: List<HistorySessionEntity> = emptyList(),  // 当前日期的会话列表
    val selectedSessionRecords: List<TimeRecordEntity> = emptyList(),  // 当前会话的记录
    val currentSessionIndex: Int = 0,                  // 当前选中的会话索引
    val datesWithRecords: Set<LocalDate> = emptySet(), // 有记录的日期集合（日历标记用）
    val isLoading: Boolean = false
)
