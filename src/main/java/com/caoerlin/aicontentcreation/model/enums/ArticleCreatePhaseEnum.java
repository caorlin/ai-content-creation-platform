package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ArticleCreatePhaseEnum {
    PENDING("PENDING", "等待中"),
    TITLE_GENERATING("TITLE_GENERATING", "标题生成中"),
    TITLE_SELECTING("TITLE_SELECTING", "标题选择中"),
    OUTLINE_GENERATING("OUTLINE_GENERATING", "大纲生成中"),
    OUTLINE_EDITING("OUTLINE_EDITING", "大纲编辑中"),
    CONTENT_GENERATING("CONTENT_GENERATING", "正文生成中");


    private final String phase;
    private final String desc;

    private static final Map<String, ArticleCreatePhaseEnum> phaseRefArticleCreatePhaseEnumMap = new HashMap<>();

    static {
        for (ArticleCreatePhaseEnum articleCreatePhaseEnum : ArticleCreatePhaseEnum.values()) {
            phaseRefArticleCreatePhaseEnumMap.put(articleCreatePhaseEnum.getPhase(), articleCreatePhaseEnum);
        }
    }

    /**
     * 根据阶段获取枚举
     *
     * @param phase 阶段
     */
    public static ArticleCreatePhaseEnum getByPhase(String phase) {
        if (StrUtil.isBlank(phase)) {
            return null;
        }
        return phaseRefArticleCreatePhaseEnumMap.get(phase);
    }
}
