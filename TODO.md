# ChronoMark 开发待办事项

> 最后更新: 2025-12-20 (Phase 7 优化与完善)
> 详细的技术要求和设计规范请查看 [CLAUDE.md](./CLAUDE.md)

## 📊 项目进度总览

- [x] Phase 1: 基础计时功能
- [x] Phase 2: 时间点记录
- [x] Phase 3: 备注编辑
- [x] Phase 4: 事件模式
- [x] Phase 5: 数据持久化
- [x] Phase 6: 分享与复制功能
- [ ] Phase 7: 优化与完善（进行中）

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

**待完成**:
- [ ] **错误处理和边缘情况优化**（优先级 1）⭐⭐⭐⭐⭐
  - [ ] DataStore 错误处理（try-catch + Result 返回）
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
- [待提交] - feat: 实现深色模式功能
- dd5bf52 - refactor: 修复代码质量警告并优化代码规范
- 02bbdc4 - refactor: 简化数据持久化 API 并优化设置页面

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
