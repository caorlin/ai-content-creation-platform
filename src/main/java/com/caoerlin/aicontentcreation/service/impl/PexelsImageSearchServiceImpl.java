package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.config.PexelsConfig;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.caoerlin.aicontentcreation.constant.ImageConstont.*;

/**
 * pexels 图片搜索
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PexelsImageSearchServiceImpl implements ImageSearchService {
    private final PexelsConfig pexelsConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isBlank(keywords)) {
            log.warn("开始 Pexels 搜索图片接口,关键词为空");
            return "";
        }

        log.info("开始 Pexels 搜索图片接口,查询图片");
        try (HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                .header("Authorization", pexelsConfig.getApiKey())
                .form("query", keywords)
                .form("per_pare", PEXELS_PER_PAGE)
                .form("orientation", PEXELS_ORIENTATION_LANDSCAPE)
                .execute()) {
            if (response.isOk()) {
                log.info("开始 Pexels 搜索图片接口,获取 Pexels 响应成功");
                //解析请求体为json对象
                JSONObject result = JSONUtil.parseObj(response.body());
                //获取图片信息
                JSONArray photos = result.getJSONArray("photos");
                if (photos.isEmpty()) {
                    log.warn("开始 Pexels 搜索图片接口,未搜索到图片");
                    return "";
                }
                JSONObject photo = photos.getJSONObject(0);
                JSONObject src = photo.getJSONObject("src");
                log.info("开始 Pexels 搜索图片接口,成功获取相关图片,src={}", src.get("large").toString());
                return src.get("large").toString();
            }
        } catch (Exception e) {
            log.warn("开始 Pexels 搜索图片接口,搜索失败,e={}", e.getMessage());
        }
        return "";
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    @Override
    public String getFallbackImage(int position) {
        //服务降级处理，随机搜索一个图片
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
