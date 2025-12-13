# ChronoMark 开发待办事项

> 最后更新: 2025-12-13 (Phase 5 数据持久化)
> 详细的技术要求和设计规范请查看 [CLAUDE.md](./CLAUDE.md)

## 📊 项目进度总览

- [x] Phase 1: 基础计时功能
- [x] Phase 2: 时间点记录
- [x] Phase 3: 备注编辑
- [x] Phase 4: 事件模式
- [x] Phase 5: 数据持久化（已完成，待提交）
- [ ] Phase 6: 导出功能
- [ ] Phase 7: 优化与完善

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

**提交**: ⚠️ 待提交

**待办**:
- [ ] 功能测试（应用重启后状态恢复）
- [ ] 提交 Phase 5 代码
- [ ] 打版本标签 v0.5-phase5

---

## 📤 Phase 6: 导出功能

详细技术要求见 [CLAUDE.md - Phase 6](./CLAUDE.md#phase-6-导出功能)

- [ ] ExportHelper 工具类
- [ ] CSV/JSON/TXT 导出逻辑
- [ ] Android 存储权限（Scoped Storage）
- [ ] 导出 UI（格式选择对话框）
- [ ] 文件分享功能（ShareSheet）
- [ ] 启用两种模式的导出按钮
- [ ] 测试导出功能

**提交**: ⬜ 未开始

---

## ✨ Phase 7: 优化与完善

详细技术要求见 [CLAUDE.md - Phase 7](./CLAUDE.md#phase-7-优化与完善)

- [ ] 性能优化（精度、滚动、协程、重组）
- [ ] UI/UX 打磨（动画、过渡、深色模式）
- [ ] 单元测试（工具类、ViewModel、导出）
- [ ] UI 测试（模式、切换、导出）
- [ ] 错误处理和边缘情况
- [ ] 文档完善（README、使用说明、CHANGELOG）
- [ ] 发布准备（签名、构建、应用商店资源）

**提交**: ⬜ 未开始

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
