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

#### 📚 历史记录

- **事件模式**：自动按天归档，可自定义归档分界点（如 04:00 前记录归档到前一天）
- **秒表模式**：手动保存会话，支持自定义会话标题
- **历史管理**：日期选择、日历选择器、会话切换、记录编辑/删除
- **历史分享**：分享指定日期的所有记录或单个会话
- **自动清理**：可配置历史记录保留时长（30/90/180/365天或永久）

#### ⚙️ 实用设置

- 🎨 **主题模式**：浅色/深色/跟随系统
- 🔆 **保持屏幕常亮**：计时时屏幕不会自动熄灭
- 📳 **震动反馈**：按钮点击时提供触觉反馈
- 🔄 **归档配置**：自动归档开关、归档分界点、历史保留时长
- 🌍 **时区检测**：启动时检测系统时区变化并提示用户
- 🔄 **应用内更新检查**：支持 Gitee/GitHub 双源自动检查，可切换更新通道
- 💾 **设置持久化**：应用重启后保留设置

---

## 下载安装

### 系统要求

- Android 8.0 (API 26) 及以上
- 支持所有 Android 手机（包括华为、小米、OPPO 等国产品牌）

### 安装方式

#### 方式一：下载 APK（推荐）

1. 前往 [GitHub Releases](https://github.com/chy5301/ChronoMark/releases) 页面
2. 下载最新版本的 APK 文件
3. 在手机上打开 APK 文件
4. 允许安装未知来源应用（如需要）
5. 完成安装

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
- 检查更新：手动检查新版本
- 更新通道：Gitee优先 / GitHub优先
- 忽略版本：跳过特定版本的更新提示

---

## 技术栈

### 开发环境

- **开发工具**：Android Studio (JetBrains Runtime 21)
- **构建工具**：Gradle (Kotlin DSL)
- **编译目标**：Java 17
- **最低 SDK**：26 (Android 8.0)
- **目标 SDK**：36

### 核心技术

- **UI 框架**：Jetpack Compose + Material3
- **编程语言**：Kotlin
- **架构模式**：MVVM (Model-View-ViewModel)
- **异步处理**：Kotlin Coroutines + Flow
- **状态管理**：StateFlow
- **数据持久化**：
  - DataStore：应用设置和工作区数据
  - Room 数据库：历史记录存储
  - Kotlinx Serialization：数据序列化
- **代码混淆**：R8 (APK 体积优化)
- **时间处理**：java.time API (高精度 nanoTime)

### 项目结构

```
app/src/main/java/io/github/chy5301/chronomark/
├── MainActivity.kt              # 主入口 Activity
├── data/
│   ├── DataStoreManager.kt      # DataStore 管理器
│   ├── database/                # Room 数据库
│   │   ├── AppDatabase.kt       # 数据库实例
│   │   ├── dao/                 # 数据访问对象
│   │   ├── entity/              # 数据库实体
│   │   └── repository/          # 数据仓库层
│   ├── network/                 # 网络请求
│   │   └── UpdateChecker.kt     # 版本更新检查器
│   └── model/                   # 数据模型
│       ├── AppMode.kt           # 应用模式枚举
│       ├── TimeRecord.kt        # 时间记录数据模型
│       ├── StopwatchStatus.kt   # 秒表状态枚举
│       ├── RecordCardMode.kt    # 记录卡片模式枚举
│       ├── SessionType.kt       # 会话类型枚举
│       └── *UiState.kt          # UI 状态数据类
├── ui/
│   ├── screen/                  # 页面层
│   │   ├── MainScreen.kt        # 主屏幕
│   │   ├── StopwatchScreen.kt   # 秒表屏幕
│   │   ├── EventScreen.kt       # 事件屏幕
│   │   ├── HistoryScreen.kt     # 历史记录屏幕
│   │   └── SettingsScreen.kt    # 设置页面
│   ├── components/              # 共享组件层 (Phase 9)
│   │   ├── navigation/          # 导航组件
│   │   ├── record/              # 记录卡片组件
│   │   ├── button/              # 按钮组件
│   │   └── dialog/              # 对话框组件
│   └── theme/                   # Compose 主题配置
├── util/                        # 工具类
│   ├── TimeFormatter.kt         # 时间格式化
│   ├── ShareHelper.kt           # 分享文本生成
│   ├── ArchiveUtils.kt          # 归档工具（逻辑日期计算）
│   └── VersionUtils.kt          # 版本比较工具
└── viewmodel/                   # ViewModel 层
    ├── StopwatchViewModel.kt    # 秒表业务逻辑
    ├── EventViewModel.kt        # 事件业务逻辑
    ├── HistoryViewModel.kt      # 历史记录业务逻辑
    └── *ViewModelFactory.kt     # ViewModel 工厂
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

- [x] Phase 1-7: 核心功能完成
- [x] Phase 8: 历史记录功能（2025-12-28 完成）
- [x] Phase 9: 代码重构与架构优化（2025-12-29 完成）
- [x] Phase 10: 自定义归档分界点（2025-12-31 完成）
- [x] **v1.0.0: 正式版本发布**（2025-12-31）
- [x] **v1.0.1**: 双击返回退出、事件模式历史修复、CI/CD 完善（2026-01-30）
- [x] **v1.0.2**: 时区变化检测、数据库迁移修复（2026-02-01）
- [x] **v1.1.0**: 应用内版本更新检查、界面优化（2026-02-01）

### v1.2 规划
- 统计功能：记录数趋势图
- 搜索功能：按备注搜索
- 标签系统：工作/学习/运动分类

详细的变更日志请查看 [CHANGELOG.md](./CHANGELOG.md)。

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
