package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;

import java.util.function.Consumer;

public interface ArticleAgentService {
    /**
     * 阶段1：执行文章标题生成（文章可选标题列表生成阶段）
     *
     * @param state         文章状态
     * @param streamHandler 模型执行状态流转
     */
    void executeArticleTitleGeneratePhage(ArticleState state, Consumer<String> streamHandler);

    /**
     * 阶段2： 文章大纲生成
     *
     * @param state         文章状态
     * @param streamHandler 模型执行状态流转
     */
    void executeArticleOutlineGenerate(ArticleState state, Consumer<String> streamHandler);

    /**
     * 阶段3：生成文章正文和配图
     *
     * @param state         文章状态
     * @param streamHandler 模型执行状态流转
     */
    void executeArticleContentAndImage(ArticleState state, Consumer<String> streamHandler);
}
