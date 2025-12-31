package io.github.chy5301.chronomark

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.ThemeMode
import io.github.chy5301.chronomark.ui.screen.MainScreen
import io.github.chy5301.chronomark.ui.theme.ChronoMarkTheme
import io.github.chy5301.chronomark.util.ArchiveUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var historyRepository: HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化数据管理器
        dataStoreManager = DataStoreManager(this)
        val database = AppDatabase.getDatabase(this)
        historyRepository = HistoryRepository(database.historyDao())

        // 执行启动时的清理和归档检查
        lifecycleScope.launch {
            checkAndCleanupOldData()
        }

        setContent {
            // 监听保持屏幕常亮设置
            val keepScreenOn by dataStoreManager.keepScreenOnFlow.collectAsState(initial = false)

            // 监听主题模式设置
            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

            // 根据主题模式设置确定是否使用深色主题
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme
            }

            ChronoMarkTheme(darkTheme = useDarkTheme) {
                // 根据设置更新窗口标志
                DisposableEffect(keepScreenOn) {
                    if (keepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }

                    onDispose {
                        // 清理：组件销毁时移除标志
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                MainScreen()
            }
        }
    }

    /**
     * Activity 从后台恢复到前台时触发
     * 检查逻辑日期是否变化，如果变化则触发归档检查
     */
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            // 读取上次检查时间戳和归档设置
            val lastCheckTimestamp = dataStoreManager.lastArchiveCheckTimestampFlow.first()
            val currentTimestamp = System.currentTimeMillis()
            val boundaryHour = dataStoreManager.archiveBoundaryHourFlow.first()
            val boundaryMinute = dataStoreManager.archiveBoundaryMinuteFlow.first()

            // 如果逻辑日期变化了，触发归档检查
            if (lastCheckTimestamp != 0L) {
                try {
                    val boundaryTime = ArchiveUtils.createBoundaryTime(boundaryHour, boundaryMinute)
                    val lastLogicalDate = ArchiveUtils.getLogicalDate(lastCheckTimestamp, boundaryTime)
                    val currentLogicalDate = ArchiveUtils.getLogicalDate(currentTimestamp, boundaryTime)

                    if (currentLogicalDate.isAfter(lastLogicalDate)) {
                        Log.i(TAG, "Logical date changed detected in onResume, triggering archive check")
                        checkAndCleanupOldData()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to check logical date in onResume", e)
                }
            }
        }
    }

    /**
     * 启动时清理旧数据并检查是否需要归档
     */
    private suspend fun checkAndCleanupOldData() {
        // 1. 先执行自动归档（必须在清理之前，避免刚归档的数据被立即删除）
        val autoArchiveEnabled = dataStoreManager.autoArchiveEnabledFlow.first()
        if (autoArchiveEnabled) {
            val lastCheckTimestamp = dataStoreManager.lastArchiveCheckTimestampFlow.first()
            val currentTimestamp = System.currentTimeMillis()
            val boundaryHour = dataStoreManager.archiveBoundaryHourFlow.first()
            val boundaryMinute = dataStoreManager.archiveBoundaryMinuteFlow.first()

            if (shouldArchive(lastCheckTimestamp, currentTimestamp, boundaryHour, boundaryMinute)) {
                Log.i(TAG, "Archive conditions met, starting auto-archive")
                performAutoArchive(boundaryHour, boundaryMinute)
                // 更新最后检查时间戳
                dataStoreManager.saveLastArchiveCheckTimestamp(currentTimestamp)
                    .onFailure { e ->
                        Log.e(TAG, "Failed to update last archive check timestamp", e)
                    }
            } else {
                Log.i(TAG, "Archive conditions not met, skipping archive")
            }
        } else {
            Log.i(TAG, "Auto-archive is disabled, skipping archive check")
        }

        // 2. 清理过期的旧数据（在归档之后执行，避免删除刚归档的数据）
        val retentionDays = dataStoreManager.historyRetentionDaysFlow.first()
        historyRepository.cleanupOldData(retentionDays)
            .onSuccess {
                Log.i(TAG, "Old data cleanup completed")
            }
            .onFailure { e ->
                // 清理失败不影响应用启动，只记录日志
                Log.w(TAG, "Cleanup failed but app continues", e)
            }
    }

    /**
     * 判断是否需要归档（基于逻辑日期）
     *
     * @param lastCheckTimestamp 上次检查的时间戳（毫秒）
     * @param currentTimestamp 当前时间戳（毫秒）
     * @param boundaryHour 分界点小时（0-23）
     * @param boundaryMinute 分界点分钟（0-59）
     * @return 是否需要归档
     */
    private suspend fun shouldArchive(
        lastCheckTimestamp: Long,
        currentTimestamp: Long,
        boundaryHour: Int,
        boundaryMinute: Int
    ): Boolean {
        // 首次使用，初始化为当前时间戳，不触发归档
        if (lastCheckTimestamp == 0L) {
            dataStoreManager.saveLastArchiveCheckTimestamp(currentTimestamp)
                .onFailure { e ->
                    Log.e(TAG, "Failed to initialize last archive check timestamp", e)
                }
            Log.i(TAG, "First time using app, initialized last check timestamp")
            return false
        }

        // 系统时间回退检测
        if (currentTimestamp < lastCheckTimestamp) {
            Log.w(TAG, "System time rollback detected, resetting timestamp without archiving")
            dataStoreManager.saveLastArchiveCheckTimestamp(currentTimestamp)
                .onFailure { e ->
                    Log.e(TAG, "Failed to reset timestamp after time rollback", e)
                }
            return false
        }

        // 计算逻辑日期
        val boundaryTime = ArchiveUtils.createBoundaryTime(boundaryHour, boundaryMinute)
        val lastLogicalDate = ArchiveUtils.getLogicalDate(lastCheckTimestamp, boundaryTime)
        val currentLogicalDate = ArchiveUtils.getLogicalDate(currentTimestamp, boundaryTime)

        // 逻辑日期变化即触发归档
        if (currentLogicalDate.isAfter(lastLogicalDate)) {
            Log.i(TAG, "Logical date changed from $lastLogicalDate to $currentLogicalDate, will archive")
            return true
        }

        return false
    }

    /**
     * 执行自动归档操作（基于逻辑日期分组）
     *
     * @param boundaryHour 分界点小时
     * @param boundaryMinute 分界点分钟
     */
    private suspend fun performAutoArchive(boundaryHour: Int, boundaryMinute: Int) {
        // 读取事件记录
        val records = dataStoreManager.eventRecordsFlow.first()
        if (records.isEmpty()) {
            Log.i(TAG, "No event records to archive")
            return
        }

        Log.i(TAG, "Starting auto archive for ${records.size} event records")

        // 计算分界点时间和当前逻辑日期
        val boundaryTime = ArchiveUtils.createBoundaryTime(boundaryHour, boundaryMinute)
        val currentTimestamp = System.currentTimeMillis()
        val today = ArchiveUtils.getLogicalDate(currentTimestamp, boundaryTime)

        // 按逻辑日期分组记录
        val recordsByLogicalDate = records.groupBy { record ->
            ArchiveUtils.getLogicalDate(record.wallClockTime, boundaryTime)
        }

        Log.i(TAG, "Grouped into ${recordsByLogicalDate.size} logical dates")

        // 分别归档每个逻辑日期的记录
        var totalArchived = 0
        var recordsToKeep = emptyList<io.github.chy5301.chronomark.data.model.TimeRecord>()

        recordsByLogicalDate.forEach { (logicalDate, dateRecords) ->
            if (logicalDate.isBefore(today)) {
                // 归档到历史
                Log.i(TAG, "Archiving ${dateRecords.size} records for $logicalDate")
                historyRepository.archiveEventRecordsByDate(logicalDate.toString(), dateRecords)
                    .onSuccess {
                        totalArchived += dateRecords.size
                        Log.i(TAG, "Successfully archived ${dateRecords.size} records for $logicalDate")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to archive records for $logicalDate", e)
                    }
            } else {
                // 保留今天的记录在工作区
                Log.i(TAG, "Keeping ${dateRecords.size} records for today ($logicalDate)")
                recordsToKeep = dateRecords
            }
        }

        // 更新工作区（只保留今天的记录）
        dataStoreManager.saveEventRecords(recordsToKeep)
            .onSuccess {
                Log.i(TAG, "Auto archive completed: $totalArchived records archived, ${recordsToKeep.size} kept in workspace")
            }
            .onFailure { e ->
                Log.e(TAG, "Failed to update workspace after archive", e)
            }
    }
}