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
}
