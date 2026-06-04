package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallingUtils;
import com.caoerlin.aicontentcreation.ai.utils.AiResponseParseUtils;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章图片需求agent
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageRequirementsAgent {
    private final ChatModel articleContentModel;

    public void analyzeArticleIllustration(ArticleState state) {
        if (ObjectUtil.isNull(state)) {
            log.error("生成文章内容异常,文章状态为空");
            return;
        }

        String availableMethods = buildAvailableMethodsDescription(state.getEnabledImageMethods());

        //生成文章需要的图片关键字prompt
        String prompt = PromptConstant.ARTICLE_IMAGE_REQUIREMENTS_AGENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent())
                .replace("{availableMethods}", availableMethods);

        //生成图片关键字
        String content = AiModelCallingUtils.callModel(articleContentModel, prompt);

        //提取结果
        ArticleState.ArticleImageRequirementsResult imageRequirementsResult =
                AiResponseParseUtils.parseJsonResponse(
                        content,
                        ArticleState.ArticleImageRequirementsResult.class,
                        "文章配图需求"
                );

        state.setImageRequirements(imageRequirementsResult.getImageRequirements());
        state.setContent(imageRequirementsResult.getContentWithPlaceholders());

        log.info("智能体 ArticleImageGenerateAgent 生成文章内容成功,size={}", imageRequirementsResult.getImageRequirements().size());
    }

    private String buildAvailableMethodsDescription(List<String> enabledImageMethods) {
        if (CollectionUtil.isEmpty(enabledImageMethods)) {
            return getDefaultImageMethods();
        }
        //构建图片方法描述
        StringBuilder sb = new StringBuilder();
        for (String enabledImageMethod : enabledImageMethods) {
            ImageMethodEnum method = ImageMethodEnum.getInstance(enabledImageMethod);
            if (ObjectUtil.isNotNull(method)) {
                sb.append("   - ")
                        .append(method.getName())
                        .append(": ")
                        .append(getMethodUsageDescription(method))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private String getMethodUsageDescription(ImageMethodEnum method) {

        return switch (method) {
            case PEXELS -> "适合真实场景、产品照片、人物照片、自然风景等写实图片";
            case NANO_BANANA -> "适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片";
            case MERMAID -> "适合流程图、架构图、时序图、关系图、甘特图等结构化图表";
            case ICONIFY -> "适合图标、符号、小型装饰性图标(如：箭头、勾选、星星、心形等)";
            case EMOJI_PACK -> "适合表情包、搞笑图片、轻松幽默的配图";
            case SVG_DIAGRAM -> "适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）";
            default -> method.getDesc();
        };
    }

    private String getDefaultImageMethods() {
        return """
                 - PEXELS: 适合真实场景、产品照片、人物照片、自然风景等写实图片
                 - NANO_BANANA: 适合创意插画、信息图表、需要文字渲染、抽象概念、艺术风格等 AI 生成图片
                 - MERMAID: 适合流程图、架构图、时序图、关系图、甘特图等结构化图表
                 - ICONIFY: 适合图标、符号、小型装饰性图标(如：箭头、勾选、星星、心形等)
                 - EMOJI_PACK: 适合表情包、搞笑图片、轻松幽默的配图
                 - SVG_DIAGRAM: 适合概念示意图、思维导图样式、逻辑关系展示（不涉及精确数据）
                """;
    }
}
