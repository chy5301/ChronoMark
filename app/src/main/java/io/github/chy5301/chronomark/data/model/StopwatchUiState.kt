package io.github.chy5301.chronomark.data.model

/**
 * 秒表 UI 状态
 *
 * @property status 当前秒表状态
 * @property currentTime 格式化的计时器时间（MM:SS.mmm）
 * @property wallClockTime 格式化的墙上时钟时间（yyyy-MM-dd HH:mm:ss）
 * @property currentTimeNanos 当前累计时间（纳秒，原始值）
 * @property records 时间记录列表
 */
data class StopwatchUiState(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val currentTime: String = "00:00.000",
    val wallClockTime: String = "0000-00-00 00:00:00",
    val currentTimeNanos: Long = 0L,
    val records: List<TimeRecord> = emptyList()
)
