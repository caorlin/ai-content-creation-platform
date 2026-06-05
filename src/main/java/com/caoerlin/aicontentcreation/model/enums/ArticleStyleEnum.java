package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
public enum ArticleStyleEnum {
    TECH("tech", "科技风格"),
    EMOTIONAL("emotional", "情感风格"),
    EDUCATIONAL("educational", "教育风格"),
    Humorous("humorous", "幽默风趣风格");


    private final String style;
    private final String desc;

    private static final Map<String, ArticleStyleEnum> styleRefArticleStyleEnumMap = new HashMap<>();

    static {
        for (ArticleStyleEnum articleStyleEnum : ArticleStyleEnum.values()) {
            styleRefArticleStyleEnumMap.put(articleStyleEnum.getStyle(), articleStyleEnum);
        }
    }

    /**
     * 获取文章风格列表
     *
     * @return 文章风格列表
     */
    public List<String> getValueList() {
        return Arrays.stream(ArticleStyleEnum.values()).map(ArticleStyleEnum::getStyle).toList();
    }

    /**
     * 根据文章风格获取文章风格枚举
     *
     * @param style 文章风格
     * @return
     */
    public static ArticleStyleEnum getInstanceByStyle(String style) {
        if (StrUtil.isBlank(style)) {
            return null;
        }
        return styleRefArticleStyleEnumMap.get(style);
    }

    public static String getArticleStylePrompt(String style) {
        if (StrUtil.isBlank(style)) {
            return "";
        }

        ArticleStyleEnum articleStyleEnum = getInstanceByStyle(style);

        if (ObjectUtil.isNull(articleStyleEnum)) {
            return null;
        }

        return switch (articleStyleEnum) {
            case TECH -> PromptConstant.STYLE_TECH_PROMPT;
            case EMOTIONAL -> PromptConstant.STYLE_EMOTIONAL_PROMPT;
            case EDUCATIONAL -> PromptConstant.STYLE_EDUCATIONAL_PROMPT;
            case Humorous -> PromptConstant.STYLE_HUMOROUS_PROMPT;
        };
    }

    public static boolean hasStyle(String style){
        return ObjectUtil.isNotNull(getInstanceByStyle(style));
    }
}
