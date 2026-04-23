#!/usr/bin/env bash
# Lingma Worktree 初始化脚本
# 在创建新 worktree 后自动运行，完成环境准备

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

cd "$REPO_ROOT"

log_info "开始初始化 worktree: $REPO_ROOT"

# ================================
# 1. 环境检查
# ================================

log_info "检查必要环境..."

# Java 21+
if ! command -v java &>/dev/null; then
  log_error "未找到 Java，请安装 Java 21+"
  exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [[ "$JAVA_VERSION" -lt 21 ]]; then
  log_error "需要 Java 21+，当前版本: $JAVA_VERSION"
  exit 1
fi
log_info "Java 版本: $(java -version 2>&1 | head -n 1)"

# Node.js 18+
if ! command -v node &>/dev/null; then
  log_error "未找到 Node.js，请安装 Node.js 18+"
  exit 1
fi
log_info "Node.js 版本: $(node --version)"

# pnpm
if ! command -v pnpm &>/dev/null; then
  log_warn "未找到 pnpm，尝试安装..."
  if command -v npm &>/dev/null; then
    npm install -g pnpm
  else
    log_error "未找到 npm，无法安装 pnpm"
    exit 1
  fi
fi
log_info "pnpm 版本: $(pnpm --version)"

# Docker (可选)
if command -v docker &>/dev/null && docker info &>/dev/null; then
  log_info "Docker 已就绪"
  HAS_DOCKER=true
else
  log_warn "Docker 未运行，跳过基础设施启动"
  HAS_DOCKER=false
fi

# ================================
# 2. 配置文件准备
# ================================

log_info "检查配置文件..."

if [[ ! -f ".env" ]]; then
  if [[ -f ".env.example" ]]; then
    cp .env.example .env
    log_warn "已复制 .env.example → .env，请编辑 .env 填入实际密钥"
  else
    log_warn "未找到 .env.example，请手动创建 .env"
  fi
else
  log_info ".env 已存在"
fi

# ================================
# 3. 前端依赖安装
# ================================

log_info "安装前端依赖..."
cd "$REPO_ROOT/frontend"

if [[ ! -d "node_modules" ]]; then
  pnpm install
  log_info "前端依赖安装完成"
else
  log_info "前端 node_modules 已存在，跳过安装"
fi

# ================================
# 4. Gradle 依赖预热
# ================================

log_info "预热 Gradle 依赖..."
cd "$REPO_ROOT"

if [[ ! -d ".gradle" ]]; then
  ./gradlew dependencies --configuration compileClasspath &>/dev/null || true
  log_info "Gradle 依赖下载完成"
else
  log_info "Gradle 缓存已存在"
fi

# ================================
# 5. 启动基础设施（可选）
# ================================

if [[ "$HAS_DOCKER" == true ]]; then
  log_info "检查 Docker 基础设施..."

  if [[ -f "docker-compose.dev.yml" ]]; then
    COMPOSE_FILE="docker-compose.dev.yml"
  elif [[ -f "docker-compose.yml" ]]; then
    COMPOSE_FILE="docker-compose.yml"
  else
    COMPOSE_FILE=""
  fi

  if [[ -n "$COMPOSE_FILE" ]]; then
    # 检查 postgres 是否已在运行
    if ! docker ps --format '{{.Names}}' | grep -q 'postgres\|interview-guide'; then
      log_info "启动基础设施: $COMPOSE_FILE"
      docker compose -f "$COMPOSE_FILE" up -d postgres redis minio
      log_info "等待数据库就绪..."
      sleep 3
    else
      log_info "基础设施已在运行"
    fi
  fi
else
  log_warn "Docker 不可用，请手动启动 PostgreSQL / Redis / MinIO"
fi

# ================================
# 6. 完成
# ================================

log_info "Worktree 初始化完成!"
echo ""
echo "================================"
echo "可用命令:"
echo "  启动后端: ./gradlew bootRun"
echo "  启动前端: cd frontend && pnpm dev"
echo "================================"
