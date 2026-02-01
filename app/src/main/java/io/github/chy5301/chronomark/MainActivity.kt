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
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.ThemeMode
import io.github.chy5301.chronomark.data.model.UpdateInfo
import io.github.chy5301.chronomark.data.network.UpdateChecker
import io.github.chy5301.chronomark.ui.components.dialog.UpdateDialog
import io.github.chy5301.chronomark.ui.screen.MainScreen
import io.github.chy5301.chronomark.ui.theme.ChronoMarkTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var historyRepository: HistoryRepository

    // 时区变化检测状态
    private val _timezoneChanged = MutableStateFlow(false)
    val timezoneChanged: StateFlow<Boolean> = _timezoneChanged.asStateFlow()

    // 更新检查器
    private val updateChecker = UpdateChecker()

    // 更新信息状态
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化数据管理器
        dataStoreManager = DataStoreManager(this)
        val database = AppDatabase.getDatabase(this)
        historyRepository = HistoryRepository(database.historyDao())

        // 执行启动时的初始化任务
        lifecycleScope.launch {
            cleanupOldData()
            checkTimezoneChange()
            checkForUpdateOnStartup()
        }

        setContent {
            // 监听保持屏幕常亮设置
            val keepScreenOn by dataStoreManager.keepScreenOnFlow.collectAsState(initial = false)

            // 监听主题模式设置
            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

            // 监听更新信息
            val currentUpdateInfo by updateInfo.collectAsState()

            // 获取当前版本号
            val versionName = remember {
                try {
                    packageManager.getPackageInfo(packageName, 0).versionName ?: "未知版本"
                } catch (_: Exception) {
                    "未知版本"
                }
            }

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

                MainScreen(
                    timezoneChanged = timezoneChanged,
                    onDismissTimezoneNotice = ::dismissTimezoneChangeNotice
                )

                // 更新提示对话框
                currentUpdateInfo?.let { info ->
                    UpdateDialog(
                        currentVersion = versionName,
                        updateInfo = info,
                        onDismiss = { dismissUpdateDialog() },
                        onIgnoreVersion = {
                            lifecycleScope.launch {
                                dataStoreManager.addIgnoredVersion(info.version)
                            }
                            dismissUpdateDialog()
                        }
                    )
                }
            }
        }
    }

    /**
     * 检测时区是否发生变化
     * 首次安装或更新时不会提示（lastTimezoneId 为空）
     */
    private suspend fun checkTimezoneChange() {
        val lastTimezoneId = dataStoreManager.lastTimezoneIdFlow.first()
        val currentTimezoneId = ZoneId.systemDefault().id

        if (lastTimezoneId.isNotEmpty() && lastTimezoneId != currentTimezoneId) {
            Log.i(TAG, "Timezone changed: $lastTimezoneId -> $currentTimezoneId")
            _timezoneChanged.value = true
        }

        // 无论是否变化，都更新存储
        dataStoreManager.saveLastTimezoneId(currentTimezoneId)
    }

    /**
     * 关闭时区变化提示
     */
    fun dismissTimezoneChangeNotice() {
        _timezoneChanged.value = false
    }

    /**
     * 清理过期的历史数据
     *
     * 注意：事件模式的归档逻辑已移至 EventViewModel，
     * 采用实时同步方式，不再在 MainActivity 中处理。
     */
    private suspend fun cleanupOldData() {
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
     * 启动时检查更新
     * 遵循以下条件：
     * 1. 自动检查更新已启用
     * 2. 距离上次检查已超过 24 小时
     */
    private suspend fun checkForUpdateOnStartup() {
        // 检查是否启用自动更新
        val autoCheckEnabled = dataStoreManager.autoUpdateCheckEnabledFlow.first()
        if (!autoCheckEnabled) {
            Log.d(TAG, "Auto update check is disabled")
            return
        }

        // 检查是否满足时间间隔
        val lastCheckTime = dataStoreManager.lastUpdateCheckTimeFlow.first()
        if (!updateChecker.shouldAutoCheck(lastCheckTime)) {
            Log.d(TAG, "Not time for auto update check yet")
            return
        }

        // 获取当前版本
        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: return
        } catch (_: Exception) {
            return
        }

        // 获取更新通道和忽略版本列表
        val channel = dataStoreManager.updateChannelFlow.first()
        val ignoredVersions = dataStoreManager.ignoredVersionsFlow.first()

        Log.d(TAG, "Starting auto update check")

        // 执行检查
        when (val result = updateChecker.checkForUpdate(currentVersion, channel, ignoredVersions)) {
            is UpdateChecker.CheckResult.UpdateAvailable -> {
                Log.i(TAG, "Auto check found new version: ${result.updateInfo.version}")
                _updateInfo.value = result.updateInfo
            }
            is UpdateChecker.CheckResult.UpToDate -> {
                Log.d(TAG, "Auto check: already up to date")
            }
            is UpdateChecker.CheckResult.Error -> {
                // 自动检查失败静默处理，不打扰用户
                Log.w(TAG, "Auto check failed: ${result.message}")
            }
        }

        // 更新检查时间
        dataStoreManager.saveLastUpdateCheckTime(System.currentTimeMillis())
    }

    /**
     * 关闭更新对话框
     */
    private fun dismissUpdateDialog() {
        _updateInfo.value = null
    }
}
