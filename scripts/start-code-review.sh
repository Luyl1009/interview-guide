#!/bin/bash
# start-code-review.sh - 启动独立的 Code Review 会话
# 
# 用途：在功能开发完成后，准备并提示用户在新会话中执行 Code Review
# 使用：./scripts/start-code-review.sh [story-file-path]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Code Review 会话准备工具${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查是否在 Git 仓库中
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo -e "${RED}❌ 错误：当前目录不是 Git 仓库${NC}"
    exit 1
fi

# 检查是否有未提交的变更
if [[ -z "$(git status --porcelain)" ]]; then
    echo -e "${YELLOW}⚠️  警告：没有检测到未提交的变更${NC}"
    echo -e "${YELLOW}   如果要审查已提交的代码，请使用分支 diff 模式${NC}"
    echo ""
fi

# 获取 Story 文件路径
STORY_FILE="$1"

if [ -z "$STORY_FILE" ]; then
    # 自动查找最近的 Story 文件
    echo -e "${BLUE}🔍 正在查找 Story 文件...${NC}"
    
    STORY_DIR="_bmad-output/implementation-artifacts"
    if [ -d "$STORY_DIR" ]; then
        LATEST_STORY=$(find "$STORY_DIR" -name "story-*.md" -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -d' ' -f2-)
        
        if [ -n "$LATEST_STORY" ]; then
            echo -e "${GREEN}✅ 找到最新的 Story 文件：${NC}${LATEST_STORY}"
            STORY_FILE="$LATEST_STORY"
        else
            echo -e "${YELLOW}⚠️  未找到 Story 文件${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Story 目录不存在：$STORY_DIR${NC}"
    fi
else
    # 验证提供的 Story 文件是否存在
    if [ ! -f "$STORY_FILE" ]; then
        echo -e "${RED}❌ 错误：Story 文件不存在：$STORY_FILE${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ 使用指定的 Story 文件：${NC}$STORY_FILE"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  下一步操作指南${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

echo -e "${GREEN}1️⃣  暂存所有修改（如果尚未暂存）：${NC}"
echo -e "   ${YELLOW}git add .${NC}"
echo ""

echo -e "${GREEN}2️⃣  提交当前工作：${NC}"
echo -e "   ${YELLOW}git commit -m \"feat: 完成XX功能\"${NC}"
echo ""

echo -e "${GREEN}3️⃣  关闭当前通义灵码会话，创建新会话：${NC}"
echo -e "   - 在 IDEA 侧边栏点击 \"X\" 关闭当前对话"
echo -e "   - 点击 \"+\" 创建新对话"
echo ""

echo -e "${GREEN}4️⃣  在新会话中输入以下命令：${NC}"
echo ""
echo -e "   ${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "   ${GREEN}Skill: bmad-code-review${NC}"
echo -e "   ${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [ -n "$STORY_FILE" ]; then
    echo -e "${GREEN}5️⃣  当 AI 询问时，提供以下信息：${NC}"
    echo ""
    echo -e "   ${YELLOW}审查范围：Uncommitted changes${NC}"
    echo -e "   ${YELLOW}Story 文件：${STORY_FILE}${NC}"
    echo ""
else
    echo -e "${GREEN}5️⃣  当 AI 询问时，提供以下信息：${NC}"
    echo ""
    echo -e "   ${YELLOW}审查范围：Uncommitted changes${NC}"
    echo -e "   ${YELLOW}Story 文件：无（或手动提供路径）${NC}"
    echo ""
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Code Review 工作流程${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}AI 将自动执行以下步骤：${NC}"
echo ""
echo -e "   ${BLUE}Step 1:${NC} Gather Context（收集上下文）"
echo -e "   ${BLUE}Step 2:${NC} Review（三层并行审查）"
echo -e "          ├─ Blind Hunter（盲审猎人）"
echo -e "          ├─ Edge Case Hunter（边界案例猎人）"
echo -e "          └─ Acceptance Auditor（验收审计员）"
echo -e "   ${BLUE}Step 3:${NC} Triage（问题分级：Critical/Important/Minor）"
echo -e "   ${BLUE}Step 4:${NC} Present（输出结构化审查报告）"
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  修复与重新提交${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}6️⃣  回到开发会话，根据 Review 结果修复问题：${NC}"
echo -e "   - 优先修复 Critical 级别问题"
echo -e "   - 处理 Important 级别问题"
echo -e "   - 记录 Minor 级别问题到技术债务清单"
echo ""
echo -e "${GREEN}7️⃣  重新提交：${NC}"
echo -e "   ${YELLOW}git add .${NC}"
echo -e "   ${YELLOW}git commit -m \"fix: 根据 Code Review 修复问题\"${NC}"
echo ""

echo -e "${GREEN}✨ 准备完成！请按照上述步骤在新会话中启动 Code Review。${NC}"
echo ""
echo -e "${BLUE}📖 详细文档：docs/CODE_REVIEW_GUIDE.md${NC}"
echo ""
