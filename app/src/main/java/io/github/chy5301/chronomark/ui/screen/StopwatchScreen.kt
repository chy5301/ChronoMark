package io.github.chy5301.chronomark.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import io.github.chy5301.chronomark.ui.components.dialog.ConfirmDialog
import io.github.chy5301.chronomark.ui.components.dialog.EditRecordDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.ui.theme.TabularNumbersStyle
import io.github.chy5301.chronomark.util.TimeFormatter
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModel

/**
 * 秒表主屏幕
 */
@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel,
    paddingValues: PaddingValues,
    vibrationEnabled: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRecord by remember { mutableStateOf<TimeRecord?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showTitleInput by remember { mutableStateOf(false) }

    // 创建震动反馈辅助类
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    // 当状态变为 Stopped 时，显示保存确认对话框
    LaunchedEffect(uiState.status) {
        if (uiState.status == StopwatchStatus.Stopped && uiState.records.isNotEmpty()) {
            showSaveConfirm = true
        }
    }

    // 保存确认对话框
    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { /* 禁止点击外部关闭 */ },
            title = { Text("保存到历史记录？") },
            text = { Text("是否保存当前会话到历史记录？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveConfirm = false
                        showTitleInput = true
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveConfirm = false
                        viewModel.reset()
                    }
                ) {
                    Text("不保存")
                }
            }
        )
    }

    // 输入标题对话框
    if (showTitleInput) {
        var titleText by remember { mutableStateOf("") }

        // 异步获取默认标题
        LaunchedEffect(Unit) {
            titleText = viewModel.getDefaultTitle()
        }

        AlertDialog(
            onDismissRequest = { /* 禁止点击外部关闭 */ },
            title = { Text("输入会话标题") },
            text = {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("会话 1") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTitleInput = false
                        viewModel.saveToHistory(titleText)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTitleInput = false
                        viewModel.reset()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

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
    ConfirmDialog(
        show = showDeleteConfirm && selectedRecord != null,
        title = "确认删除",
        message = "确定要删除记录 #${if (selectedRecord != null) "%02d".format(selectedRecord!!.index) else ""} 吗？",
        confirmText = "删除",
        isDangerous = true,
        onConfirm = {
            selectedRecord?.let { record ->
                viewModel.deleteRecord(record.id)
            }
            showDeleteConfirm = false
            selectedRecord = null
        },
        onDismiss = { showDeleteConfirm = false }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // 时间显示区
        TimeDisplaySection(
            elapsedTime = uiState.currentTime,
            wallClockTime = uiState.wallClockTime,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )

        // 记录列表区
        RecordsListSection(
            records = uiState.records,
            onRecordClick = { record -> selectedRecord = record },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // 控制按钮区
        ControlButtonsSection(
            status = uiState.status,
            onStartClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.start()
            },
            onPauseClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.pause()
            },
            onResumeClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.resume()
            },
            onStopClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.stop()
            },
            onResetClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.reset()
            },
            onMarkClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.addMark()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        )
    }
}

/**
 * 时间显示区组件
 */
@Composable
fun TimeDisplaySection(
    elapsedTime: String,
    wallClockTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 主计时器
        Text(
            text = elapsedTime,
            style = TabularNumbersStyle,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 墙上时钟（带日期，不含毫秒）
        Text(
            text = wallClockTime,
            style = TabularNumbersStyle,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 记录列表区组件
 */
@Composable
fun RecordsListSection(
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
                text = "暂无记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val listState = rememberLazyListState()

        // 当记录列表变化时，自动滚动到顶部（倒序排列，最新记录在索引 0）
        LaunchedEffect(records.size) {
            if (records.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            modifier = modifier,
            state = listState,
            // 顶部不留白：避免主时钟区与列表间距显得过大
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                RecordCard(
                    record = record,
                    onClick = { onRecordClick(record) }
                )
            }
        }
    }
}

/**
 * 记录卡片组件
 */
@Composable
fun RecordCard(
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
            // 第一行：序号 + 累计时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%02d".format(record.index),
                    style = TabularNumbersStyle,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = TimeFormatter.formatElapsed(record.elapsedTimeNanos),
                    style = TabularNumbersStyle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 第二行：时间差 + 标记时刻
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeFormatter.formatSplit(record.splitTimeNanos),
                    style = TabularNumbersStyle,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = TimeFormatter.formatWallClock(record.wallClockTime),
                    style = TabularNumbersStyle,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
 * 控制按钮区组件
 */
@Composable
fun ControlButtonsSection(
    status: StopwatchStatus,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    onResetClick: () -> Unit,
    onMarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when (status) {
            StopwatchStatus.Idle -> {
                // 初始状态：只有开始按钮
                ControlButton(
                    onClick = onStartClick,
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = "开始"
                )
            }

            StopwatchStatus.Running -> {
                // 运行中：标记 + 暂停
                Row(
                    horizontalArrangement = Arrangement.spacedBy(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        onClick = onMarkClick,
                        icon = Icons.Filled.Flag,
                        contentDescription = "标记"
                    )
                    ControlButton(
                        onClick = onPauseClick,
                        icon = Icons.Filled.Pause,
                        contentDescription = "暂停"
                    )
                }
            }

            StopwatchStatus.Paused -> {
                // 暂停：继续 + 停止
                Row(
                    horizontalArrangement = Arrangement.spacedBy(80.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        onClick = onResumeClick,
                        icon = Icons.Filled.PlayArrow,
                        contentDescription = "继续"
                    )
                    ControlButton(
                        onClick = onStopClick,
                        icon = Icons.Filled.Stop,
                        contentDescription = "停止"
                    )
                }
            }

            StopwatchStatus.Stopped -> {
                // 停止：重置
                ControlButton(
                    onClick = onResetClick,
                    icon = Icons.Filled.Refresh,
                    contentDescription = "重置"
                )
            }
        }
    }
}

/**
 * 统一样式的控制按钮
 */
@Composable
fun ControlButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.size(80.dp),
        shape = CircleShape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp)
        )
    }
}

