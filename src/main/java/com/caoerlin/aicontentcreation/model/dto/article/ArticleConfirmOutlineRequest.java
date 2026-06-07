package com.caoerlin.aicontentcreation.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "用户提交编辑大纲数据模型")
public class ArticleConfirmOutlineRequest implements Serializable {
    @Schema(name = "taskId", description = "任务id")
    private String taskId;

    @Schema(name = "selectOutlineList", description = "用户修改后的大纲列表")
    private List<ArticleState.OutlineSection> selectOutlineList;

    @Serial
    private static final long serialVersionUID = 1L;
}
