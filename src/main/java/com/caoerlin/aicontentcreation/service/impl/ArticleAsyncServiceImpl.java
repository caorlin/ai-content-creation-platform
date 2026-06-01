package com.caoerlin.aicontentcreation.service.impl;

import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.service.ArticleAsyncService;
import com.caoerlin.aicontentcreation.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAsyncServiceImpl implements ArticleAsyncService {
    private final ArticleService articleService;

    /**
     * 异步文章生成
     *
     * @param taskId 任务Id
     * @param topic  选题
     */
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId, String topic) {
        log.info("开始异步生成文章,taskId={},topic={}", taskId, topic);

        //更新文章状态为执行中
        articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);
    }
}
