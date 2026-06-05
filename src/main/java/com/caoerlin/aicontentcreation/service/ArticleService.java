package com.caoerlin.aicontentcreation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleQueryRequest;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.model.vo.article.ArticleVO;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;

/**
 * @author zyj
 * @description 针对表【article(文章表)】的数据库操作Service
 */
public interface ArticleService extends IService<Article> {
    /**
     * 创建文章生成任务
     *
     * @param topic     文章选题
     * @param style     文章风格
     * @param loginUser 操作用户信息
     * @return 任务taskId
     */
    String createArticleTask(String topic, String style, LoginUserVO loginUser);

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
     * @param state  状态
     */
    void saveArticleContent(String taskId, ArticleState state);

    /**
     * 获取文章详情
     *
     * @param taskId    任务id
     * @param loginUser 登录用户
     */
    ArticleVO getArticleDetail(String taskId, LoginUserVO loginUser);

    /**
     * 条件重新文章
     *
     * @param request   查询条件
     * @param loginUser 当前登录用户
     * @return
     */
    Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, LoginUserVO loginUser);

    /**
     * 查询对象转换为 QueryWrapper对象
     *
     * @param request 查询对象
     * @return
     */
    QueryWrapper<Article> getQueryWrapper(ArticleQueryRequest request);

    /**
     * 删除文章
     *
     * @param id        文章id
     * @param loginUser 登录用户
     * @return
     */
    boolean deleteArticle(Long id, LoginUserVO loginUser);
}
