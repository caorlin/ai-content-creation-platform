package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallingUtils;
import com.caoerlin.aicontentcreation.ai.utils.AiResponseParseUtils;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

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

        //生成文章需要的图片关键字prompt
        String prompt = PromptConstant.ARTICLE_IMAGE_REQUIREMENTS_AGENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent());

        //生成图片关键字
        String content = AiModelCallingUtils.callModel(articleContentModel, prompt);

        //提取
        List<ArticleState.ImageRequirement> imageRequirementList = AiResponseParseUtils.parseJsonListResponse(content, ArticleState.ImageRequirement.class, "文章配图需求");

        state.setImageRequirements(imageRequirementList);

        log.info("智能体 ArticleImageGenerateAgent 生成文章内容成功,size={}", imageRequirementList.size());
    }
}
