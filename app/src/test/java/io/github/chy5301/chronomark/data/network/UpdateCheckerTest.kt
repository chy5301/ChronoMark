package io.github.chy5301.chronomark.data.network

import io.github.chy5301.chronomark.data.model.UpdateChannel
import io.github.chy5301.chronomark.data.model.UpdateInfo
import io.github.chy5301.chronomark.data.model.UpdateSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * UpdateChecker 单元测试
 *
 * 测试更新检查逻辑（使用 MockK mock ReleaseApiService）
 */
class UpdateCheckerTest {

    private lateinit var mockApiService: ReleaseApiService
    private lateinit var updateChecker: UpdateChecker

    // 测试常量
    companion object {
        private const val TEST_CURRENT_VERSION = "1.0.0"
        private const val TEST_NEWER_VERSION = "1.0.1"
        private const val TEST_SAME_VERSION = "1.0.0"
    }

    @Before
    fun setup() {
        mockApiService = mockk()
        updateChecker = UpdateChecker(mockApiService)
    }

    // ========== 创建测试 UpdateInfo 的辅助方法 ==========

    private fun createUpdateInfo(
        version: String,
        source: UpdateSource = UpdateSource.GITEE
    ): UpdateInfo {
        return UpdateInfo(
            version = version,
            name = "Release $version",
            body = "Changelog for $version",
            htmlUrl = "https://example.com/release/$version",
            source = source
        )
    }

    // ========== checkForUpdate 主源成功测试 ==========

    @Test
    fun `checkForUpdate - primary source returns newer version - returns UpdateAvailable`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITEE)
        coEvery { mockApiService.fetchFromGitee() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpdateAvailable)
        val updateResult = result as UpdateChecker.CheckResult.UpdateAvailable
        assertEquals(TEST_NEWER_VERSION, updateResult.updateInfo.version)
        assertEquals(UpdateSource.GITEE, updateResult.updateInfo.source)
    }

    @Test
    fun `checkForUpdate - primary source returns same version - returns UpToDate`() = runTest {
        // Arrange
        val sameInfo = createUpdateInfo(TEST_SAME_VERSION, UpdateSource.GITEE)
        coEvery { mockApiService.fetchFromGitee() } returns Result.success(sameInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpToDate)
    }

    // ========== 主源失败备用成功测试 ==========

    @Test
    fun `checkForUpdate - primary fails fallback succeeds with newer version - returns UpdateAvailable`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITHUB)
        coEvery { mockApiService.fetchFromGitee() } returns Result.failure(Exception("Gitee failed"))
        coEvery { mockApiService.fetchFromGitHub() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpdateAvailable)
        val updateResult = result as UpdateChecker.CheckResult.UpdateAvailable
        assertEquals(TEST_NEWER_VERSION, updateResult.updateInfo.version)
        assertEquals(UpdateSource.GITHUB, updateResult.updateInfo.source)
    }

    @Test
    fun `checkForUpdate - primary fails fallback succeeds with same version - returns UpToDate`() = runTest {
        // Arrange
        val sameInfo = createUpdateInfo(TEST_SAME_VERSION, UpdateSource.GITHUB)
        coEvery { mockApiService.fetchFromGitee() } returns Result.failure(Exception("Gitee failed"))
        coEvery { mockApiService.fetchFromGitHub() } returns Result.success(sameInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpToDate)
    }

    // ========== 双源都失败测试 ==========

    @Test
    fun `checkForUpdate - both sources fail - returns Error`() = runTest {
        // Arrange
        coEvery { mockApiService.fetchFromGitee() } returns Result.failure(Exception("Gitee failed"))
        coEvery { mockApiService.fetchFromGitHub() } returns Result.failure(Exception("GitHub failed"))

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.Error)
        val errorResult = result as UpdateChecker.CheckResult.Error
        assertTrue(errorResult.message.contains("Gitee"))
        assertTrue(errorResult.message.contains("GitHub"))
    }

    // ========== 忽略版本测试 ==========

    @Test
    fun `checkForUpdate - newer version in ignored list - returns UpToDate`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITEE)
        coEvery { mockApiService.fetchFromGitee() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST,
            ignoredVersions = setOf(TEST_NEWER_VERSION)
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpToDate)
    }

    @Test
    fun `checkForUpdate - newer version not in ignored list - returns UpdateAvailable`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITEE)
        coEvery { mockApiService.fetchFromGitee() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITEE_FIRST,
            ignoredVersions = setOf("0.9.0", "0.9.5") // 其他版本在忽略列表
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpdateAvailable)
    }

    // ========== GitHub 优先通道测试 ==========

    @Test
    fun `checkForUpdate - github first channel - calls GitHub first`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITHUB)
        coEvery { mockApiService.fetchFromGitHub() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITHUB_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpdateAvailable)
        coVerify(exactly = 1) { mockApiService.fetchFromGitHub() }
        coVerify(exactly = 0) { mockApiService.fetchFromGitee() }
    }

    @Test
    fun `checkForUpdate - github first fails - uses gitee as fallback`() = runTest {
        // Arrange
        val newerInfo = createUpdateInfo(TEST_NEWER_VERSION, UpdateSource.GITEE)
        coEvery { mockApiService.fetchFromGitHub() } returns Result.failure(Exception("GitHub failed"))
        coEvery { mockApiService.fetchFromGitee() } returns Result.success(newerInfo)

        // Act
        val result = updateChecker.checkForUpdate(
            currentVersion = TEST_CURRENT_VERSION,
            channel = UpdateChannel.GITHUB_FIRST
        )

        // Assert
        assertTrue(result is UpdateChecker.CheckResult.UpdateAvailable)
        coVerifyOrder {
            mockApiService.fetchFromGitHub()
            mockApiService.fetchFromGitee()
        }
    }

    // ========== shouldAutoCheck 测试 ==========

    @Test
    fun `shouldAutoCheck - never checked before - returns true`() {
        // Arrange
        val lastCheckTime = 0L

        // Act
        val result = updateChecker.shouldAutoCheck(lastCheckTime)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `shouldAutoCheck - checked less than 24h ago - returns false`() {
        // Arrange
        val now = System.currentTimeMillis()
        val lastCheckTime = now - (12 * 60 * 60 * 1000L) // 12小时前

        // Act
        val result = updateChecker.shouldAutoCheck(lastCheckTime)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `shouldAutoCheck - checked exactly 24h ago - returns true`() {
        // Arrange
        val now = System.currentTimeMillis()
        val lastCheckTime = now - UpdateChecker.CHECK_INTERVAL_MS

        // Act
        val result = updateChecker.shouldAutoCheck(lastCheckTime)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `shouldAutoCheck - checked more than 24h ago - returns true`() {
        // Arrange
        val now = System.currentTimeMillis()
        val lastCheckTime = now - (48 * 60 * 60 * 1000L) // 48小时前

        // Act
        val result = updateChecker.shouldAutoCheck(lastCheckTime)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `shouldAutoCheck - checked 23h59m ago - returns false`() {
        // Arrange
        val now = System.currentTimeMillis()
        val lastCheckTime = now - (24 * 60 * 60 * 1000L - 60000L) // 差一分钟到 24 小时

        // Act
        val result = updateChecker.shouldAutoCheck(lastCheckTime)

        // Assert
        assertFalse(result)
    }
}
