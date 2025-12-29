package io.github.chy5301.chronomark.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.util.TimeFormatter
import java.util.Locale

/**
 * 编辑记录对话框组件
 *
 * 通用的记录编辑对话框，支持 TimeRecord（工作区）和 TimeRecordEntity（历史记录）两种类型。
 *
 * **功能**：
 * - 显示只读信息：序号、累计时间、标记时刻
 * - 编辑备注文字（最多 3 行）
 * - 保存/取消/删除操作
 *
 * **使用场景**：
 * - StopwatchScreen: 编辑工作区记录
 * - EventScreen: 编辑工作区事件
 * - HistoryScreen: 编辑历史记录
 *
 * @param record 要编辑的记录（TimeRecord 或 TimeRecordEntity）
 * @param onDismiss 关闭对话框回调
 * @param onSave 保存备注回调（参数为新备注文本）
 * @param onDeleteRequest 请求删除记录回调
 * @param modifier 修饰符
 *
 * 使用示例：
 * ```kotlin
 * // StopwatchScreen - 使用 TimeRecord
 * EditRecordDialog(
 *     record = selectedRecord,
 *     onDismiss = { selectedRecord = null },
 *     onSave = { note ->
 *         viewModel.updateRecordNote(selectedRecord.id, note)
 *         selectedRecord = null
 *     },
 *     onDeleteRequest = { showDeleteConfirm = true }
 * )
 *
 * // HistoryScreen - 使用 TimeRecordEntity
 * EditRecordDialog(
 *     record = selectedRecord,
 *     onDismiss = { selectedRecord = null },
 *     onSave = { note ->
 *         viewModel.updateRecordNote(selectedRecord.id, note)
 *         selectedRecord = null
 *     },
 *     onDeleteRequest = { showDeleteConfirm = true }
 * )
 * ```
 */
@Composable
fun EditRecordDialog(
    record: TimeRecord,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    EditRecordDialogImpl(
        id = record.id,
        index = record.index,
        elapsedTimeNanos = record.elapsedTimeNanos,
        wallClockTime = record.wallClockTime,
        initialNote = record.note,
        onDismiss = onDismiss,
        onSave = onSave,
        onDeleteRequest = onDeleteRequest,
        modifier = modifier
    )
}

@Composable
fun EditRecordDialog(
    record: TimeRecordEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    EditRecordDialogImpl(
        id = record.id,
        index = record.index,
        elapsedTimeNanos = record.elapsedTimeNanos,
        wallClockTime = record.wallClockTime,
        initialNote = record.note,
        onDismiss = onDismiss,
        onSave = onSave,
        onDeleteRequest = onDeleteRequest,
        modifier = modifier
    )
}

/**
 * 编辑记录对话框 - 内部实现
 *
 * @param id 记录 ID（用于 remember 键）
 * @param index 记录序号
 * @param elapsedTimeNanos 累计时间（纳秒）
 * @param wallClockTime 标记时刻（毫秒时间戳）
 * @param initialNote 初始备注文本
 */
@Composable
private fun EditRecordDialogImpl(
    id: String,
    index: Int,
    elapsedTimeNanos: Long,
    wallClockTime: Long,
    initialNote: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var noteText by remember(id) { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("编辑记录 #${String.format(Locale.US, "%02d", index)}")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 只读信息
                Text(
                    text = "累计时间: ${TimeFormatter.formatElapsed(elapsedTimeNanos)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "标记时刻: ${TimeFormatter.formatWallClock(wallClockTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 备注输入框
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(noteText) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                // 删除按钮
                TextButton(
                    onClick = onDeleteRequest,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 取消按钮
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
        modifier = modifier
    )
}
