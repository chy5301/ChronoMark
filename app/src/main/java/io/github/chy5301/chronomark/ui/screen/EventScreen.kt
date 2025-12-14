package io.github.chy5301.chronomark.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.util.TimeFormatter
import io.github.chy5301.chronomark.viewmodel.EventViewModel
import io.github.chy5301.chronomark.viewmodel.EventViewModelFactory

/**
 * 事件模式主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(dataStoreManager)
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedRecord by remember { mutableStateOf<TimeRecord?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // 编辑对话框
    selectedRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { selectedRecord = null },
            onSave = { note ->
                viewModel.updateRecordNote(record.id, note)
                selectedRecord = null
            },
            onDeleteRequest = { showDeleteConfirm = true }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirm && selectedRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除记录 #${"%02d".format(selectedRecord!!.index)} 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(selectedRecord!!.id)
                        showDeleteConfirm = false
                        selectedRecord = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 重置确认对话框
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("确认重置") },
            text = { Text("确定要清空所有事件记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("重置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("事件") },
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.records.isEmpty()) {
                                android.widget.Toast.makeText(context, "暂无记录", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                val shareText = viewModel.generateShareText()
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
                    IconButton(onClick = { /* TODO: 菜单功能 */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "菜单")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // 时间显示区（仅墙上时钟）
            EventTimeDisplaySection(
                wallClockTime = uiState.wallClockTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            // 记录列表区
            EventRecordsListSection(
                records = uiState.records,
                onRecordClick = { record -> selectedRecord = record },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 控制按钮区（始终显示记录和重置）
            EventControlButtonsSection(
                onRecordClick = { viewModel.recordEvent() },
                onResetClick = { showResetConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            )
        }
    }
}

/**
 * 事件模式 - 时间显示区组件（仅墙上时钟）
 */
@Composable
fun EventTimeDisplaySection(
    wallClockTime: String,
    modifier: Modifier = Modifier
) {
    // 拆分日期和时间部分（格式：yyyy-MM-dd HH:mm:ss）
    val timePart = if (wallClockTime.length >= 19) {
        wallClockTime.substring(11, 19) // HH:mm:ss
    } else {
        "00:00:00"
    }
    val datePart = if (wallClockTime.length >= 10) {
        wallClockTime.substring(0, 10) // yyyy-MM-dd
    } else {
        "0000-00-00"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 时间（56sp，加粗）
        Text(
            text = timePart,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 日期（24sp，次要颜色）
        Text(
            text = datePart,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 事件模式 - 记录列表区组件
 */
@Composable
fun EventRecordsListSection(
    records: List<TimeRecord>,
    onRecordClick: (TimeRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无事件记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val listState = rememberLazyListState()

        // 当记录列表变化时，自动滚动到末尾
        LaunchedEffect(records.size) {
            if (records.isNotEmpty()) {
                val lastIndex = records.size - 1
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

                // 如果已经在底部（最后一个或倒数第二个可见），直接跳转避免抖动
                if (lastVisibleIndex >= lastIndex - 1) {
                    listState.scrollToItem(lastIndex)
                } else {
                    // 否则使用动画滚动
                    listState.animateScrollToItem(lastIndex)
                }
            }
        }

        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                EventRecordCard(
                    record = record,
                    onClick = { onRecordClick(record) }
                )
            }
        }
    }
}

/**
 * 事件模式 - 记录卡片组件
 */
@Composable
fun EventRecordCard(
    record: TimeRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
            // 第一行：序号 + 标记时刻（主信息）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%02d".format(record.index),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = TimeFormatter.formatWallClock(record.wallClockTime),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 第二行：时间差（次要，右对齐）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = TimeFormatter.formatSplit(record.splitTimeNanos),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // 第三行：备注（如果有）
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
 * 事件模式 - 控制按钮区组件（记录 + 重置）
 */
@Composable
fun EventControlButtonsSection(
    onRecordClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                onClick = onRecordClick,
                icon = Icons.Filled.Add,
                contentDescription = "记录"
            )
            ControlButton(
                onClick = onResetClick,
                icon = Icons.Filled.Refresh,
                contentDescription = "重置"
            )
        }
    }
}
