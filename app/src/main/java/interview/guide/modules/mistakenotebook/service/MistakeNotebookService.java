package interview.guide.modules.mistakenotebook.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.mistakenotebook.async.QuestionGenerationProducer;
import interview.guide.modules.mistakenotebook.dto.QuestionCardDTO;
import interview.guide.modules.mistakenotebook.dto.QuestionFilterRequest;
import interview.guide.modules.mistakenotebook.dto.RiskAssessmentRequest;
import interview.guide.modules.mistakenotebook.dto.RiskAssessmentResponse;
import interview.guide.modules.mistakenotebook.model.QuestionCardEntity;
import interview.guide.modules.mistakenotebook.repository.QuestionCardRepository;
import interview.guide.modules.mistakenotebook.repository.ResumeSkillMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题本主 Service
 * 业务逻辑编排层
 */
@Slf4j
@Service
public class MistakeNotebookService {

    private final QuestionCardRepository questionCardRepository;
    private final ResumeSkillMappingRepository skillMappingRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final QuestionGenerationService questionGenerationService;
    private final RiskAssessmentService riskAssessmentService;
    private final QuestionGenerationProducer questionGenerationProducer;

    public MistakeNotebookService(
        QuestionCardRepository questionCardRepository,
        ResumeSkillMappingRepository skillMappingRepository,
        SpacedRepetitionService spacedRepetitionService,
        QuestionGenerationService questionGenerationService,
        RiskAssessmentService riskAssessmentService,
        QuestionGenerationProducer questionGenerationProducer
    ) {
        this.questionCardRepository = questionCardRepository;
        this.skillMappingRepository = skillMappingRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.questionGenerationService = questionGenerationService;
        this.riskAssessmentService = riskAssessmentService;
        this.questionGenerationProducer = questionGenerationProducer;
    }

    // ========== 题目生成（异步）==========

    /**
     * 基于简历异步生成题库
     * 通过 Redis Stream 发送任务，避免同步等待 LLM 调用
     */
    public void generateFromResume(String userId) {
        log.info("用户请求异步生成题库: userId={}", userId);
        questionGenerationProducer.sendGenerateTask(userId, null, 20);
    }

    // ========== 卡片复习 ==========

    /**
     * 获取下一张待复习卡片
     */
    public QuestionCardDTO getNextReviewCard(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<QuestionCardEntity> cards = questionCardRepository.findNextDueCard(userId, now);
        if (cards.isEmpty()) {
            throw new BusinessException(ErrorCode.QUESTION_CARD_NOT_FOUND, "暂无待复习卡片");
        }
        return convertToDTO(cards.get(0));
    }

    /**
     * 提交卡片复习结果
     */
    @Transactional
    public void submitCardReview(String userId, Long cardId, String mastery) {
        QuestionCardEntity card = questionCardRepository.findById(cardId)
            .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_CARD_NOT_FOUND, "题目卡片不存在"));

        if (!card.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该卡片");
        }

        SpacedRepetitionService.MasteryLevel masteryLevel = spacedRepetitionService.parseMasteryLevel(mastery);
        spacedRepetitionService.processReview(card, masteryLevel);

        questionCardRepository.save(card);
        log.info("卡片复习已保存: cardId={}, mastery={}", cardId, mastery);
    }

    // ========== 题目查询 ==========

    /**
     * 多维度筛选题目
     */
    public Page<QuestionCardDTO> filterQuestions(String userId, QuestionFilterRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());

        Page<QuestionCardEntity> page = questionCardRepository.findByFilters(
            userId,
            request.skillPoint(),
            request.difficulty(),
            request.sourceType(),
            request.minMastery(),
            request.maxMastery(),
            pageable
        );

        List<QuestionCardDTO> dtos = page.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    /**
     * 获取用户所有技能点分布
     */
    public List<String> getUserSkills(String userId) {
        return skillMappingRepository.findByUserId(userId).stream()
            .map(m -> m.getSkillName())
            .distinct()
            .collect(Collectors.toList());
    }

    // ========== 风险评估 ==========

    /**
     * 生成面试风险评估
     */
    public RiskAssessmentResponse generateRiskAssessment(String userId, RiskAssessmentRequest request) {
        return riskAssessmentService.generateAssessment(userId, request.interviewDate(), request.jobDescription());
    }

    /**
     * 获取最新风险评估
     */
    public RiskAssessmentResponse getLatestRiskAssessment(String userId) {
        return riskAssessmentService.getLatestAssessment(userId);
    }

    // ========== 转换方法 ==========

    private QuestionCardDTO convertToDTO(QuestionCardEntity entity) {
        QuestionCardDTO dto = new QuestionCardDTO();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setAnswerText(entity.getAnswerText());
        dto.setScoringPoints(entity.getScoringPoints());
        dto.setFollowUpQuestions(entity.getFollowUpQuestions());
        dto.setSkillPoint(entity.getSkillPoint());
        dto.setDifficulty(entity.getDifficulty());
        dto.setSourceType(entity.getSourceType());
        dto.setMasteryScore(entity.getMasteryScore());
        dto.setLastReviewedAt(entity.getLastReviewedAt());
        dto.setNextReviewAt(entity.getNextReviewAt());
        dto.setReviewCount(entity.getReviewCount());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
