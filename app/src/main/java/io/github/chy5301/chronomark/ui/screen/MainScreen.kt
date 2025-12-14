package io.github.chy5301.chronomark.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.DataStoreManager
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
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取当前模式
    val currentMode by dataStoreManager.currentModeFlow.collectAsState(initial = AppMode.EVENT)

    // 创建两个 ViewModel（根据模式使用对应的）
    val stopwatchViewModel: StopwatchViewModel = viewModel(
        factory = StopwatchViewModelFactory(dataStoreManager)
    )
    val eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(dataStoreManager)
    )

    // 根据当前模式获取对应的状态和方法
    val stopwatchUiState by stopwatchViewModel.uiState.collectAsState()
    val eventUiState by eventViewModel.uiState.collectAsState()

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
                                android.widget.Toast.makeText(context, "暂无记录", android.widget.Toast.LENGTH_SHORT).show()
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
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "分享记录")
                                context.startActivity(shareIntent)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                    // 菜单按钮
                    IconButton(onClick = { /* TODO: 菜单功能 */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "菜单")
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
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        when (currentMode) {
            AppMode.STOPWATCH -> StopwatchScreen(
                viewModel = stopwatchViewModel,
                paddingValues = paddingValues
            )
            AppMode.EVENT -> EventScreen(
                viewModel = eventViewModel,
                paddingValues = paddingValues
            )
        }
    }
}
