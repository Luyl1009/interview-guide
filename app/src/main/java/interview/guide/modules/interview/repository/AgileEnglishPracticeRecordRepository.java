package interview.guide.modules.interview.repository;

import interview.guide.modules.interview.model.AgileEnglishPracticeRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgileEnglishPracticeRecordRepository extends JpaRepository<AgileEnglishPracticeRecordEntity, Long> {

    /**
     * 查询会话的所有练习记录
     */
    List<AgileEnglishPracticeRecordEntity> findBySessionIdOrderByRoundNumberAsc(Long sessionId);

    /**
     * 查询会话的最新一轮记录
     */
    AgileEnglishPracticeRecordEntity findTopBySessionIdOrderByRoundNumberDesc(Long sessionId);

    /**
     * 删除会话的所有记录
     */
    void deleteBySessionId(Long sessionId);
}
