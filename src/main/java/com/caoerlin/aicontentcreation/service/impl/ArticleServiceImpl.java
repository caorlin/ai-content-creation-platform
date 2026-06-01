package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
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
            log.error("开始执行更新文章状态接口,文章状态不存在,status={}", JSONUtil.toJsonStr(statusEnum));
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章状态不存在");
        }

        //todo 未完成
    }
}




