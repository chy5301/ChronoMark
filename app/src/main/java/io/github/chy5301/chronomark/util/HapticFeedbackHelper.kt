package io.github.chy5301.chronomark.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 震动反馈辅助类
 */
class HapticFeedbackHelper(
    private val hapticFeedback: HapticFeedback,
    private val enabled: Boolean
) {
    /**
     * 执行震动反馈（如果已启用）
     */
    fun performHapticFeedback() {
        if (enabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

/**
 * 创建震动反馈辅助类的 Composable 函数
 */
@Composable
fun rememberHapticFeedback(enabled: Boolean): HapticFeedbackHelper {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(enabled) {
        HapticFeedbackHelper(hapticFeedback, enabled)
    }
}
