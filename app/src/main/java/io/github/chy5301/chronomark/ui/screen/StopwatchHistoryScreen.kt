package io.github.chy5301.chronomark.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.data.model.RecordCardMode
import io.github.chy5301.chronomark.ui.components.dialog.ConfirmDialog
import io.github.chy5301.chronomark.ui.components.dialog.EditRecordDialog
import io.github.chy5301.chronomark.ui.components.history.CalendarPickerDialog
import io.github.chy5301.chronomark.ui.components.history.DateSelector
import io.github.chy5301.chronomark.ui.components.navigation.ModeNavigationBar
import io.github.chy5301.chronomark.ui.components.record.UnifiedRecordCard
import io.github.chy5301.chronomark.util.TimeFormatter
import io.github.chy5301.chronomark.viewmodel.StopwatchHistoryViewModel
import io.github.chy5301.chronomark.viewmodel.StopwatchHistoryViewModelFactory
import java.util.Locale

/**
 * 秒表模式历史记录页面
 *
 * @param onBackClick 返回按钮点击事件
 * @param onSettingsClick 设置按钮点击事件
 * @param onModeChange 模式切换回调（切换到事件模式）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchHistoryScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onModeChange: (AppMode) -> Unit
) {
    val context = LocalContext.current
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }

    val viewModel: StopwatchHistoryViewModel = viewModel(
        factory = StopwatchHistoryViewModelFactory(historyRepository)
    )

    // 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 对话框状态
    var showSessionListDialog by remember { mutableStateOf(false) }
    var showDeleteSessionConfirm by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<TimeRecordEntity?>(null) }
    var selectedRecordIndex by remember { mutableStateOf(0) }
    var showDeleteRecordConfirm by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    // 拦截返回键
    BackHandler(onBack = onBackClick)

    // 会话选择列表对话框
    if (showSessionListDialog) {
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

    // 删除当前会话确认对话框
    ConfirmDialog(
        show = showDeleteSessionConfirm,
        title = "确认删除",
        message = run {
            val currentSession = uiState.sessions.getOrNull(uiState.currentSessionIndex)
            val sessionTitle = currentSession?.let {
                it.title.ifEmpty { "会话 ${uiState.currentSessionIndex + 1}" }
            } ?: ""
            "确定要删除会话「$sessionTitle」的所有记录吗？此操作无法撤销。"
        },
        confirmText = "删除",
        isDangerous = true,
        onConfirm = {
            viewModel.deleteCurrentSession()
            showDeleteSessionConfirm = false
        },
        onDismiss = { showDeleteSessionConfirm = false }
    )

    // 编辑会话标题对话框
    if (showEditTitleDialog) {
        val currentSession = uiState.sessions.getOrNull(uiState.currentSessionIndex)
        var titleText by remember(currentSession?.id) {
            mutableStateOf(currentSession?.title ?: "")
        }

        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = { Text("编辑会话标题") },
            text = {
                Column {
                    Text(
                        text = "为会话 ${uiState.currentSessionIndex + 1} 设置标题",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("标题") },
                        placeholder = { Text("会话 ${uiState.currentSessionIndex + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateCurrentSessionTitle(titleText)
                        showEditTitleDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑记录对话框
    selectedRecord?.let { record ->
        EditRecordDialog(
            record = record,
            index = selectedRecordIndex,
            mode = RecordCardMode.STOPWATCH,
            onDismiss = { selectedRecord = null },
            onSave = { note ->
                viewModel.updateRecordNote(record.id, note)
                selectedRecord = null
            },
            onDeleteRequest = { showDeleteRecordConfirm = true }
        )
    }

    // 删除记录确认对话框
    ConfirmDialog(
        show = showDeleteRecordConfirm && selectedRecord != null,
        title = "确认删除",
        message = "确定要删除记录 #${String.format(Locale.US, "%02d", selectedRecordIndex)} 吗？",
        confirmText = "删除",
        isDangerous = true,
        onConfirm = {
            selectedRecord?.let { record ->
                viewModel.deleteRecord(record.id)
            }
            showDeleteRecordConfirm = false
            selectedRecord = null
        },
        onDismiss = { showDeleteRecordConfirm = false }
    )

    // 日历选择器对话框
    if (showCalendarDialog) {
        CalendarPickerDialog(
            selectedDate = uiState.selectedDate,
            datesWithRecords = uiState.datesWithRecords,
            onDismiss = { showCalendarDialog = false },
            onDateSelected = { date ->
                viewModel.selectDate(date)
                showCalendarDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录 - 秒表") },
                actions = {
                    // 分享按钮
                    IconButton(
                        onClick = {
                            val records = uiState.selectedSessionRecords

                            if (records.isEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "暂无记录",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val shareText = viewModel.generateShareText()
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
            ModeNavigationBar(
                currentMode = AppMode.STOPWATCH,
                onModeChange = onModeChange
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 日期选择区（120.dp）
            DateSelector(
                selectedDate = uiState.selectedDate,
                currentMode = AppMode.STOPWATCH,
                sessionCount = uiState.sessions.size,
                onPreviousDay = { viewModel.goToPreviousDay() },
                onNextDay = { viewModel.goToNextDay() },
                onDateClick = { showCalendarDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "该日期暂无秒表记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 会话选择器 + 记录列表
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 会话选择器（64.dp）
                        SessionSelector(
                            currentSession = uiState.sessions.getOrNull(uiState.currentSessionIndex),
                            currentIndex = uiState.currentSessionIndex,
                            totalSessions = uiState.sessions.size,
                            onPreviousClick = { viewModel.goToPreviousSession() },
                            onNextClick = { viewModel.goToNextSession() },
                            onTitleClick = { showSessionListDialog = true }
                        )

                        // 记录列表（秒表模式：按时间升序，序号从 1 开始）
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.selectedSessionRecords) { listIndex, record ->
                                val displayIndex = listIndex + 1  // 序号从 1 开始
                                UnifiedRecordCard(
                                    record = record,
                                    index = displayIndex,
                                    mode = RecordCardMode.STOPWATCH,
                                    onClick = {
                                        selectedRecord = record
                                        selectedRecordIndex = displayIndex
                                    }
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
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(80.dp)
                ) {
                    // 编辑标题按钮
                    ElevatedButton(
                        onClick = {
                            // 检查是否有会话可编辑
                            if (uiState.sessions.isEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "当天无会话记录",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showEditTitleDialog = true
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑标题",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 删除会话按钮
                    ElevatedButton(
                        onClick = {
                            // 检查是否有记录可删除
                            if (uiState.selectedSessionRecords.isEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "暂无记录",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showDeleteSessionConfirm = true
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除会话",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 内部私有组件（秒表历史记录专用）
// =============================================================================

/**
 * 会话选择器组件（StopwatchHistoryScreen 内部组件）
 *
 * 用于秒表模式历史记录页面的会话选择和导航。
 *
 * @param currentSession 当前会话实体
 * @param currentIndex 当前会话索引（从 0 开始）
 * @param totalSessions 总会话数
 * @param onPreviousClick 点击前一个会话的回调
 * @param onNextClick 点击后一个会话的回调
 * @param onTitleClick 点击标题的回调（打开会话列表）
 */
@Composable
private fun SessionSelector(
    currentSession: HistorySessionEntity?,
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
            .height(64.dp)
            .padding(horizontal = 32.dp),
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
                    Text("<", style = MaterialTheme.typography.headlineLarge)
                }

                // 中间：会话标题 + 副标题
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .widthIn(max = 240.dp)
                        .clickable(onClick = onTitleClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // 会话标题
                    val title = if (currentSession.title.isNotEmpty()) {
                        "${currentSession.title} (${currentIndex + 1}/$totalSessions)"
                    } else {
                        "会话 ${currentIndex + 1}/$totalSessions"
                    }
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 右箭头按钮
                IconButton(
                    onClick = onNextClick,
                    enabled = currentIndex < totalSessions - 1
                ) {
                    Text(">", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
    }
}

/**
 * 会话选择列表对话框（StopwatchHistoryScreen 内部组件）
 *
 * 显示当前日期的所有会话列表，供用户选择。
 *
 * @param sessions 会话列表
 * @param currentIndex 当前选中的会话索引
 * @param onDismiss 关闭对话框的回调
 * @param onSessionSelected 选择会话的回调，传递选中的会话索引
 */
@Composable
private fun SessionListDialog(
    sessions: List<HistorySessionEntity>,
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
