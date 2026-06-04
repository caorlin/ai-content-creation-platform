package com.caoerlin.aicontentcreation.constant;

/**
 * @author zyj
 */
public interface ImageConstont {
    /**
     * Pexels API 地址
     */
    String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    /**
     * Pexels 每页返回数量
     */
    int PEXELS_PER_PAGE = 1;

    /**
     * Pexels 图片方向：横向
     */
    String PEXELS_ORIENTATION_LANDSCAPE = "landscape";

    /**
     * Picsum 随机图片 URL 模板
     */
    String PICSUM_URL_TEMPLATE = "https://picsum.photos/800/600?random=%d";

    // region Bing 表情包相关常量

    /**
     * Bing 图片搜索地址
     */
    String BING_IMAGE_SEARCH_URL = "https://cn.bing.com/images/async";

    /**
     * 表情包关键词后缀（程序固定拼接）
     */
    String EMOJI_PACK_SUFFIX = "表情包";

    /**
     * Bing 图片搜索每批最大数量
     */
    int BING_MAX_IMAGES = 30;

// endregion

    Integer SVG_DEFAULT_WIDTH = 1200;

    Integer SVG_DEFAULT_HEIGHT = 800;

}
