package com.caoerlin.aicontentcreation.ai.utils;

import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
public class AiModelCallingUtils {

    public static String callModelWithStreaming(ChatModel chatModel, String prompt, Consumer<String> consumer, SseMessageTypeEnum messageType) {
        //大纲内容
        StringBuilder outlineContent = new StringBuilder();

        //调用模型流式输出大纲
        Flux<ChatResponse> chatResponseFlux = chatModel.stream(new Prompt(new UserMessage(prompt)));

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
