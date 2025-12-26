package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 秒表 ViewModel
 */
class StopwatchViewModel(
    private val dataStoreManager: DataStoreManager,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StopwatchViewModel"
    }

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
                .onFailure { e ->
                    // 记录错误，但不影响 UI 状态重置
                    e.printStackTrace()
                }
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
        // 确保时间差非负，防止数据损坏或异常导致的负数
        val splitNanos = (currentNanos - lastNanos).coerceAtLeast(0L)

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
     * 确保返回值非负，防止系统时间异常导致的负数
     */
    private fun getCurrentElapsedTime(): Long {
        val elapsed = System.nanoTime() - startTimeNanos - totalPausedTimeNanos
        return elapsed.coerceAtLeast(0L)
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
            val savedElapsedNanos = dataStoreManager.stopwatchElapsedTimeFlow.first()
            val savedRecords = dataStoreManager.stopwatchRecordsFlow.first()

            // 处理不同状态的恢复
            when (savedStatus) {
                is StopwatchStatus.Idle -> {
                    // 保持默认状态
                }

                is StopwatchStatus.Running, is StopwatchStatus.Paused, is StopwatchStatus.Stopped -> {
                    // 数据验证：检测异常数据（负数或过大的值）
                    // 如果数据异常，直接清除并重置为初始状态
                    val maxReasonableNanos = 365L * 24 * 60 * 60 * 1_000_000_000L // 1 年
                    if (savedElapsedNanos !in 0..maxReasonableNanos) {
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
                .onFailure { e -> e.printStackTrace() }

            // 保存记录
            dataStoreManager.saveStopwatchRecords(currentState.records)
                .onFailure { e -> e.printStackTrace() }

            // 保存经过的时间（纳秒）
            val elapsedNanos = when (currentState.status) {
                is StopwatchStatus.Idle -> 0L
                else -> currentState.currentTimeNanos
            }
            dataStoreManager.saveStopwatchElapsedTime(elapsedNanos)
                .onFailure { e -> e.printStackTrace() }
        }
    }

    /**
     * 生成默认会话标题（自动编号）
     */
    suspend fun getDefaultTitle(): String {
        // 获取今天的日期
        val today = java.time.LocalDate.now().toString()

        // 查询今天已有的秒表会话数量
        val todaySessions = historyRepository.getSessionsByDate(
            date = today,
            sessionType = io.github.chy5301.chronomark.data.model.SessionType.STOPWATCH
        ).first()

        // 编号 = 今天的会话数 + 1
        val number = todaySessions.size + 1

        return "会话 $number"
    }

    /**
     * 保存到历史记录
     */
    fun saveToHistory(title: String) {
        viewModelScope.launch {
            val records = _uiState.value.records
            if (records.isEmpty()) {
                Log.w(TAG, "No records to save")
                return@launch
            }

            // 归档到 Room 数据库
            historyRepository.archiveStopwatchRecords(
                records = records,
                title = title,
                startTime = records.first().wallClockTime,
                totalElapsedNanos = _uiState.value.currentTimeNanos
            )
                .onSuccess {
                    Log.i(TAG, "Saved to history successfully")

                    // 清空 DataStore 工作区
                    dataStoreManager.clearStopwatchRecords()
                        .onFailure { e -> Log.e(TAG, "Failed to clear records", e) }

                    // 重置状态
                    reset()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to save to history", e)
                }
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
