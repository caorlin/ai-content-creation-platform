package com.caoerlin.aicontentcreation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.model.entity.Article;
import com.caoerlin.aicontentcreation.service.ArticleService;
import com.caoerlin.aicontentcreation.mapper.ArticleMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【article(文章表)】的数据库操作Service实现
* @createDate 2026-05-31 15:35:52
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService{

}




