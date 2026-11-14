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
     * 提取预发布标识（如 "v1.2.0-rc.1+build.5" -> ["rc", "1"]），正式版返回空列表
     * 构建元数据（+ 之后的部分）不参与比较
     */
    private fun parsePrerelease(version: String): List<String> {
        val withoutBuild = version.substringBefore('+')
        val dash = withoutBuild.indexOf('-')
        if (dash < 0) return emptyList()
        return withoutBuild.substring(dash + 1).split('.').filter { it.isNotEmpty() }
    }

    /** 当前/远程版本是否为预发布版本 */
    fun isPrerelease(version: String): Boolean = parsePrerelease(version).isNotEmpty()

    /**
     * 比较两个版本号，遵循语义化版本（SemVer 2.0）的优先级规则：
     * 先比较主版本号各段；相同时，正式版高于其预发布版本；
     * 均为预发布版本时逐段比较标识（数字按数值、数字低于字母、前缀相同则段数多者更高）
     *
     * @param current 当前版本号
     * @param remote 远程版本号
     * @return 正数表示远程版本更新，0 表示相同，负数表示当前版本更新
     */
    fun compareVersions(current: String, remote: String): Int {
        val currentParts = parseVersion(current)
        val remoteParts = parseVersion(remote)

        // 比较主版本号各段
        val maxLength = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val remotePart = remoteParts.getOrElse(i) { 0 }

            if (remotePart > currentPart) return 1
            if (remotePart < currentPart) return -1
        }

        return comparePrerelease(parsePrerelease(current), parsePrerelease(remote))
    }

    private fun comparePrerelease(current: List<String>, remote: List<String>): Int {
        // 正式版（无预发布标识）高于任何预发布版本
        if (current.isEmpty() || remote.isEmpty()) {
            return when {
                current.isEmpty() && remote.isEmpty() -> 0
                current.isEmpty() -> -1
                else -> 1
            }
        }

        for (i in 0 until minOf(current.size, remote.size)) {
            val c = current[i]
            val r = remote[i]
            val cNum = c.toLongOrNull()
            val rNum = r.toLongOrNull()
            val result = when {
                cNum != null && rNum != null -> rNum.compareTo(cNum)
                cNum != null -> 1   // 数字标识低于字母标识
                rNum != null -> -1
                else -> r.compareTo(c)
            }
            if (result != 0) return result.coerceIn(-1, 1)
        }
        return (remote.size - current.size).coerceIn(-1, 1)
    }

    /**
     * 判断远程版本是否是当前用户应收到的更新
     *
     * 正式版用户不推送预发布版本；预发布版用户可收到更新的预发布版或正式版
     *
     * @param current 当前版本号
     * @param remote 远程版本号
     * @return true 表示有新版本可用
     */
    fun isNewerVersion(current: String, remote: String): Boolean {
        if (isPrerelease(remote) && !isPrerelease(current)) return false
        return compareVersions(current, remote) > 0
    }
}
