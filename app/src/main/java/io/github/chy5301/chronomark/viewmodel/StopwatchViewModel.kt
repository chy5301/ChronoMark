package io.github.chy5301.chronomark.viewmodel

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.StopwatchTiming
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
    private val dataStoreManager: DataStoreManager,
    private val historyRepository: HistoryRepository,
    private val realtimeNanos: () -> Long = SystemClock::elapsedRealtimeNanos,
    private val wallClockMillis: () -> Long = System::currentTimeMillis
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

    // 计时基准：运行中已计时长 = accumulatedNanos + (realtimeNanos() - anchorRealtimeNanos)
    private var timing = StopwatchTiming()

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

        val nowWall = wallClockMillis()
        timing = StopwatchTiming(
            accumulatedNanos = 0L,
            anchorRealtimeNanos = realtimeNanos(),
            anchorWallMillis = nowWall,
            startWallMillis = nowWall
        )
        _uiState.update { it.copy(status = StopwatchStatus.Running) }
        startTimerTicking()
        saveCurrentState()
    }

    /**
     * 暂停计时
     */
    fun pause() {
        if (_uiState.value.status != StopwatchStatus.Running) return

        timing = timing.copy(accumulatedNanos = getCurrentElapsedTime())
        _uiState.update { it.copy(status = StopwatchStatus.Paused) }
        stopTimerTicking()
        updateCurrentTime()
        saveCurrentState()
    }

    /**
     * 继续计时
     */
    fun resume() {
        if (_uiState.value.status != StopwatchStatus.Paused) return

        timing = timing.copy(
            anchorRealtimeNanos = realtimeNanos(),
            anchorWallMillis = wallClockMillis()
        )
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
        timing = StopwatchTiming()
        _uiState.update {
            StopwatchUiState(
                status = StopwatchStatus.Idle,
                currentTime = "00:00.000",
                wallClockTime = TimeFormatter.formatWallClockWithDate(wallClockMillis()),
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
        val currentWallClockTime = wallClockMillis()
        val lastNanos = _uiState.value.records.firstOrNull()?.elapsedTimeNanos ?: 0L
        // 确保时间差非负，防止数据损坏或异常导致的负数
        val splitNanos = (currentNanos - lastNanos).coerceAtLeast(0L)

        val newRecord = TimeRecord(
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
            state.copy(records = state.records.filter { it.id != recordId })
        }
        saveCurrentState()
    }

    /**
     * 获取当前经过的时间（纳秒），直接由计时基准算出，不依赖界面刷新
     */
    private fun getCurrentElapsedTime(): Long {
        if (_uiState.value.status != StopwatchStatus.Running) return timing.accumulatedNanos
        val sinceAnchor = (realtimeNanos() - timing.anchorRealtimeNanos).coerceAtLeast(0L)
        return timing.accumulatedNanos + sinceAnchor
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
        val currentWallClockTime = wallClockMillis()
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
            val savedTiming = dataStoreManager.stopwatchTimingFlow.first()
            val savedRecords = dataStoreManager.stopwatchRecordsFlow.first()

            if (savedStatus is StopwatchStatus.Idle) return@launch

            // 数据验证：已计时长为负或超过 1 年视为数据损坏，清除并重置
            val maxReasonableNanos = 365L * 24 * 60 * 60 * 1_000_000_000L
            if (savedTiming.accumulatedNanos !in 0..maxReasonableNanos) {
                reset()
                return@launch
            }

            if (savedStatus is StopwatchStatus.Running && savedTiming.hasAnchor) {
                // 进程在计时中被杀死：补上停止运行期间经过的时间，并以当前时刻为新锚点继续计时
                val nowRealtime = realtimeNanos()
                val nowWall = wallClockMillis()
                timing = savedTiming.copy(
                    accumulatedNanos = savedTiming.accumulatedNanos +
                        savedTiming.elapsedSinceAnchorNanos(nowRealtime, nowWall),
                    anchorRealtimeNanos = nowRealtime,
                    anchorWallMillis = nowWall
                )
                _uiState.update { it.copy(status = StopwatchStatus.Running, records = savedRecords) }
                updateCurrentTime()
                startTimerTicking()
                saveCurrentState()
            } else {
                // 旧版数据没有锚点，无法得知计时中断的时刻，运行中的状态降级为暂停
                timing = savedTiming
                val newStatus = if (savedStatus is StopwatchStatus.Running) StopwatchStatus.Paused else savedStatus
                _uiState.update { it.copy(status = newStatus, records = savedRecords) }
                updateCurrentTime()
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
            totalElapsedNanos = getCurrentElapsedTime()
        )
    }

    /**
     * 保存当前状态
     *
     * 计时基准足以还原运行中的时长，因此只需在状态变化时保存，计时过程中无需定时写入。
     * 在调用时同步取快照，避免协程延后执行时读到之后的状态。
     */
    private fun saveCurrentState() {
        val status = _uiState.value.status
        val records = _uiState.value.records
        val timingSnapshot = timing
        viewModelScope.launch {
            dataStoreManager.saveStopwatchSnapshot(status, timingSnapshot, records)
                .onFailure { e -> Log.e(TAG, "Failed to save stopwatch state", e) }
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
                // 记录按新→旧排列；旧版数据没有会话开始时间时，退而取最早一条标记的时间
                startTime = timing.startWallMillis.takeIf { it > 0L } ?: records.last().wallClockTime,
                totalElapsedNanos = getCurrentElapsedTime()
            )
                .onSuccess {
                    Log.i(TAG, "Saved to history successfully")

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
        // 此时 viewModelScope 已被取消，无法再保存；各操作发生时已即时保存，运行中的时长可由计时基准还原
        stopTimerTicking()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
