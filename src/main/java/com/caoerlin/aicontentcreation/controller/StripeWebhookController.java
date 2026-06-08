package com.caoerlin.aicontentcreation.controller;

import com.caoerlin.aicontentcreation.service.PaymentRecordService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe Webhook 控制器
 *
 */
@Slf4j
@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class StripeWebhookController {
    private final PaymentRecordService paymentRecordService;

    /**
     * 处理 Stripe Webhook 回调
     */
    @PostMapping("/stripe")
    public String handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            // 验证 Webhook 签名
            Event event = paymentRecordService.constructEvent(payload, sigHeader);
            
            log.info("收到 Stripe Webhook 事件, type={}", event.getType());
            
            // 处理事件
            switch (event.getType()) {
                case "checkout.session.completed":
                    // 支付成功
                    Session session = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("无法解析 Session 对象"));
                    paymentRecordService.handlePaymentSuccess(session);
                    break;
                    
                case "checkout.session.async_payment_succeeded":
                    // 异步支付成功
                    Session asyncSession = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("无法解析 Session 对象"));
                    paymentRecordService.handlePaymentSuccess(asyncSession);
                    break;
                    
                default:
                    log.info("未处理的事件类型: {}", event.getType());
                    break;
            }
            
            return "success";
        } catch (Exception e) {
            log.error("处理 Stripe Webhook 失败", e);
            return "error";
        }
    }
}
