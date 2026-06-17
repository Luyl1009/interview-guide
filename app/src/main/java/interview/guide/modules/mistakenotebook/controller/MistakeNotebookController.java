package interview.guide.modules.mistakenotebook.controller;

import interview.guide.common.annotation.RateLimit;
import interview.guide.common.result.Result;
import interview.guide.modules.mistakenotebook.dto.QuestionCardDTO;
import interview.guide.modules.mistakenotebook.dto.QuestionFilterRequest;
import interview.guide.modules.mistakenotebook.dto.ReviewCardRequest;
import interview.guide.modules.mistakenotebook.dto.RiskAssessmentRequest;
import interview.guide.modules.mistakenotebook.dto.RiskAssessmentResponse;
import interview.guide.modules.mistakenotebook.service.MistakeNotebookService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题本 Controller
 * 提供面试题生成、卡片复习、风险预测等API
 */
@Slf4j
@RestController
@RequestMapping("/api/mistake-notebook")
public class MistakeNotebookController {

    private final MistakeNotebookService mistakeNotebookService;

    public MistakeNotebookController(MistakeNotebookService mistakeNotebookService) {
        this.mistakeNotebookService = mistakeNotebookService;
    }

    /**
     * 基于简历生成题库
     */
    @PostMapping("/generate-from-resume")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 5)
    public Result<Void> generateFromResume(@RequestParam(required = false, defaultValue = "default") String userId) {
        mistakeNotebookService.generateFromResume(userId);
        return Result.success();
    }

    /**
     * 获取下一张待复习卡片
     */
    @GetMapping("/cards/next-review")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 30)
    public Result<QuestionCardDTO> getNextReviewCard(@RequestParam(required = false, defaultValue = "default") String userId) {
        return Result.success(mistakeNotebookService.getNextReviewCard(userId));
    }

    /**
     * 提交卡片复习结果
     */
    @PostMapping("/cards/{id}/review")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 30)
    public Result<Void> submitCardReview(
        @RequestParam(required = false, defaultValue = "default") String userId,
        @PathVariable Long id,
        @Valid @RequestBody ReviewCardRequest request
    ) {
        mistakeNotebookService.submitCardReview(userId, id, request.mastery());
        return Result.success();
    }

    /**
     * 多维度筛选题目
     */
    @GetMapping("/questions")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 20)
    public Result<Page<QuestionCardDTO>> filterQuestions(
        @RequestParam(required = false, defaultValue = "default") String userId,
        @Valid QuestionFilterRequest request
    ) {
        return Result.success(mistakeNotebookService.filterQuestions(userId, request));
    }

    /**
     * 获取用户技能点列表
     */
    @GetMapping("/skills")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 10)
    public Result<List<String>> getUserSkills(@RequestParam(required = false, defaultValue = "default") String userId) {
        return Result.success(mistakeNotebookService.getUserSkills(userId));
    }

    /**
     * 生成面试风险评估
     */
    @PostMapping("/risk-assessment")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 3)
    public Result<RiskAssessmentResponse> generateRiskAssessment(
        @RequestParam(required = false, defaultValue = "default") String userId,
        @Valid @RequestBody RiskAssessmentRequest request
    ) {
        return Result.success(mistakeNotebookService.generateRiskAssessment(userId, request));
    }

    /**
     * 获取最新风险评估
     */
    @GetMapping("/risk-assessment/latest")
    @RateLimit(dimension = RateLimit.Dimension.USER, count = 10)
    public Result<RiskAssessmentResponse> getLatestRiskAssessment(@RequestParam(required = false, defaultValue = "default") String userId) {
        return Result.success(mistakeNotebookService.getLatestRiskAssessment(userId));
    }
}
