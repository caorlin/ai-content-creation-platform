package com.caoerlin.aicontentcreation.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("stripe")
public class StripeConfig {
    /**
     * stripe 支付密钥
     */
    private String apiKey;

    /**
     * webhook 签名密钥
     */
    private String webhookSecret;

    /**
     * 支付成功回调地址
     */
    private String successUrl;

    /**
     * 支付取消回调地址
     */
    private String cancelUrl;

    @PostConstruct
    public void setStripeApiKey() {
        Stripe.apiKey = this.apiKey;
    }
}
