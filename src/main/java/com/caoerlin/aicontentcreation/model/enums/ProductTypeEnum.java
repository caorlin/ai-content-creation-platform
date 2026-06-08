package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum ProductTypeEnum {
    /**
     * 永久会员,199元
     */
    VIP_PERMANENT("VIP_PERMANENT", "永久会员", new BigDecimal("199.00"));

    private final String type;
    private final String desc;
    private final BigDecimal price;

    private static final Map<String, ProductTypeEnum> STRING_PRODUCT_TYPE_ENUM_HASH_MAP = new HashMap<>();

    static {
        for (ProductTypeEnum typeEnum : ProductTypeEnum.values()) {
            STRING_PRODUCT_TYPE_ENUM_HASH_MAP.put(typeEnum.type, typeEnum);
        }
    }

    public static ProductTypeEnum getInstanceByType(String type) {
        if (StrUtil.isBlank(type)) {
            return null;
        }
        return STRING_PRODUCT_TYPE_ENUM_HASH_MAP.get(type);
    }
}
