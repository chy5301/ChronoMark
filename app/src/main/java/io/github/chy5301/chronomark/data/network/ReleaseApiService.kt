package io.github.chy5301.chronomark.data.network

import io.github.chy5301.chronomark.data.model.GiteeReleaseInfo
import io.github.chy5301.chronomark.data.model.GitHubReleaseInfo
import io.github.chy5301.chronomark.data.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Release API 请求服务
 * 支持从 Gitee 和 GitHub 获取最新版本信息
 */
class ReleaseApiService {

    companion object {
        // Gitee API
        private const val GITEE_API_URL =
            "https://gitee.com/api/v5/repos/chy5301/ChronoMark/releases/latest"

        // GitHub API
        private const val GITHUB_API_URL =
            "https://api.github.com/repos/chy5301/ChronoMark/releases/latest"

        // 超时设置
        private const val TIMEOUT_SECONDS = 10L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 从 Gitee 获取最新版本信息
     *
     * @return Result<UpdateInfo> 成功返回更新信息，失败返回异常
     */
    suspend fun fetchFromGitee(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITEE_API_URL)
                .header("Accept", "application/json")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Gitee API error: ${response.code} ${response.message}")
                )
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response from Gitee"))

            val releaseInfo = json.decodeFromString<GiteeReleaseInfo>(body)
            Result.success(UpdateInfo.fromGitee(releaseInfo))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 GitHub 获取最新版本信息
     *
     * @return Result<UpdateInfo> 成功返回更新信息，失败返回异常
     */
    suspend fun fetchFromGitHub(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "ChronoMark-App")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("GitHub API error: ${response.code} ${response.message}")
                )
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response from GitHub"))

            val releaseInfo = json.decodeFromString<GitHubReleaseInfo>(body)
            Result.success(UpdateInfo.fromGitHub(releaseInfo))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
