package com.caoerlin.aicontentcreation.service.strategy;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.constant.ArticleConstant;
import com.caoerlin.aicontentcreation.manager.CosManager;
import com.caoerlin.aicontentcreation.model.dto.image.ImageData;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.caoerlin.aicontentcreation.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * @author zyj
 * 图片生成策略选择器
 * 根据图片来源，使用不同的图片生成服务
 */
@Slf4j
@Service
public class ImageServiceStrategy {
    @Resource
    private List<ImageSearchService> imageSearchServices;

    @Resource
    private CosManager cosManager;

    /**
     * 图片生成方法对应图片生成服务
     */
    private final Map<ImageMethodEnum, ImageSearchService> imageMethodRefServiceMap = new EnumMap<>(ImageMethodEnum.class);

    @PostConstruct
    public void init() {
        //将所有的服务都映射到对现有的图片生成方法中
        for (ImageSearchService imageSearchService : imageSearchServices) {
            ImageMethodEnum method = imageSearchService.getMethod();
            imageMethodRefServiceMap.put(method, imageSearchService);

            log.info("已注册图片生成服务,{}->{},(AI Generated ->{},fallback->{})",
                    method.getName(),
                    imageSearchService.getClass().getName(),
                    method.getAiGenerate(),
                    method.getFallback()
            );
        }
    }

    /**
     * 获取图片，并且上传到cos
     *
     * @param imageSource 图片来源
     * @param request     图片请求
     * @return 图片结果
     */
    public ImageResult getImageAndUpload(String imageSource, ImageRequest request) {
        //处理未知图片来源
        ImageMethodEnum imageMethodEnum = resolveMethod(imageSource);
        //获取对应的生成服务
        ImageSearchService imageSearchService = imageMethodRefServiceMap.get(imageMethodEnum);

        if (ObjectUtil.isNull(imageMethodEnum) || !imageSearchService.isAvailable()) {
            log.warn("图片服务:{}不可用,开始启用降级方案", imageMethodEnum.getName());
            return handleFallbackImageAndUpload(request.getPosition());
        }

        try {
            //获取图片数据
            ImageData imageDate = imageSearchService.getImageDate(request);

            if (ObjectUtil.isNull(imageDate)) {
                //未获取图片采用降级方案
                log.warn("未获取到图片数据,开始启用降级方案,method={}", imageMethodEnum);
                return handleFallbackImageAndUpload(request.getPosition());
            }

            //上传到cos
            String folder = getFolderForMethod(imageMethodEnum);
            String cosUrl = cosManager.uploadImageData(imageDate, folder);

            if (StrUtil.isBlank(cosUrl)) {
                log.warn("图片上传 cos 失败,使用降级方案,method={}", imageMethodEnum);
                return handleFallbackImageAndUpload(request.getPosition());
            }

            log.info("图片上传 cos 成功,method={},cosUrl={}", imageMethodEnum, cosUrl);
            return new ImageResult(cosUrl, imageMethodEnum);
        } catch (Exception e) {
            log.info("获取图片或上传失败,使用降级方案,method={}", imageMethodEnum);
            return handleFallbackImageAndUpload(request.getPosition());
        }
    }

    private String getFolderForMethod(ImageMethodEnum imageMethodEnum) {
        return switch (imageMethodEnum) {
            case PEXELS -> "pexels";
            case NANO_BANANA -> "nano-banana";
            case MERMAID -> "mermaid";
            case ICONIFY -> "iconify";
            case EMOJI_PACK -> "emoji-pack";
            case SVG_DIAGRAM -> "svg-diagram";
            case PICSUM -> "picsum";
        };
    }

    private ImageResult handleFallbackImageAndUpload(Integer position) {
        int pos = ObjectUtil.isNull(position) ? 1 : position;
        //获取降级后得到的图片url
        String fallbackUrl = getFallbackImageUrl(pos);

        //将降级的图片上传到cos
        ImageData imageData = ImageData.fromUrl(fallbackUrl);
        String cosUrl = cosManager.uploadImageData(imageData, "fallback");

        return new ImageResult(cosUrl, ImageMethodEnum.PICSUM);
    }

    /**
     * 获取降级检索的图片url
     *
     * @param pos 序号
     * @return 降级搜索后的图片url
     */
    private String getFallbackImageUrl(int pos) {
        //先根据默认图片搜索服务搜索，没有就随机返回一个
        ImageSearchService defaultImageService = imageMethodRefServiceMap.get(ImageMethodEnum.getDefaultSearchMethod());

        if (ObjectUtil.isNotNull(defaultImageService)) {
            return defaultImageService.getFallbackImage(pos);
        }
        return String.format(PICSUM_URL_TEMPLATE, pos);
    }

    /**
     * 处理图片生成方法。处理未知值
     *
     */
    private ImageMethodEnum resolveMethod(String imageSource) {
        ImageMethodEnum method = ImageMethodEnum.getInstance(imageSource);
        if (ObjectUtil.isNull(method)) {
            //替换为默认来源
            log.warn("收到未知的图片来源:{},默认使用：{}", imageSource, ImageMethodEnum.getDefaultSearchMethod());
            return ImageMethodEnum.getDefaultSearchMethod();
        }
        return method;
    }


    @Data
    public static class ImageResult {
        private final String url;
        private final ImageMethodEnum method;
    }
}
