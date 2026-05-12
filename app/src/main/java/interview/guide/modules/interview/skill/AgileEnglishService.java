package interview.guide.modules.interview.skill;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.guide.common.ai.LlmProviderRegistry;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.interview.model.AgileEnglishPracticeRecordEntity;
import interview.guide.modules.interview.model.AgileEnglishPracticeSessionEntity;
import interview.guide.modules.interview.model.DailyQuoteDTO;
import interview.guide.modules.interview.repository.AgileEnglishPracticeRecordRepository;
import interview.guide.modules.interview.repository.AgileEnglishPracticeSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Agile English Practice Service
 * 提供敏捷开发英语练习场景生成功能
 */
@Slf4j
@Service
public class AgileEnglishService {

    private static final double SCORE_THRESHOLD_EXCELLENT = 7.0;

    private final LlmProviderRegistry llmProviderRegistry;
    private final ResourceLoader resourceLoader;
    private final PromptTemplate scenarioPromptTemplate;
    private final PromptTemplate evaluationPromptTemplate;
    private final PromptTemplate multiTurnPromptTemplate;
    private final AgileEnglishPracticeSessionRepository sessionRepository;
    private final AgileEnglishPracticeRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    private static final Map<String, ScenarioInfo> AVAILABLE_SCENARIOS = new LinkedHashMap<>();

    private static final List<DailyQuoteDTO> DAILY_QUOTES = List.of(
            new DailyQuoteDTO("Agility is about adapting to change.", "敏捷在于适应变化。"),
            new DailyQuoteDTO("Simplicity is the art of maximizing the work not done.", "简洁是最大化未完成工作的艺术。"),
            new DailyQuoteDTO("The best architectures, requirements, and designs emerge from self-organizing teams.", "最好的架构、需求和设计出自自组织团队。"),
            new DailyQuoteDTO("Continuous attention to technical excellence enhances agility.", "持续关注技术卓越能增强敏捷性。"),
            new DailyQuoteDTO("Individuals and interactions over processes and tools.", "个体和互动高于流程和工具。"),
            new DailyQuoteDTO("Working software over comprehensive documentation.", "可工作的软件高于详尽的文档。"),
            new DailyQuoteDTO("Customer collaboration over contract negotiation.", "客户合作高于合同谈判。"),
            new DailyQuoteDTO("Responding to change over following a plan.", "响应变化高于遵循计划。"),
            new DailyQuoteDTO("Fail fast, learn faster.", "快速失败，更快学习。"),
            new DailyQuoteDTO("If it hurts, do it more often.", "如果某件事很痛苦，那就更频繁地去做它。")
    );

    static {
        AVAILABLE_SCENARIOS.put("daily-standup", new ScenarioInfo(
                "daily-standup",
                "Daily Standup",
                "Practice reporting progress, current tasks, and blockers",
                "CORE"
        ));
        AVAILABLE_SCENARIOS.put("sprint-planning", new ScenarioInfo(
                "sprint-planning",
                "Sprint Planning",
                "Practice discussing user stories, estimation, and dependencies",
                "CORE"
        ));
        AVAILABLE_SCENARIOS.put("code-review", new ScenarioInfo(
                "code-review",
                "Code Review",
                "Practice providing constructive feedback on code",
                "CORE"
        ));
        AVAILABLE_SCENARIOS.put("retrospective", new ScenarioInfo(
                "retrospective",
                "Retrospective",
                "Practice discussing what went well and areas for improvement",
                "NORMAL"
        ));
        AVAILABLE_SCENARIOS.put("technical-discussion", new ScenarioInfo(
                "technical-discussion",
                "Technical Discussion",
                "Practice explaining technical solutions and trade-offs",
                "NORMAL"
        ));
        AVAILABLE_SCENARIOS.put("deployment-release", new ScenarioInfo(
                "deployment-release",
                "Deployment & Release",
                "Practice coordinating release activities and status updates",
                "NORMAL"
        ));
    }

    public AgileEnglishService(LlmProviderRegistry llmProviderRegistry,
                               ResourceLoader resourceLoader,
                               AgileEnglishPracticeSessionRepository sessionRepository,
                               AgileEnglishPracticeRecordRepository recordRepository,
                               ObjectMapper objectMapper) {
        this.llmProviderRegistry = llmProviderRegistry;
        this.resourceLoader = resourceLoader;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.objectMapper = objectMapper;
        this.scenarioPromptTemplate = loadPrompt("classpath:prompts/agile-english-scenario.st");
        this.evaluationPromptTemplate = loadPrompt("classpath:prompts/agile-english-evaluation.st");
        this.multiTurnPromptTemplate = loadPrompt("classpath:prompts/agile-english-multiturn.st");
    }

    /**
     * 获取每日一句名言
     *
     * @return 随机返回一条名言
     */
    public DailyQuoteDTO getDailyQuote() {
        Random random = new Random();
        return DAILY_QUOTES.get(random.nextInt(DAILY_QUOTES.size()));
    }

    /**
     * 生成敏捷开发场景对话
     *
     * @param request 场景请求参数
     * @return 生成的对话场景
     */
    @Transactional
    public ScenarioResponse generateScenario(ScenarioRequest request) {
        // 1. 调用 LLM (无事务)
        String response = callLLMForScenario(request);
        
        // 2. 保存会话 (独立事务)
        return saveSessionWithResponse(request, response);
    }
    
    private String callLLMForScenario(ScenarioRequest request) {
        try {
            // 使用不带工具回调的 ChatClient，避免 LLM 尝试调用 skill 工具
            ChatClient chatClient = llmProviderRegistry.createSimpleChatClient();

            Map<String, Object> variables = new HashMap<>();
            variables.put("scenarioType", request.scenarioType());
            variables.put("role", request.role() != null ? request.role() : "developer");
            variables.put("industry", request.industry() != null ? request.industry() : "software development");
            variables.put("difficultyLevel", request.difficultyLevel() != null ? request.difficultyLevel() : "intermediate");
            variables.put("customContext", request.customContext() != null ? request.customContext() : "");

            String systemPrompt = "You are an English language coach specializing in software development workplace communication. Generate realistic conversation scenarios for agile development ceremonies.";
            String userPrompt = scenarioPromptTemplate.render(variables);

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.debug("Generated agile English scenario: type={}, role={}", 
                    request.scenarioType(), request.role());
                    
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call LLM for scenario generation: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, 
                    "Failed to generate scenario: " + e.getMessage());
        }
    }
    
    @Transactional
    private ScenarioResponse saveSessionWithResponse(ScenarioRequest request, String response) {
        try {
            // 创建练习会话记录
            AgileEnglishPracticeSessionEntity session = AgileEnglishPracticeSessionEntity.builder()
                    .scenarioType(request.scenarioType())
                    .role(request.role() != null ? request.role() : "developer")
                    .industry(request.industry() != null ? request.industry() : "software development")
                    .difficultyLevel(request.difficultyLevel() != null ? request.difficultyLevel() : "intermediate")
                    .customContext(request.customContext())
                    .practiceCount(0)
                    .averageScore(0.0)
                    .status(AgileEnglishPracticeSessionEntity.SessionStatus.ACTIVE)
                    .build();
            sessionRepository.save(session);

            log.info("Created practice session: id={}, type={}", session.getId(), request.scenarioType());

            return new ScenarioResponse(
                    request.scenarioType(),
                    response,
                    extractKeyPhrases(response),
                    generatePracticeTips(response)
            );
        } catch (Exception e) {
            log.error("Failed to save practice session: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, 
                    "Failed to save practice session: " + e.getMessage());
        }
    }

    /**
     * 获取可用的练习场景列表
     *
     * @return 场景列表
     */
    public List<ScenarioInfo> getAvailableScenarios() {
        return new ArrayList<>(AVAILABLE_SCENARIOS.values());
    }

    /**
     * 获取常用短语集合
     *
     * @param category 短语分类（可选）
     * @return 短语列表
     */
    public Map<String, List<String>> getPhrases(String category) {
        Map<String, List<String>> phrases = new HashMap<>();

        if (category == null || category.isEmpty()) {
            phrases.put("daily-standup", Arrays.asList(
                    "Yesterday, I worked on...",
                    "Today, I'll be focusing on...",
                    "I'm currently blocked by...",
                    "I might need help with..."
            ));
            phrases.put("code-review", Arrays.asList(
                    "Overall, the implementation looks solid",
                    "I have a few suggestions",
                    "Could we add more comprehensive error handling?",
                    "What do you think?"
            ));
            phrases.put("sprint-planning", Arrays.asList(
                    "This story involves...",
                    "I estimate this will take...",
                    "This depends on...",
                    "We should account for..."
            ));
        } else {
            List<String> categoryPhrases = switch (category.toLowerCase()) {
                case "daily-standup" -> Arrays.asList(
                        "Yesterday, I worked on...",
                        "Today, I'll be focusing on...",
                        "I'm currently blocked by...",
                        "I might need help with..."
                );
                case "code-review" -> Arrays.asList(
                        "Overall, the implementation looks solid",
                        "I have a few suggestions",
                        "Could we add more comprehensive error handling?",
                        "What do you think?"
                );
                case "sprint-planning" -> Arrays.asList(
                        "This story involves...",
                        "I estimate this will take...",
                        "This depends on...",
                        "We should account for..."
                );
                default -> Collections.emptyList();
            };
            phrases.put(category, categoryPhrases);
        }

        return phrases;
    }

    /**
     * 评估用户的英语表达并提供改进建议
     *
     * @param request 评估请求
     * @return 评估结果和建议
     */
    public EvaluationResponse evaluateExpression(EvaluationRequest request) {
        try {
            // 使用不带工具回调的 ChatClient，避免 LLM 尝试调用 skill 工具
            ChatClient chatClient = llmProviderRegistry.createSimpleChatClient();

            Map<String, Object> variables = new HashMap<>();
            variables.put("userExpression", request.userExpression());
            variables.put("scenarioType", request.scenarioType());
            variables.put("context", request.context() != null ? request.context() : "");

            String systemPrompt = "You are an English language expert specializing in software development communication. Evaluate the user's expression and provide constructive feedback.";
            String userPrompt = evaluationPromptTemplate.render(variables);

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Evaluated English expression: scenario={}", request.scenarioType());

            return new EvaluationResponse(
                    response,
                    extractCorrections(response),
                    extractSuggestions(response)
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to evaluate expression: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR,
                    "Failed to evaluate expression: " + e.getMessage());
        }
    }

    private PromptTemplate loadPrompt(String path) {
        try {
            Resource resource = resourceLoader.getResource(path);
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new PromptTemplate(content);
        } catch (Exception e) {
            log.warn("Failed to load prompt from {}, using default", path);
            return new PromptTemplate("");
        }
    }

    private List<String> extractKeyPhrases(String response) {
        List<String> phrases = new ArrayList<>();
        String[] lines = response.split("\n");
        boolean inPhrasesSection = false;

        for (String line : lines) {
            if (line.contains("Key Phrases") || line.contains("Important Expressions")) {
                inPhrasesSection = true;
                continue;
            }
            if (inPhrasesSection && line.trim().startsWith("-") || line.trim().startsWith("*")) {
                phrases.add(line.trim().substring(1).trim());
            } else if (inPhrasesSection && !line.trim().isEmpty() && !line.startsWith(" ")) {
                break;
            }
        }

        return phrases.isEmpty() ? Collections.singletonList("Review the generated scenario for key phrases") : phrases;
    }

    private List<String> generatePracticeTips(String response) {
        return Arrays.asList(
                "Practice speaking the dialogue aloud",
                "Record yourself and compare with native speakers",
                "Try variations of the key phrases",
                "Use these expressions in your next team meeting"
        );
    }

    private String extractCorrections(String response) {
        int correctionsIndex = response.toLowerCase().indexOf("correction");
        if (correctionsIndex != -1) {
            int endIndex = response.indexOf("\n\n", correctionsIndex);
            if (endIndex == -1) endIndex = response.length();
            return response.substring(correctionsIndex, endIndex).trim();
        }
        return "No specific corrections needed";
    }

    private List<String> extractSuggestions(String response) {
        List<String> suggestions = new ArrayList<>();
        String[] lines = response.split("\n");
        boolean inSuggestionsSection = false;

        for (String line : lines) {
            if (line.toLowerCase().contains("suggestion") || line.toLowerCase().contains("improvement")) {
                inSuggestionsSection = true;
                continue;
            }
            if (inSuggestionsSection && (line.trim().startsWith("-") || line.trim().startsWith("*"))) {
                suggestions.add(line.trim().substring(1).trim());
            }
        }

        return suggestions.isEmpty() ? 
                Collections.singletonList("Continue practicing to improve fluency") : suggestions;
    }

    /**
     * 多轮对话 - AI 扮演角色与用户实时对话
     */
    @Transactional
    public MultiTurnResponse continueConversation(MultiTurnRequest request) {
        try {
            // 验证会话是否存在
            AgileEnglishPracticeSessionEntity session = sessionRepository.findById(request.sessionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                            "Session not found: " + request.sessionId()));

            // 使用不带工具回调的 ChatClient
            ChatClient chatClient = llmProviderRegistry.createSimpleChatClient();

            Map<String, Object> variables = new HashMap<>();
            variables.put("scenarioType", request.scenarioType());
            variables.put("role", request.role());
            variables.put("previousDialogue", request.previousDialogue());
            variables.put("conversationHistory", request.conversationHistory() != null ? request.conversationHistory() : "");
            variables.put("userInput", request.userInput());

            String systemPrompt = "You are role-playing in an agile development meeting. Stay in character and respond naturally.";
            String userPrompt = multiTurnPromptTemplate.render(variables);

            String aiResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            // 保存练习记录
            AgileEnglishPracticeRecordEntity record = AgileEnglishPracticeRecordEntity.builder()
                    .sessionId(request.sessionId())
                    .roundNumber(request.roundNumber())
                    .userExpression(request.userInput())
                    .aiFeedback(aiResponse)
                    .build();
            recordRepository.save(record);

            // 更新会话的最后练习时间
            session.setLastPracticedAt(java.time.LocalDateTime.now());
            session.setPracticeCount(session.getPracticeCount() + 1);
            sessionRepository.save(session);

            log.info("Multi-turn conversation continued: sessionId={}, round={}",
                    request.sessionId(), request.roundNumber());

            return new MultiTurnResponse(aiResponse, request.roundNumber() + 1);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to continue conversation: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR,
                    "Failed to continue conversation: " + e.getMessage());
        }
    }

    /**
     * 获取用户的练习历史
     */
    public PracticeHistoryResponse getPracticeHistory(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AgileEnglishPracticeSessionEntity> sessionPage = sessionRepository.findAllByOrderByCreatedAtDesc(pageable);

            List<PracticeSessionDTO> sessions = sessionPage.getContent().stream()
                    .map(this::convertToSessionDTO)
                    .toList();

            return new PracticeHistoryResponse(
                    sessions,
                    sessionPage.getNumber(),
                    sessionPage.getTotalPages(),
                    sessionPage.getTotalElements()
            );

        } catch (Exception e) {
            log.error("Failed to get practice history: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to get practice history: " + e.getMessage());
        }
    }

    /**
     * 获取会话的详细练习记录
     */
    public List<PracticeRecordDTO> getSessionRecords(Long sessionId) {
        try {
            List<AgileEnglishPracticeRecordEntity> records = recordRepository.findBySessionIdOrderByRoundNumberAsc(sessionId);
            return records.stream()
                    .map(this::convertToRecordDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Failed to get session records: sessionId={}", sessionId, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to get session records: " + e.getMessage());
        }
    }

    /**
     * 获取能力评估报告
     */
    public CapabilityReport getCapabilityReport() {
        try {
            // 获取所有会话
            List<AgileEnglishPracticeSessionEntity> allSessions = sessionRepository.findAll();

            if (allSessions.isEmpty()) {
                return new CapabilityReport(
                        0, 0, 0.0,
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        java.time.LocalDateTime.now().toString()
                );
            }

            // 计算统计数据
            int totalSessions = allSessions.size();
            int totalPractices = allSessions.stream()
                    .mapToInt(AgileEnglishPracticeSessionEntity::getPracticeCount)
                    .sum();

            // 按场景类型统计
            Map<String, Integer> scenarioDistribution = new HashMap<>();
            Map<String, List<Double>> scenarioScoresMap = new HashMap<>();

            for (AgileEnglishPracticeSessionEntity session : allSessions) {
                String scenarioType = session.getScenarioType();
                scenarioDistribution.merge(scenarioType, 1, Integer::sum);

                // 获取该会话的所有记录并计算平均分
                List<AgileEnglishPracticeRecordEntity> records = recordRepository.findBySessionIdOrderByRoundNumberAsc(session.getId());
                if (!records.isEmpty()) {
                    double avgScore = records.stream()
                            .filter(r -> r.getScore() != null)
                            .mapToDouble(AgileEnglishPracticeRecordEntity::getScore)
                            .average()
                            .orElse(0.0);
                    scenarioScoresMap.computeIfAbsent(scenarioType, k -> new ArrayList<>()).add(avgScore);
                }
            }

            // 计算每个场景的平均分
            Map<String, Double> scenarioScores = new HashMap<>();
            scenarioScoresMap.forEach((scenario, scores) -> {
                double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                scenarioScores.put(scenario, Math.round(avg * 100.0) / 100.0);
            });

            // 计算总体平均分
            double averageScore = scenarioScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            averageScore = Math.round(averageScore * 100.0) / 100.0;

            // 生成优势和改进建议（简化版）
            List<String> strengths = generateStrengths(scenarioScores);
            List<String> areasForImprovement = generateAreasForImprovement(scenarioScores);

            return new CapabilityReport(
                    totalSessions,
                    totalPractices,
                    averageScore,
                    scenarioDistribution,
                    scenarioScores,
                    strengths,
                    areasForImprovement,
                    java.time.LocalDateTime.now().toString()
            );

        } catch (Exception e) {
            log.error("Failed to get capability report: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "Failed to get capability report: " + e.getMessage());
        }
    }

    private PracticeSessionDTO convertToSessionDTO(AgileEnglishPracticeSessionEntity entity) {
        return new PracticeSessionDTO(
                // entity.getId(),
                entity.getScenarioType(),
                // entity.getRole(),
                // entity.getIndustry(),
                // entity.getDifficultyLevel(),
                // entity.getPracticeCount(),
                // entity.getAverageScore(),
                // entity.getCreatedAt().toString(),
                // entity.getLastPracticedAt() != null ? entity.getLastPracticedAt().toString() : null
                "", "", "", "", 0, 0.0, "", ""
        );
    }

    private PracticeRecordDTO convertToRecordDTO(AgileEnglishPracticeRecordEntity entity) {
        return new PracticeRecordDTO(
                // entity.getId(),
                // entity.getSessionId(),
                // entity.getRoundNumber(),
                // entity.getUserExpression(),
                // entity.getAiFeedback(),
                // entity.getScore() != null ? entity.getScore().doubleValue() : null,
                // entity.getSuggestions(),
                // entity.getBetterExpression(),
                // entity.getCreatedAt().toString()
                0L, 0L, 0, "", "", null, "", "", ""
        );
    }

    private List<String> generateStrengths(Map<String, Double> scenarioScores) {
        List<String> strengths = new ArrayList<>();
        scenarioScores.entrySet().stream()
                .filter(e -> e.getValue() >= SCORE_THRESHOLD_EXCELLENT)
                .forEach(e -> strengths.add("Strong performance in " + formatScenarioName(e.getKey())));

        if (strengths.isEmpty()) {
            strengths.add("Keep practicing to build confidence");
        }
        return strengths;
    }

    private List<String> generateAreasForImprovement(Map<String, Double> scenarioScores) {
        List<String> improvements = new ArrayList<>();
        scenarioScores.entrySet().stream()
                .filter(e -> e.getValue() < SCORE_THRESHOLD_EXCELLENT)
                .forEach(e -> improvements.add("Focus on improving " + formatScenarioName(e.getKey())));

        if (improvements.isEmpty()) {
            improvements.add("Challenge yourself with more complex scenarios");
        }
        return improvements;
    }

    private String formatScenarioName(String scenarioType) {
        return AVAILABLE_SCENARIOS.getOrDefault(scenarioType,
                new ScenarioInfo(scenarioType, scenarioType, "", ""))
                .name();
    }

    // DTOs

    public record ScenarioRequest(
            String scenarioType,
            String role,
            String industry,
            String difficultyLevel,
            String customContext
    ) {}

    public record ScenarioInfo(
            String id,
            String name,
            String description,
            String priority
    ) {}

    public record ScenarioResponse(
            String scenarioType,
            String dialogue,
            List<String> keyPhrases,
            List<String> practiceTips
    ) {}

    public record EvaluationRequest(
            String userExpression,
            String scenarioType,
            String context
    ) {}

    public record EvaluationResponse(
            String feedback,
            String corrections,
            List<String> suggestions
    ) {}

    /**
     * 多轮对话请求
     */
    public record MultiTurnRequest(
            Long sessionId,
            String scenarioType,
            String role,
            String userInput,
            String previousDialogue,
            String conversationHistory,
            Integer roundNumber
    ) {}

    /**
     * 多轮对话响应
     */
    public record MultiTurnResponse(
            String aiResponse,
            Integer nextRoundNumber
    ) {}

    /**
     * 练习会话 DTO
     */
    public record PracticeSessionDTO(
            Long id,
            String scenarioType,
            String role,
            String industry,
            String difficultyLevel,
            Integer practiceCount,
            Double averageScore,
            String createdAt,
            String lastPracticedAt
    ) {}

    /**
     * 练习历史响应
     */
    public record PracticeHistoryResponse(
            List<PracticeSessionDTO> sessions,
            Integer currentPage,
            Integer totalPages,
            Long totalElements
    ) {}

    /**
     * 练习记录 DTO
     */
    public record PracticeRecordDTO(
            Long id,
            Long sessionId,
            Integer roundNumber,
            String userExpression,
            String aiFeedback,
            Double score,
            String suggestions,
            String betterExpression,
            String createdAt
    ) {}

    /**
     * 能力评估报告
     */
    public record CapabilityReport(
            Integer totalSessions,
            Integer totalPractices,
            Double averageScore,
            Map<String, Integer> scenarioDistribution,
            Map<String, Double> scenarioScores,
            List<String> strengths,
            List<String> areasForImprovement,
            String reportGeneratedAt
    ) {}
}
