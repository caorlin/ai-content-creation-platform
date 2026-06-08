-- 添加会员和支付功能

-- 1. 扩展 user 表，添加会员相关字段
ALTER TABLE user
    ADD COLUMN vip_time DATETIME NULL COMMENT '成为会员时间';

-- 2. 创建支付记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stripe_session_id VARCHAR(128) COMMENT 'Stripe Checkout Session ID',
    stripe_payment_intentId VARCHAR(128) COMMENT 'Stripe 支付意向ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额（美元）',
    currency VARCHAR(8) DEFAULT 'usd' COMMENT '货币',
    status VARCHAR(32) NOT NULL COMMENT '状态：PENDING/SUCCEEDED/FAILED/REFUNDED',
    product_type VARCHAR(32) NOT NULL COMMENT '产品类型：VIP_PERMANENT',
    description VARCHAR(256) COMMENT '描述',
    refund_time DATETIME NULL COMMENT '退款时间',
    refund_reason VARCHAR(512) NULL COMMENT '退款原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_id (user_id),
    INDEX idx_stripe_session_id (stripe_session_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
    ) COMMENT '支付记录表' COLLATE = utf8mb4_unicode_ci;
