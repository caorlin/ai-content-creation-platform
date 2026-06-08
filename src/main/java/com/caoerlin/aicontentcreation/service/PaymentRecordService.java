package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.entity.PaymentRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【payment_record(支付记录表)】的数据库操作Service
 * @createDate 2026-06-08 17:07:27
 */
public interface PaymentRecordService extends IService<PaymentRecord> {
    /**
     * 创建永久会员支付回话
     *
     * @param userId 用户id
     * @return Stripe Checkout Session URL
     */
    String createVipPaymentSession(Long userId) throws StripeException;

    /**
     * 处理支付成功会话
     *
     * @param session 支付成功会话
     */
    void handlePaymentSuccess(Session session);

    /**
     * 处理退款
     *
     * @param userId 用户id
     * @param reason 退款原因
     * @return 是否退款成功
     */
    boolean handleRefund(Long userId, String reason) throws StripeException;

    /**
     * 验证 webhook 签名
     *
     * @param payload   请求体
     * @param sigHeader 签名头
     * @return stripe event
     */
    Event constructEvent(String payload, String sigHeader) throws Exception;

    /**
     * 获取用户支付列表
     *
     * @param userId 用户id
     * @return 用户支付列表
     */
    List<PaymentRecord> getPaymentList(Long userId);
}
