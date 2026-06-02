# Label 体系

ChronoMark 采用**命名空间前缀**的 label 体系，分为四个维度，外加少量社区协作惯例 label。

## type:（类型 · 蓝绿色系）

问题的性质，每个 issue 通常只有一个。

| Label | 颜色 | 说明 |
|---|---|---|
| `type: bug` | `#d73a4a` | 缺陷，功能未按预期工作 |
| `type: feature` | `#a2eeef` | 新功能 |
| `type: enhancement` | `#84b6eb` | 对现有功能的优化 |
| `type: docs` | `#0075ca` | 文档相关 |
| `type: chore` | `#fef2c0` | 构建、CI、依赖等杂务 |

## area:（模块 · 紫蓝色系）

问题影响的范围，对应项目的功能模块。

| Label | 颜色 | 说明 |
|---|---|---|
| `area: event-mode` | `#c5def5` | 事件模式 |
| `area: stopwatch` | `#c5def5` | 秒表模式 |
| `area: history` | `#c5def5` | 历史记录 |
| `area: settings` | `#c5def5` | 设置 / 工作区 |
| `area: ui` | `#c5def5` | Compose UI / 主题 |
| `area: build` | `#c5def5` | Gradle / CI / 发布 |

## priority:（优先级 · 红黄刻度）

重要程度。**低优先级不打标签**（默认即为低）。

| Label | 颜色 | 说明 |
|---|---|---|
| `priority: high` | `#b60205` | 高优先级，应尽快处理 |
| `priority: medium` | `#fbca04` | 中优先级 |

## status:（状态）

issue 的流转状态。

| Label | 颜色 | 说明 |
|---|---|---|
| `status: triage` | `#ededed` | 待分诊（新 issue 默认） |
| `status: blocked` | `#000000` | 被其他事项阻塞 |

## 社区协作 label（沿用 GitHub 默认）

| Label | 颜色 | 说明 |
|---|---|---|
| `good first issue` | `#7057ff` | 适合新贡献者上手 |
| `help wanted` | `#008672` | 需要更多关注 / 协助 |
| `duplicate` | `#cfd3d7` | 重复的 issue 或 PR |
| `wontfix` | `#ffffff` | 不予处理 |

## 命名约定

- 全小写，前缀统一，使用 `前缀: 值` 格式
- 同一维度使用同色系；优先级用由黄到红的刻度色
- 避免消极 label（如 `low priority`），改用"不打标签"
