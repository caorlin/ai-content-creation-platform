package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.model.dto.image.ImageData;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static com.caoerlin.aicontentcreation.constant.ImageConstont.PICSUM_URL_TEMPLATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SvgDiagramImageServiceImpl implements ImageSearchService {
    private final ChatModel imageGenerateChatModel;

    @Override
    public ImageData getImageDate(ImageRequest request) {
        String requirement = request.getEffectiveParam(true);
        return generateSvgDiagram(requirement);
    }

    private ImageData generateSvgDiagram(String requirement) {
        if (StrUtil.isBlank(requirement)) {
            log.warn("SVG 生成需求为空");
            return null;
        }

        try {
            //大模型生成svg
            String svgCode = callAiModelGenSvg(requirement);

            if (StrUtil.isBlank(svgCode)) {
                log.warn("AI 没有生成 SVG 示意图,");
                return null;
            }

            //验证 SVG 格式
            if (!isValidSvg(svgCode)) {
                log.warn("生成的 SVG 格式不正确");
                return null;
            }

            //转换为字节
            byte[] svgCodeBytes = svgCode.getBytes(StandardCharsets.UTF_8);

            log.info("生成 SVG 概念示意图完成,svgCodeBytes={} byte,requirement={}", svgCodeBytes.length, requirement);
            return ImageData.fromBytes(svgCodeBytes, "image/svg+xml");
        } catch (Exception e) {
            log.error("生成 SVG 概念示意图失败,requirement={},e={}", requirement, e.getMessage());
            return null;
        }
    }

    /**
     * 验证 SVG 格式
     */
    private boolean isValidSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return false;
        }

        // 基本验证：包含 svg 标签
        return svgCode.contains("<svg") && svgCode.contains("</svg>");
    }


    private String callAiModelGenSvg(String requirement) {
        String prompt = PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT.replace("{requirement}", requirement);

        log.info("开始调用AI模型生成 SVG 概念示意图,requirement={}", requirement);

        ChatResponse chatResponse = imageGenerateChatModel.call(new Prompt(new UserMessage(prompt)));
        String svgCode = chatResponse.getResult().getOutput().getText().trim();


        //提取svg 去除markdown代码块
        svgCode = extractSvg(svgCode);

        return svgCode;
    }

    private String extractSvg(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }

        text = text.replace("```xml", "").replace("```svg", "").replace("```", "");

        //确保含有xml声明
        if (!text.startsWith("<?xml")) {
            //如果没有xml声明，但有<svg>标签，就添加xml标签
            if (text.startsWith("<svg")) {
                text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + text;
            }
        }
        return text;
    }

    @Override
    public boolean isAvailable() {
        return ImageSearchService.super.isAvailable();
    }

    @Override
    public String searchImage(String keywords) {
        return "";
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
