package com.caoerlin.aicontentcreation.ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.article-title-agent-model")
public class ArticleTitleModelConfig {
    private String apiKey;
    private String model;
    private Integer maxTokens;

    @Bean
    public ChatModel articleTitleChatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model("qwen-turbo")
                .maxToken(maxTokens)
                .build();
        return DashScopeChatModel.builder().dashScopeApi(dashScopeApi)
                .defaultOptions(chatOptions)
                .build();
    }
}
