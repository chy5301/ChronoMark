# ChronoMark

<div align="center">

**一个现代化的 Android 时间记录应用**

基于 Jetpack Compose 构建 · Material3 设计 · 高精度计时

[功能特性](#功能特性) · [下载安装](#下载安装) · [使用说明](#使用说明) · [技术栈](#技术栈) · [开发指南](#开发指南)

</div>

---

## 项目简介

ChronoMark 是一个基于 Jetpack Compose 的 Android 时间记录应用。**核心特色是"事件模式"——一键记录当前世界时间，无需启动计时器**，适合快速记录观察点、重要时刻或时间节点。同时提供传统秒表模式，满足不同场景需求。

### 为什么选择 ChronoMark？

- **🎯 一键记录**：事件模式无需启动计时器，随时记录当前时刻
- **⏰ 世界时间**：不仅知道"经过了多久"，还能追溯"何时发生"
- **💾 数据持久化**：应用重启后自动恢复状态和记录
- **📤 便捷分享**：一键生成格式化文本，可分享到任意应用或复制
- **⚙️ 实用设置**：保持屏幕常亮、震动反馈等功能
- **🎨 现代设计**：Material3 主题，支持亮色/深色模式

---

## 功能特性

### 双模式设计

ChronoMark 提供**事件**和**秒表**两种独立的记录模式，数据独立存储，互不干扰。

#### 📋 事件模式（推荐）

极简时间点记录工具，**区别于传统秒表需要"开始-标记-停止"的操作流程，事件模式随时随地一键记录**。

**特点**：
- ✨ 一键记录当前时间点（精确到毫秒）
- 📝 可为每个时间点添加备注
- 🔄 点击记录卡片即可编辑或删除
- 📊 正序排列，方便查看时间线

**适用场景**：
- 🔬 科学实验观察记录
- 💼 工作日志和会议纪要
- 📚 学习时间记录
- 🏃 运动训练节点标记
- 🌱 生活琐事时间追踪

#### ⏱️ 秒表模式

传统秒表设计，支持高精度计时和完整的计时控制。

**特点**：
- ⚡ 高精度计时（毫秒级，使用 nanoTime）
- 🎮 完整的计时控制（开始/暂停/继续/停止/重置）
- 📍 运行中瞬间标记时间点
- 📊 每个标记点记录累计时间、时间差和世界时间时间
- 📝 可为每个标记点添加备注
- 🔄 点击记录卡片即可编辑或删除
- 📊 倒序排列（最新的在顶部）

**适用场景**：
- 🏃 运动计时（跑步、游泳等）
- 🍳 烹饪计时
- 📚 学习计时
- 💼 工作任务计时

### 核心功能

#### ⏰ 双时间显示

- **秒表模式**：主计时器（MM:SS.mmm）+ 世界时间（yyyy-MM-dd HH:mm:ss）
- **事件模式**：世界时间（HH:mm:ss）+ 日期（yyyy-MM-dd）

#### 📤 分享与复制

一键生成格式化文本，包含完整日期时间信息，支持：
- 📱 分享到任意应用（微信、QQ、便签等）
- 📋 复制到剪贴板
- 📊 每个字段独占一行，格式清晰易读

**秒表模式分享示例**：
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
...
```

**事件模式分享示例**：
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
...
```

#### ⚙️ 实用设置

- 🔆 **保持屏幕常亮**：计时时屏幕不会自动熄灭
- 📳 **震动反馈**：按钮点击时提供触觉反馈
- 💾 **设置持久化**：应用重启后保留设置

---

## 下载安装

### 系统要求

- Android 7.0 (API 24) 及以上
- 推荐 Android 8.0 (API 26) 及以上以获得最佳体验

### 安装方式

#### 方式一：下载 APK（推荐）

1. 前往 [Releases](https://github.com/chy5301/ChronoMark/releases) 页面
2. 下载最新版本的 APK 文件
3. 在手机上安装 APK

#### 方式二：从源码构建

```bash
# 克隆仓库
git clone https://github.com/chy5301/ChronoMark.git
cd ChronoMark

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 安装到连接的设备
./gradlew installDebug
```

---

## 使用说明

### 事件模式

1. 在底部导航栏选择"事件"
2. 点击"记录"按钮即可记录当前时间点
3. 点击记录卡片可添加备注或删除记录
4. 点击顶部分享按钮可分享或复制记录

### 秒表模式

1. 在底部导航栏选择"秒表"
2. 点击"开始"按钮开始计时
3. 计时中可点击"标记"按钮记录时间点
4. 点击"暂停"暂停计时，"继续"恢复计时
5. 点击"停止"停止计时，"重置"清空所有记录
6. 点击记录卡片可添加备注或删除记录
7. 点击顶部分享按钮可分享或复制记录

### 设置

点击顶部设置图标，可配置：
- 保持屏幕常亮
- 震动反馈

---

## 技术栈

### 开发环境

- **开发工具**：Android Studio (JetBrains Runtime 21)
- **构建工具**：Gradle (Kotlin DSL)
- **编译目标**：Java 17
- **最低 SDK**：24 (Android 7.0)
- **目标 SDK**：36

### 核心技术

- **UI 框架**：Jetpack Compose + Material3
- **编程语言**：Kotlin
- **架构模式**：MVVM (Model-View-ViewModel)
- **异步处理**：Kotlin Coroutines + Flow
- **状态管理**：StateFlow
- **数据持久化**：DataStore + Kotlinx Serialization
- **时间处理**：java.time API

### 项目结构

```
app/src/main/java/io/github/chy5301/chronomark/
├── MainActivity.kt              # 主入口 Activity
├── data/
│   ├── DataStoreManager.kt      # 数据持久化管理器
│   └── model/                   # 数据模型
│       ├── AppMode.kt           # 应用模式枚举
│       ├── TimeRecord.kt        # 时间记录数据模型
│       ├── StopwatchStatus.kt   # 秒表状态枚举
│       ├── StopwatchUiState.kt  # 秒表 UI 状态
│       └── EventUiState.kt      # 事件 UI 状态
├── ui/
│   ├── screen/                  # UI 页面
│   │   ├── MainScreen.kt        # 主屏幕（管理双模式切换）
│   │   ├── StopwatchScreen.kt   # 秒表屏幕
│   │   ├── EventScreen.kt       # 事件屏幕
│   │   └── SettingsScreen.kt    # 设置页面
│   └── theme/                   # Compose 主题配置
├── util/                        # 工具类
│   ├── TimeFormatter.kt         # 时间格式化
│   ├── ShareHelper.kt           # 分享文本生成
│   └── HapticFeedbackHelper.kt  # 震动反馈辅助
└── viewmodel/                   # ViewModel 层
    ├── StopwatchViewModel.kt
    ├── StopwatchViewModelFactory.kt
    ├── EventViewModel.kt
    └── EventViewModelFactory.kt
```

---

## 开发指南

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

# 运行 Android 仪器化测试
./gradlew connectedAndroidTest

# Lint 检查
./gradlew lint
```

### 代码规范

- 遵循 Kotlin 官方编码规范
- 使用 Material3 设计规范
- 提交信息遵循 Angular Commit Message Convention

### 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'feat: 添加某个功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

详细的开发指南和设计规范请查看 [CLAUDE.md](./CLAUDE.md)。

---

## 开发路线

- [x] Phase 1: 基础计时功能
- [x] Phase 2: 时间点记录
- [x] Phase 3: 备注编辑
- [x] Phase 4: 事件模式
- [x] Phase 5: 数据持久化
- [x] Phase 6: 分享与复制功能
- [ ] Phase 7: 优化与完善（进行中）
  - [x] 事件模式列表滚动优化
  - [x] 设置页面实现
  - [x] 代码质量优化
  - [ ] UI/UX 打磨
  - [ ] 性能优化
  - [ ] 测试覆盖
  - [ ] 文档完善
  - [ ] 发布准备

详细的开发计划和进度请查看 [TODO.md](./TODO.md)。

---

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](./LICENSE) 文件。

---

## 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代化的 Android UI 工具包
- [Material Design 3](https://m3.material.io/) - Google 的设计系统
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - 异步编程框架

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ by [chy5301](https://github.com/chy5301)

</div>
