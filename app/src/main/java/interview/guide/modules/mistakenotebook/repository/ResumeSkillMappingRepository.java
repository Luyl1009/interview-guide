package interview.guide.modules.mistakenotebook.repository;

import interview.guide.modules.mistakenotebook.model.ResumeSkillMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 简历技能映射 Repository
 */
@Repository
public interface ResumeSkillMappingRepository extends JpaRepository<ResumeSkillMappingEntity, Long> {

    /**
     * 根据用户ID查询所有技能映射
     */
    List<ResumeSkillMappingEntity> findByUserId(String userId);

    /**
     * 根据用户ID和简历ID查询技能映射
     */
    List<ResumeSkillMappingEntity> findByUserIdAndResumeId(String userId, Long resumeId);

    /**
     * 根据用户ID和技能名称查询
     */
    List<ResumeSkillMappingEntity> findByUserIdAndSkillName(String userId, String skillName);

    /**
     * 删除用户的所有技能映射（用于重新生成）
     */
    void deleteByUserId(String userId);
}
