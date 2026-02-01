package io.github.chy5301.chronomark.data.network

import android.util.Log
import io.github.chy5301.chronomark.data.model.UpdateChannel
import io.github.chy5301.chronomark.data.model.UpdateInfo
import io.github.chy5301.chronomark.util.VersionUtils

/**
 * 版本更新检查器
 * 支持 Gitee 优先 + GitHub 备用的双源策略
 */
class UpdateChecker(
    private val apiService: ReleaseApiService = ReleaseApiService()
) {

    companion object {
        private const val TAG = "UpdateChecker"

        // 24 小时检查间隔（毫秒）
        const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    /**
     * 检查更新结果
     */
    sealed class CheckResult {
        /**
         * 发现新版本
         */
        data class UpdateAvailable(val updateInfo: UpdateInfo) : CheckResult()

        /**
         * 已是最新版本
         */
        data object UpToDate : CheckResult()

        /**
         * 检查失败
         */
        data class Error(val message: String) : CheckResult()
    }

    /**
     * 检查是否有可用更新
     *
     * @param currentVersion 当前版本号
     * @param channel 更新通道（决定优先使用哪个源）
     * @param ignoredVersions 已忽略的版本列表
     * @return CheckResult 检查结果
     */
    suspend fun checkForUpdate(
        currentVersion: String,
        channel: UpdateChannel = UpdateChannel.GITEE_FIRST,
        ignoredVersions: Set<String> = emptySet()
    ): CheckResult {
        Log.d(TAG, "Checking for update, current version: $currentVersion, channel: $channel")

        // 根据通道确定请求顺序进行请求
        val (primaryResult, fallbackFetcher) = when (channel) {
            UpdateChannel.GITEE_FIRST -> {
                Pair(apiService.fetchFromGitee(), suspend { apiService.fetchFromGitHub() })
            }
            UpdateChannel.GITHUB_FIRST -> {
                Pair(apiService.fetchFromGitHub(), suspend { apiService.fetchFromGitee() })
            }
        }

        // 先尝试主源
        var updateInfo: UpdateInfo? = null
        var lastError: String? = null

        primaryResult.onSuccess {
            updateInfo = it
            Log.d(TAG, "Primary source success: ${it.version} from ${it.source}")
        }.onFailure { e ->
            Log.w(TAG, "Primary source failed, trying fallback", e)
            lastError = e.message

            // 主源失败，尝试备用源
            fallbackFetcher().onSuccess {
                updateInfo = it
                Log.d(TAG, "Fallback source success: ${it.version} from ${it.source}")
            }.onFailure { fallbackError ->
                Log.e(TAG, "Both sources failed", fallbackError)
                lastError = "主源: ${e.message}, 备用源: ${fallbackError.message}"
            }
        }

        // 处理结果
        val info = updateInfo ?: return CheckResult.Error(lastError ?: "未知错误")

        // 检查是否已忽略该版本
        if (ignoredVersions.contains(info.version)) {
            Log.d(TAG, "Version ${info.version} is ignored")
            return CheckResult.UpToDate
        }

        // 比较版本
        return if (VersionUtils.isNewerVersion(currentVersion, info.version)) {
            Log.i(TAG, "New version available: ${info.version}")
            CheckResult.UpdateAvailable(info)
        } else {
            Log.d(TAG, "Already up to date")
            CheckResult.UpToDate
        }
    }

    /**
     * 判断是否应该自动检查更新
     *
     * @param lastCheckTime 上次检查时间戳
     * @return true 表示应该检查
     */
    fun shouldAutoCheck(lastCheckTime: Long): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastCheckTime) >= CHECK_INTERVAL_MS
    }
}
