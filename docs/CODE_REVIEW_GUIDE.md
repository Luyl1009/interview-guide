# Code Review 独立会话操作指南

## 🎯 核心原则

**开发会话 ≠ Review 会话**

- ✅ **开发会话**：专注于功能实现、代码生成、Bug 修复
- ✅ **Review 会话**：专注于代码质量审查、问题发现、改进建议
- ❌ **禁止混合**：严禁在同一个会话中既写代码又审查代码（认知偏差）

---

## 📋 操作流程

### 阶段 1：开发完成后的准备（在开发会话中）

**前置条件**：已完成 Story 实现（通过 `Skill: bmad-dev-story`）

1. **确保所有修改已暂存**
   ```bash
   git add .
   ```

2. **确认 Story 文件路径**
   - 查看 `_bmad-output/implementation-artifacts/` 目录
   - 找到对应的 Story 文件（如 `story-1-2-daily-quote.md`）

3. **提交当前工作**
   ```bash
   git commit -m "feat: 完成XX功能"
   ```

4. **获取 Review 指令**
   - 在开发会话中输入："启动 Code Review"
   - AI 会输出类似以下提示：
     ```
     ⚠️ 请在新会话中启动 Code Review，使用命令：
     Skill: bmad-code-review
     
     并提供以下信息：
     - Story 文件路径：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md
     - 审查范围：当前分支的所有未提交变更（或指定文件）
     ```

---

### 阶段 2：启动独立的 Review 会话

#### 步骤 1：打开新的通义灵码会话

- 在 IDEA 中关闭当前侧边栏对话
- 点击 "+" 创建新对话
- **重要**：不要在新会话中继续之前的话题

#### 步骤 2：激活 Code Review Skill

在新会话中输入：

```
Skill: bmad-code-review
```

或者更明确地：

```
我想对当前分支的代码变更进行 Code Review。

Story 文件：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md
审查范围：git diff HEAD（所有未提交变更）
```

#### 步骤 3：BMad Code Review 工作流程

`bmad-code-review` skill 会自动执行以下 4 个步骤：

##### Step 1: Gather Context（收集上下文）

AI 会询问你：

```
What do you want to review?

1. Uncommitted changes (staged + unstaged)
2. Staged changes only
3. Branch diff vs a base branch
4. Specific commit range
5. Provided diff or file list
```

**推荐选择**：
- 如果是功能开发完成后：**选项 1**（Uncommitted changes）
- 如果只想审查特定文件：**选项 5**，然后提供文件路径列表

AI 还会询问：

```
Is there a spec or story file that provides context for these changes?
```

**回答**：提供 Story 文件路径，例如：
```
_bmad-output/implementation-artifacts/story-1-2-daily-quote.md
```

##### Step 2: Review（并行审查）

AI 会启动 **3 个并行的 adversarial review layers**：

1. **Blind Hunter**（盲审猎人）
   - **输入**：仅接收 diff，无项目上下文
   - **目标**：发现通用代码质量问题（命名、结构、重复等）
   - **特点**：完全隔离，避免先入为主

2. **Edge Case Hunter**（边界案例猎人）
   - **输入**：diff + 项目读取权限
   - **目标**：发现边界条件、异常处理、并发安全问题
   - **特点**：结合项目架构分析

3. **Acceptance Auditor**（验收审计员）
   - **输入**：diff + Story 文件 + 上下文文档
   - **目标**：检查是否符合 Story 的验收标准
   - **特点**：对照需求规格进行审计

**注意**：如果子代理不可用，AI 会生成 prompt 文件并要求你在不同会话中手动运行。

##### Step 3: Triage（问题分级）

AI 会将所有发现的问题分类为：

- **Critical**（严重）：必须立即修复（阻塞性问题）
  - 示例：空指针风险、事务内调用外部 API、安全漏洞
  
- **Important**（重要）：修复后再继续
  - 示例：缺少单元测试、DTO 直接返回 Entity、硬编码密钥
  
- **Minor**（轻微）：记录，可延后处理
  - 示例：注释不完整、日志格式不规范、变量命名不够清晰

每个问题包含：
- 标题（一行描述）
- 违反的规则/标准
- 证据（来自 diff 的具体代码行）
- 修复建议

##### Step 4: Present（呈现结果）

AI 会以结构化 Markdown 格式输出审查报告，包含：

```markdown
# Code Review Report

## Summary
- Files changed: X
- Lines added: +Y
- Lines removed: -Z
- Total findings: N (Critical: A, Important: B, Minor: C)

## Critical Issues
1. [问题标题]
   - Rule violated: ...
   - Evidence: ...
   - Suggestion: ...

## Important Issues
...

## Minor Issues
...

## Overall Assessment
[总体评价和建议]
```

---

### 阶段 3：根据 Review 结果修复（回到开发会话）

1. **优先修复 Critical 问题**
   - 在开发会话中逐个修复
   - 每修复一个问题，运行测试验证

2. **处理 Important 问题**
   - 评估是否需要在当前迭代中修复
   - 如果时间允许，立即修复

3. **记录 Minor 问题**
   - 可以延后到下一个迭代
   - 或在技术债务清单中记录

4. **重新提交**
   ```bash
   git add .
   git commit -m "fix: 根据 Code Review 修复问题"
   ```

5. **可选：二次 Review**
   - 如果 Critical 问题较多，建议再次启动 Review 会话
   - 确保所有严重问题已解决

---

## 🔧 高级用法

### 1. 审查特定文件

在 Step 1 中选择 "Provided diff or file list"，然后提供：

```
app/src/main/java/interview/guide/modules/interview/skill/DailyQuoteService.java
frontend/src/pages/AgileEnglishPage.tsx
```

### 2. 审查分支差异

如果你想审查整个分支相对于 `main` 的变化：

```
Branch diff vs main
```

AI 会执行：
```bash
git diff main..feature/daily-quote
```

### 3. 审查特定 Commit 范围

```
Commit range: abc123..def456
```

### 4. 分块审查大改动

如果 diff 超过 3000 行，AI 会提示：

```
Diff exceeds 3000 lines. Would you like to chunk the review by file group?
```

你可以选择：
- **按模块分块**：先审查后端，再审查前端
- **按文件类型分块**：先审查 Java 文件，再审查 TypeScript 文件

---

## ⚠️ 注意事项

### 禁止行为

- ❌ 在开发会话中直接说 "帮我审查一下这段代码"
- ❌ 在同一会话中先生成代码，然后立即要求审查
- ❌ 忽略 Critical 问题直接合并代码
- ❌ 不提供 Story 文件就进行 full review（会导致 Acceptance Auditor 跳过）

### 推荐行为

- ✅ 每次功能开发完成后，强制启动新的 Review 会话
- ✅ 提供 Story 文件作为上下文，确保验收标准被审计
- ✅ 优先修复 Critical 和 Important 问题
- ✅ 将 Minor 问题记录到技术债务清单
- ✅ 对于复杂改动，考虑分块审查

---

## 📊 示例场景

### 场景 1：完成"每日一句"功能后的 Review

**开发会话最后**：
```
用户：我已经完成了每日一句功能的开发和测试。

AI：✅ Self-Check Passed
⚠️ 请在新会话中启动 Code Review，使用命令：
Skill: bmad-code-review

并提供以下信息：
- Story 文件路径：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md
- 审查范围：当前分支的所有未提交变更
```

**新 Review 会话**：
```
用户：Skill: bmad-code-review

AI：[执行 Step 1] What do you want to review?

用户：Uncommitted changes

AI：[执行 Step 1] Is there a spec or story file?

用户：_bmad-output/implementation-artifacts/story-1-2-daily-quote.md

AI：[执行 Step 2-4，输出审查报告]
```

### 场景 2：修复 Bug 后的快速 Review

**开发会话最后**：
```
用户：Bug 已修复，测试通过。

AI：✅ Self-Check Passed
⚠️ 请在新会话中启动 Code Review，使用命令：
Skill: bmad-code-review

审查范围建议：只审查修改的文件
- app/src/main/java/.../ResumeParseService.java
```

**新 Review 会话**：
```
用户：Skill: bmad-code-review

AI：What do you want to review?

用户：Provided diff or file list

AI：Please provide the file paths.

用户：
app/src/main/java/interview/guide/modules/resume/service/ResumeParseService.java
```

---

## 🎓 为什么需要独立会话？

### 认知偏差风险

| 偏差类型 | 说明 | 影响 |
|---------|------|------|
| **确认偏误** | 开发者倾向于证明自己写的代码是正确的 | 忽略潜在问题 |
| **沉没成本谬误** | 投入了大量精力，不愿承认缺陷 | 拒绝必要的重构 |
| **熟悉度盲区** | 对自己的代码太熟悉，看不到明显问题 | 遗漏边界情况 |
| **目标污染** | 既要实现功能又要审查质量，注意力分散 | 两者都做不好 |

### 独立会话的优势

- ✅ **零上下文偏见**：Review 会话不知道代码是谁写的、为什么这样写
- ✅ **专注单一目标**：开发会话专注实现，Review 会话专注质量
- ✅ **Adversarial Review**：3 个并行审查层从不同角度攻击代码
- ✅ **客观分级**：基于规则而非个人判断进行问题分级

---

## 📝 总结

**Vibe Coding + BMad + Harness + Code Review 完整流程**：

1. **设计阶段**：`bmad-create-story` → 生成功能 Story
2. **开发阶段**：在独立 Worktree 分支中实现功能
3. **自测阶段**：运行单元测试、集成测试
4. **自查阶段**：AI 自动执行 Self-Check Protocol
5. **Review 阶段**：**在新会话中**执行 `bmad-code-review`
6. **修复阶段**：回到开发会话，根据 Review 结果修复问题
7. **合并阶段**：所有 Critical/Important 问题解决后，合并到主分支

**记住**：Code Review 不是形式，而是质量保证的关键环节。独立会话机制确保了审查的客观性和有效性。
