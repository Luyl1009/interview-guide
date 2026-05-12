import request from './request';

export interface ScenarioRequest {
  scenarioType: string;
  role?: string;
  industry?: string;
  difficultyLevel?: string;
  customContext?: string;
}

export interface ScenarioResponse {
  scenarioType: string;
  dialogue: string;
  keyPhrases: string[];
  practiceTips: string[];
}

export interface ScenarioInfo {
  id: string;
  name: string;
  description: string;
  priority: string;
}

export interface EvaluationRequest {
  userExpression: string;
  scenarioType: string;
  context?: string;
  sessionId?: number;
}

export interface EvaluationResponse {
  feedback: string;
  corrections: string;
  suggestions: string[];
  score?: number;
  betterExpression?: string;
}

export interface MultiTurnRequest {
  sessionId: number;
  scenarioType: string;
  role: string;
  userInput: string;
  previousDialogue: string;
  conversationHistory?: string;
  roundNumber: number;
}

export interface MultiTurnResponse {
  aiResponse: string;
  nextRoundNumber: number;
}

export interface PracticeSessionDTO {
  id: number;
  scenarioType: string;
  role: string;
  industry: string;
  difficultyLevel: string;
  practiceCount: number;
  averageScore: number;
  createdAt: string;
  lastPracticedAt: string;
}

export interface PracticeRecordDTO {
  id: number;
  sessionId: number;
  roundNumber: number;
  userExpression: string;
  aiFeedback: string;
  score: number;
  suggestions: string;
  betterExpression: string;
  createdAt: string;
}

export interface PracticeHistoryResponse {
  sessions: PracticeSessionDTO[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export interface CapabilityReport {
  totalSessions: number;
  totalPractices: number;
  averageScore: number;
  scenarioDistribution: Record<string, number>;
  scenarioScores: Record<string, number>;
  strengths: string[];
  areasForImprovement: string[];
  reportGeneratedAt: string;
}

export interface DailyQuoteDTO {
  english: string;
  chinese: string;
}

/**
 * 获取每日一句名言
 */
export function getDailyQuote() {
  return request.get<DailyQuoteDTO>('/api/agile-english/daily-quote');
}

/**
 * 生成敏捷开发场景对话
 */
export function generateScenario(data: ScenarioRequest) {
  return request.post<ScenarioResponse>('/api/agile-english/generate-scenario', data);
}

/**
 * 获取可用的练习场景列表
 */
export function getAvailableScenarios() {
  return request.get<ScenarioInfo[]>('/api/agile-english/scenarios');
}

/**
 * 获取常用短语集合
 */
export function getPhrases(category?: string) {
  return request.get<Record<string, string[]>>('/api/agile-english/phrases', {
    params: category ? { category } : {},
  });
}

/**
 * 评估用户的英语表达
 */
export function evaluateExpression(data: EvaluationRequest) {
  return request.post<EvaluationResponse>('/api/agile-english/evaluate-expression', data);
}

/**
 * 多轮对话 - AI扮演角色与用户实时对话
 */
export function continueConversation(data: MultiTurnRequest) {
  return request.post<MultiTurnResponse>('/api/agile-english/continue-conversation', data);
}

/**
 * 获取用户的练习历史
 */
export function getPracticeHistory(page: number = 0, size: number = 10) {
  return request.get<PracticeHistoryResponse>('/api/agile-english/practice-history', {
    params: { page, size },
  });
}

/**
 * 获取会话的详细练习记录
 */
export function getSessionRecords(sessionId: number) {
  return request.get<PracticeRecordDTO[]>(`/api/agile-english/session/${sessionId}/records`);
}

/**
 * 获取能力评估报告
 */
export function getCapabilityReport() {
  return request.get<CapabilityReport>('/api/agile-english/capability-report');
}
