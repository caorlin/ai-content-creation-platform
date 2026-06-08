package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatusEnum {

    PENDING("PENDING", "待支付"),
    SUCCEEDED("SUCCEEDED", "支付成功"),
    FAILED("FAILED", "支付失败"),
    REFUNDED("REFUNDED", "已退款");

    private final String status;
    private final String desc;

    private static final Map<String, PaymentStatusEnum> STRING_PAYMENT_STATUS_ENUM_HASH_MAP = new HashMap<>();

    static {
        for (PaymentStatusEnum statusEnum : PaymentStatusEnum.values()) {
            STRING_PAYMENT_STATUS_ENUM_HASH_MAP.put(statusEnum.getStatus(), statusEnum);
        }
    }

    public static PaymentStatusEnum getByStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        return STRING_PAYMENT_STATUS_ENUM_HASH_MAP.get(status);
    }
}
