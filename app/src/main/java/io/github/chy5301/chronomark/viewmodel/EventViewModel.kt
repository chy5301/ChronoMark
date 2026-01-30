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
 *
 * 实现实时同步：所有记录直接存储到 Room 数据库，
 * 界面只显示今天逻辑日期的记录。
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
        // 迁移遗留数据并加载今天的记录
        migrateAndLoadRecords()
    }

    /**
     * 迁移 DataStore 遗留数据并加载今天的记录
     */
    private fun migrateAndLoadRecords() {
        viewModelScope.launch {
            // 1. 检查并迁移 DataStore 中的遗留数据
            val legacyRecords = dataStoreManager.eventRecordsFlow.first()
            if (legacyRecords.isNotEmpty()) {
                Log.i(TAG, "Migrating ${legacyRecords.size} legacy records from DataStore")
                migrateRecordsToRoom(legacyRecords)
                dataStoreManager.clearEventData()
                    .onFailure { e ->
                        Log.e(TAG, "Failed to clear legacy data from DataStore", e)
                    }
            }

            // 2. 加载今天的记录
            loadTodayRecords()
        }
    }

    /**
     * 将记录迁移到 Room（按逻辑日期分组）
     */
    private suspend fun migrateRecordsToRoom(records: List<TimeRecord>) {
        val boundaryHour = dataStoreManager.archiveBoundaryHourFlow.first()
        val boundaryMinute = dataStoreManager.archiveBoundaryMinuteFlow.first()
        val boundaryTime = ArchiveUtils.createBoundaryTime(boundaryHour, boundaryMinute)

        // 按逻辑日期分组
        val grouped = records.groupBy { record ->
            ArchiveUtils.getLogicalDate(record.wallClockTime, boundaryTime).toString()
        }

        // 逐日期存入 Room
        grouped.forEach { (date, dateRecords) ->
            historyRepository.insertEventRecords(date, dateRecords)
                .onSuccess {
                    Log.i(TAG, "Migrated ${dateRecords.size} records for $date")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to migrate records for $date", e)
                }
        }
    }

    /**
     * 加载今天的记录
     */
    private suspend fun loadTodayRecords() {
        val todayDate = getTodayLogicalDate()
        val records = historyRepository.getEventRecordsByDate(todayDate)
        _uiState.update {
            it.copy(records = records)
        }
        Log.d(TAG, "Loaded ${records.size} records for today ($todayDate)")
    }

    /**
     * 记录事件（直接写入 Room）
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

        // 更新 UI
        _uiState.update {
            it.copy(records = it.records + listOf(newRecord))
        }

        // 写入 Room
        viewModelScope.launch {
            val todayDate = getTodayLogicalDate()
            historyRepository.insertEventRecord(todayDate, newRecord)
                .onFailure { e ->
                    Log.e(TAG, "Failed to save record to database", e)
                }
        }
    }

    /**
     * 重置今天的记录
     */
    fun reset() {
        _uiState.update {
            EventUiState(
                wallClockTime = TimeFormatter.formatWallClockWithDate(System.currentTimeMillis()),
                records = emptyList()
            )
        }

        // 删除今天在 Room 中的记录
        viewModelScope.launch {
            val todayDate = getTodayLogicalDate()
            historyRepository.deleteEventRecordsByDate(todayDate)
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete today's records from database", e)
                }
        }
    }

    /**
     * 更新记录的备注（直接更新 Room）
     */
    fun updateRecordNote(recordId: String, note: String) {
        _uiState.update { state ->
            state.copy(
                records = state.records.map { record ->
                    if (record.id == recordId) record.copy(note = note) else record
                }
            )
        }

        // 更新 Room
        viewModelScope.launch {
            historyRepository.updateEventRecordNote(recordId, note)
                .onFailure { e ->
                    Log.e(TAG, "Failed to update note in database", e)
                }
        }
    }

    /**
     * 删除记录（直接从 Room 删除）
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

        // 从 Room 删除
        viewModelScope.launch {
            historyRepository.deleteEventRecord(recordId)
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete record from database", e)
                }
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

    /**
     * 生成分享文本
     */
    fun generateShareText(): String {
        return ShareHelper.generateEventShareText(_uiState.value.records)
    }

    /**
     * 获取今天的逻辑日期
     */
    private suspend fun getTodayLogicalDate(): String {
        val boundaryHour = dataStoreManager.archiveBoundaryHourFlow.first()
        val boundaryMinute = dataStoreManager.archiveBoundaryMinuteFlow.first()
        val boundaryTime = ArchiveUtils.createBoundaryTime(boundaryHour, boundaryMinute)
        return ArchiveUtils.getLogicalDate(System.currentTimeMillis(), boundaryTime).toString()
    }

    override fun onCleared() {
        super.onCleared()
        wallClockJob?.cancel()
        wallClockJob = null
    }
}
