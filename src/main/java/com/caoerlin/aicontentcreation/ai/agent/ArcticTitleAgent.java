package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

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
    public void generateArticleTitle(ArticleState state) {
        //文章选题
        String topic = state.getTopic();
        //将用户选题加入prompt
        String articleTitlePrompt = PromptConstant.AGENT1_TITLE_PROMPT.replace("{topic}", topic);
        //调用模型生成文章标题
        String content = callModel(articleTitlePrompt);

        ArticleState.TitleResult titleResult = parseArticleTitle(content);
        //设置文章标题
        state.setTitle(titleResult);

        log.info("智能体 ArcticTitleAgent 生成文章标题成功,mainTitle={}", titleResult.getMainTitle());
    }

    private ArticleState.TitleResult parseArticleTitle(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        return JSONUtil.toBean(content, ArticleState.TitleResult.class);
    }

    private String callModel(String prompt) {
        if (StrUtil.isBlank(prompt)) {
            return "";
        }
        ChatResponse chatResponse = articleTitleChatModel.call(new Prompt(prompt));
        return chatResponse.getResult().getOutput().getText();
    }
}
