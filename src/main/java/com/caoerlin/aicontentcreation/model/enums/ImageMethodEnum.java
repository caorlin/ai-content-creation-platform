package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片检索方法
 * aiGenerate 是否ai生成
 * fallback 是否是降级方案
 */
@Getter
@RequiredArgsConstructor
public enum ImageMethodEnum {
    /**
     * Pexels 图库
     */
    PEXELS("PEXELS", "Pexels 图库", false, false),

    /**
     * nano banana AI生图
     */
    NANO_BANANA("NANO_BANANA", "nano banana AI生图", true, false),

    /**
     * mermaid 流程图
     */
    MERMAID("MERMAID", "mermaid 流程图", false, false),

    /**
     * iconify 图标库
     */
    ICONIFY("ICONIFY", "iconify 图标库", false, false),

    /**
     * 表情包检索
     */
    EMOJI_PACK("EMOJI_PACK", "表情包检索", false, false),

    /**
     * SVG 概念图(AI 生成)
     */
    SVG_DIAGRAM("SVG_DIAGRAM", "SVG 概念图", true, false),

    /**
     * Picsum 随机图片（降级方案）
     */
    PICSUM("PICSUM", "Picsum 随机图片", false, true);

    /**
     * 新增图片检索类型
     * nano banana AI生图
     * mermaid 流程图
     * iconify 图标库
     * 表情包检索
     * SVG 概念图(AI 生成)
     */


    private final String name;
    private final String desc;
    private final Boolean aiGenerate;
    private final Boolean fallback;

    private static final Map<String, ImageMethodEnum> imageNameRefImageMethodEnumMap = new HashMap<>();

    static {
        for (ImageMethodEnum imageMethodEnum : ImageMethodEnum.values()) {
            imageNameRefImageMethodEnumMap.put(imageMethodEnum.name, imageMethodEnum);
        }
    }

    /**
     * 根据图片检索库名称获取检索方式
     *
     * @param name 图片检索库名称
     * @return
     */
    public static ImageMethodEnum getInstance(String name) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        return imageNameRefImageMethodEnumMap.get(name);
    }

    public static ImageMethodEnum getDefaultSearchMethod() {
        return PEXELS;
    }

    public static ImageMethodEnum getDefaultAiGenerateMethod() {
        return NANO_BANANA;
    }
}
