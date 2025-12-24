package io.github.chy5301.chronomark.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.viewmodel.HistoryViewModel
import io.github.chy5301.chronomark.viewmodel.HistoryViewModelFactory

/**
 * 历史记录页面
 *
 * @param initialMode 初始模式（从主页传递过来）
 * @param onBackClick 返回按钮点击事件
 * @param onSettingsClick 设置按钮点击事件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    initialMode: SessionType = SessionType.EVENT,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }

    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(historyRepository)
    )

    // 设置初始模式
    LaunchedEffect(initialMode) {
        viewModel.switchMode(initialMode)
    }

    // 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 拦截返回键
    BackHandler(onBack = onBackClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                actions = {
                    // 分享按钮
                    IconButton(onClick = { /* TODO: 实现分享功能 */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享"
                        )
                    }
                    // 关闭按钮
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                    // 设置按钮
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 底部导航栏（事件/秒表切换）
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.currentMode == SessionType.EVENT,
                    onClick = { viewModel.switchMode(SessionType.EVENT) },
                    icon = {
                        Text("📋")
                    },
                    label = { Text("事件") }
                )
                NavigationBarItem(
                    selected = uiState.currentMode == SessionType.STOPWATCH,
                    onClick = { viewModel.switchMode(SessionType.STOPWATCH) },
                    icon = {
                        Text("⏱️")
                    },
                    label = { Text("秒表") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // 日期选择区（160.dp）
            DateSelectionSection(
                selectedDate = uiState.selectedDate,
                currentMode = uiState.currentMode,
                sessionCount = uiState.sessions.size,
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() },
                onDateClick = { /* TODO: 打开日历选择器 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            // 记录列表区（占据剩余空间）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.sessions.isEmpty()) {
                    // 空状态显示
                    EmptyHistoryState(
                        currentMode = uiState.currentMode,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // TODO: 显示记录列表（事件模式 或 秒表模式）
                    Text(
                        text = "记录列表区域（待实现）",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }

            // 控制按钮区（96.dp）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // TODO: 根据模式显示不同的控制按钮
                Text(
                    text = "控制按钮区域（待实现）",
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * 日期选择区组件
 */
@Composable
fun DateSelectionSection(
    selectedDate: java.time.LocalDate,
    currentMode: SessionType,
    sessionCount: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 日期显示（大号字体 + 左右箭头）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPreviousDay) {
                Text("<", style = MaterialTheme.typography.headlineLarge)
            }

            Text(
                text = selectedDate.toString(),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            IconButton(onClick = onNextDay) {
                Text(">", style = MaterialTheme.typography.headlineLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            SessionType.EVENT -> "$dayOfWeek · 共${sessionCount}条"
            SessionType.STOPWATCH -> "$dayOfWeek · ${sessionCount}个会话"
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 空状态显示组件
 */
@Composable
fun EmptyHistoryState(
    currentMode: SessionType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (currentMode) {
                SessionType.EVENT -> "该日期暂无事件记录"
                SessionType.STOPWATCH -> "该日期暂无秒表记录"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
