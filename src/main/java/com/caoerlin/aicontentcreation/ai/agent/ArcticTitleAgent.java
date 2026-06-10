package com.caoerlin.aicontentcreation.ai.agent;

import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallingUtils;
import com.caoerlin.aicontentcreation.ai.utils.AiResponseParseUtils;
import com.caoerlin.aicontentcreation.common.annotation.AgentExecution;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.caoerlin.aicontentcreation.model.enums.ArticleStyleEnum.getArticleStylePrompt;

/**
 * 生成文章标题agent
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArcticTitleAgent {
    private final ChatModel articleTitleChatModel;

    /**
     * 生成文章标题
     *
     * @param state 文章状态
     */
    @AgentExecution(value = "ARTICLE_TITLE_AGENT", description = "文章标题生成Agent")
    public void generateArticleTitle(ArticleState state) {
        //文章选题
        String topic = state.getTopic();
        //将用户选题加入prompt
        String articleTitlePrompt = PromptConstant.ARTICLE_TITLE_AGENT_PROMPT
                .replace("{topic}", topic)
                + getArticleStylePrompt(state.getStyle());
        //调用模型生成文章标题
        String content = AiModelCallingUtils.callModel(articleTitleChatModel, articleTitlePrompt);

        //ArticleState.TitleResult titleResult = AiResponseParseUtils.parseJsonResponse(content, ArticleState.TitleResult.class, "文章标题");
        List<ArticleState.TitleOption> titleOptionList = AiResponseParseUtils.parseJsonListResponse(content, ArticleState.TitleOption.class, "文章标题");
        //设置文章可选标题列表
        //state.setTitle(titleResult);
        state.setTitleOptions(titleOptionList);

        log.info("智能体 ArcticTitleAgent 生成文章可选标题列表成功,titleOptionSize={}", titleOptionList.size());
    }
}
