package interview.guide.modules.mistakenotebook.service;

import interview.guide.common.ai.LlmProviderRegistry;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.mistakenotebook.dto.RiskAssessmentResponse;
import interview.guide.modules.mistakenotebook.model.InterviewRiskAssessmentEntity;
import interview.guide.modules.mistakenotebook.repository.InterviewRiskAssessmentRepository;
import interview.guide.modules.mistakenotebook.repository.QuestionCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面试风险评估 Service
 * 基于错题本数据生成面试风险预测
 */
@Slf4j
@Service
public class RiskAssessmentService {

    private final LlmProviderRegistry llmProviderRegistry;
    private final InterviewRiskAssessmentRepository riskAssessmentRepository;
    private final QuestionCardRepository questionCardRepository;
    private final ResourceLoader resourceLoader;

    public RiskAssessmentService(
        LlmProviderRegistry llmProviderRegistry,
        InterviewRiskAssessmentRepository riskAssessmentRepository,
        QuestionCardRepository questionCardRepository,
        ResourceLoader resourceLoader
    ) {
        this.llmProviderRegistry = llmProviderRegistry;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.questionCardRepository = questionCardRepository;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 生成面试风险评估
     *
     * @param userId        用户ID
     * @param interviewDate 面试时间
     * @param jobDescription 岗位JD
     * @return 风险评估结果
     */
    public RiskAssessmentResponse generateAssessment(String userId, LocalDateTime interviewDate, String jobDescription) {
        log.info("开始生成面试风险评估: userId={}, interviewDate={}", userId, interviewDate);

        // 1. 获取用户薄弱技能点
        List<Object[]> weakSkills = questionCardRepository.findWeakSkills(userId, 60.0);
        String weakSkillsText = formatWeakSkills(weakSkills);

        // 2. 调用AI生成评估
        String aiResponse = callAiForAssessment(weakSkillsText, jobDescription);

        // 3. 解析并保存结果
        InterviewRiskAssessmentEntity entity = parseAndSaveAssessment(
            userId, interviewDate, jobDescription, aiResponse
        );

        // 4. 转换为DTO
        return convertToDTO(entity);
    }

    /**
     * 获取用户最新的风险评估
     */
    public RiskAssessmentResponse getLatestAssessment(String userId) {
        InterviewRiskAssessmentEntity entity = riskAssessmentRepository
            .findTopByUserIdOrderByGeneratedAtDesc(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RISK_ASSESSMENT_NOT_FOUND, "暂无风险评估记录"));
        return convertToDTO(entity);
    }

    /**
     * 调用AI进行风险评估（带30秒超时）
     */
    private String callAiForAssessment(String weakSkills, String jobDescription) {
        try {
            ChatClient chatClient = llmProviderRegistry.getChatClientOrDefault(null);

            String promptText = loadPromptTemplate("risk-assessment");
            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                "weakSkills", weakSkills,
                "jobDescription", truncateText(jobDescription, 3000)
            ));

            return chatClient.prompt(prompt)
                .call()
                .content();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("风险评估AI调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.RISK_ASSESSMENT_FAILED,
                "风险评估生成失败: " + e.getMessage());
        }
    }

    /**
     * 解析并保存评估结果
     */
    private InterviewRiskAssessmentEntity parseAndSaveAssessment(
        String userId, LocalDateTime interviewDate, String jobDescription, String aiResponse
    ) {
        InterviewRiskAssessmentEntity entity = new InterviewRiskAssessmentEntity();
        entity.setUserId(userId);
        entity.setInterviewDate(interviewDate);
        entity.setJobDescription(jobDescription);

        Map<String, String> heatmap = new HashMap<>();
        List<InterviewRiskAssessmentEntity.TopQuestion> topQuestions = new ArrayList<>();

        if (aiResponse != null) {
            // 解析风险热力图（按技能提取风险等级）
            heatmap.putAll(parseRiskHeatmap(aiResponse));
            if (heatmap.isEmpty()) {
                heatmap.put("综合技能", "MEDIUM");
            }

            // 解析死亡10题
            topQuestions.addAll(parseTopQuestions(aiResponse));
        }

        entity.setRiskHeatmap(heatmap);
        entity.setTop10Questions(topQuestions);

        return riskAssessmentRepository.save(entity);
    }

    /**
     * 解析风险热力图
     */
    private Map<String, String> parseRiskHeatmap(String aiResponse) {
        Map<String, String> heatmap = new HashMap<>();
        String[] lines = aiResponse.split("\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // 匹配格式：技能名: HIGH/MEDIUM/LOW 或 - 技能名 (HIGH)
            if (trimmed.matches(".*?(HIGH|MEDIUM|LOW).*")) {
                String level = trimmed.contains("HIGH") ? "HIGH"
                    : trimmed.contains("MEDIUM") ? "MEDIUM" : "LOW";
                // 提取技能名（冒号或括号前的部分）
                String skill = trimmed.replaceAll("^(\\s*[-*]?\\s*)", "")
                    .replaceAll("[:：].*", "")
                    .replaceAll("\\s*[(（].*?[)）]", "")
                    .trim();
                if (!skill.isEmpty() && skill.length() < 50) {
                    heatmap.put(skill, level);
                }
            }
        }
        return heatmap;
    }

    /**
     * 解析死亡10题
     */
    private List<InterviewRiskAssessmentEntity.TopQuestion> parseTopQuestions(String aiResponse) {
        List<InterviewRiskAssessmentEntity.TopQuestion> questions = new ArrayList<>();
        String[] lines = aiResponse.split("\\n");
        String currentQuestion = null;
        String currentEmergency = null;

        for (String line : lines) {
            String trimmed = line.trim();
            // 识别题目行（Q1. / 1. / Q: 等格式）
            if (trimmed.matches("^(Q\\d*\\.?|\\d+[.．)）]|【?题目】?).*")) {
                if (currentQuestion != null && questions.size() < 10) {
                    questions.add(new InterviewRiskAssessmentEntity.TopQuestion(
                        currentQuestion,
                        currentEmergency != null ? currentEmergency : "建议坦诚回答，展示学习意愿和解决问题的思路",
                        "核心技能"
                    ));
                }
                currentQuestion = trimmed.replaceFirst("^(Q\\d*\\.?|\\d+[.．)）]|【?题目】?)\\s*", "").trim();
                currentEmergency = null;
            } else if (trimmed.matches(".*?(应急|话术|圆场|补救|答不上).*")) {
                currentEmergency = trimmed.replaceFirst("^.*?(应急|话术|圆场|补救|答不上)[:：]\\s*", "").trim();
            }
        }
        // 添加最后一个题目
        if (currentQuestion != null && questions.size() < 10) {
            questions.add(new InterviewRiskAssessmentEntity.TopQuestion(
                currentQuestion,
                currentEmergency != null ? currentEmergency : "建议坦诚回答，展示学习意愿和解决问题的思路",
                "核心技能"
            ));
        }
        return questions;
    }

    /**
     * 格式化薄弱技能
     */
    private String formatWeakSkills(List<Object[]> weakSkills) {
        if (weakSkills == null || weakSkills.isEmpty()) {
            return "暂无薄弱技能记录";
        }
        StringBuilder sb = new StringBuilder();
        for (Object[] row : weakSkills) {
            String skill = (String) row[0];
            Double avgScore = (Double) row[1];
            sb.append("- ").append(skill).append("(平均掌握度: ")
              .append(String.format("%.1f", avgScore)).append(")\\n");
        }
        return sb.toString();
    }

    /**
     * 转换为DTO
     */
    private RiskAssessmentResponse convertToDTO(InterviewRiskAssessmentEntity entity) {
        RiskAssessmentResponse dto = new RiskAssessmentResponse();
        dto.setId(entity.getId());
        dto.setInterviewDate(entity.getInterviewDate());
        dto.setJobDescription(entity.getJobDescription());
        dto.setRiskHeatmap(entity.getRiskHeatmap());
        dto.setGeneratedAt(entity.getGeneratedAt());

        if (entity.getTop10Questions() != null) {
            List<RiskAssessmentResponse.TopQuestionDTO> questions = entity.getTop10Questions().stream()
                .map(q -> new RiskAssessmentResponse.TopQuestionDTO(
                    q.getQuestion(), q.getEmergencyResponse(), q.getRelatedWeakSkill()
                ))
                .toList();
            dto.setTop10Questions(questions);
        }

        return dto;
    }

    /**
     * 加载Prompt模板
     */
    private String loadPromptTemplate(String templateName) {
        try {
            org.springframework.core.io.Resource resource = resourceLoader.getResource(
                "classpath:prompts/mistake-notebook/" + templateName + ".st"
            );
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("加载Prompt模板失败: {}, 使用默认模板", templateName);
            return getDefaultPromptTemplate();
        }
    }

    private String getDefaultPromptTemplate() {
        return """
            你是面试策略顾问，请分析用户的面试风险：

            用户薄弱技能点(基于错题本):
            {weakSkills}

            目标岗位JD要求:
            {jobDescription}

            请输出：
            1. 风险热力图（HIGH/MEDIUM/LOW）
            2. 最可能考倒用户的10道题
            3. 每道题的应急话术
            """;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
