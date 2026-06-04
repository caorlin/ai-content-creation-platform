package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleQueryRequest;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.model.vo.article.ArticleVO;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.service.ArticleService;
import com.caoerlin.aicontentcreation.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author zyj
 * @description 针对表【article(文章表)】的数据库操作Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {
    private final ArticleMapper articleMapper;

    @Override
    public String createArticleTask(String topic, LoginUserVO loginUser) {
        log.info("开始执行创建文章任务接口");

        if (StrUtil.isBlank(topic)) {
            log.error("创建文章任务接口,文章选题不能为空,userAccount={}", loginUser.getUserAccount());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入选题");
        }

        //生成文章任务id
        String taskId = IdUtil.simpleUUID();

        Article article = new Article();
        article.setTaskId(taskId);
        article.setUserId(loginUser.getId());
        article.setTopic(topic);

        log.info("创建文章任务接口,创建文章开始保存文章任务,taskId={},userAccount={}", taskId, loginUser.getUserAccount());
        boolean result = save(article);
        if (!result) {
            log.error("创建文章任务接口,创建文章任务失败,获取SQL执行结果错误,userAccount={}", loginUser.getUserAccount());
        }

        log.info("创建文章任务接口,创建文章任务完成,taskId={},userAccount={}", taskId, loginUser.getUserAccount());

        return taskId;
    }

    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum articleStatusEnum, String errorMessage) {
        log.info("开始执行更新文章状态接口");
        //判断文章状态是否合法
        ArticleStatusEnum statusEnum = ArticleStatusEnum.getArticleStatusEnumByStatus(articleStatusEnum.getStatus());
        if (ObjectUtil.isNull(statusEnum)) {
            log.error("文章状态不存在,status={}", JSONUtil.toJsonStr(statusEnum));
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章状态不存在");
        }

        //根据任务id获取文章信息
        Article article = getArticleByTaskId(taskId);

        if (ObjectUtil.isNull(article)) {
            log.warn("当前任务不存在");
            return;
        }

        //设置状态
        article.setStatus(articleStatusEnum.getStatus());
        if (StrUtil.isNotBlank(errorMessage)) {
            article.setErrorMessage(errorMessage);
        }
        //更新文章状态
        boolean result = updateById(article);
        if (!result) {
            log.error("更新文章状态失败,获取SQL执行结果错误,taskId={}", taskId);
        }
        log.info("更新文章状态成功,taskId={}", taskId);
    }

    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        log.info("开始保存文章内容");

        if (ObjectUtil.isNull(state) || StrUtil.isBlank(state.getContent())) {
            log.warn("文章内容不能为空");
            return;
        }
        //根据任务id获取文章信息
        Article article = getArticleByTaskId(taskId);
        if (ObjectUtil.isNull(article)) {
            log.error("文章不存在,taskId={}", taskId);
            return;
        }
        //保存文章内容
        String content = state.getContent();
        article.setMainTitle(state.getTitle().getMainTitle());
        article.setSubTitle(state.getTitle().getSubTitle());
        article.setOutline(JSONUtil.toJsonStr(state.getOutline().getSections()));
        article.setContent(content);
        article.setFullContent(state.getFullContent());

        //提前文章封面
        List<ArticleState.ImageResult> images = state.getImages();
        if (CollectionUtil.isNotEmpty(images)) {
            //position=1 为封面图
            ArticleState.ImageResult cover = images.stream()
                    .filter(img ->
                            ObjectUtil.isNotNull(img) && img.getPosition() == 1
                    )
                    .findFirst()
                    .orElse(null);
            if (ObjectUtil.isNotNull(cover)) {
                article.setCoverImage(cover.getUrl());
            }
        }
        article.setImages(JSONUtil.toJsonStr(images));
        article.setCompletedTime(LocalDateTime.now());

        boolean result = updateById(article);
        if (!result) {
            log.error("保存文章信息失败,获取SQL执行结果错误,taskId={}", taskId);
        }
        log.info("保存文章信息成功,taskId={}", taskId);
    }

    @Override
    public ArticleVO getArticleDetail(String taskId, LoginUserVO loginUser) {
        //根据文章任务id获取文章信息
        Article article = getArticleByTaskId(taskId);
        if (ObjectUtil.isNull(article)) {
            log.error("文章不存在,userAccount={}", loginUser.getUserAccount());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }
        //判断用户是否有权限操作
        Long userId = loginUser.getId();
        if (!ObjectUtil.equal(userId, article.getUserId())) {
            log.error("用户：{},无权限查看文章,articleId={}", loginUser.getUserAccount(), article.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权限查看");
        }

        //
        ArticleVO articleVO = new ArticleVO();
        BeanUtil.copyProperties(article, articleVO);
        return articleVO;
    }

    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, LoginUserVO loginUser) {
        int pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();

        QueryWrapper<Article> wrapper = getQueryWrapper(request);

        //非管理员用户只能查看自己的文章
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            wrapper.eq("user_id", loginUser.getId());
        } else if (ObjectUtil.isNotNull(request.getUserId())) {
            wrapper.eq("user_id", request.getUserId());
        }

        //分页查询
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        //类型转换
        List<Article> records = articlePage.getRecords();
        List<ArticleVO> result = records.stream().map(article -> {
            ArticleVO articleVO = new ArticleVO();
            BeanUtil.copyProperties(article, articleVO);
            return articleVO;
        }).toList();

        Page<ArticleVO> articleVOPage = new Page<>();
        articleVOPage.setRecords(result);
        articleVOPage.setTotal(articlePage.getTotal());
        articleVOPage.setCurrent(articlePage.getCurrent());
        articleVOPage.setSize(articlePage.getSize());
        return articleVOPage;
    }

    @Override
    public QueryWrapper<Article> getQueryWrapper(ArticleQueryRequest request) {
        if (ObjectUtil.isNull(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String status = request.getStatus();
        Long userId = request.getUserId();
        String sortOrder = request.getSortOrder();
        String sortField = request.getSortField();

        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        return wrapper.eq(StrUtil.isNotBlank(status), "status", status)
                .eq(ObjectUtil.isNotNull(userId), "user_id", userId)
                .orderBy(StrUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
    }

    @Override
    public boolean deleteArticle(Long id, LoginUserVO loginUser) {
        if (ObjectUtil.isNull(id)) {
            log.error("文章id不能为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章id不能为空");
        }


        //判断文章是否存在
        Article article = getById(id);
        if (ObjectUtil.isNull(article)) {
            log.error("文章不存在");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }

        //非管理员用户只能删除自己的文章
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) && !article.getUserId().equals(loginUser.getId())) {
            log.error("无法删除除自己创建的其他文章,userAccount={},文章id={}", loginUser.getUserAccount(), id);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无权限删除");
        }

        //逻辑删除
        boolean result = removeById(article.getId());
        if (!result) {
            log.error("删除文章失败,获取SQL执行结果错误,文章id：{}", id);
        }
        log.error("删除文章成功,文章id：{}", id);
        return result;
    }

    private Article getArticleByTaskId(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            log.error("文章任务id为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不能为空");
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTaskId, taskId);
        return getOne(wrapper);
    }
}




