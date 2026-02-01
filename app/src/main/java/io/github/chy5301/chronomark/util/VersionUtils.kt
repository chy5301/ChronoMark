package io.github.chy5301.chronomark.util

/**
 * 版本号工具类
 * 支持语义化版本比较（如 v1.0.2 vs v1.0.3）
 */
object VersionUtils {

    /**
     * 解析版本号为整数数组
     * 支持 v 前缀和预发布后缀
     *
     * @param version 版本号字符串（如 "v1.0.2"、"1.0.2"、"v1.0.3-beta"）
     * @return 版本号整数列表，如 [1, 0, 2]
     */
    fun parseVersion(version: String): List<Int> {
        // 移除 v/V 前缀
        val cleanVersion = version.trimStart('v', 'V')

        // 移除预发布后缀（-alpha, -beta, -rc 等）
        val mainVersion = cleanVersion.split('-').first()

        // 分割并解析每个数字部分
        return mainVersion.split('.').mapNotNull { part ->
            part.toIntOrNull()
        }
    }

    /**
     * 比较两个版本号
     *
     * @param current 当前版本号
     * @param remote 远程版本号
     * @return 正数表示远程版本更新，0 表示相同，负数表示当前版本更新
     */
    fun compareVersions(current: String, remote: String): Int {
        val currentParts = parseVersion(current)
        val remoteParts = parseVersion(remote)

        // 比较每个部分
        val maxLength = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val remotePart = remoteParts.getOrElse(i) { 0 }

            if (remotePart > currentPart) return 1
            if (remotePart < currentPart) return -1
        }

        return 0
    }

    /**
     * 判断远程版本是否比当前版本更新
     *
     * @param current 当前版本号
     * @param remote 远程版本号
     * @return true 表示有新版本可用
     */
    fun isNewerVersion(current: String, remote: String): Boolean {
        return compareVersions(current, remote) > 0
    }
}
