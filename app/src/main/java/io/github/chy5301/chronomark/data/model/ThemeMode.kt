package io.github.chy5301.chronomark.data.model

/**
 * 主题模式
 */
enum class ThemeMode {
    /** 浅色模式 */
    LIGHT,

    /** 深色模式 */
    DARK,

    /** 跟随系统 */
    SYSTEM;

    /**
     * 获取主题模式的显示名称
     */
    fun getDisplayName(): String = when (this) {
        LIGHT -> "浅色"
        DARK -> "深色"
        SYSTEM -> "跟随系统"
    }
}
