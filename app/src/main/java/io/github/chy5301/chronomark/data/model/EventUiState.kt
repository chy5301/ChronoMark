package io.github.chy5301.chronomark.data.model

/**
 * 事件模式 UI 状态
 */
data class EventUiState(
    val wallClockTime: String = "0000-00-00 00:00:00", // 格式化的墙上时钟（带日期）
    val records: List<TimeRecord> = emptyList()         // 事件记录列表
)
