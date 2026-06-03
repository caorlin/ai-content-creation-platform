package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.config.NanoBananaConfig;
import com.caoerlin.aicontentcreation.model.dto.image.ImageData;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.caoerlin.aicontentcreation.constant.ImageConstont.PICSUM_URL_TEMPLATE;

/**
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NanoBananImageServiceImpl implements ImageSearchService {

    private final NanoBananaConfig nanoBananaConfig;

    @Override
    public String searchImage(String prompt) {
        //废弃,在ImageServiceStrategy使用getImageDate()获取、生成图片
        return "";
    }

    @Override
    public ImageData getImageDate(ImageRequest request) {
        String prompt = request.getEffectiveParam(true);
        return generateImageData(prompt);
    }

    /**
     * 根据提示词生成图片
     *
     * @param prompt 提示词
     * @return 图片数据
     */
    private ImageData generateImageData(String prompt) {
        //获取client
        try (Client nanoClient = Client.builder().apiKey(nanoBananaConfig.getApiKey()).build()) {
            //图片配置
            ImageConfig.Builder imageConfigBuilder = ImageConfig.builder()
                    .aspectRatio(nanoBananaConfig.getAspectRatio());

            //Gemini 3 pro image支持跟高的分辨率
            String model = nanoBananaConfig.getModel();
            if (StrUtil.isNotBlank(model) || model.contains("gemini-3-pro")) {
                imageConfigBuilder.imageSize(nanoBananaConfig.getImageSize());
            }

            //构建生成配置
            GenerateContentConfig generateContentConfig = GenerateContentConfig.builder()
                    .responseModalities("TEXT", "IMAGE")
                    .imageConfig(imageConfigBuilder.build())
                    .build();

            log.info("Nano Banan 开始生成图片,model={},prompt={}", model, prompt);

            GenerateContentResponse response = nanoClient.models.generateContent(
                    StrUtil.isBlank(model) ? "gemini-2.5-flash-image" : model,
                    prompt,
                    generateContentConfig
            );

            //从响应中提取图片
            ImmutableList<Part> partImmutableList = response.parts();
            if (CollectionUtil.isNotEmpty(partImmutableList)) {
                for (Part part : partImmutableList) {
                    if (part.inlineData().isPresent()) {
                        Blob blob = part.inlineData().get();
                        if (blob.data().isPresent()) {
                            byte[] imageBytes = blob.data().get();
                            String mimeType = blob.mimeType().orElse("image/png");

                            log.info("Nano Banan 图片生成成功,size={},mimeType={}", imageBytes.length, mimeType);

                            return ImageData.fromBytes(imageBytes, mimeType);
                        }
                    }
                }
            }

            log.warn("Nano Banan 未生成图片,prompt={}", prompt);
            return null;
        } catch (Exception e) {
            log.error("Nano Banan 生成图片发生异常,prompt={},e={}", prompt, e.getMessage());
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.NANO_BANANA;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
