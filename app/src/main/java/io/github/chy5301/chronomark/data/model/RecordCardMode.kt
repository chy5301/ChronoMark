package io.github.chy5301.chronomark.data.model

/**
 * 记录卡片显示模式
 *
 * 用于控制 UnifiedRecordCard 的显示样式
 */
enum class RecordCardMode {
    /**
     * 事件模式
     * - 显示：序号 + 标记时刻（大字体）
     * - 不显示累计时间和时间差
     */
    EVENT,

    /**
     * 秒表模式
     * - 显示：序号 + 累计时间（大字体）
     * - 第二行：时间差 + 标记时刻
     */
    STOPWATCH
}
