package io.github.chy5301.chronomark.data.model

import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import java.time.LocalDate

/**
 * 历史记录页面 UI 状态
 */
data class HistoryUiState(
    val currentMode: SessionType = SessionType.EVENT,  // 当前选中模式（事件/秒表）
    val selectedDate: LocalDate = LocalDate.now(),     // 当前选中日期
    val sessions: List<HistorySessionEntity> = emptyList(),  // 当前选中日期的会话列表
    val selectedSessionRecords: List<TimeRecordEntity> = emptyList(),  // 当前会话的记录
    val currentSessionIndex: Int = 0,                  // 当前选中的会话索引（秒表模式）
    val datesWithRecords: Set<LocalDate> = emptySet(), // 有记录的日期集合（日历标记用）
    val isLoading: Boolean = false
)
