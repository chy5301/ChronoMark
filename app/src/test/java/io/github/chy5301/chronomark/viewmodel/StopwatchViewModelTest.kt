package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.StopwatchStatus
import io.github.chy5301.chronomark.data.model.StopwatchTiming
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * StopwatchViewModel 单元测试
 *
 * 通过注入假时钟控制时间流逝；只调用 runCurrent()，不推进虚拟时间，
 * 因此 10ms 刷新循环不会运行，可以验证持久化不依赖界面刷新值。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StopwatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val sec = 1_000_000_000L
    private val baseWall = 1_700_000_000_000L

    private var nowRealtime = 1_000 * sec
    private var nowWall = baseWall

    private lateinit var viewModel: StopwatchViewModel
    private lateinit var dataStore: DataStoreManager
    private lateinit var history: HistoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        dataStore = mockk(relaxed = true)
        history = mockk(relaxed = true)
        givenSaved(StopwatchStatus.Idle, StopwatchTiming(), emptyList())
        coEvery { dataStore.saveStopwatchSnapshot(any(), any(), any()) } returns Result.success(Unit)
        coEvery { dataStore.clearStopwatchData() } returns Result.success(Unit)
        coEvery { history.archiveStopwatchRecords(any(), any(), any(), any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun givenSaved(status: StopwatchStatus, timing: StopwatchTiming, records: List<TimeRecord>) {
        every { dataStore.stopwatchStatusFlow } returns flowOf(status)
        every { dataStore.stopwatchTimingFlow } returns flowOf(timing)
        every { dataStore.stopwatchRecordsFlow } returns flowOf(records)
    }

    private fun TestScope.createViewModel() {
        viewModel = StopwatchViewModel(dataStore, history, { nowRealtime }, { nowWall })
        testScheduler.runCurrent()
    }

    private fun advance(seconds: Long) {
        nowRealtime += seconds * sec
        nowWall += seconds * 1000
    }

    /**
     * 包装 runTest：无论断言是否失败，都在 finally 中调用 onCleared 取消 ViewModel 的无限循环协程。
     * 否则 runTest 收尾时会在虚拟时间里无限推进这些循环，测试进程永远卡住。
     */
    private fun vmTest(body: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            body()
        } finally {
            if (::viewModel.isInitialized) {
                val m = viewModel::class.java.getDeclaredMethod("onCleared")
                m.isAccessible = true
                m.invoke(viewModel)
            }
        }
    }

    @Test
    fun `restore while running - keeps running and counts time spent while process was dead`() =
        vmTest {
            givenSaved(
                StopwatchStatus.Running,
                StopwatchTiming(
                    accumulatedNanos = 5 * sec,
                    anchorRealtimeNanos = nowRealtime,
                    anchorWallMillis = nowWall,
                    startWallMillis = baseWall
                ),
                emptyList()
            )
            advance(3600)

            createViewModel()

            assertEquals(StopwatchStatus.Running, viewModel.uiState.value.status)
            assertEquals(3605 * sec, viewModel.uiState.value.currentTimeNanos)
        }

    @Test
    fun `restore after running - next mark split is measured from previous mark`() =
        vmTest {
            val previousMark = TimeRecord(
                id = "m1", wallClockTime = baseWall, elapsedTimeNanos = 5 * sec, splitTimeNanos = 5 * sec
            )
            givenSaved(
                StopwatchStatus.Running,
                StopwatchTiming(
                    accumulatedNanos = 5 * sec,
                    anchorRealtimeNanos = nowRealtime,
                    anchorWallMillis = nowWall,
                    startWallMillis = baseWall
                ),
                listOf(previousMark)
            )
            advance(100)
            createViewModel()

            viewModel.addMark()

            val newest = viewModel.uiState.value.records.first()
            assertEquals(105 * sec, newest.elapsedTimeNanos)
            assertEquals(100 * sec, newest.splitTimeNanos)
        }

    @Test
    fun `pause - persists exact elapsed time without relying on ui refresh`() =
        vmTest {
            createViewModel()
            viewModel.start()
            testScheduler.runCurrent()
            advance(42)

            viewModel.pause()
            testScheduler.runCurrent()

            val timing = slot<StopwatchTiming>()
            coVerify { dataStore.saveStopwatchSnapshot(StopwatchStatus.Paused, capture(timing), any()) }
            assertEquals(42 * sec, timing.captured.accumulatedNanos)
            assertEquals(42 * sec, viewModel.uiState.value.currentTimeNanos)
        }

    @Test
    fun `legacy data without anchor - restores running as paused with saved elapsed`() =
        vmTest {
            givenSaved(StopwatchStatus.Running, StopwatchTiming(accumulatedNanos = 7 * sec), emptyList())

            createViewModel()

            assertEquals(StopwatchStatus.Paused, viewModel.uiState.value.status)
            assertEquals(7 * sec, viewModel.uiState.value.currentTimeNanos)
        }

    @Test
    fun `saveToHistory - uses session start time and exact total elapsed`() =
        vmTest {
            createViewModel()
            viewModel.start()
            advance(300)
            viewModel.addMark()
            advance(1500)
            viewModel.addMark()
            advance(10)
            viewModel.pause()
            viewModel.stop()

            viewModel.saveToHistory("会话 1")
            testScheduler.runCurrent()

            coVerify {
                history.archiveStopwatchRecords(
                    records = any(),
                    title = "会话 1",
                    startTime = baseWall,
                    totalElapsedNanos = 1810 * sec
                )
            }
        }

    @Test
    fun `saveToHistory - legacy session without start time falls back to earliest mark`() =
        vmTest {
            val earliest = TimeRecord(id = "a", wallClockTime = baseWall + 1_000, elapsedTimeNanos = sec, splitTimeNanos = sec)
            val latest = TimeRecord(id = "b", wallClockTime = baseWall + 9_000, elapsedTimeNanos = 9 * sec, splitTimeNanos = 8 * sec)
            givenSaved(StopwatchStatus.Stopped, StopwatchTiming(accumulatedNanos = 10 * sec), listOf(latest, earliest))
            createViewModel()

            viewModel.saveToHistory("旧会话")
            testScheduler.runCurrent()

            coVerify {
                history.archiveStopwatchRecords(any(), "旧会话", baseWall + 1_000, 10 * sec)
            }
        }
}
