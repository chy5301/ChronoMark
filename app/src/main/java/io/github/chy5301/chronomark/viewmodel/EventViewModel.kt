package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.EventUiState
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.util.ShareHelper
import io.github.chy5301.chronomark.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 事件模式 ViewModel
 */
class EventViewModel(
    private val dataStoreManager: DataStoreManager,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "EventViewModel"
    }

    // UI 状态
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // 墙上时钟协程（始终运行）
    private var wallClockJob: Job? = null

    init {
        // 启动墙上时钟更新（始终运行）
        startWallClockTicking()
        // 加载保存的记录
        loadSavedRecords()
    }

    /**
     * 记录事件（立即记录当前时间点）
     */
    fun recordEvent() {
        val currentWallClockTime = System.currentTimeMillis()
        val lastTimestamp =
            _uiState.value.records.lastOrNull()?.wallClockTime ?: currentWallClockTime
        // 确保时间差非负，防止系统时间被调整到过去导致的负数
        val timeDiff = (currentWallClockTime - lastTimestamp).coerceAtLeast(0L)
        val splitNanos = timeDiff * 1_000_000 // 毫秒转纳秒

        val newRecord = TimeRecord(
            index = _uiState.value.records.size + 1,
            wallClockTime = currentWallClockTime,
            elapsedTimeNanos = 0L,  // 事件模式不需要累计时间
            splitTimeNanos = splitNanos,
            note = ""
        )

        _uiState.update {
            it.copy(records = it.records + listOf(newRecord))
        }
        saveRecords()
    }

    /**
     * 重置所有记录
     */
    fun reset() {
        _uiState.update {
            EventUiState(
                wallClockTime = TimeFormatter.formatWallClockWithDate(System.currentTimeMillis()),
                records = emptyList()
            )
        }
        // 清除保存的数据
        viewModelScope.launch {
            dataStoreManager.clearEventData()
                .onFailure { e ->
                    // 记录错误，但不影响 UI 状态重置
                    e.printStackTrace()
                }
        }
    }

    /**
     * 更新记录的备注
     */
    fun updateRecordNote(recordId: String, note: String) {
        _uiState.update { state ->
            state.copy(
                records = state.records.map { record ->
                    if (record.id == recordId) record.copy(note = note) else record
                }
            )
        }
        saveRecords()
    }

    /**
     * 删除记录
     */
    fun deleteRecord(recordId: String) {
        _uiState.update { state ->
            val updatedRecords = state.records.filter { it.id != recordId }
            // 重新计算序号（正序排列）
            val reindexedRecords = updatedRecords.mapIndexed { index, record ->
                record.copy(index = index + 1)
            }
            state.copy(records = reindexedRecords)
        }
        saveRecords()
    }

    /**
     * 启动墙上时钟刻度（始终运行）
     */
    private fun startWallClockTicking() {
        wallClockJob = viewModelScope.launch {
            while (isActive) {
                updateWallClock()
                delay(1000) // 每秒更新一次墙上时钟
            }
        }
    }

    /**
     * 更新墙上时钟显示
     */
    private fun updateWallClock() {
        val currentWallClockTime = System.currentTimeMillis()
        _uiState.update {
            it.copy(wallClockTime = TimeFormatter.formatWallClockWithDate(currentWallClockTime))
        }
    }

    /**
     * 加载保存的记录
     */
    private fun loadSavedRecords() {
        viewModelScope.launch {
            val savedRecords = dataStoreManager.eventRecordsFlow.first()
            _uiState.update {
                it.copy(records = savedRecords)
            }
        }
    }

    /**
     * 生成分享文本
     */
    fun generateShareText(): String {
        return ShareHelper.generateEventShareText(_uiState.value.records)
    }

    /**
     * 自动归档（跨天时触发）
     * 将昨日的事件记录归档到历史数据库
     */
    suspend fun autoArchive() {
        val records = _uiState.value.records
        if (records.isEmpty()) {
            Log.i(TAG, "No records to archive")
            return
        }

        Log.i(TAG, "Starting auto archive for ${records.size} records")

        // 1. 归档到 Room 数据库
        historyRepository.archiveEventRecords(records)
            .onSuccess {
                Log.i(TAG, "Archive successful, clearing workspace")

                // 2. 清空 DataStore 工作区
                dataStoreManager.clearEventRecords()
                    .onFailure { e ->
                        Log.e(TAG, "Failed to clear workspace after archive", e)
                    }

                // 3. 更新 UI 状态
                _uiState.update { it.copy(records = emptyList()) }

                Log.i(TAG, "Auto archive completed: ${records.size} records archived")
            }
            .onFailure { e ->
                Log.e(TAG, "Archive failed", e)
            }
    }

    /**
     * 保存记录
     */
    private fun saveRecords() {
        viewModelScope.launch {
            dataStoreManager.saveEventRecords(_uiState.value.records)
                .onFailure { e -> e.printStackTrace() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 保存记录
        saveRecords()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
