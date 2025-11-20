# CLAUDE.md

该文件为 Claude Code（claude.ai/code）在处理本代码库时提供指导。

## 项目概述

ChronoMark 是一个基于 Jetpack Compose 的 Android 秒表应用，采用简洁的传统秒表设计，支持高精度计时（毫秒级）、时间点标记、备注添加、实时墙上时钟显示以及多格式数据导出功能。

## 项目状态

**当前阶段**: Phase 1 & 2 核心功能已完成，准备开发备注编辑和数据持久化功能

### 已完成功能
- 高精度计时（毫秒级，使用 nanoTime）
- 基础计时操作（开始/暂停/继续/停止/重置）
- 时间点标记功能
- 双时间显示（计时器 + 墙上时钟）
- 记录列表 UI（LazyColumn + RecordCard）
- 控制按钮 UI 优化

## 技术栈

- **开发环境**: JetBrains Runtime 21 (Android Studio 自带 JDK)
- **UI 框架**: Jetpack Compose + Material3
- **语言**: Kotlin
- **构建工具**: Gradle (Kotlin DSL)
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36
- **项目编译目标**: Java 17

## 项目结构

```
app/src/main/java/io/github/chy5301/chronomark/
├── MainActivity.kt              # 主入口 Activity
├── data/
│   └── model/
│       ├── TimeRecord.kt        # 时间记录数据模型
│       ├── StopwatchStatus.kt   # 秒表状态枚举
│       └── StopwatchUiState.kt  # UI 状态数据类
├── ui/
│   ├── screen/
│   │   └── StopwatchScreen.kt   # 秒表主屏幕及所有 UI 组件
│   └── theme/                   # Compose 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   └── TimeFormatter.kt         # 时间格式化工具类
└── viewmodel/
    └── StopwatchViewModel.kt    # 秒表业务逻辑和状态管理
```

## 常用命令

### 构建和运行
```bash
# 构建项目
./gradlew build

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 安装并运行 Debug 版本
./gradlew installDebug
```

### 测试
```bash
# 运行单元测试
./gradlew test

# 运行单元测试(仅指定模块)
./gradlew :app:test

# 运行 Android 仪器化测试
./gradlew connectedAndroidTest

# 运行特定测试类
./gradlew test --tests ExampleUnitTest
```

### 代码质量
```bash
# 清理构建产物
./gradlew clean

# Lint 检查
./gradlew lint
```

## 依赖管理

项目使用 Gradle Version Catalog (`gradle/libs.versions.toml`) 管理依赖版本。添加新依赖时：

1. 在 `libs.versions.toml` 的 `[versions]` 部分添加版本号
2. 在 `[libraries]` 或 `[plugins]` 部分定义依赖
3. 在 `app/build.gradle.kts` 中通过 `libs.` 引用

## 项目架构

### 架构模式
- **MVVM (Model-View-ViewModel)** 配合 Jetpack Compose
- 使用 `ViewModel` 管理 UI 状态和业务逻辑
- 使用 `StateFlow` 进行状态管理
- 使用 Coroutines 处理异步操作和计时更新

### 核心功能模块

1. **秒表计时**: 高精度计时（毫秒级），支持开始、暂停、继续、停止、重置
2. **时间点标记**: 运行中可瞬间标记时间点，不中断计时流程
3. **双时间显示**: 同时显示累计经过时间和当前墙上时钟时间（精确到毫秒）
4. **备注功能**: 为每个时间点添加文字说明（后续手动补充）
5. **数据导出**: 支持导出为 CSV/JSON/TXT 格式，包含完整日期时间信息

## 详细设计规范

### 界面布局设计

#### 主界面结构
```
┌──────────────────────────────┐
│ 顶部栏 (TopAppBar)           │  ← 标题"秒表" + 导出按钮 + 菜单按钮
├──────────────────────────────┤
│ 时间显示区 (160.dp)          │  ← 计时器 + 墙上时钟（固定高度）
├──────────────────────────────┤
│                              │
│ 记录列表区 (weight 1f)       │  ← LazyColumn（可滚动，占据剩余空间）
│                              │
├──────────────────────────────┤
│ 控制按钮区 (160.dp)          │  ← 操作按钮（固定高度）
└──────────────────────────────┘
```

#### 时间显示区
```
           00:14.235                ← 主计时器（64sp，加粗）
      2025-11-10 13:47:37           ← 墙上时钟（24sp，次要颜色，带日期）
```

#### 记录卡片布局
```
┌─────────────────────────────────────┐
│ 01            00:07.403              │  ← 序号 + 累计时间
│ +00:07.403    13:47:30.474          │  ← 时间差 + 标记时刻
│ 📝 第一圈完成                        │  ← 备注（可选）
└─────────────────────────────────────┘
```

**字段说明**：
- **序号**: 记录编号（01, 02, 03...）
- **累计时间**: 从开始到该标记点的总时长（MM:SS.mmm）
- **时间差**: 与上一个标记点的时间差（+MM:SS.mmm）
- **标记时刻**: 标记时的系统时间（HH:mm:ss.SSS）
- **备注**: 用户添加的文字说明（可选显示）

### 操作流程设计

#### 状态流转
```
[初始状态]
  ↓ 点击"开始"
[运行中] - 左:[标记] 右:[暂停]
  ↓ 点击"暂停"
[暂停] - 左:[继续] 右:[停止]
  ↓ 点击"继续"
[运行中] - 继续计时
  ↓ 点击"暂停" → 点击"停止"
[已停止] - 显示[重置]按钮
  ↓ 点击"重置"
[初始状态]
```

#### 标记操作（核心交互）
1. **点击"标记"按钮** → 立即记录当前时间点
2. 在列表顶部插入新记录卡片（带淡入动画）
3. 计时器继续运行，完全不中断
4. 列表自动滚动到顶部显示最新记录

#### 备注编辑
- **暂停或停止后**：点击记录卡片 → 弹出编辑对话框
- 可添加/编辑备注文字
- 可删除该条记录

### 数据模型设计

```kotlin
// 时间记录
data class TimeRecord(
    val id: String = UUID.randomUUID().toString(),
    val index: Int,                   // 序号
    val wallClockTime: Long,          // 标记时的系统时间戳（毫秒）
    val elapsedTimeNanos: Long,       // 累计经过时间（纳秒）
    val splitTimeNanos: Long,         // 与上次的时间差（纳秒）
    val note: String = ""             // 备注
)

// 秒表状态
sealed class StopwatchStatus {
    object Idle : StopwatchStatus()       // 初始状态
    object Running : StopwatchStatus()    // 运行中
    object Paused : StopwatchStatus()     // 暂停
    object Stopped : StopwatchStatus()    // 停止（有记录）
}

// UI 状态
data class StopwatchUiState(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val currentTime: String = "00:00.000",              // 格式化的计时器时间
    val wallClockTime: String = "0000-00-00 00:00:00", // 格式化的墙上时钟（带日期）
    val currentTimeNanos: Long = 0L,                    // 原始纳秒值
    val records: List<TimeRecord> = emptyList()
)
```

### 时间格式化规范

```kotlin
// 经过时间格式: MM:SS.mmm (分:秒.毫秒)
formatElapsed(nanos) -> "00:07.403"

// 时间差格式: +MM:SS.mmm
formatSplit(nanos) -> "+00:07.403"

// 墙上时钟格式（记录卡片）: HH:mm:ss.SSS (时:分:秒.毫秒)
formatWallClock(timestampMillis) -> "13:47:30.474"

// 墙上时钟格式（主界面显示）: yyyy-MM-dd HH:mm:ss (日期+时间，不含毫秒)
formatWallClockWithDate(timestampMillis) -> "2025-11-10 13:47:37"

// 导出用完整时间: yyyy-MM-dd HH:mm:ss.SSS
formatFullTimestamp(timestampMillis) -> "2025-11-10 13:47:30.474"
```

### 导出格式规范

#### CSV 格式
```csv
序号,累计时间,时间差,标记时间,备注
01,00:07.403,+00:07.403,2025-11-10 13:47:30.474,第一圈完成
02,00:10.553,+00:03.150,2025-11-10 13:47:33.624,第二圈
```

#### JSON 格式
```json
{
  "session": {
    "totalTime": "00:12.055",
    "recordCount": 3,
    "exportTime": "2025-11-10 13:50:00.000"
  },
  "records": [
    {
      "index": 1,
      "elapsedTime": "00:07.403",
      "splitTime": "+00:07.403",
      "markTime": "2025-11-10 13:47:30.474",
      "note": "第一圈完成"
    }
  ]
}
```

#### TXT 格式
```
ChronoMark 秒表记录
导出时间: 2025-11-10 13:50:00
总用时: 00:12.055
记录数: 3

────────────────────────────────────

#01  累计: 00:07.403  差值: +00:07.403
     时间: 2025-11-10 13:47:30.474
     备注: 第一圈完成
```

### 视觉设计规范

#### 字体大小
- 主计时器: 64sp（加粗）
- 墙上时钟（主界面）: 24sp（常规）
- 记录累计时间: 28sp（加粗）
- 记录序号: 16sp（常规）
- 记录时间差: 20sp（常规）
- 记录时刻: 18sp（常规）
- 备注: 16sp（常规）

#### 颜色方案
使用 Material3 动态主题色，支持亮色/深色主题自适应：
- 主时间: `MaterialTheme.colorScheme.onSurface`
- 墙上时钟: `MaterialTheme.colorScheme.onSurfaceVariant`
- 时间差: `MaterialTheme.colorScheme.tertiary`
- 序号/次要信息: `MaterialTheme.colorScheme.onSurfaceVariant`
- 备注: `MaterialTheme.colorScheme.onSurface`
- 按钮背景: `MaterialTheme.colorScheme.surface`
- 按钮图标: `MaterialTheme.colorScheme.primary`

#### 间距规范
- 卡片水平边距: 12dp
- 卡片垂直间距: 8dp
- 卡片内边距: 16dp
- 时间显示区内部间距: 8dp
- 按钮大小: 80dp 直径
- 按钮间距: 80dp（spacedBy）

### 实施路线图

#### Phase 1: 基础计时功能 ✅ 已完成
1. ✅ 创建数据模型（TimeRecord, StopwatchStatus, UiState）
2. ✅ 实现 TimeFormatter 工具类
3. ✅ 创建 StopwatchViewModel 基础框架
4. ✅ 实现主界面布局（时间显示区 + 按钮区）
5. ✅ 实现基础计时逻辑（开始/暂停/继续/停止/重置）

#### Phase 2: 时间点记录 ✅ 已完成
6. ✅ 实现标记功能（瞬间记录，不中断）
7. ✅ 实现记录列表 UI（LazyColumn + RecordCard）
8. ✅ 实现列表自动滚动到顶部
9. ✅ 添加空状态显示

#### Phase 3: 备注编辑 ✅ 已完成
10. ✅ 实现编辑备注对话框（EditRecordDialog）
11. ✅ 实现点击记录卡片展开/编辑（仅暂停/停止状态可编辑）
12. ✅ 实现删除记录功能（带二次确认）

#### Phase 4: 数据持久化
13. 集成 DataStore 实现状态保存
14. 实现应用重启后状态恢复
15. 处理边缘情况（应用被杀死等）

#### Phase 5: 导出功能
16. 实现 ExportHelper 工具类
17. 实现 CSV 导出
18. 实现 JSON 导出
19. 实现 TXT 导出
20. 处理 Android 存储权限（Scoped Storage）

#### Phase 6: 优化与完善
21. 性能优化（确保毫秒级精度）
22. UI/UX 打磨（动画、过渡效果）
23. 添加单元测试
24. 添加 UI 测试
25. 完善错误处理和边缘情况

## 开发注意事项

### JDK 配置
- **Gradle JDK**: 使用 Android Studio 自带的 **JetBrains Runtime 21**
  - 设置路径: `File → Settings → Build Tools → Gradle → Gradle JDK`
  - 选择: `GRADLE_LOCAL_JAVA_HOME JetBrains Runtime 21.0.8`
- **项目编译目标**: **Java 17**
  - 已在 `app/build.gradle.kts` 中配置
  - `sourceCompatibility / targetCompatibility = JavaVersion.VERSION_17`
  - `kotlinOptions.jvmTarget = "17"`

### 开发建议
- 项目使用 Kotlin 作为主要开发语言
- Compose UI 预览需要在 Android Studio 中启用 Compose Preview
- **高精度计时**: 使用 `System.nanoTime()` 获取纳秒级精度，确保毫秒显示准确
- **时间格式化**: 使用 `java.time` API（项目最低 SDK 24 已支持，无需额外库）
- **导出功能**: 需要处理 Android 存储权限（Android 10+ 使用 Scoped Storage）
- **计时更新频率**: ViewModel 中使用协程每 10ms 更新一次计时器，确保毫秒精度显示
- **墙上时钟更新**: 独立协程每秒更新一次墙上时钟，不受秒表状态影响
- **状态管理**: 使用 StateFlow 确保 UI 状态单向数据流
- **传统秒表操作**: 标记操作必须瞬间完成，不能中断计时流程

### 技术要点

#### 高精度计时实现
```kotlin
// 使用 nanoTime() 而非 currentTimeMillis()
private var startTimeNanos: Long = 0L
private var pausedTimeNanos: Long = 0L

fun getCurrentElapsedTime(): Long {
    return System.nanoTime() - startTimeNanos - pausedTimeNanos
}
```

#### 协程计时更新
```kotlin
// 计时器更新（仅在运行时执行）
private fun startTimerTicking() {
    timerJob = viewModelScope.launch {
        while (isActive) {
            delay(10)  // 每 10ms 更新一次计时器
            updateCurrentTime()
        }
    }
}

// 墙上时钟更新（始终运行，独立于秒表状态）
private fun startWallClockTicking() {
    wallClockJob = viewModelScope.launch {
        while (isActive) {
            updateWallClock()
            delay(1000)  // 每秒更新一次墙上时钟
        }
    }
}

// 在 ViewModel 的 init 块中启动墙上时钟
init {
    startWallClockTicking()
}
```

#### 时间格式化示例
```kotlin
// 使用 java.time API

// 记录卡片时间戳格式（HH:mm:ss.SSS）
fun formatWallClock(timestampMillis: Long): String {
    val instant = Instant.ofEpochMilli(timestampMillis)
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

// 主界面墙上时钟格式（yyyy-MM-dd HH:mm:ss）
fun formatWallClockWithDate(timestampMillis: Long): String {
    val instant = Instant.ofEpochMilli(timestampMillis)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
```