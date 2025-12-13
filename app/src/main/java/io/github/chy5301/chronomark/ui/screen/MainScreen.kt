package io.github.chy5301.chronomark.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.model.AppMode
import kotlinx.coroutines.launch

/**
 * 主屏幕 - 管理秒表和事件模式的切换
 */
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取当前模式
    val currentMode by dataStoreManager.currentModeFlow.collectAsState(initial = AppMode.EVENT)

    Scaffold(
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
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentMode) {
                AppMode.STOPWATCH -> StopwatchScreen()
                AppMode.EVENT -> EventScreen()
            }
        }
    }
}
