# BMad Skill 完整工作流指南

## 🎯 核心原则

**所有开发步骤必须通过 BMad Skill 触发**，禁止直接使用自然语言描述需求让 AI 生成代码。

---

## 📋 标准开发流程

### 流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    完整开发工作流                             │
└─────────────────────────────────────────────────────────────┘

1️⃣  Worktree 隔离
   └─> ./scripts/start-feature.sh <feature-name>

2️⃣  Story 创建 (BMad)
   └─> Skill: bmad-create-story
       └─> 输出: _bmad-output/implementation-artifacts/story-*.md

3️⃣  Story 实现 (BMad + Vibe Coding)
   └─> Skill: bmad-dev-story
       └─> 自动按照任务列表实现代码

4️⃣  质量自查 (Harness)
   └─> AI 自动执行 Self-Check Protocol
       └─> 输出: ✅ Self-Check Passed

5️⃣  提交代码
   └─> git add . && git commit -m "feat: 完成XX功能"

6️⃣  Code Review (独立会话)
   └─> Skill: bmad-code-review (在新会话中)
       └─> 输出: Critical/Important/Minor 问题报告

7️⃣  修复问题
   └─> 回到开发会话修复 Critical/Important 问题
       └─> git commit -m "fix: 根据 Code Review 修复问题"

8️⃣  合并分支
   └─> 所有问题解决后，合并到主分支
```

---

## 🔧 核心 Skill 详解

### 1. `bmad-create-story` - Story 创建

**用途**：将需求转化为结构化的 Story 文件，包含完整的上下文信息

**触发方式**：
```
Skill: bmad-create-story
```

**输入**：
- Epic 和 Story 选择（从 sprint-status.yaml）
- 或新功能需求描述

**输出**：
- Story 文件：`_bmad-output/implementation-artifacts/story-*.md`
- 包含：验收标准、任务列表、技术约束、相关文件引用

**AI 自动执行**：
1. 读取项目上下文（project-context.md）
2. 分析 PRD、架构设计、UX 设计等文档
3. 提取相关技术约束和模式
4. 生成包含完整上下文的 Story 文件

**示例场景**：
```
用户：我想添加一个每日一句功能

AI：Skill: bmad-create-story

AI 会询问：
- 这个功能属于哪个 Epic？
- 有哪些验收标准？
- 需要哪些技术约束？

最终生成：story-1-2-daily-quote.md
```

---

### 2. `bmad-dev-story` - Story 实现

**用途**：按照 Story 文件中的任务列表，自动化实现代码

**触发方式**：
```
Skill: bmad-dev-story
```

**输入**：
- Story 文件路径（如 `_bmad-output/implementation-artifacts/story-1-2-daily-quote.md`）

**输出**：
- 完整的代码实现
- 更新的 Story 文件（任务勾选状态、变更日志）

**AI 自动执行**：
1. 读取 Story 文件中的所有任务和验收标准
2. 按顺序执行每个任务
3. 遵循项目现有的架构模式和编码规范
4. 生成单元测试
5. 更新 Story 文件的状态

**关键特性**：
- **不会中途停止**：除非遇到 HALT 条件，否则持续执行直到所有任务完成
- **严格遵循 Story**：不偏离 Story 中定义的范围
- **自动复用模式**：识别并复用项目中已有的抽象类和工具类

**示例场景**：
```
用户：Skill: bmad-dev-story

AI：请提供 Story 文件路径

用户：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

AI 开始自动实现：
- Step 1: 创建 DailyQuoteDTO
- Step 2: 创建 DailyQuoteService
- Step 3: 修改 AgileEnglishController
- Step 4: 修改 AgileEnglishPage.tsx
- Step 5: 生成单元测试
- ...
```

---

### 3. `bmad-code-review` - Code Review

**用途**：在独立会话中对代码变更进行三层并行 adversarial review

**触发方式**：
```
Skill: bmad-code-review
```

**重要**：必须在**全新的通义灵码会话**中执行

**输入**：
- 审查范围（未提交变更 / 分支差异 / 特定文件）
- Story 文件路径（用于 Acceptance Auditor）

**输出**：
- 结构化审查报告（Critical / Important / Minor）
- 每个问题的详细描述、证据、修复建议

**AI 自动执行**：
1. **Step 1: Gather Context** - 收集上下文
2. **Step 2: Review** - 三层并行审查
   - Blind Hunter（盲审猎人）：仅看 diff，无偏见
   - Edge Case Hunter（边界案例猎人）：结合项目架构
   - Acceptance Auditor（验收审计员）：对照 Story 验收标准
3. **Step 3: Triage** - 问题分级
4. **Step 4: Present** - 输出报告

**示例场景**：
```
# 在新会话中输入
用户：Skill: bmad-code-review

AI：What do you want to review?

用户：Uncommitted changes

AI：Is there a spec or story file?

用户：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

AI 开始执行三层审查，最终输出：
# Code Review Report
## Summary
- Files changed: 5
- Lines added: +120
- Total findings: 8 (Critical: 2, Important: 4, Minor: 2)

## Critical Issues
1. [问题标题]
   - Rule violated: ...
   - Evidence: ...
   - Suggestion: ...
...
```

---

## 🚫 禁止行为

### ❌ 错误做法

1. **跳过 Story 创建**
   ```
   # 错误：直接描述需求让 AI 生成代码
   用户：帮我实现一个每日一句功能
   
   # 正确：先创建 Story
   用户：Skill: bmad-create-story
   ```

2. **在同一会话中既开发又审查**
   ```
   # 错误：在开发会话中要求审查
   用户：帮我看看这段代码有什么问题
   
   # 正确：在新会话中启动 Code Review
   用户：Skill: bmad-code-review
   ```

3. **不使用 Skill 直接生成代码**
   ```
   # 错误：自然语言描述需求
   用户：在 Controller 中添加一个接口
   
   # 正确：使用 bmad-dev-story
   用户：Skill: bmad-dev-story
   ```

### ✅ 正确做法

1. **始终通过 Skill 触发**
   - 创建功能 → `Skill: bmad-create-story`
   - 实现代码 → `Skill: bmad-dev-story`
   - 审查代码 → `Skill: bmad-code-review`

2. **会话隔离**
   - 开发会话：只负责 Story 实现
   - Review 会话：只负责代码审查

3. **遵循 Story 范围**
   - 不偏离 Story 中定义的任务
   - 如需扩展，先更新 Story 文件

---

## 📊 Skill 使用对比

| 维度 | 不使用 Skill | 使用 Skill |
|------|-------------|-----------|
| **需求理解** | AI 可能误解意图 | Story 文件明确验收标准 |
| **代码质量** | 依赖 AI 自由发挥 | 严格遵循项目模式 |
| **审查客观性** | 自我审查有偏见 | 独立会话 + 三层审查 |
| **可追溯性** | 难以追踪决策过程 | Story 文件记录完整上下文 |
| **团队协作** | 每个人风格不同 | 标准化工作流程 |
| **问题发现率** | ~60% | ~95% |

---

## 🔄 完整示例：每日一句功能

### 步骤 1：Worktree 隔离
```bash
./scripts/start-feature.sh daily-quote
```

### 步骤 2：创建 Story
```
# 在通义灵码侧边栏
Skill: bmad-create-story

# AI 询问
用户：我想添加一个每日一句功能，显示敏捷开发相关的名言

# AI 生成
_bmad-output/implementation-artifacts/story-1-2-daily-quote.md
```

### 步骤 3：实现 Story
```
# 在新会话中
Skill: bmad-dev-story

# 提供 Story 文件
_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

# AI 自动实现
- 创建 DailyQuoteDTO.java
- 创建 DailyQuoteService.java
- 修改 AgileEnglishController.java
- 修改 AgileEnglishPage.tsx
- 生成单元测试
```

### 步骤 4：提交代码
```bash
git add .
git commit -m "feat: 实现每日一句功能"
```

### 步骤 5：Code Review
```
# 在新会话中
Skill: bmad-code-review

# 提供信息
审查范围：Uncommitted changes
Story 文件：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

# AI 输出报告
# Code Review Report
## Critical Issues
1. 事务内调用外部 API
   - Rule violated: 事务边界规则
   - Evidence: AgileEnglishService.java:45
   - Suggestion: 改为 Redis Stream 异步处理
...
```

### 步骤 6：修复问题
```
# 回到开发会话
# 根据 Review 报告修复 Critical 问题
git add .
git commit -m "fix: 根据 Code Review 修复事务边界问题"
```

### 步骤 7：合并分支
```bash
git checkout main
git merge feature/daily-quote
```

---

## 🎓 为什么必须使用 Skill？

### 1. 消除认知偏差
- **确认偏误**：开发者倾向于证明自己写的代码是正确的
- **沉没成本谬误**：投入越多越不愿放弃
- **熟悉度盲区**：太熟悉导致视而不见

### 2. 保证一致性
- 所有团队成员遵循相同的工作流
- 代码风格和架构模式保持一致
- 便于 Code Review 和知识传承

### 3. 提升质量
- Story 文件确保需求理解准确
- bmad-dev-story 严格遵循项目模式
- bmad-code-review 三层并行审查覆盖全面

### 4. 可追溯性
- Story 文件记录完整的决策过程
- 便于后续维护和迭代
- 新人可以快速理解历史背景

---

## 📚 相关文档

- [VIBE_CODING_HANDBOOK.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/VIBE_CODING_HANDBOOK.md) - 团队 Vibe Coding 手册
- [CODE_REVIEW_GUIDE.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/CODE_REVIEW_GUIDE.md) - Code Review 操作指南
- [CODE_REVIEW_IMPROVEMENT.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/CODE_REVIEW_IMPROVEMENT.md) - Code Review 改进说明
- [vibe_coding_bmad.md](file:///Users/lyl/IdeaProjects/interview-guide/.lingma/rules/vibe_coding_bmad.md) - AI 实时执行规则

---

## ✨ 总结

**BMad Skill 工作流的核心价值**：

1. ✅ **标准化**：所有开发步骤通过 Skill 触发
2. ✅ **自动化**：AI 自动执行复杂分析和代码生成
3. ✅ **客观性**：独立会话消除认知偏差
4. ✅ **可追溯**：Story 文件记录完整上下文
5. ✅ **高质量**：三层并行审查确保代码质量

**记住**：不要跳过任何 Skill，每一步都是质量保证的关键环节。
