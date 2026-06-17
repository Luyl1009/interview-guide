package interview.guide.modules.mistakenotebook.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目卡片 DTO
 * 用于前后端数据传输
 */
public class QuestionCardDTO {

    private Long id;
    private String questionText;
    private String answerText;
    private List<String> scoringPoints;
    private List<String> followUpQuestions;
    private String skillPoint;
    private Integer difficulty;
    private String sourceType;
    private Double masteryScore;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
    private Integer reviewCount;
    private LocalDateTime createdAt;

    public QuestionCardDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public List<String> getScoringPoints() {
        return scoringPoints;
    }

    public void setScoringPoints(List<String> scoringPoints) {
        this.scoringPoints = scoringPoints;
    }

    public List<String> getFollowUpQuestions() {
        return followUpQuestions;
    }

    public void setFollowUpQuestions(List<String> followUpQuestions) {
        this.followUpQuestions = followUpQuestions;
    }

    public String getSkillPoint() {
        return skillPoint;
    }

    public void setSkillPoint(String skillPoint) {
        this.skillPoint = skillPoint;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(LocalDateTime nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
