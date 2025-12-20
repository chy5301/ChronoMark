# CLAUDE.md

该文件为 Claude Code（claude.ai/code）在处理本代码库时提供指导。

## 项目概述

ChronoMark 是一个基于 Jetpack Compose 的 Android 时间记录应用。**核心特色是"事件模式"——一键记录当前墙上时钟时间，无需启动计时器**，适合快速记录观察点、重要时刻或时间节点。同时提供传统秒表模式，满足不同场景需求。

应用提供**事件**和**秒表**两种独立的记录模式：

- **事件模式**（推荐）：极简时间点记录工具，无需计时器，一键记录当前墙上时钟时间（精确到毫秒），可快速添加备注。**区别于传统秒表需要"开始-标记-停止"的操作流程，事件模式随时随地一键记录**，适合科学实验观察、工作日志、学习记录、生活琐事等各类场景
- **秒表模式**：传统秒表设计，支持高精度计时（毫秒级）、完整的计时控制（开始/暂停/继续/停止/重置）、运行中瞬间标记时间点，**每个标记点不仅记录累计时间和时间差，还记录实际的墙上时钟时间**（精确到毫秒），并可为每个记录添加备注

**核心特性**：
- 一键记录：事件模式无需启动计时器，随时记录当前时刻
- 墙上时钟：不仅知道"经过了多久"，还能追溯"何时发生"
- 数据持久化：应用重启后自动恢复状态和记录
- 便捷分享：一键生成格式化文本，包含完整日期时间信息，可分享到任意应用或复制
- 实用设置：保持屏幕常亮、震动反馈等功能
- 现代设计：Material3 主题，支持亮色/深色模式

两种模式数据独立存储，互不干扰，满足不同场景的时间记录需求。

## 项目状态

**当前阶段**: Phase 7 优化与完善进行中

### 已完成功能
- ✅ 高精度计时（毫秒级，使用 nanoTime）
- ✅ 基础计时操作（开始/暂停/继续/停止/重置）
- ✅ 时间点标记功能
- ✅ 双时间显示（计时器 + 墙上时钟）
- ✅ 记录列表 UI（LazyColumn + RecordCard）
- ✅ 控制按钮 UI 优化
- ✅ 备注编辑功能（点击记录卡片编辑/删除）
- ✅ 事件模式（简化的时间点记录）
- ✅ 秒表/事件双模式切换（底部导航栏）
- ✅ 数据持久化（DataStore，应用重启后恢复状态）
- ✅ 分享与复制功能（系统分享面板，支持分享到任意应用）
- ✅ 事件模式列表滚动优化（修复进入界面闪烁问题）
- ✅ 设置页面（保持屏幕常亮、震动反馈）

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
│   ├── DataStoreManager.kt      # 数据持久化管理器
│   └── model/
│       ├── AppMode.kt           # 应用模式枚举（秒表/事件）
│       ├── TimeRecord.kt        # 时间记录数据模型
│       ├── StopwatchStatus.kt   # 秒表状态枚举
│       ├── StopwatchUiState.kt  # 秒表 UI 状态数据类
│       └── EventUiState.kt      # 事件 UI 状态数据类
├── ui/
│   ├── screen/
│   │   ├── MainScreen.kt        # 主屏幕（管理双模式切换）
│   │   ├── StopwatchScreen.kt   # 秒表主屏幕及所有 UI 组件
│   │   ├── EventScreen.kt       # 事件主屏幕及所有 UI 组件
│   │   └── SettingsScreen.kt    # 设置页面
│   └── theme/                   # Compose 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/
│   ├── TimeFormatter.kt         # 时间格式化工具类
│   ├── ShareHelper.kt           # 分享文本生成工具类
│   └── HapticFeedbackHelper.kt  # 震动反馈辅助工具类
└── viewmodel/
    ├── StopwatchViewModel.kt    # 秒表业务逻辑和状态管理
    ├── StopwatchViewModelFactory.kt  # 秒表 ViewModel 工厂
    ├── EventViewModel.kt        # 事件业务逻辑和状态管理
    └── EventViewModelFactory.kt # 事件 ViewModel 工厂
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

1. **秒表模式**: 高精度计时（毫秒级），支持开始、暂停、继续、停止、重置、标记
2. **事件模式**: 简化的时间点记录，只有记录和重置操作，无计时逻辑
3. **时间点标记**: 运行中可瞬间标记时间点，不中断计时流程
4. **双时间显示**: 同时显示累计经过时间和当前墙上时钟时间（精确到毫秒）
5. **备注功能**: 为每个时间点添加文字说明（后续手动补充）
6. **数据导出**: 支持导出为 CSV/JSON/TXT 格式，包含完整日期时间信息
7. **模式切换**: 底部导航栏切换秒表/事件模式，数据分开存储

## 详细设计规范

### 界面布局设计

#### 应用整体结构
```
┌──────────────────────────────┐
│ 顶部栏 (TopAppBar)           │  ← 标题 + 导出按钮 + 菜单按钮
├──────────────────────────────┤
│ 时间显示区 (160.dp)          │  ← 计时器/墙上时钟（固定高度）
├──────────────────────────────┤
│                              │
│ 记录列表区 (weight 1f)       │  ← LazyColumn（可滚动，占据剩余空间）
│                              │
├──────────────────────────────┤
│ 控制按钮区 (96.dp)           │  ← 操作按钮（固定高度）
├──────────────────────────────┤
│   📋事件        ⏱️秒表      │  ← 底部导航栏（NavigationBar）
└──────────────────────────────┘
```

#### 秒表模式 - 时间显示区
```
           00:14.235                ← 主计时器（60sp，加粗）
      2025-11-10 13:47:37           ← 墙上时钟（24sp，次要颜色，带日期）
```

#### 事件模式 - 时间显示区
```
         13:47:37                   ← 墙上时钟（60sp，加粗）
      2025-11-10                    ← 日期（24sp，次要颜色）
```

#### 秒表模式 - 记录卡片布局
```
┌─────────────────────────────────────┐
│ 01            00:07.403              │  ← 序号 + 累计时间
│ +00:07.403    13:47:30.474          │  ← 时间差 + 标记时刻
│ 📝 第一圈完成                        │  ← 备注（可选）
└─────────────────────────────────────┘
```

**秒表模式字段说明**：
- **序号**: 记录编号（01, 02, 03...）
- **累计时间**: 从开始到该标记点的总时长（MM:SS.mmm）
- **时间差**: 与上一个标记点的时间差（+MM:SS.mmm）
- **标记时刻**: 标记时的系统时间（HH:mm:ss.SSS）
- **备注**: 用户添加的文字说明（可选显示）
- **排列顺序**: 倒序排列（最新的在顶部），添加新记录后自动滚动到顶部

#### 事件模式 - 记录卡片布局
```
┌─────────────────────────────────────┐
│ 01                  13:47:30.474    │  ← 序号 + 标记时刻
│ 📝 第一次观察                        │  ← 备注（可选）
└─────────────────────────────────────┘
```

**事件模式字段说明**：
- **序号**: 记录编号（01, 02, 03...）
- **标记时刻**: 标记时的系统时间（HH:mm:ss.SSS）
- **备注**: 用户添加的文字说明（可选显示）
- **排列顺序**: 正序排列（最早的在顶部，最新的在底部），添加新记录后自动滚动到末尾
- **设计理念**: 极简设计，只保留最关键的时间点信息，卡片高度更小，可显示更多记录

### 操作流程设计

#### 秒表模式 - 状态流转
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

#### 秒表模式 - 标记操作
1. **点击"标记"按钮** → 立即记录当前时间点
2. 在列表顶部插入新记录卡片（带淡入动画）
3. 计时器继续运行，完全不中断
4. 列表自动滚动到顶部显示最新记录

#### 事件模式 - 操作流程
```
[始终就绪] - 左:[记录] 右:[重置]
```

**操作说明**：
- **记录按钮**: 立即记录当前时间点，无需先"开始"
- **重置按钮**: 清空所有记录（需确认）
- 点击记录卡片可编辑备注或删除

#### 备注编辑
- **任意状态下**：点击记录卡片 → 弹出编辑对话框
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

// 分享用日期格式: yyyy-MM-dd
formatShareDate(timestampMillis) -> "2025-11-10"
```

### 视觉设计规范

#### 字体大小
- 主计时器/主时钟: 60sp（加粗）
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

### 布局实现细节

#### Scaffold 嵌套处理
应用使用了嵌套 Scaffold 结构：
- **MainScreen**: 外层 Scaffold，管理 bottomBar (NavigationBar)
- **EventScreen/StopwatchScreen**: 内层 Scaffold，管理各自的 topBar (TopAppBar)

**关键实现**:
```kotlin
// MainScreen: 应用外层 Scaffold 的 paddingValues
Box(modifier = Modifier.padding(paddingValues))

// EventScreen/StopwatchScreen: 只应用顶部 padding，避免双重底部间距
Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()))
```

#### 控制按钮区域对齐
控制按钮区域（96.dp）使用 `TopCenter` 对齐，确保上下留白平衡：
- `contentAlignment = Alignment.TopCenter` - 按钮靠近顶部
- `padding(top = 4.dp)` - 保持与记录列表的小间距
- 下边留白由 MainScreen 的 bottomBar padding 自然形成

#### 自动滚动优化
**秒表模式**: 倒序排列，新记录在索引 0
```kotlin
LaunchedEffect(records.size) {
    if (records.isNotEmpty()) {
        listState.animateScrollToItem(0)  // 滚动到顶部
    }
}
```

**事件模式**: 正序排列，新记录在末尾，需避免首次加载闪烁和抖动
```kotlin
// 创建列表状态时设置初始位置，避免进入界面时"闪一下第一条"的问题
val listState = rememberLazyListState(
    initialFirstVisibleItemIndex = if (records.isNotEmpty()) records.size - 1 else 0
)

// 记录上一次的列表大小，用于判断是否新增了记录
var previousSize by remember { mutableStateOf(records.size) }

LaunchedEffect(records.size) {
    if (records.isNotEmpty()) {
        val lastIndex = records.size - 1
        
        // 只有当列表大小增加时（新增记录）才执行滚动
        if (records.size > previousSize) {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            
            // 已在底部 → 直接跳转（无动画，避免抖动）
            if (lastVisibleIndex >= lastIndex - 1) {
                listState.scrollToItem(lastIndex)
            } else {
                // 不在底部 → 使用动画滚动
                listState.animateScrollToItem(lastIndex)
            }
        }
        
        // 更新记录的大小
        previousSize = records.size
    }
}
```

**关键优化点**：
- 使用 `initialFirstVisibleItemIndex` 设置初始位置，进入界面时直接在底部，避免"从第一条闪到最后一条"
- 通过 `previousSize` 精确判断是新增记录还是进入界面/删除记录，只在新增时滚动
- 删除记录时（列表大小减少）不触发滚动，保持当前位置

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
11. ✅ 实现点击记录卡片展开/编辑（任意状态可编辑）
12. ✅ 实现删除记录功能（带二次确认）

#### Phase 4: 事件模式 ✅ 已完成
13. ✅ 实现底部导航栏（NavigationBar）切换秒表/事件模式
14. ✅ 创建 AppMode 枚举和 EventUiState 数据模型
15. ✅ 创建 EventViewModel 管理事件模式状态
16. ✅ 实现 MainScreen 管理模式切换
17. ✅ 实现事件模式时间显示区（墙上时钟 + 日期，60sp 加粗）
18. ✅ 实现事件模式记录卡片（标记时刻 + 时间差，正序排列）
19. ✅ 实现事件模式控制按钮（记录 + 重置）
20. ✅ 实现自动滚动到列表末尾
21. ✅ 两种模式数据分开存储
22. ✅ 更新 MainActivity 使用 MainScreen

#### Phase 5: 数据持久化 ✅ 已完成
23. ✅ 集成 DataStore 依赖和 Kotlinx Serialization
24. ✅ 创建 DataStoreManager 工具类
25. ✅ 为 TimeRecord 添加序列化注解
26. ✅ 实现秒表模式数据持久化（状态、时间、记录列表）
27. ✅ 实现事件模式数据持久化（记录列表）
28. ✅ 实现应用设置持久化（当前模式）
29. ✅ 在 StopwatchViewModel 中实现状态恢复逻辑
30. ✅ 在 EventViewModel 中实现状态恢复逻辑
31. ✅ 处理边缘情况（Running 状态自动转为 Paused）
32. ✅ 创建 ViewModelFactory 支持依赖注入
33. ✅ 更新所有 Screen 使用新的 ViewModelFactory

#### Phase 6: 分享与复制功能 ✅ 已完成
**目标**: 像便签应用一样，支持将记录分享到其他应用或复制到剪贴板。

**设计理念**:
- 简洁的文本格式，易读易分享，每个字段独占一行
- 日期只在开头显示一次（yyyy-MM-dd），记录中只显示时间（HH:mm:ss.SSS）
- 直接调用系统分享面板，系统已包含复制到剪贴板功能

**文本格式示例**（秒表模式）:
```
ChronoMark 秒表记录
记录时间: 2025-11-10
总用时: 00:12.055
记录数: 3
────────────────────────────────────
#01
累计: 00:07.403
差值: +00:07.403
时间: 13:47:30.474
备注: 第一圈完成

#02
累计: 00:10.553
差值: +00:03.150
时间: 13:47:33.624

#03
累计: 00:12.055
差值: +00:01.502
时间: 13:47:35.126
备注: 冲刺阶段
```

**文本格式示例**（事件模式）:
```
ChronoMark 事件记录
记录时间: 2025-11-10
记录数: 3
────────────────────────────────────
#01
时间: 13:47:30.474
备注: 第一次观察

#02
时间: 13:47:33.624

#03
时间: 13:47:35.126
备注: 记录结束
```

**核心任务**:
29. ✅ 创建 ShareHelper 工具类（生成分享文本）
30. ✅ 实现秒表模式文本格式化（每个字段独占一行）
31. ✅ 实现事件模式文本格式化（极简设计，仅保留时间和备注）
32. ✅ 在 ViewModel 中添加 generateShareText() 方法
33. ✅ 在两种模式的 TopAppBar 中启用分享按钮
34. ✅ 实现系统分享功能（直接调用 Intent.ACTION_SEND）
35. ✅ 添加空记录时的友好提示（Toast）

**UI 交互设计**:
```
点击 TopAppBar 分享按钮
    ↓
直接打开系统分享面板
    ↓
用户选择目标应用（微信/QQ/便签等）或复制到剪贴板
```

**技术要点**:
- 使用 `Intent.ACTION_SEND` + `Intent.EXTRA_TEXT` 调用系统分享面板
- 系统分享面板自带"复制到剪贴板"选项，无需单独实现
- 日期只在文本开头显示一次（yyyy-MM-dd）
- 记录中时间格式为 HH:mm:ss.SSS（不含日期）
- 每个字段独占一行，格式清晰易读
- 空记录时显示 Toast 提示"暂无记录"

#### Phase 7: 优化与完善 🔄 进行中
**目标**: 性能优化、UI/UX 打磨、测试覆盖，确保应用稳定性和用户体验。

**已完成的优化**:
- ✅ **事件模式列表滚动优化**（2025-12-14）
  - 使用 `initialFirstVisibleItemIndex` 设置列表初始位置
  - 通过 `previousSize` 精确判断新增/删除/进入界面场景
  - 修复进入界面时"从第一条闪到最后一条"的视觉问题
  - 只在新增记录时触发自动滚动，删除记录时保持当前位置

- ✅ **设置页面实现**（2025-12-14）
  - 创建 SettingsScreen 页面框架
  - 实现保持屏幕常亮功能（通过 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON）
  - 实现震动反馈功能（使用 HapticFeedback API）
  - 创建 HapticFeedbackHelper 工具类
  - 在 DataStoreManager 中添加设置项持久化
  - 设置页面支持返回键处理（BackHandler）
  - 所有按钮点击时根据设置提供震动反馈

- ✅ **代码质量优化**（2025-12-20）
  - 修复代码质量警告
  - 优化代码规范
  - 简化数据持久化 API
  - 优化设置页面实现

**待完成任务**:
38. ✅ 代码质量优化（已完成）
39. UI/UX 打磨（动画、过渡效果、深色模式优化）
40. 性能优化（大量记录场景下的列表性能、协程优化）
41. 单元测试（工具类、ViewModel、分享逻辑）
42. UI 测试（两种模式、模式切换、分享功能）
43. 错误处理和边缘情况（异常捕获、用户友好提示）
44. 文档完善（README、使用说明、CHANGELOG）
45. 发布准备（签名密钥、Release 构建、应用商店资源）

**技术要点**:
- 使用 `remember` 和 `derivedStateOf` 减少重组
- 大量记录时优化列表性能
- 添加记录插入动画（淡入效果）
- 确保深色模式下的颜色对比度

---

**📝 重要提醒**: 完成每个 Phase 后，请务必：
1. 更新 CLAUDE.md 中对应 Phase 的完成状态（添加 ✅ 标记）
2. 更新 TODO.md 中的进度追踪
3. 提交代码并打上版本标签（如 `v0.4-phase4`）

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