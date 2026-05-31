package com.caoerlin.aicontentcreation.ai.agent;

import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;

import java.util.function.Consumer;

/**
 * 文章生成agent
 *
 * @author zyj
 */
public interface BaseArticleCreationAgent {

    /**
     * 文章生成
     *
     * @param articleState  文章状态
     * @param streamHandler 模型执行状态流转
     */
    void executeArticleGeneration(ArticleState articleState, Consumer<String> streamHandler);
}
