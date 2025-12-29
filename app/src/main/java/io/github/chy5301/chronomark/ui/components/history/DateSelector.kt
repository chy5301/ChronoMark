package io.github.chy5301.chronomark.ui.components.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chy5301.chronomark.data.model.AppMode
import java.time.LocalDate

/**
 * 日期选择器组件
 *
 * 用于历史记录页面的日期选择和导航。
 *
 * @param selectedDate 当前选中的日期
 * @param currentMode 当前模式（事件/秒表）
 * @param sessionCount 当前日期的会话数量
 * @param onPreviousDay 点击前一天的回调
 * @param onNextDay 点击后一天的回调
 * @param onDateClick 点击日期文本的回调（打开日历选择器）
 * @param modifier 修饰符
 */
@Composable
fun DateSelector(
    selectedDate: LocalDate,
    currentMode: AppMode,
    sessionCount: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 日期显示（大号字体 + 左右箭头）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPreviousDay) {
                Text("<", style = MaterialTheme.typography.headlineLarge)
            }

            Text(
                text = selectedDate.toString(),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.clickable(onClick = onDateClick)
            )

            IconButton(onClick = onNextDay) {
                Text(">", style = MaterialTheme.typography.headlineLarge)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 副标题（星期 + 统计）
        val dayOfWeek = when (selectedDate.dayOfWeek.value) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> ""
        }

        val subtitle = when (currentMode) {
            AppMode.EVENT -> "$dayOfWeek · 共${sessionCount}条"
            AppMode.STOPWATCH -> "$dayOfWeek · ${sessionCount}个会话"
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
