package com.caoerlin.aicontentcreation.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyj
 */
@Getter
public enum UserRoleEnum {

    /**
     *
     */
    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    private final static Map<String,UserRoleEnum> VALUE_MAP = new HashMap<>();

    static {
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            VALUE_MAP.put(anEnum.value,anEnum);
        }
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value){
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        if (!VALUE_MAP.containsKey(value)){
            return null;
        }
        return VALUE_MAP.get(value);
    }
}
