package io.github.chy5301.chronomark.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 通用确认对话框组件
 *
 * 用于所有需要用户确认的操作场景，如删除、重置、清空等。
 *
 * @param show 是否显示对话框
 * @param title 对话框标题
 * @param message 对话框内容消息
 * @param confirmText 确认按钮文本（默认"确定"）
 * @param dismissText 取消按钮文本（默认"取消"）
 * @param onConfirm 确认按钮点击回调
 * @param onDismiss 取消/关闭对话框回调
 * @param isDangerous 是否为危险操作（true 时确认按钮显示为红色）
 * @param modifier 修饰符
 *
 * 使用示例：
 * ```kotlin
 * // 删除确认（危险操作）
 * ConfirmDialog(
 *     show = showDeleteConfirm,
 *     title = "确认删除",
 *     message = "确定要删除记录 #01 吗？",
 *     confirmText = "删除",
 *     isDangerous = true,
 *     onConfirm = {
 *         viewModel.deleteRecord(recordId)
 *         showDeleteConfirm = false
 *     },
 *     onDismiss = { showDeleteConfirm = false }
 * )
 *
 * // 普通确认
 * ConfirmDialog(
 *     show = showSaveConfirm,
 *     title = "保存到历史",
 *     message = "是否保存当前记录到历史？",
 *     confirmText = "保存",
 *     onConfirm = {
 *         viewModel.saveToHistory()
 *         showSaveConfirm = false
 *     },
 *     onDismiss = { showSaveConfirm = false }
 * )
 * ```
 */
@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDangerous: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDangerous)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            },
            modifier = modifier
        )
    }
}
