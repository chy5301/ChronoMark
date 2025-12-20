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
import kotlinx.coroutines.launch
import android.content.pm.PackageManager

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
    val coroutineScope = rememberCoroutineScope()

    // 从 DataStore 读取设置
    val keepScreenOn by dataStoreManager.keepScreenOnFlow.collectAsState(initial = false)
    val vibrationEnabled by dataStoreManager.vibrationEnabledFlow.collectAsState(initial = true)

    // 获取应用版本号
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
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
                        }
                    }
                )
            }

            // 外观设置分组
            SettingsGroup(title = "外观") {
                // 主题设置
                SettingsNavigationItem(
                    title = "主题",
                    description = "跟随系统",
                    onClick = { /* TODO: 打开主题选择对话框 */ }
                )
            }

            // 关于设置分组
            SettingsGroup(title = "关于") {
                // 版本信息
                SettingsInfoItem(
                    title = "版本",
                    description = versionName
                )
            }
        }
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
