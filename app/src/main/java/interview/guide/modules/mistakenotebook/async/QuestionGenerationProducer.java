package interview.guide.modules.mistakenotebook.async;

import interview.guide.common.async.AbstractStreamProducer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.infrastructure.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 错题本题目生成任务生产者
 * 负责发送题目生成任务到 Redis Stream
 */
@Slf4j
@Component
public class QuestionGenerationProducer extends AbstractStreamProducer<QuestionGenerationProducer.GenerationTaskPayload> {

  public QuestionGenerationProducer(RedisService redisService) {
    super(redisService);
  }

  record GenerationTaskPayload(String userId, Long resumeId, int count) {
  }

  /**
   * 发送题目生成任务到 Redis Stream
   *
   * @param userId   用户ID
   * @param resumeId 简历ID（可为null，表示使用用户最新简历）
   * @param count    生成数量
   */
  public void sendGenerateTask(String userId, Long resumeId, int count) {
    sendTask(new GenerationTaskPayload(userId, resumeId, count));
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
  protected Map<String, String> buildMessage(GenerationTaskPayload payload) {
    return Map.of(
      AsyncTaskStreamConstants.FIELD_USER_ID, payload.userId(),
      AsyncTaskStreamConstants.FIELD_RESUME_ID, payload.resumeId() != null ? payload.resumeId().toString() : "",
      AsyncTaskStreamConstants.FIELD_GENERATE_COUNT, String.valueOf(payload.count()),
      AsyncTaskStreamConstants.FIELD_RETRY_COUNT, "0"
    );
  }

  @Override
  protected String payloadIdentifier(GenerationTaskPayload payload) {
    return "userId=" + payload.userId() + ", resumeId=" + payload.resumeId() + ", count=" + payload.count();
  }

  @Override
  protected void onSendFailed(GenerationTaskPayload payload, String error) {
    log.error("题目生成任务入队失败: userId={}, error={}", payload.userId(), error);
  }
}
