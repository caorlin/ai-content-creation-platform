package com.caoerlin.aicontentcreation.ai.agent;

import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 文章创作agent
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public abstract class ArticleCreationAgent implements BaseArticleCreationAgent {
    private final ArcticTitleAgent arcticTitleAgent;

    /**
     * 生成文章
     *
     * @param state         文章状态
     * @param streamHandler 模型执行状态流转
     */
    @Override
    public void executeArticleGeneration(ArticleState state, Consumer<String> streamHandler) {
        log.info("开始执行 ArcticCreationAgent");

        //调用文章标题agent生成文章标题
        log.info("调用 ArcticTitleAgent：开始生成文章标题,taskId:{}", state.getTaskId());
        arcticTitleAgent.generateArticleTitle(state);
        streamHandler.accept(SseMessageTypeEnum.ARTICLE_TITLE_AGENT_COMPLETE.getValue());

        log.info("文章标题生成完毕,开始调用 ArticleOutlineAgent 生成文章大纲");

        //生成文章标题完成，开始生成文章大纲(流式输出)
        log.info("调用 ArticleOutlineAgent：开始生成文章大纲,taskId:{}", state.getTaskId());
        generateArticleOutline(state, streamHandler);
        streamHandler.accept(SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE.getValue());

        log.info("文章大纲生成完毕,开始调用 ArticleContentAgent 生成文章内容");

        //文章大纲生成完毕，开始生成文章内容(流式输出)
        log.info("调用 ArticleContentAgent：开始生成文章内容,taskId:{}", state.getTaskId());
        generateArticleContent(state, streamHandler);
        streamHandler.accept(SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_COMPLETE.getValue());

        log.info("文章内容生成完毕,开始调用  ArticleIllustrationAnalysisAgent 分析文章需要的配图");

        //开始分析文章所须配图
        log.info("调用 ArticleIllustrationAnalysisAgent：开始分析文章配图,taskId:{}", state.getTaskId());
        analyzeArticleIllustration(state);
        streamHandler.accept(SseMessageTypeEnum.ARTICLE_ILLUSTRATION_ANALYSIS_AGENT_COMPLETE.getValue());

        log.info("分析文章配图完毕,开始调用  ArticleImageGenerateAgent 生成配图");

        //开始生成文章配图
        log.info("调用 ArticleImageGenerateAgent：开始生成配图,taskId:{}", state.getTaskId());
        generateArticleImage(state, streamHandler);
        streamHandler.accept(SseMessageTypeEnum.ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE.getValue());

        log.info("文章配图生成完毕,开始调用  ArticleMergeAgent 合并图文");

        //开始合并图文
        log.info("调用 ArticleMergeAgent ：开始合并图文,taskId:{}", state.getTaskId());
        mergeArticleAndImage(state);
        streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());
    }

    /**
     * todo 先用抽象方法，等后续真实现
     */
    protected abstract void generateArticleOutline(ArticleState articleState, Consumer<String> consumer);

    protected abstract void generateArticleContent(ArticleState state, Consumer<String> streamHandler);

    protected abstract void analyzeArticleIllustration(ArticleState state);

    protected abstract void generateArticleImage(ArticleState state, Consumer<String> streamHandler);

    protected abstract void mergeArticleAndImage(ArticleState state);
}
