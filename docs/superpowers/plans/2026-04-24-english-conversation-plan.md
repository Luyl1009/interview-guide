# English Conversation Practice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 扩展 AgileEnglish 模块，新增旅行/餐厅情境的英语对话练习功能

**Architecture:** 复用现有 AgileEnglish 架构，新增 Scenario Provider + Prompt 模板

**Tech Stack:** Spring Boot, Spring AI, JPA, PostgreSQL

---

## File Structure

```
// 新增
app/src/main/java/.../modules/interview/skill/
├── controller/EnglishConversationController.java     # 新增
├── service/EnglishConversationService.java   # 新增 (或复用 AgileEnglishService)
├── provider/ScenarioProvider.java             # 情境提供者接口
├── provider/TravelScenarioProvider.java   # 旅行情境 (新增)
└── provider/RestaurantScenarioProvider.java # 餐厅情境 (新增)

// 修改
app/src/main/java/.../modules/interview/skill/
└── AgileEnglishService.java               # 添加新场景定义

// 新增 prompt 模板
resources/prompts/
├── english-conversation-travel.st      # 旅行情境 prompt
└── english-conversation-restaurant.st # 餐厅情境 prompt
```

---

## Task 1: 添加新场景定义到 AgileEnglishService

**Files:**
- Modify: `app/src/main/java/interview/guide/modules/interview/skill/AgileEnglishService.java:45-84`

- [ ] **Step 1: 添加 Travel/Restaurant 场景定义**

在 AVAILABLE_SCENARIOS static block 中添加：

```java
AVAILABLE_SCENARIOS.put("travel-airport", new ScenarioInfo(
    "travel-airport",
    "Airport Check-in",
    "Practice airport check-in, boarding pass, luggage",
    "CORE"
));
AVAILABLE_SCENARIOS.put("travel-hotel", new ScenarioInfo(
    "travel-hotel",
    "Hotel Check-in",
    "Practice hotel check-in, room preferences, amenities",
    "CORE"
));
AVAILABLE_SCENARIOS.put("restaurant-ordering", new ScenarioInfo(
    "restaurant-ordering",
    "Restaurant Ordering",
    "Practice ordering food, asking about menu, dietary restrictions",
    "CORE"
));
AVAILABLE_SCENARIOS.put("restaurant-reservation", new ScenarioInfo(
    "restaurant-reservation",
    "Restaurant Reservation",
    "Practice making reservations, timing, party size",
    "CORE"
));
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/.../AgileEnglishService.java
git commit -m "feat(english): add travel and restaurant scenarios"
```

---

## Task 2: 创建情境 Prompt 模板

**Files:**
- Create: `resources/prompts/english-conversation-travel.st`
- Create: `resources/prompts/english-conversation-restaurant.st`

- [ ] **Step 1: 创建旅行情境 prompt**

```st
You are a friendly local at an airport/hotel in a foreign country.

Scenario: {scenario_type}
- Airport: help user with check-in, boarding pass issues, luggage
- Hotel: help user with check-in, room preferences, amenities questions

Context: {user_level} (beginner/intermediate/advanced)
Style: Friendly, casual, helpful

Start with a warm greeting and ask how you can help.
Keep responses short (2-3 sentences).
If user makes mistakes, gently correct with explanation.
```

- [ ] **Step 2: 创建餐厅情境 prompt**

```st
You are a helpful server/waiter at a restaurant.

Scenario: {scenario_type}
- Ordering: help user read menu, make choices, handle dietary restrictions
- Reservation: help user with booking, timing, party size

Context: {user_level} (beginner/intermediate/advanced)
Style: Friendly, casual, professional

Start with a warm greeting and ask if they have questions.
Keep responses short (2-3 sentences).
If user makes mistakes, gently correct with explanation.
```

- [ ] **Step 3: Commit**

```bash
git add resources/prompts/english-conversation-*.st
git commit -m "feat(prompt): add travel and restaurant conversation prompts"
```

---

## Task 3: 添加新场景的场景生成逻辑

**Files:**
- Modify: `app/src/main/java/.../modules/interview/skill/AgileEnglishService.java`

- [ ] **Step 1: 添加 getScenarioPromptTemplate 方法**

在 AgileEnglishService 中添加根据场景类型返回对应 prompt 模板的逻辑：

```java
private PromptTemplate getScenarioPromptTemplate(String scenarioId) {
    return switch (scenarioId) {
        case "travel-airport", "travel-hotel" -> loadPrompt("classpath:prompts/english-conversation-travel.st");
        case "restaurant-ordering", "restaurant-reservation" -> loadPrompt("classpath:prompts/english-conversation-restaurant.st");
        default -> scenarioPromptTemplate; // 原有默认
    };
}
```

- [ ] **Step 2: 修改 generateScenario 方法使用动态 prompt**

修改 generateScenario 使用 getScenarioPromptTemplate 加载对应 prompt

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/.../AgileEnglishService.java
git commit -m "feat(english): support dynamic prompt for travel/restaurant scenarios"
```

---

## Task 4: 服务启动验证

**Files:**
- None (验证)

- [ ] **Step 1: 启动应用验证**

```bash
./gradlew bootRun
# 或
./gradlew build
```

- [ ] **Step 2: 测试 API**

```bash
curl http://localhost:8080/api/agile-english/scenarios
```

验证新场景出现在列表中

- [ ] **Step 3: 测试场景生成**

```bash
curl -X POST http://localhost:8080/api/agile-english/generate-scenario \
  -H "Content-Type: application/json" \
  -d '{"scenarioId": "travel-airport", "userLevel": "intermediate"}'
```

验证返回正确的旅行情境对话

---

## Task 5: 单元测试

**Files:**
- Create: `app/src/test/java/.../AgileEnglishServiceTest.java`

- [ ] **Step 1: 编写场景测试**

```java
@Test
void testTravelScenariosIncluded() {
    var scenarios = service.getAvailableScenarios();
    assertTrue(scenarios.stream().anyMatch(s -> s.id().equals("travel-airport")));
    assertTrue(scenarios.stream().anyMatch(s -> s.id().equals("travel-hotel")));
}

@Test
void testRestaurantScenariosIncluded() {
    var scenarios = service.getAvailableScenarios();
    assertTrue(scenarios.stream().anyMatch(s -> s.id().equals("restaurant-ordering")));
    assertTrue(scenarios.stream().anyMatch(s -> s.id().equals("restaurant-reservation")));
}
```

- [ ] **Step 2: 运行测试**

```bash
./gradlew test --tests AgileEnglishServiceTest
```

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/.../AgileEnglishServiceTest.java
git commit -m "test: add unit tests for travel/restaurant scenarios"
```

---

## Summary

| Task | Description |
|------|-------------|
| 1 | 添加场景定义到 AVAILABLE_SCENARIOS |
| 2 | 创建 prompt 模板文件 |
| 3 | 添加动态 prompt 加载逻辑 |
| 4 | 启动验证 |
| 5 | 单元测试 |