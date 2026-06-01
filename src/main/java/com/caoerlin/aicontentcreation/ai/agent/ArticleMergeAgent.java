package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArticleMergeAgent {
    /**
     * 图文合成：将配图插入正文对应位置
     */
    public void mergeArticleAndImage(ArticleState state) {
        String content = state.getContent();
        List<ArticleState.ImageResult> images = state.getImages();

        if (images == null || images.isEmpty()) {
            state.setFullContent(content);
            return;
        }

        StringBuilder fullContent = new StringBuilder();

        // 按行处理正文，在章节标题后插入对应图片
        String[] lines = content.split("\n");
        for (String line : lines) {
            fullContent.append(line).append("\n");

            // 检查是否是章节标题（以 ## 开头）
            if (line.startsWith("## ")) {
                String sectionTitle = line.substring(3).trim();
                insertImageAfterSection(fullContent, images, sectionTitle);
            }
        }

        state.setFullContent(fullContent.toString());
        log.info("图文合成完成, fullContentLength={}", fullContent.length());
    }

    /**
     * 文章标题后面插入对于图片
     *
     * @param fullContent
     * @param images
     * @param sectionTitle
     */
    private void insertImageAfterSection(StringBuilder fullContent,
                                         List<ArticleState.ImageResult> images,
                                         String sectionTitle) {
        for (ArticleState.ImageResult image : images) {
            if (image.getPosition() > 1 &&
                    StrUtil.isNotBlank(image.getSectionTitle()) &&
                    sectionTitle.contains(image.getSectionTitle().trim())) {
                fullContent.append("\n![").append(image.getDescription())
                        .append("](").append(image.getUrl()).append(")\n");
            }
        }

    }

}
