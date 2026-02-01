package io.github.chy5301.chronomark.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * VersionUtils 单元测试
 *
 * 测试版本号解析和比较逻辑
 */
class VersionUtilsTest {

    // ========== parseVersion 测试 ==========

    @Test
    fun `parseVersion - standard version - returns correct parts`() {
        val result = VersionUtils.parseVersion("1.2.3")

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseVersion - with lowercase v prefix - strips prefix and returns correct parts`() {
        val result = VersionUtils.parseVersion("v1.2.3")

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseVersion - with uppercase V prefix - strips prefix and returns correct parts`() {
        val result = VersionUtils.parseVersion("V1.2.3")

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseVersion - with prerelease suffix - strips suffix and returns correct parts`() {
        val result = VersionUtils.parseVersion("v1.2.3-beta")

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `parseVersion - two segment version - returns two parts`() {
        val result = VersionUtils.parseVersion("1.2")

        assertEquals(listOf(1, 2), result)
    }

    @Test
    fun `parseVersion - four segment version - returns four parts`() {
        val result = VersionUtils.parseVersion("1.2.3.4")

        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun `parseVersion - with alpha suffix - strips suffix correctly`() {
        val result = VersionUtils.parseVersion("v2.0.0-alpha.1")

        assertEquals(listOf(2, 0, 0), result)
    }

    @Test
    fun `parseVersion - with rc suffix - strips suffix correctly`() {
        val result = VersionUtils.parseVersion("1.0.0-rc.2")

        assertEquals(listOf(1, 0, 0), result)
    }

    // ========== compareVersions 测试 ==========

    @Test
    fun `compareVersions - same version - returns zero`() {
        val result = VersionUtils.compareVersions("1.2.3", "1.2.3")

        assertEquals(0, result)
    }

    @Test
    fun `compareVersions - same version with v prefix - returns zero`() {
        val result = VersionUtils.compareVersions("v1.2.3", "1.2.3")

        assertEquals(0, result)
    }

    @Test
    fun `compareVersions - patch update available - returns positive`() {
        val result = VersionUtils.compareVersions("1.2.3", "1.2.4")

        assertEquals(1, result)
    }

    @Test
    fun `compareVersions - minor update available - returns positive`() {
        val result = VersionUtils.compareVersions("1.2.3", "1.3.0")

        assertEquals(1, result)
    }

    @Test
    fun `compareVersions - major update available - returns positive`() {
        val result = VersionUtils.compareVersions("1.2.3", "2.0.0")

        assertEquals(1, result)
    }

    @Test
    fun `compareVersions - remote is older patch - returns negative`() {
        val result = VersionUtils.compareVersions("1.2.4", "1.2.3")

        assertEquals(-1, result)
    }

    @Test
    fun `compareVersions - remote is older minor - returns negative`() {
        val result = VersionUtils.compareVersions("1.3.0", "1.2.9")

        assertEquals(-1, result)
    }

    @Test
    fun `compareVersions - remote is older major - returns negative`() {
        val result = VersionUtils.compareVersions("2.0.0", "1.9.9")

        assertEquals(-1, result)
    }

    @Test
    fun `compareVersions - different length versions - handles correctly`() {
        // 1.2 vs 1.2.0 应该相等
        assertEquals(0, VersionUtils.compareVersions("1.2", "1.2.0"))

        // 1.2 vs 1.2.1 应该是远程更新
        assertEquals(1, VersionUtils.compareVersions("1.2", "1.2.1"))

        // 1.2.1 vs 1.2 应该是当前更新
        assertEquals(-1, VersionUtils.compareVersions("1.2.1", "1.2"))
    }

    // ========== isNewerVersion 测试 ==========

    @Test
    fun `isNewerVersion - remote is newer - returns true`() {
        val result = VersionUtils.isNewerVersion("1.2.3", "1.2.4")

        assertTrue(result)
    }

    @Test
    fun `isNewerVersion - remote is same - returns false`() {
        val result = VersionUtils.isNewerVersion("1.2.3", "1.2.3")

        assertFalse(result)
    }

    @Test
    fun `isNewerVersion - remote is older - returns false`() {
        val result = VersionUtils.isNewerVersion("1.2.4", "1.2.3")

        assertFalse(result)
    }

    @Test
    fun `isNewerVersion - major update - returns true`() {
        val result = VersionUtils.isNewerVersion("1.9.9", "2.0.0")

        assertTrue(result)
    }

    @Test
    fun `isNewerVersion - with v prefix - works correctly`() {
        assertTrue(VersionUtils.isNewerVersion("v1.0.0", "v1.0.1"))
        assertFalse(VersionUtils.isNewerVersion("v1.0.1", "v1.0.0"))
    }
}
