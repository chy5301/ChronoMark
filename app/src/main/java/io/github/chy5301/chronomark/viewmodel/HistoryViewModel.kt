package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.AppMode
import io.github.chy5301.chronomark.data.model.HistoryUiState
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.util.ShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 历史记录 ViewModel
 */
class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"

        /**
         * UI 层 AppMode → 数据层 SessionType 转换
         */
        private fun AppMode.toSessionType(): SessionType {
            return when (this) {
                AppMode.EVENT -> SessionType.EVENT
                AppMode.STOPWATCH -> SessionType.STOPWATCH
            }
        }
    }

    // UI 状态
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // 初始化：加载今天的会话列表和有记录的日期
        loadSessionsForDate(_uiState.value.selectedDate)
        loadDatesWithRecords()
    }

    /**
     * 切换模式（事件/秒表）
     */
    fun switchMode(mode: AppMode) {
        _uiState.update { it.copy(currentMode = mode, currentSessionIndex = 0) }
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
     * 切换到上一个会话（秒表模式）
     */
    fun goToPreviousSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex - 1 + sessions.size) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
        loadRecordsForCurrentSession()
    }

    /**
     * 切换到下一个会话（秒表模式）
     */
    fun goToNextSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex + 1) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
        loadRecordsForCurrentSession()
    }

    /**
     * 选择指定会话（秒表模式）
     */
    fun selectSession(index: Int) {
        if (index < 0 || index >= _uiState.value.sessions.size) return

        _uiState.update { it.copy(currentSessionIndex = index) }
        loadRecordsForCurrentSession()
    }

    /**
     * 删除当天所有会话（事件模式）
     */
    fun deleteAllSessionsForCurrentDate() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate.toString()
            val mode = _uiState.value.currentMode.toSessionType()

            historyRepository.deleteSessionsByDateAndType(date, mode)
                .onSuccess {
                    Log.i(TAG, "Deleted all sessions for $date")
                    loadSessionsForDate(_uiState.value.selectedDate)
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to delete sessions", e)
                }
        }
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
                    // 重新加载会话列表
                    loadSessionsForDate(_uiState.value.selectedDate)
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
                    // 重新加载会话列表
                    loadSessionsForDate(_uiState.value.selectedDate)
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
                    // 重新加载记录列表
                    loadRecordsForCurrentSession()
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
                    // 重新加载记录列表
                    loadRecordsForCurrentSession()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to update record note", e)
                }
        }
    }

    /**
     * 生成分享文本
     * 事件模式：分享该天的所有事件记录
     * 秒表模式：分享当前选中的会话
     */
    fun generateShareText(): String {
        val currentState = _uiState.value
        val sessions = currentState.sessions

        if (sessions.isEmpty()) {
            return when (currentState.currentMode) {
                AppMode.EVENT -> "ChronoMark 事件记录\n\n暂无记录"
                AppMode.STOPWATCH -> "ChronoMark 秒表记录\n\n暂无记录"
            }
        }

        return when (currentState.currentMode) {
            AppMode.EVENT -> {
                // 事件模式：分享该天的所有记录（事件模式一天只有一个会话）
                val session = sessions.first()
                ShareHelper.generateHistoryShareText(session, currentState.selectedSessionRecords)
            }
            AppMode.STOPWATCH -> {
                // 秒表模式：分享当前选中的会话
                val currentSession = sessions[currentState.currentSessionIndex]
                ShareHelper.generateHistoryShareText(currentSession, currentState.selectedSessionRecords)
            }
        }
    }

    /**
     * 加载指定日期的会话列表
     */
    private fun loadSessionsForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val dateString = date.toString()
            val mode = _uiState.value.currentMode.toSessionType()

            // 使用 Flow 收集会话列表
            historyRepository.getSessionsByDate(dateString, mode)
                .collect { sessions ->
                    _uiState.update {
                        it.copy(
                            sessions = sessions,
                            isLoading = false,
                            currentSessionIndex = 0
                        )
                    }

                    // 如果有会话，加载第一个会话的记录
                    if (sessions.isNotEmpty()) {
                        loadRecordsForCurrentSession()
                    } else {
                        _uiState.update { it.copy(selectedSessionRecords = emptyList()) }
                    }
                }
        }
    }

    /**
     * 加载当前会话的记录列表
     */
    private fun loadRecordsForCurrentSession() {
        viewModelScope.launch {
            val sessions = _uiState.value.sessions
            if (sessions.isEmpty()) {
                _uiState.update { it.copy(selectedSessionRecords = emptyList()) }
                return@launch
            }

            val currentSession = sessions[_uiState.value.currentSessionIndex]

            // 使用 Flow 收集记录列表
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
            val mode = _uiState.value.currentMode.toSessionType()

            // 使用 Flow 收集有记录的日期
            historyRepository.getDatesWithRecords(mode)
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
