# Agile English 场景分类体系重构设计文档

## 1. 概述

### 背景
现有 Agile English 功能仅覆盖敏捷仪式类场景（Sprint Planning、Daily Standup、Retrospective 等）。用户反馈需要扩展至更广泛的开发工作场景，包括深度 Code Review、跨团队协作、日常开发交流。

### 目标
- 将场景从单一列表重构为三级分类体系（敏捷仪式 / 技术交流 / 团队协作）
- 新增 3 个场景类型，每个场景配备独立的 Prompt 模板
- 保持向后兼容，零数据库迁移成本
- 评估维度按场景类型差异化

### 非目标
- 不支持语音输入（ASR）
- 不引入角色 × 场景组合模式
- 不增加社交/排行榜功能

---

## 2. 数据模型与分类设计

### 2.1 分类体系

| 分类编码 | 分类名称 | 图标（Lucide） | 包含场景 |
|---------|---------|---------------|---------|
| `agile` | 敏捷仪式 | `Users` | Sprint Planning、Daily Standup、Retrospective、Sprint Review |
| `tech` | 技术交流 | `Code2` | Code Review（基础）、Code Review（深度/架构）、技术面试英语 |
| `team` | 团队协作 | `UsersRound` | 跨团队沟通（PM/设计/运维）、日常开发交流 |

### 2.2 枚举扩展

```java
public enum ScenarioCategory {
  AGILE("agile", "敏捷仪式", "Users"),
  TECH("tech", "技术交流", "Code2"),
  TEAM("team", "团队协作", "UsersRound");

  private final String code;
  private final String displayName;
  private final String lucideIcon;
}
```

```java
public enum ScenarioType {
  // 现有场景
  SPRINT_PLANNING("agile", "Sprint Planning", "agile-english-multiturn.st", 2),
  DAILY_STANDUP("agile", "Daily Standup", "agile-english-multiturn.st", 1),
  RETROSPECTIVE("agile", "Retrospective", "agile-english-multiturn.st", 2),

  // 新增场景
  CODE_REVIEW_ADVANCED("tech", "深度 Code Review", "advanced-code-review.st", 4),
  CROSS_TEAM_COMM("team", "跨团队沟通", "cross-team-collaboration.st", 3),
  DAILY_DEV_COMM("team", "日常开发交流", "daily-dev-communication.st", 2);

  private final String categoryCode;
  private final String displayName;
  private final String promptTemplate;
  private final int difficultyLevel; // 1-5
}
```

### 2.3 数据库影响

- `agile_english_sessions.scenario_type` 为 `VARCHAR`，**无需改表结构**
- 新场景类型值直接存入（如 `"CODE_REVIEW_ADVANCED"`）

---

## 3. 后端架构

### 3.1 API 变更

**新增响应 DTO：**

```java
public record ScenarioCategoryResponse(
    String code,
    String name,
    String icon,
    List<ScenarioInfo> scenarios
) {}

public record ScenarioInfo(
    String code,
    String name,
    String description,
    String categoryCode,
    int difficultyLevel
) {}
```

**Controller 层修改：**

```java
@GetMapping("/scenarios")
public Result<List<ScenarioCategoryResponse>> getScenarios() {
    // 返回分组后的场景列表
}

// 现有接口保持兼容
@PostMapping("/generate-scenario")
public Result<ScenarioResponse> generateScenario(
    @RequestBody @Valid GenerateScenarioRequest request
) {
    // request.scenarioType 仍使用 String，内部映射到枚举
}
```

### 3.2 Prompt 模板文件

```
resources/prompts/
├── agile-english-multiturn.st          # 现有：敏捷仪式
├── advanced-code-review.st             # 新增：深度 Code Review
├── cross-team-collaboration.st         # 新增：跨团队协作
└── daily-dev-communication.st          # 新增：日常开发交流
```

### 3.3 Prompt 模板规范

每个新模板遵循统一结构：
- **角色设定**：AI 扮演的角色（如 Senior Architect、Product Manager）
- **场景背景**：具体的上下文（如"你正在评审一个微服务拆分方案"）
- **对话目标**：用户需要练习的技能点
- **评估维度**：语法、专业术语、表达清晰度、沟通技巧
- **中英对照**：关键术语提供中文翻译（遵循现有英语对话场景中英对照规范）

### 3.4 场景路由服务

```java
@Service
public class ScenarioPromptService {
  private final Map<ScenarioType, String> promptTemplates;

  public String getPromptTemplate(ScenarioType type) {
    return promptTemplates.getOrDefault(
        type,
        promptTemplates.get(ScenarioType.DAILY_STANDUP) // fallback
    );
  }
}
```

### 3.5 评估维度差异化

| 场景类型 | 核心评估维度 | 权重调整 |
|---------|------------|---------|
| 深度 Code Review | 技术术语准确度、建议建设性 | 技术术语 40%，语法 20% |
| 跨团队协作 | 沟通清晰度、需求理解力 | 沟通技巧 40%，语法 30% |
| 日常开发交流 | 表达自然度、响应及时性 | 流利度 40%，语法 30% |

---

## 4. 前端交互设计

### 4.1 场景选择器重构

- 将现有下拉框改为 **分组选择卡片**
- 先展示 3 个分类标签（敏捷仪式 / 技术交流 / 团队协作）
- 点击分类后展开该分类下的场景卡片
- 每个卡片显示：场景名称、难度星级、简要描述

### 4.2 历史记录页适配

- 增加"分类筛选"标签页
- 能力报告按分类展示雷达图（敏捷仪式/技术交流/团队协作 三维能力分布）

### 4.3 API 对接变更

```typescript
// api/agileEnglish.ts 新增接口
export const getScenarioCategories = () =>
  request.get<ScenarioCategoryResponse[]>('/api/agile-english/scenarios');
```

---

## 5. 数据迁移与兼容性

- **零迁移成本**：现有 `scenario_type` 为 VARCHAR，存量数据无需处理
- **Fallback 策略**：后端对未知场景类型默认使用 `DAILY_STANDUP` 模板
- **API 兼容**：现有 `POST /generate-scenario` 接口字段不变，前端逐步升级

---

## 6. 测试策略

| 层级 | 测试内容 |
|------|---------|
| 单元测试 | `ScenarioType` 分类映射、Prompt 模板加载、Eval 权重计算 |
| 集成测试 | API 返回分组结构正确、新场景全流程对话 |
| 前端测试 | 分类选择器交互、场景卡片渲染、历史页筛选 |

---

## 7. 边界情况与错误处理

| 场景 | 处理策略 |
|------|---------|
| 前端传入未知 scenarioType | 后端 Fallback 到 `DAILY_STANDUP`，记录 warn 日志 |
| Prompt 模板文件缺失 | 启动时报错，阻止应用启动（fail-fast） |
| 旧版本前端调用新后端 | `/scenarios` 返回新结构，旧前端忽略多余字段，核心流程不受影响 |
| 分类下无场景 | 返回空列表，前端展示"暂无场景"占位 |

---

## 8. 任务分解预估

| 任务 | 预估时间 | 依赖 |
|------|---------|------|
| 扩展 ScenarioType/ScenarioCategory 枚举 | 15 min | 无 |
| 新增 3 个 Prompt 模板 | 45 min | 无 |
| 修改 `/scenarios` API 返回分组结构 | 20 min | 枚举扩展 |
| 实现评估维度差异化 | 30 min | Prompt 模板 |
| 前端场景选择器重构（分组卡片） | 40 min | API 修改 |
| 前端历史页分类筛选 | 25 min | API 修改 |
| 单元测试编写 | 30 min | 后端完成 |
| 集成测试验证 | 20 min | 全部完成 |

**总计：约 3.5 小时**
