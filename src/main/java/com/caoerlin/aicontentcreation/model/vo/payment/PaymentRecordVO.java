package com.caoerlin.aicontentcreation.model.vo.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户支付详情列表模型")
public class PaymentRecordVO implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 用户ID
     */
    @Schema(description = "主键")
    private Long userId;

    /**
     * Stripe Checkout Session ID
     */
    @Schema(description = "Stripe Checkout Session ID")
    private String stripeSessionId;

    /**
     * Stripe 支付意向ID
     */
    @Schema(description = "Stripe 支付意向ID")
    private String stripePaymentIntentId;

    /**
     * 金额（美元）
     */
    @Schema(description = "金额（美元）")
    private BigDecimal amount;

    /**
     * 货币
     */
    @Schema(description = "货币")
    private String currency;

    /**
     * 状态：PENDING/SUCCEEDED/FAILED/REFUNDED
     */
    @Schema(description = "状态", examples = {"PENDING", "SUCCEEDED", "FAILED", "REFUNDED"})
    private String status;

    /**
     * 产品类型：VIP_PERMANENT
     */
    @Schema(description = "产品类型", example = "VIP_PERMANENT")
    private String productType;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 退款时间
     */
    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    @Schema(description = "退款原因")
    private String refundReason;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
