package io.github.chy5301.chronomark.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 通用的事件/秒表模式切换导航栏组件
 *
 * 这是一个泛型组件，支持不同的模式类型（如 AppMode、SessionType）。
 *
 * @param T 模式类型（必须可比较）
 * @param currentMode 当前选中的模式
 * @param eventMode 事件模式的值
 * @param stopwatchMode 秒表模式的值
 * @param onModeChange 模式切换回调
 * @param modifier 修饰符
 *
 * 使用示例：
 * ```kotlin
 * // MainScreen - 使用 AppMode
 * ModeNavigationBar(
 *     currentMode = currentMode,
 *     eventMode = AppMode.EVENT,
 *     stopwatchMode = AppMode.STOPWATCH,
 *     onModeChange = { mode ->
 *         coroutineScope.launch {
 *             dataStoreManager.saveCurrentMode(mode)
 *         }
 *     }
 * )
 *
 * // HistoryScreen - 使用 SessionType
 * ModeNavigationBar(
 *     currentMode = uiState.currentMode,
 *     eventMode = SessionType.EVENT,
 *     stopwatchMode = SessionType.STOPWATCH,
 *     onModeChange = { mode -> viewModel.switchMode(mode) }
 * )
 * ```
 */
@Composable
fun <T> ModeNavigationBar(
    currentMode: T,
    eventMode: T,
    stopwatchMode: T,
    onModeChange: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        // 事件模式标签
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Event, contentDescription = "事件") },
            label = { Text("事件") },
            selected = currentMode == eventMode,
            onClick = { onModeChange(eventMode) }
        )

        // 秒表模式标签
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Timer, contentDescription = "秒表") },
            label = { Text("秒表") },
            selected = currentMode == stopwatchMode,
            onClick = { onModeChange(stopwatchMode) }
        )
    }
}
