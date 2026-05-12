package interview.guide.modules.interview.skill;

import interview.guide.modules.interview.model.DailyQuoteDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 每日一句服务
 */
@Service
public class DailyQuoteService {

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

    /**
     * 获取随机名言
     *
     * @return 随机返回一条名言
     */
    public DailyQuoteDTO getRandomQuote() {
        Random random = new Random();
        return DAILY_QUOTES.get(random.nextInt(DAILY_QUOTES.size()));
    }
}