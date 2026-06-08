<template>
  <div id="vipPage">
    <!-- ==================== Hero 标题引导区 ==================== -->
    <div class="hero-section">
      <div class="container">
        <div class="hero-badge">
          <CrownOutlined />
          <span>VIP 会员</span>
        </div>
        <h1 class="hero-title">升级 VIP，解锁全部创作能力</h1>
        <p class="hero-subtitle">一次购买，永久使用，享受无限 AI 创作体验</p>
      </div>
    </div>

    <!-- ==================== 主体内容区：左侧价格卡片 + 右侧特权列表 ==================== -->
    <div class="main-section">
      <div class="container">
        <div class="main-grid">
          <!-- 左侧：价格卡片 -->
          <div class="pricing-column">
            <div class="price-card">
              <div class="price-card-header">
                <h3 class="plan-name">永久会员</h3>
                <p class="plan-desc">一次购买，终身畅享全部特权</p>
              </div>
              <div class="price-card-body">
                <div class="price">
                  <span class="price-symbol">¥</span>
                  <span class="price-amount">199</span>
                  <span class="price-unit">/永久</span>
                </div>
                <a-tag color="blue" class="discount-tag">限时特惠</a-tag>
                <ul class="plan-features">
                  <li><CheckCircleOutlined class="check-icon" /> 无限创作配额</li>
                  <li><CheckCircleOutlined class="check-icon" /> AI 智能生图</li>
                  <li><CheckCircleOutlined class="check-icon" /> SVG 图表生成</li>
                  <li><CheckCircleOutlined class="check-icon" /> AI 大纲编辑</li>
                  <li><CheckCircleOutlined class="check-icon" /> 优先队列处理</li>
                  <li><CheckCircleOutlined class="check-icon" /> 终身有效</li>
                </ul>
              </div>
              <div class="price-card-footer">
                <a-button
                  type="primary"
                  size="large"
                  block
                  :loading="loadingBuy"
                  :disabled="isVip"
                  @click="handleBuy"
                >
                  <CrownOutlined /> {{ isVip ? '您已是永久会员' : '立即升级' }}
                </a-button>
                <p v-if="!isVip" class="guarantee-text">
                  <SafetyCertificateOutlined /> 7天无忧退款
                </p>
              </div>
            </div>
          </div>

          <!-- 右侧：会员特权列表 -->
          <div class="privileges-card">
            <div class="privileges-header">
              <h3 class="privileges-title"><GiftOutlined /> 会员专属特权</h3>
              <p class="privileges-subtitle">对比免费版，VIP 会员拥有以下专属能力</p>
            </div>

            <div class="compare-table">
              <div class="compare-row compare-row-header">
                <div class="compare-cell compare-cell-feature">功能特性</div>
                <div class="compare-cell compare-cell-plan">
                  <span class="plan-label free-label">免费版</span>
                </div>
                <div class="compare-cell compare-cell-plan">
                  <span class="plan-label vip-label"> <CrownOutlined /> VIP </span>
                </div>
              </div>

              <div v-for="(item, index) in privilegeList" :key="index" class="compare-row">
                <div class="compare-cell compare-cell-feature">
                  <span class="feature-name">{{ item.name }}</span>
                  <a-tooltip v-if="item.tip" :title="item.tip">
                    <QuestionCircleOutlined class="tip-icon" />
                  </a-tooltip>
                </div>
                <div class="compare-cell compare-cell-plan">
                  <span v-if="item.free === true">
                    <CheckOutlined class="available-icon free-available" />
                  </span>
                  <span v-else-if="typeof item.free === 'string'" class="limit-text">
                    {{ item.free }}
                  </span>
                  <span v-else>
                    <CloseOutlined class="unavailable-icon" />
                  </span>
                </div>
                <div class="compare-cell compare-cell-plan">
                  <span v-if="item.vip === true">
                    <CheckOutlined class="available-icon vip-available" />
                  </span>
                  <span v-else-if="typeof item.vip === 'string'" class="limit-text vip-text">
                    {{ item.vip }}
                  </span>
                  <span v-else>
                    <CloseOutlined class="unavailable-icon" />
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 底部：常见问题 ==================== -->
    <div class="faq-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">常见问题</h2>
          <p class="section-subtitle">关于 VIP 会员的常见疑问，这里都有答案</p>
        </div>
        <div class="faq-list">
          <a-collapse :bordered="false" :expand-icon-position="'end'">
            <a-collapse-panel v-for="(faq, index) in faqList" :key="index" :header="faq.question">
              <p class="faq-answer">{{ faq.answer }}</p>
            </a-collapse-panel>
          </a-collapse>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser'
import { createVipPaymentRecord } from '@/api/paymentController'
import {
  CrownOutlined,
  CheckCircleOutlined,
  SafetyCertificateOutlined,
  GiftOutlined,
  CheckOutlined,
  CloseOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons-vue'
import { USER_ROLE_VIP } from '@/constant/user.ts'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const loadingBuy = ref(false)

/** 判断当前用户是否为 VIP */
const isVip = computed(() => {
  return loginUserStore.loginUser.userRole === USER_ROLE_VIP
})

const privilegeList = [
  {
    name: '创作配额',
    free: '每日有限次',
    vip: '无限创作配额',
    tip: '包括标题生成、大纲生成、正文创作等全部 AI 功能',
  },
  {
    name: 'AI 智能生图',
    free: false,
    vip: true,
    tip: '自动检索高质量无版权图片，完美匹配内容',
  },
  {
    name: 'SVG 图表生成',
    free: false,
    vip: true,
    tip: '一键生成数据可视化图表、流程图、架构图',
  },
  {
    name: 'AI 大纲编辑',
    free: '基础大纲',
    vip: 'AI 智能大纲编辑',
    tip: '智能规划文章结构，支持自由调整和优化',
  },
  {
    name: '处理优先级',
    free: '普通队列',
    vip: '优先队列',
    tip: '创作请求优先处理，响应速度更快',
  },
  {
    name: '有效期',
    free: '--',
    vip: '终身有效',
    tip: '一次购买，永久享有全部会员特权',
  },
]

const faqList = [
  {
    question: 'VIP 会员支持哪些支付方式？',
    answer:
      '目前支持微信支付、支付宝以及国际信用卡（Visa、Mastercard）等主流支付方式，通过 Stripe 安全支付网关完成交易。',
  },
  {
    question: '购买后可以退款吗？',
    answer: '永久会员支持 7 天内无忧退款。退款后会员权益将立即失效，已使用的创作额度不予退还。',
  },
  {
    question: 'VIP 会员到期后会怎样？',
    answer: '永久会员一次购买终身有效，无需续费，不会过期。',
  },
  {
    question: 'VIP 会员可以在多台设备上使用吗？',
    answer: '可以。VIP 会员权益与您的账号绑定，您可以在任意设备上登录使用，无需重复购买。',
  },
  {
    question: '如何查看我的会员状态？',
    answer: '登录后，您可以在个人中心页面查看会员状态和消费记录。如有疑问可随时联系客服。',
  },
]

/** 页面加载时处理 Stripe 支付回调 */
onMounted(async () => {
  const { success, cancel } = route.query
  if (success === 'true') {
    await loginUserStore.fetchLoginUser()
    message.success('支付成功！您已是永久会员，尽情享受全部特权吧')
  } else if (cancel === 'true') {
    message.info('支付已取消')
  }
  // 清除 URL 参数，保持地址栏整洁
  if (success || cancel) {
    router.replace({ path: '/vip' })
  }
})

/** 购买处理 */
const handleBuy = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录后再购买 VIP')
    router.push({ path: '/user/login', query: { redirect: '/vip' } })
    return
  }

  loadingBuy.value = true

  try {
    const res = await createVipPaymentRecord({ params: { productType: 'permanent' } })
    if (res.data.code === 0 && res.data.data) {
      window.location.href = res.data.data
    } else {
      message.error('创建支付订单失败：' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('支付请求异常，请稍后重试')
    console.error('VIP 购买失败:', error)
  } finally {
    loadingBuy.value = false
  }
}
</script>

<style scoped>
/* ==================== Hero 标题引导区 ==================== */
.hero-section {
  padding: 72px 20px 64px;
  text-align: center;
  background: var(--gradient-hero);
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: var(--shadow-primary);
}

.hero-title {
  font-size: 40px;
  font-weight: 800;
  margin: 0 0 12px;
  color: var(--color-text);
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.6;
}

/* ==================== 主体内容区 ==================== */
.main-section {
  padding: 0 20px 60px;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: start;
}

/* ==================== 价格卡片 ==================== */
.pricing-column {
  display: flex;
  justify-content: center;
}

.price-card {
  background: #fff;
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  padding: 36px 32px;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-card);
  width: 100%;
  max-width: 400px;
}

.price-card:hover {
  border-color: var(--color-primary-light);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.price-card-header {
  margin-bottom: 20px;
  text-align: center;
}

.plan-name {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--color-text);
}

.plan-desc {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

.price-card-body {
  margin-bottom: 28px;
  text-align: center;
}

.price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  margin-bottom: 10px;
}

.price-symbol {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
}

.price-amount {
  font-size: 48px;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;
}

.price-unit {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin-left: 2px;
}

.discount-tag {
  margin-bottom: 14px;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin: 16px 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}

.plan-features li {
  font-size: 14px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.4;
}

.check-icon {
  color: var(--color-primary);
  font-size: 15px;
  flex-shrink: 0;
}

.price-card-footer {
  text-align: center;
}

.price-card-footer :deep(.ant-btn-primary) {
  font-weight: 600;
  font-size: 16px;
  height: 48px;
  border-radius: var(--radius-md);
}

.guarantee-text {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ==================== 会员特权列表 ==================== */
.privileges-card {
  background: #fff;
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  padding: 28px;
  box-shadow: var(--shadow-card);
}

.privileges-header {
  margin-bottom: 24px;
}

.privileges-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 6px;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.privileges-subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

.compare-table {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.compare-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  border-bottom: 1px solid var(--color-border-light);
  transition: background var(--transition-normal);
}

.compare-row:last-child {
  border-bottom: none;
}

.compare-row:not(.compare-row-header):hover {
  background: var(--color-background-tertiary);
}

.compare-row-header {
  background: var(--color-background-secondary);
  font-weight: 600;
  font-size: 13px;
  color: var(--color-text);
}

.compare-cell {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.compare-cell-feature {
  color: var(--color-text);
  gap: 6px;
}

.compare-cell-plan {
  justify-content: center;
}

.feature-name {
  font-size: 14px;
}

.tip-icon {
  color: var(--color-text-muted);
  font-size: 13px;
  cursor: help;
}

.plan-label {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
}

.free-label {
  background: var(--color-background-tertiary);
  color: var(--color-text-secondary);
}

.vip-label {
  background: rgba(14, 165, 233, 0.1);
  color: var(--color-primary-dark);
  display: flex;
  align-items: center;
  gap: 4px;
}

.available-icon {
  font-size: 16px;
  font-weight: 700;
}

.free-available {
  color: var(--color-text-secondary);
}

.vip-available {
  color: var(--color-primary);
}

.unavailable-icon {
  color: var(--color-border);
  font-size: 16px;
}

.limit-text {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.vip-text {
  color: var(--color-primary-dark);
  font-weight: 600;
}

/* ==================== FAQ 常见问题 ==================== */
.faq-section {
  padding: 40px 20px 80px;
  background: var(--color-background-secondary);
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 10px;
  color: var(--color-text);
}

.section-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0;
}

.faq-list {
  max-width: 720px;
  margin: 0 auto;
  background: #fff;
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  padding: 8px;
  box-shadow: var(--shadow-card);
}

.faq-list :deep(.ant-collapse) {
  background: transparent;
}

.faq-list :deep(.ant-collapse-item) {
  border-bottom: 1px solid var(--color-border-light);
}

.faq-list :deep(.ant-collapse-item:last-child) {
  border-bottom: none;
}

.faq-list :deep(.ant-collapse-header) {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text);
  padding: 16px 8px !important;
}

.faq-list :deep(.ant-collapse-content-box) {
  padding: 0 8px 16px !important;
}

.faq-answer {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin: 0;
}

/* ==================== 响应式 ==================== */
@media (max-width: 992px) {
  .main-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 48px 16px 40px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .price-card {
    padding: 24px 20px;
  }

  .price-amount {
    font-size: 40px;
  }

  .privileges-card {
    padding: 20px;
  }

  .section-title {
    font-size: 22px;
  }

  .compare-row {
    grid-template-columns: 1.5fr 1fr 1fr;
  }

  .compare-cell {
    padding: 10px 8px;
    font-size: 12px;
  }
}
</style>
