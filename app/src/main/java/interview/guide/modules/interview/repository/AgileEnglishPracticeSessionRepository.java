package interview.guide.modules.interview.repository;

import interview.guide.modules.interview.model.AgileEnglishPracticeSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgileEnglishPracticeSessionRepository extends JpaRepository<AgileEnglishPracticeSessionEntity, Long> {

    /**
     * 查询用户的练习会话列表（按创建时间倒序）
     */
    Page<AgileEnglishPracticeSessionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 按场景类型查询
     */
    List<AgileEnglishPracticeSessionEntity> findByScenarioType(String scenarioType);

    /**
     * 查询指定时间范围内的会话
     */
    @Query("SELECT s FROM AgileEnglishPracticeSessionEntity s WHERE s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<AgileEnglishPracticeSessionEntity> findByCreatedAtAfter(LocalDateTime startDate);

    /**
     * 获取练习统计信息
     */
    @Query("SELECT COUNT(s), AVG(s.averageScore) FROM AgileEnglishPracticeSessionEntity s")
    Object[] getPracticeStatistics();
}
