export interface QuestionCard {
  id: number;
  questionText: string;
  answerText: string;
  scoringPoints: string[];
  followUpQuestions: string[];
  skillPoint: string;
  difficulty: number;
  sourceType: string;
  masteryScore: number;
  lastReviewedAt: string;
  nextReviewAt: string;
  reviewCount: number;
  createdAt: string;
}

export interface TopQuestion {
  question: string;
  emergencyResponse: string;
  relatedWeakSkill: string;
}

export interface RiskAssessment {
  id: number;
  interviewDate: string;
  jobDescription: string;
  riskHeatmap: Record<string, string>;
  top10Questions: TopQuestion[];
  generatedAt: string;
}

export interface QuestionFilterParams {
  skillPoint?: string;
  difficulty?: number;
  sourceType?: string;
  minMastery?: number;
  maxMastery?: number;
  page?: number;
  size?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ReviewRequest {
  mastery: 'again' | 'hard' | 'good' | 'easy';
}

export interface RiskAssessmentRequest {
  interviewDate: string;
  jobDescription: string;
}
