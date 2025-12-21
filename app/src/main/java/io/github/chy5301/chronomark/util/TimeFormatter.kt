package io.github.chy5301.chronomark.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 时间格式化工具类
 */
object TimeFormatter {

    /**
     * 格式化经过时间
     *
     * @param nanos 纳秒值
     * @return 格式化的时间字符串（MM:SS.mmm）
     */
    fun formatElapsed(nanos: Long): String {
        // 防御性编程：如果是负数，返回 "00:00.000"
        if (nanos < 0) {
            return "00:00.000"
        }

        val totalMillis = nanos / 1_000_000
        val minutes = totalMillis / 60000
        val seconds = (totalMillis % 60000) / 1000
        val millis = totalMillis % 1000
        return "%02d:%02d.%03d".format(minutes, seconds, millis)
    }

    /**
     * 格式化时间差
     *
     * @param nanos 纳秒值
     * @return 格式化的时间差字符串（+MM:SS.mmm）
     */
    fun formatSplit(nanos: Long): String {
        // 防御性编程：如果是负数，返回 "+00:00.000"
        if (nanos < 0) {
            return "+00:00.000"
        }
        return "+" + formatElapsed(nanos)
    }

    /**
     * 格式化墙上时钟时间（仅时分秒毫秒，用于记录卡片）
     *
     * @param timestampMillis 时间戳（毫秒）
     * @return 格式化的时间字符串（HH:mm:ss.SSS）
     */
    fun formatWallClock(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    /**
     * 格式化墙上时钟时间（带日期，用于主界面显示）
     *
     * @param timestampMillis 时间戳（毫秒）
     * @return 格式化的时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    fun formatWallClockWithDate(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    /**
     * 格式化完整时间戳（用于导出）
     *
     * @param timestampMillis 时间戳（毫秒）
     * @return 格式化的完整时间字符串（yyyy-MM-dd HH:mm:ss.SSS）
     */
    fun formatFullTimestamp(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    /**
     * 格式化日期（用于分享文本）
     *
     * @param timestampMillis 时间戳（毫秒）
     * @return 格式化的日期字符串（yyyy-MM-dd）
     */
    fun formatShareDate(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}
