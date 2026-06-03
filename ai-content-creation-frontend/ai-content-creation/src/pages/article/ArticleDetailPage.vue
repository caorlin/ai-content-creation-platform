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
          <div v-if="article.content" v-html="markdownToHtml(article.content)" class="markdown-content"></div>
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
  DownloadOutlined,
  OrderedListOutlined,
  FileTextOutlined,
  PictureOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { getArticle } from '@/api/articleController.ts'
import { markdownToHtml } from '@/utils/markdown.ts'

const route = useRoute()
const router = useRouter()

const article = ref<API.ArticleVO | null>(null)
const loading = ref(false)

const statusMap: Record<string, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: 'default' },
  GENERATING: { label: '生成中', color: 'blue' },
  COMPLETED: { label: '已完成', color: 'green' },
  FAILED: { label: '失败', color: 'red' },
}

const getStatusLabel = (status?: string) => statusMap[status ?? '']?.label ?? (status || '-')
const getStatusColor = (status?: string) => statusMap[status ?? '']?.color ?? 'default'

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

/** 解析后的 outline，兼容后端返回 JSON 字符串 */
const parsedOutline = computed<API.OutlineItem[]>(() => {
  const raw = article.value?.outline
  if (!raw) return []
  const parsed = safeJsonParse(raw)
  return Array.isArray(parsed) ? (parsed as API.OutlineItem[]) : []
})

/** 解析后的 images，兼容后端返回 JSON 字符串 */
const parsedImages = computed<string[]>(() => {
  const raw = article.value?.images
  if (!raw) return []
  const parsed = safeJsonParse(raw)
  if (!Array.isArray(parsed)) return []
  return parsed.map((item: unknown) => {
    if (typeof item === 'string') return item
    if (item && typeof item === 'object' && 'url' in (item as Record<string, unknown>))
      return (item as Record<string, unknown>).url as string
    return ''
  }).filter(Boolean)
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
</style>
