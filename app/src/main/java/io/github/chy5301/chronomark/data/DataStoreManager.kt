package io.github.chy5301.chronomark.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
            } catch (e: IllegalArgumentException) {
                AppMode.EVENT
            }
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
     * 保存秒表时间数据
     */
    suspend fun saveStopwatchTime(
        startTimeNanos: Long,
        pausedTimeNanos: Long,
        pauseTimestamp: Long = 0L
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STOPWATCH_START_TIME] = startTimeNanos
            preferences[KEY_STOPWATCH_PAUSED_TIME] = pausedTimeNanos
            preferences[KEY_STOPWATCH_PAUSE_TIMESTAMP] = pauseTimestamp
        }
    }

    /**
     * 读取秒表时间数据
     */
    data class StopwatchTimeData(
        val startTimeNanos: Long = 0L,
        val pausedTimeNanos: Long = 0L,
        val pauseTimestamp: Long = 0L
    )

    val stopwatchTimeFlow: Flow<StopwatchTimeData> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            StopwatchTimeData(
                startTimeNanos = preferences[KEY_STOPWATCH_START_TIME] ?: 0L,
                pausedTimeNanos = preferences[KEY_STOPWATCH_PAUSED_TIME] ?: 0L,
                pauseTimestamp = preferences[KEY_STOPWATCH_PAUSE_TIMESTAMP] ?: 0L
            )
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
                } catch (e: Exception) {
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
                } catch (e: Exception) {
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
            preferences.remove(KEY_STOPWATCH_START_TIME)
            preferences.remove(KEY_STOPWATCH_PAUSED_TIME)
            preferences.remove(KEY_STOPWATCH_PAUSE_TIMESTAMP)
            preferences.remove(KEY_STOPWATCH_RECORDS)
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
     */
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
