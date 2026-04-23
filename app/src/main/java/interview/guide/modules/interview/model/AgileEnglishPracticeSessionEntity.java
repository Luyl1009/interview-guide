package interview.guide.modules.interview.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agile English Practice Session Entity
 * 敏捷英语练习会话记录
 */
@Entity
@Table(name = "agile_english_sessions", indexes = {
    @Index(name = "idx_ae_session_created", columnList = "createdAt"),
    @Index(name = "idx_ae_session_scenario", columnList = "scenarioType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgileEnglishPracticeSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 场景类型
     */
    @Column(nullable = false, length = 50)
    private String scenarioType;

    /**
     * 用户角色
     */
    @Column(length = 100)
    private String role;

    /**
     * 行业
     */
    @Column(length = 100)
    private String industry;

    /**
     * 难度级别
     */
    @Column(length = 20)
    private String difficultyLevel;

    /**
     * 自定义上下文
     */
    @Column(columnDefinition = "TEXT")
    private String customContext;

    /**
     * 生成的对话内容
     */
    @Column(columnDefinition = "TEXT")
    private String generatedDialogue;

    /**
     * 关键短语（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String keyPhrases;

    /**
     * 会话状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    /**
     * 练习次数
     */
    @Builder.Default
    private Integer practiceCount = 0;

    /**
     * 平均评分
     */
    @Builder.Default
    private Double averageScore = 0.0;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后练习时间
     */
    private LocalDateTime lastPracticedAt;

    public enum SessionStatus {
        ACTIVE,     // 活跃
        COMPLETED,  // 已完成
        ARCHIVED    // 已归档
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastPracticedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastPracticedAt = LocalDateTime.now();
    }

    /**
     * 更新练习统计
     */
    public void updatePracticeStats(double score) {
        this.practiceCount++;
        if (this.averageScore == 0.0) {
            this.averageScore = score;
        } else {
            this.averageScore = (this.averageScore * (this.practiceCount - 1) + score) / this.practiceCount;
        }
        this.lastPracticedAt = LocalDateTime.now();
    }
}
