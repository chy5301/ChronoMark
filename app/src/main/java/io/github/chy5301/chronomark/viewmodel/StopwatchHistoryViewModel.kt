package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.github.chy5301.chronomark.data.model.StopwatchHistoryUiState
import io.github.chy5301.chronomark.util.ShareHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    init {
        // 会话与记录均由 UI 状态（选中日期 / 选中会话）驱动订阅，切换时自动取消旧订阅
        observeSessionsOfSelectedDate()
        observeRecordsOfSelectedSession()
        loadDatesWithRecords()
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
        _uiState.update {
            it.copy(selectedDate = date, sessions = emptyList(), currentSessionIndex = 0, isLoading = true)
        }
    }

    /**
     * 切换到上一个会话
     */
    fun goToPreviousSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex - 1 + sessions.size) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
    }

    /**
     * 切换到下一个会话
     */
    fun goToNextSession() {
        val sessions = _uiState.value.sessions
        if (sessions.isEmpty()) return

        val newIndex = (_uiState.value.currentSessionIndex + 1) % sessions.size
        _uiState.update { it.copy(currentSessionIndex = newIndex) }
    }

    /**
     * 选择指定会话
     */
    fun selectSession(index: Int) {
        if (index < 0 || index >= _uiState.value.sessions.size) return

        _uiState.update { it.copy(currentSessionIndex = index) }
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
     * 订阅选中日期的会话列表；日期变化时 flatMapLatest 自动取消旧日期的订阅
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSessionsOfSelectedDate() {
        viewModelScope.launch {
            _uiState.map { it.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date ->
                    historyRepository.getSessionsByDate(date.toString(), SessionType.STOPWATCH)
                }
                .collect { sessions ->
                    _uiState.update { state ->
                        // 按 id 保持原选中的会话；它已被删除时退回到相邻的合法位置
                        val selectedId = state.sessions.getOrNull(state.currentSessionIndex)?.id
                        val index = sessions.indexOfFirst { it.id == selectedId }
                            .takeIf { it >= 0 }
                            ?: state.currentSessionIndex.coerceIn(0, maxOf(sessions.lastIndex, 0))
                        state.copy(sessions = sessions, isLoading = false, currentSessionIndex = index)
                    }
                }
        }
    }

    /**
     * 订阅选中会话的记录；只在选中的会话 id 变化时重新订阅，
     * 会话列表因改名、新归档等刷新时不会重复查询
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecordsOfSelectedSession() {
        viewModelScope.launch {
            _uiState.map { it.sessions.getOrNull(it.currentSessionIndex)?.id }
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
