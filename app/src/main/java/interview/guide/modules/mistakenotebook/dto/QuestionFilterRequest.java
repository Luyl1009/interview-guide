package interview.guide.modules.mistakenotebook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 题目筛选请求
 */
public record QuestionFilterRequest(

    /**
     * 技能点（可选）
     */
    String skillPoint,

    /**
     * 难度 1-5（可选）
     */
    @Min(value = 1, message = "难度最低为1")
    @Max(value = 5, message = "难度最高为5")
    Integer difficulty,

    /**
     * 来源类型：AI_GENERATED / MANUAL / INTERVIEW_MISTAKE（可选）
     */
    String sourceType,

    /**
     * 最小掌握度 0-100（可选）
     */
    @Min(value = 0, message = "掌握度最低为0")
    @Max(value = 100, message = "掌握度最高为100")
    Double minMastery,

    /**
     * 最大掌握度 0-100（可选）
     */
    @Min(value = 0, message = "掌握度最低为0")
    @Max(value = 100, message = "掌握度最高为100")
    Double maxMastery,

    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能为负数")
    Integer page,

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页至少1条")
    @Max(value = 100, message = "每页最多100条")
    Integer size
) {
    public QuestionFilterRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
