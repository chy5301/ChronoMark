package io.github.chy5301.chronomark.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * ArchiveUtils 单元测试
 *
 * 测试逻辑日期计算和分界点创建功能
 */
class ArchiveUtilsTest {

    // ========== getLogicalDate 测试 ==========

    @Test
    fun `getLogicalDate - before boundary - returns previous day`() {
        // 2025-01-15 03:59:59 with 04:00 boundary -> 应该属于 2025-01-14
        val timestamp = LocalDateTime.of(2025, 1, 15, 3, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val boundary = ArchiveUtils.createBoundaryTime(4, 0)

        val result = ArchiveUtils.getLogicalDate(timestamp, boundary)

        assertEquals(LocalDate.of(2025, 1, 14), result)
    }

    @Test
    fun `getLogicalDate - at boundary exactly - returns current day`() {
        // 2025-01-15 04:00:00 with 04:00 boundary -> 应该属于 2025-01-15
        val timestamp = LocalDateTime.of(2025, 1, 15, 4, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val boundary = ArchiveUtils.createBoundaryTime(4, 0)

        val result = ArchiveUtils.getLogicalDate(timestamp, boundary)

        assertEquals(LocalDate.of(2025, 1, 15), result)
    }

    @Test
    fun `getLogicalDate - after boundary - returns current day`() {
        // 2025-01-15 05:00:00 with 04:00 boundary -> 应该属于 2025-01-15
        val timestamp = LocalDateTime.of(2025, 1, 15, 5, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val boundary = ArchiveUtils.createBoundaryTime(4, 0)

        val result = ArchiveUtils.getLogicalDate(timestamp, boundary)

        assertEquals(LocalDate.of(2025, 1, 15), result)
    }

    @Test
    fun `getLogicalDate - midnight boundary - all times belong to current day`() {
        // 00:00 分界点时，任何时间都 >= 00:00，所以都属于当天
        val boundary = ArchiveUtils.createBoundaryTime(0, 0)

        // 测试 00:00:00
        val midnight = LocalDateTime.of(2025, 1, 15, 0, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(LocalDate.of(2025, 1, 15), ArchiveUtils.getLogicalDate(midnight, boundary))

        // 测试 12:00:00
        val noon = LocalDateTime.of(2025, 1, 15, 12, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(LocalDate.of(2025, 1, 15), ArchiveUtils.getLogicalDate(noon, boundary))

        // 测试 23:59:59
        val endOfDay = LocalDateTime.of(2025, 1, 15, 23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(LocalDate.of(2025, 1, 15), ArchiveUtils.getLogicalDate(endOfDay, boundary))
    }

    @Test
    fun `getLogicalDate - late boundary 23h - most times belong to previous day`() {
        // 23:00 分界点时，22:59 之前都属于前一天
        val boundary = ArchiveUtils.createBoundaryTime(23, 0)

        // 22:59:59 -> 前一天
        val before = LocalDateTime.of(2025, 1, 15, 22, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(LocalDate.of(2025, 1, 14), ArchiveUtils.getLogicalDate(before, boundary))

        // 23:00:00 -> 当天
        val at = LocalDateTime.of(2025, 1, 15, 23, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(LocalDate.of(2025, 1, 15), ArchiveUtils.getLogicalDate(at, boundary))
    }

    // ========== createBoundaryTime 测试 ==========

    @Test
    fun `createBoundaryTime - valid input - returns correct LocalTime`() {
        val result = ArchiveUtils.createBoundaryTime(4, 30)

        assertEquals(LocalTime.of(4, 30), result)
    }

    @Test
    fun `createBoundaryTime - boundary values - returns correct LocalTime`() {
        // 测试边界值
        assertEquals(LocalTime.of(0, 0), ArchiveUtils.createBoundaryTime(0, 0))
        assertEquals(LocalTime.of(23, 59), ArchiveUtils.createBoundaryTime(23, 59))
    }

    @Test
    fun `createBoundaryTime - invalid hour above range - throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            ArchiveUtils.createBoundaryTime(24, 0)
        }
    }

    @Test
    fun `createBoundaryTime - invalid hour below range - throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            ArchiveUtils.createBoundaryTime(-1, 0)
        }
    }

    @Test
    fun `createBoundaryTime - invalid minute above range - throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            ArchiveUtils.createBoundaryTime(4, 60)
        }
    }

    @Test
    fun `createBoundaryTime - invalid minute below range - throws exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            ArchiveUtils.createBoundaryTime(4, -1)
        }
    }
}
