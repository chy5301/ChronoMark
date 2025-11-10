package io.github.chy5301.chronomark.data.model

/**
 * 秒表状态
 */
sealed class StopwatchStatus {
    /**
     * 初始状态（未开始）
     */
    data object Idle : StopwatchStatus()

    /**
     * 运行中
     */
    data object Running : StopwatchStatus()

    /**
     * 暂停
     */
    data object Paused : StopwatchStatus()

    /**
     * 停止（有记录）
     */
    data object Stopped : StopwatchStatus()
}
