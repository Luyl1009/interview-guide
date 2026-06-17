package interview.guide.modules.mistakenotebook.repository;

import interview.guide.modules.mistakenotebook.model.InterviewRiskAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 面试风险评估 Repository
 */
@Repository
public interface InterviewRiskAssessmentRepository extends JpaRepository<InterviewRiskAssessmentEntity, Long> {

    /**
     * 查询用户的所有风险评估记录
     */
    List<InterviewRiskAssessmentEntity> findByUserIdOrderByGeneratedAtDesc(String userId);

    /**
     * 查询用户最新的风险评估记录
     */
    Optional<InterviewRiskAssessmentEntity> findTopByUserIdOrderByGeneratedAtDesc(String userId);

    /**
     * 删除用户的所有风险评估记录
     */
    void deleteByUserId(String userId);
}
