package interview.guide.modules.mistakenotebook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 简历技能映射实体
 * 存储从简历中提取的技能点信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resume_skill_mapping", indexes = {
    @Index(name = "idx_resume_skill_user", columnList = "userId")
})
public class ResumeSkillMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联用户ID
    @Column(nullable = false, length = 64)
    private String userId;

    // 关联简历ID
    private Long resumeId;

    // 技能名称（如 "Java"）
    @Column(nullable = false, length = 128)
    private String skillName;

    // 熟练度：BEGINNER / INTERMEDIATE / ADVANCED / EXPERT
    @Column(length = 32)
    private String proficiencyLevel;

    // 提取来源：WORK_EXPERIENCE / PROJECTS / SKILLS_SECTION
    @Column(length = 32)
    private String extractedFrom;

    // AI提取置信度 0-1
    private Double confidenceScore;

    // 创建时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
