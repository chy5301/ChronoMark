package io.github.chy5301.chronomark.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.util.TimeFormatter
import io.github.chy5301.chronomark.viewmodel.HistoryViewModel
import io.github.chy5301.chronomark.viewmodel.HistoryViewModelFactory
import java.util.Locale

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

    // 对话框状态
    var showSessionListDialog by remember { mutableStateOf(false) }
    var showDeleteEventConfirm by remember { mutableStateOf(false) }
    var showDeleteSessionConfirm by remember { mutableStateOf(false) }

    // 拦截返回键
    BackHandler(onBack = onBackClick)

    // 会话选择列表对话框
    if (showSessionListDialog && uiState.currentMode == SessionType.STOPWATCH) {
        SessionListDialog(
            sessions = uiState.sessions,
            currentIndex = uiState.currentSessionIndex,
            onDismiss = { showSessionListDialog = false },
            onSessionSelected = { index ->
                viewModel.selectSession(index)
                showSessionListDialog = false
            }
        )
    }

    // 删除当天记录确认对话框（事件模式）
    if (showDeleteEventConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteEventConfirm = false },
            title = { Text("确认删除") },
            text = {
                Text("确定要删除当前日期的所有事件记录吗？此操作无法撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllSessionsForCurrentDate()
                        showDeleteEventConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteEventConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 删除当前会话确认对话框（秒表模式）
    if (showDeleteSessionConfirm) {
        val currentSession = uiState.sessions.getOrNull(uiState.currentSessionIndex)
        val sessionTitle = currentSession?.let {
            it.title.ifEmpty { "会话 ${uiState.currentSessionIndex + 1}" }
        } ?: ""

        AlertDialog(
            onDismissRequest = { showDeleteSessionConfirm = false },
            title = { Text("确认删除") },
            text = {
                Text("确定要删除会话「$sessionTitle」的所有记录吗？此操作无法撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCurrentSession()
                        showDeleteSessionConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSessionConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

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
                    // 返回主页按钮
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "返回主页"
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
                    icon = { Icon(Icons.Filled.Event, contentDescription = "事件") },
                    label = { Text("事件") }
                )
                NavigationBarItem(
                    selected = uiState.currentMode == SessionType.STOPWATCH,
                    onClick = { viewModel.switchMode(SessionType.STOPWATCH) },
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "秒表") },
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
                    // 根据模式显示记录列表
                    when (uiState.currentMode) {
                        SessionType.EVENT -> {
                            // 事件模式：显示当天的所有记录
                            EventHistoryRecordsList(
                                records = uiState.selectedSessionRecords,
                                onRecordClick = { record ->
                                    // TODO: 打开编辑记录对话框
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SessionType.STOPWATCH -> {
                            // 秒表模式：会话选择器 + 记录列表
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 会话选择器（80.dp）
                                SessionSelector(
                                    currentSession = uiState.sessions.getOrNull(uiState.currentSessionIndex),
                                    currentIndex = uiState.currentSessionIndex,
                                    totalSessions = uiState.sessions.size,
                                    onPreviousClick = { viewModel.goToPreviousSession() },
                                    onNextClick = { viewModel.goToNextSession() },
                                    onTitleClick = { showSessionListDialog = true }
                                )

                                // 记录列表
                                StopwatchHistoryRecordsList(
                                    records = uiState.selectedSessionRecords,
                                    onRecordClick = { record ->
                                        // TODO: 打开编辑记录对话框
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 控制按钮区（96.dp）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (uiState.currentMode) {
                    SessionType.EVENT -> {
                        // 事件模式：删除当天按钮
                        EventHistoryControlButtons(
                            onDeleteAllClick = {
                                showDeleteEventConfirm = true
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    SessionType.STOPWATCH -> {
                        // 秒表模式：编辑标题 + 删除会话按钮
                        StopwatchHistoryControlButtons(
                            onEditTitleClick = {
                                // TODO: 打开编辑标题对话框
                            },
                            onDeleteClick = {
                                showDeleteSessionConfirm = true
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
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

/**
 * 事件模式历史记录列表
 */
@Composable
fun EventHistoryRecordsList(
    records: List<TimeRecordEntity>,
    onRecordClick: (TimeRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records) { record ->
            HistoryRecordCard(
                record = record,
                onClick = { onRecordClick(record) }
            )
        }
    }
}

/**
 * 历史记录卡片组件（适用于事件和秒表模式）
 */
@Composable
fun HistoryRecordCard(
    record: TimeRecordEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showElapsedTime: Boolean = false  // 是否显示累计时间（秒表模式）
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 序号 + 标记时刻（或累计时间）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%02d", record.index),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showElapsedTime) {
                    // 秒表模式：显示累计时间
                    Text(
                        text = TimeFormatter.formatElapsed(record.elapsedTimeNanos),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    // 事件模式：显示标记时刻
                    Text(
                        text = TimeFormatter.formatWallClock(record.wallClockTime),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 秒表模式的额外信息（时间差 + 标记时刻）
            if (showElapsedTime) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = TimeFormatter.formatSplit(record.splitTimeNanos),
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = TimeFormatter.formatWallClock(record.wallClockTime),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 备注（如果有）
            if (record.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${record.note}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 事件模式历史记录控制按钮
 */
@Composable
fun EventHistoryControlButtons(
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onDeleteAllClick,
        modifier = modifier.size(80.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "删除当天",
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * 秒表模式历史记录列表
 */
@Composable
fun StopwatchHistoryRecordsList(
    records: List<TimeRecordEntity>,
    onRecordClick: (TimeRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records) { record ->
            HistoryRecordCard(
                record = record,
                onClick = { onRecordClick(record) },
                showElapsedTime = true  // 秒表模式显示累计时间
            )
        }
    }
}

/**
 * 秒表模式历史记录控制按钮
 */
@Composable
fun StopwatchHistoryControlButtons(
    onEditTitleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 编辑标题按钮
        FilledIconButton(
            onClick = onEditTitleClick,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑标题",
                modifier = Modifier.size(32.dp)
            )
        }

        // 删除会话按钮
        FilledIconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除会话",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 秒表模式会话选择器组件
 */
@Composable
fun SessionSelector(
    currentSession: io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity?,
    currentIndex: Int,
    totalSessions: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (currentSession != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左箭头按钮
                IconButton(
                    onClick = onPreviousClick,
                    enabled = currentIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "上一个会话"
                    )
                }

                // 中间：会话标题 + 副标题
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onTitleClick),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 会话标题
                    val title = if (currentSession.title.isNotEmpty()) {
                        "${currentSession.title} (${currentIndex + 1}/$totalSessions)"
                    } else {
                        "会话 ${currentIndex + 1}/$totalSessions"
                    }
                    Text(
                        text = title,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    // 副标题：开始时间 + 总用时
                    val startTime = TimeFormatter.formatWallClock(currentSession.startTime)
                        .substring(0, 5)  // 只取 HH:mm 部分
                    val totalTime = TimeFormatter.formatElapsed(currentSession.totalElapsedNanos)
                        .let {
                            // 去掉毫秒部分，只保留 MM:SS
                            val parts = it.split(".")
                            parts[0]
                        }
                    Text(
                        text = "$startTime · 用时 $totalTime",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 右箭头按钮
                IconButton(
                    onClick = onNextClick,
                    enabled = currentIndex < totalSessions - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "下一个会话"
                    )
                }
            }
        }
    }
}

/**
 * 会话选择列表对话框
 */
@Composable
fun SessionListDialog(
    sessions: List<io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSessionSelected: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(currentIndex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择会话") },
        text = {
            LazyColumn {
                itemsIndexed(sessions) { index, session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 单选按钮
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 会话信息
                        Column(modifier = Modifier.weight(1f)) {
                            // 会话标题
                            Text(
                                text = session.title.ifEmpty {
                                    "会话 ${index + 1}"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // 开始时间 + 总用时
                            val startTime = TimeFormatter.formatWallClock(session.startTime)
                                .substring(0, 5)  // HH:mm
                            val totalTime = TimeFormatter.formatElapsed(session.totalElapsedNanos)
                                .split(".")[0]  // 去掉毫秒部分
                            Text(
                                text = "$startTime · 用时 $totalTime",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (index < sessions.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSessionSelected(selectedIndex) }
            ) {
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
