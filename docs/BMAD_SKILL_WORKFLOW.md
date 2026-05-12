# BMad Skill 完整工作流指南

## 🎯 核心原则

**所有开发步骤必须通过 BMad Skill 触发**，禁止直接使用自然语言描述需求让 AI 生成代码。

---

## 📋 标准开发流程（7步法）

### 流程图

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ 1.头脑风暴   │ →   │ 2.Git Worktree│ →   │ 3.编写计划    │ →   │ 4.子代理开发  │
│  澄清需求   │     │  创建隔离空间  │     │ 拆2-5分钟任务  │     │ 独立上下文    │
└─────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
       ↓                                                                                  ↓
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ 7.完成分支    │ ←   │ 6.代码审查    │ ←   │ 5.测试驱动    │
│  验证收尾    │     │  自动审查    │     │ TDD循环      │
└──────────────┘     └──────────────┘     └──────────────┘
```

---

## 🔧 7步流程详解

### 步骤 1：头脑风暴 (Brainstorming)

**目标**：澄清需求，探索多种方案，确定最佳实现路径

**使用的 Skill**：`bmad-brainstorming`

**触发方式**：
```
Skill: bmad-brainstorming
```

**执行内容**：
1. **需求澄清**：AI 会问一系列问题，明确功能的目的、约束、成功标准
2. **方案探索**：提出 2-3 种实现方案及权衡
3. **推荐方案**：给出推荐并解释原因
4. **输出文档**：将批准的设计写入 `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md`

**示例**：
```
用户：我想添加一个每日一句功能

AI：Skill: bmad-brainstorming

AI 会询问：
- 这个功能的目标用户是谁？
- 名言的来源是什么？（硬编码 / API / 数据库）
- 是否需要支持多语言？
- 刷新频率是多少？

最终生成：docs/superpowers/specs/2024-05-12-daily-quote-design.md
```

---

### 步骤 2：Git Worktree 隔离

**目标**：创建独立的工作空间，避免影响主分支

**不使用 Skill**：使用脚本

**执行命令**：
```bash
./scripts/start-feature.sh <feature-name>
```

**自动执行**：
1. 创建 `.worktrees/<feature-name>` 目录
2. 创建新分支 `feature/<feature-name>`
3. 安装前后端依赖
4. 验证测试基线干净

**安全丢弃**：
```bash
git worktree remove -f .worktrees/<feature-name>
```

---

### 步骤 3：编写计划 (Writing Plans)

**目标**：将设计分解为 bite-sized 任务（每个 2-5 分钟）

**使用的 Skill**：`writing-plans`（通义灵码内置 skill）

**触发方式**：
```
Skill: writing-plans
```

**输入**：
- 步骤 1 生成的设计文档
- 或 PRD、架构设计等规划文档

**输出**：
- 计划文件：`docs/superpowers/plans/YYYY-MM-DD-<feature>.md`
- 包含：精确文件路径、完整代码、测试代码、验证命令、预期输出

**关键规则**：
- ❌ **禁止占位符**：不允许 "TBD"、"TODO"、"稍后实现"
- ✅ **小步快跑**：每个任务 2-5 分钟
- ✅ **完整代码**：每个任务包含完整的实现代码

**示例**：
```
用户：Skill: writing-plans

AI 会读取设计文档，然后生成计划：

Task 1: 创建 DailyQuoteDTO.java (2分钟)
  - 文件路径: app/src/main/java/.../DailyQuoteDTO.java
  - 完整代码: public record DailyQuoteDTO(String english, String chinese) {}
  - 验证命令: ./gradlew compileJava

Task 2: 创建 DailyQuoteService.java (3分钟)
  - 文件路径: app/src/main/java/.../DailyQuoteService.java
  - 完整代码: ...
  - 验证命令: ./gradlew test --tests DailyQuoteServiceTest
...
```

---

### 步骤 4：子代理开发 (Subagent-Driven Development)

**目标**：按照计划逐步实现代码，每个任务在独立上下文中执行

**使用的 Skill**：`subagent-driven-development`（通义灵码内置 skill）

**触发方式**：
```
Skill: subagent-driven-development
```

**输入**：
- 步骤 3 生成的计划文件

**执行过程**：
1. **按顺序执行任务**：每个任务 2-5 分钟
2. **独立上下文**：每个任务有独立的 AI 会话
3. **实时验证**：每个任务完成后运行测试
4. **标记完成**：勾选任务列表中的 checkbox

**关键特性**：
- ✅ **不会中途停止**：除非遇到 HALT 条件，否则持续执行直到所有任务完成
- ✅ **严格遵循计划**：不偏离计划中定义的范围
- ✅ **自动复用模式**：识别并复用项目中已有的抽象类和工具类

**替代方案**：如果 `subagent-driven-development` 不可用，可以使用 BMad 的 `bmad-dev-story`

```bash
# 先创建 Story
Skill: bmad-create-story

# 再实现 Story
Skill: bmad-dev-story
```

---

### 步骤 5：测试驱动 (TDD)

**目标**：确保代码质量，先写失败的测试，再写实现代码

**使用的 Skill**：`test-driven-development`（通义灵码内置 skill）

**触发方式**：
```
Skill: test-driven-development
```

**TDD 循环**：
1. **红（Red）**：写一个失败的测试
2. **绿（Green）**：写最小代码让测试通过
3. **重构（Refactor）**：优化代码结构，保持测试通过

**关键规则**：
- ❌ **没有先失败的测试，不写生产代码**
- ✅ **测试必须先失败**：验证测试本身是正确的
- ✅ **覆盖边界情况**：包括异常路径和错误处理

**示例**：
```
用户：Skill: test-driven-development

AI 会执行：

Step 1: 写测试（红）
@Test
void shouldReturnRandomQuote() {
    DailyQuoteService service = new DailyQuoteService();
    DailyQuoteDTO quote = service.getRandomQuote();
    assertNotNull(quote);
}
// 运行测试 → 失败（因为服务类还不存在）

Step 2: 写实现（绿）
public class DailyQuoteService {
    public DailyQuoteDTO getRandomQuote() {
        return new DailyQuoteDTO("Test", "测试");
    }
}
// 运行测试 → 通过

Step 3: 重构
// 优化代码结构，添加更多名言
...
```

**替代方案**：如果 `test-driven-development` 不可用，可以使用 IDE 内置的“生成单元测试”功能

---

### 步骤 6：代码审查 (Code Review)

**目标**：在独立会话中对代码变更进行三层并行 adversarial review

**使用的 Skill**：`bmad-code-review`

**重要**：必须在**全新的通义灵码会话**中执行

**触发方式**：
```
Skill: bmad-code-review
```

**输入**：
- 审查范围（未提交变更 / 分支差异 / 特定文件）
- Story 文件或计划文件（用于 Acceptance Auditor）

**执行过程**：
1. **Step 1: Gather Context** - 收集上下文
2. **Step 2: Review** - 三层并行审查
   - Blind Hunter（盲审猎人）：仅看 diff，无偏见
   - Edge Case Hunter（边界案例猎人）：结合项目架构
   - Acceptance Auditor（验收审计员）：对照 Story/计划验收标准
3. **Step 3: Triage** - 问题分级（Critical / Important / Minor）
4. **Step 4: Present** - 输出报告

**输出**：
```markdown
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

### 步骤 7：完成分支 (Finishing a Development Branch)

**目标**：验证所有工作完成，决定如何集成分支

**使用的 Skill**：`finishing-a-development-branch`（通义灵码内置 skill）

**触发方式**：
```
Skill: finishing-a-development-branch
```

**执行内容**：
1. **验证测试通过**：运行所有单元测试和集成测试
2. **检查代码审查**：确认所有 Critical/Important 问题已修复
3. **提供选项**：
   - 选项 1：本地合并回主分支
   - 选项 2：推送并创建 Pull Request
   - 选项 3：保持分支不变（稍后处理）
   - 选项 4：丢弃此工作（需用户输入 "discard" 确认）

**示例**：
```
用户：Skill: finishing-a-development-branch

AI 会执行：
- 运行测试：✅ 全部通过
- 检查 CR：✅ 所有 Critical 问题已修复
- 提供选项：
  1. Merge to main
  2. Create PR
  3. Keep branch
  4. Discard

用户选择：1

AI 执行：
git checkout main
git merge feature/daily-quote
git branch -d feature/daily-quote
```

---

## 🚫 禁止行为

### ❌ 错误做法

1. **跳过头脑风暴**
   ```
   # 错误：直接开始编码
   用户：帮我实现一个每日一句功能
   
   # 正确：先进行头脑风暴
   用户：Skill: bmad-brainstorming
   ```

2. **不在 Worktree 中开发**
   ```
   # 错误：直接在主分支上修改
   git checkout main
   # 开始编码...
   
   # 正确：使用 Worktree 隔离
   ./scripts/start-feature.sh daily-quote
   ```

3. **不编写计划直接开发**
   ```
   # 错误：没有计划就开始编码
   用户：帮我写代码
   
   # 正确：先编写计划
   用户：Skill: writing-plans
   ```

4. **在同一会话中既开发又审查**
   ```
   # 错误：在开发会话中要求审查
   用户：帮我看看这段代码有什么问题
   
   # 正确：在新会话中启动 Code Review
   用户：Skill: bmad-code-review
   ```

5. **不使用 TDD**
   ```
   # 错误：先写实现代码，后补测试
   用户：帮我实现这个功能
   
   # 正确：先写失败的测试
   用户：Skill: test-driven-development
   ```

### ✅ 正确做法

1. **始终遵循 7 步流程**
   - 头脑风暴 → Worktree → 编写计划 → 子代理开发 → TDD → Code Review → 完成分支

2. **每个步骤使用对应的 Skill**
   - 步骤 1：`Skill: bmad-brainstorming`
   - 步骤 3：`Skill: writing-plans`
   - 步骤 4：`Skill: subagent-driven-development` 或 `bmad-dev-story`
   - 步骤 5：`Skill: test-driven-development`
   - 步骤 6：`Skill: bmad-code-review`（新会话）
   - 步骤 7：`Skill: finishing-a-development-branch`

3. **会话隔离**
   - 开发会话：只负责步骤 1-5
   - Review 会话：只负责步骤 6
   - 完成会话：只负责步骤 7

---

## 🔄 完整示例：每日一句功能

### 步骤 1：头脑风暴
```bash
# 在通义灵码侧边栏
Skill: bmad-brainstorming

# AI 询问
用户：我想添加一个每日一句功能，显示敏捷开发相关的名言

AI 会询问：
- 这个功能的目标用户是谁？
- 名言的来源是什么？（硬编码 / API / 数据库）
- 是否需要支持多语言？
- 刷新频率是多少？

# 最终生成
docs/superpowers/specs/2024-05-12-daily-quote-design.md
```

### 步骤 2：Git Worktree 隔离
```bash
./scripts/start-feature.sh daily-quote

# 自动执行：
# - 创建 .worktrees/daily-quote 目录
# - 创建新分支 feature/daily-quote
# - 安装前后端依赖
# - 验证测试基线干净
```

### 步骤 3：编写计划
```bash
# 在通义灵码侧边栏
Skill: writing-plans

# AI 读取设计文档，生成计划
docs/superpowers/plans/2024-05-12-daily-quote.md

# 计划包含：
Task 1: 创建 DailyQuoteDTO.java (2分钟)
Task 2: 创建 DailyQuoteService.java (3分钟)
Task 3: 修改 AgileEnglishController.java (2分钟)
Task 4: 修改 AgileEnglishPage.tsx (3分钟)
Task 5: 生成单元测试 (2分钟)
...
```

### 步骤 4：子代理开发
```bash
# 在通义灵码侧边栏
Skill: subagent-driven-development

# 提供计划文件路径
docs/superpowers/plans/2024-05-12-daily-quote.md

# AI 按顺序执行每个任务：
# - Task 1: 创建 DailyQuoteDTO.java
# - Task 2: 创建 DailyQuoteService.java
# - Task 3: 修改 AgileEnglishController.java
# - Task 4: 修改 AgileEnglishPage.tsx
# - Task 5: 生成单元测试
# ...

# 替代方案：使用 BMad Story
# Skill: bmad-create-story
# Skill: bmad-dev-story
```

### 步骤 5：测试驱动
```bash
# 在通义灵码侧边栏
Skill: test-driven-development

# AI 执行 TDD 循环：
# Step 1: 写测试（红）
@Test
void shouldReturnRandomQuote() {
    DailyQuoteService service = new DailyQuoteService();
    DailyQuoteDTO quote = service.getRandomQuote();
    assertNotNull(quote);
}
// 运行测试 → 失败

# Step 2: 写实现（绿）
public class DailyQuoteService {
    public DailyQuoteDTO getRandomQuote() {
        // 实现代码
    }
}
// 运行测试 → 通过

# Step 3: 重构
// 优化代码结构
```

### 步骤 6：代码审查
```bash
# 提交代码
git add .
git commit -m "feat: 实现每日一句功能"

# 关闭当前会话，创建新会话
# 在新会话中输入
Skill: bmad-code-review

# 提供信息
审查范围：Uncommitted changes
Story 文件：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

# AI 输出报告
# Code Review Report
# ## Critical Issues
# 1. 事务内调用外部 API
#    - Rule violated: 事务边界规则
#    - Evidence: AgileEnglishService.java:45
#    - Suggestion: 改为 Redis Stream 异步处理
# ...
```

### 步骤 7：完成分支
```bash
# 回到开发会话，修复 Critical 问题
git add .
git commit -m "fix: 根据 Code Review 修复事务边界问题"

# 在新会话中
Skill: finishing-a-development-branch

# AI 执行：
# - 运行测试：✅ 全部通过
# - 检查 CR：✅ 所有 Critical 问题已修复
# - 提供选项：
#   1. Merge to main
#   2. Create PR
#   3. Keep branch
#   4. Discard

# 用户选择：1
# AI 执行：
git checkout main
git merge feature/daily-quote
git branch -d feature/daily-quote
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
