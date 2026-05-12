#!/bin/bash
# start-bmad-workflow.sh - BMad 7步标准化开发流程启动器
# 
# 用途：引导用户完成完整的7步开发流程
# 使用：./scripts/start-bmad-workflow.sh <feature-name>

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

FEATURE_NAME=$1

if [ -z "$FEATURE_NAME" ]; then
    echo -e "${RED}❌ 错误: 请提供功能名称${NC}"
    echo -e "${YELLOW}用法: ./scripts/start-bmad-workflow.sh <feature-name>${NC}"
    echo ""
    echo -e "${BLUE}示例:${NC}"
    echo -e "   ./scripts/start-bmad-workflow.sh daily-quote"
    echo -e "   ./scripts/start-bmad-workflow.sh resume-parse-optimize"
    exit 1
fi

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     BMad 7步标准化开发流程 - 启动器                      ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# 显示流程图
echo -e "${CYAN}📋 完整开发流程:${NC}"
echo ""
echo -e "${PURPLE}┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐${NC}"
echo -e "${PURPLE}│ 1.头脑风暴   │ →   │ 2.Git Worktree│ →   │ 3.编写计划    │ →   │ 4.子代理开发  │${NC}"
echo -e "${PURPLE}│  澄清需求   │     │  创建隔离空间  │     │ 拆2-5分钟任务  │     │ 独立上下文    │${NC}"
echo -e "${PURPLE}└─────────────┘     └──────────────┘     └──────────────┘     └──────────────┘${NC}"
echo -e "${PURPLE}       ↓                                                                                  ↓${NC}"
echo -e "${PURPLE}┌──────────────┐     ┌──────────────┐     ┌──────────────┐${NC}"
echo -e "${PURPLE}│ 7.完成分支    │ ←   │ 6.代码审查    │ ←   │ 5.测试驱动    │${NC}"
echo -e "${PURPLE}│  验证收尾    │     │  自动审查    │     │ TDD循环      │${NC}"
echo -e "${PURPLE}└──────────────┘     └──────────────┘     └──────────────┘${NC}"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}功能名称: ${GREEN}${FEATURE_NAME}${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 步骤 1：头脑风暴
echo -e "${GREEN}🎯 步骤 1: 头脑风暴 (Brainstorming)${NC}"
echo -e "${YELLOW}   Skill: bmad-brainstorming${NC}"
echo ""
echo -e "${BLUE}   在通义灵码侧边栏输入:${NC}"
echo -e "   ${CYAN}Skill: bmad-brainstorming${NC}"
echo ""
echo -e "${BLUE}   AI 会询问:${NC}"
echo -e "   - 这个功能的目标用户是谁？"
echo -e "   - 有哪些技术约束？"
echo -e "   - 成功标准是什么？"
echo ""
echo -e "${BLUE}   输出文档:${NC}"
echo -e "   docs/superpowers/specs/YYYY-MM-DD-${FEATURE_NAME}-design.md"
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 2：Git Worktree 隔离
echo -e "${GREEN}🌳 步骤 2: Git Worktree 隔离${NC}"
echo -e "${YELLOW}   命令: ./scripts/start-feature.sh${NC}"
echo ""

WORKTREE_DIR="./.worktrees/${FEATURE_NAME}"
BRANCH_NAME="feature/${FEATURE_NAME}"

if [ -d "$WORKTREE_DIR" ]; then
    echo -e "${YELLOW}⚠️  警告: 工作树目录 ${WORKTREE_DIR} 已存在。${NC}"
    read -p "是否强制删除并重新创建? (y/N): " confirm
    if [[ $confirm == [yY] ]]; then
        git worktree remove -f "$WORKTREE_DIR" 2>/dev/null || true
        rm -rf "$WORKTREE_DIR"
    else
        echo -e "${YELLOW}🛑 操作取消。你可以直接进入该目录继续开发: cd $WORKTREE_DIR${NC}"
        exit 0
    fi
fi

echo -e "${BLUE}   正在创建 Git Worktree...${NC}"
git worktree add "$WORKTREE_DIR" "$BRANCH_NAME" 2>/dev/null || {
    git branch "$BRANCH_NAME"
    git worktree add "$WORKTREE_DIR" "$BRANCH_NAME"
}

cd "$WORKTREE_DIR"

echo -e "${BLUE}   安装后端依赖 (Gradle)...${NC}"
./gradlew build -x test --quiet || echo -e "${YELLOW}⚠️  Gradle 构建跳过或失败，请手动检查${NC}"

echo -e "${BLUE}   安装前端依赖 (pnpm)...${NC}"
if [ -d "frontend" ]; then
    cd frontend
    pnpm install --silent || npm install --silent
    cd ..
fi

echo -e "${GREEN}✅ Worktree 创建完成！${NC}"
echo -e "${BLUE}   工作目录: ${WORKTREE_DIR}${NC}"
echo -e "${BLUE}   分支名称: ${BRANCH_NAME}${NC}"
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 3：编写计划
echo -e "${GREEN}📝 步骤 3: 编写计划 (Writing Plans)${NC}"
echo -e "${YELLOW}   Skill: writing-plans${NC}"
echo ""
echo -e "${BLUE}   在通义灵码侧边栏输入:${NC}"
echo -e "   ${CYAN}Skill: writing-plans${NC}"
echo ""
echo -e "${BLUE}   提供信息:${NC}"
echo -e "   - 设计文档路径: docs/superpowers/specs/YYYY-MM-DD-${FEATURE_NAME}-design.md"
echo ""
echo -e "${BLUE}   输出文档:${NC}"
echo -e "   docs/superpowers/plans/YYYY-MM-DD-${FEATURE_NAME}.md"
echo ""
echo -e "${BLUE}   计划包含:${NC}"
echo -e "   - Task 1: 创建 XXX.java (2分钟)"
echo -e "   - Task 2: 实现 YYY 逻辑 (3分钟)"
echo -e "   - Task 3: 修改 ZZZ.tsx (2分钟)"
echo -e "   ..."
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 4：子代理开发
echo -e "${GREEN}🤖 步骤 4: 子代理开发 (Subagent-Driven Development)${NC}"
echo -e "${YELLOW}   Skill: subagent-driven-development${NC}"
echo ""
echo -e "${BLUE}   在通义灵码侧边栏输入:${NC}"
echo -e "   ${CYAN}Skill: subagent-driven-development${NC}"
echo ""
echo -e "${BLUE}   提供计划文件:${NC}"
echo -e "   docs/superpowers/plans/YYYY-MM-DD-${FEATURE_NAME}.md"
echo ""
echo -e "${BLUE}   AI 会自动执行:${NC}"
echo -e "   - 按顺序执行每个任务（每个 2-5 分钟）"
echo -e "   - 每个任务在独立上下文中"
echo -e "   - 实时验证测试结果"
echo -e "   - 标记任务完成状态"
echo ""
echo -e "${BLUE}   替代方案（如果 subagent-driven-development 不可用）:${NC}"
echo -e "   ${CYAN}Skill: bmad-create-story${NC}"
echo -e "   ${CYAN}Skill: bmad-dev-story${NC}"
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 5：测试驱动
echo -e "${GREEN}🧪 步骤 5: 测试驱动 (TDD)${NC}"
echo -e "${YELLOW}   Skill: test-driven-development${NC}"
echo ""
echo -e "${BLUE}   在通义灵码侧边栏输入:${NC}"
echo -e "   ${CYAN}Skill: test-driven-development${NC}"
echo ""
echo -e "${BLUE}   TDD 循环:${NC}"
echo -e "   1. 🔴 红: 写一个失败的测试"
echo -e "   2. 🟢 绿: 写最小代码让测试通过"
echo -e "   3. 🔵 重构: 优化代码结构，保持测试通过"
echo ""
echo -e "${BLUE}   关键规则:${NC}"
echo -e "   ❌ 没有先失败的测试，不写生产代码"
echo -e "   ✅ 测试必须先失败（验证测试本身是正确的）"
echo -e "   ✅ 覆盖边界情况和异常路径"
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 6：代码审查
echo -e "${GREEN}🔍 步骤 6: 代码审查 (Code Review)${NC}"
echo -e "${YELLOW}   Skill: bmad-code-review${NC}"
echo ""
echo -e "${RED}⚠️  重要: 必须在新的通义灵码会话中执行！${NC}"
echo ""
echo -e "${BLUE}   先提交代码:${NC}"
echo -e "   ${CYAN}git add .${NC}"
echo -e "   ${CYAN}git commit -m \"feat: 完成${FEATURE_NAME}功能\"${NC}"
echo ""
echo -e "${BLUE}   然后关闭当前会话，创建新会话，输入:${NC}"
echo -e "   ${CYAN}Skill: bmad-code-review${NC}"
echo ""
echo -e "${BLUE}   提供信息:${NC}"
echo -e "   - 审查范围: Uncommitted changes"
echo -e "   - Story/计划文件: docs/superpowers/plans/YYYY-MM-DD-${FEATURE_NAME}.md"
echo ""
echo -e "${BLUE}   AI 会执行三层并行审查:${NC}"
echo -e "   - Blind Hunter（盲审猎人）：仅看 diff，无偏见"
echo -e "   - Edge Case Hunter（边界案例猎人）：结合项目架构"
echo -e "   - Acceptance Auditor（验收审计员）：对照验收标准"
echo ""
echo -e "${BLUE}   输出报告:${NC}"
echo -e "   - Critical Issues（必须立即修复）"
echo -e "   - Important Issues（修复后再继续）"
echo -e "   - Minor Issues（记录，可延后处理）"
echo ""

read -p "$(echo -e ${YELLOW}按回车继续...${NC})"

# 步骤 7：完成分支
echo -e "${GREEN}✨ 步骤 7: 完成分支 (Finishing a Development Branch)${NC}"
echo -e "${YELLOW}   Skill: finishing-a-development-branch${NC}"
echo ""
echo -e "${BLUE}   回到开发会话，修复 Critical/Important 问题:${NC}"
echo -e "   ${CYAN}git add .${NC}"
echo -e "   ${CYAN}git commit -m \"fix: 根据 Code Review 修复问题\"${NC}"
echo ""
echo -e "${BLUE}   在新会话中输入:${NC}"
echo -e "   ${CYAN}Skill: finishing-a-development-branch${NC}"
echo ""
echo -e "${BLUE}   AI 会执行:${NC}"
echo -e "   - ✅ 运行所有测试"
echo -e "   - ✅ 检查 Code Review 结果"
echo -e "   - ✅ 提供选项:"
echo -e "      1. Merge to main（本地合并）"
echo -e "      2. Create PR（创建 Pull Request）"
echo -e "      3. Keep branch（保持分支不变）"
echo -e "      4. Discard（丢弃此工作）"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🎉 7步流程指南已完成！${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

echo -e "${BLUE}📚 相关文档:${NC}"
echo -e "   - docs/BMAD_SKILL_WORKFLOW.md（完整工作流指南）"
echo -e "   - docs/VIBE_CODING_HANDBOOK.md（团队手册）"
echo -e "   - docs/CODE_REVIEW_GUIDE.md（Code Review 指南）"
echo ""

echo -e "${BLUE}💡 提示:${NC}"
echo -e "   - 每个步骤都必须使用对应的 Skill"
echo -e "   - 步骤 6（Code Review）必须在新的会话中执行"
echo -e "   - 遵循 TDD 原则：先写失败的测试，再写实现代码"
echo -e "   - 不要跳过任何步骤，每一步都是质量保证的关键环节"
echo ""

echo -e "${GREEN}✨ 开始你的开发之旅吧！${NC}"
echo ""
