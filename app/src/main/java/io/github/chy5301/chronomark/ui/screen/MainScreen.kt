package io.github.chy5301.chronomark.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.viewmodel.EventViewModel
import io.github.chy5301.chronomark.viewmodel.EventViewModelFactory
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModel
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModelFactory
import kotlinx.coroutines.launch

/**
 * 主屏幕 - 管理秒表和事件模式的切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取当前模式
    val currentMode by dataStoreManager.currentModeFlow.collectAsState(initial = AppMode.EVENT)

    // 创建两个 ViewModel（根据模式使用对应的）
    val stopwatchViewModel: StopwatchViewModel = viewModel(
        factory = StopwatchViewModelFactory(dataStoreManager)
    )
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(dataStoreManager, historyRepository)
    )

    // 设置页面导航状态
    var showSettings by remember { mutableStateOf(false) }

    // 如果显示设置页面，直接返回设置界面
    if (showSettings) {
        SettingsScreen(onBackClick = { showSettings = false })
        return
    }

    // 根据当前模式获取对应的状态和方法
    val stopwatchUiState by stopwatchViewModel.uiState.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()

    // 读取震动反馈设置
    val vibrationEnabled by dataStoreManager.vibrationEnabledFlow.collectAsState(initial = true)

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
                    // 设置按钮
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Event, contentDescription = "事件") },
                    label = { Text("事件") },
                    selected = currentMode == AppMode.EVENT,
                    onClick = {
                        coroutineScope.launch {
                            dataStoreManager.saveCurrentMode(AppMode.EVENT)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "秒表") },
                    label = { Text("秒表") },
                    selected = currentMode == AppMode.STOPWATCH,
                    onClick = {
                        coroutineScope.launch {
                            dataStoreManager.saveCurrentMode(AppMode.STOPWATCH)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )
            }
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
