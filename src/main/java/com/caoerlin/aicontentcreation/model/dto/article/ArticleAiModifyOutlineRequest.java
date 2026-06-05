package com.caoerlin.aicontentcreation.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "AI 修改大纲数据模型")
public class ArticleAiModifyOutlineRequest implements Serializable {
    @Schema(name = "taskId", description = "任务id")
    private String taskId;

    @Schema(name = "modifySuggestion", description = "用户的修改建议")
    private String modifySuggestion;

    @Serial
    private static final long serialVersionUID = 1L;
}
