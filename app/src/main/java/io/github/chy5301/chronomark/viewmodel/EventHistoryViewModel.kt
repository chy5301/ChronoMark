package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.EventHistoryUiState
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.util.ArchiveUtils
import io.github.chy5301.chronomark.util.ShareHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 事件模式历史记录 ViewModel
 *
 * 负责管理事件模式的历史记录 UI 状态和业务逻辑。
 * 事件模式特点：一天一个会话（自动归档），无需会话选择器。
 */
class EventHistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val dataStoreManager: DataStoreManager,
    private val wallClockMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {

    companion object {
        private const val TAG = "EventHistoryViewModel"
    }

    // UI 状态
    private val _uiState = MutableStateFlow(EventHistoryUiState())
    val uiState: StateFlow<EventHistoryUiState> = _uiState.asStateFlow()

    // 当前日期的会话订阅与记录订阅；切换日期时必须取消旧订阅，否则旧查询会在数据库变化时覆盖界面
    private var sessionsJob: Job? = null
    private var recordsJob: Job? = null

    // 上一次自动定位到的「逻辑今天」，用于判断是否已跨过逻辑日期
    private var lastLogicalToday: LocalDate? = null

    init {
        // 初始化：定位到逻辑上的今天（与事件页一致），并加载有记录的日期
        jumpToLogicalTodayIfChanged()
        loadDatesWithRecords()
    }

    /**
     * 历史页每次显示时调用
     *
     * ViewModel 的生命周期跟随 Activity，应用长时间不退出时，逻辑日期可能已变化；
     * 此时自动跳到新的一天。同一逻辑日内不改动，保留用户自己选择的日期。
     */
    fun onScreenShown() {
        jumpToLogicalTodayIfChanged()
    }

    private fun jumpToLogicalTodayIfChanged() {
        viewModelScope.launch {
            val boundaryTime = ArchiveUtils.createBoundaryTime(
                dataStoreManager.archiveBoundaryHourFlow.first(),
                dataStoreManager.archiveBoundaryMinuteFlow.first()
            )
            // 事件记录按逻辑日期归档（分界点前属于前一天），默认日期必须用同一规则，
            // 否则凌晨打开历史页会落在日历上的今天，看不到刚记的记录
            val logicalToday = ArchiveUtils.getLogicalDate(wallClockMillis(), boundaryTime)
            if (logicalToday != lastLogicalToday) {
                lastLogicalToday = logicalToday
                selectDate(logicalToday)
            }
        }
    }

    /**
     * 切换到上一天
     */
    fun goToPreviousDay() {
        val previousDate = _uiState.value.selectedDate.minusDays(1)
        _uiState.update { it.copy(selectedDate = previousDate) }
        loadSessionsForDate(previousDate)
    }

    /**
     * 切换到下一天
     */
    fun goToNextDay() {
        val nextDate = _uiState.value.selectedDate.plusDays(1)
        _uiState.update { it.copy(selectedDate = nextDate) }
        loadSessionsForDate(nextDate)
    }

    /**
     * 选择指定日期
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadSessionsForDate(date)
    }

    /**
     * 删除当天所有记录（事件模式）
     */
    fun deleteAllRecordsForCurrentDate() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate.toString()

            historyRepository.deleteSessionsByDateAndType(date, SessionType.EVENT)
                .onSuccess {
                    Log.i(TAG, "Deleted all event sessions for $date")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete event sessions", e)
                }
        }
    }

    /**
     * 删除单条记录
     */
    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            historyRepository.deleteRecord(recordId)
                .onSuccess {
                    Log.i(TAG, "Deleted record $recordId")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete record", e)
                }
        }
    }

    /**
     * 更新记录备注
     */
    fun updateRecordNote(recordId: String, newNote: String) {
        viewModelScope.launch {
            historyRepository.updateRecordNote(recordId, newNote)
                .onSuccess {
                    Log.i(TAG, "Updated record note")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to update record note", e)
                }
        }
    }

    /**
     * 生成分享文本（事件模式）
     * 分享当天的所有事件记录
     */
    fun generateShareText(): String {
        val currentState = _uiState.value
        val sessions = currentState.sessions

        if (sessions.isEmpty()) {
            return "ChronoMark 事件记录\n\n暂无记录"
        }

        // 事件模式一天只有一个会话
        val session = sessions.first()
        return ShareHelper.generateHistoryShareText(session, currentState.selectedSessionRecords)
    }

    /**
     * 加载指定日期的会话列表（事件模式）
     */
    private fun loadSessionsForDate(date: LocalDate) {
        sessionsJob?.cancel()
        recordsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 订阅会话列表，数据库变化时自动更新
            historyRepository.getSessionsByDate(date.toString(), SessionType.EVENT)
                .collect { sessions ->
                    _uiState.update { it.copy(sessions = sessions, isLoading = false) }
                    loadRecordsForCurrentSession()
                }
        }
    }

    /**
     * 订阅当前会话的记录列表（事件模式一天只有一个会话）
     */
    private fun loadRecordsForCurrentSession() {
        recordsJob?.cancel()
        val currentSession = _uiState.value.sessions.firstOrNull()
        if (currentSession == null) {
            _uiState.update { it.copy(selectedSessionRecords = emptyList()) }
            return
        }

        recordsJob = viewModelScope.launch {
            historyRepository.getRecordsBySessionId(currentSession.id)
                .collect { records ->
                    _uiState.update { it.copy(selectedSessionRecords = records) }
                }
        }
    }

    /**
     * 加载有记录的日期列表（用于日历标记）
     */
    private fun loadDatesWithRecords() {
        viewModelScope.launch {
            // 使用 Flow 收集有记录的日期（事件模式）
            historyRepository.getDatesWithRecords(SessionType.EVENT)
                .collect { dateStrings ->
                    // 将字符串日期转换为 LocalDate 集合
                    val dates = dateStrings.mapNotNull { dateString ->
                        try {
                            LocalDate.parse(dateString)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse date: $dateString", e)
                            null
                        }
                    }.toSet()

                    _uiState.update { it.copy(datesWithRecords = dates) }
                }
        }
    }
}
