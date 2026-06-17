package interview.guide.modules.mistakenotebook.service;

import interview.guide.modules.mistakenotebook.model.QuestionCardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 间隔重复算法 Service
 * 基于简化版 SM-2 算法计算下次复习时间
 */
@Slf4j
@Service
public class SpacedRepetitionService {

    /**
     * 掌握度评级
     */
    public enum MasteryLevel {
        AGAIN,  // 完全不会 - 1小时后复习
        HARD,   // 有点印象 - 1天后复习
        GOOD,   // 基本正确 - 3天后复习
        EASY    // 完美回答 - 指数增长
    }

    /**
     * 根据掌握度评级计算下次复习时间
     *
     * @param masteryLevel 掌握度评级
     * @param reviewCount  已复习次数
     * @param lastReview   最后复习时间
     * @return 下次复习时间
     */
    public LocalDateTime calculateNextReview(MasteryLevel masteryLevel, int reviewCount, LocalDateTime lastReview) {
        if (lastReview == null) {
            lastReview = LocalDateTime.now();
        }

        LocalDateTime nextReview = switch (masteryLevel) {
            case AGAIN -> lastReview.plusHours(1);
            case HARD -> lastReview.plusDays(1);
            case GOOD -> lastReview.plusDays(3);
            case EASY -> lastReview.plusDays(7L * Math.max(reviewCount, 1));
        };

        log.info("间隔重复计算: mastery={}, reviewCount={}, nextReview={}",
            masteryLevel, reviewCount, nextReview);

        return nextReview;
    }

    /**
     * 计算新的掌握度评分
     *
     * @param currentScore 当前掌握度 0-100
     * @param masteryLevel 掌握度评级
     * @return 更新后的掌握度评分
     */
    public double calculateMasteryScore(double currentScore, MasteryLevel masteryLevel) {
        double targetScore = switch (masteryLevel) {
            case AGAIN -> 0.0;
            case HARD -> 50.0;
            case GOOD -> 75.0;
            case EASY -> 100.0;
        };

        // 使用加权移动平均，新评分权重40%
        double newScore = currentScore * 0.6 + targetScore * 0.4;
        return Math.min(100.0, Math.max(0.0, newScore));
    }

    /**
     * 将字符串评级转换为枚举
     *
     * @param mastery 字符串评级
     * @return MasteryLevel 枚举
     */
    public MasteryLevel parseMasteryLevel(String mastery) {
        if (mastery == null) {
            throw new interview.guide.common.exception.BusinessException(
                interview.guide.common.exception.ErrorCode.BAD_REQUEST, "掌握度评级不能为空");
        }
        return switch (mastery.toLowerCase()) {
            case "again" -> MasteryLevel.AGAIN;
            case "hard" -> MasteryLevel.HARD;
            case "good" -> MasteryLevel.GOOD;
            case "easy" -> MasteryLevel.EASY;
            default -> throw new interview.guide.common.exception.BusinessException(
                interview.guide.common.exception.ErrorCode.BAD_REQUEST,
                "无效的掌握度评级: " + mastery + "，可选值: again, hard, good, easy");
        };
    }

    /**
     * 处理卡片复习
     *
     * @param card         题目卡片
     * @param masteryLevel 掌握度评级
     */
    public void processReview(QuestionCardEntity card, MasteryLevel masteryLevel) {
        int newReviewCount = card.getReviewCount() + 1;
        double newMasteryScore = calculateMasteryScore(
            card.getMasteryScore() != null ? card.getMasteryScore() : 0.0,
            masteryLevel
        );
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReview = calculateNextReview(masteryLevel, newReviewCount, now);

        card.setReviewCount(newReviewCount);
        card.setMasteryScore(newMasteryScore);
        card.setLastReviewedAt(now);
        card.setNextReviewAt(nextReview);

        log.info("卡片复习完成: cardId={}, mastery={}, newScore={}, reviewCount={}",
            card.getId(), masteryLevel, newMasteryScore, newReviewCount);
    }
}
