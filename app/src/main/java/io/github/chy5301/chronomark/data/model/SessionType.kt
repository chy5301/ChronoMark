package io.github.chy5301.chronomark.data.model

/**
 * 会话类型枚举
 * 用于区分事件模式和秒表模式的历史记录
 */
enum class SessionType {
    /** 事件模式 - 简单的时间点记录 */
    EVENT,

    /** 秒表模式 - 传统秒表计时 */
    STOPWATCH
}
