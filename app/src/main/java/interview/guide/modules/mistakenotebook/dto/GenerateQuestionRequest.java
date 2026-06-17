package interview.guide.modules.mistakenotebook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 生成题目请求
 * 基于简历生成个性化面试题
 */
public record GenerateQuestionRequest(

    /**
     * 目标技能点（可选，不指定则根据简历自动提取）
     */
    String skillPoint,

    /**
     * 题目类型：CONCEPT / SCENARIO / CODING / COMPARISON
     */
    @NotBlank(message = "题目类型不能为空")
    String questionType,

    /**
     * 难度等级 1-5
     */
    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度最低为1")
    @Max(value = 5, message = "难度最高为5")
    Integer difficulty,

    /**
     * 生成数量
     */
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "至少生成1道题")
    @Max(value = 50, message = "最多生成50道题")
    Integer count
) {
}
