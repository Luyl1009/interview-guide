import client from './request';
import type { QuestionCard, RiskAssessment, QuestionFilterParams, ReviewRequest, RiskAssessmentRequest, Page } from '../types/mistakeNotebook';

const DEFAULT_USER_ID = 'default';

export const mistakeNotebookApi = {
  generateFromResume: () =>
    client.post(`/mistake-notebook/generate-from-resume?userId=${DEFAULT_USER_ID}`),

  getNextReviewCard: () =>
    client.get<QuestionCard>(`/mistake-notebook/cards/next-review?userId=${DEFAULT_USER_ID}`),

  submitCardReview: (id: number, data: ReviewRequest) =>
    client.post(`/mistake-notebook/cards/${id}/review?userId=${DEFAULT_USER_ID}`, data),

  filterQuestions: (params: QuestionFilterParams) =>
    client.get<Page<QuestionCard>>(`/mistake-notebook/questions?userId=${DEFAULT_USER_ID}`, { params }),

  getUserSkills: () =>
    client.get<string[]>(`/mistake-notebook/skills?userId=${DEFAULT_USER_ID}`),

  generateRiskAssessment: (data: RiskAssessmentRequest) =>
    client.post<RiskAssessment>(`/mistake-notebook/risk-assessment?userId=${DEFAULT_USER_ID}`, data),

  getLatestRiskAssessment: () =>
    client.get<RiskAssessment>(`/mistake-notebook/risk-assessment/latest?userId=${DEFAULT_USER_ID}`),
};
