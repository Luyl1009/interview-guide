# English Conversation Practice Design

**Date:** 2026-04-24

## Overview

英语情境对话练习模块，目标是提升口语流利度。

## Core Features

- 情境选择（旅行/餐厅）
- AI 扮演角色进行自由对话
- 口语流利度评估反馈
- 复用语音面试的 WebSocket + 流式 TTS

## Scenarios

### 旅行 (Travel)

- 机场值机 (Airport check-in)
- 酒店入住 (Hotel check-in)
- 景点问答 (Sightseeing Q&A)

### 餐厅 (Restaurant)

- 点餐 (Ordering)
- 结账 (Payment)
- 预约 (Reservation)

## Architecture

```
EnglishConversationController
    ↓
EnglishConversationService
    ↓
ScenarioProvider (Travel/Restaurant)
    ↓
ChatClient (LLM)
    ↓
VoiceWebSocketService (复用现有)
```

## Technical Decisions

1. **独立模块**：与现有面试流程解耦
2. **复用**：语音面试的 WebSocket、流式 TTS
3. **评估**：口语流利度、词汇多样性、语法准确度

## Out of Scope

- 其他情境（购物等）暂不包含
- AI Agent 开发方向题库
- 简历相关功能