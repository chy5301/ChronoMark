package io.github.chy5301.chronomark.ui.components.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.chy5301.chronomark.ui.components.dialog.CompactAlertDialog
import java.time.LocalDate

/**
 * 日历选择器对话框
 *
 * 用于历史记录页面选择日期。
 *
 * @param selectedDate 当前选中的日期
 * @param datesWithRecords 有记录的日期集合（显示小圆点）
 * @param onDismiss 关闭对话框的回调
 * @param onDateSelected 选择日期的回调
 */
@Composable
fun CalendarPickerDialog(
    selectedDate: LocalDate,
    datesWithRecords: Set<LocalDate>,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(selectedDate.withDayOfMonth(1)) }
    var tempSelectedDate by remember { mutableStateOf(selectedDate) }

    CompactAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column {
                // 月份选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Text("<", style = MaterialTheme.typography.headlineSmall)
                    }

                    Text(
                        text = "${currentMonth.year}年${currentMonth.monthValue}月",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Text(">", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 星期标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 日历网格
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = tempSelectedDate,
                    datesWithRecords = datesWithRecords,
                    onDateClick = { tempSelectedDate = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected(tempSelectedDate) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 日历网格组件
 *
 * 显示一个月的日期网格，支持选中日期和标记有记录的日期。
 *
 * @param currentMonth 当前显示的月份（LocalDate，只使用年月信息）
 * @param selectedDate 选中的日期
 * @param datesWithRecords 有记录的日期集合
 * @param onDateClick 点击日期的回调
 */
@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    datesWithRecords: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0=周日, 1=周一, ..., 6=周六

    Column {
        var dayOfMonth = 1
        val weeksNeeded = ((firstDayOfWeek + currentMonth.lengthOfMonth() + 6) / 7)

        repeat(weeksNeeded) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    val isInMonth =
                        cellIndex >= firstDayOfWeek && dayOfMonth <= lastDayOfMonth.dayOfMonth

                    if (isInMonth) {
                        val date = currentMonth.withDayOfMonth(dayOfMonth)
                        val isSelected = date == selectedDate
                        val hasRecords = datesWithRecords.contains(date)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                onClick = { onDateClick(date) },
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )

                                        // 有记录的日期显示小圆点
                                        if (hasRecords) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.onPrimaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        dayOfMonth++
                    } else {
                        // 空白占位
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}
