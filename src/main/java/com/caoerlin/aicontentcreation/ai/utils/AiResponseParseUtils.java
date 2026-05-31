package com.caoerlin.aicontentcreation.ai.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
        hasContent(content, name);
        try {
            return JSONUtil.toBean(content, clazz);
        } catch (Exception e) {
            parseExceptionLogger(content, name, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, name + "解析失败");
        }
    }

    /**
     * 解析json数组
     *
     * @param content ai响应的内容
     * @param clazz   要转换的类型
     * @param name    名称
     * @return
     */
    public static <T> List<T> parseJsonListResponse(String content, Class<T> clazz, String name) {
        hasContent(content, name);
        try {
            JSONArray jsonArray = JSONUtil.parseArray(content);
            return JSONUtil.toList(jsonArray, clazz);
        } catch (Exception e) {
            parseExceptionLogger(content, name, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, name + "解析失败");
        }
    }

    private static void hasContent(String content, String name) {
        if (StrUtil.isBlank(content)) {
            log.error("{}解析内容为空,解析失败", name);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, name + "解析内容为空,解析失败");
        }
    }

    private static void parseExceptionLogger(String content, String name, Exception e) {
        log.error("{}解析失败,content = {},e = {}", name, content, e.getMessage());
    }
}
