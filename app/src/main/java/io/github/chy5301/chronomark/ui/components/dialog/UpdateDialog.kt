package io.github.chy5301.chronomark.ui.components.dialog

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.chy5301.chronomark.data.model.UpdateInfo

/**
 * 更新提示对话框
 *
 * @param currentVersion 当前版本号
 * @param updateInfo 更新信息
 * @param onDismiss 关闭对话框（稍后提醒）
 * @param onIgnoreVersion 忽略此版本
 */
@Composable
fun UpdateDialog(
    currentVersion: String,
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onIgnoreVersion: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "发现新版本",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 版本信息
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentVersion,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = updateInfo.version,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 来源信息
                Text(
                    text = "来源: ${updateInfo.getSourceDisplayName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // 分割线
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                // 更新内容标题
                Text(
                    text = "更新内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 更新内容（可滚动区域）
                val simplifiedBody = updateInfo.getSimplifiedBody()
                if (simplifiedBody.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .heightIn(max = 200.dp)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = simplifiedBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                } else {
                    Text(
                        text = "暂无更新说明",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // 打开浏览器跳转到 Release 页面
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.htmlUrl))
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text("前往下载")
            }
        },
        dismissButton = {
            Row {
                // 忽略此版本
                TextButton(onClick = onIgnoreVersion) {
                    Text("忽略此版本")
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 稍后提醒
                TextButton(onClick = onDismiss) {
                    Text("稍后提醒")
                }
            }
        }
    )
}

/**
 * 检查更新结果对话框（手动检查时使用）
 *
 * @param isUpToDate 是否已是最新版本
 * @param errorMessage 错误信息（如果有）
 * @param onDismiss 关闭对话框
 */
@Composable
fun UpdateCheckResultDialog(
    isUpToDate: Boolean,
    errorMessage: String? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (errorMessage != null) "检查失败" else "检查更新"
            )
        },
        text = {
            Text(
                text = when {
                    errorMessage != null -> "无法检查更新: $errorMessage"
                    isUpToDate -> "当前已是最新版本"
                    else -> "检查中..."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
