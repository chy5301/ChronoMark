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
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * EventViewModel 单元测试
 *
 * 使用 MockK 模拟 DataStoreManager 和 HistoryRepository，
 * 测试事件模式的核心业务逻辑
 *
 * 注意：EventViewModel 的 init 块会启动一个无限循环的协程（startWallClockTicking），
 * 必须在每个测试的 runTest 块内部调用 clearViewModel() 来取消协程，
 * 否则 runTest 会永远等待协程完成。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: EventViewModel
    private lateinit var mockDataStoreManager: DataStoreManager
    private lateinit var mockHistoryRepository: HistoryRepository

    @Before
    fun setup() {
        // 设置 Main dispatcher
        Dispatchers.setMain(testDispatcher)

        // Mock android.util.Log
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockDataStoreManager = mockk(relaxed = true)
        mockHistoryRepository = mockk(relaxed = true)

        // 默认设置：无遗留数据，分界点 04:00
        coEvery { mockDataStoreManager.eventRecordsFlow } returns flowOf(emptyList())
        coEvery { mockDataStoreManager.archiveBoundaryHourFlow } returns flowOf(4)
        coEvery { mockDataStoreManager.archiveBoundaryMinuteFlow } returns flowOf(0)
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    /**
     * 清理 ViewModel，取消无限循环协程
     * 必须在每个 runTest 块末尾调用！
     */
    private fun clearViewModel() {
        if (::viewModel.isInitialized) {
            try {
                val onClearedMethod = viewModel::class.java.getDeclaredMethod("onCleared")
                onClearedMethod.isAccessible = true
                onClearedMethod.invoke(viewModel)
            } catch (e: Exception) {
                // 忽略反射错误
            }
        }
    }

    // ========== 初始化测试 ==========

    @Test
    fun `init - no legacy data - loads today records directly`() = runTest(testDispatcher) {
        // Arrange
        coEvery { mockDataStoreManager.eventRecordsFlow } returns flowOf(emptyList())

        // Act
        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Assert
        coVerify(exactly = 0) { mockHistoryRepository.insertEventRecords(any(), any()) }
        coVerify(exactly = 1) { mockHistoryRepository.getEventRecordsByDate(any()) }
        assertTrue(viewModel.uiState.value.records.isEmpty())

        // 清理：必须在 runTest 块内调用
        clearViewModel()
    }

    @Test
    fun `init - with legacy data - migrates and clears datastore`() = runTest(testDispatcher) {
        // Arrange
        val legacyRecords = listOf(
            TimeRecord(id = "legacy-1", wallClockTime = 1705280400000L, elapsedTimeNanos = 0L, splitTimeNanos = 0L),
            TimeRecord(id = "legacy-2", wallClockTime = 1705280500000L, elapsedTimeNanos = 0L, splitTimeNanos = 100_000_000L)
        )
        coEvery { mockDataStoreManager.eventRecordsFlow } returns flowOf(legacyRecords)
        coEvery { mockDataStoreManager.clearEventData() } returns Result.success(Unit)
        coEvery { mockHistoryRepository.insertEventRecords(any(), any()) } returns Result.success(Unit)

        // Act
        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Assert
        coVerify(atLeast = 1) { mockHistoryRepository.insertEventRecords(any(), any()) }
        coVerify(exactly = 1) { mockDataStoreManager.clearEventData() }

        clearViewModel()
    }

    // ========== recordEvent 测试 ==========

    @Test
    fun `recordEvent - first record - splitTime is zero`() = runTest(testDispatcher) {
        // Arrange
        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.recordEvent()
        testScheduler.runCurrent()

        // Assert
        val records = viewModel.uiState.value.records
        assertEquals(1, records.size)
        assertEquals(0L, records[0].splitTimeNanos)

        clearViewModel()
    }

    @Test
    fun `recordEvent - subsequent record - calculates split time`() = runTest(testDispatcher) {
        // Arrange
        val existingRecords = listOf(
            TimeRecord(
                id = "existing-1",
                wallClockTime = System.currentTimeMillis() - 1000,
                elapsedTimeNanos = 0L,
                splitTimeNanos = 0L
            )
        )
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns existingRecords

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.recordEvent()
        testScheduler.runCurrent()

        // Assert
        val records = viewModel.uiState.value.records
        assertEquals(2, records.size)
        assertTrue(records[1].splitTimeNanos >= 900_000_000L)

        clearViewModel()
    }

    @Test
    fun `recordEvent - updates UI immediately`() = runTest(testDispatcher) {
        // Arrange
        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        val initialSize = viewModel.uiState.value.records.size

        // Act
        viewModel.recordEvent()

        // Assert - UI 应该已经更新（乐观更新）
        assertEquals(initialSize + 1, viewModel.uiState.value.records.size)

        clearViewModel()
    }

    @Test
    fun `recordEvent - persists to Room`() = runTest(testDispatcher) {
        // Arrange
        val recordSlot = slot<TimeRecord>()
        coEvery { mockHistoryRepository.insertEventRecord(any(), capture(recordSlot)) } returns Result.success(Unit)

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.recordEvent()
        testScheduler.runCurrent()

        // Assert
        coVerify(exactly = 1) { mockHistoryRepository.insertEventRecord(any(), any()) }
        assertTrue(recordSlot.isCaptured)

        clearViewModel()
    }

    // ========== updateRecordNote 测试 ==========

    @Test
    fun `updateRecordNote - updates UI and Room`() = runTest(testDispatcher) {
        // Arrange
        val existingRecord = TimeRecord(
            id = "record-1",
            wallClockTime = System.currentTimeMillis(),
            elapsedTimeNanos = 0L,
            splitTimeNanos = 0L,
            note = ""
        )
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns listOf(existingRecord)
        coEvery { mockHistoryRepository.updateEventRecordNote(any(), any()) } returns Result.success(Unit)

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.updateRecordNote("record-1", "updated note")
        testScheduler.runCurrent()

        // Assert
        assertEquals("updated note", viewModel.uiState.value.records[0].note)
        coVerify(exactly = 1) { mockHistoryRepository.updateEventRecordNote("record-1", "updated note") }

        clearViewModel()
    }

    // ========== deleteRecord 测试 ==========

    @Test
    fun `deleteRecord - removes record from list`() = runTest(testDispatcher) {
        // Arrange
        val records = listOf(
            TimeRecord(id = "r1", wallClockTime = 1000L, elapsedTimeNanos = 0L, splitTimeNanos = 0L),
            TimeRecord(id = "r2", wallClockTime = 2000L, elapsedTimeNanos = 0L, splitTimeNanos = 1_000_000_000L),
            TimeRecord(id = "r3", wallClockTime = 3000L, elapsedTimeNanos = 0L, splitTimeNanos = 1_000_000_000L)
        )
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns records
        coEvery { mockHistoryRepository.deleteEventRecord(any()) } returns Result.success(Unit)

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.deleteRecord("r2")
        testScheduler.runCurrent()

        // Assert
        val remaining = viewModel.uiState.value.records
        assertEquals(2, remaining.size)
        assertEquals("r1", remaining[0].id)
        assertEquals("r3", remaining[1].id)
        coVerify(exactly = 1) { mockHistoryRepository.deleteEventRecord("r2") }

        clearViewModel()
    }

    // ========== reset 测试 ==========

    @Test
    fun `reset - clears UI and Room`() = runTest(testDispatcher) {
        // Arrange
        val records = listOf(
            TimeRecord(id = "r1", wallClockTime = 1000L, elapsedTimeNanos = 0L, splitTimeNanos = 0L)
        )
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns records
        coEvery { mockHistoryRepository.deleteEventRecordsByDate(any()) } returns Result.success(Unit)

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        viewModel.reset()
        testScheduler.runCurrent()

        // Assert
        assertTrue(viewModel.uiState.value.records.isEmpty())
        coVerify(exactly = 1) { mockHistoryRepository.deleteEventRecordsByDate(any()) }

        clearViewModel()
    }

    // ========== generateShareText 测试 ==========

    @Test
    fun `generateShareText - returns formatted text`() = runTest(testDispatcher) {
        // Arrange
        val records = listOf(
            TimeRecord(id = "r1", wallClockTime = 1705280400000L, elapsedTimeNanos = 0L, splitTimeNanos = 0L, note = "first")
        )
        coEvery { mockHistoryRepository.getEventRecordsByDate(any()) } returns records

        viewModel = EventViewModel(mockDataStoreManager, mockHistoryRepository)
        testScheduler.runCurrent()

        // Act
        val shareText = viewModel.generateShareText()

        // Assert
        assertTrue(shareText.isNotEmpty())

        clearViewModel()
    }
}
