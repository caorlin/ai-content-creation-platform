package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.agent.ArticleCreationAgent;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.manager.SseEmitterManager;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.service.ArticleAsyncService;
import com.caoerlin.aicontentcreation.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.caoerlin.aicontentcreation.constant.ArticleConstant.*;

/**
 * 文章异步生成
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAsyncServiceImpl implements ArticleAsyncService {
    private final ArticleService articleService;
    private final ArticleCreationAgent articleCreationAgent;
    private final SseEmitterManager sseEmitterManager;


    @Override
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId, String topic, String style, List<String> enableImageMethods) {
        log.info("开始异步生成文章,taskId={},topic={}", taskId, topic);
        try {
            //更新文章状态为执行中
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);

            //参加文章状态
            ArticleState state = new ArticleState();
            state.setTopic(topic);
            state.setTaskId(taskId);
            state.setStyle(style);
            state.setEnabledImageMethods(enableImageMethods);

            //执行文章创建Agent，SSE实时推送
            articleCreationAgent.executeArticleGeneration(state, message -> {
                handleAgentMessage(taskId, message, state);
            });

            //保存完整文章内容
            articleService.saveArticleContent(taskId, state);

            //更新状态为已完成
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.COMPLETED, null);

            //推送已完成消息
            sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId", taskId));

            //完成
            sseEmitterManager.complete(taskId);

            log.info("文章生成异步任务执行完成,taskId={}", taskId);
        } catch (Exception e) {
            log.error("文章生成异步任务执行失败,taskId={}", taskId);

            //更新文章状态为失败
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            //推送失败消息
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            //完成
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * 发送SSE消息
     *
     * @param taskId            任务id
     * @param type              发送类型
     * @param additionalDataMap 附加消息
     */
    private void sendSseMessage(String taskId, SseMessageTypeEnum type, Map<String, String> additionalDataMap) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.putAll(additionalDataMap);
        sseEmitterManager.send(taskId, JSONUtil.toJsonStr(data));
    }

    /**
     * agent控制实时消息发送
     *
     * @param taskId  任务id
     * @param message 消息
     * @param state   当前文章状态
     */
    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        Map<String, Object> data = buildMessageData(message, state);
        if (CollectionUtil.isNotEmpty(data)) {
            //发送sse消息
            sseEmitterManager.send(taskId, JSONUtil.toJsonStr(data));
        }
    }

    /**
     * 构建消息数据
     *
     * @param message 消息
     * @param state   当前文章状态
     * @return
     */
    private Map<String, Object> buildMessageData(String message, ArticleState state) {
        //文章大纲流式消息前缀
        String articleOutlineStreamingPrefix = SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_STREAMING.getStreamingPrefix();
        //文章内容消息前缀
        String articleContentStreamingPrefix = SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_STREAMING.getStreamingPrefix();
        //图片检索完成前缀
        String imageCompleteStreamingPrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();

        //处理文章大纲消息
        if (message.startsWith(articleOutlineStreamingPrefix)) {
            return buildStreamingData(SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_STREAMING,
                    message.substring(articleOutlineStreamingPrefix.length()));
        }

        //处理文章内容消息
        if (message.startsWith(articleContentStreamingPrefix)) {
            return buildStreamingData(SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_STREAMING,
                    message.substring(articleContentStreamingPrefix.length()));
        }

        //处理图片检索消息
        if (message.startsWith(imageCompleteStreamingPrefix)) {
            String imageJsonStr = message.substring(imageCompleteStreamingPrefix.length());
            return buildImageCompleteData(imageJsonStr);
        }

        //处理完成消息
        return buildCompleteMessage(message, state);
    }


    /**
     * 任务完成后发送的消息数据
     *
     * @param message agent执行完成后消息
     * @param state   当前文章状态消息
     * @return
     */
    private Map<String, Object> buildCompleteMessage(String message, ArticleState state) {
        Map<String, Object> data = new HashMap<>();

        String titleAgentComplete = SseMessageTypeEnum.ARTICLE_TITLE_AGENT_COMPLETE.getValue();
        String outlineAgentAgentComplete = SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE.getValue();
        String contentAgentComplete = SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_COMPLETE.getValue();
        String imageRequirementsAgentComplete = SseMessageTypeEnum.ARTICLE_IMAGE_REQUIREMENTS_AGENT_COMPLETE.getValue();
        String imageGenerateAgentComplete = SseMessageTypeEnum.ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE.getValue();
        String mergeComplete = SseMessageTypeEnum.MERGE_COMPLETE.getValue();

        switch (message) {
            case ARTICLE_TITLE_AGENT_COMPLETE -> {
                data.put("type", titleAgentComplete);
                data.put("title", state.getTitle());
            }
            case ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE -> {
                data.put("type", outlineAgentAgentComplete);
                data.put("outline", state.getOutline());
            }
            case ARTICLE_CONTENT_AGENT_COMPLETE -> {
                data.put("type", contentAgentComplete);
                data.put("content", state.getContent());
            }
            case ARTICLE_IMAGE_REQUIREMENTS_AGENT_COMPLETE -> {
                data.put("type", imageRequirementsAgentComplete);
                data.put("imageRequirements", state.getImageRequirements());
            }
            case ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE -> {
                data.put("type", imageGenerateAgentComplete);
                data.put("images", state.getImages());
            }
            case MERGE_COMPLETE -> {
                data.put("type", mergeComplete);
                data.put("fullContent", state.getFullContent());
            }
            default -> {

            }
        }
        return data;
    }


    /**
     * 图片检索完成信息
     *
     * @param imageJsonStr 图片集合
     * @return
     */
    private Map<String, Object> buildImageCompleteData(String imageJsonStr) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
        data.put("image", JSONUtil.toBean(imageJsonStr, ArticleState.ImageResult.class));
        return data;
    }

    /**
     * 流水信息数据
     *
     * @param type    消息类型
     * @param content 消息内容
     * @return
     */
    private Map<String, Object> buildStreamingData(SseMessageTypeEnum type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("content", content);
        return data;
    }
}