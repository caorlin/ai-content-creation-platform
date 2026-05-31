package com.caoerlin.aicontentcreation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 文章表
 * @TableName article
 */
@TableName(value ="article")
public class Article implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID（UUID）
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
     * 主标题
     */
    private String mainTitle;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 大纲（JSON格式）
     */
    private Object outline;

    /**
     * 正文（Markdown格式）
     */
    private String content;

    /**
     * 完整图文（Markdown格式，含配图）
     */
    private String fullContent;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 配图列表（JSON数组）
     */
    private Object images;

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date completedTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 任务ID（UUID）
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 任务ID（UUID）
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 选题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 选题
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 主标题
     */
    public String getMainTitle() {
        return mainTitle;
    }

    /**
     * 主标题
     */
    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    /**
     * 副标题
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     * 副标题
     */
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    /**
     * 大纲（JSON格式）
     */
    public Object getOutline() {
        return outline;
    }

    /**
     * 大纲（JSON格式）
     */
    public void setOutline(Object outline) {
        this.outline = outline;
    }

    /**
     * 正文（Markdown格式）
     */
    public String getContent() {
        return content;
    }

    /**
     * 正文（Markdown格式）
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 完整图文（Markdown格式，含配图）
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * 完整图文（Markdown格式，含配图）
     */
    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }

    /**
     * 封面图 URL
     */
    public String getCoverImage() {
        return coverImage;
    }

    /**
     * 封面图 URL
     */
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    /**
     * 配图列表（JSON数组）
     */
    public Object getImages() {
        return images;
    }

    /**
     * 配图列表（JSON数组）
     */
    public void setImages(Object images) {
        this.images = images;
    }

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    public String getStatus() {
        return status;
    }

    /**
     * 状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 错误信息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 完成时间
     */
    public Date getCompletedTime() {
        return completedTime;
    }

    /**
     * 完成时间
     */
    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}