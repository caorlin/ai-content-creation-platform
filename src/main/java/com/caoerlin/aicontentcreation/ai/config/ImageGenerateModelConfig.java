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

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.image-generate-model")
public class ImageGenerateModelConfig {
    private String apiKey;
    private String model;

    @Bean
    public ChatModel imageGenerateChatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model(model)
                .build();
        return DashScopeChatModel.builder().dashScopeApi(dashScopeApi)
                .defaultOptions(chatOptions)
                .build();
    }
}
