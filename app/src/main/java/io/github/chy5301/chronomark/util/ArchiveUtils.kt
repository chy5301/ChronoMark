package io.github.chy5301.chronomark.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 归档工具类
 * 提供基于分界点的逻辑日期计算功能
 */
object ArchiveUtils {

    /**
     * 根据时间戳和分界点计算逻辑日期
     *
     * 逻辑日期定义：
     * - 如果记录时间 < 分界点时间：逻辑日期 = 自然日期 - 1天
     * - 如果记录时间 >= 分界点时间：逻辑日期 = 自然日期
     *
     * 示例（分界点 = 04:00）：
     * - 2025-12-29 03:00 → 逻辑日期 = 2025-12-28（分界点前，属于前一天）
     * - 2025-12-29 05:00 → 逻辑日期 = 2025-12-29（分界点后，属于当天）
     *
     * @param timestampMillis 毫秒级时间戳
     * @param boundaryTime 分界点时间（LocalTime）
     * @return 逻辑日期（LocalDate）
     */
    fun getLogicalDate(
        timestampMillis: Long,
        boundaryTime: LocalTime
    ): LocalDate {
        val zoneId = ZoneId.systemDefault()
        val dateTime = Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .toLocalDateTime()

        return if (dateTime.toLocalTime() < boundaryTime) {
            // 在分界点之前 → 属于前一天
            dateTime.toLocalDate().minusDays(1)
        } else {
            // 在分界点之后 → 属于当天
            dateTime.toLocalDate()
        }
    }

    /**
     * 创建分界点时间
     *
     * @param hour 小时（0-23）
     * @param minute 分钟（0-59）
     * @return LocalTime 对象
     * @throws IllegalArgumentException 如果小时或分钟超出范围
     */
    fun createBoundaryTime(hour: Int, minute: Int): LocalTime {
        require(hour in 0..23) { "Hour must be in range 0-23, got $hour" }
        require(minute in 0..59) { "Minute must be in range 0-59, got $minute" }
        return LocalTime.of(hour, minute)
    }
}
