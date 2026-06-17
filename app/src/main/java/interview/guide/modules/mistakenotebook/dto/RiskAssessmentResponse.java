package interview.guide.modules.mistakenotebook.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 面试风险评估响应 DTO
 */
public class RiskAssessmentResponse {

    private Long id;
    private LocalDateTime interviewDate;
    private String jobDescription;
    private Map<String, String> riskHeatmap;
    private List<TopQuestionDTO> top10Questions;
    private LocalDateTime generatedAt;

    public RiskAssessmentResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Map<String, String> getRiskHeatmap() {
        return riskHeatmap;
    }

    public void setRiskHeatmap(Map<String, String> riskHeatmap) {
        this.riskHeatmap = riskHeatmap;
    }

    public List<TopQuestionDTO> getTop10Questions() {
        return top10Questions;
    }

    public void setTop10Questions(List<TopQuestionDTO> top10Questions) {
        this.top10Questions = top10Questions;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * 死亡题目 DTO
     */
    public static class TopQuestionDTO {
        private String question;
        private String emergencyResponse;
        private String relatedWeakSkill;

        public TopQuestionDTO() {
        }

        public TopQuestionDTO(String question, String emergencyResponse, String relatedWeakSkill) {
            this.question = question;
            this.emergencyResponse = emergencyResponse;
            this.relatedWeakSkill = relatedWeakSkill;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getEmergencyResponse() {
            return emergencyResponse;
        }

        public void setEmergencyResponse(String emergencyResponse) {
            this.emergencyResponse = emergencyResponse;
        }

        public String getRelatedWeakSkill() {
            return relatedWeakSkill;
        }

        public void setRelatedWeakSkill(String relatedWeakSkill) {
            this.relatedWeakSkill = relatedWeakSkill;
        }
    }
}
