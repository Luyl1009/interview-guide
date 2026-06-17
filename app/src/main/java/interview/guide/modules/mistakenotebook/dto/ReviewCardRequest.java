package interview.guide.modules.mistakenotebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 卡片复习请求
 */
public record ReviewCardRequest(

    /**
     * 掌握度评级：again / hard / good / easy
     */
    @NotBlank(message = "掌握度评级不能为空")
    String mastery
) {
}
