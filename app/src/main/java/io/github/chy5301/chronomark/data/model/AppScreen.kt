package io.github.chy5301.chronomark.data.model

/**
 * 应用页面导航枚举
 *
 * 定义应用的顶层页面导航状态。
 * 与 [AppMode] 的区别：
 * - AppScreen: 页面级导航（工作区、历史、设置）
 * - AppMode: Tab 级导航（事件、秒表），仅在工作区内使用
 */
enum class AppScreen {
    /**
     * 主工作区页面
     * 包含事件模式和秒表模式的 Tab 切换
     */
    WORKSPACE,

    /**
     * 历史记录页面
     * 查看已归档的事件记录和秒表会话
     */
    HISTORY,

    /**
     * 设置页面
     * 应用配置和偏好设置
     */
    SETTINGS
}
