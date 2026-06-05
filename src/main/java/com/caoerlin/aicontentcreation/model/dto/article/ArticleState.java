package com.caoerlin.aicontentcreation.model.dto.article;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleState implements Serializable {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 标题结果（智能体1输出）
     */
    private TitleResult title;

    /**
     * 大纲结果（智能体2输出）
     */
    private OutlineResult outline;

    /**
     * 正文内容（智能体3输出）
     */
    private String content;

    /**
     * 配图需求列表（智能体4输出）
     */
    private List<ImageRequirement> imageRequirements;

    /**
     * 封面图 URL（单独存储，同时 images 列表中的 position=1 也是封面图）
     */
    private String coverImage;

    /**
     * 配图结果列表（智能体5输出）
     */
    private List<ImageResult> images;

    /**
     * 完整图文内容（合成后）
     */
    private String fullContent;

    /**
     * 允许的配图方式列表（为空表示支持所有方式）
     */
    private List<String> enabledImageMethods;

    /**
     * 文章风格
     */
    private String style;

    private static final long serialVersionUID = 1L;

    /**
     * 嵌套内部类
     */

    /**
     * 标题结果
     */
    @Data
    public static class TitleResult implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    /**
     * 大纲结果
     */
    @Data
    public static class OutlineResult implements Serializable {
        private List<OutlineSection> sections;
    }

    /**
     * 大纲章节
     */
    @Data
    public static class OutlineSection implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    /**
     * 配图需求
     */
    @Data
    public static class ImageRequirement implements Serializable {
        private Integer position;
        private String type;
        private String sectionTitle;
        private String keywords;
        private String imageSource;
        private String prompt;

        /**
         * 占位符ID，用于正文中定位插入位置
         */
        private String placeholderId;
    }

    /**
     * 配图结果
     */
    @Data
    @Builder
    public static class ImageResult implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;

        /**
         * 占位符ID，用于正文中定位插入位置
         */
        private String placeholderId;
    }

    /**
     * 文字图片需求智能体返回结果（包含带占位符的正文和配图需求列表）
     */
    @Data
    public static class ArticleImageRequirementsResult implements Serializable {
        /**
         * 包含占位符的正文内容
         */
        private String contentWithPlaceholders;
        /**
         * 配图需求列表
         */
        private List<ImageRequirement> imageRequirements;
    }

}