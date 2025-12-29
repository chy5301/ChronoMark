package io.github.chy5301.chronomark.ui.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 统一样式的控制按钮
 *
 * 用于秒表和事件模式的所有操作按钮，提供一致的视觉风格。
 *
 * @param onClick 点击回调
 * @param icon 按钮图标
 * @param contentDescription 无障碍描述
 * @param modifier 修饰符
 * @param containerColor 按钮背景色（默认使用主题 surface 色）
 * @param contentColor 图标颜色（默认使用主题 primary 色）
 *
 * 使用示例：
 * ```kotlin
 * ControlButton(
 *     onClick = { viewModel.start() },
 *     icon = Icons.Filled.PlayArrow,
 *     contentDescription = "开始"
 * )
 *
 * // 危险操作（红色）
 * ControlButton(
 *     onClick = { viewModel.delete() },
 *     icon = Icons.Filled.Delete,
 *     contentDescription = "删除",
 *     contentColor = MaterialTheme.colorScheme.error
 * )
 * ```
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
