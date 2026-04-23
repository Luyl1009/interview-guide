package interview.guide.modules.interview.skill;

import interview.guide.common.annotation.RateLimit;
import interview.guide.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agile English Practice Controller
 * 提供敏捷开发英语练习场景生成功能
 */
@RestController
@RequestMapping("/api/agile-english")
public class AgileEnglishController {

    private final AgileEnglishService agileEnglishService;

    public AgileEnglishController(AgileEnglishService agileEnglishService) {
        this.agileEnglishService = agileEnglishService;
    }

    /**
     * 生成敏捷开发场景对话
     *
     * @param request 场景请求参数
     * @return 生成的对话场景
     */
    @PostMapping("/generate-scenario")
    @RateLimit(dimension = RateLimit.Dimension.IP, count = 10)
    public Result<AgileEnglishService.ScenarioResponse> generateScenario(
            @Valid @RequestBody AgileEnglishService.ScenarioRequest request) {
        return Result.success(agileEnglishService.generateScenario(request));
    }

    /**
     * 获取可用的练习场景列表
     *
     * @return 场景列表
     */
    @GetMapping("/scenarios")
    public Result<List<AgileEnglishService.ScenarioInfo>> getAvailableScenarios() {
        return Result.success(agileEnglishService.getAvailableScenarios());
    }

    /**
     * 获取常用短语集合
     *
     * @param category 短语分类（可选）
     * @return 短语列表
     */
    @GetMapping("/phrases")
    public Result<Map<String, List<String>>> getPhrases(
            @RequestParam(required = false) String category) {
        return Result.success(agileEnglishService.getPhrases(category));
    }

    /**
     * 评估用户的英语表达并提供改进建议
     *
     * @param request 评估请求
     * @return 评估结果和建议
     */
    @PostMapping("/evaluate-expression")
    @RateLimit(dimension = RateLimit.Dimension.IP, count = 15)
    public Result<AgileEnglishService.EvaluationResponse> evaluateExpression(
            @Valid @RequestBody AgileEnglishService.EvaluationRequest request) {
        return Result.success(agileEnglishService.evaluateExpression(request));
    }

    /**
     * 多轮对话 - AI扮演角色与用户实时对话
     */
    @PostMapping("/continue-conversation")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 30)
    public Result<AgileEnglishService.MultiTurnResponse> continueConversation(
            @Valid @RequestBody AgileEnglishService.MultiTurnRequest request) {
        return Result.success(agileEnglishService.continueConversation(request));
    }

    /**
     * 获取用户的练习历史
     */
    @GetMapping("/practice-history")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 10)
    public Result<AgileEnglishService.PracticeHistoryResponse> getPracticeHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(agileEnglishService.getPracticeHistory(page, size));
    }

    /**
     * 获取会话的详细练习记录
     */
    @GetMapping("/session/{sessionId}/records")
    public Result<List<AgileEnglishService.PracticeRecordDTO>> getSessionRecords(
            @PathVariable Long sessionId) {
        return Result.success(agileEnglishService.getSessionRecords(sessionId));
    }

    /**
     * 获取能力评估报告
     */
    @GetMapping("/capability-report")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 5)
    public Result<AgileEnglishService.CapabilityReport> getCapabilityReport() {
        return Result.success(agileEnglishService.getCapabilityReport());
    }
}
