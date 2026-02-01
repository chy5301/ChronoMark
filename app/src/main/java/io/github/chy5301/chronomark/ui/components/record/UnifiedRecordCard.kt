package io.github.chy5301.chronomark.ui.components.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.RecordCardMode
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.ui.theme.TabularNumbersStyle
import io.github.chy5301.chronomark.util.TimeFormatter

/**
 * 统一记录卡片组件 - TimeRecord 版本（工作区数据）
 *
 * 支持事件模式和秒表模式两种显示样式
 *
 * @param record 时间记录数据（TimeRecord）
 * @param index 动态计算的序号（从 1 开始）
 * @param mode 显示模式（EVENT 或 STOPWATCH）
 * @param onClick 点击卡片的回调
 * @param modifier 修饰符
 */
@Composable
fun UnifiedRecordCard(
    record: TimeRecord,
    index: Int,
    mode: RecordCardMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedRecordCardImpl(
        index = index,
        wallClockTime = record.wallClockTime,
        elapsedTimeNanos = record.elapsedTimeNanos,
        splitTimeNanos = record.splitTimeNanos,
        note = record.note,
        mode = mode,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * 统一记录卡片组件 - TimeRecordEntity 版本（历史数据）
 *
 * 支持事件模式和秒表模式两种显示样式
 *
 * @param record 时间记录实体（TimeRecordEntity）
 * @param index 动态计算的序号（从 1 开始）
 * @param mode 显示模式（EVENT 或 STOPWATCH）
 * @param onClick 点击卡片的回调
 * @param modifier 修饰符
 */
@Composable
fun UnifiedRecordCard(
    record: TimeRecordEntity,
    index: Int,
    mode: RecordCardMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedRecordCardImpl(
        index = index,
        wallClockTime = record.wallClockTime,
        elapsedTimeNanos = record.elapsedTimeNanos,
        splitTimeNanos = record.splitTimeNanos,
        note = record.note,
        mode = mode,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * 统一记录卡片组件的内部实现
 *
 * 根据 mode 参数显示不同的布局：
 * - EVENT: 序号 + 标记时刻（大字体）+ 备注
 * - STOPWATCH: 序号 + 累计时间（大字体）+ 时间差 + 标记时刻 + 备注
 */
@Composable
private fun UnifiedRecordCardImpl(
    index: Int,
    wallClockTime: Long,
    elapsedTimeNanos: Long,
    splitTimeNanos: Long,
    note: String,
    mode: RecordCardMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 第一行：序号 + 主要时间信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 序号
                Text(
                    text = "%02d".format(index),
                    style = TabularNumbersStyle,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 主要时间信息（根据模式不同显示不同内容）
                when (mode) {
                    RecordCardMode.EVENT -> {
                        // 事件模式：显示标记时刻
                        Text(
                            text = TimeFormatter.formatWallClock(wallClockTime),
                            style = TabularNumbersStyle,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    RecordCardMode.STOPWATCH -> {
                        // 秒表模式：显示累计时间
                        Text(
                            text = TimeFormatter.formatElapsed(elapsedTimeNanos),
                            style = TabularNumbersStyle,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 秒表模式的第二行：时间差 + 标记时刻
            if (mode == RecordCardMode.STOPWATCH) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 时间差
                    Text(
                        text = TimeFormatter.formatSplit(splitTimeNanos),
                        style = TabularNumbersStyle,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    // 标记时刻
                    Text(
                        text = TimeFormatter.formatWallClock(wallClockTime),
                        style = TabularNumbersStyle,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 备注（如果有）
            if (note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
