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
import io.github.chy5301.chronomark.data.model.AppMode

/**
 * 事件/秒表模式切换导航栏组件
 *
 * 用于 MainScreen 和 HistoryScreen 的模式切换。
 *
 * @param currentMode 当前选中的模式
 * @param onModeChange 模式切换回调
 * @param modifier 修饰符
 *
 * 使用示例：
 * ```kotlin
 * // MainScreen
 * ModeNavigationBar(
 *     currentMode = currentMode,
 *     onModeChange = { mode ->
 *         coroutineScope.launch {
 *             dataStoreManager.saveCurrentMode(mode)
 *         }
 *     }
 * )
 *
 * // HistoryScreen
 * ModeNavigationBar(
 *     currentMode = uiState.currentMode,
 *     onModeChange = { mode -> viewModel.switchMode(mode) }
 * )
 * ```
 */
@Composable
fun ModeNavigationBar(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        // 事件模式标签
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Event, contentDescription = "事件") },
            label = { Text("事件") },
            selected = currentMode == AppMode.EVENT,
            onClick = { onModeChange(AppMode.EVENT) }
        )

        // 秒表模式标签
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Timer, contentDescription = "秒表") },
            label = { Text("秒表") },
            selected = currentMode == AppMode.STOPWATCH,
            onClick = { onModeChange(AppMode.STOPWATCH) }
        )
    }
}
