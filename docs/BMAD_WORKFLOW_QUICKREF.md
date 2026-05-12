# BMad 7步开发流程 - 快速参考

## 🚀 快速启动

```bash
./scripts/start-bmad-workflow.sh <feature-name>
```

示例：
```bash
./scripts/start-bmad-workflow.sh daily-quote
```

---

## 📋 7步流程概览

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

## 🔧 每步对应的 Skill

| 步骤 | 名称 | Skill | 触发指令 |
|------|------|-------|---------|
| **1** | 头脑风暴 | `bmad-brainstorming` | `Skill: bmad-brainstorming` |
| **2** | Git Worktree | 脚本 | `./scripts/start-feature.sh <name>` |
| **3** | 编写计划 | `writing-plans` | `Skill: writing-plans` |
| **4** | 子代理开发 | `subagent-driven-development` | `Skill: subagent-driven-development` |
| **5** | 测试驱动 | `test-driven-development` | `Skill: test-driven-development` |
| **6** | 代码审查 | `bmad-code-review` | `Skill: bmad-code-review`（新会话） |
| **7** | 完成分支 | `finishing-a-development-branch` | `Skill: finishing-a-development-branch` |

---

## 💡 关键要点

### ✅ 必须遵守

1. **每个步骤都必须使用对应的 Skill**
2. **步骤 6（Code Review）必须在新的会话中执行**
3. **遵循 TDD 原则**：先写失败的测试，再写实现代码
4. **不要跳过任何步骤**

### ❌ 禁止行为

1. ❌ 跳过头脑风暴直接编码
2. ❌ 不在 Worktree 中开发
3. ❌ 不编写计划直接开发
4. ❌ 在同一会话中既开发又审查
5. ❌ 不使用 TDD

---

## 📝 详细步骤

### 步骤 1：头脑风暴

```bash
Skill: bmad-brainstorming
```

**输出**：`docs/superpowers/specs/YYYY-MM-DD-<feature>-design.md`

---

### 步骤 2：Git Worktree

```bash
./scripts/start-feature.sh <feature-name>
```

**自动执行**：
- 创建 `.worktrees/<feature-name>` 目录
- 创建新分支 `feature/<feature-name>`
- 安装前后端依赖

---

### 步骤 3：编写计划

```bash
Skill: writing-plans
```

**输入**：步骤 1 生成的设计文档  
**输出**：`docs/superpowers/plans/YYYY-MM-DD-<feature>.md`

---

### 步骤 4：子代理开发

```bash
Skill: subagent-driven-development
```

**输入**：步骤 3 生成的计划文件  
**替代方案**：
```bash
Skill: bmad-create-story
Skill: bmad-dev-story
```

---

### 步骤 5：测试驱动

```bash
Skill: test-driven-development
```

**TDD 循环**：
1. 🔴 红：写一个失败的测试
2. 🟢 绿：写最小代码让测试通过
3. 🔵 重构：优化代码结构

---

### 步骤 6：代码审查

```bash
# 先提交代码
git add .
git commit -m "feat: 完成XX功能"

# 在新会话中
Skill: bmad-code-review
```

**重要**：必须在**全新的通义灵码会话**中执行

---

### 步骤 7：完成分支

```bash
# 修复 Critical/Important 问题
git add .
git commit -m "fix: 根据 Code Review 修复问题"

# 在新会话中
Skill: finishing-a-development-branch
```

**提供选项**：
1. Merge to main
2. Create PR
3. Keep branch
4. Discard

---

## 📚 相关文档

- [BMAD_SKILL_WORKFLOW.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/BMAD_SKILL_WORKFLOW.md) - 完整工作流指南
- [VIBE_CODING_HANDBOOK.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/VIBE_CODING_HANDBOOK.md) - 团队手册
- [CODE_REVIEW_GUIDE.md](file:///Users/lyl/IdeaProjects/interview-guide/docs/CODE_REVIEW_GUIDE.md) - Code Review 指南

---

## 🎯 核心价值

- ✅ **标准化**：所有开发步骤通过 Skill 触发
- ✅ **自动化**：AI 自动执行复杂分析和代码生成
- ✅ **客观性**：独立会话消除认知偏差
- ✅ **可追溯**：文档记录完整上下文
- ✅ **高质量**：三层并行审查确保代码质量
