package com.caoerlin.aicontentcreation.service.impl;

import com.caoerlin.aicontentcreation.ai.agent.*;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.service.ArticleAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAgentServiceImpl implements ArticleAgentService {
    private final ArcticTitleAgent arcticTitleAgent;
    private final ArticleOutlineAgent articleOutlineAgent;
    private final ArticleContentAgent articleContentAgent;
    private final ArticleImageRequirementsAgent articleImageRequirementsAgent;
    private final ArticleImageGenerateAgent articleImageGenerateAgent;
    private final ArticleMergeAgent articleMergeAgent;

    @Override
    public void executeArticleTitleGeneratePhage(ArticleState state, Consumer<String> streamHandler) {
        try {
            log.info("文章标题方案生成阶段,开始生成文章可选标题列表,taskId={},topic={},style={}", state.getTaskId(), state.getTopic(), state.getStyle());
            //开始生成文章标题
            arcticTitleAgent.generateArticleTitle(state);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_TITLE_AGENT_COMPLETE.getValue());
            log.info("文章可选标题列表生成完成,taskId={},titleOptionSize={}", state.getTaskId(), state.getTitleOptions().size());
        } catch (Exception e) {
            log.error("文章可选标题列表生成失败,taskId={},e={}", state.getTaskId(), e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文章标题方案生成失败");
        }
    }

    @Override
    public void executeArticleOutlineGenerate(ArticleState state, Consumer<String> streamHandler) {
        try {
            log.info("文章大纲生成阶段，开始生成文章大纲,taskId={},style={}", state.getTaskId(), state.getStyle());
            articleOutlineAgent.generateArticleOutline(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE.getValue());
            log.info("文章大纲生成阶段,文章大纲生成完成,taskId={},outlineSectionSize={}", state.getTaskId(), state.getOutline().getSections().size());
        } catch (Exception e) {
            log.error("文章大纲生成阶段,文章大纲生成失败,taskId={},e={}", state.getTaskId(), e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文章大纲生成失败");
        }
    }

    @Override
    public void executeArticleContentAndImage(ArticleState state, Consumer<String> streamHandler) {
        try {
            log.info("文章正文和配图阶段,开始生成文章正文和配图,taskId={},style={},imageMethod={}", state.getTaskId(), state.getStyle(), state.getEnabledImageMethods());

            articleContentAgent.generateArticleContent(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_COMPLETE.getValue());
            log.info("文章内容生成完毕,开始调用  ArticleImageRequirementsAgent 分析文章需要的配图");

            articleImageRequirementsAgent.analyzeArticleIllustration(state);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_IMAGE_REQUIREMENTS_AGENT_COMPLETE.getValue());
            log.info("分析文章配图完毕,开始调用  ArticleImageGenerateAgent 生成配图");

            articleImageGenerateAgent.generateArticleImage(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE.getValue());
            log.info("文章配图生成完毕,开始调用  ArticleMergeAgent 合并图文");

            articleMergeAgent.mergeArticleAndImage(state);
            streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            log.info("ArticleMergeAgent 执行完成,图文合并完成");
        } catch (Exception e) {
            log.error("文章正文和配图阶段，生成文章正文和配图失败,taskId={},e={}", state.getTaskId(), e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成文章正文和配图失败");
        }
    }
}
