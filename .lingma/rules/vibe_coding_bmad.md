---
trigger: always_on
---

# Vibe Coding + BMad + Harness 规则

## 核心原则
1. **意图驱动 (Vibe)**：优先通过自然语言描述需求，利用 AI 生成代码骨架和逻辑。
2. **规范约束 (BMad)**：所有功能实现必须遵循 `bmad-create-story` 生成的 Story 上下文，确保架构一致性。
3. **质量护栏 (Harness)**：代码生成后必须经过自动化测试（JUnit/React Testing Library）和安全扫描。

## 实施指南

### 1. 开发前准备
- **读取上下文**：在开始任何编码任务前，必须先阅读 `_bmad-output/project-context.md`。
- **引用 Story**：如果存在对应的 Story 文件（位于 `_bmad-output/implementation-artifacts/`），请将其作为主要参考依据。

### 2. 代码生成规范
- **后端 (Spring Boot)**：
  - 严格遵循分层架构：Controller → Service → Repository。
  - 使用 `BusinessException` 处理业务异常，禁止直接抛出 `RuntimeException`。
  - 异步任务必须使用 `AbstractStreamProducer/Consumer` 模板。
- **前端 (React)**：
  - **强制使用 TailwindCSS** 进行样式编写，严禁引入 Ant Design 等其他 UI 库。
  - 组件必须使用 TypeScript 接口定义 Props。
  - API 调用统一通过 `frontend/src/api/client.ts` 封装的实例。

### 3. Vibe Coding 实操手法 (IDEA + Lingma)
- **A. 意图描述 (Prompt Engineering)**：
  - 不要直接写代码，先在侧边栏用自然语言描述需求。
  - **示例**：“我想在 `ResumeController` 中添加一个批量导入接口，参考现有的 `uploadResume` 逻辑，使用 Redis Stream 异步处理。”
  - **技巧**：使用 `@workspace` 或 `@file` 引用相关代码文件，让灵码基于现有架构生成代码。
- **B. 代码生成与注入 (Code Generation)**：
  - **智能补全**：在编辑器中编写注释或方法签名时，灵码会自动给出整段代码建议，按 `Tab` 接受。
  - **对话生成**：在侧边栏生成的代码块上点击 “插入到编辑器” 或 “替换选中代码”。
  - **多文件编辑**：如果任务涉及前后端联动，要求灵码列出所有需要修改的文件，并逐个生成。
- **C. 胶水编程 (Glue Programming)**：
  - **复用现有模式**：明确告诉灵码：“请遵循项目中 `AbstractStreamConsumer` 的模式来实现新的消费者。”
  - **避免重复造轮子**：询问灵码：“项目中是否已有处理 PDF 解析的工具类？”如果有，让它直接调用。
- **D. 持续审查与纠偏 (Review & Iterate)**：
  - **代码解释**：选中灵码生成的复杂逻辑，右键选择“解释代码”，确保理解其实现原理。
  - **单元测试生成**：对生成的 Service 类，右键选择“生成单元测试”，灵码会根据 JUnit 5 和 Mockito 规范自动创建测试用例。
  - **Bug 修复**：如果运行报错，直接将控制台日志粘贴到侧边栏，灵码会分析堆栈并给出修复建议。

### 4. 审查与迭代
- **自我审查**：AI 生成的代码必须包含必要的 Javadoc 或注释。
- **测试先行**：对于复杂逻辑，优先生成单元测试用例。
- **错误修复**：遇到报错时，直接将日志粘贴给 AI，要求其根据 `project-context.md` 中的技术栈版本进行修复。
- **Code Review 会话隔离**（重要）：
  - **独立会话原则**：完成功能开发后，必须启动一个**全新的通义灵码会话**专门用于 Code Review
  - **触发方式**：在开发会话中输入 "启动 Code Review"，AI 会指导你在新会话中使用 `bmad-code-review` skill
  - **禁止自审**：严禁在同一个会话中既写代码又审查代码（认知偏差风险）
  - **Review 范围**：默认审查当前分支的所有未提交变更（`git diff HEAD`），也可指定特定文件

## 常用指令映射
- "创建新功能" -> **必须使用** `Skill: bmad-brainstorming` 进行需求澄清
- "编写计划" -> **必须使用** `Skill: writing-plans` 生成详细计划
- "实现功能" -> **必须使用** `Skill: subagent-driven-development` 或 `bmad-dev-story`
- "测试驱动" -> **必须使用** `Skill: test-driven-development` 执行 TDD
- "启动 Code Review" -> **必须在新会话中**使用 `Skill: bmad-code-review`
- "完成分支" -> **必须使用** `Skill: finishing-a-development-branch`

---

## 🤖 AI 自动化自查清单 (Self-Check Protocol)

**在执行任何代码生成任务后，AI 必须在内部完成以下检查，若发现违规需自动修正：**

### 0. 环境隔离检查 (Worktree Isolation) 🔒
- [ ] **分支确认**：当前是否在 `feature/*` 或独立的 Git Worktree 分支中？
- [ ] **主分支保护**：严禁直接在 `main`、`master` 或 `dev` 分支上进行功能开发。
- [ ] **安全丢弃**：如果用户表示需求取消，必须提示使用 `git worktree remove -f` 进行清理。

### 1. 架构与规范自查
- [ ] **分层检查**：Controller 是否只负责路由？业务逻辑是否已下沉至 Service？
- [ ] **DTO 转换**：返回给前端的是否为 DTO 而非 Entity？是否使用了 MapStruct？
- [ ] **异常处理**：是否捕获了所有受检异常并转换为 `BusinessException`？
- [ ] **前端样式**：React 组件中是否混入了内联样式或 Ant Design？（强制 TailwindCSS）

### 2. 胶水编程自查
- [ ] **复用性检查**：项目中是否已有类似的工具类或抽象模板（如 `AbstractStreamConsumer`）？
- [ ] **避免重复**：是否尝试重新实现已有的 PDF 解析、Redis 操作或 LLM 调用逻辑？

### 3. 质量护栏自查
- [ ] **类型安全**：TypeScript 接口是否与后端 DTO 严格匹配？Java 泛型是否正确？
- [ ] **日志规范**：是否使用了 SLF4J？异常堆栈是否作为最后一个参数传入？
- [ ] **注释完整性**：公共方法是否有 Javadoc？复杂逻辑是否有行内解释？
- [ ] **事务边界**：Service 层是否包含了耗时的外部 API 调用（如 LLM、S3）？若有，必须改为异步 Redis Stream 处理。

### 4. Code Review 准备检查（新增）🔍
- [ ] **会话隔离确认**：当前会话仅用于开发，未执行任何 Code Review 动作
- [ ] **Git 状态清晰**：所有修改已暂存（`git add .`），可通过 `git diff --cached` 查看
- [ ] **计划/Story 关联**：对应的计划文件或 Story 文件路径已知
- [ ] **Review 指令就绪**：准备在新会话中执行 `Skill: bmad-code-review`，并提供计划/Story 文件路径作为上下文
- [ ] **前置 Skill 完成**：确认已通过完整的7步流程（Brainstorming → Worktree → Writing Plans → Subagent Dev → TDD）完成功能开发

### 5. 执行动作
- 如果自查发现问题，**立即修正代码**并告知用户："已根据 Vibe Coding 准则自动优化了以下部分..."
- 如果代码符合所有准则，在回复末尾标注：`✅ Self-Check Passed`
- **强制提醒**：在所有开发任务完成后，必须提示用户："⚠️ 请在新会话中启动 Code Review，使用命令：`Skill: bmad-code-review`"