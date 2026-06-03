package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.dto.image.ImageData;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;

/**
 * 图片搜索service
 *
 * @author zyj
 */
public interface ImageSearchService {

    /**
     * 根据图片请求生成图片
     *
     * @param request 图片请求
     * @return url
     */
    default String getImage(ImageRequest request) {
        String promptOrKeywords = request.getEffectiveParam(getMethod().getAiGenerate());
        return searchImage(promptOrKeywords);
    }

    /**
     * 获取图片数据
     *
     * @param request 图片请求
     * @return 图片参数
     */
    default ImageData getImageDate(ImageRequest request) {
        //默认根据图片请求获取图片url
        String url = getImage(request);
        return ImageData.fromUrl(url);
    }

    /**
     * 根据关键词检索图片
     *
     * @param keywords 搜索关键词
     * @return 图片URL，检索失败返回null
     */
    String searchImage(String keywords);

    /**
     * 获取图片生成方式
     *
     * @return 图片检索方式枚举
     */
    ImageMethodEnum getMethod();

    /**
     * 获取降级图片URL
     *
     * @param position 位置序号（用于生成唯一的随机图片）
     * @return 降级图片URL
     */
    String getFallbackImage(int position);

    /**
     * 服务是否可用,其他服务可用重写该方法，改变状态
     *
     * @return 可用
     */
    default boolean isAvailable() {
        return true;
    }
}
