---
project_name: 'interview-guide'
user_name: 'Lyl'
date: '2026-05-12'
sections_completed: ['technology_stack', 'critical_implementation_rules']
existing_patterns_found: 15
---

# Project Context for AI Agents

_This file contains critical rules and patterns that AI agents must follow when implementing code in this project. Focus on unobvious details that agents might otherwise miss._

---

## Technology Stack & Versions

### Backend (Spring Boot)
- **Framework**: Spring Boot 4.0.1 + Java 21 (Virtual Threads)
- **AI Integration**: Spring AI 2.0.0-M4 (OpenAI compatible mode for Alibaba DashScope)
- **Database**: PostgreSQL 14+ with `pgvector` extension (1024-dim COSINE similarity)
- **Cache/Queue**: Redis 6+ with Redisson 4.0 (Redis Stream for async tasks)
- **Storage**: RustFS (S3 compatible, AWS SDK v2)
- **Document Parsing**: Apache Tika
- **PDF Export**: iText 8
- **Object Mapping**: MapStruct + Lombok
- **API Docs**: SpringDoc OpenAPI

### Frontend (React)
- **Framework**: React 18.3 + TypeScript 5.6
- **Build Tool**: Vite 5.4
- **Styling**: TailwindCSS 4.1 (No Ant Design)
- **State Management**: React Hooks + Context
- **Routing**: React Router DOM 7.11
- **Charts**: Recharts 3.6
- **Icons**: Lucide React + React Icons
- **Audio**: Web Audio API + WebSocket
- **Package Manager**: pnpm 10.26

### Infrastructure
- **Containerization**: Docker Compose (dev/prod profiles)
- **CI/CD**: GitHub Actions (via BMad skills)
- **Worktrees**: `.lingma/config.yaml` managed isolation

---

## Critical Implementation Rules

### 1. Architecture & Layering
- **Modular Monolith**: Strict separation in `app/src/main/java/interview/guide/modules/{module}`.
- **Layering**: Controller → Service → Repository. No business logic in Controllers.
- **DTOs**: Never return Entities to the frontend. Use `XxxDTO`, `XxxRequest`, `XxxResponse`.
- **MapStruct**: Use `@Mapper(componentModel = "spring")` for all Entity ↔ DTO conversions.

### 2. Database & JPA
- **Naming**: Entities end with `Entity` (e.g., `ResumeEntity`).
- **Lazy Loading**: Avoid `LazyInitializationException` by using `@Transactional(readOnly = true)` in Services or `JOIN FETCH` in JPQL.
- **Vector Search**: Use `pgvector` for RAG. Embeddings are 1024 dimensions.
- **Migrations**: Handled by JPA `ddl-auto: update` in dev; manual SQL in prod if needed.

### 3. AI & Async Processing
- **LLM Provider**: Always use `LlmProviderRegistry.getChatClientOrDefault(provider)`.
- **Structured Output**: Wrap calls with `StructuredOutputInvoker` for retry logic.
- **Async Tasks**: Use `AbstractStreamProducer` / `AbstractStreamConsumer` for heavy tasks (resume parsing, vectorization).
- **Prompt Templates**: Store in `resources/prompts/` as `.st` (StringTemplate) files.

### 4. Error Handling & Logging
- **Exceptions**: Throw `BusinessException(ErrorCode.XXX, message)`. Never `RuntimeException`.
- **Error Codes**: Follow the domain ranges (e.g., 2xxx for Resume, 3xxx for Interview).
- **Logging**: Use SLF4J (`@Slf4j`). Log exceptions as the last parameter: `log.error("msg", e)`.

### 5. Frontend Standards
- **UI Library**: Use **TailwindCSS** exclusively. Do not install or use Ant Design/Material UI.
- **API Calls**: Use `axios` with a centralized instance in `frontend/src/api/client.ts`.
- **Type Safety**: All API responses must have corresponding TypeScript interfaces.
- **State**: Use `useState` and `useEffect`. For complex state, consider `useReducer`.

### 6. Development Workflow (Vibe Coding + BMad)
- **Context First**: Before coding, read `docs/superpowers/specs/` and `_bmad-output/`.
- **Story Driven**: Implement based on `bmad-create-story` outputs.
- **Testing**: Write JUnit 5 tests for Services. Use H2 for integration tests.
- **Worktrees**: Use `.lingma/init-worktree.sh` for isolated feature development.

### 7. Security & Configuration
- **Secrets**: Store API keys in `.env`. Never hardcode.
- **CORS**: Configured in `WebConfig.java`.
- **Rate Limiting**: Use `@RateLimit` annotation (AOP + Redis Lua).

---

## Common Patterns & Conventions

### Naming Conventions
- **Java**: UpperCamelCase for classes, lowerCamelCase for methods.
- **Files**: kebab-case for resources, PascalCase for React components.
- **DB**: snake_case for columns and tables.

### Code Style
- **Indentation**: 2 spaces for Java, 2 spaces for TS/JS.
- **Imports**: No wildcard imports. Group imports logically.
- **Comments**: Javadoc for public methods. Inline comments for complex logic.

### Git Workflow
- **Branches**: `main`, `develop`, `feature/{name}`.
- **Commits**: Conventional Commits (feat, fix, docs, style, refactor, test, chore).
- **PRs**: Must pass CI checks and include a description of changes.
