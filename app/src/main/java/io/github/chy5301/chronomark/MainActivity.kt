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
     * 检查日期是否变化，如果变化则触发归档检查
     */
    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            // 读取上次检查日期
            val lastCheckDate = dataStoreManager.lastArchiveCheckDateFlow.first()
            val today = LocalDate.now()

            // 如果日期变化了，触发归档检查
            if (lastCheckDate.isNotEmpty()) {
                try {
                    val lastDate = LocalDate.parse(lastCheckDate)
                    if (today.isAfter(lastDate)) {
                        Log.i(TAG, "Date changed detected in onResume, triggering archive check")
                        checkAndCleanupOldData()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse lastCheckDate in onResume: $lastCheckDate", e)
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
            val lastCheckDate = dataStoreManager.lastArchiveCheckDateFlow.first()
            val today = LocalDate.now()

            // TODO: 方案B临时简化 - 固定使用00:00作为分界点
            // 未来需实现方案C：使用WorkManager在自定义boundary时间点主动触发归档
            // 相关代码已保留在DataStoreManager和SettingsScreen中，等待启用
            if (shouldArchive(lastCheckDate, today)) {
                Log.i(TAG, "Archive conditions met, starting auto-archive")
                performAutoArchive()
                // 更新最后检查日期
                dataStoreManager.saveLastArchiveCheckDate(today.toString())
                    .onFailure { e ->
                        Log.e(TAG, "Failed to update last archive check date", e)
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
     * 判断是否需要归档（方案B简化版本）
     *
     * @param lastCheckDate 上次检查日期（yyyy-MM-dd）
     * @param currentDate 当前日期
     * @return 是否需要归档
     */
    private suspend fun shouldArchive(
        lastCheckDate: String,
        currentDate: LocalDate
    ): Boolean {
        // 首次使用，初始化为当前日期，不触发归档
        if (lastCheckDate.isEmpty()) {
            dataStoreManager.saveLastArchiveCheckDate(currentDate.toString())
                .onFailure { e ->
                    Log.e(TAG, "Failed to initialize last archive check date", e)
                }
            Log.i(TAG, "First time using app, initialized last check date to $currentDate")
            return false
        }

        val lastDate = try {
            LocalDate.parse(lastCheckDate)
        } catch (e: Exception) {
            // 日期格式错误，重置为当前日期
            Log.w(TAG, "Invalid last check date format: $lastCheckDate, resetting to today", e)
            dataStoreManager.saveLastArchiveCheckDate(currentDate.toString())
            return false
        }

        // 方案B简化逻辑：只要日期变化就归档（固定00:00分界点）
        if (currentDate.isAfter(lastDate)) {
            Log.i(TAG, "Date changed from $lastDate to $currentDate, will archive")
            return true
        }

        return false
    }

    /**
     * 执行自动归档操作
     */
    private suspend fun performAutoArchive() {
        // 读取事件记录
        val records = dataStoreManager.eventRecordsFlow.first()
        if (records.isEmpty()) {
            Log.i(TAG, "No event records to archive")
            return
        }

        Log.i(TAG, "Starting auto archive for ${records.size} event records")

        // 归档到 Room 数据库
        historyRepository.archiveEventRecords(records)
            .onSuccess {
                Log.i(TAG, "Archive successful, clearing workspace")

                // 清空 DataStore 工作区
                dataStoreManager.clearEventRecords()
                    .onFailure { e ->
                        Log.e(TAG, "Failed to clear workspace after archive", e)
                    }

                Log.i(TAG, "Auto archive completed: ${records.size} records archived")
            }
            .onFailure { e ->
                Log.e(TAG, "Archive failed", e)
            }
    }
}