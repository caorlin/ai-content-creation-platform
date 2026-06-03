package com.caoerlin.aicontentcreation.model.dto.image;

import cn.hutool.core.util.StrUtil;

/**
 * @author zyj
 * 图片请求
 * 统一封装图片检索想要的参数
 */
public class ImageRequest {
    /**
     * 关键词
     */
    private String keywords;

    /**
     * AI生成提示词
     */
    private String prompt;

    /**
     * 图片类型 cover/section
     */
    private String type;

    /**
     * 宽高比 16:9 1：1
     */
    private String aspectRatio;

    /**
     * 图片风格描述
     */
    private String style;

    /**
     * 获取图片生成影响的参数
     * 如果是AI优先使用提示词生成,不是就使用关键词
     *
     * @param aiGenerated 是否是AI生成
     * @return AI生成返回prompt,其他使用关键词
     */
    public String getEffectiveParam(boolean aiGenerated) {
        if (aiGenerated) {
            return StrUtil.isBlank(prompt) ? keywords : prompt;
        }
        return StrUtil.isBlank(keywords) ? prompt : keywords;
    }
}
