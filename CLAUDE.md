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
- 历史记录：事件模式自动按天归档，秒表模式手动保存会话，可配置归档分界点
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
│   ├── DataStoreManager.kt      # DataStore 管理器（应用设置 + 工作区暂存）
│   ├── database/                # Room 数据库（历史记录存储）
│   │   ├── AppDatabase.kt       # 数据库实例（单例）
│   │   ├── dao/
│   │   │   └── HistoryDao.kt    # 历史记录 DAO 接口
│   │   ├── entity/
│   │   │   ├── HistorySessionEntity.kt  # 会话表实体
│   │   │   └── TimeRecordEntity.kt      # 记录表实体
│   │   └── repository/
│   │       └── HistoryRepository.kt     # 历史数据仓库层
│   └── model/
│       ├── AppMode.kt           # 应用模式枚举（秒表/事件）
│       ├── TimeRecord.kt        # 时间记录数据模型（工作区使用）
│       ├── StopwatchStatus.kt   # 秒表状态枚举
│       ├── StopwatchUiState.kt  # 秒表 UI 状态数据类
│       ├── EventUiState.kt      # 事件 UI 状态数据类
│       ├── HistoryUiState.kt    # 历史 UI 状态数据类
│       └── SessionType.kt       # 会话类型枚举（事件/秒表）
├── ui/
│   ├── screen/
│   │   ├── MainScreen.kt        # 主屏幕（管理模式切换）
│   │   ├── StopwatchScreen.kt   # 秒表主屏幕及所有 UI 组件
│   │   ├── EventScreen.kt       # 事件主屏幕及所有 UI 组件
│   │   ├── HistoryScreen.kt     # 历史记录主屏幕（含展开/折叠会话卡片）
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
    ├── EventViewModelFactory.kt # 事件 ViewModel 工厂
    ├── HistoryViewModel.kt      # 历史记录业务逻辑和状态管理
    └── HistoryViewModelFactory.kt  # 历史 ViewModel 工厂
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
6. **分享功能**: 通过系统分享面板分享记录（格式化文本），支持分享到任意应用或复制到剪贴板
7. **模式切换**: 底部导航栏切换秒表/事件两种模式，历史记录和设置通过 TopAppBar 按钮访问，数据分开存储
8. **历史记录**: 事件模式自动按天归档，秒表模式手动保存会话，支持查看、分享、管理历史数据

## 详细设计规范

### 界面布局设计

#### 应用整体结构
```
┌──────────────────────────────┐
│ 顶部栏 (TopAppBar)           │  ← 标题 + [📤分享] [📚历史] [⚙️设置]
├──────────────────────────────┤
│ 时间显示区 (160.dp)          │  ← 计时器/墙上时钟（固定高度）
├──────────────────────────────┤
│                              │
│ 记录列表区 (weight 1f)       │  ← LazyColumn（可滚动，占据剩余空间）
│                              │
├──────────────────────────────┤
│ 控制按钮区 (96.dp)           │  ← 操作按钮（固定高度）
├──────────────────────────────┤
│      📋事件        ⏱️秒表     │  ← 底部导航栏（NavigationBar，2个标签）
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

- ✅ **深色模式实现**（2025-12-21）
  - 创建 ThemeMode 枚举（浅色/深色/跟随系统）
  - 在 DataStoreManager 中添加主题模式持久化
  - 实现主题选择对话框（RadioButton 选择）
  - 在 MainActivity 中应用主题设置
  - 支持三种主题模式切换
  - 优化对话框间距（更紧凑的布局）

- ✅ **错误处理和边缘情况分析**（2025-12-21）
  - 全面分析当前代码的错误处理情况
  - 识别 6 大类潜在问题（数据持久化、时间计算、大量数据、用户输入、分享功能、协程管理）
  - 设计完整的优化方案（优先级分级）
  - 编写实施计划（Phase 1-3）

- ✅ **DataStore 错误处理优化**（2025-12-21）
  - 为所有 save*/clear* 方法添加 try-catch 和 Result<Unit> 返回类型
  - 在 ViewModel 和 Screen 中使用 .onFailure 处理错误
  - 采用"优雅降级"策略：保存失败不影响 UI，只记录日志
  - 确保应用在数据持久化失败时不会崩溃
  - 共计修改 10 个 DataStoreManager 方法和 9 处调用点

**待完成任务**:
38. **错误处理和边缘情况优化**（优先级 1）⭐⭐⭐⭐⭐
   - ✅ DataStore 错误处理（try-catch + Result 返回）
   - ⏳ 记录数量限制（MAX_RECORDS = 1000）
   - ⏳ 备注长度限制（MAX_NOTE_LENGTH = 500）
   - ⏳ 时间计算边界检查（防止溢出和负数）
   - ⏳ Toast 提示系统（操作反馈）
   - ⏳ 分享功能异常处理
   - ⏳ 协程异常处理和取消逻辑

39. 性能优化（大量记录场景下的列表性能、协程优化）
40. 单元测试（工具类、ViewModel、分享逻辑）
41. UI 测试（两种模式、模式切换、分享功能）
42. 文档完善（CHANGELOG）
43. 发布准备（签名密钥、Release 构建、应用商店资源）

**技术要点**:
- 使用 `remember` 和 `derivedStateOf` 减少重组
- 大量记录时优化列表性能
- 添加记录插入动画（淡入效果）
- 确保深色模式下的颜色对比度

#### Phase 8: 历史记录功能 📋 待开始
**目标**: 实现自动归档和历史记录管理，让用户能够回顾和管理过往数据。

**功能概述**:
- **事件模式**: 按天自动归档（到达分界点时自动转移到历史）
- **秒表模式**: 按会话手动归档（停止后可选择保存到历史）
- **可配置分界点**: 用户可在设置中自定义归档时间点（默认凌晨 4 点）
- **历史管理**: 查看、分享、编辑标题、删除历史会话

##### 8.1 数据模型设计

**会话类型枚举**:
```kotlin
// data/model/SessionType.kt
enum class SessionType {
    EVENT,      // 事件模式
    STOPWATCH   // 秒表模式
}
```

**历史 UI 状态**:
```kotlin
// data/model/HistoryUiState.kt
data class HistoryUiState(
    val currentMode: SessionType = SessionType.EVENT,  // 当前选中模式（事件/秒表）
    val selectedDate: LocalDate = LocalDate.now(),     // 当前选中日期
    val sessions: List<HistorySessionEntity> = emptyList(),  // 当前选中日期的会话列表
    val selectedSessionRecords: List<TimeRecordEntity> = emptyList(),  // 当前会话的记录
    val currentSessionIndex: Int = 0,                  // 当前选中的会话索引（秒表模式）
    val datesWithRecords: Set<LocalDate> = emptySet(), // 有记录的日期集合（日历标记用）
    val isLoading: Boolean = false
)
```

**设计说明**：
- 直接使用 Room Entity（`HistorySessionEntity` + `TimeRecordEntity`）作为数据模型
- 避免 Entity ↔ Domain 层转换，简化架构
- ViewModel 通过 Flow 响应式查询获取数据

**设置项扩展**:
```kotlin
// 在 DataStoreManager 中添加
data class AppSettings(
    // ... 现有设置
    val archiveBoundaryHour: Int = 4,          // 归档分界点 - 时（0-23）默认凌晨 4 点
    val archiveBoundaryMinute: Int = 0,        // 归档分界点 - 分（0-59）默认 0 分
    val autoArchiveEnabled: Boolean = true     // 是否启用自动归档
)
```

##### 8.2 UI 设计

**导航结构调整**:
```
┌──────────────────────────────┐
│ [标题]    [📤] [📚] [⚙️]     │  ← TopAppBar（分享、历史、设置按钮）
├──────────────────────────────┤
│   主工作区内容               │
├──────────────────────────────┤
│      📋事件        ⏱️秒表     │  ← 底部导航栏（2 个标签）
└──────────────────────────────┘
```

**设计说明**:
- **TopAppBar 右上角按钮布局**（从左到右）：
  1. 📤 分享按钮（分享当前模式的记录）
  2. 📚 历史按钮（打开历史记录页面）
  3. ⚙️ 设置按钮（打开设置页面）

- **底部导航栏**（2 个标签）：
  1. 📋 事件模式
  2. ⏱️ 秒表模式

- **导航逻辑**：
  - 底部导航栏：事件/秒表模式切换
  - TopAppBar 按钮：分享/历史/设置（独立页面，支持返回）
  - 设计理念：核心功能（事件/秒表）在底部，辅助功能（分享/历史/设置）在顶部

**历史记录页面布局**（与主页保持一致的结构）:

**事件模式**（点击底部"事件"标签）:
```
┌──────────────────────────────┐
│ 历史记录    [📤] [🏠] [⚙️]   │  ← TopAppBar（分享/主页/设置按钮）
├──────────────────────────────┤
│      [<] 2025-12-11 [>]      │  ← 日期选择区（160.dp，点击日期打开日历）
│         周三 · 共3条         │  ← 星期 + 记录统计
├──────────────────────────────┤
│                              │
│ 记录列表区 (weight 1f)       │  ← LazyColumn（可滚动）
│                              │
│ #01  09:23:15.234           │  ← 事件记录（复用主页 RecordCard）
│ 📝 早会开始                  │
│                              │
│ #02  10:15:42.891           │
│ 📝 完成第一个任务            │
│                              │
│ #03  14:30:25.156           │
│ 📝 下午会议                  │
│                              │
├──────────────────────────────┤
│        [🗑️ 删除当天]         │  ← 控制按钮区（96.dp）
├──────────────────────────────┤
│      📋事件        ⏱️秒表     │  ← 底部导航栏（当前选中：事件）
└──────────────────────────────┘
```

**秒表模式**（点击底部"秒表"标签）:
```
┌──────────────────────────────┐
│ 历史记录    [📤] [🏠] [⚙️]   │  ← TopAppBar（分享/主页/设置）
├──────────────────────────────┤
│      [<] 2025-12-11 [>]      │  ← 日期选择区（点击日期打开日历）
│         周三 · 2个会话       │  ← 星期 + 会话统计
├──────────────────────────────┤
│   [<] 晨跑计时 (1/2) [>]     │  ← 会话选择器（80.dp，点击标题打开会话列表）
│      06:15 · 用时 28:35      │  ← 开始时间 + 总用时
├──────────────────────────────┤
│                              │
│ 记录列表区 (weight 1f)       │  ← LazyColumn（只显示当前会话）
│                              │
│ #01  06:15:23.125           │  ← 复用主页 RecordCard
│      00:07.403              │  ← 点击记录可编辑备注/删除
│      +00:07.403             │
│ 📝 热身完成                  │
│                              │
│ #02  06:22:45.607           │
│      07:30.082              │
│      +07:22.679             │
│ 📝 第一圈                    │
│                              │
│ #03  06:30:18.743           │
│      15:03.218              │
│      +07:33.136             │
│                              │
├──────────────────────────────┤
│    [✏️ 编辑标题] [🗑️ 删除]   │  ← 控制按钮区（96.dp，两个按钮）
├──────────────────────────────┤
│      📋事件        ⏱️秒表     │  ← 底部导航栏（当前选中：秒表）
└──────────────────────────────┘
```

**日历选择器对话框**（点击"选择日期"打开）:
```
┌──────────────────────────────┐
│ 选择日期            [✕]      │
├──────────────────────────────┤
│   [< 2025年12月 >]           │  ← 月份切换
│                              │
│   日 一 二 三 四 五 六       │
│   1  2  3• 4  5  6  7        │  ← 有记录的日期标记小圆点
│   8  9 10•11 12 13 14        │  ← 当前选中日期高亮
│  15 16 17 18 19 20 21        │
│  22 23 24 25 26 27 28        │
│  29 30 31                    │
├──────────────────────────────┤
│        [取消]    [确定]      │
└──────────────────────────────┘
```

**会话选择列表对话框**（秒表模式，点击会话选择器标题打开）:
```
┌──────────────────────────────┐
│ 选择会话            [✕]      │
├──────────────────────────────┤
│ ○ 晨跑计时                   │  ← 第1个会话（有标题）
│   06:15 · 用时 28:35         │
│                              │
│ ● 会话 2                     │  ← 第2个会话（当前选中，无标题）
│   08:30 · 用时 15:20         │
│                              │
│ ○ 午后锻炼                   │  ← 第3个会话
│   14:45 · 用时 42:18         │
├──────────────────────────────┤
│            [确定]            │
└──────────────────────────────┘
```

**设计说明**:

- **整体布局**：
  - 与事件/秒表页面保持完全一致的结构
  - 复用相同的布局组件和高度设置
  - 确保视觉和交互的连贯性

- **TopAppBar 右上角按钮**（从左到右，两种模式相同）：
  1. 📤 分享按钮
     - 事件模式：分享该天的所有事件记录
     - 秒表模式：分享当前选中的会话
  2. 🏠 主页按钮 - 退出历史记录，返回主页
  3. ⚙️ 设置按钮 - 打开设置页面（功能与主页相同）

- **日期选择区**（160.dp，对应主页的时间显示区）：
  - **大号日期显示**：2025-12-11（60sp，加粗）
  - **左右箭头**：[<] 上一天、[>] 下一天
  - **副标题**：星期 + 记录统计（24sp，次要颜色）
  - **点击交互**：
    - 点击日期（中间区域）：打开日历选择器对话框
    - 点击左箭头：切换到上一天
    - 点击右箭头：切换到下一天
  - **视觉反馈**：日期区域有点击涟漪效果，提示可点击

- **日历选择器对话框**：
  - 月份选择器：[<] [YYYY年MM月] [>]
  - 日历视图：有记录的日期显示小圆点
  - 当前选中日期高亮显示
  - 点击日期 → 关闭对话框 → 刷新记录列表

- **会话选择器**（80.dp，秒表模式专用）：
  - **会话标题显示**：晨跑计时 (1/2)（40sp，加粗）
    - 标题格式："标题 (X/Y)"或"会话 X/Y"（无自定义标题时）
    - X 为当前会话序号，Y 为该日期的总会话数
    - 标题可点击，打开会话选择列表
  - **左右箭头**：[<] 上一个会话、[>] 下一个会话（快速切换）
  - **副标题**：开始时间 + 总用时（20sp，次要颜色）
    - 格式："HH:mm · 用时 MM:SS"（例如："06:15 · 用时 28:35"）
  - **点击交互**：
    - 点击会话标题：打开会话选择列表对话框，显示当天所有会话
    - 点击左箭头：切换到上一个会话，列表刷新
    - 点击右箭头：切换到下一个会话，列表刷新
  - **视觉反馈**：标题区域有点击涟漪效果
  - **只在秒表模式显示**：事件模式不显示此区域

- **会话选择列表对话框**（秒表模式专用）：
  - 显示当天所有会话的列表
  - 每个会话项显示：
    - 单选按钮（标记当前选中）
    - 会话标题（有标题显示标题，无标题显示"会话 X"）
    - 开始时间 + 总用时
  - 点击会话项：选中该会话
  - 点击确定：关闭对话框，切换到选中会话，刷新记录列表
  - 点击关闭/取消：关闭对话框，不切换会话

- **记录列表区**（weight 1f）：
  - 通过底部导航栏切换显示模式（与主页逻辑完全一致）
  - **事件模式**（点击底部"事件"标签）：
    - 只显示该天的所有事件记录
    - 复用主页的 `RecordCard` 组件
    - 点击记录可编辑备注或删除
    - 副标题显示："周三 · 共3条"
  - **秒表模式**（点击底部"秒表"标签）：
    - 一次只显示一个会话的记录列表
    - 通过会话选择器切换查看不同会话
    - 复用主页的 `RecordCard` 组件
    - **点击记录卡片**：弹出编辑对话框
      - 可编辑该条记录的备注
      - 可删除该条记录（需确认）
      - 与主页的编辑逻辑完全一致
    - 副标题显示："周三 · 2个会话"

- **控制按钮区**（96.dp，对应主页的控制按钮区）：
  - **事件模式**：显示删除当天记录按钮
    - [🗑️ 删除当天] 按钮：删除当前日期的所有事件记录（需确认对话框）
    - 按钮样式与主页的控制按钮一致（80dp 直径圆形按钮）
    - 居中显示
  - **秒表模式**：显示两个按钮（编辑标题 + 删除会话）
    - [✏️ 编辑标题] 按钮（左）：弹出对话框编辑当前会话标题（需确认对话框）
    - [🗑️ 删除] 按钮（右）：删除当前会话的所有记录（需确认对话框）
    - 按钮样式与主页的控制按钮一致（80dp 直径圆形按钮）
    - 两个按钮水平排列，居中显示，间距 16dp
  - 确保与主页布局高度一致

- **底部导航栏**（2个标签）：
  - 📋 事件：切换到事件模式，只显示事件记录
  - ⏱️ 秒表：切换到秒表模式，只显示秒表会话
  - 切换逻辑与主页完全一致（切换后保持当前选中日期）
  - 切换时刷新记录列表和副标题统计

- **分享功能**：
  - 事件模式：分享该天的所有事件记录
  - 秒表模式：分享当前选中的会话（不是该天所有会话）

- **返回键行为**：
  - 按系统返回键：退出历史记录，返回主页

**交互细节**:
- **进入历史记录**：
  - 从事件模式点击历史按钮 → 进入事件模式的历史记录
  - 从秒表模式点击历史按钮 → 进入秒表模式的历史记录
  - 保持主页的模式状态（上下文连贯）
- **模式切换**：点击底部导航栏切换事件/秒表模式（与主页完全一致）
- **日期切换**：左右箭头切换时，列表淡入淡出动画
- **副标题更新**：
  - 事件模式："周三 · 共3条"
  - 秒表模式："周三 · 2个会话"
  - 切换模式或日期时自动更新
- **空状态显示**：
  - 事件模式无记录："该日期暂无事件记录"
  - 秒表模式无记录："该日期暂无秒表记录"
- **默认日期**：默认显示今天的日期（如果今天无记录则显示最近有记录的日期）
- **月份自动跳转**：点击箭头切换日期时，如果跨月则日历选择器自动切换月份
- **记录统计**：只统计当前选中模式的数据（不混合显示）

##### 8.3 自动归档逻辑

**触发时机**:
```kotlin
// 在 MainActivity 的 onResume 检查
fun checkAndArchiveIfNeeded() {
    val now = LocalDateTime.now()
    val currentTime = now.hour * 60 + now.minute  // 转换为分钟数
    val boundaryTime = settings.archiveBoundaryHour * 60 + settings.archiveBoundaryMinute
    val lastCheckDate = dataStore.getLastArchiveCheckDate()
    val today = LocalDate.now()

    if (shouldArchive(lastCheckDate, today, currentTime, boundaryTime)) {
        archiveCurrentRecords()
        dataStore.saveLastArchiveCheckDate(today)
    }
}
```

**跨天判断逻辑**:
```kotlin
fun shouldArchive(
    lastCheckDate: String,
    currentDate: LocalDate,
    currentTimeInMinutes: Int,      // 当前时间（分钟数，0-1439）
    boundaryTimeInMinutes: Int      // 分界点时间（分钟数，0-1439）
): Boolean {
    // 首次使用，初始化为当前日期，不触发归档
    if (lastCheckDate.isEmpty()) {
        dataStore.saveLastArchiveCheckDate(currentDate.toString())
        return false
    }

    val lastDate = LocalDate.parse(lastCheckDate)

    // 日期变化且已过分界点
    if (currentDate.isAfter(lastDate)) {
        return currentTimeInMinutes >= boundaryTimeInMinutes
    }
    return false
}
```

**设计说明**：
- **首次使用处理**：当 `lastCheckDate` 为空时，自动初始化为当前日期，避免首次跨天时归档失败
- **分界点判断**：只有在日期变化且当前时间已过分界点时才触发归档
- **防重复归档**：通过更新 `lastCheckDate` 确保同一天只归档一次

**归档操作**:
```kotlin
// EventViewModel - 自动归档
suspend fun autoArchive() {
    val records = _uiState.value.records
    if (records.isEmpty()) return

    // 1. 归档到 Room 数据库
    historyRepository.archiveEventRecords(records)
        .onSuccess {
            // 2. 清空 DataStore 工作区
            dataStoreManager.clearEventRecords()

            // 3. 更新 UI 状态
            _uiState.update { it.copy(records = emptyList()) }

            // 4. 提示用户
            _toastMessage.value = "昨日记录已归档（${records.size} 条）"
        }
        .onFailure { e ->
            Log.e(TAG, "Archive failed", e)
            _toastMessage.value = "归档失败，请重试"
        }
}

// StopwatchViewModel - 生成默认标题
fun getDefaultTitle(): String {
    // 生成默认标题：基于开始时间 "会话 HH:mm"
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val time = Instant.ofEpochMilli(startWallClockTime)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    return "会话 $time"
}

// StopwatchViewModel - 手动归档
suspend fun saveToHistory(title: String) {
    val records = _uiState.value.records
    if (records.isEmpty()) return

    // 1. 归档到 Room 数据库
    historyRepository.archiveStopwatchRecords(
        records = records,
        title = title,
        startTime = startWallClockTime,
        totalElapsedNanos = _uiState.value.currentTimeNanos
    )
        .onSuccess {
            // 2. 清空 DataStore 工作区
            dataStoreManager.clearStopwatchRecords()

            // 3. 重置状态
            reset()

            // 4. 提示用户
            _toastMessage.value = "已保存到历史记录"
        }
        .onFailure { e ->
            Log.e(TAG, "Save to history failed", e)
            _toastMessage.value = "保存失败，请重试"
        }
}
```

##### 8.4 两种模式的差异对比

| 维度 | 事件模式 | 秒表模式 |
|------|---------|---------|
| **归档方式** | 自动归档（到达分界点） | 手动归档（停止后询问） |
| **归档单位** | 天（一天一个会话） | 会话（一次计时一个会话） |
| **标题** | 无标题字段 | 有默认标题（"会话 HH:mm"），可修改 |
| **标题编辑** | 不支持 | 支持（保存时确认/修改 + 历史记录中编辑） |
| **显示信息** | 日期 + 记录数 | 标题 + 开始时间 + 总用时 |
| **分组** | 按日期 | 按会话（一天可能多个） |

##### 8.5 设置页面新增项

```
┌──────────────────────────────┐
│ [<] 设置                     │
├──────────────────────────────┤
│ ... 现有设置项 ...           │
│                              │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│ 历史记录                     │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│                              │
│ 自动归档               [✓]   │  ← 开关
│ 在分界点自动将事件记录归档到历史
│                              │
│ 归档分界点             [04:00]│  ← 时间选择器（时:分）
│ 跨过此时间点时执行归档        │
│                              │
│ 历史记录保留            [365天]│  ← 时长选择器
│ 超过时长的记录将被自动删除    │
│                              │
│ 清空所有历史记录             │  ← 危险操作（红色）
└──────────────────────────────┘
```

**分界点时间选择器**（点击打开对话框）:
```
┌──────────────────────────────┐
│ 选择归档时间点       [✕]     │
├──────────────────────────────┤
│                              │
│      ┌────┐   :   ┌────┐    │
│      │ 03 │        │ 45 │    │  ← 可滚动选择器
│      │ 04 │   :    │ 00 │    │  ← 当前选中（高亮）
│      │ 05 │        │ 15 │    │
│      └────┘        └────┘    │
│       时            分        │
│                              │
│ 推荐：凌晨 4:00（避免日常活动时间）
├──────────────────────────────┤
│        [取消]    [确定]      │
└──────────────────────────────┘
```

**设计说明**:
- **滚动选择器**：时（0-23）和分（0-59）独立滚动
- **默认值**：04:00（凌晨 4 点）
- **显示格式**：HH:mm（24 小时制，补零）
- **交互方式**：点击设置项打开对话框，滚动选择后确定

---

**历史记录保留时长选择器**（点击打开对话框）:
```
┌──────────────────────────────┐
│ 历史记录保留时长     [✕]     │
├──────────────────────────────┤
│                              │
│ ○ 30 天                      │
│   节省存储空间               │
│                              │
│ ○ 90 天                      │
│   保留最近三个月             │
│                              │
│ ○ 180 天                     │
│   保留半年记录               │
│                              │
│ ● 365 天（推荐）             │  ← 默认选中
│   保留一年记录               │
│                              │
│ ○ 永久保留                   │
│   不自动删除（需手动清理）   │
│                              │
├──────────────────────────────┤
│        [取消]    [确定]      │
└──────────────────────────────┘
```

**设计说明**:
- **单选列表**：使用 RadioButton 选择
- **预设选项**：30天、90天、180天、365天（推荐）、永久保留
- **默认值**：365 天
- **存储值**：
  - 30/90/180/365：对应天数
  - 永久保留：存储为 -1 或 Int.MAX_VALUE
- **清理逻辑**：
  - 应用启动时自动清理超过时长的记录
  - 永久保留时不执行自动清理
- **说明文案**：每个选项附带简短说明

##### 8.6 用户交互流程

**事件模式自动归档**:
```
用户使用事件模式记录
    ↓
时间跨过分界点（如 04:00）
    ↓
下次打开 App 检测到跨天
    ↓
Toast 提示："昨日记录已归档（5 条）"
    ↓
主工作区清空，准备记录新一天
    ↓
可在"历史"标签查看昨天的记录
```

**秒表模式手动归档**:
```
完成秒表计时并停止
    ↓
显示对话框："保存到历史记录？"
    ├─ [保存] → 输入标题对话框
    │            - 输入框预填充默认标题："会话 HH:mm"（基于开始时间）
    │            - 用户可直接确认使用默认标题
    │            - 或修改成自定义标题
    │            → 保存到历史
    └─ [不保存] → 直接重置
```

**查看和管理历史**:
```
在主页点击 TopAppBar "历史"按钮
    ↓
进入历史记录页面（自动保持当前模式）
    ├─ 从事件模式进入 → 显示事件历史
    └─ 从秒表模式进入 → 显示秒表历史
    ↓
[可选] 通过底部导航栏切换模式
    ↓
选择日期
    ├─ 点击左右箭头：切换上一天/下一天
    └─ 点击日期：打开日历选择器
    ↓
查看该日期的记录
    ├─ [事件模式] 直接显示记录列表
    │   - 点击记录：编辑备注或删除
    │   - 点击控制按钮区"删除当天"：删除该天所有事件记录
    │   - 点击分享：分享该天的所有事件记录
    │
    └─ [秒表模式] 使用会话选择器切换查看不同会话
        - 会话选择器：显示标题 (X/Y)
          · 点击标题：打开会话选择列表（显示当天所有会话）
          · 左右箭头：快速切换上一个/下一个会话
        - 记录列表：一次只显示当前选中会话的记录
        - 记录操作：点击单条记录编辑备注或删除
        - 控制按钮区：
          · 编辑标题：修改当前会话标题（弹出对话框）
          · 删除：删除当前会话的所有记录
        - 点击分享：分享当前选中的会话（不是该天所有会话）
    ↓
按返回键 / 点击主页按钮 → 返回主页（保持历史页面的模式）
```

##### 8.7 数据存储策略

**存储架构**：采用 **DataStore + Room** 分层存储方案

```
┌─────────────────────────────────────────┐
│ DataStore Preferences                   │  ← 轻量级键值对存储
├─────────────────────────────────────────┤
│ • 应用设置（主题、震动、归档配置等）     │
│ • 秒表工作区（状态 + 当前记录）         │
│ • 事件工作区（当前记录）                │
└─────────────────────────────────────────┘
                  ↓ 归档操作
┌─────────────────────────────────────────┐
│ Room Database                           │  ← 关系型数据库
├─────────────────────────────────────────┤
│ • history_sessions 表（会话元数据）     │
│ • time_records 表（历史记录）           │
└─────────────────────────────────────────┘
```

---

#### DataStore 键值设计

**用途**：存储应用设置和工作区暂存数据

```kotlin
object PreferencesKeys {
    // 归档设置
    val ARCHIVE_BOUNDARY_HOUR = intPreferencesKey("archive_boundary_hour")
    val ARCHIVE_BOUNDARY_MINUTE = intPreferencesKey("archive_boundary_minute")
    val AUTO_ARCHIVE_ENABLED = booleanPreferencesKey("auto_archive_enabled")
    val LAST_ARCHIVE_CHECK_DATE = stringPreferencesKey("last_archive_check_date")
    val HISTORY_RETENTION_DAYS = intPreferencesKey("history_retention_days")  // 保留天数（-1表示永久）

    // 秒表工作区（继续使用现有键）
    val STOPWATCH_STATUS = stringPreferencesKey("stopwatch_status")
    val STOPWATCH_RECORDS = stringPreferencesKey("stopwatch_records")
    // ... 其他秒表状态

    // 事件工作区（继续使用现有键）
    val EVENT_RECORDS = stringPreferencesKey("event_records")
}
```

**特点**：
- ✅ 数据量小（< 100 条记录）
- ✅ 读写频繁，毫秒级响应
- ✅ 应用重启快速恢复

---

#### Room 数据库设计

**用途**：存储已归档的历史会话和记录

##### 1. 数据库实体

**会话表**（存储会话元数据）：
```kotlin
// data/database/entity/HistorySessionEntity.kt
@Entity(
    tableName = "history_sessions",
    indices = [
        Index(value = ["date"]),           // 按日期查询
        Index(value = ["session_type"])    // 按类型筛选
    ]
)
data class HistorySessionEntity(
    @PrimaryKey
    val id: String,

    val date: String,                      // "yyyy-MM-dd"

    @ColumnInfo(name = "session_type")
    val sessionType: SessionType,          // ← 直接使用 enum（通过 TypeConverter 转换）

    val title: String,                     // 会话标题（秒表专用）

    @ColumnInfo(name = "created_at")
    val createdAt: Long,                   // 创建时间戳

    // 秒表专用字段
    @ColumnInfo(name = "total_elapsed_nanos")
    val totalElapsedNanos: Long = 0L,      // 总用时（纳秒）

    @ColumnInfo(name = "start_time")
    val startTime: Long = 0L,              // 会话开始时间

    @ColumnInfo(name = "end_time")
    val endTime: Long = 0L                 // 会话结束时间
)
```

**记录表**（存储时间记录详情）：
```kotlin
// data/database/entity/TimeRecordEntity.kt
@Entity(
    tableName = "time_records",
    foreignKeys = [
        ForeignKey(
            entity = HistorySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE  // 级联删除
        )
    ],
    indices = [Index("session_id")]        // 加速关联查询
)
data class TimeRecordEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,                 // 关联会话 ID

    val index: Int,                        // 记录序号

    @ColumnInfo(name = "wall_clock_time")
    val wallClockTime: Long,               // 标记时刻（毫秒）

    @ColumnInfo(name = "elapsed_time_nanos")
    val elapsedTimeNanos: Long,            // 累计时间（纳秒）

    @ColumnInfo(name = "split_time_nanos")
    val splitTimeNanos: Long,              // 时间差（纳秒）

    val note: String                       // 备注
)
```

**类型转换器**（SessionType ↔ String）：
```kotlin
// data/database/Converters.kt
import androidx.room.TypeConverter
import io.github.chy5301.chronomark.data.model.SessionType

class Converters {
    @TypeConverter
    fun fromSessionType(value: SessionType): String {
        return value.name  // EVENT → "EVENT", STOPWATCH → "STOPWATCH"
    }

    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return SessionType.valueOf(value)  // "EVENT" → EVENT
    }
}
```

**设计说明**：
- Room 只支持基本类型（Int、String 等），不支持 enum
- TypeConverter 让我们在代码中使用 enum，数据库中存储 String
- 转换过程对开发者透明，Room 自动调用
- 数据库中实际存储："EVENT"、"STOPWATCH"（字符串）
- 代码中使用：`SessionType.EVENT`、`SessionType.STOPWATCH`（enum）

##### 2. DAO 接口

```kotlin
@Dao
interface HistoryDao {

    // ========== 查询操作 ==========

    /**
     * 查询指定日期的所有会话（用于历史页面）
     */
    @Transaction
    @Query("""
        SELECT * FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
        ORDER BY created_at ASC
    """)
    fun getSessionsByDate(date: String, sessionType: SessionType): Flow<List<HistorySessionEntity>>

    /**
     * 查询指定会话的所有记录
     */
    @Query("""
        SELECT * FROM time_records
        WHERE session_id = :sessionId
        ORDER BY index ASC
    """)
    fun getRecordsBySessionId(sessionId: String): Flow<List<TimeRecordEntity>>

    /**
     * 查询包含记录的日期列表（用于日历标记）
     */
    @Query("""
        SELECT DISTINCT date FROM history_sessions
        WHERE session_type = :sessionType
        ORDER BY date DESC
    """)
    fun getDatesWithRecords(sessionType: SessionType): Flow<List<String>>

    /**
     * 查询指定日期的会话数量
     */
    @Query("""
        SELECT COUNT(*) FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
    """)
    suspend fun getSessionCountByDate(date: String, sessionType: SessionType): Int

    /**
     * 查询指定会话的记录数量
     */
    @Query("""
        SELECT COUNT(*) FROM time_records
        WHERE session_id = :sessionId
    """)
    suspend fun getRecordCountBySessionId(sessionId: String): Int

    // ========== 插入操作 ==========

    /**
     * 插入会话（归档时使用）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HistorySessionEntity)

    /**
     * 批量插入记录（归档时使用）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TimeRecordEntity>)

    /**
     * 归档会话（事务操作，保证原子性）
     */
    @Transaction
    suspend fun archiveSession(session: HistorySessionEntity, records: List<TimeRecordEntity>) {
        insertSession(session)
        insertRecords(records)
    }

    // ========== 更新操作 ==========

    /**
     * 更新会话标题（秒表模式编辑标题）
     */
    @Query("UPDATE history_sessions SET title = :title WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String)

    /**
     * 更新记录备注（编辑单条记录）
     */
    @Query("UPDATE time_records SET note = :note WHERE id = :recordId")
    suspend fun updateRecordNote(recordId: String, note: String)

    // ========== 删除操作 ==========

    /**
     * 删除指定会话（级联删除所有记录）
     */
    @Query("DELETE FROM history_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    /**
     * 删除指定日期和类型的会话（通用方法）
     */
    @Query("""
        DELETE FROM history_sessions
        WHERE date = :date AND session_type = :sessionType
    """)
    suspend fun deleteSessionsByDateAndType(date: String, sessionType: SessionType)

    /**
     * 删除单条记录
     */
    @Query("DELETE FROM time_records WHERE id = :recordId")
    suspend fun deleteRecord(recordId: String)

    /**
     * 清空所有历史记录（设置页面危险操作）
     */
    @Query("DELETE FROM history_sessions")
    suspend fun deleteAllSessions()

    /**
     * 删除指定日期之前的旧数据（自动清理）
     */
    @Query("DELETE FROM history_sessions WHERE date < :beforeDate")
    suspend fun deleteSessionsBeforeDate(beforeDate: String)
}
```

##### 3. Database 类

```kotlin
// data/database/AppDatabase.kt
@Database(
    entities = [
        HistorySessionEntity::class,
        TimeRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)  // ← 注册类型转换器
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chronomark_database"
                )
                    .fallbackToDestructiveMigration()  // 开发阶段可用
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

##### 4. Repository 层

```kotlin
class HistoryRepository(
    private val historyDao: HistoryDao,
    private val dataStoreManager: DataStoreManager
) {

    /**
     * 归档事件模式记录
     */
    suspend fun archiveEventRecords(records: List<TimeRecord>): Result<Unit> {
        return try {
            if (records.isEmpty()) {
                return Result.failure(Exception("No records to archive"))
            }

            val yesterday = LocalDate.now().minusDays(1).toString()
            val session = HistorySessionEntity(
                id = UUID.randomUUID().toString(),
                date = yesterday,
                sessionType = SessionType.EVENT,  // ← 使用 enum
                title = "",
                createdAt = System.currentTimeMillis()
            )

            val recordEntities = records.map { record ->
                TimeRecordEntity(
                    id = record.id,
                    sessionId = session.id,
                    index = record.index,
                    wallClockTime = record.wallClockTime,
                    elapsedTimeNanos = record.elapsedTimeNanos,
                    splitTimeNanos = record.splitTimeNanos,
                    note = record.note
                )
            }

            historyDao.archiveSession(session, recordEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 归档秒表模式记录
     */
    suspend fun archiveStopwatchRecords(
        records: List<TimeRecord>,
        title: String,
        startTime: Long,
        totalElapsedNanos: Long
    ): Result<Unit> {
        return try {
            if (records.isEmpty()) {
                return Result.failure(Exception("No records to archive"))
            }

            val today = LocalDate.now().toString()
            val session = HistorySessionEntity(
                id = UUID.randomUUID().toString(),
                date = today,
                sessionType = SessionType.STOPWATCH,  // ← 使用 enum
                title = title,
                createdAt = startTime,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                totalElapsedNanos = totalElapsedNanos
            )

            val recordEntities = records.map { record ->
                TimeRecordEntity(
                    id = record.id,
                    sessionId = session.id,
                    index = record.index,
                    wallClockTime = record.wallClockTime,
                    elapsedTimeNanos = record.elapsedTimeNanos,
                    splitTimeNanos = record.splitTimeNanos,
                    note = record.note
                )
            }

            historyDao.archiveSession(session, recordEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 查询指定日期的会话列表
     */
    fun getSessionsByDate(date: String, sessionType: SessionType): Flow<List<HistorySessionEntity>> {
        return historyDao.getSessionsByDate(date, sessionType)
    }

    /**
     * 查询指定会话的记录列表
     */
    fun getRecordsBySessionId(sessionId: String): Flow<List<TimeRecordEntity>> {
        return historyDao.getRecordsBySessionId(sessionId)
    }

    /**
     * 自动清理旧数据（根据用户设置的保留天数）
     *
     * @param retentionDays 保留天数（-1 表示永久保留，不执行清理）
     * @return Result<Unit> 成功或失败结果
     */
    suspend fun cleanupOldData(retentionDays: Int): Result<Unit> {
        return try {
            // 永久保留或无效值（防止整型溢出）
            if (retentionDays < 0 || retentionDays > 36500) {
                return Result.success(Unit)
            }

            val cutoffDate = LocalDate.now()
                .minusDays(retentionDays.toLong())
                .toString()

            historyDao.deleteSessionsBeforeDate(cutoffDate)
            Log.i(TAG, "Cleaned up data before $cutoffDate")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old data", e)
            Result.failure(e)
        }
    }
}
```

---

#### 数据流转流程

**事件模式自动归档**：
```kotlin
// EventViewModel.kt
suspend fun autoArchive() {
    val records = _uiState.value.records
    if (records.isEmpty()) return

    // 1. 归档到 Room
    historyRepository.archiveEventRecords(records)
        .onSuccess {
            // 2. 清空 DataStore 工作区
            dataStoreManager.clearEventRecords()

            // 3. 更新 UI 状态
            _uiState.update { it.copy(records = emptyList()) }

            // 4. 提示用户
            _toastMessage.value = "昨日记录已归档（${records.size} 条）"
        }
        .onFailure { e ->
            Log.e(TAG, "Archive failed", e)
            _toastMessage.value = "归档失败，请重试"
        }
}
```

**秒表模式手动归档**：
```kotlin
// StopwatchViewModel.kt
suspend fun saveToHistory(title: String) {
    val records = _uiState.value.records
    if (records.isEmpty()) return

    // 1. 归档到 Room
    historyRepository.archiveStopwatchRecords(
        records = records,
        title = title,
        startTime = startWallClockTime,
        totalElapsedNanos = _uiState.value.currentTimeNanos
    )
        .onSuccess {
            // 2. 清空 DataStore 工作区
            dataStoreManager.clearStopwatchRecords()

            // 3. 重置状态
            reset()

            // 4. 提示用户
            _toastMessage.value = "已保存到历史记录"
        }
        .onFailure { e ->
            Log.e(TAG, "Save to history failed", e)
            _toastMessage.value = "保存失败，请重试"
        }
}
```

---

#### 性能优化策略

1. **索引优化**：
   - `date` 字段索引：加速按日期查询
   - `session_type` 字段索引：加速按类型筛选
   - `session_id` 外键索引：加速关联查询

2. **分页加载**：
   - 使用 Jetpack Paging 3 库
   - 按需加载，避免一次性读取所有数据

3. **Flow 响应式查询**：
   - 自动监听数据变化
   - UI 自动更新，无需手动刷新

4. **自动清理**：
   - 应用启动时检查并删除过期数据（根据用户设置的保留天数）
   - 保持数据库大小可控
   - 清理失败不影响应用启动（优雅降级）
   - 示例代码：
     ```kotlin
     // MainActivity.onCreate
     lifecycleScope.launch {
         val retentionDays = dataStoreManager.getHistoryRetentionDays()
         historyRepository.cleanupOldData(retentionDays)
             .onSuccess {
                 Log.i(TAG, "Old data cleanup completed")
             }
             .onFailure { e ->
                 // 清理失败不影响应用启动，只记录日志
                 Log.w(TAG, "Cleanup failed but app continues", e)
             }
     }
     ```

5. **事务保证**：
   - 归档操作使用事务（`@Transaction`）
   - 保证会话和记录一起成功或失败

##### 8.8 核心任务清单

1. **Room 数据库搭建**（优先级：⭐⭐⭐⭐⭐）
   - [ ] 添加 Room 依赖到 `build.gradle.kts`
     ```kotlin
     implementation("androidx.room:room-runtime:2.6.1")
     implementation("androidx.room:room-ktx:2.6.1")
     ksp("androidx.room:room-compiler:2.6.1")
     ```
   - [ ] 创建 `HistorySessionEntity` 实体类
     - [ ] 定义表结构和字段
     - [ ] 添加索引（date, session_type）
   - [ ] 创建 `TimeRecordEntity` 实体类
     - [ ] 定义表结构和字段
     - [ ] 添加外键关联（级联删除）
     - [ ] 添加索引（session_id）
   - [ ] 创建 `HistoryDao` 接口
     - [ ] 查询操作（按日期、按会话、统计）
     - [ ] 插入操作（归档会话和记录）
     - [ ] 更新操作（编辑标题、备注）
     - [ ] 删除操作（删除会话、记录、清空历史）
   - [ ] 创建 `AppDatabase` 类
     - [ ] 配置数据库（version = 1）
     - [ ] 实现单例模式
   - [ ] 创建 `HistoryRepository` 类
     - [ ] 归档事件记录方法
     - [ ] 归档秒表记录方法
     - [ ] 查询历史数据方法
     - [ ] 自动清理旧数据方法

2. **DataStore 扩展**（优先级：⭐⭐⭐⭐⭐）
   - [ ] 添加归档设置键值
     - [ ] `ARCHIVE_BOUNDARY_HOUR`
     - [ ] `ARCHIVE_BOUNDARY_MINUTE`
     - [ ] `AUTO_ARCHIVE_ENABLED`
     - [ ] `LAST_ARCHIVE_CHECK_DATE`
     - [ ] `HISTORY_RETENTION_DAYS`（默认 365，-1 表示永久）
   - [ ] 添加保存/读取归档设置方法
     - [ ] `getHistoryRetentionDays()` / `saveHistoryRetentionDays(days: Int)`
   - [ ] 添加清空工作区方法
     - [ ] `clearEventRecords()`
     - [ ] `clearStopwatchRecords()`

3. **自动归档逻辑**（优先级：⭐⭐⭐⭐⭐）
   - [ ] 实现跨天检测（`shouldArchive`）
     - [ ] 使用分钟级时间比较（0-1439）
     - [ ] 检查 `LAST_ARCHIVE_CHECK_DATE`
   - [ ] `EventViewModel` 添加自动归档方法
     - [ ] 归档到 Room 数据库
     - [ ] 清空 DataStore 工作区
     - [ ] Toast 提示用户
   - [ ] `MainActivity` 启动时检查归档
     - [ ] `onResume` 调用 `checkAndArchiveIfNeeded`
     - [ ] 更新 `LAST_ARCHIVE_CHECK_DATE`

4. **设置页面**（优先级：⭐⭐⭐⭐）
   - [ ] 自动归档开关
   - [ ] 分界点时间选择器（支持时:分精确选择，滚动选择器）
     - [ ] 点击设置项打开时间选择器对话框
     - [ ] 时（0-23）和分（0-59）独立滚动选择
     - [ ] 显示格式 HH:mm（24 小时制）
   - [ ] 历史记录保留时长选择器
     - [ ] 点击设置项打开单选对话框
     - [ ] 预设选项：30天、90天、180天、365天（推荐）、永久保留
     - [ ] 显示格式："365天" 或 "永久保留"
     - [ ] 存储值：30/90/180/365 或 -1（永久）
   - [ ] 清空历史按钮（调用 `historyDao.deleteAllSessions()`）
   - [ ] 设置项持久化
     - [ ] `archiveBoundaryHour` 和 `archiveBoundaryMinute`
     - [ ] `historyRetentionDays`

5. **历史记录 UI**（优先级：⭐⭐⭐⭐）
   - [ ] 创建 `HistoryViewModel`
     - [ ] 依赖注入 `HistoryRepository`
     - [ ] 管理选中日期、会话索引、模式状态
     - [ ] 提供 Flow 数据流（会话列表、记录列表）
   - [ ] 创建 `HistoryViewModelFactory`
   - [ ] 在事件/秒表页面 TopAppBar 添加历史按钮（位于分享和设置按钮之间）
   - [ ] `HistoryScreen` 页面框架（复用主页布局结构）
   - [ ] 从主页传递当前模式到历史页面（导航参数）
   - [ ] 历史页面根据传入模式初始化显示
   - [ ] TopAppBar 添加分享/主页/设置按钮
   - [ ] 日期选择区（160.dp，替代时间显示区）
     - 大号日期显示（60sp，点击打开日历）
     - 左右箭头切换日期（[<] [>]）
     - 副标题显示星期 + 记录统计（24sp）
     - 日期区域点击涟漪效果
   - [ ] 日历选择器对话框
     - 月份选择器（[<] [YYYY年MM月] [>]）
     - 日历视图（有记录的日期标记小圆点）
     - 当前选中日期高亮
     - 点击日期关闭对话框并刷新列表
   - [ ] 底部导航栏（事件/秒表切换，复用主页组件）
   - [ ] 事件模式：直接显示记录列表（复用主页 `RecordCard`）
   - [ ] 秒表模式实现：
     - [ ] 会话选择器（80.dp）
       - 会话标题显示："标题 (X/Y)"（40sp，加粗，可点击）
       - 点击标题打开会话选择列表对话框
       - 左右箭头快速切换会话
       - 副标题显示开始时间和总用时："HH:mm · 用时 MM:SS"（20sp）
       - 标题区域点击涟漪效果
     - [ ] 会话选择列表对话框
       - 显示当天所有会话列表（单选列表）
       - 每项显示：单选按钮 + 标题 + 开始时间 + 总用时
       - 点击会话项选中，点击确定关闭并切换
       - 点击关闭/取消不切换
     - [ ] 记录列表（一次只显示一个会话）
       - 复用主页 `RecordCard` 组件
       - 点击记录弹出对话框（编辑备注/删除记录）
       - 会话切换时刷新列表
     - [ ] 控制按钮区（96.dp，两个按钮）
       - [✏️ 编辑标题] 按钮（左，80dp 圆形）
       - [🗑️ 删除] 按钮（右，80dp 圆形）
       - 水平排列，居中，间距 16dp
       - 按钮样式与主页一致
   - [ ] 事件模式：控制按钮区（96.dp）
     - [ ] [🗑️ 删除当天] 按钮（80dp 圆形，居中）
     - [ ] 删除当前日期的所有事件记录（需确认对话框）
     - [ ] 按钮样式与主页一致
   - [ ] 日期切换动画（淡入淡出）
   - [ ] 会话切换动画（淡入淡出）
   - [ ] 返回键处理（BackHandler，返回主页）

6. **秒表手动归档**（优先级：⭐⭐⭐）
   - [ ] `StopwatchViewModel` 添加 `HistoryRepository` 依赖
   - [ ] 停止后"保存到历史"确认对话框
   - [ ] 输入会话标题对话框
     - [ ] 生成默认标题："会话 HH:mm"（基于开始时间）
     - [ ] 输入框预填充默认标题
     - [ ] 用户可直接确认或修改标题
   - [ ] `saveToHistory()` 方法实现
     - [ ] 调用 `historyRepository.archiveStopwatchRecords()`
     - [ ] 清空 DataStore 工作区
     - [ ] 重置 ViewModel 状态

7. **分享与管理**（优先级：⭐⭐⭐）
   - [ ] 扩展 `ShareHelper` 支持历史会话分享
     - [ ] `generateHistoryShareText(session, records)` 方法
   - [ ] 历史记录页面分享功能
     - [ ] 复用现有 ShareHelper 工具类（与主页分享功能接口统一）
     - [ ] 通过系统分享面板分享（Intent.ACTION_SEND）
   - [ ] 事件模式：分享该天的所有事件记录（格式与主页一致）
   - [ ] 秒表模式：分享当前选中的会话（只分享当前会话，不是该天所有会话，格式与主页一致）
   - [ ] 控制按钮区功能
     - [ ] 事件模式：删除当天所有记录（带确认对话框）
       - [ ] 调用 `historyDao.deleteSessionsByDateAndType(date, SessionType.EVENT)`
     - [ ] 秒表模式：编辑会话标题 + 删除会话（两个按钮）
       - [ ] 编辑标题按钮：弹出对话框修改当前会话标题
       - [ ] 删除按钮：删除当前会话所有记录（带确认对话框）
   - [ ] 编辑单条记录备注
     - [ ] 复用现有备注编辑对话框
     - [ ] 调用 `historyDao.updateRecordNote()`
   - [ ] 删除单条记录
     - [ ] 带确认对话框
     - [ ] 调用 `historyDao.deleteRecord()`

8. **性能优化与测试**（优先级：⭐⭐）
   - [ ] 实现自动清理旧数据
     - [ ] 应用启动时调用 `historyRepository.cleanupOldData(retentionDays)`
     - [ ] 从 DataStore 读取用户配置的保留天数
     - [ ] 如果设置为永久保留（-1）则跳过清理
     - [ ] 使用 `.onFailure` 处理清理失败（优雅降级，不影响应用启动）
     - [ ] 添加边界检查（防止 retentionDays > 36500 导致整型溢出）
   - [ ] 使用 Flow 响应式查询
     - [ ] 自动监听数据库变化
     - [ ] UI 自动更新
   - [ ] （可选）集成 Paging 3 实现分页加载
   - [ ] 测试大量数据场景（1000+ 条记录）
   - [ ] 测试归档操作的事务性（失败回滚）

9. **边缘情况处理**（优先级：⭐⭐⭐）
   - [ ] 空历史记录状态显示
   - [ ] 归档失败错误处理（Toast 提示）
   - [ ] 数据库操作异常处理（try-catch）
   - [ ] 并发归档冲突处理
   - [ ] 数据库版本迁移策略（预留）

---

**技术要点**:
- 使用 Room 数据库存储历史记录（支持大数据量）
- DataStore 仅用于工作区暂存和应用设置
- 使用 `java.time` API 进行日期计算
- 归档操作使用事务保证原子性
- Flow 响应式查询自动更新 UI
- 索引优化加速查询性能
- **历史记录保留时长**：
  - 用户可配置：30天、90天、180天、365天（推荐）、永久保留
  - 应用启动时自动清理超过时长的记录
  - 永久保留时不执行自动清理
- **分享功能接口统一**：
  - 复用现有 ShareHelper 工具类（Phase 6 已实现）
  - 历史记录和主页使用相同的文本格式化逻辑
  - 通过系统分享面板分享（Intent.ACTION_SEND）
  - 只需扩展 ShareHelper 支持从 Room Entity 转换为分享文本

**未来扩展**:
- 统计功能（记录数趋势图）
- 搜索功能（按备注搜索）
- 标签系统（工作/学习/运动）
- 批量导出历史（CSV/JSON）
- 云同步（多设备同步）

---

#### Phase 9: 代码重构与架构优化 📋 规划中

> **注意**: 这是基于当前代码（Phase 8 进行中）的初步重构方案。Phase 8 完成后需要重新评估和改进。

**目标**: 提取共享组件，优化代码结构，提高可维护性和一致性。

##### 背景与问题

在 Phase 1-8 的快速迭代中，主界面和历史记录界面采用了不同的架构模式：

**当前架构**:
```
主界面结构：
├── MainScreen.kt (管理器 - 导航和模式切换)
├── EventScreen.kt (事件模式内容)
└── StopwatchScreen.kt (秒表模式内容)

历史记录结构：
└── HistoryScreen.kt (所有内容集中在一个文件)
```

**2x2 功能矩阵**:
```
                    主界面              历史记录界面
事件模式     EventScreen          HistoryScreen(Event)
秒表模式     StopwatchScreen      HistoryScreen(Stopwatch)
```

**问题分析**:
1. **架构不一致**: 主界面拆分为 3 个文件，历史界面单文件
2. **大量重复代码**: 约 200 行重复代码（~46% 可优化）
   - NavigationBar: ~30 行重复（85% 相似度）
   - 确认对话框: ~100 行重复（90% 相似度）
   - RecordCard: ~150 行重复（70% 相似度）
3. **相似度高但未复用**:
   - 界面结构: ~85% 相似（TopAppBar + 内容区 + 底部导航）
   - 切换逻辑: ~90% 相似（事件/秒表模式切换）
   - 顶部按钮: ~70% 相似（分享、设置相同）

##### 重构方案（初步）

**设计原则**:
1. **保持 Screen 文件完整性**: MainScreen 和 HistoryScreen 保持单文件模式，易于理解整体流程
2. **提取真正可复用的组件**: 只提取重复度 > 60% 的组件，避免过度抽象
3. **分层清晰**: Screen 层（页面管理） → Components 层（可复用组件） → ViewModel 层（业务逻辑）

**新增目录结构**:
```
app/src/main/java/io/github/chy5301/chronomark/
├── ui/
│   ├── screen/                        # 页面层（保持不变）
│   │   ├── MainScreen.kt
│   │   ├── HistoryScreen.kt
│   │   └── SettingsScreen.kt
│   │
│   └── components/                    # 共享组件层（新增）
│       ├── navigation/
│       │   └── ModeNavigationBar.kt   # 事件/秒表切换导航栏
│       │
│       ├── record/
│       │   ├── RecordCard.kt          # 统一的记录卡片组件
│       │   ├── RecordsList.kt         # 记录列表组件（可选）
│       │   └── EmptyState.kt          # 空状态显示（可选）
│       │
│       └── dialog/
│           ├── ConfirmDialog.kt       # 通用确认对话框
│           ├── EditRecordDialog.kt    # 编辑记录对话框（移动）
│           └── EditTitleDialog.kt     # 编辑标题对话框
│
├── viewmodel/                         # ViewModel 层（保持不变）
├── data/                              # 数据层（保持不变）
└── util/                              # 工具类（保持不变）
```

##### 核心任务（优先级排序）

**1. 提取共享组件**（优先级：⭐⭐⭐⭐⭐）

- [ ] **ModeNavigationBar.kt** - 事件/秒表切换导航栏
  ```kotlin
  @Composable
  fun <T> ModeNavigationBar(
      currentMode: T,
      eventMode: T,
      stopwatchMode: T,
      onModeChange: (T) -> Unit,
      modifier: Modifier = Modifier
  )
  ```
  - 收益: 减少 ~30 行重复代码
  - 使用场景: MainScreen + HistoryScreen

- [ ] **ConfirmDialog.kt** - 通用确认对话框
  ```kotlin
  @Composable
  fun ConfirmDialog(
      show: Boolean,
      title: String,
      message: String,
      confirmText: String = "确定",
      dismissText: String = "取消",
      onConfirm: () -> Unit,
      onDismiss: () -> Unit,
      isDangerous: Boolean = false  // 危险操作（红色按钮）
  )
  ```
  - 收益: 减少 ~100 行重复代码
  - 使用场景: 删除记录、重置、删除会话、清空历史等所有确认场景

- [ ] **RecordCard.kt** - 统一记录卡片
  ```kotlin
  @Composable
  fun RecordCard(
      record: TimeRecord,
      mode: RecordCardMode,
      onClick: () -> Unit,
      modifier: Modifier = Modifier
  )

  enum class RecordCardMode {
      STOPWATCH,           // 秒表（累计+差值+时刻）
      EVENT,               // 事件（序号+时刻）
      HISTORY_STOPWATCH,   // 历史秒表（同 STOPWATCH）
      HISTORY_EVENT        // 历史事件（同 EVENT）
  }
  ```
  - 收益: 减少 ~150 行重复代码
  - 使用场景: EventScreen + StopwatchScreen + HistoryScreen

- [ ] **移动 EditRecordDialog** - 从 StopwatchScreen.kt 移动到 components/dialog/
  - 已被复用，只需移动位置
  - 统一对话框组件的存放位置

**2. 重构现有 Screen**（优先级：⭐⭐⭐⭐）

- [ ] MainScreen.kt - 使用 ModeNavigationBar 和 ConfirmDialog
- [ ] HistoryScreen.kt - 使用 ModeNavigationBar、ConfirmDialog、RecordCard
- [ ] EventScreen.kt - 使用 ConfirmDialog 和 RecordCard
- [ ] StopwatchScreen.kt - 使用 ConfirmDialog 和 RecordCard

**3. 可选优化**（优先级：⭐⭐）

- [ ] 提取 RecordsList.kt - 记录列表组件
- [ ] 提取 EmptyState.kt - 空状态显示组件
- [ ] 考虑是否需要 CommonTopAppBar（评估后决定）

##### 预期收益

**代码减少量**:
| 组件 | 当前行数 | 重构后行数 | 减少量 |
|------|---------|-----------|--------|
| ModeNavigationBar | 48 (24×2) | 30 | **-18 行** |
| ConfirmDialog | 120 (30×4) | 40 | **-80 行** |
| RecordCard | 200 (50×4) | 100 | **-100 行** |
| EditRecordDialog | 65 | 65 | 0（移动）|
| **总计** | **433 行** | **235 行** | **-198 行 (-46%)** |

**架构改进**:
- ✅ Screen 文件保持完整性和可读性
- ✅ 组件层职责清晰，易于复用
- ✅ 降低维护成本（修改一次，全局生效）
- ✅ 统一 UI 样式，提高一致性
- ✅ 更符合 Compose 最佳实践

##### 实施时机

- **启动时机**: Phase 8（历史记录功能）完成并测试稳定后
- **重新评估**: Phase 8 完成时，基于实际代码重新分析重复模式，优化重构方案
- **预计工作量**: 2-3 天

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