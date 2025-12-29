package io.github.chy5301.chronomark.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.data.model.AppScreen
import kotlinx.coroutines.launch

/**
 * 应用主屏幕 - 顶层页面导航管理器
 *
 * 负责管理应用的三个主要页面：
 * - WORKSPACE: 主工作区（包含事件/秒表 Tab 切换）
 * - HISTORY: 历史记录查看
 * - SETTINGS: 应用设置
 *
 * 状态管理策略：
 * - currentMode: 持久化到 DataStore，记住用户的工作模式偏好
 * - currentScreen: 不持久化，每次启动从 WORKSPACE 开始
 * - 历史记录的模式切换不影响主工作区的模式
 */
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取当前工作模式（持久化）
    val currentMode by dataStoreManager.currentModeFlow.collectAsState(initial = AppMode.EVENT)

    // 当前显示的页面（不持久化，每次启动从工作区开始）
    var currentScreen by remember { mutableStateOf(AppScreen.WORKSPACE) }

    when (currentScreen) {
        AppScreen.WORKSPACE -> WorkspaceScreen(
            currentMode = currentMode,
            onModeChange = { mode ->
                // 保存模式切换到 DataStore
                coroutineScope.launch {
                    dataStoreManager.saveCurrentMode(mode)
                        .onFailure { e -> e.printStackTrace() }
                }
            },
            onHistoryClick = {
                currentScreen = AppScreen.HISTORY
            },
            onSettingsClick = {
                currentScreen = AppScreen.SETTINGS
            }
        )

        AppScreen.HISTORY -> {
            // 根据当前模式选择对应的历史页面
            when (currentMode) {
                AppMode.EVENT -> EventHistoryScreen(
                    onBackClick = {
                        currentScreen = AppScreen.WORKSPACE
                    },
                    onSettingsClick = {
                        currentScreen = AppScreen.SETTINGS
                    },
                    onModeChange = { mode ->
                        // 保存模式切换到 DataStore
                        coroutineScope.launch {
                            dataStoreManager.saveCurrentMode(mode)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )

                AppMode.STOPWATCH -> StopwatchHistoryScreen(
                    onBackClick = {
                        currentScreen = AppScreen.WORKSPACE
                    },
                    onSettingsClick = {
                        currentScreen = AppScreen.SETTINGS
                    },
                    onModeChange = { mode ->
                        // 保存模式切换到 DataStore
                        coroutineScope.launch {
                            dataStoreManager.saveCurrentMode(mode)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )
            }
        }

        AppScreen.SETTINGS -> SettingsScreen(
            onBackClick = {
                currentScreen = AppScreen.WORKSPACE
            }
        )
    }
}
