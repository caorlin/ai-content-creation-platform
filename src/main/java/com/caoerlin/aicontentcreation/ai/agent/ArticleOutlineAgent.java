package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
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
                .replace("{subTitle}", title.getSubTitle());
        String content = callModelWithStreaming(articleOutlinePrompt, consumer, SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_STREAMING);
        //解析大纲
        ArticleState.OutlineResult outlineResult = AiResponseParseUtils.parseJsonResponse(content, ArticleState.OutlineResult.class, "文章大纲");
        articleState.setOutline(outlineResult);

        log.info("智能体 ArticleOutlineAgent 生成文章大纲成功,sections={}",outlineResult.getSections());
    }

    private String callModelWithStreaming(String prompt, Consumer<String> consumer, SseMessageTypeEnum messageType) {
        //大纲内容
        StringBuilder outlineContent = new StringBuilder();

        //调用模型流式输出大纲
        Flux<ChatResponse> chatResponseFlux = articleContentModel.stream(new Prompt(new UserMessage(prompt)));

        //处理流式数据
        chatResponseFlux
                .doOnNext(chatResponse -> {
                    String chunk = chatResponse.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        outlineContent.append(chunk);
                        //用于前段流式输出打字机效果
                        consumer.accept(messageType.getStreamingPrefix() + chunk);
                    }
                })
                .doOnError(e -> {
                    log.error("LLM 流式调用失败, messageType={},e={}", messageType, e.getMessage());
                })
                .blockLast();

        return outlineContent.toString();
    }
}
