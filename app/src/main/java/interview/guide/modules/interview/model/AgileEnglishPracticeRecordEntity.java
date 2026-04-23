package interview.guide.modules.interview.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agile English Practice Record Entity
 * 敏捷英语练习记录（多轮对话和评估历史）
 */
@Entity
@Table(name = "agile_english_practice_records", indexes = {
    @Index(name = "idx_ae_record_session", columnList = "sessionId"),
    @Index(name = "idx_ae_record_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgileEnglishPracticeRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的会话ID
     */
    @Column(nullable = false)
    private Long sessionId;

    /**
     * 多对一：关联会话
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessionId", insertable = false, updatable = false)
    private AgileEnglishPracticeSessionEntity session;

    /**
     * 轮次编号
     */
    @Column(nullable = false)
    private Integer roundNumber;

    /**
     * 用户输入的表达
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String userExpression;

    /**
     * AI反馈内容
     */
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    /**
     * 评分（0-100）
     */
    private Integer score;

    /**
     * 改进建议（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String suggestions;

    /**
     * 更好的表达方式
     */
    @Column(columnDefinition = "TEXT")
    private String betterExpression;

    /**
     * 纠正内容
     */
    @Column(columnDefinition = "TEXT")
    private String corrections;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
