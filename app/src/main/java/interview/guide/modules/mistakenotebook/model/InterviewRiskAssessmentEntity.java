package interview.guide.modules.mistakenotebook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 面试风险评估实体
 * 存储AI生成的面试风险预测结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interview_risk_assessment", indexes = {
    @Index(name = "idx_risk_assessment_user", columnList = "userId,interviewDate")
})
public class InterviewRiskAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联用户ID
    @Column(nullable = false, length = 64)
    private String userId;

    // 面试时间
    private LocalDateTime interviewDate;

    // 岗位JD原文
    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    // 风险热力图数据 {skill: risk_level}
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> riskHeatmap;

    // "死亡10题"列表
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<TopQuestion> top10Questions;

    // 生成时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    /**
     * 死亡题目内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopQuestion {
        private String question;
        private String emergencyResponse;
        private String relatedWeakSkill;
    }
}
