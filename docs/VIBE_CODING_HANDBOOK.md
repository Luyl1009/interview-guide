# 团队 Vibe Coding + BMad + Harness 使用手册

## 📖 1. 概述

本手册旨在指导团队成员在 **interview-guide** 项目中高效、规范地使用 **Vibe Coding（氛围编程）**、**BMad（规范驱动开发）** 和 **Harness（工程化质量护栏）**。

*   **Vibe Coding**：通过自然语言意图驱动，利用 AI 快速生成代码骨架。
*   **BMad**：通过 Story 和规范约束，确保 AI 生成的代码符合项目架构与业务逻辑。
*   **Harness**：通过自动化测试、安全扫描和规则自查，保障生产环境的稳定性与安全性。

---

## 🛠️ 2. 环境准备

### 2.1 必备工具
*   **IDE**：IntelliJ IDEA (推荐) 或 VS Code。
*   **AI 插件**：通义灵码 (Tongyi Lingma) 最新版。
*   **技能库**：确保项目根目录下的 `_bmad` 和 `.lingma/rules` 已同步到最新版本。

### 2.2 核心配置文件
*   **[project-context.md](file:///Users/lyl/IdeaProjects/interview-guide/_bmad-output/project-context.md)**：AI 的全局记忆，包含技术栈版本、模块划分和核心规范。
*   **[vibe_coding_bmad.md](file:///Users/lyl/IdeaProjects/interview-guide/.lingma/rules/vibe_coding_bmad.md)**：IDE 实时执行的规则，包含代码生成规范和 AI 自查清单。
*   **[ai-error-patterns.md](file:///Users/lyl/IdeaProjects/interview-guide/_bmad-output/ai-error-patterns.md)**：错误模式库，用于记录并迭代优化 AI 行为。

---

## 🚀 3. 标准开发工作流 (SOP) - 7步法

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

### 阶段零：环境隔离 (Worktree Isolation) 🔒
**强制要求**：所有新功能开发或实验性代码必须在 Git Worktree 中进行。
1. **启动功能**：运行 `./scripts/start-feature.sh <feature-name>`。
2. **自动配置**：脚本会自动创建隔离目录、新分支并安装前后端依赖。
3. **安全丢弃**：如果需求取消或代码不满足预期，直接运行 `git worktree remove -f .worktrees/<feature-name>`，主分支不受任何影响。

### 阶段一：需求澄清与头脑风暴 (Brainstorming)
1. **执行 Skill**：在通义灵码侧边栏输入：
   ```
   Skill: bmad-brainstorming
   ```
2. **AI 会询问**：
   - 这个功能的目标用户是谁？
   - 有哪些技术约束？
   - 成功标准是什么？
3. **输出文档**：`docs/superpowers/specs/YYYY-MM-DD-<feature>-design.md`

### 阶段二：Git Worktree 隔离
1. **执行命令**：
   ```bash
   ./scripts/start-feature.sh <feature-name>
   ```
2. **自动执行**：
   - 创建 `.worktrees/<feature-name>` 目录
   - 创建新分支 `feature/<feature-name>`
   - 安装前后端依赖
   - 验证测试基线干净

### 阶段三：编写计划 (Writing Plans)
1. **执行 Skill**：
   ```
   Skill: writing-plans
   ```
2. **提供输入**：阶段一生成的设计文档
3. **输出**：`docs/superpowers/plans/YYYY-MM-DD-<feature>.md`
4. **关键规则**：
   - ❌ **禁止占位符**：不允许 "TBD"、"TODO"、"稍后实现"
   - ✅ **小步快跑**：每个任务 2-5 分钟
   - ✅ **完整代码**：每个任务包含完整的实现代码

### 阶段四：子代理开发 (Subagent-Driven Development)
1. **执行 Skill**：
   ```
   Skill: subagent-driven-development
   ```
2. **提供输入**：阶段三生成的计划文件
3. **AI 自动执行**：
   - 按顺序执行每个任务（每个 2-5 分钟）
   - 每个任务在独立上下文中
   - 实时验证测试结果
   - 标记任务完成状态
4. **替代方案**：如果 `subagent-driven-development` 不可用，可以使用 BMad 的 Story 流程：
   ```
   Skill: bmad-create-story
   Skill: bmad-dev-story
   ```

### 阶段五：测试驱动 (TDD)
1. **执行 Skill**：
   ```
   Skill: test-driven-development
   ```
2. **TDD 循环**：
   - 🔴 **红**：写一个失败的测试
   - 🟢 **绿**：写最小代码让测试通过
   - 🔵 **重构**：优化代码结构，保持测试通过
3. **关键规则**：
   - ❌ **没有先失败的测试，不写生产代码**
   - ✅ **测试必须先失败**：验证测试本身是正确的
   - ✅ **覆盖边界情况**：包括异常路径和错误处理

### 阶段六：独立 Code Review 会话 🔍
**强制要求**：功能开发完成后，必须在**全新的通义灵码会话**中执行 Code Review。
1. **准备 Review**：
    *   确保所有修改已暂存：`git add .`
    *   确认计划/Story 文件路径
    *   提交当前工作：`git commit -m "feat: 完成XX功能"`
2. **启动新会话**：关闭当前侧边栏对话，点击 "+" 创建新对话。
3. **执行 Skill**：在新会话中输入：
   ```
   Skill: bmad-code-review
   ```
4. **提供上下文**：
    *   审查范围：`Uncommitted changes`（或指定文件）
    *   计划/Story 文件：提供对应的路径
5. **查看报告**：AI 会输出结构化审查报告（Critical / Important / Minor）。
6. **修复问题**：回到开发会话，优先修复 Critical 和 Important 问题。
7. **重新提交**：`git add . && git commit -m "fix: 根据 Code Review 修复问题"`

**详细操作指南**：参见 [CODE_REVIEW_GUIDE.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/CODE_REVIEW_GUIDE.md)

### 阶段七：完成分支 (Finishing a Development Branch)
1. **执行 Skill**：
   ```
   Skill: finishing-a-development-branch
   ```
2. **AI 自动执行**：
   - ✅ 运行所有单元测试和集成测试
   - ✅ 检查 Code Review 结果
   - ✅ 提供选项：
     1. Merge to main（本地合并）
     2. Create PR（创建 Pull Request）
     3. Keep branch（保持分支不变）
     4. Discard（丢弃此工作）

---

## 🤖 4. AI 自动化自查清单 (Self-Check Protocol)

所有成员在提交代码前，应确认 AI 已通过以下自查（查看对话末尾的 `✅ Self-Check Passed` 标记）：

| 维度 | 检查项 | 违规修正动作 |
| :--- | :--- | :--- |
| **架构规范** | Controller 是否只负责路由？DTO 转换是否完整？ | 自动下沉逻辑至 Service，补全 MapStruct 映射 |
| **胶水编程** | 是否复用了 `AbstractStreamConsumer` 等模板？ | 自动替换为项目现有的抽象类实现 |
| **前端样式** | 是否混入了 Ant Design 或内联样式？ | 强制转换为 TailwindCSS 类名 |
| **事务边界** | Service 层是否包含了耗时的外部 API 调用？ | 自动重构为 Redis Stream 异步处理 |
| **类型安全** | TypeScript 接口是否与后端 DTO 严格匹配？ | 自动同步更新前端 Interface 定义 |

---

## 🔄 5. 准则迭代流程

为了保持规则的鲜活度，我们采用以下闭环流程：

1. **发现**：在 Code Review 中发现 AI 重复犯错（如：再次引入 Ant Design）。
2. **记录**：在 `ai-error-patterns.md` 中按模板新增一条记录。
3. **回顾**：每两周运行 `bmad-retrospective` 分析错误模式。
4. **转化**：将高频错误点加入 `.lingma/rules/vibe_coding_bmad.md` 的自查清单。
5. **同步**：通过 Git 提交规则变更，全员同步最新约束。

---

## 🔍 6. Code Review 机制详解

### 为什么需要独立会话？

| 认知偏差类型 | 说明 | 影响 |
|------------|------|------|
| **确认偏误** | 开发者倾向于证明自己写的代码是正确的 | 忽略潜在问题 |
| **沉没成本谬误** | 投入了大量精力，不愿承认缺陷 | 拒绝必要的重构 |
| **熟悉度盲区** | 对自己的代码太熟悉，看不到明显问题 | 遗漏边界情况 |
| **目标污染** | 既要实现功能又要审查质量，注意力分散 | 两者都做不好 |

### BMad Code Review 三层并行审查

1. **Blind Hunter（盲审猎人）**
   - **输入**：仅接收 diff，无项目上下文
   - **目标**：发现通用代码质量问题（命名、结构、重复等）
   - **特点**：完全隔离，避免先入为主

2. **Edge Case Hunter（边界案例猎人）**
   - **输入**：diff + 项目读取权限
   - **目标**：发现边界条件、异常处理、并发安全问题
   - **特点**：结合项目架构分析

3. **Acceptance Auditor（验收审计员）**
   - **输入**：diff + Story 文件 + 上下文文档
   - **目标**：检查是否符合 Story 的验收标准
   - **特点**：对照需求规格进行审计

### 问题分级标准

- **Critical（严重）**：必须立即修复（阻塞性问题）
  - 示例：空指针风险、事务内调用外部 API、安全漏洞
  
- **Important（重要）**：修复后再继续
  - 示例：缺少单元测试、DTO 直接返回 Entity、硬编码密钥
  
- **Minor（轻微）**：记录，可延后处理
  - 示例：注释不完整、日志格式不规范、变量命名不够清晰

---

## 💡 7. 常用指令速查

| 步骤 | 场景 | 推荐 Skill | 触发指令 |
|------|------|-----------|----------|
| **1** | 需求澄清 | `bmad-brainstorming` | `Skill: bmad-brainstorming` |
| **2** | Worktree 隔离 | 脚本 | `./scripts/start-feature.sh <name>` |
| **3** | 编写计划 | `writing-plans` | `Skill: writing-plans` |
| **4** | 子代理开发 | `subagent-driven-development` | `Skill: subagent-driven-development` |
| **4(替代)** | Story 开发 | `bmad-create-story` + `bmad-dev-story` | 依次执行两个 Skill |
| **5** | 测试驱动 | `test-driven-development` | `Skill: test-driven-development` |
| **6** | 代码审查 | `bmad-code-review` | ⚠️ **必须在新会话中**：`Skill: bmad-code-review` |
| **7** | 完成分支 | `finishing-a-development-branch` | `Skill: finishing-a-development-branch` |

---

## ⚠️ 8. 禁止事项 (Red Lines)

*   ❌ **禁止**直接返回 Entity 给前端，必须使用 DTO。
*   ❌ **禁止**在事务方法内同步调用 LLM 或 S3 等外部服务。
*   ❌ **禁止**在前端引入 Ant Design、Material UI 等非 TailwindCSS 库。
*   ❌ **禁止**裸抛 `RuntimeException`，必须使用 `BusinessException`。
*   ❌ **禁止**在同一个会话中既写代码又审查代码（必须使用独立会话）。
*   ❌ **禁止**忽略 Critical 级别的 Code Review 问题直接合并代码。
*   ❌ **禁止**在没有 `project-context.md` 上下文的情况下进行大规模重构。
