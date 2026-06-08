package com.caoerlin.aicontentcreation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 支付记录表
 * @TableName payment_record
 */
@TableName(value ="payment_record")
@Data
public class PaymentRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Stripe Checkout Session ID
     */
    private String stripeSessionId;

    /**
     * Stripe 支付意向ID
     */
    private String stripePaymentIntentId;

    /**
     * 金额（美元）
     */
    private BigDecimal amount;

    /**
     * 货币
     */
    private String currency;

    /**
     * 状态：PENDING/SUCCEEDED/FAILED/REFUNDED
     */
    private String status;

    /**
     * 产品类型：VIP_PERMANENT
     */
    private String productType;

    /**
     * 描述
     */
    private String description;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}