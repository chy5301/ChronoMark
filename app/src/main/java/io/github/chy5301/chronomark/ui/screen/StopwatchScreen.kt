package io.github.chy5301.chronomark.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.viewmodel.StopwatchViewModel

/**
 * 秒表主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("秒表") },
                actions = {
                    IconButton(onClick = { /* TODO: 导出功能 */ }) {
                        Icon(Icons.Default.Share, contentDescription = "导出")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 控制按钮区
            ControlButtonsSection(
                status = uiState.status,
                onStartClick = { viewModel.start() },
                onPauseClick = { viewModel.pause() },
                onResumeClick = { viewModel.resume() },
                onStopClick = { viewModel.stop() },
                onResetClick = { viewModel.reset() },
                onMarkClick = { viewModel.addMark() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
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
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 墙上时钟（带日期，不含毫秒）
        Text(
            text = wallClockTime,
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
    records: List<io.github.chy5301.chronomark.data.model.TimeRecord>,
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
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                RecordCard(record = record)
            }
        }
    }
}

/**
 * 记录卡片组件
 */
@Composable
fun RecordCard(
    record: io.github.chy5301.chronomark.data.model.TimeRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = io.github.chy5301.chronomark.util.TimeFormatter.formatElapsed(record.elapsedTimeNanos),
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
                    text = io.github.chy5301.chronomark.util.TimeFormatter.formatSplit(record.splitTimeNanos),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = io.github.chy5301.chronomark.util.TimeFormatter.formatWallClock(record.wallClockTime),
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
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            StopwatchStatus.Idle -> {
                // 初始状态：只有开始按钮
                FilledTonalButton(
                    onClick = onStartClick,
                    modifier = Modifier.size(72.dp)
                ) {
                    Text("▶")
                }
            }

            StopwatchStatus.Running -> {
                // 运行中：标记 + 暂停
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = onMarkClick,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Text("🚩", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("标记", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = onPauseClick,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Text("⏸", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("暂停", fontSize = 12.sp)
                    }
                }
            }

            StopwatchStatus.Paused -> {
                // 暂停：继续 + 停止
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = onResumeClick,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Text("▶", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("继续", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = onStopClick,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Text("⏹", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("停止", fontSize = 12.sp)
                    }
                }
            }

            StopwatchStatus.Stopped -> {
                // 停止：重置
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalButton(
                        onClick = onResetClick,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Text("↻", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("重置", fontSize = 12.sp)
                }
            }
        }
    }
}
