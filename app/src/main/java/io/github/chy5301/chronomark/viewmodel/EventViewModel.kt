package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.EventUiState
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.github.chy5301.chronomark.util.ArchiveUtils
import io.github.chy5301.chronomark.util.ShareHelper
import io.github.chy5301.chronomark.util.TimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 事件模式 ViewModel
 *
 * 实现实时同步：所有记录直接存储到 Room 数据库，界面订阅「今天逻辑日期」的记录。
 * 数据库是唯一数据源：在历史页删除/编辑、在设置页清空历史、跨过逻辑日期分界点，
 * 界面都会自动随之更新。
 */
class EventViewModel(
    private val dataStoreManager: DataStoreManager,
    private val historyRepository: HistoryRepository,
    private val wallClockMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {

    companion object {
        private const val TAG = "EventViewModel"
    }

    // UI 状态
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // 墙上时钟协程（始终运行）
    private var wallClockJob: Job? = null

    // 当前墙上时间，由墙上时钟每秒刷新，用于检测跨过逻辑日期分界点
    private val nowMillis = MutableStateFlow(wallClockMillis())

    // 串行化记录写入，保证连续快速点击时每条记录的时间差都基于上一条已落库的记录
    private val recordMutex = Mutex()

    init {
        // 启动墙上时钟更新（始终运行）
        startWallClockTicking()
        // 迁移遗留数据后订阅今天的记录
        viewModelScope.launch {
            migrateLegacyRecords()
            observeTodayRecords()
        }
    }

    /**
     * 迁移 DataStore 遗留数据到 Room（按逻辑日期分组）
     * 只有所有日期都写入成功才清空 DataStore，否则保留原数据，下次启动重试
     */
    private suspend fun migrateLegacyRecords() {
        val legacyRecords = dataStoreManager.eventRecordsFlow.first()
        if (legacyRecords.isEmpty()) return

        Log.i(TAG, "Migrating ${legacyRecords.size} legacy records from DataStore")
        val boundaryTime = currentBoundaryTime()
        val grouped = legacyRecords.groupBy { record ->
            ArchiveUtils.getLogicalDate(record.wallClockTime, boundaryTime).toString()
        }

        var allSucceeded = true
        grouped.forEach { (date, dateRecords) ->
            historyRepository.insertEventRecords(date, dateRecords)
                .onSuccess { Log.i(TAG, "Migrated ${dateRecords.size} records for $date") }
                .onFailure { e ->
                    allSucceeded = false
                    Log.e(TAG, "Failed to migrate records for $date", e)
                }
        }

        if (allSucceeded) {
            dataStoreManager.clearEventData()
                .onFailure { e -> Log.e(TAG, "Failed to clear legacy data from DataStore", e) }
        } else {
            Log.e(TAG, "Migration incomplete, keeping legacy data in DataStore for retry")
        }
    }

    /**
     * 订阅今天逻辑日期的记录；逻辑日期变化（跨过分界点或修改分界点设置）时自动切换
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeTodayRecords() {
        combine(
            dataStoreManager.archiveBoundaryHourFlow,
            dataStoreManager.archiveBoundaryMinuteFlow,
            nowMillis
        ) { hour, minute, now ->
            ArchiveUtils.getLogicalDate(now, ArchiveUtils.createBoundaryTime(hour, minute)).toString()
        }
            .distinctUntilChanged()
            .flatMapLatest { date -> historyRepository.observeEventRecordsByDate(date) }
            .collect { records ->
                _uiState.update { it.copy(records = records) }
            }
    }

    /**
     * 记录事件（直接写入 Room，界面通过订阅自动更新）
     */
    fun recordEvent() {
        val currentWallClockTime = wallClockMillis()

        viewModelScope.launch {
            recordMutex.withLock {
                val date = logicalDateOf(currentWallClockTime)
                // 以数据库中当天最后一条记录为基准计算时间差，而不是可能尚未刷新的界面列表
                val lastTimestamp = historyRepository.getEventRecordsByDate(date)
                    .lastOrNull()?.wallClockTime ?: currentWallClockTime
                // 确保时间差非负，防止系统时间被调整到过去导致的负数
                val splitNanos = (currentWallClockTime - lastTimestamp).coerceAtLeast(0L) * 1_000_000

                val newRecord = TimeRecord(
                    wallClockTime = currentWallClockTime,
                    elapsedTimeNanos = 0L,  // 事件模式不需要累计时间
                    splitTimeNanos = splitNanos,
                    note = ""
                )
                historyRepository.insertEventRecord(date, newRecord)
                    .onFailure { e -> Log.e(TAG, "Failed to save record to database", e) }
            }
        }
    }

    /**
     * 重置今天的记录
     */
    fun reset() {
        viewModelScope.launch {
            val todayDate = logicalDateOf(wallClockMillis())
            historyRepository.deleteEventRecordsByDate(todayDate)
                .onFailure { e -> Log.e(TAG, "Failed to delete today's records from database", e) }
        }
    }

    /**
     * 更新记录的备注
     */
    fun updateRecordNote(recordId: String, note: String) {
        viewModelScope.launch {
            historyRepository.updateEventRecordNote(recordId, note)
                .onFailure { e -> Log.e(TAG, "Failed to update note in database", e) }
        }
    }

    /**
     * 删除记录
     */
    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            historyRepository.deleteEventRecord(recordId)
                .onFailure { e -> Log.e(TAG, "Failed to delete record from database", e) }
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
     * 更新墙上时钟显示，并推进用于检测日期切换的当前时间
     */
    private fun updateWallClock() {
        val currentWallClockTime = wallClockMillis()
        nowMillis.value = currentWallClockTime
        _uiState.update {
            it.copy(wallClockTime = TimeFormatter.formatWallClockWithDate(currentWallClockTime))
        }
    }

    /**
     * 生成分享文本
     */
    fun generateShareText(): String {
        return ShareHelper.generateEventShareText(_uiState.value.records)
    }

    /**
     * 读取当前的逻辑日期分界点设置
     */
    private suspend fun currentBoundaryTime() = ArchiveUtils.createBoundaryTime(
        dataStoreManager.archiveBoundaryHourFlow.first(),
        dataStoreManager.archiveBoundaryMinuteFlow.first()
    )

    /**
     * 计算指定时刻所属的逻辑日期
     */
    private suspend fun logicalDateOf(wallClockMillis: Long): String {
        return ArchiveUtils.getLogicalDate(wallClockMillis, currentBoundaryTime()).toString()
    }

    override fun onCleared() {
        super.onCleared()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
