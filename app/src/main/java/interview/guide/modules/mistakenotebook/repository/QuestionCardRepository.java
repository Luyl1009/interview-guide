package interview.guide.modules.mistakenotebook.repository;

import interview.guide.modules.mistakenotebook.model.QuestionCardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 题目卡片 Repository
 */
@Repository
public interface QuestionCardRepository extends JpaRepository<QuestionCardEntity, Long> {

    /**
     * 根据用户ID查询所有题目卡片
     */
    List<QuestionCardEntity> findByUserId(String userId);

    /**
     * 分页查询用户的题目卡片
     */
    Page<QuestionCardEntity> findByUserId(String userId, Pageable pageable);

    /**
     * 查询用户指定技能点的题目卡片
     */
    List<QuestionCardEntity> findByUserIdAndSkillPoint(String userId, String skillPoint);

    /**
     * 查询用户待复习的题目卡片（按下次复习时间排序）
     */
    @Query("SELECT q FROM QuestionCardEntity q WHERE q.userId = :userId AND q.nextReviewAt <= :now ORDER BY q.nextReviewAt ASC")
    List<QuestionCardEntity> findDueCards(@Param("userId") String userId, @Param("now") LocalDateTime now);

    /**
     * 查询用户下一张待复习的卡片
     */
    @Query("SELECT q FROM QuestionCardEntity q WHERE q.userId = :userId AND q.nextReviewAt <= :now ORDER BY q.nextReviewAt ASC")
    List<QuestionCardEntity> findNextDueCard(@Param("userId") String userId, @Param("now") LocalDateTime now);

    /**
     * 多条件筛选查询
     */
    @Query("SELECT q FROM QuestionCardEntity q WHERE q.userId = :userId " +
           "AND (:skillPoint IS NULL OR q.skillPoint = :skillPoint) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:sourceType IS NULL OR q.sourceType = :sourceType) " +
           "AND (:minMastery IS NULL OR q.masteryScore >= :minMastery) " +
           "AND (:maxMastery IS NULL OR q.masteryScore <= :maxMastery) " +
           "ORDER BY q.createdAt DESC")
    Page<QuestionCardEntity> findByFilters(
        @Param("userId") String userId,
        @Param("skillPoint") String skillPoint,
        @Param("difficulty") Integer difficulty,
        @Param("sourceType") String sourceType,
        @Param("minMastery") Double minMastery,
        @Param("maxMastery") Double maxMastery,
        Pageable pageable
    );

    /**
     * 统计用户各技能点的题目数量
     */
    @Query("SELECT q.skillPoint, COUNT(q) FROM QuestionCardEntity q WHERE q.userId = :userId GROUP BY q.skillPoint")
    List<Object[]> countBySkillPoint(@Param("userId") String userId);

    /**
     * 查询用户掌握度低于阈值的技能点（薄弱项）
     */
    @Query("SELECT q.skillPoint, AVG(q.masteryScore) as avgScore FROM QuestionCardEntity q " +
           "WHERE q.userId = :userId GROUP BY q.skillPoint HAVING AVG(q.masteryScore) < :threshold")
    List<Object[]> findWeakSkills(@Param("userId") String userId, @Param("threshold") Double threshold);
}
