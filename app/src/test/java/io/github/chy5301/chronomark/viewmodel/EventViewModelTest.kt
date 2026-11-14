package io.github.chy5301.chronomark.viewmodel

import android.util.Log
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.TimeRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * EventViewModel 单元测试
 *
 * HistoryRepository 用内存「假数据库」实现：每个日期一个 MutableStateFlow，
 * 写操作修改它，订阅者随之收到推送，行为与 Room 的 Flow 查询一致。
 * 墙上时间通过注入的时钟控制，便于测试跨过逻辑日期分界点。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: EventViewModel
    private lateinit var dataStore: DataStoreManager
    private lateinit var repository: HistoryRepository

    /** 假数据库：逻辑日期 -> 当天记录（按时间升序） */
    private val db = mutableMapOf<String, MutableStateFlow<List<TimeRecord>>>()
    private fun day(date: String) = db.getOrPut(date) { MutableStateFlow(emptyList()) }

    // 默认时间 2025-01-15 10:00（分界点 04:00，逻辑日期即 2025-01-15）
    private var now = millisOf(2025, 1, 15, 10, 0)
    private val today = "2025-01-15"

    private fun millisOf(y: Int, mo: Int, d: Int, h: Int, mi: Int, s: Int = 0) =
        LocalDateTime.of(y, mo, d, h, mi, s).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun record(id: String, wallClock: Long, note: String = "") =
        TimeRecord(id = id, wallClockTime = wallClock, elapsedTimeNanos = 0L, splitTimeNanos = 0L, note = note)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        dataStore = mockk(relaxed = true)
        every { dataStore.eventRecordsFlow } returns flowOf(emptyList())
        every { dataStore.archiveBoundaryHourFlow } returns flowOf(4)
        every { dataStore.archiveBoundaryMinuteFlow } returns flowOf(0)
        coEvery { dataStore.clearEventData() } returns Result.success(Unit)

        repository = mockk(relaxed = true)
        every { repository.observeEventRecordsByDate(any()) } answers { day(firstArg()) }
        coEvery { repository.getEventRecordsByDate(any()) } answers { day(firstArg()).value }
        coEvery { repository.insertEventRecord(any(), any()) } answers {
            val d = day(firstArg())
            d.value = (d.value + secondArg<TimeRecord>()).sortedBy { it.wallClockTime }
            Result.success(Unit)
        }
        coEvery { repository.insertEventRecords(any(), any()) } answers {
            val d = day(firstArg())
            d.value = (d.value + secondArg<List<TimeRecord>>()).sortedBy { it.wallClockTime }
            Result.success(Unit)
        }
        coEvery { repository.updateEventRecordNote(any(), any()) } answers {
            val id = firstArg<String>()
            db.values.forEach { d -> d.value = d.value.map { if (it.id == id) it.copy(note = secondArg()) else it } }
            Result.success(Unit)
        }
        coEvery { repository.deleteEventRecord(any()) } answers {
            val id = firstArg<String>()
            db.values.forEach { d -> d.value = d.value.filter { it.id != id } }
            Result.success(Unit)
        }
        coEvery { repository.deleteEventRecordsByDate(any()) } answers {
            day(firstArg()).value = emptyList()
            Result.success(Unit)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun TestScope.createViewModel(runInit: Boolean = true) {
        viewModel = EventViewModel(dataStore, repository) { now }
        if (runInit) testScheduler.runCurrent()
    }

    /**
     * 包装 runTest：无论断言是否失败，都在 finally 中调用 onCleared 取消墙上时钟的无限循环，
     * 否则 runTest 收尾时会在虚拟时间里无限推进该循环，测试进程永远卡住。
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

    // ========== 加载与迁移 ==========

    @Test
    fun `init - shows today's records from database`() = vmTest {
        day(today).value = listOf(record("r1", now - 60_000))

        createViewModel()

        assertEquals(listOf("r1"), viewModel.uiState.value.records.map { it.id })
    }

    @Test
    fun `init - with legacy data - migrates and clears datastore`() = vmTest {
        every { dataStore.eventRecordsFlow } returns flowOf(listOf(record("legacy-1", now - 1000)))

        createViewModel()

        coVerify(exactly = 1) { repository.insertEventRecords(today, any()) }
        coVerify(exactly = 1) { dataStore.clearEventData() }
        assertEquals(listOf("legacy-1"), viewModel.uiState.value.records.map { it.id })
    }

    @Test
    fun `init - migration fails for one date - keeps legacy data in datastore`() = vmTest {
        val yesterday = millisOf(2025, 1, 14, 10, 0)
        every { dataStore.eventRecordsFlow } returns
            flowOf(listOf(record("old", yesterday), record("new", now - 1000)))
        coEvery { repository.insertEventRecords("2025-01-14", any()) } returns Result.failure(Exception("disk full"))

        createViewModel()

        coVerify(exactly = 0) { dataStore.clearEventData() }
    }

    // ========== recordEvent ==========

    @Test
    fun `recordEvent - first record - splitTime is zero and persisted to today`() = vmTest {
        createViewModel()

        viewModel.recordEvent()
        testScheduler.runCurrent()

        val records = viewModel.uiState.value.records
        assertEquals(1, records.size)
        assertEquals(0L, records[0].splitTimeNanos)
        coVerify(exactly = 1) { repository.insertEventRecord(today, any()) }
    }

    @Test
    fun `recordEvent - subsequent record - split measured from previous record`() = vmTest {
        day(today).value = listOf(record("r1", now - 90_000))
        createViewModel()

        viewModel.recordEvent()
        testScheduler.runCurrent()

        val records = viewModel.uiState.value.records
        assertEquals(2, records.size)
        assertEquals(90_000_000_000L, records[1].splitTimeNanos)
    }

    @Test
    fun `recordEvent - two quick taps - second split measured from first`() = vmTest {
        createViewModel()

        viewModel.recordEvent()
        now += 2_000
        viewModel.recordEvent()
        testScheduler.runCurrent()

        val splits = viewModel.uiState.value.records.map { it.splitTimeNanos }
        assertEquals(listOf(0L, 2_000_000_000L), splits)
    }

    @Test
    fun `recordEvent - tapped before startup load finishes - record is not lost from screen`() = vmTest {
        createViewModel(runInit = false)

        viewModel.recordEvent()
        testScheduler.runCurrent()

        assertEquals(1, viewModel.uiState.value.records.size)
    }

    // ========== 外部数据变化 ==========

    @Test
    fun `records deleted elsewhere - disappear from workspace`() = vmTest {
        day(today).value = listOf(record("r1", now - 60_000))
        createViewModel()

        // 例如在设置页「清空所有历史」或在历史页删除今天的记录
        day(today).value = emptyList()
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.records.isEmpty())
    }

    @Test
    fun `crossing logical day boundary - shows new day and new record starts fresh`() = vmTest {
        now = millisOf(2025, 1, 15, 3, 59, 30)
        day("2025-01-14").value = listOf(record("late-night", now - 60_000))
        createViewModel()
        assertEquals(listOf("late-night"), viewModel.uiState.value.records.map { it.id })

        now = millisOf(2025, 1, 15, 4, 0, 30)
        testScheduler.advanceTimeBy(1_001)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.records.isEmpty())

        viewModel.recordEvent()
        testScheduler.runCurrent()
        assertEquals(1, day(today).value.size)
        assertEquals(0L, day(today).value[0].splitTimeNanos)
        assertEquals(listOf("late-night"), day("2025-01-14").value.map { it.id })
    }

    // ========== 编辑、删除、重置、分享 ==========

    @Test
    fun `updateRecordNote - updates database and screen`() = vmTest {
        day(today).value = listOf(record("r1", now - 1000))
        createViewModel()

        viewModel.updateRecordNote("r1", "updated note")
        testScheduler.runCurrent()

        assertEquals("updated note", viewModel.uiState.value.records[0].note)
        coVerify(exactly = 1) { repository.updateEventRecordNote("r1", "updated note") }
    }

    @Test
    fun `deleteRecord - removes record from database and screen`() = vmTest {
        day(today).value = listOf(record("r1", now - 3000), record("r2", now - 2000), record("r3", now - 1000))
        createViewModel()

        viewModel.deleteRecord("r2")
        testScheduler.runCurrent()

        assertEquals(listOf("r1", "r3"), viewModel.uiState.value.records.map { it.id })
        coVerify(exactly = 1) { repository.deleteEventRecord("r2") }
    }

    @Test
    fun `reset - clears today's records only`() = vmTest {
        day(today).value = listOf(record("r1", now - 1000))
        day("2025-01-14").value = listOf(record("old", millisOf(2025, 1, 14, 10, 0)))
        createViewModel()

        viewModel.reset()
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.records.isEmpty())
        coVerify(exactly = 1) { repository.deleteEventRecordsByDate(today) }
        assertEquals(1, day("2025-01-14").value.size)
    }

    @Test
    fun `generateShareText - returns formatted text`() = vmTest {
        day(today).value = listOf(record("r1", now - 1000, note = "first"))
        createViewModel()

        val shareText = viewModel.generateShareText()

        assertTrue(shareText.contains("first"))
    }
}
