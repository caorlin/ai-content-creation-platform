<template>
  <div id="articleDetailPage">
    <!-- 加载中 -->
    <a-spin v-if="loading" size="large" class="loading-spin" tip="加载中..." />

    <!-- 文章内容 -->
    <template v-else-if="article">
      <div class="top-bar">
        <a-button type="link" @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回列表
        </a-button>
        <a-button type="primary" @click="doExportMarkdown">
          <template #icon><DownloadOutlined /></template>
          导出 Markdown
        </a-button>
      </div>

      <!-- 头部信息 -->
      <div class="article-header">
        <h1 class="main-title">{{ article.mainTitle || '-' }}</h1>
        <p v-if="article.subTitle" class="sub-title">{{ article.subTitle }}</p>
        <div class="meta-row">
          <a-tag :color="getStatusColor(article.status)" bordered class="status-tag">
            {{ getStatusLabel(article.status) }}
          </a-tag>
          <span class="completed-time">
            <ClockCircleOutlined />
            {{ article.completedTime || '-' }}
          </span>
        </div>
      </div>

      <a-divider />

      <!-- 执行日志面板 -->
      <div
        v-if="executionStats && executionStats.logs && executionStats.logs.length > 0"
        class="execution-logs-section"
      >
        <div class="logs-header" @click="showExecutionLogs = !showExecutionLogs">
          <div class="logs-header-left">
            <div class="logs-icon-wrapper">
              <ThunderboltOutlined />
            </div>
            <div class="logs-title-group">
              <span class="logs-title">执行日志</span>
              <span class="logs-subtitle">智能体协作详情</span>
            </div>
            <a-tag
              :color="getStatusColor(executionStats.overallStatus ?? '')"
              class="status-tag-small"
            >
              {{ executionStats.overallStatus ?? '' }}
            </a-tag>
          </div>
          <span class="logs-header-right">
            <span v-if="executionStats.totalDurationMs" class="total-duration-badge">
              <ClockCircleOutlined /> {{ executionStats.totalDurationMs }}ms
            </span>
            <CaretDownOutlined :class="['toggle-icon', { expanded: showExecutionLogs }]" />
          </span>
        </div>

        <Transition name="expand">
          <div v-show="showExecutionLogs" class="logs-content">
            <!-- 统计概览 -->
            <div class="stats-cards">
              <div class="stat-card">
                <div class="stat-card-icon">
                  <ClockCircleOutlined />
                </div>
                <div class="stat-card-body">
                  <span class="stat-card-label">总耗时</span>
                  <span class="stat-card-value"
                    >{{ executionStats.totalDurationMs ?? 0 }}<small>ms</small></span
                  >
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-card-icon">
                  <RobotOutlined />
                </div>
                <div class="stat-card-body">
                  <span class="stat-card-label">智能体数量</span>
                  <span class="stat-card-value"
                    >{{ executionStats.agentCount ?? 0 }}<small> 个</small></span
                  >
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-card-icon">
                  <ThunderboltOutlined />
                </div>
                <div class="stat-card-body">
                  <span class="stat-card-label">平均耗时</span>
                  <span class="stat-card-value">
                    {{
                      executionStats.agentCount && executionStats.totalDurationMs
                        ? Math.round(executionStats.totalDurationMs / executionStats.agentCount)
                        : 0
                    }}<small>ms</small>
                  </span>
                </div>
              </div>
            </div>

            <!-- 智能体时间线 -->
            <div class="agent-timeline-wrapper">
              <div class="agent-timeline">
                <div
                  v-for="(log, index) in executionStats.logs"
                  :key="log.id"
                  :class="[
                    'timeline-item',
                    log.status?.toLowerCase(),
                    { first: index === 0, last: index === executionStats.logs.length - 1 },
                  ]"
                >
                  <div class="timeline-line">
                    <div class="timeline-dot">
                      <CheckCircleOutlined v-if="log.status === 'SUCCESS'" />
                      <CloseCircleOutlined v-else-if="log.status === 'FAILED'" />
                      <LoadingOutlined v-else spin />
                    </div>
                  </div>
                  <div class="timeline-card">
                    <div class="timeline-card-header">
                      <span class="agent-name">{{ getAgentDisplayName(log.agentName ?? '') }}</span>
                      <a-tag
                        :color="
                          log.status === 'SUCCESS'
                            ? 'success'
                            : log.status === 'FAILED'
                              ? 'error'
                              : 'processing'
                        "
                        size="small"
                      >
                        {{
                          log.status === 'SUCCESS'
                            ? '成功'
                            : log.status === 'FAILED'
                              ? '失败'
                              : '执行中'
                        }}
                      </a-tag>
                    </div>
                    <div class="timeline-card-meta">
                      <span class="timeline-time">
                        <ClockCircleOutlined />
                        {{ log.startTime ? formatDate(log.startTime) : '--' }}
                      </span>
                      <span class="duration">{{ log.durationMs ?? 0 }}ms</span>
                    </div>
                    <div v-if="log.errorMessage" class="error-message">
                      <CloseCircleOutlined /> {{ log.errorMessage }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <!-- 大纲 -->
      <div class="outline-section">
        <h2 class="section-title">
          <OrderedListOutlined class="section-icon" />
          文章大纲
        </h2>
        <div v-if="parsedOutline && parsedOutline.length > 0" class="outline-list">
          <div v-for="item in parsedOutline" :key="item.section" class="outline-code-block">
            <div class="outline-code-header">
              <span class="code-label">{{ item.section }}. {{ item.title }}</span>
            </div>
            <pre class="outline-code-body"><code>{{ formatOutlinePoints(item.points) }}</code></pre>
          </div>
        </div>
        <a-empty v-else description="暂无大纲数据" :image-style="{ height: '60px' }" />
      </div>

      <!-- 配图 -->
      <div v-if="parsedImages && parsedImages.length > 0" class="images-section">
        <h2 class="section-title">
          <PictureOutlined class="section-icon" />
          文章配图
        </h2>
        <div class="images-grid">
          <div v-for="(img, idx) in parsedImages" :key="idx" class="image-item">
            <a-image :src="img" :alt="`配图 ${idx + 1}`" class="article-image" />
          </div>
        </div>
      </div>

      <!-- 完整图文（优先展示） -->
      <div v-if="article.fullContent" class="content-section">
        <h2 class="section-title">
          <FileTextOutlined class="section-icon" />
          完整图文
        </h2>
        <div v-html="markdownToHtml(article.fullContent)" class="markdown-content"></div>
      </div>

      <!-- 无 fullContent 时整合展示 -->
      <div v-else class="content-section">
        <h2 class="section-title">
          <FileTextOutlined class="section-icon" />
          文章正文
        </h2>
        <!-- 整合大纲 + 配图 + 正文 -->
        <div class="integrated-content">
          <div v-if="parsedOutline && parsedOutline.length > 0" class="integrated-outline">
            <h3>文章大纲</h3>
            <div v-for="item in parsedOutline" :key="item.section" class="integrated-outline-item">
              <strong>{{ item.section }}. {{ item.title }}</strong>
              <ul>
                <li v-for="(point, idx) in item.points" :key="idx">{{ point }}</li>
              </ul>
            </div>
          </div>
          <div v-if="parsedImages && parsedImages.length > 0" class="integrated-images">
            <h3>文章配图</h3>
            <div class="integrated-images-grid">
              <div v-for="(img, idx) in parsedImages" :key="idx" class="integrated-image-item">
                <a-image :src="img" :alt="`配图 ${idx + 1}`" />
              </div>
            </div>
          </div>
          <div
            v-if="article.content"
            v-html="markdownToHtml(article.content)"
            class="markdown-content"
          ></div>
        </div>
      </div>
    </template>

    <!-- 文章不存在 -->
    <a-result
      v-else-if="!loading"
      status="404"
      title="文章未找到"
      sub-title="请检查文章标识是否正确"
    >
      <template #extra>
        <a-button type="primary" @click="goBack">返回列表</a-button>
      </template>
    </a-result>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CaretDownOutlined,
  DownloadOutlined,
  OrderedListOutlined,
  FileTextOutlined,
  PictureOutlined,
  RobotOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { getArticle, getExecutionLogs } from '@/api/articleController.ts'
import { markdownToHtml } from '@/utils/markdown.ts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const article = ref<API.ArticleVO | null>(null)
const loading = ref(false)

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: 'default' },
  GENERATING: { label: '生成中', color: 'blue' },
  COMPLETED: { label: '已完成', color: 'green' },
  FAILED: { label: '执行失败', color: 'red' },
  SUCCESS: { label: '执行成功', color: 'green' },
  RUNNING: { label: '执行中', color: 'blue' },
}

const getStatusLabel = (status?: string) => statusMap[status ?? '']?.label ?? (status || '-')
const getStatusColor = (status?: string) => statusMap[status ?? '']?.color ?? 'default'

const executionStats = ref<API.AgentExecutionStats | null>(null)
const logsLoading = ref(false)
const showExecutionLogs = ref(false)

// 加载执行日志
const loadExecutionLogs = async (taskId: string) => {
  logsLoading.value = true
  try {
    const res = await getExecutionLogs({ taskId })
    executionStats.value = res.data.data || null
  } catch (error) {
    console.error('加载执行日志失败:', error)
  } finally {
    logsLoading.value = false
  }
}

// 获取智能体显示名称
const getAgentDisplayName = (agentName: string) => {
  const nameMap: Record<string, string> = {
    ARTICLE_TITLE_AGENT: '生成标题',
    ARTICLE_OUTLINE_AGENT: '生成大纲',
    ARTICLE_CONTENT_AGENT: '生成正文',
    ARTICLE_IMAGE_REQUIREMENTS_AGENT: '分析配图需求',
    ARTICLE_IMAGE_GENERATE_AGENT: '生成配图',
    ARTICLE_MERGE_AGENT: '图文合成',
    AI_MODIFY_OUTLINE_AGENT: 'AI修改大纲',
  }
  return nameMap[agentName] || agentName
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 安全解析 JSON 字符串
 */
const safeJsonParse = (value: unknown): unknown => {
  if (!value) return null
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

// 大纲项类型
interface OutlineItem {
  title: string
  points: string[]
  section: number
}

/** 解析后的 outline，兼容后端返回 JSON 字符串 */
const parsedOutline = computed<OutlineItem[]>(() => {
  const raw = article.value?.outline
  if (!raw) return []
  const parsed = safeJsonParse(raw)
  return Array.isArray(parsed) ? (parsed as OutlineItem[]) : []
})

/** 解析后的 images，兼容后端返回 JSON 字符串 */
const parsedImages = computed<string[]>(() => {
  const raw = article.value?.images
  if (!raw) return []
  const parsed = safeJsonParse(raw)
  if (!Array.isArray(parsed)) return []
  return parsed
    .map((item: unknown) => {
      if (typeof item === 'string') return item
      if (item && typeof item === 'object' && 'url' in (item as Record<string, unknown>))
        return (item as Record<string, unknown>).url as string
      return ''
    })
    .filter(Boolean)
})

const formatOutlinePoints = (points?: string[]): string => {
  if (!points || points.length === 0) return ''
  return points.map((p) => `- ${p}`).join('\n')
}

const fetchArticle = async () => {
  const taskId = route.params.taskId as string
  if (!taskId) {
    message.error('缺少文章标识')
    return
  }
  loading.value = true
  try {
    const res = await getArticle({ taskId })
    await loadExecutionLogs(taskId)
    if (res.data.code === 0 && res.data.data) {
      article.value = res.data.data
    } else {
      message.error('获取文章详情失败：' + res.data.message)
    }
  } catch {
    message.error('请求失败，请检查网络')
  } finally {
    loading.value = false
  }
}

const buildExportMarkdown = (): string => {
  if (!article.value) return ''
  if (article.value.fullContent) return article.value.fullContent

  const lines: string[] = []
  lines.push(`# ${article.value.mainTitle || ''}`)
  if (article.value.subTitle) lines.push(`> ${article.value.subTitle}`)
  lines.push('')

  if (parsedOutline.value.length > 0) {
    lines.push('## 文章大纲')
    lines.push('')
    for (const item of parsedOutline.value) {
      lines.push(`### ${item.section}. ${item.title}`)
      for (const point of item.points || []) {
        lines.push(`- ${point}`)
      }
      lines.push('')
    }
  }

  if (parsedImages.value.length > 0) {
    lines.push('## 文章配图')
    lines.push('')
    for (const url of parsedImages.value) {
      lines.push(`![](${url})`)
      lines.push('')
    }
  }

  if (article.value.content) {
    lines.push('## 文章正文')
    lines.push('')
    lines.push(article.value.content)
  }

  return lines.join('\n')
}

const doExportMarkdown = () => {
  const md = buildExportMarkdown()
  if (!md) {
    message.warning('暂无可导出的内容')
    return
  }
  const blob = new Blob(['\uFEFF' + md], { type: 'text/markdown;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${article.value?.mainTitle || '文章'}.md`
  link.click()
  URL.revokeObjectURL(url)
  message.success('导出成功')
}

const goBack = () => {
  router.push({ name: '创作历史' })
}

onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
#articleDetailPage {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
  background: #fff;
  border-radius: 8px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.loading-spin {
  display: flex;
  justify-content: center;
  padding: 120px 0;
}

/* 头部 */
.article-header {
  text-align: center;
  padding: 20px 0 8px;
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px;
  line-height: 1.4;
}

.sub-title {
  font-size: 18px;
  color: #666;
  margin: 0 0 16px;
  line-height: 1.5;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
}

.status-tag {
  font-size: 13px;
}

.completed-time {
  color: #999;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 大纲 */
.outline-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  font-size: 20px;
  color: #1677ff;
}

.outline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.outline-code-block {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.outline-code-header {
  background: #f5f5f5;
  padding: 8px 16px;
  border-bottom: 1px solid #e8e8e8;
  font-size: 13px;
  color: #666;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

.code-label {
  color: #1677ff;
  font-weight: 500;
}

.outline-code-body {
  margin: 0;
  padding: 16px 20px;
  background: #fff;
  overflow-x: auto;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.outline-code-body code {
  font-family: inherit;
  color: inherit;
}

/* 配图 */
.images-section {
  margin-bottom: 32px;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.image-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.article-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

/* 内容 */
.content-section {
  margin-bottom: 32px;
}

.markdown-content {
  line-height: 1.8;
  color: #333;
  font-size: 15px;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  color: #1a1a2e;
}

.markdown-content :deep(p) {
  margin-bottom: 12px;
}

.markdown-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}

.markdown-content :deep(pre) {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
}

.markdown-content :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #1677ff;
  padding-left: 16px;
  color: #666;
  margin: 12px 0;
}

/* 整合内容 */
.integrated-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 24px 0 12px;
}

.integrated-outline {
  margin-bottom: 24px;
}

.integrated-outline-item {
  margin-bottom: 12px;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 3px solid #1677ff;
}

.integrated-outline-item strong {
  display: block;
  margin-bottom: 6px;
  color: #1a1a2e;
}

.integrated-outline-item ul {
  margin: 0;
  padding-left: 20px;
}

.integrated-outline-item li {
  margin-bottom: 2px;
  color: #555;
  font-size: 14px;
}

.integrated-images {
  margin-bottom: 24px;
}

.integrated-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.integrated-image-item {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

/* ========== 执行日志面板 ========== */
.execution-logs-section {
  margin-bottom: 32px;
  background: var(--color-background-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-normal);

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

.logs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  cursor: pointer;
  user-select: none;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.04) 0%, rgba(255, 255, 255, 0) 100%);
  transition: background var(--transition-fast);

  &:hover {
    background: linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, rgba(255, 255, 255, 0) 100%);
  }

  &:active {
    background: linear-gradient(135deg, rgba(14, 165, 233, 0.12) 0%, rgba(255, 255, 255, 0) 100%);
  }
}

.logs-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logs-icon-wrapper {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  color: #fff;
  font-size: 20px;
  box-shadow: var(--shadow-primary);
  flex-shrink: 0;
}

.logs-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logs-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text);
  font-family: 'Outfit', sans-serif;
  line-height: 1.2;
}

.logs-subtitle {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1;
}

.status-tag-small {
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.logs-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.total-duration-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  white-space: nowrap;
}

.toggle-icon {
  font-size: 16px;
  color: var(--color-text-muted);
  transition: transform var(--transition-normal);

  &.expanded {
    transform: rotate(180deg);
  }
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 20px 24px 0;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--color-background);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);

  &:hover {
    border-color: var(--color-primary-light);
    box-shadow: var(--shadow-card-hover);
    transform: translateY(-2px);
  }
}

.stat-card-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(14, 165, 233, 0.1);
  border-radius: var(--radius-md);
  color: var(--color-primary);
  font-size: 18px;
  flex-shrink: 0;
}

.stat-card-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.stat-card-label {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-card-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  font-family: 'Outfit', sans-serif;
  line-height: 1;

  small {
    font-size: 12px;
    font-weight: 500;
    color: var(--color-text-muted);
    margin-left: 2px;
  }
}

/* 智能体时间线 */
.agent-timeline-wrapper {
  padding: 24px 24px 12px;
}

.agent-timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 16px;
  position: relative;

  &:not(.last) {
    padding-bottom: 20px;
  }
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 32px;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 32px;
    bottom: -20px;
    left: 50%;
    transform: translateX(-50%);
    width: 2px;
    background: var(--color-border);
  }
}

.timeline-item.last .timeline-line::after {
  display: none;
}

.timeline-item.success .timeline-line::after {
  background: linear-gradient(to bottom, #22c55e, var(--color-border));
}

.timeline-item.failed .timeline-line::after {
  background: linear-gradient(to bottom, var(--color-error), var(--color-border));
}

.timeline-dot {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 16px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  background: var(--color-background);
  border: 2px solid var(--color-border);
  color: var(--color-text-muted);
  transition: all var(--transition-normal);
}

.timeline-item.success .timeline-dot {
  background: #f0fdf4;
  border-color: #22c55e;
  color: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.1);
}

.timeline-item.failed .timeline-dot {
  background: #fef2f2;
  border-color: var(--color-error);
  color: var(--color-error);
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
}

.timeline-item.running .timeline-dot {
  background: #eff6ff;
  border-color: var(--color-primary);
  color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.1);
}

.timeline-card {
  flex: 1;
  min-width: 0;
  padding: 14px 16px;
  background: var(--color-background);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);

  &:hover {
    box-shadow: var(--shadow-card-hover);
  }
}

.timeline-item.success .timeline-card {
  border-left: 3px solid #22c55e;
}

.timeline-item.failed .timeline-card {
  border-left: 3px solid var(--color-error);
}

.timeline-item.running .timeline-card {
  border-left: 3px solid var(--color-primary);
}

.timeline-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.agent-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.timeline-card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-muted);
}

.timeline-time {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-text-muted);
}

.duration {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary);
  background: rgba(14, 165, 233, 0.08);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}

.error-message {
  margin-top: 10px;
  padding: 10px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: var(--radius-sm);
  color: var(--color-error);
  font-size: 13px;
  line-height: 1.5;
  display: flex;
  align-items: flex-start;
  gap: 8px;

  .anticon {
    margin-top: 2px;
    flex-shrink: 0;
  }
}

/* 内容区域 */
.logs-content {
  border-top: 1px solid var(--color-border-light);
  background: linear-gradient(180deg, rgba(14, 165, 233, 0.02) 0%, transparent 100%);
}

/* 展开/收起过渡动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-8px);
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 2000px;
  transform: translateY(0);
}
</style>
