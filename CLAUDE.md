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

**当前阶段**: Phase 8 历史记录功能进行中（95% 完成）

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

> **注意**:
> - 在 Bash 环境（Git Bash、WSL、Linux、macOS）中使用 `./gradlew`
> - 在 Windows CMD 或 PowerShell 中使用 `gradlew.bat`
> - **本项目开发环境所有命令示例使用 `./gradlew`**

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

### 核心功能（已实现）

应用已实现的主要功能模块：

**双模式设计**：
- 秒表模式：传统计时器，支持开始/暂停/继续/停止/重置，运行中标记时间点
- 事件模式：极简时间点记录，无需启动计时器，一键记录当前时刻

**时间记录管理**：
- 高精度时间记录（毫秒级，使用 nanoTime）
- 双时间显示：计时器时间 + 墙上时钟（精确到毫秒）
- 记录列表：支持备注编辑、删除、自动滚动
- 空状态提示

**数据持久化**：
- DataStore：应用设置和工作区暂存
- 状态恢复：应用重启后自动恢复状态和记录
- ViewModelFactory：依赖注入支持

**分享功能**：
- ShareHelper：生成格式化分享文本
- 系统分享面板：支持分享到任意应用或复制到剪贴板

**分享文本格式示例**：

秒表模式：
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

事件模式：
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

**设置与体验**：
- 主题模式：浅色/深色/跟随系统
- 保持屏幕常亮
- 震动反馈
- 错误处理：优雅降级策略

**历史记录**：
- Room 数据库：存储历史会话和记录（HistorySessionEntity + TimeRecordEntity）
- 自动归档：事件模式按天自动归档（可配置分界点，默认凌晨 4 点）
- 手动归档：秒表模式停止后可保存会话到历史（支持自定义标题）
- 历史 UI：日期选择、会话切换、日历选择器、记录编辑/删除
- 数据管理：历史记录保留时长配置（30/90/180/365天或永久）、自动清理旧数据

---

### 未来规划

#### Phase 8: 历史记录功能 ✅ 95% 完成

**已完成核心功能**：
- ✅ Room 数据库搭建（HistorySessionEntity + TimeRecordEntity + HistoryDao + Repository）
- ✅ 自动归档逻辑（跨天检测、分界点配置、自动清理旧数据）
- ✅ 秒表手动归档（停止后保存会话，支持自定义标题）
- ✅ 设置页面扩展（归档开关、分界点时间选择器、保留时长配置）
- ✅ 历史记录 UI（日期选择、会话切换、日历选择器、记录编辑/删除）
- ✅ 历史分享功能（扩展 ShareHelper，支持事件/秒表历史会话分享）

**核心设计概要**：

**数据模型**：
- `SessionType` 枚举：区分事件模式 (EVENT) 和秒表模式 (STOPWATCH)
- `HistoryUiState`：管理当前模式、选中日期、会话列表、记录列表、会话索引
- Room Entity：直接使用 `HistorySessionEntity` + `TimeRecordEntity`，避免 Entity ↔ Domain 转换
- 设置扩展：归档分界点（时/分）、自动归档开关、历史保留时长

**历史记录 UI 布局**：

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

事件模式布局：

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

**设计要点**：
- 日期选择：左右箭头切换、点击日期打开日历选择器
- 日历选择器：月份导航、有记录日期标记小圆点、当前选中日期高亮
- 秒表模式会话选择器：显示会话标题 (X/Y)、左右箭头切换会话、点击标题打开会话列表
- 记录编辑/删除：点击记录卡片弹出编辑对话框（与主页逻辑一致）
- 控制按钮：事件模式删除当天、秒表模式编辑标题+删除会话

**功能设计**：

**自动归档逻辑**（事件模式）：
- MainActivity 启动时检查是否跨过分界点（默认凌晨 4 点）
- 跨天判断：使用分钟级时间比较（0-1439），日期变化且已过分界点时触发
- 首次使用处理：初始化 `lastCheckDate`，避免首次归档失败
- 归档操作：从 DataStore 移动到 Room 数据库，清空工作区，Toast 提示用户

**手动归档逻辑**（秒表模式）：
- 秒表停止后弹出保存确认对话框
- 生成默认标题："会话 HH:mm"（基于开始时间），用户可修改
- 归档操作：保存到 Room 数据库，清空 DataStore 工作区，重置状态

**两种模式的差异对比**：

| 维度 | 事件模式 | 秒表模式 |
|------|---------|---------|
| **归档方式** | 自动归档（到达分界点） | 手动归档（停止后询问） |
| **归档单位** | 天（一天一个会话） | 会话（一次计时一个会话） |
| **标题** | 无标题字段 | 有默认标题（"会话 HH:mm"），可修改 |
| **标题编辑** | 不支持 | 支持（保存时确认/修改 + 历史记录中编辑） |
| **显示信息** | 日期 + 记录数 | 标题 + 开始时间 + 总用时 |
| **分组** | 按日期 | 按会话（一天可能多个） |

**用户交互流程**：

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

**设置页面 UI 布局**：

**归档设置区域**：
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

**数据存储架构**：

```
DataStore (轻量级)          →  归档  →    Room Database (持久化)
- 应用设置                                 - history_sessions 表
- 秒表工作区（当前状态）                    - time_records 表
- 事件工作区（当前记录）
```

**Room 数据库设计**：
- `history_sessions` 表：会话元数据（id, date, sessionType, title, createdAt, totalElapsedNanos, startTime, endTime）
- `time_records` 表：时间记录详情（id, sessionId, index, wallClockTime, elapsedTimeNanos, splitTimeNanos, note）
- 外键关联：`time_records.sessionId` → `history_sessions.id`（级联删除）
- 索引优化：date、session_type、session_id
- TypeConverter：SessionType enum ↔ String

**剩余任务**（可选优化）：
- [ ] 性能优化：Paging 3 分页加载、大数据场景测试（1000+ 条记录）
- [ ] 边缘情况处理：归档失败错误处理、数据库异常处理、并发冲突、数据库版本迁移策略
- [ ] 动画优化：日期/会话切换动画（淡入淡出）

**未来扩展**：
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