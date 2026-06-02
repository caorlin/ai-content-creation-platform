package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;

/**
 * @author zyj
 * @description 针对表【article(文章表)】的数据库操作Service
 */
public interface ArticleService extends IService<Article> {

    /**
     * 更新文章状态
     *
     * @param taskId            任务id
     * @param articleStatusEnum 更新后的文章类型
     * @param errorMessage      错误消息
     */
    void updateArticleStatus(String taskId, ArticleStatusEnum articleStatusEnum, String errorMessage);

    /**
     * 保存文章内容
     *
     * @param taskId 任务id
     * @param state 状态
     */
    void saveArticleContent(String taskId, ArticleState state);
}
