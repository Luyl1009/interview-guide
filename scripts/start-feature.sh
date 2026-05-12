#!/bin/bash

# Vibe Coding + BMad Feature Starter Script
# Usage: ./scripts/start-feature.sh <feature-name>

set -e

FEATURE_NAME=$1
WORKTREE_DIR="./.worktrees/${FEATURE_NAME}"
BRANCH_NAME="feature/${FEATURE_NAME}"

if [ -z "$FEATURE_NAME" ]; then
    echo "❌ 错误: 请提供功能名称 (例如: ./scripts/start-feature.sh daily-quote)"
    exit 1
fi

echo "🚀 正在为 '${FEATURE_NAME}' 准备隔离开发环境..."

# 1. 检查目录是否已存在
if [ -d "$WORKTREE_DIR" ]; then
    echo "⚠️  警告: 工作树目录 ${WORKTREE_DIR} 已存在。"
    read -p "是否强制删除并重新创建? (y/N): " confirm
    if [[ $confirm == [yY] ]]; then
        git worktree remove -f "$WORKTREE_DIR" 2>/dev/null || true
        rm -rf "$WORKTREE_DIR"
    else
        echo "🛑 操作取消。你可以直接进入该目录继续开发: cd $WORKTREE_DIR"
        exit 0
    fi
fi

# 2. 创建 Git Worktree
echo "🌳 创建 Git Worktree: ${BRANCH_NAME} -> ${WORKTREE_DIR}"
git worktree add "$WORKTREE_DIR" "$BRANCH_NAME" 2>/dev/null || {
    # 如果分支不存在，先基于当前 HEAD 创建分支
    git branch "$BRANCH_NAME"
    git worktree add "$WORKTREE_DIR" "$BRANCH_NAME"
}

# 3. 进入工作树并初始化环境
cd "$WORKTREE_DIR"

echo "📦 安装后端依赖 (Gradle)..."
./gradlew build -x test --quiet || echo "⚠️  Gradle 构建跳过或失败，请手动检查"

echo "📦 安装前端依赖 (pnpm)..."
if [ -d "frontend" ]; then
    cd frontend
    pnpm install --silent || npm install --silent
    cd ..
fi

# 4. 给出后续指令
echo ""
echo "✅ 环境准备就绪！"
echo "---------------------------------------------------"
echo "📂 工作目录: ${WORKTREE_DIR}"
echo "🌿 分支名称: ${BRANCH_NAME}"
echo ""
echo "💡 下一步建议："
echo "   1. 在 IDEA 中打开此目录进行 Vibe Coding"
echo "   2. 运行后端: ./gradlew bootRun"
echo "   3. 运行前端: cd frontend && pnpm dev"
echo ""
echo "🗑️  如果想丢弃此功能，直接运行:"
echo "   git worktree remove -f .worktrees/${FEATURE_NAME}"
echo "---------------------------------------------------"
