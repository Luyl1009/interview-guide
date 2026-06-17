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

/**
 * 题目卡片实体
 * 存储面试题、参考答案、掌握度等信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_card", indexes = {
    @Index(name = "idx_question_card_user_skill", columnList = "userId,skillPoint"),
    @Index(name = "idx_question_card_next_review", columnList = "nextReviewAt")
})
public class QuestionCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联用户ID
    @Column(nullable = false, length = 64)
    private String userId;

    // 题目内容
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // 参考答案
    @Column(columnDefinition = "TEXT")
    private String answerText;

    // 得分要点（JSON数组存储）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> scoringPoints;

    // 常见追问（JSON数组存储）
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> followUpQuestions;

    // 关联技能点
    @Column(length = 128)
    private String skillPoint;

    // 难度 1-5
    @Column(nullable = false)
    private Integer difficulty = 3;

    // 来源类型：AI_GENERATED / MANUAL / INTERVIEW_MISTAKE
    @Column(length = 32)
    private String sourceType = "AI_GENERATED";

    // 掌握度评分 0-100
    @Column(nullable = false)
    private Double masteryScore = 0.0;

    // 最后复习时间
    private LocalDateTime lastReviewedAt;

    // 下次复习时间（间隔重复算法计算）
    private LocalDateTime nextReviewAt;

    // 复习次数
    @Column(nullable = false)
    private Integer reviewCount = 0;

    // 创建时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (masteryScore == null) {
            masteryScore = 0.0;
        }
        if (reviewCount == null) {
            reviewCount = 0;
        }
        if (difficulty == null) {
            difficulty = 3;
        }
        if (sourceType == null) {
            sourceType = "AI_GENERATED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
