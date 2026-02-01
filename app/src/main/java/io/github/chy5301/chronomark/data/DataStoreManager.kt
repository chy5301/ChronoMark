package io.github.chy5301.chronomark.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.ThemeMode
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.data.model.UpdateChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chronomark_preferences")

/**
 * DataStore 管理器，负责应用数据的持久化
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // 应用设置
        private val KEY_CURRENT_MODE = stringPreferencesKey("current_mode")
        private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LAST_TIMEZONE_ID = stringPreferencesKey("last_timezone_id")

        // 归档设置
        private val KEY_ARCHIVE_BOUNDARY_HOUR = intPreferencesKey("archive_boundary_hour")
        private val KEY_ARCHIVE_BOUNDARY_MINUTE = intPreferencesKey("archive_boundary_minute")
        private val KEY_AUTO_ARCHIVE_ENABLED = booleanPreferencesKey("auto_archive_enabled")
        private val KEY_HISTORY_RETENTION_DAYS = intPreferencesKey("history_retention_days")

        // 秒表模式数据
        private val KEY_STOPWATCH_STATUS = stringPreferencesKey("stopwatch_status")
        private val KEY_STOPWATCH_START_TIME = longPreferencesKey("stopwatch_start_time")
        private val KEY_STOPWATCH_PAUSED_TIME = longPreferencesKey("stopwatch_paused_time")
        private val KEY_STOPWATCH_PAUSE_TIMESTAMP = longPreferencesKey("stopwatch_pause_timestamp")
        private val KEY_STOPWATCH_RECORDS = stringPreferencesKey("stopwatch_records")

        // 事件模式数据
        private val KEY_EVENT_RECORDS = stringPreferencesKey("event_records")

        // 更新检查设置
        private val KEY_AUTO_UPDATE_CHECK_ENABLED = booleanPreferencesKey("auto_update_check_enabled")
        private val KEY_LAST_UPDATE_CHECK_TIME = longPreferencesKey("last_update_check_time")
        private val KEY_IGNORED_VERSIONS = stringPreferencesKey("ignored_versions")
        private val KEY_UPDATE_CHANNEL = stringPreferencesKey("update_channel")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ==================== 应用设置 ====================

    /**
     * 保存当前模式
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveCurrentMode(mode: AppMode): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_CURRENT_MODE] = mode.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取当前模式
     */
    val currentModeFlow: Flow<AppMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeName = preferences[KEY_CURRENT_MODE] ?: AppMode.EVENT.name
            try {
                AppMode.valueOf(modeName)
            } catch (_: IllegalArgumentException) {
                AppMode.EVENT
            }
        }

    /**
     * 保存保持屏幕常亮设置
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveKeepScreenOn(enabled: Boolean): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_KEEP_SCREEN_ON] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取保持屏幕常亮设置
     */
    val keepScreenOnFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_KEEP_SCREEN_ON] ?: false
        }

    /**
     * 保存震动反馈设置
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveVibrationEnabled(enabled: Boolean): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_VIBRATION_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取震动反馈设置
     */
    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_VIBRATION_ENABLED] ?: true
        }

    /**
     * 保存主题模式
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveThemeMode(mode: ThemeMode): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_THEME_MODE] = mode.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取主题模式
     */
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeName = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(modeName)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    /**
     * 保存上次记录的时区 ID
     * @param timezoneId 时区 ID（如 "Asia/Shanghai"）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveLastTimezoneId(timezoneId: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_LAST_TIMEZONE_ID] = timezoneId
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取上次记录的时区 ID
     * 首次运行或更新时返回空字符串
     */
    val lastTimezoneIdFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_TIMEZONE_ID] ?: ""
        }

    // ==================== 归档设置 ====================

    /**
     * 保存归档分界点 - 时
     * @param hour 时（0-23）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveArchiveBoundaryHour(hour: Int): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_ARCHIVE_BOUNDARY_HOUR] = hour
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取归档分界点 - 时
     */
    val archiveBoundaryHourFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ARCHIVE_BOUNDARY_HOUR] ?: 4  // 默认凌晨 4 点
        }

    /**
     * 保存归档分界点 - 分
     * @param minute 分（0-59）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveArchiveBoundaryMinute(minute: Int): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_ARCHIVE_BOUNDARY_MINUTE] = minute
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取归档分界点 - 分
     */
    val archiveBoundaryMinuteFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ARCHIVE_BOUNDARY_MINUTE] ?: 0  // 默认 0 分
        }

    /**
     * 保存自动归档开关
     * @param enabled 是否启用自动归档
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveAutoArchiveEnabled(enabled: Boolean): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_AUTO_ARCHIVE_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取自动归档开关
     */
    val autoArchiveEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_ARCHIVE_ENABLED] ?: true  // 默认启用
        }

    /**
     * 保存历史记录保留天数
     * @param days 保留天数（-1 表示永久保留）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveHistoryRetentionDays(days: Int): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_HISTORY_RETENTION_DAYS] = days
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取历史记录保留天数
     */
    val historyRetentionDaysFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HISTORY_RETENTION_DAYS] ?: 365  // 默认保留 365 天
        }

    // ==================== 秒表模式数据 ====================

    /**
     * 保存秒表状态
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveStopwatchStatus(status: StopwatchStatus): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_STOPWATCH_STATUS] = when (status) {
                    is StopwatchStatus.Idle -> "Idle"
                    is StopwatchStatus.Running -> "Running"
                    is StopwatchStatus.Paused -> "Paused"
                    is StopwatchStatus.Stopped -> "Stopped"
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取秒表状态
     */
    val stopwatchStatusFlow: Flow<StopwatchStatus> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[KEY_STOPWATCH_STATUS]) {
                "Running" -> StopwatchStatus.Running
                "Paused" -> StopwatchStatus.Paused
                "Stopped" -> StopwatchStatus.Stopped
                else -> StopwatchStatus.Idle
            }
        }

    /**
     * 保存秒表经过的时间（纳秒）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveStopwatchElapsedTime(elapsedNanos: Long): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_STOPWATCH_PAUSE_TIMESTAMP] = elapsedNanos
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取秒表经过的时间（纳秒）
     */
    val stopwatchElapsedTimeFlow: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_STOPWATCH_PAUSE_TIMESTAMP] ?: 0L
        }

    /**
     * 保存秒表记录列表
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveStopwatchRecords(records: List<TimeRecord>): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val jsonString = json.encodeToString(records)
                preferences[KEY_STOPWATCH_RECORDS] = jsonString
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取秒表记录列表
     */
    val stopwatchRecordsFlow: Flow<List<TimeRecord>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_STOPWATCH_RECORDS]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    json.decodeFromString<List<TimeRecord>>(jsonString)
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

    // ==================== 事件模式数据 ====================

    /**
     * 保存事件记录列表
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveEventRecords(records: List<TimeRecord>): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val jsonString = json.encodeToString(records)
                preferences[KEY_EVENT_RECORDS] = jsonString
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取事件记录列表
     */
    val eventRecordsFlow: Flow<List<TimeRecord>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_EVENT_RECORDS]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    json.decodeFromString<List<TimeRecord>>(jsonString)
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

    // ==================== 更新检查设置 ====================

    /**
     * 保存自动检查更新开关
     * @param enabled 是否启用自动检查
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveAutoUpdateCheckEnabled(enabled: Boolean): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_AUTO_UPDATE_CHECK_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取自动检查更新开关
     */
    val autoUpdateCheckEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_UPDATE_CHECK_ENABLED] ?: true  // 默认启用
        }

    /**
     * 保存上次检查更新时间
     * @param timestamp 时间戳（毫秒）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveLastUpdateCheckTime(timestamp: Long): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_LAST_UPDATE_CHECK_TIME] = timestamp
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取上次检查更新时间
     */
    val lastUpdateCheckTimeFlow: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_UPDATE_CHECK_TIME] ?: 0L
        }

    /**
     * 保存忽略的版本列表
     * @param versions 版本号集合
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveIgnoredVersions(versions: Set<String>): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val jsonString = json.encodeToString(versions.toList())
                preferences[KEY_IGNORED_VERSIONS] = jsonString
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 添加忽略的版本
     * @param version 要忽略的版本号
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun addIgnoredVersion(version: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[KEY_IGNORED_VERSIONS] ?: "[]"
                val currentVersions = try {
                    json.decodeFromString<List<String>>(currentJson).toMutableSet()
                } catch (_: Exception) {
                    mutableSetOf()
                }
                currentVersions.add(version)
                preferences[KEY_IGNORED_VERSIONS] = json.encodeToString(currentVersions.toList())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取忽略的版本列表
     */
    val ignoredVersionsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[KEY_IGNORED_VERSIONS]
            if (jsonString.isNullOrEmpty()) {
                emptySet()
            } else {
                try {
                    json.decodeFromString<List<String>>(jsonString).toSet()
                } catch (_: Exception) {
                    emptySet()
                }
            }
        }

    /**
     * 保存更新通道设置
     * @param channel 更新通道
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun saveUpdateChannel(channel: UpdateChannel): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_UPDATE_CHANNEL] = channel.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 读取更新通道设置
     */
    val updateChannelFlow: Flow<UpdateChannel> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val channelName = preferences[KEY_UPDATE_CHANNEL] ?: UpdateChannel.GITEE_FIRST.name
            try {
                UpdateChannel.valueOf(channelName)
            } catch (_: IllegalArgumentException) {
                UpdateChannel.GITEE_FIRST
            }
        }

    // ==================== 清除数据 ====================

    /**
     * 清除所有秒表数据
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun clearStopwatchData(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.remove(KEY_STOPWATCH_STATUS)
                preferences.remove(KEY_STOPWATCH_PAUSE_TIMESTAMP)  // 当前使用：存储经过的时间
                preferences.remove(KEY_STOPWATCH_RECORDS)
                // 清除已废弃的 key（兼容旧版本数据）
                preferences.remove(KEY_STOPWATCH_START_TIME)
                preferences.remove(KEY_STOPWATCH_PAUSED_TIME)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 清除所有事件数据
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun clearEventData(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.remove(KEY_EVENT_RECORDS)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 清空事件记录（归档后使用）
     * 别名方法，与 clearEventData() 功能相同
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun clearEventRecords(): Result<Unit> {
        return clearEventData()
    }

    /**
     * 清空秒表记录（归档后使用）
     * 只清空记录，不清空状态
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    suspend fun clearStopwatchRecords(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.remove(KEY_STOPWATCH_RECORDS)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 清除所有数据
     * 注：此函数保留以备将来使用（如设置页面的"清除所有数据"功能）
     * @return Result.success(Unit) 或 Result.failure(exception)
     */
    @Suppress("unused")
    suspend fun clearAllData(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
