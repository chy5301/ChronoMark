package io.github.chy5301.chronomark.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StopwatchTimingTest {

    private val sec = 1_000_000_000L

    @Test
    fun `same boot - uses realtime delta`() {
        val timing = StopwatchTiming(
            accumulatedNanos = 0L,
            anchorRealtimeNanos = 100 * sec,
            anchorWallMillis = 1_700_000_000_000L
        )

        // 墙上时间被 NTP 校正了 2 秒，应以单调时钟为准
        val elapsed = timing.elapsedSinceAnchorNanos(
            nowRealtimeNanos = 160 * sec,
            nowWallMillis = 1_700_000_062_000L
        )

        assertEquals(60 * sec, elapsed)
    }

    @Test
    fun `after reboot realtime goes backwards - falls back to wall clock delta`() {
        val timing = StopwatchTiming(
            anchorRealtimeNanos = 10_000 * sec,
            anchorWallMillis = 1_700_000_000_000L
        )

        val elapsed = timing.elapsedSinceAnchorNanos(
            nowRealtimeNanos = 50 * sec,
            nowWallMillis = 1_700_000_600_000L
        )

        assertEquals(600 * sec, elapsed)
    }

    @Test
    fun `after reboot with long uptime realtime delta is too small - falls back to wall clock delta`() {
        val timing = StopwatchTiming(
            anchorRealtimeNanos = 100 * sec,
            anchorWallMillis = 1_700_000_000_000L
        )

        // 重启后设备又跑了 200 秒，realtime 恰好大于锚点，但墙上时间过了 1 小时
        val elapsed = timing.elapsedSinceAnchorNanos(
            nowRealtimeNanos = 200 * sec,
            nowWallMillis = 1_700_003_600_000L
        )

        assertEquals(3600 * sec, elapsed)
    }

    @Test
    fun `both clocks go backwards - never negative`() {
        val timing = StopwatchTiming(
            anchorRealtimeNanos = 10_000 * sec,
            anchorWallMillis = 1_700_000_000_000L
        )

        val elapsed = timing.elapsedSinceAnchorNanos(
            nowRealtimeNanos = 50 * sec,
            nowWallMillis = 1_699_999_000_000L
        )

        assertEquals(0L, elapsed)
    }
}
