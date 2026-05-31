package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallUtils;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 文章内容生成
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleContentAgent {
    private final ChatModel articleContentModel;

    public void generateArticleContent(ArticleState state, Consumer<String> streamHandler) {
        if (ObjectUtil.isNull(state)) {
            log.error("生成文章内容异常,文章状态为空");
            return;
        }

        //生成文章内容prompt
        ArticleState.TitleResult title = state.getTitle();
        ArticleState.OutlineResult outline = state.getOutline();
        String outlineJsonStr = JSONUtil.toJsonStr(outline.getSections());
        String prompt = PromptConstant.ARTICLE_CONTENT_AGENT_PROMPT.replace("{mainTitle}", title.getMainTitle())
                .replace("{subTitle}", title.getSubTitle())
                .replace("{outline}", outlineJsonStr);

        //根据prompt生成文章内容
        String content = AiModelCallUtils.callModelWithStreaming(articleContentModel, prompt, streamHandler, SseMessageTypeEnum.ARTICLE_CONTENT_AGENT_STREAMING);

        state.setContent(content);

        log.info("智能体 ArticleContentAgent 生成文章内容成功,length={}", content.length());
    }
}
