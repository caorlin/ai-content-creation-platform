package com.caoerlin.aicontentcreation.service;

import java.util.List;

/**
 * @author zyj
 */
public interface ArticleAsyncService {

    /**
     * 异步文章生成
     *
     * @param taskId 任务Id
     * @param style  文章风格
     * @param topic  选题
     */
    void executeArticleGeneration(String taskId, String topic, String style, List<String> enableImageMethods);
}
