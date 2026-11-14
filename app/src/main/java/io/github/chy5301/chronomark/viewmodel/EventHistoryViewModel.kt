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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    // 上一次自动定位到的「逻辑今天」，用于判断是否已跨过逻辑日期
    private var lastLogicalToday: LocalDate? = null

    init {
        // 初始化：定位到逻辑上的今天（与事件页一致）后开始订阅会话；记录随选中会话自动订阅
        jumpToLogicalTodayIfChanged()
        observeRecordsOfSelectedSession()
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
            when (lastLogicalToday) {
                null -> {
                    // 首次定位：确定日期后才开始订阅会话，避免先按日历日期查询一次
                    lastLogicalToday = logicalToday
                    _uiState.update { it.copy(selectedDate = logicalToday, isLoading = true) }
                    observeSessionsOfSelectedDate()
                }
                logicalToday -> Unit
                else -> {
                    lastLogicalToday = logicalToday
                    selectDate(logicalToday)
                }
            }
        }
    }

    /**
     * 切换到上一天
     */
    fun goToPreviousDay() {
        selectDate(_uiState.value.selectedDate.minusDays(1))
    }

    /**
     * 切换到下一天
     */
    fun goToNextDay() {
        selectDate(_uiState.value.selectedDate.plusDays(1))
    }

    /**
     * 选择指定日期
     */
    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        // 清空旧日期的会话，避免新日期数据到达前仍按旧会话订阅记录
        _uiState.update { it.copy(selectedDate = date, sessions = emptyList(), isLoading = true) }
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
     * 订阅选中日期的会话列表；日期变化时 flatMapLatest 自动取消旧日期的订阅
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSessionsOfSelectedDate() {
        viewModelScope.launch {
            _uiState.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date -> historyRepository.getSessionsByDate(date.toString(), SessionType.EVENT) }
                .collect { sessions ->
                    _uiState.update { it.copy(sessions = sessions, isLoading = false) }
                }
        }
    }

    /**
     * 订阅当天会话的记录（事件模式一天只有一个会话）；只在会话 id 变化时重新订阅
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecordsOfSelectedSession() {
        viewModelScope.launch {
            _uiState.map { it.sessions.firstOrNull()?.id }
                .distinctUntilChanged()
                .flatMapLatest { sessionId ->
                    if (sessionId == null) flowOf(emptyList())
                    else historyRepository.getRecordsBySessionId(sessionId)
                }
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
