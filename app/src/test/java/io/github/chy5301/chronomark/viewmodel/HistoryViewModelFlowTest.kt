package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import io.github.chy5301.chronomark.data.database.entity.HistorySessionEntity
import io.github.chy5301.chronomark.data.database.entity.TimeRecordEntity
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.SessionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * 历史页 ViewModel 的数据订阅测试
 *
 * 用 MutableStateFlow 模拟 Room 的 Flow：数据库变化时，所有仍在订阅的查询都会重新推送。
 * 切换日期/会话后，旧查询的推送不应再影响界面状态。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)

    private lateinit var repository: HistoryRepository
    private val sessionFlows = mutableMapOf<Pair<String, SessionType>, MutableStateFlow<List<HistorySessionEntity>>>()
    private val recordFlows = mutableMapOf<String, MutableStateFlow<List<TimeRecordEntity>>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        repository = mockk(relaxed = true)
        every { repository.getSessionsByDate(any(), any()) } answers {
            sessionsOf(firstArg(), secondArg())
        }
        every { repository.getRecordsBySessionId(any()) } answers { recordsOf(firstArg()) }
        every { repository.getDatesWithRecords(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun sessionsOf(date: String, type: SessionType) =
        sessionFlows.getOrPut(date to type) { MutableStateFlow(emptyList()) }

    private fun recordsOf(sessionId: String) =
        recordFlows.getOrPut(sessionId) { MutableStateFlow(emptyList()) }

    private fun session(id: String, date: LocalDate, type: SessionType, title: String = id) =
        HistorySessionEntity(id = id, date = date.toString(), sessionType = type, title = title, createdAt = 0L)

    private fun record(id: String, sessionId: String) = TimeRecordEntity(
        id = id, sessionId = sessionId, wallClockTime = 0L, elapsedTimeNanos = 0L, splitTimeNanos = 0L, note = ""
    )

    // ========== 秒表历史 ==========

    @Test
    fun `stopwatch - after switching date away and back, old date query no longer overwrites sessions`() =
        runTest(testDispatcher) {
            val type = SessionType.STOPWATCH
            sessionsOf(today.toString(), type).value = listOf(session("t1", today, type))
            sessionsOf(yesterday.toString(), type).value = listOf(session("y1", yesterday, type))
            val vm = StopwatchHistoryViewModel(repository)
            testScheduler.runCurrent()

            vm.goToPreviousDay()
            testScheduler.runCurrent()
            vm.goToNextDay()
            testScheduler.runCurrent()
            // 数据库变化：昨天的查询重新推送
            sessionsOf(yesterday.toString(), type).value = listOf(session("y1", yesterday, type, "改名"))
            testScheduler.runCurrent()

            assertEquals(listOf("t1"), vm.uiState.value.sessions.map { it.id })
        }

    @Test
    fun `stopwatch - after switching session, old session query no longer overwrites records`() =
        runTest(testDispatcher) {
            val type = SessionType.STOPWATCH
            sessionsOf(today.toString(), type).value = listOf(session("s1", today, type), session("s2", today, type))
            recordsOf("s1").value = listOf(record("r1", "s1"))
            recordsOf("s2").value = listOf(record("r2", "s2"))
            val vm = StopwatchHistoryViewModel(repository)
            testScheduler.runCurrent()

            vm.selectSession(1)
            testScheduler.runCurrent()
            recordsOf("s1").value = listOf(record("r1", "s1"), record("r1b", "s1"))
            testScheduler.runCurrent()

            assertEquals(listOf("r2"), vm.uiState.value.selectedSessionRecords.map { it.id })
        }

    @Test
    fun `stopwatch - renaming the selected session keeps it selected`() =
        runTest(testDispatcher) {
            val type = SessionType.STOPWATCH
            sessionsOf(today.toString(), type).value = listOf(session("s1", today, type), session("s2", today, type))
            recordsOf("s2").value = listOf(record("r2", "s2"))
            val vm = StopwatchHistoryViewModel(repository)
            testScheduler.runCurrent()
            vm.selectSession(1)
            testScheduler.runCurrent()

            sessionsOf(today.toString(), type).value =
                listOf(session("s1", today, type), session("s2", today, type, "新标题"))
            testScheduler.runCurrent()

            assertEquals(1, vm.uiState.value.currentSessionIndex)
            assertEquals(listOf("r2"), vm.uiState.value.selectedSessionRecords.map { it.id })
        }

    @Test
    fun `stopwatch - deleting the selected last session falls back to a valid index`() =
        runTest(testDispatcher) {
            val type = SessionType.STOPWATCH
            sessionsOf(today.toString(), type).value = listOf(session("s1", today, type), session("s2", today, type))
            recordsOf("s1").value = listOf(record("r1", "s1"))
            val vm = StopwatchHistoryViewModel(repository)
            testScheduler.runCurrent()
            vm.selectSession(1)
            testScheduler.runCurrent()

            sessionsOf(today.toString(), type).value = listOf(session("s1", today, type))
            testScheduler.runCurrent()

            assertEquals(0, vm.uiState.value.currentSessionIndex)
            assertEquals(listOf("r1"), vm.uiState.value.selectedSessionRecords.map { it.id })
        }

    // ========== 事件历史 ==========

    @Test
    fun `event - after switching date away and back, old date queries no longer overwrite state`() =
        runTest(testDispatcher) {
            val type = SessionType.EVENT
            sessionsOf(today.toString(), type).value = listOf(session("t", today, type))
            sessionsOf(yesterday.toString(), type).value = listOf(session("y", yesterday, type))
            recordsOf("t").value = listOf(record("rt", "t"))
            recordsOf("y").value = listOf(record("ry", "y"))
            val vm = EventHistoryViewModel(repository)
            testScheduler.runCurrent()

            vm.goToPreviousDay()
            testScheduler.runCurrent()
            vm.goToNextDay()
            testScheduler.runCurrent()
            sessionsOf(yesterday.toString(), type).value = listOf(session("y", yesterday, type, "x"))
            recordsOf("y").value = listOf(record("ry", "y"), record("ry2", "y"))
            testScheduler.runCurrent()

            assertEquals(listOf("t"), vm.uiState.value.sessions.map { it.id })
            assertEquals(listOf("rt"), vm.uiState.value.selectedSessionRecords.map { it.id })
        }
}
