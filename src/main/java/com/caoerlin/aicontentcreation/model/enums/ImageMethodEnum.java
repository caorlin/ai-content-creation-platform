package com.caoerlin.aicontentcreation.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageMethodEnum {
    /**
     *
     */
    PEXELS("PEXELS", "Pexels 图库", false, false),
    /**
     * Picsum 随机图片（降级方案）
     */
    PICSUM("PICSUM", "Picsum 随机图片", false, true);


    private final String name;
    private final String desc;
    private final Boolean aiGenerate;
    private final Boolean fallback;

    public static ImageMethodEnum getDefaultSearchMethod() {
        return PEXELS;
    }
}
