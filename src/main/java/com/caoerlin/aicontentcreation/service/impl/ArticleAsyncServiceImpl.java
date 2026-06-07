package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.caoerlin.aicontentcreation.ai.agent.ArticleCreationAgent;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.manager.SseEmitterManager;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.caoerlin.aicontentcreation.model.enums.ArticleCreatePhaseEnum;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.service.ArticleAgentService;
import com.caoerlin.aicontentcreation.service.ArticleAsyncService;
import com.caoerlin.aicontentcreation.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private final ArticleAgentService articleAgentService;


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

    @Override
    @Async("articleExecutor")
    public void executeArticleTitleGeneratePhage(String taskId, String topic, String style) {
        log.info("开始进入异步生成文章标题阶段,taskId={},topic={},stlye={}", taskId, topic, style);

        try {
            //更新文章状态
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, "");
            articleService.updatePhase(taskId, ArticleCreatePhaseEnum.TITLE_GENERATING);

            //创建文章状态对象
            ArticleState state = new ArticleState();
            state.setTopic(topic);
            state.setStyle(style);
            state.setTaskId(taskId);

            //执行文章标题生成Agent
            articleAgentService.executeArticleTitleGeneratePhage(state, message -> {
                handleAgentMessage(taskId, message, state);
            });

            //保存文章标题到数据库
            articleService.saveTitleOptions(taskId, state.getTitleOptions());

            //更新文章阶段状态，标题选择状态
            articleService.updatePhase(taskId, ArticleCreatePhaseEnum.TITLE_SELECTING);

            //推送标题生成完成消息
            Map<String, Object> data = new HashMap<>();
            data.put("titleOptions", state.getTitleOptions());
            sendSseMessage(taskId, SseMessageTypeEnum.ARTICLE_TITLE_GENERATED, data);

            log.info("文章标题列表生成完成,taskId={},titleOptionsSize={}", taskId, state.getTitleOptions().size());
        } catch (Exception e) {
            log.error("异步生成文章标题阶段执行失败,taskId={},e={}", taskId, e.getMessage());

            //更新文章状态为失败
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            //推送失败消息
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            //完成
            sseEmitterManager.complete(taskId);
        }
    }

    @Override
    @Async("articleExecutor")
    public void executeArticleOutlineGeneratePhage(String taskId) {
        log.info("文章大纲生成阶段：开始生成文章大纲,taskId={}", taskId);

        if (StringUtils.isBlank(taskId)) {
            log.error("文章任务id不能为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章任务id为空");
        }

        try {
            Article article = articleService.getArticleByTaskId(taskId);
            if (Objects.isNull(article)) {
                log.error("文章信息不存在,taskId={}", taskId);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
            }

            //更新文章生成阶段状态
            articleService.updatePhase(taskId, ArticleCreatePhaseEnum.OUTLINE_GENERATING);

            //设置文章主状态
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setStyle(article.getStyle());
            state.setUserDescription(article.getUserDescription());

            //设置文章标题
            ArticleState.TitleResult titleResult = new ArticleState.TitleResult();
            titleResult.setMainTitle(article.getMainTitle());
            titleResult.setSubTitle(article.getSubTitle());
            state.setTitle(titleResult);

            articleAgentService.executeArticleOutlineGenerate(state, message -> {
                handleAgentMessage(taskId, message, state);
            });

            //保存文章大纲
            Article updateArticle = articleService.getArticleByTaskId(taskId);
            updateArticle.setOutline(JSONUtil.toJsonStr(state.getOutline().getSections()));
            articleService.updateById(updateArticle);

            //更新文章阶段状态,文章大纲编辑中
            articleService.updatePhase(taskId, ArticleCreatePhaseEnum.OUTLINE_EDITING);

            //推送大纲生成完成
            Map<String, Object> data = new HashMap<>();
            data.put("outline", state.getOutline().getSections());
            sendSseMessage(taskId, SseMessageTypeEnum.ARTICLE_OUTLINE_GENERATED, data);

            log.info("文章大纲生成完成,taskId={},outlineSectionSize={}", taskId, state.getOutline().getSections().size());
        } catch (BusinessException e) {
            log.error("文章大纲生成失败,taskId={},e={}", taskId, e.getMessage());

            //更新文章状态为失败
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());

            //推送失败消息
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));

            //完成
            sseEmitterManager.complete(taskId);
        }
    }

    @Override
    @Async("articleExecutor")
    public void executeArticleContentGeneratePhage(String taskId) {
        log.info("开始执行文章正文生成阶段,taskId={}", taskId);

        try {
            //获取文章信息
            Article article = articleService.getArticleByTaskId(taskId);
            if (Objects.isNull(article)) {
                log.info("文章不存在,taskId={}", taskId);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
            }

            //设置文章状态
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setStyle(article.getStyle());

            //获取文章配图方案
            String imageMethods = article.getEnabledImageMethods();
            //设置文章配图方案
            List<String> imageMethodList = new ArrayList<>();
            if (StrUtil.isNotBlank(imageMethods)) {
                imageMethodList = JSONUtil.toList(imageMethods, String.class);
            }
            state.setEnabledImageMethods(imageMethodList);

            //设置文章标题
            ArticleState.TitleResult titleResult = new ArticleState.TitleResult();
            titleResult.setMainTitle(article.getMainTitle());
            titleResult.setSubTitle(article.getSubTitle());
            state.setTitle(titleResult);

            //设置文章大纲
            ArticleState.OutlineResult outlineResult = new ArticleState.OutlineResult();
            outlineResult.setSections(JSONUtil.toList(article.getOutline(), ArticleState.OutlineSection.class));
            state.setOutline(outlineResult);

            //生成文章正文和配图
            articleAgentService.executeArticleContentAndImage(state, message -> {
                handleAgentMessage(taskId, message, state);
            });

            //保存文章正文到数据库
            articleService.saveArticleContent(taskId, state);

            //更新文章状态为已完成
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.COMPLETED, "");

            //发送完成消息
            sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId", taskId));

            //完成sse连接
            sseEmitterManager.complete(taskId);
        } catch (BusinessException e) {
            log.error("生成文章正文和配图失败,taskId={},e={}", taskId, e.getMessage());

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
    private void sendSseMessage(String taskId, SseMessageTypeEnum type, Map<String, Object> additionalDataMap) {
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