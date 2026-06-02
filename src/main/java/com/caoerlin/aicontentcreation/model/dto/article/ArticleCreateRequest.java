package com.caoerlin.aicontentcreation.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zyj
 */
@Data
@Schema(description = "文章创建模型")
public class ArticleCreateRequest {

    @Schema(name = "topic", description = "文章选题")
    private String topic;
}
