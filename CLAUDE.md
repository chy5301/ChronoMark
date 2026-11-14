# CLAUDE.md

该文件为 Claude Code 在处理本代码库时提供指导。

## 项目概述

ChronoMark 是基于 Jetpack Compose 的 Android 时间记录应用，提供**事件模式**（一键记录当前时刻）和**秒表模式**（高精度计时）两种独立模式。

**当前版本**: v1.1.0

## 技术栈

- Kotlin 2.4 + Jetpack Compose + Material3
- MVVM 架构 + StateFlow + Coroutines
- 数据持久化: DataStore（设置/工作区）+ Room（历史记录）
- 构建: Gradle 9.9 + AGP 9.5（内置 Kotlin 支持，不再应用 `org.jetbrains.kotlin.android` 插件），KSP2 生成 Room 代码；JDK ≥17 均可（CI 用 Temurin 17，未声明 toolchain），Java 17 编译目标
- 最低 SDK 26，编译 SDK 37，目标 SDK 36

## 常用命令

```bash
./gradlew build              # 构建项目
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK（R8 混淆）
./gradlew test               # 单元测试
./gradlew lint               # Lint 检查
./gradlew testDebugUnitTest --tests "*.EventViewModelTest"  # 只跑单个测试类
./gradlew installDebug       # 安装到已连接的设备/模拟器
./gradlew connectedDebugAndroidTest  # 仪器测试（需设备，含 HistoryDaoTest）
```

## CI/CD

- `.github/workflows/ci.yml` - Push/PR 到 main 时运行 Lint + 测试 + Debug 构建
- `.github/workflows/release.yml` - Push tag `v*.*.*` 时自动构建签名 APK 并创建 Release

**发布新版本**:
```bash
# 1. 更新 CHANGELOG.md 和 app/build.gradle.kts 版本号
# 2. 提交推送后创建 tag
git tag -a vX.Y.Z -m "vX.Y.Z" && git push origin vX.Y.Z
```

**签名配置**: 本地用 `keystore.properties`，CI 用 GitHub Secrets

## 项目结构

```
app/src/main/java/io/github/chy5301/chronomark/
├── data/
│   ├── DataStoreManager.kt          # 设置 + 工作区
│   ├── database/                    # Room（历史记录）
│   ├── network/                     # 网络请求（OkHttp）
│   └── model/                       # 数据模型
├── ui/
│   ├── screen/                      # 页面（Main/Event/Stopwatch/History/Settings）
│   ├── components/                  # 共享组件
│   └── theme/
├── util/                            # 工具类
└── viewmodel/                       # ViewModel
```

## 关键约定

- **依赖管理**: 使用 `gradle/libs.versions.toml`，通过 `libs.xxx` 引用
- **时间精度**: 秒表用 `SystemClock.elapsedRealtimeNanos()` 计时（`System.nanoTime()` 在深度休眠时会停），每 10ms 更新显示；持久化的是计时锚点 `StopwatchTiming`（累计时长 + 锚点时刻 + 开机次数），不是已计时长，恢复时现算
- **逻辑日期**: 事件记录按归档分界点（默认 04:00）计算逻辑日期，用 `ArchiveUtils.getLogicalDate`；凡涉及事件「今天」的地方都要用它，不能用 `LocalDate.now()`
- **数据源**: 事件页与历史页订阅 Room Flow，数据库是唯一数据源，不在内存维护记录副本；按日期/会话切换的订阅用 `flatMapLatest`，不要手动管理 Job
- **ViewModel 创建**: 无 DI 框架，每个 ViewModel 配一个 `XxxViewModelFactory`，在对应 Screen 中通过 `viewModel(factory = ...)` 创建；新增 ViewModel 需同步新增 Factory
- **ViewModel 测试**: 时钟以 `() -> Long` 构造参数注入（默认值为系统时钟）以便测试控制时间；含无限循环（计时/墙上时钟）的 ViewModel 测试用 `vmTest {}` 包装，在 finally 中 `viewModelScope.cancel()`，否则断言失败时 `runTest` 收尾会无限推进虚拟时间，测试进程卡死
- **Room 结构变更**: 提升 `AppDatabase` 的 `version`，添加对应 `Migration` 并注册到 `addMigrations`，同时提交 `app/schemas/` 下新生成的 JSON
- **CHANGELOG**: 遵循 Keep a Changelog 格式，`[Unreleased]` 在顶部
- **Git Commit**: Angular 规范，中文编写

## 注意事项

- R8 混淆已启用，`proguard-rules.pro` 保留了 Room/DataStore/Serialization/Compose 相关类
- 嵌套 Scaffold 结构：MainScreen 管理 bottomBar，子页面管理各自 topBar
