package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.caoerlin.aicontentcreation.model.enums.ArticleStatusEnum;
import com.caoerlin.aicontentcreation.service.ArticleService;
import com.caoerlin.aicontentcreation.mapper.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zyj
 * @description 针对表【article(文章表)】的数据库操作Service实现
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {

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
        updateById(article);
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
        article.setContent(content);
        updateById(article);
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




