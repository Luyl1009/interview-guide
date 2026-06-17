package interview.guide.modules.mistakenotebook.async;

import interview.guide.common.async.AbstractStreamConsumer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.mistakenotebook.service.QuestionGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.stream.StreamMessageId;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 错题本题目生成 Stream 消费者
 * 负责从 Redis Stream 消费消息并执行 AI 题目生成
 */
@Slf4j
@Component
public class QuestionGenerationConsumer extends AbstractStreamConsumer<QuestionGenerationConsumer.GenerationPayload> {

  private final QuestionGenerationService questionGenerationService;

  public QuestionGenerationConsumer(
    RedisService redisService,
    QuestionGenerationService questionGenerationService
  ) {
    super(redisService);
    this.questionGenerationService = questionGenerationService;
  }

  record GenerationPayload(String userId, Long resumeId, int count) {
  }

  @Override
  protected String taskDisplayName() {
    return "错题本题目生成";
  }

  @Override
  protected String streamKey() {
    return AsyncTaskStreamConstants.MISTAKE_NOTEBOOK_GENERATE_STREAM_KEY;
  }

  @Override
  protected String groupName() {
    return AsyncTaskStreamConstants.MISTAKE_NOTEBOOK_GENERATE_GROUP_NAME;
  }

  @Override
  protected String consumerPrefix() {
    return AsyncTaskStreamConstants.MISTAKE_NOTEBOOK_GENERATE_CONSUMER_PREFIX;
  }

  @Override
  protected String threadName() {
    return "mistake-notebook-generate-consumer";
  }

  @Override
  protected GenerationPayload parsePayload(StreamMessageId messageId, Map<String, String> data) {
    String userId = data.get(AsyncTaskStreamConstants.FIELD_USER_ID);
    String resumeIdStr = data.get(AsyncTaskStreamConstants.FIELD_RESUME_ID);
    String countStr = data.get(AsyncTaskStreamConstants.FIELD_GENERATE_COUNT);

    if (userId == null || countStr == null) {
      log.warn("消息格式错误，跳过: messageId={}", messageId);
      return null;
    }

    Long resumeId = null;
    if (resumeIdStr != null && !resumeIdStr.isEmpty()) {
      try {
        resumeId = Long.parseLong(resumeIdStr);
      } catch (NumberFormatException e) {
        log.warn("简历ID格式错误，使用最新简历: messageId={}, resumeIdStr={}", messageId, resumeIdStr);
      }
    }

    int count;
    try {
      count = Integer.parseInt(countStr);
    } catch (NumberFormatException e) {
      log.warn("生成数量格式错误，跳过: messageId={}, countStr={}", messageId, countStr);
      return null;
    }

    return new GenerationPayload(userId, resumeId, count);
  }

  @Override
  protected String payloadIdentifier(GenerationPayload payload) {
    return "userId=" + payload.userId() + ", resumeId=" + payload.resumeId() + ", count=" + payload.count();
  }

  @Override
  protected void markProcessing(GenerationPayload payload) {
    log.info("开始题目生成任务: {}", payloadIdentifier(payload));
  }

  @Override
  protected void processBusiness(GenerationPayload payload) {
    questionGenerationService.generateQuestionsFromResume(
      payload.userId(),
      payload.resumeId(),
      payload.count()
    );
    log.info("题目生成任务完成: {}", payloadIdentifier(payload));
  }

  @Override
  protected void markCompleted(GenerationPayload payload) {
    log.info("题目生成任务确认完成: userId={}", payload.userId());
  }

  @Override
  protected void markFailed(GenerationPayload payload, String error) {
    log.error("题目生成任务失败: userId={}, error={}", payload.userId(), error);
  }

  @Override
  protected void retryMessage(GenerationPayload payload, int retryCount) {
    log.warn("题目生成任务即将重试: userId={}, retryCount={}", payload.userId(), retryCount);
  }
}
