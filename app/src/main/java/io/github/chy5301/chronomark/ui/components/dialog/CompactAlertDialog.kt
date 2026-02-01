package io.github.chy5301.chronomark.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 紧凑型对话框组件
 *
 * 使用 Dialog + Surface + Column 实现，相比 Material3 的 AlertDialog：
 * - 按钮区域与内容间距从 24dp 减少到 16dp
 * - 按钮区域与底部边距从 24dp 减少到 16dp（通过统一 padding 实现）
 *
 * 保留 Material3 AlertDialog 的视觉风格（形状、颜色、阴影）。
 *
 * @param onDismissRequest 对话框关闭回调
 * @param title 标题内容（可选）
 * @param text 主体内容（可选）
 * @param confirmButton 确认按钮
 * @param dismissButton 取消按钮（可选）
 * @param modifier 修饰符
 */
@Composable
fun CompactAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp  // 底部从 24dp 减少到 16dp
                )
            ) {
                // 标题
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            it()
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))  // 标题与内容间距：从 16dp 减到 12dp
                }

                // 内容
                text?.let {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            it()
                        }
                    }
                }

                // 按钮区域（间距从 16dp 减少到 12dp）
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
