package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ArticleStatusEnum {
    PENDING("PENDING", "待定"),
    PROCESSING("PROCESSING", "执行中"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "执行失败");

    private final String status;
    private final String desc;

    private static final Map<String, ArticleStatusEnum> statusRefArticleStatusEnumMap = new HashMap<>();

    static {
        for (ArticleStatusEnum value : ArticleStatusEnum.values()) {
            statusRefArticleStatusEnumMap.put(value.getStatus(), value);
        }
    }

    public static ArticleStatusEnum getArticleStatusEnumByStatus(String status) {
        ArticleStatusEnum articleStatusEnum = statusRefArticleStatusEnumMap.get(status);
        if (ObjectUtil.isNull(articleStatusEnum)) {
            return null;
        }
        return articleStatusEnum;
    }
}
