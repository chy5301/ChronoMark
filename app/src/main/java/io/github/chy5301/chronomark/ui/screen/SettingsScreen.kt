package io.github.chy5301.chronomark.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.chy5301.chronomark.data.DataStoreManager
import io.github.chy5301.chronomark.data.database.AppDatabase
import io.github.chy5301.chronomark.data.database.repository.HistoryRepository
import io.github.chy5301.chronomark.data.model.ThemeMode
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import java.util.Locale

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val historyRepository = remember {
        val database = AppDatabase.getDatabase(context)
        HistoryRepository(database.historyDao())
    }
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取常规设置
    val keepScreenOn by dataStoreManager.keepScreenOnFlow.collectAsState(initial = false)
    val vibrationEnabled by dataStoreManager.vibrationEnabledFlow.collectAsState(initial = true)
    val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

    // 从 DataStore 读取归档设置
    val autoArchiveEnabled by dataStoreManager.autoArchiveEnabledFlow.collectAsState(initial = true)
    val archiveBoundaryHour by dataStoreManager.archiveBoundaryHourFlow.collectAsState(initial = 4)
    val archiveBoundaryMinute by dataStoreManager.archiveBoundaryMinuteFlow.collectAsState(initial = 0)
    val historyRetentionDays by dataStoreManager.historyRetentionDaysFlow.collectAsState(initial = 365)

    // 对话框状态
    var showThemeDialog by remember { mutableStateOf(false) }
    var showBoundaryTimeDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // 获取应用版本号
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: PackageManager.NameNotFoundException) {
            "未知版本"
        }
    }

    // 拦截返回键，返回主界面而不是关闭应用
    BackHandler(onBack = onBackClick)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 常规设置分组
            SettingsGroup(title = "常规") {
                // 保持屏幕常亮
                SettingsSwitchItem(
                    title = "保持屏幕常亮",
                    description = "计时时防止屏幕自动熄灭",
                    checked = keepScreenOn,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            dataStoreManager.saveKeepScreenOn(enabled)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )

                HorizontalDivider()

                // 震动反馈
                SettingsSwitchItem(
                    title = "震动反馈",
                    description = "操作按钮时提供震动反馈",
                    checked = vibrationEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            dataStoreManager.saveVibrationEnabled(enabled)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )
            }

            // 外观设置分组
            SettingsGroup(title = "外观") {
                // 主题设置
                SettingsNavigationItem(
                    title = "主题",
                    description = themeMode.getDisplayName(),
                    onClick = { showThemeDialog = true }
                )
            }

            // 历史记录设置分组
            SettingsGroup(title = "历史记录") {
                // 自动归档开关
                SettingsSwitchItem(
                    title = "自动归档",
                    description = "在分界点自动将事件记录归档到历史",
                    checked = autoArchiveEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            dataStoreManager.saveAutoArchiveEnabled(enabled)
                                .onFailure { e -> e.printStackTrace() }
                        }
                    }
                )

                HorizontalDivider()

                // 归档分界点时间选择器
                SettingsNavigationItem(
                    title = "归档分界点",
                    description = String.format(
                        Locale.US,
                        "%02d:%02d",
                        archiveBoundaryHour,
                        archiveBoundaryMinute
                    ),
                    onClick = { showBoundaryTimeDialog = true }
                )

                HorizontalDivider()

                // 历史记录保留时长选择器
                SettingsNavigationItem(
                    title = "历史记录保留",
                    description = if (historyRetentionDays < 0) "永久保留" else "${historyRetentionDays}天",
                    onClick = { showRetentionDialog = true }
                )

                HorizontalDivider()

                // 清空所有历史记录按钮
                SettingsDangerItem(
                    title = "清空所有历史记录",
                    onClick = { showClearHistoryDialog = true }
                )
            }

            // 关于设置分组
            SettingsGroup(title = "关于") {
                // 版本信息
                SettingsInfoItem(
                    title = "版本",
                    description = versionName ?: "未知版本"
                )
            }
        }
    }

    // 主题选择对话框
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { selectedTheme ->
                coroutineScope.launch {
                    dataStoreManager.saveThemeMode(selectedTheme)
                        .onFailure { e -> e.printStackTrace() }
                }
                showThemeDialog = false
            }
        )
    }

    // 分界点时间选择器对话框
    if (showBoundaryTimeDialog) {
        BoundaryTimePickerDialog(
            currentHour = archiveBoundaryHour,
            currentMinute = archiveBoundaryMinute,
            onDismiss = { showBoundaryTimeDialog = false },
            onTimeSelected = { hour, minute ->
                coroutineScope.launch {
                    dataStoreManager.saveArchiveBoundaryHour(hour)
                        .onFailure { e -> e.printStackTrace() }
                    dataStoreManager.saveArchiveBoundaryMinute(minute)
                        .onFailure { e -> e.printStackTrace() }
                }
                showBoundaryTimeDialog = false
            }
        )
    }

    // 保留时长选择器对话框
    if (showRetentionDialog) {
        RetentionDaysDialog(
            currentDays = historyRetentionDays,
            onDismiss = { showRetentionDialog = false },
            onDaysSelected = { days ->
                coroutineScope.launch {
                    dataStoreManager.saveHistoryRetentionDays(days)
                        .onFailure { e -> e.printStackTrace() }
                }
                showRetentionDialog = false
            }
        )
    }

    // 清空历史记录确认对话框
    if (showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onDismiss = { showClearHistoryDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    historyRepository.deleteAllSessions()
                        .onSuccess {
                            // 可以在这里显示 Toast 提示成功
                        }
                        .onFailure { e ->
                            e.printStackTrace()
                            // 可以在这里显示 Toast 提示失败
                        }
                }
                showClearHistoryDialog = false
            }
        )
    }
}

/**
 * 设置分组标题
 */
@Composable
fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 带开关的设置项
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * 可导航的设置项
 */
@Composable
fun SettingsNavigationItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 信息展示设置项（不可点击）
 */
@Composable
fun SettingsInfoItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 危险操作设置项（红色文字）
 */
@Composable
fun SettingsDangerItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * 主题选择对话框
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "选择主题")
        },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = theme.getDisplayName(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 分界点时间选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoundaryTimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择归档时间点",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Material3 TimePicker（时间轮样式）
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = "推荐：凌晨 4:00（避免日常活动时间）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onTimeSelected(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 历史记录保留时长选择器对话框
 */
@Composable
fun RetentionDaysDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onDaysSelected: (Int) -> Unit
) {
    val options = listOf(
        30 to "30 天\n节省存储空间",
        90 to "90 天\n保留最近三个月",
        180 to "180 天\n保留半年记录",
        365 to "365 天（推荐）\n保留一年记录",
        -1 to "永久保留\n不自动删除（需手动清理）"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "历史记录保留时长")
        },
        text = {
            Column {
                options.forEach { (days, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDaysSelected(days) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = days == currentDays,
                            onClick = { onDaysSelected(days) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 清空历史记录确认对话框
 */
@Composable
fun ClearHistoryConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "清空所有历史记录")
        },
        text = {
            Text(
                text = "此操作将删除所有已归档的历史记录，且无法恢复。确定要继续吗？",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确定删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
