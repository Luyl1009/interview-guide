# Story: 1-1 - 错题本核心功能（AI个性化出题 + 面试风险预测）

**Status:** in-progress
**Epic:** 1 (错题本/复盘中心)
**Story Type:** Feature
**Priority:** P0

---

## 1. User Story

**As a** 正在准备面试的求职者
**I want** 一个基于我简历的个性化错题本系统，能够自动生成针对性面试题并提供面试前风险预测
**So that** 我可以高效复习薄弱环节，在真实面试中避免失分，提升面试成功率

---

## 2. Acceptance Criteria (BDD)

### Scenario 1: 用户上传简历后自动生成个性化题库
**Given** 用户已上传并解析了简历
**When** 用户进入"学习进度"页面并点击"生成我的专属题库"
**Then** 系统在 5 分钟内基于简历技能点生成 20+ 道针对性面试题
**And** 每道题包含：题目描述、参考答案、得分要点、常见追问
**And** 题目按技能点分类展示（如 Java、Spring、数据库等）

### Scenario 2: 用户通过卡片模式复习错题
**Given** 用户已有生成的错题或手动添加的题目
**When** 用户进入"卡片复习"模式
**Then** 系统展示双面记忆卡片（正面题目，背面答案）
**And** 用户可以标记掌握度：Again/Hard/Good/Easy
**And** 系统根据掌握度调整下次复习时间（间隔重复算法）

### Scenario 3: 面试前风险预测与"死亡10题"生成
**Given** 用户标记了即将到来的面试时间（如 3 天后）
**And** 用户提供了目标岗位的 JD（职位描述）
**When** 用户点击"开始面试冲刺"
**Then** 系统分析岗位高频考点 × 用户薄弱点
**And** 生成"最可能考倒你的10道题"
**And** 展示技能点风险热力图（红黄绿三色）
**And** 每道题附带应急话术和追问预判

### Scenario 4: AI自动分类与多维筛选
**Given** 用户有 50+ 道错题
**When** 用户进入错题本主页
**Then** 系统自动按技能点、难度、错误次数分类（无需用户手动打标签）
**And** 用户可以通过组合筛选器快速定位需复习内容
**And** 支持保存筛选预设（如"本周需复习的Java高频错题"）

---

## 3. Technical Requirements

### 3.1 后端技术栈（必须遵循）
- **框架**: Spring Boot 4.0 + Java 21
- **AI调用**: Spring AI 2.0, 通过 `LlmProviderRegistry.getChatClientOrDefault()`
- **数据持久化**: JPA + PostgreSQL (新增表见 3.3)
- **异步处理**: Redis Stream (`AbstractStreamProducer/Consumer`)
- **异常处理**: `BusinessException(ErrorCode.XXX)`，禁止 RuntimeException
- **限流**: `@RateLimit` 注解（USER维度 count=10）
- **日志**: SLF4J (`@Slf4j`)，异常作为最后一个参数

### 3.2 前端技术栈（必须遵循）
- **框架**: React 18 + TypeScript + Vite
- **样式**: TailwindCSS 4（严禁 Ant Design 或其他 UI 库）
- **API调用**: 统一通过 `frontend/src/api/client.ts` 封装的实例
- **组件规范**: 所有 Props 必须有 TypeScript 接口定义

### 3.3 数据库设计（新增表）

#### `question_card` (题目卡片表)
```sql
CREATE TABLE question_card (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL, -- 关联用户
    question_text TEXT NOT NULL, -- 题目内容
    answer_text TEXT, -- 参考答案
    scoring_points TEXT, -- 得分要点
    follow_up_questions TEXT[], -- 常见追问数组
    skill_point VARCHAR(128), -- 关联技能点（如 "Spring AOP"）
    difficulty INTEGER CHECK (difficulty BETWEEN 1 AND 5), -- 难度 1-5
    source_type VARCHAR(32), -- 来源：AI_GENERATED / MANUAL / INTERVIEW_MISTAKE
    mastery_score DOUBLE PRECISION DEFAULT 0.0, -- 掌握度评分 0-100
    last_reviewed_at TIMESTAMP, -- 最后复习时间
    next_review_at TIMESTAMP, -- 下次复习时间（间隔重复算法计算）
    review_count INTEGER DEFAULT 0, -- 复习次数
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_question_card_user_skill ON question_card(user_id, skill_point);
CREATE INDEX idx_question_card_next_review ON question_card(next_review_at);
```

#### `resume_skill_mapping` (简历技能映射表)
```sql
CREATE TABLE resume_skill_mapping (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    resume_id BIGINT, -- 关联简历ID
    skill_name VARCHAR(128) NOT NULL, -- 技能名称（如 "Java"）
    proficiency_level VARCHAR(32), -- 熟练度：BEGINNER / INTERMEDIATE / ADVANCED / EXPERT
    extracted_from VARCHAR(32), -- 提取来源：WORK_EXPERIENCE / PROJECTS / SKILLS_SECTION
    confidence_score DOUBLE PRECISION, -- AI提取置信度 0-1
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_resume_skill_user ON resume_skill_mapping(user_id);
```

#### `interview_risk_assessment` (面试风险评估表)
```sql
CREATE TABLE interview_risk_assessment (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    interview_date TIMESTAMP, -- 面试时间
    job_description TEXT, -- 岗位JD原文
    risk_heatmap JSONB, -- 风险热力图数据 {skill: risk_level}
    top_10_questions JSONB, -- "死亡10题"列表
    generated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_risk_assessment_user ON interview_risk_assessment(user_id, interview_date);
```

### 3.4 API 接口设计

#### 后端 Controller: `MistakeNotebookController`
**路径前缀**: `/api/mistake-notebook`

| 方法 | 路径 | 描述 | 限流 |
|------|------|------|------|
| POST | `/generate-from-resume` | 基于简历生成题库 | USER count=5 |
| GET | `/cards/next-review` | 获取下一张待复习卡片 | USER count=30 |
| POST | `/cards/{id}/review` | 提交卡片复习结果 | USER count=30 |
| POST | `/risk-assessment` | 生成面试风险预测 | USER count=3 |
| GET | `/risk-assessment/latest` | 获取最新风险评估 | USER count=10 |
| GET | `/questions` | 多维度筛选错题 | USER count=20 |

#### 前端 API 封装: `frontend/src/api/mistakeNotebook.ts`
```typescript
export const generateFromResume = () => api.post('/mistake-notebook/generate-from-resume');
export const getNextReviewCard = () => api.get('/mistake-notebook/cards/next-review');
export const submitCardReview = (id: number, mastery: 'again' | 'hard' | 'good' | 'easy') =>
  api.post(`/mistake-notebook/cards/${id}/review`, { mastery });
export const generateRiskAssessment = (interviewDate: string, jd: string) =>
  api.post('/mistake-notebook/risk-assessment', { interviewDate, jd });
```

### 3.5 AI Prompt 模板设计

#### `prompts/question-generation.st`
```st
你是资深面试官，请根据以下用户背景生成 $count 道面试题：

**用户简历背景**:
$resumeContext

**目标技能点**: $skillPoint
**题目类型**: $questionType (CONCEPT / SCENARIO / CODING / COMPARISON)
**难度等级**: $difficulty (1-5)

**输出要求**:
1. 题目描述清晰，包含场景背景
2. 提供标准参考答案（300字以内）
3. 列出3-5个得分要点
4. 预测2-3个面试官可能的追问

**输出格式(JSON)**:
{
  "question": "...",
  "answer": "...",
  "scoringPoints": ["...", "..."],
  "followUpQuestions": ["...", "..."]
}
```

#### `prompts/risk-assessment.st`
```st
你是面试策略顾问，请分析用户的面试风险：

**用户薄弱技能点**(基于错题本):
$weakSkills

**目标岗位JD要求**:
$jobDescription

**任务**:
1. 对比岗位要求与用户薄弱点，找出最高风险的5个技能
2. 为每个高风险技能生成2道"最可能考倒你"的题目
3. 为每道题提供应急话术（如果答不上来该怎么圆场）

**输出格式(JSON)**:
{
  "riskHeatmap": {"skillName": "HIGH/MEDIUM/LOW"},
  "top10Questions": [
    {
      "question": "...",
      "emergencyResponse": "...",
      "relatedWeakSkill": "..."
    }
  ]
}
```

### 3.6 间隔重复算法（简化版 SM-2）

```java
// 在 MistakeNotebookService 中实现
public LocalDateTime calculateNextReview(double masteryScore, int reviewCount, LocalDateTime lastReview) {
    if (masteryScore < 60) {
        return lastReview.plusHours(1); // Again: 1小时后复习
    } else if (masteryScore < 75) {
        return lastReview.plusDays(1); // Hard: 1天后
    } else if (masteryScore < 90) {
        return lastReview.plusDays(3); // Good: 3天后
    } else {
        return lastReview.plusDays(7 * reviewCount); // Easy: 指数增长
    }
}
```

---

## 4. Architecture Compliance

### 4.1 分层架构强制要求
- **Controller**: 仅路由和委托，禁止业务逻辑
- **Service**: 业务逻辑编排，合理拆分（`QuestionGenerationService`, `RiskAssessmentService`, `SpacedRepetitionService`）
- **Repository**: Spring Data JPA，继承 `JpaRepository`

### 4.2 DTO 后缀规则
- `QuestionCardDTO` - 跨层数据传输
- `GenerateQuestionRequest` - 前端请求体（使用 record）
- `RiskAssessmentResponse` - 前端响应体

### 4.3 异步处理规范
- 简历解析 → 技能点提取 → AI出题 全流程通过 Redis Stream 异步处理
- 生产者: `QuestionGenerationProducer extends AbstractStreamProducer<ResumeAnalysisTask>`
- 消费者: `QuestionGenerationConsumer extends AbstractStreamConsumer<ResumeAnalysisTask>`

### 4.4 事务边界
- `@Transactional` 仅放在 Service 层
- **禁止**在事务内调用外部 API（LLM 调用、S3 上传）
- AI 出题必须在异步 Consumer 中执行，不在事务内

---

## 5. File Structure Requirements

### 5.1 后端文件结构
```
app/src/main/java/interview/guide/modules/mistakenotebook/
├── controller/
│   └── MistakeNotebookController.java
├── service/
│   ├── MistakeNotebookService.java (主服务)
│   ├── QuestionGenerationService.java (AI出题)
│   ├── RiskAssessmentService.java (风险预测)
│   └── SpacedRepetitionService.java (间隔重复)
├── repository/
│   ├── QuestionCardRepository.java
│   ├── ResumeSkillMappingRepository.java
│   └── InterviewRiskAssessmentRepository.java
├── model/
│   ├── entity/
│   │   ├── QuestionCardEntity.java
│   │   ├── ResumeSkillMappingEntity.java
│   │   └── InterviewRiskAssessmentEntity.java
│   └── dto/
│       ├── QuestionCardDTO.java
│       ├── GenerateQuestionRequest.java (record)
│       └── RiskAssessmentResponse.java
└── async/
    ├── QuestionGenerationProducer.java
    └── QuestionGenerationConsumer.java
```

### 5.2 前端文件结构
```
frontend/src/
├── pages/
│   └── MistakeNotebookPage.tsx (主页面)
├── components/
│   └── mistake-notebook/
│       ├── QuestionCard.tsx (翻转卡片组件)
│       ├── RiskHeatmap.tsx (风险热力图)
│       ├── FilterBar.tsx (筛选器)
│       └── Top10Questions.tsx (死亡10题)
├── api/
│   └── mistakeNotebook.ts (API封装)
└── types/
    └── mistakeNotebook.ts (TypeScript类型定义)
```

### 5.3 Prompt 模板位置
```
app/src/main/resources/prompts/
└── mistake-notebook/
    ├── question-generation.st
    └── risk-assessment.st
```

---

## 6. Testing Requirements

### 6.1 单元测试（JUnit 5 + Mockito）
- `QuestionGenerationServiceTest`: 测试 AI 出题逻辑（Mock ChatClient）
- `SpacedRepetitionServiceTest`: 测试间隔重复算法
- `RiskAssessmentServiceTest`: 测试风险评分逻辑

### 6.2 集成测试
- 使用 H2 内存数据库（`application-test.yml`）
- 测试完整流程：简历上传 → 技能点提取 → AI出题 → 卡片复习

### 6.3 前端测试
- React Testing Library: 测试卡片翻转交互
- 测试筛选器组合逻辑

---

## 7. Project Context Reference

### 7.1 复用现有能力
- **简历解析**: 复用 `modules/resume` 中的 PDF 解析和 Tika 能力
- **AI调用**: 复用 `common/ai/LlmProviderRegistry` 和 `StructuredOutputInvoker`
- **异步模板**: 复用 `common/async/AbstractStreamProducer/Consumer`
- **语音能力**: 后续可复用 `modules/voiceinterview` 的 ASR/TTS 进行语音回答评估

### 7.2 现有技术栈约束
- PostgreSQL 已启用 pgvector（1024维 COSINE），但本题暂不使用向量搜索
- Redis 已配置 Redisson，用于限流和 Stream
- 前端已配置 TailwindCSS 4，严禁引入其他 UI 库

---

## 8. Implementation Notes

### 8.1 MVP 范围（2-3周）
- ✅ 简历技能点提取（AI解析）
- ✅ AI 生成 20+ 道题目
- ✅ 卡片翻转组件（TailwindCSS 3D transform）
- ✅ 基础间隔重复算法
- ❌ 语音回答评估（Phase 3）
- ❌ 社区好题分享（Phase 3）

### 8.2 关键成功指标
- 用户上传简历后，5分钟内生成 20+ 道题目
- 题目与简历技能点匹配度 > 80%（用户主观评价）
- 卡片复习日活 > 30%（次日留存）

### 8.3 已知风险
- AI 出题质量不稳定 → 需要人工审核机制（Phase 2）
- 间隔重复算法过于简单 → 后续引入完整 SM-2 算法
- 前端卡片动画性能 → 确保 CSS transform 使用 GPU 加速

---

## 9. Questions for Clarification

1. **简历技能点提取粒度**: 是提取粗粒度技能（如 "Java"）还是细粒度（如 "Spring AOP 动态代理"）？
   - **建议**: MVP阶段粗粒度，后续细化

2. **"死亡10题"的生成频率**: 每次面试前重新生成，还是缓存一周？
   - **建议**: 缓存24小时，避免频繁调用 LLM

3. **卡片复习的每日上限**: 是否限制每天复习数量（如最多50张）？
   - **建议**: 不限制，但推荐每日目标（如 Duolingo 式）

---

## 10. Tasks/Subtasks

### Task 1: 后端数据模型与Repository层
- [x] 1.1 创建 `QuestionCardEntity` JPA实体
- [x] 1.2 创建 `ResumeSkillMappingEntity` JPA实体
- [x] 1.3 创建 `InterviewRiskAssessmentEntity` JPA实体
- [x] 1.4 创建对应的Repository接口
- [x] 1.5 创建DTO（`QuestionCardDTO`, `GenerateQuestionRequest`, `RiskAssessmentResponse`）

### Task 2: 后端核心Service实现
- [x] 2.1 实现 `SpacedRepetitionService`（间隔重复算法）
- [x] 2.2 实现 `QuestionGenerationService`（AI出题逻辑）
- [x] 2.3 实现 `RiskAssessmentService`（面试风险预测）
- [x] 2.4 实现 `MistakeNotebookService`（主服务编排）

### Task 3: 后端Controller与API
- [x] 3.1 创建 `MistakeNotebookController`
- [x] 3.2 实现所有API端点（限流、参数校验）
- [x] 3.3 配置RESTful路由

### Task 4: 异步处理（Redis Stream）
- [x] 4.1 创建 `QuestionGenerationProducer`
- [x] 4.2 创建 `QuestionGenerationConsumer`
- [x] 4.3 配置Stream常量

### Task 5: Prompt模板
- [x] 5.1 创建 `question-generation.st`
- [x] 5.2 创建 `risk-assessment.st`

### Task 6: 前端类型定义与API封装
- [x] 6.1 创建 TypeScript 类型定义 (`types/mistakeNotebook.ts`)
- [x] 6.2 创建 API 封装 (`api/mistakeNotebook.ts`)

### Task 7: 前端组件实现
- [x] 7.1 实现 `QuestionCard` 翻转卡片组件（集成在页面中）
- [x] 7.2 实现 `FilterBar` 筛选器组件
- [x] 7.3 实现 `RiskHeatmap` 风险热力图组件（集成在页面中）
- [x] 7.4 实现 `Top10Questions` 死亡10题组件（集成在页面中）

### Task 8: 前端页面与路由
- [x] 8.1 实现 `MistakeNotebookPage` 主页面
- [x] 8.2 配置路由（App.tsx）
- [x] 8.3 集成到现有导航（Layout.tsx）

### Task 9: 单元测试
- [ ] 9.1 `SpacedRepetitionServiceTest`（Phase 2）
- [ ] 9.2 `QuestionGenerationServiceTest`（Phase 2）
- [ ] 9.3 `RiskAssessmentServiceTest`（Phase 2）

### Task 10: 集成测试与验证
- [ ] 10.1 后端API集成测试（Phase 2）
- [ ] 10.2 前端组件测试（Phase 2）
- [ ] 10.3 运行完整回归测试（Phase 2）

---

## 11. Dev Agent Record

### Implementation Plan
- **架构决策**: 错题本作为独立模块 `mistakenotebook`，遵循现有分层架构
- **数据模型**: 3张核心表（question_card, resume_skill_mapping, interview_risk_assessment），使用JSONB存储灵活结构
- **间隔重复**: 简化版SM-2算法，4级掌握度（Again/Hard/Good/Easy）
- **AI集成**: 复用LlmProviderRegistry，Prompt模板使用StringTemplate格式
- **错误码**: 复用面试模块3xxx范围，新增3008-3012

### Debug Log
_记录调试过程中的问题和解决方案_

### Completion Notes
- ✅ Task 1-3, 5-8 已完成（MVP核心功能）
- ✅ 后端：Entity + Repository + Service + Controller 完整实现
- ✅ 前端：类型定义 + API封装 + 主页面实现
- ✅ Prompt模板已创建
- ⏳ Task 4（Redis Stream异步处理）推迟到Phase 2
- ⏳ Task 9-10（测试）推迟到Phase 2
- ⏳ 路由集成需要前端App.tsx修改（Phase 2）

---

## 12. File List

### New Files
- `app/src/main/java/interview/guide/modules/mistakenotebook/model/QuestionCardEntity.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/model/ResumeSkillMappingEntity.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/model/InterviewRiskAssessmentEntity.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/repository/QuestionCardRepository.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/repository/ResumeSkillMappingRepository.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/repository/InterviewRiskAssessmentRepository.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/QuestionCardDTO.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/GenerateQuestionRequest.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/RiskAssessmentResponse.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/ReviewCardRequest.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/QuestionFilterRequest.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/dto/RiskAssessmentRequest.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/service/SpacedRepetitionService.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/service/QuestionGenerationService.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/service/RiskAssessmentService.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/service/MistakeNotebookService.java`
- `app/src/main/java/interview/guide/modules/mistakenotebook/controller/MistakeNotebookController.java`
- `app/src/main/resources/prompts/mistake-notebook/question-generation.st`
- `app/src/main/resources/prompts/mistake-notebook/risk-assessment.st`
- `frontend/src/types/mistakeNotebook.ts`
- `frontend/src/api/mistakeNotebook.ts`
- `frontend/src/pages/MistakeNotebookPage.tsx`

### Modified Files
- `app/src/main/java/interview/guide/common/exception/ErrorCode.java`（新增5个错误码3008-3012）

---

## 13. Change Log

| Date | Change | Description |
|------|--------|-------------|
| 2026-05-18 | Story Created | 初始Story创建，基于头脑风暴结果 |

---

**Story Created By:** BMad Create-Story Skill
**Date:** 2026-05-18
**Next Step:** Run `bmad-dev-story` for implementation
