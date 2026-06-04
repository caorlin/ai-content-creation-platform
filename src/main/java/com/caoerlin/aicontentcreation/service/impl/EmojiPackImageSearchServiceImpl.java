package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.config.EmojiPackConfig;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.caoerlin.aicontentcreation.constant.ImageConstont.PICSUM_URL_TEMPLATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiPackImageSearchServiceImpl implements ImageSearchService {
    private final EmojiPackConfig emojiPackConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isBlank(keywords)) {
            log.warn("emoji pack 搜索关键词为空");
            return null;
        }

        try {
            //构建搜索词,默认添加 表情包
            String searchKeywords = keywords + emojiPackConfig.getSuffix();
            log.info("开始搜索表情包:{}->{}", keywords, searchKeywords);

            //创建URL
            String searchUrl = buildSearchUrl(searchKeywords);

            //Jsoup搜索
            Document document = Jsoup.connect(searchUrl)
                    .timeout(emojiPackConfig.getTimeout())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
                    .get();

            //定位图片容器
            Element div = document.getElementsByClass("dgControl").first();
            if (ObjectUtil.isNull(div)) {
                log.warn("Bing 未找到图片容器,keywords={}", keywords);
                return null;
            }

            //使用css选择器选择图片
            Elements imgElements = div.select("img.mimg");
            if (imgElements.isEmpty()) {
                log.warn("Bing 未检索到表情包,keywords={},searchKeywords={}", keywords, searchKeywords);
                return null;
            }

            //获取第一张图片url
            String imgUrl = imgElements.get(0).attr("src");
            if (StrUtil.isBlank(imgUrl)) {
                log.warn("图片 URL 为空,keywords={}", keywords);
                return null;
            }

            //清理url参数,移除?s=xxx&q=xxx
            imgUrl = clearImgUrl(imgUrl);

            log.info("Bing 检索到表情包完成,keywords={},url={}", keywords, imgUrl);
            return imgUrl;
        } catch (IOException e) {
            log.error("Bing 检索到表情包失败,keywords={},e={}", keywords, e.getMessage());
            return null;
        }
    }

    private String clearImgUrl(String imgUrl) {
        if (StrUtil.isBlank(imgUrl)) {
            return imgUrl;
        }

        int questionMarkIndex = imgUrl.indexOf("?");
        if (questionMarkIndex > 0) {
            return imgUrl.substring(0, questionMarkIndex);
        }
        return imgUrl;
    }

    /**
     * 构建搜索url
     */
    private String buildSearchUrl(String searchKeywords) {
        String encodeKeywords = URLEncoder.encode(searchKeywords, StandardCharsets.UTF_8);

        return String.format("%s?q=%s&mmasync=1",
                emojiPackConfig.getSearchUrl(),
                encodeKeywords
        );
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.EMOJI_PACK;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
