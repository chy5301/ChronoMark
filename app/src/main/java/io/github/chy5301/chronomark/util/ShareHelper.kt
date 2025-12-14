package io.github.chy5301.chronomark.util

import io.github.chy5301.chronomark.data.model.TimeRecord

/**
 * 分享辅助工具类，用于生成分享文本
 */
object ShareHelper {

    private const val SEPARATOR = "────────────────────────────────────"

    /**
     * 生成秒表模式的分享文本
     *
     * @param records 记录列表
     * @param totalElapsedNanos 总用时（纳秒）
     * @return 格式化的分享文本
     */
    fun generateStopwatchShareText(records: List<TimeRecord>, totalElapsedNanos: Long): String {
        if (records.isEmpty()) {
            return "ChronoMark 秒表记录\n\n暂无记录"
        }

        val sb = StringBuilder()

        // 头部信息
        sb.appendLine("ChronoMark 秒表记录")
        sb.appendLine("记录时间: ${TimeFormatter.formatShareDate(records.first().wallClockTime)}")
        sb.appendLine("总用时: ${TimeFormatter.formatElapsed(totalElapsedNanos)}")
        sb.appendLine("记录数: ${records.size}")
        sb.appendLine(SEPARATOR)

        // 记录列表
        records.forEachIndexed { index, record ->
            sb.appendLine("#%02d".format(record.index))
            sb.appendLine("累计: ${TimeFormatter.formatElapsed(record.elapsedTimeNanos)}")
            sb.appendLine("差值: ${TimeFormatter.formatSplit(record.splitTimeNanos)}")
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (index < records.size - 1) {
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    /**
     * 生成事件模式的分享文本
     *
     * @param records 记录列表
     * @return 格式化的分享文本
     */
    fun generateEventShareText(records: List<TimeRecord>): String {
        if (records.isEmpty()) {
            return "ChronoMark 事件记录\n\n暂无记录"
        }

        val sb = StringBuilder()

        // 头部信息
        sb.appendLine("ChronoMark 事件记录")
        sb.appendLine("记录时间: ${TimeFormatter.formatShareDate(records.first().wallClockTime)}")
        sb.appendLine("记录数: ${records.size}")
        sb.appendLine(SEPARATOR)

        // 记录列表
        records.forEachIndexed { index, record ->
            sb.appendLine("#%02d".format(record.index))
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            sb.appendLine("间隔: ${TimeFormatter.formatSplit(record.splitTimeNanos)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (index < records.size - 1) {
                sb.appendLine()
            }
        }

        return sb.toString()
    }
}
