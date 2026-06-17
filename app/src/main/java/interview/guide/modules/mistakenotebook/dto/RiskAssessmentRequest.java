package interview.guide.modules.mistakenotebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 面试风险评估请求
 */
public record RiskAssessmentRequest(

    /**
     * 面试时间
     */
    @NotNull(message = "面试时间不能为空")
    LocalDateTime interviewDate,

    /**
     * 岗位JD描述
     */
    @NotBlank(message = "岗位JD不能为空")
    String jobDescription
) {
}
