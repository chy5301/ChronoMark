# Phase 9: 代码重构与架构优化 - 详细实施计划

> 基于 2025-12-28 代码分析报告
> 预计工作量: 5-7 天
> 目标: 减少 600+ 行重复代码，提升代码可维护性

---

## 📊 重构数据概览

### 当前项目状态
- **核心 UI 代码**: 2,995 行
- **重复代码量**: ~700-750 行（23-25%）
- **高度重复**: 254 行（95% 相似度）
- **中度重复**: 400-450 行（60-70% 相似度）

### 重构目标
- **减少重复代码**: 600 行（-86%）
- **提升 UI 一致性**: 70% → 95%
- **增加代码复用率**: 0% → 40%+
- **净代码量减少**: 245 行（-10%）

---

## 🎯 五阶段实施计划

### Phase 9-1: 高优先级组件提取 ⭐⭐⭐⭐⭐

**目标**: 提取最高收益、最低难度的组件
**时间**: 1-1.5 天

#### 任务 1.1: 创建 ModeNavigationBar 组件

**文件**: `ui/components/navigation/ModeNavigationBar.kt`

**提取来源**:
- MainScreen.kt (第 162-186 行)
- HistoryScreen.kt (第 327-340 行)

**预期收益**:
- 减少 30 行重复代码
- 统一导航栏样式

**实现清单**:
- [ ] 创建 `ui/components/navigation/` 目录
- [ ] 编写 ModeNavigationBar 泛型组件
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
- [ ] 添加详细的 KDoc 文档
- [ ] MainScreen.kt 替换为新组件（测试 AppMode 类型）
- [ ] HistoryScreen.kt 替换为新组件（测试 SessionType 类型）
- [ ] 构建测试通过
- [ ] 功能测试：导航栏切换正常

---

#### 任务 1.2: 创建 ConfirmDialog 通用确认对话框

**文件**: `ui/components/dialog/ConfirmDialog.kt`

**提取来源** (7-8 处):
- EventScreen.kt: 删除确认、重置确认（2 处）
- StopwatchScreen.kt: 删除确认（1 处）
- HistoryScreen.kt: 删除确认、重置确认（4 处）

**预期收益**:
- 减少 160-180 行重复代码
- 统一确认对话框样式
- 简化对话框调用

**实现清单**:
- [ ] 创建 `ui/components/dialog/` 目录
- [ ] 编写 ConfirmDialog 组件
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
      isDangerous: Boolean = false
  )
  ```
- [ ] 添加详细的 KDoc 文档
- [ ] EventScreen.kt 替换 2 个确认对话框
- [ ] StopwatchScreen.kt 替换 1 个确认对话框
- [ ] HistoryScreen.kt 替换 4 个确认对话框
- [ ] 构建测试通过
- [ ] 功能测试：所有确认对话框正常工作

---

#### 任务 1.3: 阶段性提交

- [ ] 运行代码质量检查：`./gradlew lint`
- [ ] 运行单元测试（如有）：`./gradlew test`
- [ ] 创建 Git 提交
  ```
  refactor(phase9-1): 提取高优先级共享组件

  新增组件：
  - ModeNavigationBar: 统一事件/秒表导航栏（2 处复用）
  - ConfirmDialog: 通用确认对话框（7 处复用）

  代码改进：
  - 减少重复代码 ~190 行
  - 统一 UI 样式和交互逻辑
  ```

---

### Phase 9-2: 编辑对话框组件提取 ⭐⭐⭐⭐

**目标**: 统一编辑记录对话框
**时间**: 0.5-1 天

#### 任务 2.1: 创建 EditRecordDialog 通用组件

**文件**: `ui/components/dialog/EditRecordDialog.kt`

**提取来源**:
- StopwatchScreen.kt: EditRecordDialog (第 532-598 行，68 行)
- HistoryScreen.kt: EditHistoryRecordDialog (第 945-1010 行，60 行)

**预期收益**:
- 减少 110-120 行重复代码
- 统一编辑对话框样式

**设计方案**:
```kotlin
// 方案 A: 泛型接口（推荐）
interface IRecordData {
    val id: String
    val index: Int
    val elapsedTimeNanos: Long
    val wallClockTime: Long
    val note: String
}

@Composable
fun <T : IRecordData> EditRecordDialog(
    record: T,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDeleteRequest: () -> Unit
)

// 方案 B: 数据转换适配器
data class EditableRecord(
    val id: String,
    val index: Int,
    val elapsedTimeNanos: Long,
    val wallClockTime: Long,
    val note: String
)

fun TimeRecord.toEditable() = EditableRecord(...)
fun TimeRecordEntity.toEditable() = EditableRecord(...)
```

**实现清单**:
- [ ] 决定使用方案 A 或方案 B
- [ ] 创建接口/数据类（如需要）
- [ ] 编写 EditRecordDialog 组件
- [ ] 添加详细的 KDoc 文档
- [ ] StopwatchScreen.kt 删除旧组件，使用新组件
- [ ] HistoryScreen.kt 删除旧组件，使用新组件
- [ ] 构建测试通过
- [ ] 功能测试：编辑记录、删除记录功能正常

---

#### 任务 2.2: 阶段性提交

- [ ] 代码质量检查
- [ ] 创建 Git 提交
  ```
  refactor(phase9-2): 提取统一编辑记录对话框

  新增组件：
  - EditRecordDialog: 通用编辑记录对话框（支持 TimeRecord 和 TimeRecordEntity）

  代码改进：
  - 减少重复代码 ~120 行
  - 统一编辑/删除记录交互
  ```

---

### Phase 9-3: 统一记录卡片组件 ⭐⭐⭐⭐

**目标**: 统一所有记录卡片样式
**时间**: 1-1.5 天

#### 任务 3.1: 创建 UnifiedRecordCard 组件

**文件**: `ui/components/record/UnifiedRecordCard.kt`

**提取来源**:
- EventScreen.kt: EventRecordCard (第 304-350 行，47 行)
- StopwatchScreen.kt: RecordCard (第 351-419 行，69 行)
- HistoryScreen.kt: HistoryRecordCard (第 597-674 行，78 行)

**预期收益**:
- 减少 130-150 行重复代码
- 统一卡片样式和布局

**设计方案**:
```kotlin
enum class RecordCardMode {
    EVENT,                  // 事件模式：序号 + 时刻
    STOPWATCH,              // 秒表模式：序号 + 累计 + 差值 + 时刻
    HISTORY_EVENT,          // 历史事件：同 EVENT
    HISTORY_STOPWATCH       // 历史秒表：同 STOPWATCH
}

@Composable
fun UnifiedRecordCard(
    index: Int,
    wallClockTime: Long,
    note: String,
    mode: RecordCardMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // 秒表模式专用参数
    elapsedTimeNanos: Long? = null,
    splitTimeNanos: Long? = null
)
```

**实现清单**:
- [ ] 创建 `ui/components/record/` 目录
- [ ] 创建 RecordCardMode 枚举
- [ ] 编写 UnifiedRecordCard 组件
- [ ] 添加详细的 KDoc 文档
- [ ] EventScreen.kt 删除 EventRecordCard，使用新组件
- [ ] StopwatchScreen.kt 删除 RecordCard，使用新组件
- [ ] HistoryScreen.kt 删除 HistoryRecordCard，使用新组件
- [ ] 构建测试通过
- [ ] UI 测试：所有模式卡片显示正常
- [ ] 交互测试：点击卡片编辑功能正常

---

#### 任务 3.2: 阶段性提交

- [ ] 代码质量检查
- [ ] 创建 Git 提交
  ```
  refactor(phase9-3): 统一所有记录卡片组件

  新增组件：
  - RecordCardMode: 卡片模式枚举
  - UnifiedRecordCard: 统一记录卡片（支持 4 种显示模式）

  代码改进：
  - 减少重复代码 ~150 行
  - 统一卡片样式和交互
  ```

---

### Phase 9-4: 重构现有 Screen 文件 ⭐⭐⭐

**目标**: 整理和优化 Screen 文件
**时间**: 1 天

#### 任务 4.1: 重构 MainScreen.kt

**清理内容**:
- [x] 已使用 ModeNavigationBar（任务 1.1 完成）

**额外优化**:
- [ ] 检查是否有其他可优化的代码
- [ ] 更新 import 语句
- [ ] 添加组件使用说明注释

---

#### 任务 4.2: 重构 EventScreen.kt

**清理内容**:
- [x] 已使用 ConfirmDialog（任务 1.2 完成）
- [x] 已使用 UnifiedRecordCard（任务 3.1 完成）

**额外优化**:
- [ ] 删除旧的 EventRecordCard 组件定义
- [ ] 检查 ControlButton 使用情况
- [ ] 更新 import 语句
- [ ] 添加组件使用说明注释

**预期结果**:
- 文件行数: 381 → 330 行（-51 行，-13%）

---

#### 任务 4.3: 重构 StopwatchScreen.kt

**清理内容**:
- [x] 已使用 ConfirmDialog（任务 1.2 完成）
- [x] 已使用 EditRecordDialog（任务 2.1 完成）
- [x] 已使用 UnifiedRecordCard（任务 3.1 完成）

**移动到 components**:
- [ ] 移动 ControlButton 到 `ui/components/button/ControlButton.kt`
- [ ] 删除旧的 RecordCard、EditRecordDialog 定义
- [ ] 更新所有对 ControlButton 的引用

**额外优化**:
- [ ] 简化 ControlButtonsSection
- [ ] 更新 import 语句
- [ ] 添加组件使用说明注释

**预期结果**:
- 文件行数: 598 → 450 行（-148 行，-25%）

---

#### 任务 4.4: 重构 HistoryScreen.kt

**清理内容**:
- [x] 已使用 ModeNavigationBar（任务 1.1 完成）
- [x] 已使用 ConfirmDialog（任务 1.2 完成）
- [x] 已使用 EditRecordDialog（任务 2.1 完成）
- [x] 已使用 UnifiedRecordCard（任务 3.1 完成）

**额外优化**:
- [ ] 删除旧的 HistoryRecordCard、EditHistoryRecordDialog 定义
- [ ] 考虑是否提取 EventHistoryControlButtons 和 StopwatchHistoryControlButtons
- [ ] 更新 import 语句
- [ ] 添加组件使用说明注释

**预期结果**:
- 文件行数: 1192 → 950 行（-242 行，-20%）

---

#### 任务 4.5: 阶段性提交

- [ ] 运行完整构建：`./gradlew build`
- [ ] 功能测试：所有模式正常工作
- [ ] 创建 Git 提交
  ```
  refactor(phase9-4): 重构所有 Screen 文件使用共享组件

  改进：
  - MainScreen: 使用 ModeNavigationBar
  - EventScreen: 使用 ConfirmDialog + UnifiedRecordCard (-51 行)
  - StopwatchScreen: 使用所有共享组件，移动 ControlButton (-148 行)
  - HistoryScreen: 使用所有共享组件 (-242 行)

  总计减少重复代码: ~441 行
  ```

---

### Phase 9-5: 测试验证和文档更新 ⭐⭐⭐⭐⭐

**目标**: 确保重构质量，更新文档
**时间**: 1-1.5 天

#### 任务 5.1: 全面功能测试

**测试清单**:
- [ ] **主界面测试**
  - [ ] 事件/秒表模式切换
  - [ ] 导航栏高亮状态
  - [ ] 数据独立性

- [ ] **事件模式测试**
  - [ ] 记录时间点
  - [ ] 记录卡片显示（UnifiedRecordCard）
  - [ ] 编辑备注
  - [ ] 删除记录（ConfirmDialog）
  - [ ] 重置确认（ConfirmDialog）
  - [ ] 分享功能

- [ ] **秒表模式测试**
  - [ ] 开始/暂停/继续/停止/重置
  - [ ] 标记时间点
  - [ ] 记录卡片显示（UnifiedRecordCard）
  - [ ] 编辑备注（EditRecordDialog）
  - [ ] 删除记录（ConfirmDialog）
  - [ ] 保存到历史（ConfirmDialog）
  - [ ] 分享功能

- [ ] **历史记录测试**
  - [ ] 事件/秒表模式切换（ModeNavigationBar）
  - [ ] 日期选择
  - [ ] 日历选择器
  - [ ] 事件模式记录显示（UnifiedRecordCard）
  - [ ] 秒表模式记录显示（UnifiedRecordCard）
  - [ ] 会话选择器
  - [ ] 编辑记录（EditRecordDialog）
  - [ ] 删除记录（ConfirmDialog）
  - [ ] 删除会话（ConfirmDialog）
  - [ ] 分享功能

- [ ] **设置页面测试**
  - [ ] 所有设置项正常
  - [ ] 清空历史确认（ConfirmDialog）

---

#### 任务 5.2: 代码质量检查

- [ ] 运行 Lint 检查：`./gradlew lint`
- [ ] 检查所有 import 语句是否正确
- [ ] 检查是否有未使用的代码
- [ ] 检查组件文档是否完整
- [ ] 检查代码格式一致性

---

#### 任务 5.3: 更新项目文档

**更新 CLAUDE.md**:
- [ ] 更新项目结构（添加 ui/components/ 目录）
- [ ] 标记 Phase 9 为已完成
- [ ] 更新"已完成功能"列表
- [ ] 添加组件使用说明

**更新 TODO.md**:
- [ ] 标记 Phase 9 所有任务为已完成
- [ ] 更新最后更新日期
- [ ] 添加 Phase 9 完成总结

**创建 CHANGELOG.md** (可选):
- [ ] 记录 Phase 9 的所有改进
- [ ] 列出新增的共享组件
- [ ] 说明代码质量提升

---

#### 任务 5.4: 最终提交和标签

- [ ] 创建最终提交
  ```
  docs(phase9): 完成 Phase 9 代码重构，更新文档

  Phase 9 完成总结：
  - 提取 5 个共享组件（ModeNavigationBar, ConfirmDialog, EditRecordDialog, UnifiedRecordCard, ControlButton）
  - 减少重复代码 600+ 行（-86%）
  - 4 个 Screen 文件代码量减少 441 行
  - UI 一致性提升至 95%
  - 代码可维护性大幅提升
  ```

- [ ] 打版本标签
  ```bash
  git tag v0.9-phase9
  git push --tags
  ```

---

## 📁 新增文件清单

```
ui/components/
├── navigation/
│   └── ModeNavigationBar.kt          (~40 行，泛型导航栏)
├── record/
│   ├── RecordCardMode.kt             (~10 行，枚举)
│   └── UnifiedRecordCard.kt          (~90 行，统一卡片)
├── button/
│   └── ControlButton.kt              (~25 行，从 StopwatchScreen 移动)
└── dialog/
    ├── ConfirmDialog.kt              (~50 行，通用确认对话框)
    └── EditRecordDialog.kt           (~60 行，通用编辑对话框)

总计新增: ~275 行（高复用组件）
```

---

## 📊 预期最终结果

### 代码量对比

| 文件 | 重构前 | 重构后 | 变化 |
|------|--------|--------|------|
| MainScreen.kt | 202 | 175 | -27 (-13%) |
| EventScreen.kt | 381 | 330 | -51 (-13%) |
| StopwatchScreen.kt | 598 | 450 | -148 (-25%) |
| HistoryScreen.kt | 1192 | 950 | -242 (-20%) |
| SettingsScreen.kt | 622 | 622 | 0 |
| **Screen 小计** | **2995** | **2527** | **-468 (-16%)** |
| **新增组件** | **0** | **275** | **+275** |
| **总计** | **2995** | **2802** | **-193 (-6%)** |

### 质量指标改进

| 指标 | 重构前 | 重构后 | 改进 |
|------|--------|--------|------|
| 重复代码量 | ~700 行 | ~100 行 | **-600 行 (-86%)** |
| UI 组件复用率 | 0% | 40%+ | **+40%** |
| UI 一致性 | 70% | 95% | **+25%** |
| 文件维护点（重复） | 多处 | 单处 | **集中化** |
| 组件可测试性 | 低 | 高 | **显著提升** |

---

## ⚠️ 风险和注意事项

### 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| 泛型参数复杂性 | 中 | 添加详细文档和使用示例 |
| TimeRecord vs TimeRecordEntity 兼容性 | 中 | 使用接口或适配器模式 |
| 回归测试覆盖 | 中 | 编写详细的测试清单 |
| 构建失败 | 低 | 每个阶段都运行构建测试 |

### 回滚计划

如果重构过程中出现严重问题：
1. 每个 Phase 都有独立的 Git 提交
2. 可以使用 `git revert` 回滚特定提交
3. 保留原有代码的 Git 历史

---

## 🎯 成功标准

Phase 9 重构成功的标志：
- ✅ 所有功能测试通过（事件、秒表、历史、设置）
- ✅ 代码重复率从 25% 降至 <5%
- ✅ 构建无错误无警告
- ✅ UI 一致性达到 95%
- ✅ 文档完整更新
- ✅ Git 提交历史清晰

---

## 📅 时间规划

| 阶段 | 工作量 | 开始日期 | 完成日期 |
|------|--------|---------|---------|
| Phase 9-1 | 1-1.5 天 | Day 1 | Day 2 中午 |
| Phase 9-2 | 0.5-1 天 | Day 2 下午 | Day 3 上午 |
| Phase 9-3 | 1-1.5 天 | Day 3 下午 | Day 4 |
| Phase 9-4 | 1 天 | Day 5 | Day 5 |
| Phase 9-5 | 1-1.5 天 | Day 6 | Day 7 |
| **总计** | **5-7 天** | - | - |

---

## 📖 参考资料

- [代码分析报告](./docs/code_analysis_2025-12-28.md)
- [Jetpack Compose 最佳实践](https://developer.android.com/jetpack/compose/mental-model)
- [Android 代码风格指南](https://developer.android.com/kotlin/style-guide)

---

**准备开始？运行以下命令查看当前待办事项：**
```bash
# 查看 Phase 9 待办事项（使用 TodoWrite 工具跟踪）
```

祝重构顺利！🚀
