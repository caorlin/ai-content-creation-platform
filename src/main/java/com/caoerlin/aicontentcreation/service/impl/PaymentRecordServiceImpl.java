package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.config.StripeConfig;
import com.caoerlin.aicontentcreation.constant.UserConstant;
import com.caoerlin.aicontentcreation.mapper.UserMapper;
import com.caoerlin.aicontentcreation.model.entity.PaymentRecord;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.PaymentStatusEnum;
import com.caoerlin.aicontentcreation.model.enums.ProductTypeEnum;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.service.PaymentRecordService;
import com.caoerlin.aicontentcreation.mapper.PaymentRecordMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【payment_record(支付记录表)】的数据库操作Service实现
 * @createDate 2026-06-08 17:07:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
        implements PaymentRecordService {
    private static final String CURRENCY_USD = "usd";
    private static final long CENTS_MULTIPLIER = 100L;

    private final StripeConfig stripeConfig;
    private final UserMapper userMapper;

    @Override
    public String createVipPaymentSession(Long userId) throws StripeException {
        User user = getUserOrThrow(userId);
        boolean hasVip = validatedHasVip(user);
        if (hasVip) {
            log.warn("用户已开通VIP,请勿重复购买,userId={}", userId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已开通VIP,请勿重复购买");
        }

        //创建支付会话
        ProductTypeEnum productType = ProductTypeEnum.VIP_PERMANENT;
        Session session = createStripeSession(userId, productType);

        //保持支付信息
        savePaymentRecord(userId, productType, session);

        log.info("创建支付会话成功,userId={},sessionId={}", userId, session.getId());
        return session.getUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(Session session) {
        String sessionId = session.getId();
        String userId = session.getMetadata().get("userId");
        String paymentIntentId = session.getPaymentIntent();

        //查询支付信息,根据支付sessionId获取
        PaymentRecord paymentRecord = getPaymentRecordBySessionId(sessionId);

        //幂等性检查
        if (PaymentStatusEnum.SUCCEEDED.getStatus().equals(paymentRecord.getStatus())) {
            log.info("订单记录已处理,sessionId={}", sessionId);
            return;
        }

        updatePaymentRecordStatus(paymentRecord.getId(), PaymentStatusEnum.SUCCEEDED.getStatus(), paymentIntentId);

        updateUserRoleToVip(Long.valueOf(userId));

        log.info("支付成功,用户：{}已升级为VIP用户,sessionId={}", userId, sessionId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long userId, String reason) throws StripeException {
        User user = getUserOrThrow(userId);
        boolean hasVip = validatedHasVip(user);
        if (!hasVip) {
            log.warn("用户未开通 VIP 操作失败,userId={}", userId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未开通 VIP 操作失败");
        }

        //查询订单信息
        PaymentRecord record = getLatestSuccessfulPayment(userId);
        if (ObjectUtil.isNull(record)) {
            log.error("支付记录不存在,userId={}", userId);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "支付记录不存在");
        }

        if (StrUtil.isBlank(record.getStripePaymentIntentId())) {
            log.error("支付记录无效,userId={}", userId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "支付记录无效");
        }

        Refund refund = createStripeRefund(record.getStripePaymentIntentId());

        if (!"succeeded".equals(refund.getStatus())) {
            return false;
        }

        updateRefundRecord(record.getId(), reason);

        revokeVipStatus(userId);

        log.info("退款成功,已取消用户:{}的VIP身份,refundId={}", userId, refund.getId());
        return true;
    }


    @Override
    public Event constructEvent(String payload, String sigHeader) throws Exception {
        return Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
    }


    @Override
    public List<PaymentRecord> getPaymentList(Long userId) {
        return List.of();
    }

    /**
     * 判断用户是否存在
     */
    private User getUserOrThrow(Long userId) {
        if (ObjectUtil.isEmpty(userId)) {
            log.error("用户id不能为空");
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户id不能为空");
        }

        User user = userMapper.selectById(userId);
        if (ObjectUtil.isNull(user)) {
            log.error("用户不存在,userId={}", userId);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    /**
     * 判断用户是否已是VIP
     *
     */
    private boolean validatedHasVip(User user) {
        return UserConstant.VIP_ROLE.equals(user.getUserRole());
    }

    /**
     * 创建 Stripe 支付会话
     */
    private Session createStripeSession(Long userId, ProductTypeEnum productType) throws StripeException {
        long amountInCents = productType.getPrice().multiply(new BigDecimal(CENTS_MULTIPLIER)).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl())
                .setCancelUrl(stripeConfig.getCancelUrl())
                .addLineItem(buildLineItem(productType, amountInCents))
                .putMetadata("userId", String.valueOf(userId))
                .putMetadata("productType", productType.getType())
                .build();

        return Session.create(params);
    }

    /**
     * 构建支付行项目
     */
    private SessionCreateParams.LineItem buildLineItem(ProductTypeEnum productType, long amountInCents) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(CURRENCY_USD)
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(productType.getDesc())
                                                .setDescription("解锁全部高级功能，无限创作配额，终身有效")
                                                .build()
                                )
                                .build()
                )
                .setQuantity(1L)
                .build();
    }

    /**
     * 保存支付信息
     */
    private void savePaymentRecord(Long userId, ProductTypeEnum productType, Session session) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setUserId(userId);
        paymentRecord.setStripeSessionId(session.getId());
        paymentRecord.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRecord.setAmount(new BigDecimal(session.getAmountTotal()));
        paymentRecord.setCurrency(CURRENCY_USD);
        paymentRecord.setStatus(PaymentStatusEnum.PENDING.getStatus());
        paymentRecord.setProductType(productType.getType());
        paymentRecord.setDescription(paymentRecord.getDescription());
        this.save(paymentRecord);
    }

    /**
     * 根据支付的SessionId获取支付信息
     */
    private PaymentRecord getPaymentRecordBySessionId(String sessionId) {
        if (StrUtil.isBlank(sessionId)) {
            log.error("支付SessionId不能为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付 SessionId 不能为空");
        }

        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getStripeSessionId, sessionId);
        PaymentRecord paymentRecord = getOne(wrapper);
        if (ObjectUtil.isNull(paymentRecord)) {
            log.error("订单不存在,sessionId={}", sessionId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单不存在");
        }
        return paymentRecord;
    }

    /**
     * 更新支付信息状态
     */
    private void updatePaymentRecordStatus(Long id, String status, String paymentIntentId) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setId(id);
        paymentRecord.setStatus(status);
        paymentRecord.setStripePaymentIntentId(paymentIntentId);
        updateById(paymentRecord);
    }

    /**
     *
     * @param userId
     */
    private void updateUserRoleToVip(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setVipTime(LocalDateTime.now());
        user.setUserRole(UserRoleEnum.VIP.getValue());
        userMapper.updateById(user);
    }

    /**
     * 查询最近的成功支付记录
     */
    private PaymentRecord getLatestSuccessfulPayment(Long userId) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getUserId, userId)
                .eq(PaymentRecord::getStatus, PaymentStatusEnum.SUCCEEDED.getStatus())
                .eq(PaymentRecord::getProductType, ProductTypeEnum.VIP_PERMANENT.getType())
                .orderBy(true, false, PaymentRecord::getCreateTime);
        return getOne(wrapper);
    }


    private Refund createStripeRefund(String stripePaymentIntentId) throws StripeException {
        RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                .setPaymentIntent(stripePaymentIntentId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
        return Refund.create(refundCreateParams);
    }

    /**
     * 更新退款记录
     */
    private void updateRefundRecord(Long id, String reason) {
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setId(id);
        paymentRecord.setRefundReason(reason);
        paymentRecord.setRefundTime(LocalDateTime.now());
        paymentRecord.setStatus(PaymentStatusEnum.REFUNDED.getStatus());
        updateById(paymentRecord);
    }

    private void revokeVipStatus(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setVipTime(null);
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        userMapper.updateById(user);
    }
}




