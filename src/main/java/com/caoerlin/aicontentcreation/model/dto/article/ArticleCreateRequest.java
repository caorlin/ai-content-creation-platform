package com.caoerlin.aicontentcreation.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author zyj
 */
@Data
@Schema(description = "文章创建模型")
public class ArticleCreateRequest {

    @Schema(name = "topic", description = "文章选题")
    private String topic;

    @Schema(name = "style", description = "文章风格", examples = {"tech", "emotional", "educational", "humorous"})
    private String style;

    @Schema(name = "enabledImageMethods", description = "文章配图生成方式", examples = {"PEXELS", "NANO_BANANA", "MERMAID", "ICONIFY", "EMOJI_PACK", "SVG_DIAGRAM"})
    private List<String> enabledImageMethods;
}
