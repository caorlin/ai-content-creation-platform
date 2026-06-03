package com.caoerlin.aicontentcreation.model.vo.article;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zyj
 */
@Data
public class ArticleVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 用户补充描述
     */
    private String userDescription;

    /**
     * 主标题
     */
    private String mainTitle;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 文章大纲
     */
    private String outline;

    /**
     * 正文
     */
    private String content;

    /**
     * 完整图文（含配图）
     */
    private String fullContent;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 配图列表（JSON数组）
     */
    private String images;

    /**
     * 状态
     */
    private String status;

    /**
     * 当前阶段
     */
    private String phase;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
