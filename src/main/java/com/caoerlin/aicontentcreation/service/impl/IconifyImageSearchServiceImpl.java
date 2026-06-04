package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.config.IconifyConfig;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.caoerlin.aicontentcreation.constant.ImageConstont.PICSUM_URL_TEMPLATE;

/**
 * Iconify图标检索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IconifyImageSearchServiceImpl implements ImageSearchService {
    private final IconifyConfig iconifyConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isBlank(keywords)) {
            log.warn("iconify 搜索关键词为空");
            return null;
        }

        //搜索图标
        String searchUrl = buildSearchUrl(keywords);
        String searchResult = callApi(searchUrl);

        if (StrUtil.isBlank(searchResult)) {
            return null;
        }

        //解析结果
        String iconName = parseSearchResult(searchResult);

        if (StrUtil.isBlank(iconName)) {
            log.warn("未解析到 iconify 图标：keywords={}", keywords);
        }

        //获取svg
        String svgUrl = buildSvgUrl(iconName);

        log.info("iconify 图标检索成功,{}->{}", keywords, svgUrl);
        return svgUrl;
    }

    /**
     * 构建 svg
     * API URI that generates SVG is /{prefix}/{name}.svg, where:
     * "{prefix}" is icon set prefix.
     * "{name}" is icon name.
     * 比如："https://api.iconify.design/bi/bell-fill.svg?height=16&color=%23ba3329"
     *
     * @param iconName 图标名称
     * @return svgUrl
     */
    private String buildSvgUrl(String iconName) {
        if (StrUtil.isBlank(iconName)) {
            log.warn("图标名称为空");
            return null;
        }

        //将:替换为/
        //如 ic:baseline-people -> ic/baseline-people
        String path = iconName.replace(":", "/");

        //
        StringBuilder svgUrl = new StringBuilder(iconifyConfig.getApiUrl())
                .append("/")
                .append(path)
                .append(".svg");

        //设置高度
        boolean hasParam = false;
        if (ObjectUtil.isNotNull(iconifyConfig.getDefaultHeight())) {
            svgUrl.append("?height=")
                    .append(iconifyConfig.getDefaultHeight());
            hasParam = true;
        }

        //设置颜色
        String defaultColor = iconifyConfig.getDefaultColor();
        if (StrUtil.isNotBlank(defaultColor)) {
            String colorParam = hasParam ? "&color=" : "?color=";

            //处理颜色格式 URI不能包含“#”，所以如果你使用十六进制颜色，比如上面示例中的“#ba3329”，请确保将“#”替换为“%23
            if (defaultColor.startsWith("#")) {
                defaultColor = "%23" + defaultColor.substring(1);
            }

            svgUrl.append(colorParam).append(defaultColor);
        }

        return svgUrl.toString();
    }

    /**
     * 解析 iconify 图标
     *
     *
     */
    private String parseSearchResult(String searchResult) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(searchResult);
            JSONArray icons = jsonObject.getJSONArray("icons");

            if (JSONUtil.isNull(icons)) {
                return null;
            }

            log.info("iconify 解析到图标,icon={}", icons.get(0).toString());
            //取第一个图标
            return icons.get(0).toString();
        } catch (Exception e) {
            log.error("iconify 解析图标失败,e={}", e.getMessage());
            return null;
        }
    }

    private String callApi(String searchUrl) {
        try (HttpResponse response = HttpRequest.get(searchUrl).execute()) {
            if (!response.isOk()) {
                log.warn("iconify 调用失败,url={}", searchUrl);
                return "";
            }
            return response.body();
        } catch (Exception e) {
            log.warn("iconify 调用异常,url={}", searchUrl);
            return null;
        }
    }

    /**
     * 创建搜索url
     *
     * @param keywords
     * @return
     */
    private String buildSearchUrl(String keywords) {
        String encodeKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
        return String.format("%S/search?query=%s&limit=%d",
                iconifyConfig.getApiUrl(),
                encodeKeywords,
                iconifyConfig.getSearchLimit());
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.ICONIFY;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
