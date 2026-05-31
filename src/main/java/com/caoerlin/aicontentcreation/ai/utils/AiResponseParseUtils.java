package com.caoerlin.aicontentcreation.ai.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.common.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AiResponseParseUtils {

    /**
     * 解析json内容
     *
     * @param content ai响应的内容
     * @param clazz   要转换的类型
     * @param name    名称
     * @return
     */
    public static <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        if (StrUtil.isBlank(content)) {
            log.error("{}解析内容为空,解析失败", name);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + "解析内容为空,解析失败");
        }
        try {
            return JSONUtil.toBean(content, clazz);
        } catch (Exception e) {
            log.error("{}解析失败,content = {},e = {}", name, content, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,name + "解析失败");
        }
    }
}
