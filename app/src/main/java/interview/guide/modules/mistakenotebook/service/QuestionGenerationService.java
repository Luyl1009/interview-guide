package interview.guide.modules.mistakenotebook.service;

import interview.guide.common.ai.LlmProviderRegistry;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.mistakenotebook.model.QuestionCardEntity;
import interview.guide.modules.mistakenotebook.model.ResumeSkillMappingEntity;
import interview.guide.modules.mistakenotebook.repository.QuestionCardRepository;
import interview.guide.modules.mistakenotebook.repository.ResumeSkillMappingRepository;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI 题目生成 Service
 * 基于简历技能点生成个性化面试题
 */
@Slf4j
@Service
public class QuestionGenerationService {

    private final LlmProviderRegistry llmProviderRegistry;
    private final QuestionCardRepository questionCardRepository;
    private final ResumeSkillMappingRepository skillMappingRepository;
    private final ResumeRepository resumeRepository;
    private final ResourceLoader resourceLoader;

    public QuestionGenerationService(
        LlmProviderRegistry llmProviderRegistry,
        QuestionCardRepository questionCardRepository,
        ResumeSkillMappingRepository skillMappingRepository,
        ResumeRepository resumeRepository,
        ResourceLoader resourceLoader
    ) {
        this.llmProviderRegistry = llmProviderRegistry;
        this.questionCardRepository = questionCardRepository;
        this.skillMappingRepository = skillMappingRepository;
        this.resumeRepository = resumeRepository;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 基于简历生成题目
     *
     * @param userId   用户ID
     * @param resumeId 简历ID（可选，为null则使用用户最新简历）
     * @param count    生成数量
     */
    public void generateQuestionsFromResume(String userId, Long resumeId, int count) {
        log.info("开始为用户生成面试题: userId={}, resumeId={}, count={}", userId, resumeId, count);

        // 1. 获取简历
        ResumeEntity resume = getResume(userId, resumeId);
        if (resume == null || resume.getResumeText() == null) {
            throw new BusinessException(ErrorCode.RESUME_NOT_FOUND, "未找到有效的简历，请先上传简历");
        }

        // 2. 提取技能点（简化版：直接从简历文本提取关键词）
        List<String> skills = extractSkills(resume.getResumeText());
        if (skills.isEmpty()) {
            skills = List.of("Java", "Spring", "数据库"); // 默认技能点
        }

        // 3. 保存技能映射
        saveSkillMappings(userId, resume.getId(), skills);

        // 4. 为每个技能点生成题目
        List<QuestionCardEntity> generatedCards = new ArrayList<>();
        int perSkillCount = Math.max(1, count / skills.size());

        for (String skill : skills) {
            List<QuestionCardEntity> cards = generateQuestionsForSkill(
                userId, resume.getResumeText(), skill, perSkillCount
            );
            generatedCards.addAll(cards);
        }

        // 5. 保存到数据库
        questionCardRepository.saveAll(generatedCards);

        log.info("题目生成完成: userId={}, generatedCount={}", userId, generatedCards.size());
    }

    /**
     * 为指定技能点生成题目
     */
    private List<QuestionCardEntity> generateQuestionsForSkill(
        String userId, String resumeText, String skill, int count
    ) {
        try {
            ChatClient chatClient = llmProviderRegistry.getChatClientOrDefault(null);

            String promptText = loadPromptTemplate("question-generation");
            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                "resumeContext", truncateText(resumeText, 2000),
                "skillPoint", skill,
                "questionType", "CONCEPT",
                "difficulty", "3",
                "count", String.valueOf(count)
            ));

            String response = chatClient.prompt(prompt).call().content();
            return parseGeneratedQuestions(userId, skill, response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成题目失败: skill={}, error={}", skill, e.getMessage(), e);
            throw new BusinessException(ErrorCode.QUESTION_GENERATION_FAILED,
                "生成题目失败: " + e.getMessage());
        }
    }

    /**
     * 解析AI生成的题目
     */
    private List<QuestionCardEntity> parseGeneratedQuestions(String userId, String skill, String response) {
        List<QuestionCardEntity> cards = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return cards;
        }

        // 简化解析：按题目块分割
        String[] blocks = response.split("(?m)^#{1,2}\\s+");
        for (String block : blocks) {
            if (block.trim().isEmpty()) continue;

            QuestionCardEntity card = new QuestionCardEntity();
            card.setUserId(userId);
            card.setSkillPoint(skill);
            card.setQuestionText(extractSection(block, "题目|Question"));
            card.setAnswerText(extractSection(block, "答案|Answer|参考答案"));
            card.setScoringPoints(extractListSection(block, "得分要点|Scoring Points|要点"));
            card.setFollowUpQuestions(extractListSection(block, "追问|Follow-up|常见追问"));
            card.setDifficulty(3);
            card.setSourceType("AI_GENERATED");
            card.setMasteryScore(0.0);
            card.setReviewCount(0);

            if (card.getQuestionText() != null && !card.getQuestionText().isBlank()) {
                cards.add(card);
            }
        }

        return cards;
    }

    /**
     * 提取列表段落内容（按行分割）
     */
    private List<String> extractListSection(String text, String sectionNames) {
        String section = extractSection(text, sectionNames);
        if (section == null || section.isBlank()) {
            return List.of();
        }
        return section.lines()
            .map(String::trim)
            .filter(line -> !line.isEmpty() && (line.startsWith("-") || line.startsWith("*") || line.matches("^\\d+[.)]")))
            .map(line -> line.replaceFirst("^[-*\\d.\\)\\s]+", "").trim())
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * 从简历文本提取技能点（简化实现）
     */
    private List<String> extractSkills(String resumeText) {
        List<String> commonSkills = List.of(
            "Java", "Spring", "Spring Boot", "MySQL", "Redis", "Kafka",
            "Docker", "Kubernetes", "React", "Vue", "Python", "Go",
            "微服务", "分布式", "高并发", "Linux"
        );

        String upperText = resumeText.toUpperCase();
        return commonSkills.stream()
            .filter(skill -> upperText.contains(skill.toUpperCase()))
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
    }

    /**
     * 保存技能映射
     */
    private void saveSkillMappings(String userId, Long resumeId, List<String> skills) {
        skillMappingRepository.deleteByUserId(userId);

        List<ResumeSkillMappingEntity> mappings = skills.stream()
            .map(skill -> {
                ResumeSkillMappingEntity mapping = new ResumeSkillMappingEntity();
                mapping.setUserId(userId);
                mapping.setResumeId(resumeId);
                mapping.setSkillName(skill);
                mapping.setProficiencyLevel("INTERMEDIATE");
                mapping.setExtractedFrom("SKILLS_SECTION");
                mapping.setConfidenceScore(0.8);
                return mapping;
            })
            .collect(Collectors.toList());

        skillMappingRepository.saveAll(mappings);
    }

    /**
     * 获取简历
     */
    private ResumeEntity getResume(String userId, Long resumeId) {
        if (resumeId != null) {
            return resumeRepository.findById(resumeId).orElse(null);
        }
        // 获取最新上传的简历（限制只查1条，避免全表扫描）
        Page<ResumeEntity> page = resumeRepository.findAll(
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "uploadedAt"))
        );
        return page.hasContent() ? page.getContent().get(0) : null;
    }

    private final Map<String, String> promptTemplateCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void preloadPromptTemplates() {
        preloadTemplate("question-generation");
        preloadTemplate("risk-assessment");
    }

    private void preloadTemplate(String templateName) {
        try {
            org.springframework.core.io.Resource resource = resourceLoader.getResource(
                "classpath:prompts/mistake-notebook/" + templateName + ".st"
            );
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            promptTemplateCache.put(templateName, content);
            log.info("Prompt模板预加载成功: {}", templateName);
        } catch (Exception e) {
            log.warn("Prompt模板预加载失败: {}, 将在运行时回退到默认模板", templateName);
        }
    }

    /**
     * 加载Prompt模板（优先从缓存读取）
     */
    private String loadPromptTemplate(String templateName) {
        String cached = promptTemplateCache.get(templateName);
        if (cached != null) {
            return cached;
        }
        return getDefaultPromptTemplate();
    }

    /**
     * 默认Prompt模板
     */
    private String getDefaultPromptTemplate() {
        return """
            你是资深面试官，请根据以下用户背景生成 {count} 道面试题：

            用户简历背景:
            {resumeContext}

            目标技能点: {skillPoint}
            题目类型: {questionType}
            难度等级: {difficulty}

            每道题请包含：
            ## 题目
            [题目内容]
            ## 答案
            [参考答案]
            """;
    }

    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * 提取段落内容
     */
    private String extractSection(String text, String sectionNames) {
        String[] names = sectionNames.split("\\|");
        for (String name : names) {
            int index = text.indexOf(name);
            if (index >= 0) {
                int start = index + name.length();
                int end = text.indexOf("##", start);
                if (end < 0) end = text.length();
                return text.substring(start, end).trim();
            }
        }
        return text.trim();
    }
}
