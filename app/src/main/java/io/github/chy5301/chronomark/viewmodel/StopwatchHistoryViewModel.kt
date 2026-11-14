package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.data.model.StopwatchHistoryUiState
import io.github.chy5301.chronomark.util.ShareHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 秒表模式历史记录 ViewModel
 *
 * 负责管理秒表模式的历史记录 UI 状态和业务逻辑。
 * 秒表模式特点：一天可能有多个会话（手动保存），需要会话选择器。
 */
class StopwatchHistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StopwatchHistoryViewModel"
    }

    // UI 状态
    private val _uiState = MutableStateFlow(StopwatchHistoryUiState())
    val uiState: StateFlow<StopwatchHistoryUiState> = _uiState.asStateFlow()

    // 当前日期的会话订阅与当前会话的记录订阅；切换时必须取消旧订阅，否则旧查询会在数据库变化时覆盖界面
    private var sessionsJob: Job? = null
    private var recordsJob: Job? = null

    init {
        // 初始化：加载今天的会话列表和有记录的日期
        loadSessionsForDate(_uiState.value.selectedDate)
        loadDatesWithRecords()
    }

    /**
     * 切换到上一天
     */
    fun goToPreviousDay() {
        val previousDate = _uiState.value.selectedDate.minusDays(1)
        _uiState.update { it.copy(selectedDate = previousDate, currentSessionIndex = 0) }
        loadSessionsForDate(previousDate)
    }

    /**
     * 切换到下一天
     */
    fun goToNextDay() {
        val nextDate = _uiState.value.selectedDate.plusDays(1)
        _uiState.update { it.copy(selectedDate = nextDate, currentSessionIndex = 0) }
        loadSessionsForDate(nextDate)
    }

    /**
     * 选择指定日期
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, currentSessionIndex = 0) }
        loadSessionsForDate(date)
    }

    /**
     * 切换到上一个会话
     */
    fun goToPreviousSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex - 1 + sessions.size) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
        loadRecordsForCurrentSession()
    }

    /**
     * 切换到下一个会话
     */
    fun goToNextSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex + 1) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
        loadRecordsForCurrentSession()
    }

    /**
     * 选择指定会话
     */
    fun selectSession(index: Int) {
        if (index < 0 || index >= _uiState.value.sessions.size) return

        _uiState.update { it.copy(currentSessionIndex = index) }
        loadRecordsForCurrentSession()
    }

    /**
     * 删除当前会话（秒表模式）
     */
    fun deleteCurrentSession() {
        viewModelScope.launch {
            val sessions = _uiState.value.sessions
            if (sessions.isEmpty()) return@launch

            val currentSession = sessions[_uiState.value.currentSessionIndex]

            historyRepository.deleteSession(currentSession.id)
                .onSuccess {
                    Log.i(TAG, "Deleted session ${currentSession.id}")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete session", e)
                }
        }
    }

    /**
     * 更新当前会话标题（秒表模式）
     */
    fun updateCurrentSessionTitle(newTitle: String) {
        viewModelScope.launch {
            val sessions = _uiState.value.sessions
            if (sessions.isEmpty()) return@launch

            val currentSession = sessions[_uiState.value.currentSessionIndex]

            historyRepository.updateSessionTitle(currentSession.id, newTitle)
                .onSuccess {
                    Log.i(TAG, "Updated session title to: $newTitle")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to update session title", e)
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
     * 生成分享文本（秒表模式）
     * 分享当前选中的会话
     */
    fun generateShareText(): String {
        val currentState = _uiState.value
        val sessions = currentState.sessions

        if (sessions.isEmpty()) {
            return "ChronoMark 秒表记录\n\n暂无记录"
        }

        // 秒表模式：分享当前选中的会话
        val currentSession = sessions[currentState.currentSessionIndex]
        return ShareHelper.generateHistoryShareText(
            currentSession,
            currentState.selectedSessionRecords
        )
    }

    /**
     * 加载指定日期的会话列表（秒表模式）
     */
    private fun loadSessionsForDate(date: LocalDate) {
        sessionsJob?.cancel()
        recordsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 订阅会话列表，数据库变化（删除、改名、新归档）时自动更新
            historyRepository.getSessionsByDate(date.toString(), SessionType.STOPWATCH)
                .collect { sessions ->
                    _uiState.update { state ->
                        // 按 id 保持原选中的会话；它已被删除时退回到相邻的合法位置
                        val selectedId = state.sessions.getOrNull(state.currentSessionIndex)?.id
                        val index = sessions.indexOfFirst { it.id == selectedId }
                            .takeIf { it >= 0 }
                            ?: state.currentSessionIndex.coerceIn(0, maxOf(sessions.lastIndex, 0))
                        state.copy(sessions = sessions, isLoading = false, currentSessionIndex = index)
                    }
                    loadRecordsForCurrentSession()
                }
        }
    }

    /**
     * 订阅当前会话的记录列表
     */
    private fun loadRecordsForCurrentSession() {
        recordsJob?.cancel()
        val state = _uiState.value
        val currentSession = state.sessions.getOrNull(state.currentSessionIndex)
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
            // 使用 Flow 收集有记录的日期（秒表模式）
            historyRepository.getDatesWithRecords(SessionType.STOPWATCH)
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
