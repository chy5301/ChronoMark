package io.github.chy5301.chronomark.data.model

import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import java.time.LocalDate

/**
 * 事件模式历史记录 UI 状态
 *
 * 事件模式一天只有一个会话（自动归档），因此不需要 currentSessionIndex。
 */
data class EventHistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),     // 当前选中日期
    val sessions: List<HistorySessionEntity> = emptyList(),  // 当前日期的会话（事件模式一天只有一个）
    val selectedSessionRecords: List<TimeRecordEntity> = emptyList(),  // 当前会话的记录
    val datesWithRecords: Set<LocalDate> = emptySet(), // 有记录的日期集合（日历标记用）
    val isLoading: Boolean = false
)
