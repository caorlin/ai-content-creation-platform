package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallingUtils;
import com.caoerlin.aicontentcreation.ai.utils.AiResponseParseUtils;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

import static com.caoerlin.aicontentcreation.model.enums.ArticleStyleEnum.getArticleStylePrompt;

/**
 * 文章大纲生成
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleOutlineAgent {
    private final ChatModel articleContentModel;

    public void generateArticleOutline(ArticleState articleState, Consumer<String> consumer) {
        if (ObjectUtil.isNull(articleState)) {
            log.error("生成文章大纲异常,文章状态为空");
            return;
        }
        //获取文章标题
        ArticleState.TitleResult title = articleState.getTitle();
        //生成文章大纲prompt
        String articleOutlinePrompt = PromptConstant.ARTICLE_OUTLINE_AGENT_PROMPT
                .replace("{mainTitle}", title.getMainTitle())
                .replace("{subTitle}", title.getSubTitle())
                + getArticleStylePrompt(articleState.getStyle());
        String content = AiModelCallingUtils.callModelWithStreaming(articleContentModel, articleOutlinePrompt, consumer, SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_STREAMING);
        //解析大纲
        ArticleState.OutlineResult outlineResult = AiResponseParseUtils.parseJsonResponse(content, ArticleState.OutlineResult.class, "文章大纲");
        articleState.setOutline(outlineResult);

        log.info("智能体 ArticleOutlineAgent 生成文章大纲成功,sections={}", outlineResult.getSections());
    }
}
