package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.model.EventUiState
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 事件模式 ViewModel
 */
class EventViewModel : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // 墙上时钟协程（始终运行）
    private var wallClockJob: Job? = null

    init {
        // 启动墙上时钟更新（始终运行）
        startWallClockTicking()
    }

    /**
     * 记录事件（立即记录当前时间点）
     */
    fun recordEvent() {
        val currentWallClockTime = System.currentTimeMillis()
        val lastTimestamp = _uiState.value.records.lastOrNull()?.wallClockTime ?: currentWallClockTime
        val splitNanos = (currentWallClockTime - lastTimestamp) * 1_000_000 // 毫秒转纳秒

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

    override fun onCleared() {
        super.onCleared()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
