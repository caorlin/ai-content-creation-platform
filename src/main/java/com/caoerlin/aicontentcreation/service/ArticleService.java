package com.caoerlin.aicontentcreation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleQueryRequest;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.ArticleCreatePhaseEnum;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.model.vo.article.ArticleVO;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;

import java.util.List;

/**
 * @author zyj
 * @description 针对表【article(文章表)】的数据库操作Service
 */
public interface ArticleService extends IService<Article> {
    /**
     * 创建文章生成任务
     *
     * @param topic              文章选题
     * @param style              文章风格
     * @param enableImageMethods 文章配图方式
     * @param loginUser          操作用户信息
     * @return 任务taskId
     */
    String createArticleTask(String topic, String style, List<String> enableImageMethods, User loginUser);

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

    /**
     * 确认标题（用户选择后）
     *
     * @param taskId          任务ID
     * @param mainTitle       选中的主标题
     * @param subTitle        选中的副标题
     * @param userDescription 用户补充描述
     * @param loginUser       当前登录用户
     */
    void confirmTitle(String taskId, String mainTitle, String subTitle, String userDescription, User loginUser);

    /**
     * 确认大纲（用户编辑后）
     *
     * @param taskId    任务ID
     * @param outline   用户编辑后的大纲
     * @param loginUser 当前登录用户
     */
    void confirmOutline(String taskId, List<ArticleState.OutlineSection> outline, User loginUser);

    /**
     * 更新阶段
     *
     * @param taskId 任务ID
     * @param phase  阶段枚举
     */
    void updatePhase(String taskId, ArticleCreatePhaseEnum phase);

    /**
     * 保存标题方案
     *
     * @param taskId       任务ID
     * @param titleOptions 标题方案列表
     */
    void saveTitleOptions(String taskId, List<ArticleState.TitleOption> titleOptions);

    /**
     * AI 修改大纲
     *
     * @param taskId           任务ID
     * @param modifySuggestion 用户修改建议
     * @param loginUser        当前登录用户
     * @return 修改后的大纲
     */
    List<ArticleState.OutlineSection> aiModifyOutline(String taskId, String modifySuggestion, User loginUser);

    /**
     * 根据文章任务id获取文章信息
     * @param taskId 文章任务id
     */
    Article getArticleByTaskId(String taskId);

    /**
     * 参加文章任务并且检查配额
     * @param loginUser 登录用户
     * @return 任务id
     */
    String createArticleTaskWithCheckQuota(String topic, String style, List<String> enableImageMethods, User loginUser);
}
