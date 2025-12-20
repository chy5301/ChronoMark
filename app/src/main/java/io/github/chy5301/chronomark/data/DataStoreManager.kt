package io.github.chy5301.chronomark.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.TimeRecord
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

        // 秒表模式数据
        private val KEY_STOPWATCH_STATUS = stringPreferencesKey("stopwatch_status")
        private val KEY_STOPWATCH_START_TIME = longPreferencesKey("stopwatch_start_time")
        private val KEY_STOPWATCH_PAUSED_TIME = longPreferencesKey("stopwatch_paused_time")
        private val KEY_STOPWATCH_PAUSE_TIMESTAMP = longPreferencesKey("stopwatch_pause_timestamp")
        private val KEY_STOPWATCH_RECORDS = stringPreferencesKey("stopwatch_records")

        // 事件模式数据
        private val KEY_EVENT_RECORDS = stringPreferencesKey("event_records")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ==================== 应用设置 ====================

    /**
     * 保存当前模式
     */
    suspend fun saveCurrentMode(mode: AppMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_MODE] = mode.name
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
     */
    suspend fun saveKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_KEEP_SCREEN_ON] = enabled
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
     */
    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIBRATION_ENABLED] = enabled
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

    // ==================== 秒表模式数据 ====================

    /**
     * 保存秒表状态
     */
    suspend fun saveStopwatchStatus(status: StopwatchStatus) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STOPWATCH_STATUS] = when (status) {
                is StopwatchStatus.Idle -> "Idle"
                is StopwatchStatus.Running -> "Running"
                is StopwatchStatus.Paused -> "Paused"
                is StopwatchStatus.Stopped -> "Stopped"
            }
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
     */
    suspend fun saveStopwatchElapsedTime(elapsedNanos: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STOPWATCH_PAUSE_TIMESTAMP] = elapsedNanos
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
     */
    suspend fun saveStopwatchRecords(records: List<TimeRecord>) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(records)
            preferences[KEY_STOPWATCH_RECORDS] = jsonString
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
     */
    suspend fun saveEventRecords(records: List<TimeRecord>) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(records)
            preferences[KEY_EVENT_RECORDS] = jsonString
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

    // ==================== 清除数据 ====================

    /**
     * 清除所有秒表数据
     */
    suspend fun clearStopwatchData() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_STOPWATCH_STATUS)
            preferences.remove(KEY_STOPWATCH_PAUSE_TIMESTAMP)  // 当前使用：存储经过的时间
            preferences.remove(KEY_STOPWATCH_RECORDS)
            // 清除已废弃的 key（兼容旧版本数据）
            preferences.remove(KEY_STOPWATCH_START_TIME)
            preferences.remove(KEY_STOPWATCH_PAUSED_TIME)
        }
    }

    /**
     * 清除所有事件数据
     */
    suspend fun clearEventData() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_EVENT_RECORDS)
        }
    }

    /**
     * 清除所有数据
     * 注：此函数保留以备将来使用（如设置页面的"清除所有数据"功能）
     */
    @Suppress("unused")
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
