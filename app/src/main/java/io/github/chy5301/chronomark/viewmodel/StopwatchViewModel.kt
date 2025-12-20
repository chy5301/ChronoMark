package io.github.chy5301.chronomark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.StopwatchUiState
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
 * 秒表 ViewModel
 */
class StopwatchViewModel(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    // 计时器协程
    private var timerJob: Job? = null

    // 墙上时钟协程（独立运行，不受秒表状态影响）
    private var wallClockJob: Job? = null

    // 计时相关变量
    private var startTimeNanos: Long = 0L
    private var pausedTimeNanos: Long = 0L
    private var totalPausedTimeNanos: Long = 0L

    init {
        // 启动墙上时钟更新（始终运行）
        startWallClockTicking()
        // 加载保存的状态
        loadSavedState()
    }

    /**
     * 开始计时
     */
    fun start() {
        if (_uiState.value.status != StopwatchStatus.Idle) return

        startTimeNanos = System.nanoTime()
        totalPausedTimeNanos = 0L
        _uiState.update { it.copy(status = StopwatchStatus.Running) }
        startTimerTicking()
        saveCurrentState()
    }

    /**
     * 暂停计时
     */
    fun pause() {
        if (_uiState.value.status != StopwatchStatus.Running) return

        pausedTimeNanos = System.nanoTime()
        _uiState.update { it.copy(status = StopwatchStatus.Paused) }
        stopTimerTicking()
        saveCurrentState()
    }

    /**
     * 继续计时
     */
    fun resume() {
        if (_uiState.value.status != StopwatchStatus.Paused) return

        val pauseDuration = System.nanoTime() - pausedTimeNanos
        totalPausedTimeNanos += pauseDuration
        _uiState.update { it.copy(status = StopwatchStatus.Running) }
        startTimerTicking()
        saveCurrentState()
    }

    /**
     * 停止计时
     */
    fun stop() {
        if (_uiState.value.status != StopwatchStatus.Paused) return

        _uiState.update { it.copy(status = StopwatchStatus.Stopped) }
        stopTimerTicking()
        saveCurrentState()
    }

    /**
     * 重置秒表
     */
    fun reset() {
        stopTimerTicking()
        startTimeNanos = 0L
        pausedTimeNanos = 0L
        totalPausedTimeNanos = 0L
        _uiState.update {
            StopwatchUiState(
                status = StopwatchStatus.Idle,
                currentTime = "00:00.000",
                wallClockTime = TimeFormatter.formatWallClockWithDate(System.currentTimeMillis()),
                currentTimeNanos = 0L,
                records = emptyList()
            )
        }
        // 清除保存的数据
        viewModelScope.launch {
            dataStoreManager.clearStopwatchData()
        }
    }

    /**
     * 添加标记
     */
    fun addMark() {
        if (_uiState.value.status != StopwatchStatus.Running) return

        val currentNanos = getCurrentElapsedTime()
        val currentWallClockTime = System.currentTimeMillis()
        val lastNanos = _uiState.value.records.firstOrNull()?.elapsedTimeNanos ?: 0L
        val splitNanos = currentNanos - lastNanos

        val newRecord = TimeRecord(
            index = _uiState.value.records.size + 1,
            wallClockTime = currentWallClockTime,
            elapsedTimeNanos = currentNanos,
            splitTimeNanos = splitNanos,
            note = ""
        )

        _uiState.update {
            it.copy(records = listOf(newRecord) + it.records)
        }
        saveCurrentState()
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
        saveCurrentState()
    }

    /**
     * 删除记录
     */
    fun deleteRecord(recordId: String) {
        _uiState.update { state ->
            val updatedRecords = state.records.filter { it.id != recordId }
            // 重新计算序号
            val reindexedRecords = updatedRecords.mapIndexed { index, record ->
                record.copy(index = updatedRecords.size - index)
            }
            state.copy(records = reindexedRecords)
        }
        saveCurrentState()
    }

    /**
     * 获取当前经过的时间（纳秒）
     */
    private fun getCurrentElapsedTime(): Long {
        return System.nanoTime() - startTimeNanos - totalPausedTimeNanos
    }

    /**
     * 启动计时器刻度
     */
    private fun startTimerTicking() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(10) // 每 10ms 更新一次
                updateCurrentTime()
            }
        }
    }

    /**
     * 停止计时器刻度
     */
    private fun stopTimerTicking() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * 更新当前时间显示（仅更新计时器时间）
     */
    private fun updateCurrentTime() {
        val currentNanos = getCurrentElapsedTime()

        _uiState.update {
            it.copy(
                currentTime = TimeFormatter.formatElapsed(currentNanos),
                currentTimeNanos = currentNanos
            )
        }
    }

    /**
     * 启动墙上时钟刻度（始终运行，独立于秒表状态）
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
     * 加载保存的状态
     */
    private fun loadSavedState() {
        viewModelScope.launch {
            // 加载状态
            val savedStatus = dataStoreManager.stopwatchStatusFlow.first()
            val savedTime = dataStoreManager.stopwatchTimeFlow.first()
            val savedRecords = dataStoreManager.stopwatchRecordsFlow.first()

            // 处理不同状态的恢复
            when (savedStatus) {
                is StopwatchStatus.Idle -> {
                    // 保持默认状态
                }
                is StopwatchStatus.Running, is StopwatchStatus.Paused, is StopwatchStatus.Stopped -> {
                    // 从保存的数据中恢复经过的时间
                    val savedElapsedNanos = savedTime.pauseTimestamp

                    // 数据验证：检测异常数据（负数或过大的值）
                    // 如果数据异常，直接清除并重置为初始状态
                    val maxReasonableNanos = 365L * 24 * 60 * 60 * 1_000_000_000L // 1 年
                    if (savedElapsedNanos < 0 || savedElapsedNanos > maxReasonableNanos) {
                        // 数据异常，清除并重置
                        reset()
                        return@launch
                    }

                    // 重新初始化时间基准
                    // getCurrentElapsedTime() = System.nanoTime() - startTimeNanos - totalPausedTimeNanos
                    // 我们希望恢复后 getCurrentElapsedTime() = savedElapsedNanos
                    // 所以: savedElapsedNanos = System.nanoTime() - startTimeNanos - totalPausedTimeNanos
                    // 设 totalPausedTimeNanos = 0，则: startTimeNanos = System.nanoTime() - savedElapsedNanos
                    val now = System.nanoTime()
                    startTimeNanos = now - savedElapsedNanos
                    totalPausedTimeNanos = 0L
                    pausedTimeNanos = now

                    // Running 状态需要特殊处理：
                    // 如果应用被杀死或重启，Running 状态会变为 Paused
                    val newStatus = if (savedStatus is StopwatchStatus.Running) {
                        StopwatchStatus.Paused
                    } else {
                        savedStatus
                    }

                    _uiState.update {
                        it.copy(
                            status = newStatus,
                            records = savedRecords
                        )
                    }
                    updateCurrentTime()
                }
            }
        }
    }

    /**
     * 生成分享文本
     */
    fun generateShareText(): String {
        val currentState = _uiState.value
        return ShareHelper.generateStopwatchShareText(
            records = currentState.records,
            totalElapsedNanos = currentState.currentTimeNanos
        )
    }

    /**
     * 保存当前状态
     */
    private fun saveCurrentState() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // 保存状态
            dataStoreManager.saveStopwatchStatus(currentState.status)

            // 保存记录
            dataStoreManager.saveStopwatchRecords(currentState.records)

            // 保存经过的时间（不保存 System.nanoTime() 的绝对值）
            // pauseTimestamp 字段用于存储经过的时间（纳秒）
            val elapsedNanos = when (currentState.status) {
                is StopwatchStatus.Idle -> 0L
                else -> currentState.currentTimeNanos
            }
            dataStoreManager.saveStopwatchTime(
                startTimeNanos = 0L,  // 不再使用
                pausedTimeNanos = 0L,  // 不再使用
                pauseTimestamp = elapsedNanos  // 保存经过的时间
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 保存状态
        saveCurrentState()
        stopTimerTicking()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
