package io.github.chy5301.chronomark.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chy5301.chronomark.data.model.RecordCardMode
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.ui.components.button.ControlButton
import io.github.chy5301.chronomark.ui.components.dialog.ConfirmDialog
import io.github.chy5301.chronomark.ui.components.dialog.EditRecordDialog
import io.github.chy5301.chronomark.ui.components.record.UnifiedRecordCard
import io.github.chy5301.chronomark.ui.theme.TabularNumbersStyle
import io.github.chy5301.chronomark.viewmodel.EventViewModel

/**
 * 事件模式主屏幕
 */
@Composable
fun EventScreen(
    viewModel: EventViewModel,
    paddingValues: PaddingValues,
    vibrationEnabled: Boolean = true
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedRecord by remember { mutableStateOf<TimeRecord?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // 创建震动反馈辅助类
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

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

    // 重置确认对话框
    ConfirmDialog(
        show = showResetConfirm,
        title = "确认重置",
        message = "确定要清空所有事件记录吗？此操作无法撤销。",
        confirmText = "重置",
        isDangerous = true,
        onConfirm = {
            viewModel.reset()
            showResetConfirm = false
        },
        onDismiss = { showResetConfirm = false }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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
            onRecordClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                viewModel.recordEvent()
            },
            onResetClick = {
                if (vibrationEnabled) {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                }
                // 检查是否有记录可重置
                if (uiState.records.isEmpty()) {
                    android.widget.Toast.makeText(
                        context,
                        "暂无记录",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showResetConfirm = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        )
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
        wallClockTime.take(10) // yyyy-MM-dd
    } else {
        "0000-00-00"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 时间（60sp，加粗）
        Text(
            text = timePart,
            style = TabularNumbersStyle,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 日期（24sp，次要颜色）
        Text(
            text = datePart,
            style = TabularNumbersStyle,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 事件模式 - 记录列表区组件
 */
@Suppress("UNUSED_VALUE")
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
        // 创建列表状态，初始位置设置为最后一项，避免"闪一下第一条"的问题
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = records.size - 1
        )

        // 记录上一次的列表大小，用于判断是否新增了记录
        var previousSize by remember { mutableIntStateOf(records.size) }

        // 当记录列表变化时，自动滚动到末尾
        LaunchedEffect(records.size) {
            if (records.isNotEmpty()) {
                val lastIndex = records.size - 1

                // 只有当列表大小增加时（新增记录）才执行滚动
                if (records.size > previousSize) {
                    val lastVisibleIndex =
                        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

                    // 如果已经在底部（最后一个或倒数第二个可见），直接跳转避免抖动
                    if (lastVisibleIndex >= lastIndex - 1) {
                        listState.scrollToItem(lastIndex)
                    } else {
                        // 否则使用动画滚动
                        listState.animateScrollToItem(lastIndex)
                    }
                }

                // 更新记录的大小
                previousSize = records.size
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
                UnifiedRecordCard(
                    record = record,
                    mode = RecordCardMode.EVENT,
                    onClick = { onRecordClick(record) }
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
