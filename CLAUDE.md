# CLAUDE.md

该文件为 Claude Code（claude.ai/code）在处理本代码库时提供指导。
## 项目概述

ChronoMark 是一个基于 Jetpack Compose 的 Android 秒表应用，支持记录时间点、时间差、世界时间以及为每个时间点添加备注和导出功能。

## 项目状态

**当前阶段**: 项目初始化完成，处于架构规划和功能开发阶段

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
├── MainActivity.kt           # 主入口 Activity
└── ui/theme/                # Compose 主题配置
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
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

## 规划的项目架构

### 架构模式
- **MVVM (Model-View-ViewModel)** 配合 Jetpack Compose
- 使用 `ViewModel` 管理 UI 状态和业务逻辑
- 使用 `StateFlow` 进行状态管理

### 规划的目录结构
```
app/src/main/java/io/github/chy5301/chronomark/
├── data/              # 数据层
│   ├── model/        # 数据模型(TimeRecord, WorldTime 等)
│   ├── repository/   # 数据仓库
│   └── local/        # 本地存储(Room 数据库或 DataStore)
├── ui/
│   ├── screen/       # 各个屏幕的 Composable
│   ├── component/    # 可复用的 UI 组件
│   └── theme/        # Compose 主题配置 (已有)
├── viewmodel/        # ViewModel 层
└── util/             # 工具类(时间格式化、导出等)
```

### 核心功能模块

1. **秒表计时**: 精确计时，支持开始、暂停、继续、重置
2. **时间点记录**: 记录每个标记点的时间戳、时间差
3. **世界时间**: 显示标记时对应的不同时区时间
4. **备注功能**: 为每个时间点添加文字说明
5. **数据导出**: 支持导出为 CSV/JSON/TXT 格式

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
- 秒表计时建议使用 `System.nanoTime()` 获取高精度时间
- 导出功能需要处理 Android 存储权限 (Android 10+ 使用 Scoped Storage)
- 世界时间功能建议使用 `java.time` API (项目最低 SDK 24 已支持，无需额外库)