package io.github.chy5301.chronomark.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.database.AppDatabase
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
import io.github.chy5301.chronomark.viewmodel.EventHistoryViewModel
import io.github.chy5301.chronomark.viewmodel.EventHistoryViewModelFactory
import java.util.Locale

/**
 * 事件模式历史记录页面
 *
 * @param onBackClick 返回按钮点击事件
 * @param onSettingsClick 设置按钮点击事件
 * @param onModeChange 模式切换回调（切换到秒表模式）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventHistoryScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onModeChange: (AppMode) -> Unit
) {
    val context = LocalContext.current
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }

    val viewModel: EventHistoryViewModel = viewModel(
        factory = EventHistoryViewModelFactory(historyRepository)
    )

    // 收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 对话框状态
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<TimeRecordEntity?>(null) }
    var selectedRecordIndex by remember { mutableStateOf(0) }
    var showDeleteRecordConfirm by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }

    // 拦截返回键
    BackHandler(onBack = onBackClick)

    // 删除当天记录确认对话框
    ConfirmDialog(
        show = showDeleteConfirm,
        title = "确认删除",
        message = "确定要删除当前日期的所有事件记录吗？此操作无法撤销。",
        confirmText = "删除",
        isDangerous = true,
        onConfirm = {
            viewModel.deleteAllRecordsForCurrentDate()
            showDeleteConfirm = false
        },
        onDismiss = { showDeleteConfirm = false }
    )

    // 编辑记录对话框
    selectedRecord?.let { record ->
        EditRecordDialog(
            record = record,
            index = selectedRecordIndex,
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
                title = { Text("历史记录 - 事件") },
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
                currentMode = AppMode.EVENT,
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
                currentMode = AppMode.EVENT,
                sessionCount = uiState.selectedSessionRecords.size,
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
                } else if (uiState.selectedSessionRecords.isEmpty()) {
                    // 空状态显示
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "该日期暂无事件记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 记录列表（事件模式：按时间升序，序号从 1 开始）
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.selectedSessionRecords) { listIndex, record ->
                            val displayIndex = listIndex + 1  // 序号从 1 开始
                            UnifiedRecordCard(
                                record = record,
                                index = displayIndex,
                                mode = RecordCardMode.EVENT,
                                onClick = {
                                    selectedRecord = record
                                    selectedRecordIndex = displayIndex
                                }
                            )
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
                // 删除当天按钮
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
                            showDeleteConfirm = true
                        }
                    },
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除当天",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
