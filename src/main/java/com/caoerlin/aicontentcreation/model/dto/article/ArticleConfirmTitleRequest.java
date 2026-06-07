package com.caoerlin.aicontentcreation.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "文章标题选择数据模型")
public class ArticleConfirmTitleRequest implements Serializable {
    @Schema(name = "taskId", description = "任务id")
    private String taskId;

    @Schema(name = "selectedMainTitle", description = "用户挑选的主标题")
    private String selectedMainTitle;

    @Schema(name = "selectedSubTitle", description = "用户挑选的副标题")
    private String selectedSubTitle;

    @Schema(name = "userDescription", description = "用户追加的标题描述（可选）")
    private String userDescription;


    @Serial
    private static final long serialVersionUID = 1L;
}
