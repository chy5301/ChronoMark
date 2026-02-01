package io.github.chy5301.chronomark.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 更新通道枚举
 */
enum class UpdateChannel {
    GITEE_FIRST,    // Gitee 优先（默认）
    GITHUB_FIRST    // GitHub 优先
}

/**
 * 更新来源枚举
 */
enum class UpdateSource {
    GITEE,
    GITHUB
}

/**
 * Gitee Release API 响应模型
 */
@Serializable
data class GiteeReleaseInfo(
    @SerialName("tag_name")
    val tagName: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("body")
    val body: String? = null,

    @SerialName("html_url")
    val htmlUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * GitHub Release API 响应模型
 */
@Serializable
data class GitHubReleaseInfo(
    @SerialName("tag_name")
    val tagName: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("body")
    val body: String? = null,

    @SerialName("html_url")
    val htmlUrl: String? = null,

    @SerialName("published_at")
    val publishedAt: String? = null
)

/**
 * 统一的更新信息模型
 */
data class UpdateInfo(
    val version: String,            // 版本号（tag_name）
    val name: String,               // Release 名称
    val body: String,               // 更新说明（Markdown 格式）
    val htmlUrl: String,            // Release 页面 URL
    val source: UpdateSource        // 来源（Gitee 或 GitHub）
) {
    companion object {
        /**
         * 从 Gitee Release 创建 UpdateInfo
         */
        fun fromGitee(release: GiteeReleaseInfo): UpdateInfo {
            return UpdateInfo(
                version = release.tagName,
                name = release.name ?: release.tagName,
                body = release.body ?: "",
                htmlUrl = release.htmlUrl ?: "https://gitee.com/chy5301/ChronoMark/releases",
                source = UpdateSource.GITEE
            )
        }

        /**
         * 从 GitHub Release 创建 UpdateInfo
         */
        fun fromGitHub(release: GitHubReleaseInfo): UpdateInfo {
            return UpdateInfo(
                version = release.tagName,
                name = release.name ?: release.tagName,
                body = release.body ?: "",
                htmlUrl = release.htmlUrl ?: "https://github.com/chy5301/ChronoMark/releases",
                source = UpdateSource.GITHUB
            )
        }
    }

    /**
     * 获取来源显示名称
     */
    fun getSourceDisplayName(): String {
        return when (source) {
            UpdateSource.GITEE -> "Gitee"
            UpdateSource.GITHUB -> "GitHub"
        }
    }

    /**
     * 简化 Markdown 格式的更新说明
     * 移除 Markdown 标记，保留纯文本
     */
    fun getSimplifiedBody(): String {
        return body
            // 移除标题标记
            .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
            // 移除加粗和斜体
            .replace(Regex("\\*{1,2}([^*]+)\\*{1,2}"), "$1")
            .replace(Regex("_{1,2}([^_]+)_{1,2}"), "$1")
            // 移除链接，保留文本
            .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")
            // 移除代码块标记
            .replace(Regex("```[^`]*```", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("`([^`]+)`"), "$1")
            // 移除水平线
            .replace(Regex("^-{3,}$", RegexOption.MULTILINE), "")
            .replace(Regex("^\\*{3,}$", RegexOption.MULTILINE), "")
            // 清理多余空行
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}
