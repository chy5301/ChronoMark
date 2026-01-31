package io.github.chy5301.chronomark.util

import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.SessionType
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

        // 记录列表（秒表模式：记录按时间倒序存储，序号从大到小）
        records.forEachIndexed { listIndex, record ->
            val displayIndex = records.size - listIndex
            sb.appendLine("#%02d".format(displayIndex))
            sb.appendLine("累计: ${TimeFormatter.formatElapsed(record.elapsedTimeNanos)}")
            sb.appendLine("差值: ${TimeFormatter.formatSplit(record.splitTimeNanos)}")
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (listIndex < records.size - 1) {
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

        // 记录列表（事件模式：记录按时间升序存储，序号从 1 开始）
        records.forEachIndexed { listIndex, record ->
            val displayIndex = listIndex + 1
            sb.appendLine("#%02d".format(displayIndex))
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (listIndex < records.size - 1) {
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    /**
     * 生成历史记录的分享文本（自动判断类型）
     *
     * @param session 历史会话
     * @param records 记录列表
     * @return 格式化的分享文本
     */
    fun generateHistoryShareText(
        session: HistorySessionEntity,
        records: List<TimeRecordEntity>
    ): String {
        return when (session.sessionType) {
            SessionType.STOPWATCH -> generateHistoryStopwatchShareText(session, records)
            SessionType.EVENT -> generateHistoryEventShareText(session, records)
        }
    }

    /**
     * 生成历史秒表会话的分享文本
     *
     * @param session 秒表会话
     * @param records 记录列表
     * @return 格式化的分享文本
     */
    private fun generateHistoryStopwatchShareText(
        session: HistorySessionEntity,
        records: List<TimeRecordEntity>
    ): String {
        if (records.isEmpty()) {
            return "ChronoMark 秒表记录\n\n暂无记录"
        }

        val sb = StringBuilder()

        // 头部信息（包含会话标题）
        sb.appendLine("ChronoMark 秒表记录")
        if (session.title.isNotBlank()) {
            sb.appendLine("标题: ${session.title}")
        }
        sb.appendLine("记录时间: ${TimeFormatter.formatShareDate(session.startTime)}")
        sb.appendLine("总用时: ${TimeFormatter.formatElapsed(session.totalElapsedNanos)}")
        sb.appendLine("记录数: ${records.size}")
        sb.appendLine(SEPARATOR)

        // 记录列表（历史数据按时间升序查询，序号从 1 开始）
        records.forEachIndexed { listIndex, record ->
            val displayIndex = listIndex + 1
            sb.appendLine("#%02d".format(displayIndex))
            sb.appendLine("累计: ${TimeFormatter.formatElapsed(record.elapsedTimeNanos)}")
            sb.appendLine("差值: ${TimeFormatter.formatSplit(record.splitTimeNanos)}")
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (listIndex < records.size - 1) {
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    /**
     * 生成历史事件会话的分享文本
     *
     * @param session 事件会话
     * @param records 记录列表
     * @return 格式化的分享文本
     */
    private fun generateHistoryEventShareText(
        session: HistorySessionEntity,
        records: List<TimeRecordEntity>
    ): String {
        if (records.isEmpty()) {
            return "ChronoMark 事件记录\n\n暂无记录"
        }

        val sb = StringBuilder()

        // 头部信息
        sb.appendLine("ChronoMark 事件记录")
        sb.appendLine("记录时间: ${session.date}")
        sb.appendLine("记录数: ${records.size}")
        sb.appendLine(SEPARATOR)

        // 记录列表（历史数据按时间升序查询，序号从 1 开始）
        records.forEachIndexed { listIndex, record ->
            val displayIndex = listIndex + 1
            sb.appendLine("#%02d".format(displayIndex))
            sb.appendLine("时间: ${TimeFormatter.formatWallClock(record.wallClockTime)}")
            if (record.note.isNotBlank()) {
                sb.appendLine("备注: ${record.note}")
            }
            // 记录之间添加空行（最后一条除外）
            if (listIndex < records.size - 1) {
                sb.appendLine()
            }
        }

        return sb.toString()
    }
}
