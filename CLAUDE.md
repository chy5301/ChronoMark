# CLAUDE.md

该文件为 Claude Code 在处理本代码库时提供指导。

## 项目概述

ChronoMark 是基于 Jetpack Compose 的 Android 时间记录应用，提供**事件模式**（一键记录当前时刻）和**秒表模式**（高精度计时）两种独立模式。

**当前版本**: v1.0.2

## 技术栈

- Kotlin + Jetpack Compose + Material3
- MVVM 架构 + StateFlow + Coroutines
- 数据持久化: DataStore（设置/工作区）+ Room（历史记录）
- 构建: Gradle Kotlin DSL，JDK 21 构建，Java 17 编译目标
- 最低 SDK 26，目标 SDK 36

## 常用命令

```bash
./gradlew build              # 构建项目
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK（R8 混淆）
./gradlew test               # 单元测试
./gradlew lint               # Lint 检查
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
- **时间精度**: 使用 `System.nanoTime()` 计时，每 10ms 更新显示
- **CHANGELOG**: 遵循 Keep a Changelog 格式，`[Unreleased]` 在顶部
- **Git Commit**: Angular 规范，中文编写

## 注意事项

- R8 混淆已启用，`proguard-rules.pro` 保留了 Room/DataStore/Serialization/Compose 相关类
- 嵌套 Scaffold 结构：MainScreen 管理 bottomBar，子页面管理各自 topBar
