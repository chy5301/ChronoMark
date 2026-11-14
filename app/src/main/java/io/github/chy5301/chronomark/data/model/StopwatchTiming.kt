package io.github.chy5301.chronomark.data.model

/**
 * 秒表的持久化计时基准
 *
 * 不直接保存「已计时长」，而是保存可以随时还原出已计时长的锚点：
 * 运行中的已计时长 = [accumulatedNanos] + (当前时刻 - 锚点时刻)。
 * 这样进程在运行中被杀死后，恢复时仍能得到正确的时长。
 *
 * @property accumulatedNanos 截至锚点时刻的已计时长（纳秒）；暂停/停止时即为总时长
 * @property anchorRealtimeNanos 最近一次开始/继续时的 SystemClock.elapsedRealtimeNanos()，仅运行中有意义
 * @property anchorWallMillis 与 [anchorRealtimeNanos] 同一时刻的墙上时间，设备重启后作兜底；0 表示没有锚点（旧版数据）
 * @property startWallMillis 本次会话首次开始计时的墙上时间；0 表示未知（旧版数据）
 */
data class StopwatchTiming(
    val accumulatedNanos: Long = 0L,
    val anchorRealtimeNanos: Long = 0L,
    val anchorWallMillis: Long = 0L,
    val startWallMillis: Long = 0L
) {
    /** 是否带有可用于还原运行中时长的锚点 */
    val hasAnchor: Boolean get() = anchorWallMillis > 0L

    /**
     * 计算从锚点到现在经过的时间（纳秒）
     *
     * 同一次开机内优先用 elapsedRealtime（单调、含深度休眠，不受改时间影响）；
     * 设备重启后 elapsedRealtime 归零，此时它会倒退或明显少于墙上时间的差值，改用墙上时间兜底。
     */
    fun elapsedSinceAnchorNanos(nowRealtimeNanos: Long, nowWallMillis: Long): Long {
        val realtimeDelta = nowRealtimeNanos - anchorRealtimeNanos
        val wallDelta = (nowWallMillis - anchorWallMillis) * 1_000_000L
        if (realtimeDelta >= 0 && wallDelta - realtimeDelta <= REBOOT_DETECTION_TOLERANCE_NANOS) {
            return realtimeDelta
        }
        return wallDelta.coerceAtLeast(0L)
    }

    companion object {
        /** 墙上时间比单调时钟多走超过该值时，认为中间发生过重启（容忍 NTP 校时等小幅跳变） */
        private const val REBOOT_DETECTION_TOLERANCE_NANOS = 60L * 1_000_000_000L
    }
}
