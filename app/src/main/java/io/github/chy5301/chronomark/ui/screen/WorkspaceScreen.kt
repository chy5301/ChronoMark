package io.github.chy5301.chronomark.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.ui.components.navigation.ModeNavigationBar
import io.github.chy5301.chronomark.viewmodel.EventViewModel
import io.github.chy5301.chronomark.viewmodel.EventViewModelFactory
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModel
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModelFactory

/**
 * 主工作区屏幕 - 管理事件和秒表模式的 Tab 切换
 *
 * @param currentMode 当前选中的工作模式（事件/秒表）
 * @param onModeChange 模式切换回调
 * @param onHistoryClick 历史按钮点击回调
 * @param onSettingsClick 设置按钮点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }

    // 创建两个 ViewModel（根据模式使用对应的）
    val stopwatchViewModel: StopwatchViewModel = viewModel(
        factory = StopwatchViewModelFactory(dataStoreManager, historyRepository)
    )
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(dataStoreManager, historyRepository)
    )

    // 根据当前模式获取对应的状态和方法
    val stopwatchUiState by stopwatchViewModel.uiState.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()

    // 读取震动反馈设置
    val vibrationEnabled by dataStoreManager.vibrationEnabledFlow.collectAsState(initial = true)

    // 双击返回退出逻辑
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            // 2秒内再次按下，退出应用
            (context as? Activity)?.finish()
        } else {
            // 第一次按下或超时，显示提示
            lastBackPressTime = currentTime
            Toast.makeText(context, "再按一次退出", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentMode) {
                            AppMode.STOPWATCH -> "秒表"
                            AppMode.EVENT -> "事件"
                        }
                    )
                },
                actions = {
                    // 分享按钮
                    IconButton(
                        onClick = {
                            val records = when (currentMode) {
                                AppMode.STOPWATCH -> stopwatchUiState.records
                                AppMode.EVENT -> eventUiState.records
                            }

                            if (records.isEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "暂无记录",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val shareText = when (currentMode) {
                                    AppMode.STOPWATCH -> stopwatchViewModel.generateShareText()
                                    AppMode.EVENT -> eventViewModel.generateShareText()
                                }
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent =
                                    android.content.Intent.createChooser(sendIntent, "分享记录")
                                context.startActivity(shareIntent)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                    // 历史按钮
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "历史")
                    }
                    // 设置按钮
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            ModeNavigationBar(
                currentMode = currentMode,
                onModeChange = onModeChange
            )
        }
    ) { paddingValues ->
        when (currentMode) {
            AppMode.STOPWATCH -> StopwatchScreen(
                viewModel = stopwatchViewModel,
                paddingValues = paddingValues,
                vibrationEnabled = vibrationEnabled
            )

            AppMode.EVENT -> EventScreen(
                viewModel = eventViewModel,
                paddingValues = paddingValues,
                vibrationEnabled = vibrationEnabled
            )
        }
    }
}
