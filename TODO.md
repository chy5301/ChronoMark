# ChronoMark 开发待办事项

> 最后更新: 2025-12-25 (Phase 8 历史记录功能 - 85% 完成)
> 详细的技术要求和设计规范请查看 [CLAUDE.md](./CLAUDE.md)

## 📊 项目进度总览

- [x] Phase 1: 基础计时功能
- [x] Phase 2: 时间点记录
- [x] Phase 3: 备注编辑
- [x] Phase 4: 事件模式
- [x] Phase 5: 数据持久化
- [x] Phase 6: 分享与复制功能
- [x] Phase 7: 优化与完善
- [ ] Phase 8: 历史记录功能（进行中，约 85% 完成）
- [ ] Phase 9: 代码重构与架构优化（规划中）

---

## ✅ Phase 1: 基础计时功能

- [x] 数据模型 + TimeFormatter 工具类
- [x] StopwatchViewModel 基础框架
- [x] 主界面布局（时间显示区 + 按钮区）
- [x] 基础计时逻辑（开始/暂停/继续/停止/重置）

**提交**: ✅ commit e005c39

---

## ✅ Phase 2: 时间点记录

- [x] 标记功能（瞬间记录，不中断）
- [x] 记录列表 UI（LazyColumn + RecordCard）
- [x] 自动滚动到顶部
- [x] 空状态显示

**提交**: ✅ commit 19e97dd

---

## ✅ Phase 3: 备注编辑

- [x] 编辑备注对话框（EditRecordDialog）
- [x] 点击记录卡片展开/编辑
- [x] 删除记录功能（带二次确认）

**提交**: ✅ commit 0333fc2

---

## ✅ Phase 4: 事件模式

- [x] 底部导航栏切换秒表/事件模式
- [x] AppMode 枚举 + EventUiState 数据模型
- [x] EventViewModel + MainScreen
- [x] 事件模式时间显示区（墙上时钟 + 日期）
- [x] 事件模式记录卡片（正序排列 + 自动滚动到末尾）
- [x] 事件模式控制按钮（记录 + 重置）
- [x] 两种模式数据分开存储

**提交**: ✅ commit 52c0c2a

---

## ✅ Phase 5: 数据持久化

详细技术要求见 [CLAUDE.md - Phase 5](./CLAUDE.md#phase-5-数据持久化)

- [x] 集成 DataStore 依赖和 Kotlinx Serialization
- [x] 创建 DataStoreManager 工具类
- [x] 为 TimeRecord 添加序列化注解
- [x] 秒表模式数据持久化（状态、时间、记录）
- [x] 事件模式数据持久化（记录列表）
- [x] 应用设置持久化（当前模式）
- [x] 在 StopwatchViewModel 中实现状态恢复逻辑
- [x] 在 EventViewModel 中实现状态恢复逻辑
- [x] 边缘情况处理（Running 状态自动转为 Paused）
- [x] 创建 ViewModelFactory 支持依赖注入
- [x] 更新所有 Screen 使用新的 ViewModelFactory
- [x] 项目构建测试通过
- [x] 功能测试（应用重启后状态恢复）

**提交**: ✅ commit 19402ef

---

## ✅ Phase 6: 分享与复制功能

详细技术要求见 [CLAUDE.md - Phase 6](./CLAUDE.md#phase-6-分享与复制功能)

- [x] 创建 ShareHelper 工具类（生成分享文本）
- [x] 实现秒表模式文本格式化（每个字段独占一行）
- [x] 实现事件模式文本格式化（极简设计，仅保留时间和备注）
- [x] 在 ViewModel 中添加 generateShareText() 方法
- [x] 在两种模式的 TopAppBar 中启用分享按钮
- [x] 实现系统分享功能（直接调用 Intent.ACTION_SEND）
- [x] 添加空记录时的友好提示（Toast）
- [x] 测试分享功能

**功能特点**:
- 简洁的文本格式，易读易分享，每个字段独占一行
- 日期只在开头显示一次（yyyy-MM-dd），记录中只显示时间
- 直接调用系统分享面板，系统已包含复制到剪贴板功能
- 支持分享到任意应用（微信/QQ/便签等）

**提交**: ✅ commit 7929e61

---

## 🔄 Phase 7: 优化与完善（进行中）

详细技术要求见 [CLAUDE.md - Phase 7](./CLAUDE.md#phase-7-优化与完善)

**已完成**:
- [x] 事件模式列表滚动优化（修复进入界面闪烁问题）
  - 使用 `initialFirstVisibleItemIndex` 设置列表初始位置
  - 通过 `previousSize` 精确判断新增/删除/进入界面场景
  - 只在新增记录时触发自动滚动，删除记录时保持当前位置

- [x] 设置页面实现（2025-12-14）
  - 创建 SettingsScreen 页面框架
  - 实现保持屏幕常亮功能（WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON）
  - 实现震动反馈功能（HapticFeedback API）
  - 创建 HapticFeedbackHelper 工具类
  - 在 DataStoreManager 中添加设置项持久化
  - 设置页面支持返回键处理（BackHandler）
  - 所有按钮点击时根据设置提供震动反馈
  - 菜单按钮改为直接的设置图标（简化导航）

- [x] 代码质量优化（2025-12-20）
  - 修复代码质量警告
  - 优化代码规范
  - 简化数据持久化 API
  - 优化设置页面实现

- [x] 深色模式实现（2025-12-21）
  - 创建 ThemeMode 枚举（浅色/深色/跟随系统）
  - 在 DataStoreManager 中添加主题模式持久化
  - 实现主题选择对话框（RadioButton 选择）
  - 在 MainActivity 中应用主题设置
  - 支持三种主题模式切换
  - 优化对话框间距（更紧凑的布局）

- [x] 错误处理和边缘情况分析（2025-12-21）
  - 全面分析当前代码的错误处理情况
  - 识别 6 大类潜在问题（数据持久化、时间计算、大量数据、用户输入、分享功能、协程管理）
  - 设计完整的优化方案（优先级分级）
  - 编写实施计划（Phase 1-3）

- [x] DataStore 错误处理优化（2025-12-21）
  - 为所有 save*/clear* 方法添加 try-catch 和 Result<Unit> 返回类型
  - 在 ViewModel 和 Screen 中使用 .onFailure 处理错误
  - 采用"优雅降级"策略：保存失败不影响 UI，只记录日志
  - 确保应用在数据持久化失败时不会崩溃
  - 共计修改 10 个 DataStoreManager 方法和 9 处调用点

**待完成**:
- [ ] **错误处理和边缘情况优化**（优先级 1）⭐⭐⭐⭐⭐
  - [x] DataStore 错误处理（try-catch + Result 返回）
  - [ ] 记录数量限制（MAX_RECORDS = 1000）
  - [ ] 备注长度限制（MAX_NOTE_LENGTH = 500）
  - [ ] 时间计算边界检查（防止溢出和负数）
  - [ ] Toast 提示系统（操作反馈）
  - [ ] 分享功能异常处理
  - [ ] 协程异常处理和取消逻辑

- [ ] 性能优化（大量记录场景下的列表性能、协程优化）
- [ ] 单元测试（工具类、ViewModel、分享逻辑）
- [ ] UI 测试（两种模式、模式切换、分享功能）
- [ ] 文档完善（CHANGELOG）
- [ ] 发布准备（签名密钥、Release 构建、应用商店资源）

**最近提交**:
- [待提交] - feat: 添加 DataStore 错误处理机制
- 47016f8 - feat: 实现深色模式功能并完成错误处理分析
- dd5bf52 - refactor: 修复代码质量警告并优化代码规范

---

## 🔄 Phase 8: 历史记录功能（进行中，约 95% 完成）

详细技术要求见 [CLAUDE.md - Phase 8](./CLAUDE.md#phase-8-历史记录功能)

### 已完成 ✅

#### 1. Room 数据库搭建 (100%)
- [x] 添加 Room 依赖到 `build.gradle.kts`
- [x] 创建 `HistorySessionEntity` 实体类（含索引和外键）
- [x] 创建 `TimeRecordEntity` 实体类（含外键和级联删除）
- [x] 创建 `HistoryDao` 接口（查询/插入/更新/删除操作完整）
- [x] 创建 `AppDatabase` 类（单例模式 + TypeConverters）
- [x] 创建 `HistoryRepository` 类（归档事件/秒表记录 + 查询 + 清理）

**提交**: ✅ commit [待标记]

#### 2. DataStore 扩展 (100%)
- [x] 添加归档设置键值（ARCHIVE_BOUNDARY_HOUR/MINUTE 等）
- [x] 添加保存/读取归档设置方法
- [x] 添加清空工作区方法（clearEventRecords/clearStopwatchRecords）

**提交**: ✅ commit [待标记]

#### 3. 自动归档逻辑 (100%)
- [x] 实现跨天检测（shouldArchive 方法，分钟级时间比较）
- [x] MainActivity 启动时检查归档（checkAndCleanupOldData）
- [x] 实现自动清理旧数据（基于用户配置的保留天数）
- [x] 首次使用处理和边界检查

**提交**: ✅ commit [待标记]

#### 4. 设置页面 (100%)
- [x] 自动归档开关
- [x] 分界点时间选择器（支持时:分精确选择，上下箭头）
- [x] 历史记录保留时长选择器（30/90/180/365天 + 永久保留）
- [x] 清空历史按钮（危险操作样式）
- [x] 设置项持久化（使用 DataStore）

**提交**: ✅ commit [待标记]

#### 5. 历史记录 UI (100%)

**已完成**:
- [x] 创建 `HistoryViewModel` 和 `HistoryViewModelFactory`
- [x] `HistoryScreen` 页面框架（复用主页布局结构）
- [x] TopAppBar（分享/主页/设置按钮）
- [x] 日期选择区（160.dp，大号日期 + 左右箭头 + 副标题统计）
- [x] 底部导航栏（事件/秒表切换）
- [x] 空状态显示（EmptyHistoryState）
- [x] 返回键处理（BackHandler）
- [x] 在主页 TopAppBar 添加历史按钮（导航入口）
- [x] 从主页传递当前模式到历史页面
- [x] 退出图标改为主页图标（更符合语义）
- [x] **事件模式**：
  - [x] 记录列表显示（EventHistoryRecordsList）
  - [x] 历史记录卡片（HistoryRecordCard，支持事件/秒表模式）
  - [x] 控制按钮（删除当天按钮）
  - [x] 删除当天记录确认对话框
- [x] **秒表模式**：
  - [x] 记录列表显示（StopwatchHistoryRecordsList，showElapsedTime=true）
  - [x] 控制按钮（编辑标题 + 删除会话按钮）
  - [x] 会话选择器组件（80.dp，标题显示 + 左右箭头 + 副标题）
  - [x] 会话选择列表对话框（RadioButton 单选 + 会话元数据显示）
  - [x] 删除当前会话确认对话框

- [x] **秒表模式**（续）：
  - [x] 编辑会话标题对话框（OutlinedTextField + 预填充 + 保存/取消）
- [x] 编辑/删除单条记录功能：
  - [x] EditHistoryRecordDialog（显示累计时间、标记时刻、编辑备注）
  - [x] 删除单条记录确认对话框
  - [x] 连接到事件和秒表模式的记录点击处理器
- [x] 日历选择器对话框：
  - [x] CalendarPickerDialog（月份导航 + 日历网格）
  - [x] CalendarGrid 组件（星期标题 + 日期选择 + 高亮当前选中）
  - [x] 日期文本可点击（连接到对话框）
  - [x] 临时选择预览 + 确认/取消按钮
  - [x] 日历中标记有记录的日期（4.dp 小圆点，颜色自适应）
  - [x] ViewModel 加载有记录日期数据（loadDatesWithRecords）

**提交**: ✅ commit b3ca48f (历史 UI 并集成主页导航)
**提交**: ✅ commit 69ae1b9 (会话选择器 + 图标优化 + 对话框实现)
**提交**: ✅ commit 70bb23f (编辑标题 + 编辑记录 + 日历选择器 + 日期标记)

**未完成**（可选优化）:
- [ ] 日期/会话切换动画

#### 6. 秒表手动归档 (100%)
- [x] StopwatchViewModel 添加 HistoryRepository 依赖注入
- [x] StopwatchViewModelFactory 支持 HistoryRepository 参数
- [x] MainScreen 创建 StopwatchViewModel 时传入 HistoryRepository
- [x] 实现 getDefaultTitle() 方法（生成默认标题"会话 HH:mm"）
- [x] 实现 saveToHistory(title) 方法（归档到 Room 数据库）
- [x] 停止后保存确认对话框（保存/不保存选项）
- [x] 输入会话标题对话框（预填充默认标题，可编辑）

**提交**: ✅ commit f8b4aec (秒表手动归档功能)

#### 7. 分享与管理 (100%)

**已完成**:
- [x] 控制按钮确认对话框：
  - [x] 删除当天记录确认对话框（事件模式）
  - [x] 删除当前会话确认对话框（秒表模式）
- [x] 编辑会话标题对话框（秒表模式）
- [x] 编辑单条记录备注对话框（EditHistoryRecordDialog）
- [x] 删除单条记录确认对话框
- [x] 扩展 ShareHelper 支持历史会话分享：
  - [x] generateHistoryShareText() 自动判断类型
  - [x] generateHistoryStopwatchShareText() 秒表分享（含标题）
  - [x] generateHistoryEventShareText() 事件分享
- [x] HistoryViewModel 添加 generateShareText() 方法
- [x] 历史记录页面分享功能实现（TopAppBar 分享按钮）
- [x] 事件模式分享该天所有记录
- [x] 秒表模式分享当前选中会话

**提交**: ✅ commit [待标记] (历史记录分享功能)

#### 8. 性能优化与测试 (40%)

**已完成**:
- [x] 实现自动清理旧数据（MainActivity 启动时调用）
- [x] 使用 Flow 响应式查询（HistoryDao）

**未完成**:
- [ ] 集成 Paging 3 分页加载（可选）
- [ ] 测试大量数据场景（1000+ 条记录）
- [ ] 测试归档操作的事务性（失败回滚）

#### 9. 边缘情况处理 (20%)

**已完成**:
- [x] 空历史记录状态显示

**未完成**:
- [ ] 归档失败错误处理（Toast 提示）
- [ ] 数据库操作异常处理（try-catch）
- [ ] 并发归档冲突处理
- [ ] 数据库版本迁移策略

### 下一步优先级

**已完成** ✅：
- ~~在主页集成历史按钮导航~~
- ~~秒表模式会话选择器和对话框~~
- ~~删除确认对话框（事件/秒表）~~
- ~~编辑会话标题对话框（秒表模式）~~
- ~~编辑/删除单条记录对话框~~
- ~~日历选择器对话框~~
- ~~日历中标记有记录的日期（小圆点）~~
- ~~秒表手动归档功能（停止后保存到历史）~~
- ~~历史记录分享功能（扩展 ShareHelper）~~

**剩余任务**：

1. **高优先级（核心功能）**：
   - ✅ 全部完成！

2. **中优先级（用户体验）**：
   - 日期/会话切换动画（可选）

3. **低优先级（完善）**：
   - 边缘情况处理（归档失败、数据库异常等）
   - 性能测试（大量数据场景）

---

## 📋 Phase 9: 代码重构与架构优化（规划中）

> **注意**: 这是基于当前代码（Phase 8 进行中）的初步重构方案。Phase 8 完成后需要重新评估和改进。

详细技术要求见 [CLAUDE.md - Phase 9](./CLAUDE.md#phase-9-代码重构与架构优化)

### 背景

- **问题**: 主界面和历史界面架构不一致，存在约 200 行重复代码（~46% 可优化）
- **目标**: 提取共享组件，统一架构模式，提高可维护性
- **时机**: Phase 8 完成后启动，Phase 8 完成时重新评估方案

### 核心任务

#### 1. 提取共享组件（优先级：⭐⭐⭐⭐⭐）

- [ ] 创建 `ui/components/` 目录结构
  - [ ] `navigation/ModeNavigationBar.kt` - 事件/秒表切换导航栏
  - [ ] `dialog/ConfirmDialog.kt` - 通用确认对话框
  - [ ] `record/RecordCard.kt` - 统一记录卡片组件
  - [ ] 移动 `EditRecordDialog` 到 `dialog/` 目录

#### 2. 重构现有 Screen（优先级：⭐⭐⭐⭐）

- [ ] 重构 MainScreen.kt
  - [ ] 使用 ModeNavigationBar 组件
  - [ ] 使用 ConfirmDialog 组件（如适用）

- [ ] 重构 HistoryScreen.kt
  - [ ] 使用 ModeNavigationBar 组件
  - [ ] 使用 ConfirmDialog 替换内联对话框
  - [ ] 使用统一的 RecordCard 组件

- [ ] 重构 EventScreen.kt
  - [ ] 使用 ConfirmDialog 组件
  - [ ] 使用统一的 RecordCard 组件

- [ ] 重构 StopwatchScreen.kt
  - [ ] 使用 ConfirmDialog 组件
  - [ ] 使用统一的 RecordCard 组件

#### 3. 可选优化（优先级：⭐⭐）

- [ ] 评估是否需要 RecordsList 组件
- [ ] 评估是否需要 EmptyState 组件
- [ ] 评估是否需要 CommonTopAppBar 组件

### 预期收益

- **代码减少**: 约 200 行（-46%）
- **架构改进**:
  - ✅ 统一组件样式
  - ✅ 降低维护成本
  - ✅ 提高代码复用率
  - ✅ 符合 Compose 最佳实践

### 实施计划

1. **Phase 8 完成后**: 重新评估代码，识别实际重复模式
2. **更新重构方案**: 基于实际情况优化方案设计
3. **逐步实施**: 按优先级依次实施（预计 2-3 天）
4. **测试验证**: 确保重构不影响现有功能

---

## 💡 额外功能（可选）

- [ ] 多会话支持
- [ ] 记录历史浏览
- [ ] 统计分析
- [ ] 主题自定义
- [ ] 声音提示
- [ ] 小组件支持
- [ ] 导入功能

---

## 🐛 问题追踪

### 已知问题
- 暂无

### 已解决问题
- ✅ 事件模式滚动抖动：在底部添加记录时列表抖动 → 根据滚动位置智能选择滚动方式
- ✅ Scaffold 嵌套双重间距：按钮区域与底部导航栏间距过大 → 内层 Scaffold 只应用顶部 padding
- ✅ 控制按钮区域上下留白不均：上边留白大于下边 → 使用 TopCenter 对齐 + 4dp 顶部间距

### 待改进
- 暂无

---

## 📝 开发流程

每完成一个 Phase 后：
1. ✅ 充分测试功能
2. ✅ 更新 CLAUDE.md 中对应 Phase 的完成状态（添加 ✅）
3. ✅ 更新 TODO.md 中的进度追踪
4. ✅ 提交代码并打上版本标签（如 `v0.4-phase4`）
5. ✅ 更新"最后更新"时间
