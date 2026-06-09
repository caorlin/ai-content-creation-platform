﻿<template>
  <div class="outline-editing-stage">
    <div class="stage-header">
      <h2 class="stage-title">编辑文章大纲</h2>
      <p class="stage-subtitle">您可以编辑、调整章节顺序，或添加新章节</p>
    </div>

    <div class="outline-list" ref="outlineListRef">
      <div
        v-for="(section, index) in outlineSections"
        :key="section.section"
        class="outline-section"
        :data-section-id="section.section"
      >
        <div class="section-header">
          <span class="drag-handle" title="拖动排序">⋮⋮</span>
          <span class="section-number">{{ index + 1 }}</span>
          <a-input
            v-model:value="section.title"
            placeholder="章节标题"
            class="section-title-input"
          />
          <a-button type="text" danger @click="deleteSection(index)" class="delete-btn">
            <template #icon>
              <DeleteOutlined />
            </template>
          </a-button>
        </div>

        <div class="section-points">
          <div v-for="(point, pointIdx) in section.points" :key="pointIdx" class="point-item">
            <span class="point-bullet">•</span>
            <a-input
              v-model:value="section.points[pointIdx]"
              placeholder="要点内容"
              class="point-input"
            />
            <a-button
              type="text"
              size="small"
              @click="deletePoint(index, pointIdx)"
              class="delete-point-btn"
            >
              ×
            </a-button>
          </div>

          <a-button type="dashed" @click="addPoint(index)" class="add-point-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            添加要点
          </a-button>
        </div>
      </div>
    </div>

    <!-- AI 修改大纲：VIP/管理员可用 -->
    <div v-if="isVipOrAdmin" class="ai-chat-section">
      <div class="chat-header">
        <RobotOutlined />
        <span>AI 助手修改大纲</span>
      </div>

      <div class="chat-input-wrapper">
        <a-textarea
          v-model:value="modifySuggestion"
          placeholder="告诉 AI 如何修改大纲，例如：请在第二章节后增加一个关于实践案例的章节"
          :rows="3"
          :maxlength="500"
          show-count
          class="chat-textarea"
        />
        <a-button
          type="primary"
          :loading="aiModifying"
          :disabled="!modifySuggestion.trim()"
          @click="handleAiModify"
          class="ai-modify-btn"
        >
          <template #icon>
            <RobotOutlined />
          </template>
          AI 修改大纲
        </a-button>
      </div>
    </div>

    <!-- 非VIP用户：升级提示卡片 -->
    <div v-else class="ai-upgrade-card">
      <div class="upgrade-card-header">
        <CrownOutlined class="upgrade-crown-icon" />
        <span>AI 修改大纲</span>
        <span class="upgrade-vip-tag">VIP</span>
      </div>
      <p class="upgrade-card-desc">AI 智能分析您的修改建议，自动优化大纲结构和内容，让创作更高效</p>
      <a-button type="primary" class="upgrade-card-btn" @click="router.push('/vip')">
        <CrownOutlined />
        升级会员解锁
      </a-button>
    </div>

    <div class="actions">
      <a-button size="large" @click="addSection" class="add-section-btn">
        <template #icon>
          <PlusOutlined />
        </template>
        添加章节
      </a-button>

      <a-button
        type="primary"
        size="large"
        :loading="loading"
        :disabled="!canConfirm"
        @click="handleConfirm"
        class="confirm-btn"
      >
        <template #icon>
          <CheckOutlined />
        </template>
        确认并生成正文
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import Sortable from 'sortablejs'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { USER_ROLE_ADMIN, USER_ROLE_VIP } from '@/constant/user'
import {
  CrownOutlined,
  RobotOutlined,
  DeleteOutlined,
  PlusOutlined,
  CheckOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { aiModifyOutline } from '@/api/articleController.ts'

interface OutlineSection {
  section: number
  title: string
  points: string[]
}

interface Props {
  outline: API.OutlineSection[]
  taskId: string
  loading?: boolean
}

interface Emits {
  (e: 'confirm', outline: OutlineSection[]): void
}

const router = useRouter()
const loginUserStore = useLoginUserStore()
const isVipOrAdmin = computed(() =>
  [USER_ROLE_ADMIN, USER_ROLE_VIP].includes(loginUserStore.loginUser.userRole || ''),
)

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

// 转换 API 类型为内部类型
const outlineSections = ref<OutlineSection[]>(
  props.outline.map((item, index) => ({
    section: item.section ?? index + 1,
    title: item.title ?? '',
    points: item.points ?? [],
  })),
)
const outlineListRef = ref<HTMLElement | null>(null)
const modifySuggestion = ref('')
const aiModifying = ref(false)

const canConfirm = computed(() => {
  return (
    outlineSections.value.length > 0 &&
    outlineSections.value.every(
      (section) =>
        section.title.trim() &&
        section.points.length > 0 &&
        section.points.every((point) => point.trim()),
    )
  )
})

onMounted(() => {
  nextTick(() => {
    if (outlineListRef.value) {
      Sortable.create(outlineListRef.value, {
        animation: 150,
        handle: '.drag-handle',
        onEnd: (evt) => {
          const { oldIndex, newIndex } = evt
          if (oldIndex !== undefined && newIndex !== undefined) {
            const item = outlineSections.value.splice(oldIndex, 1)[0]
            outlineSections.value.splice(newIndex, 0, item)
            // 更新 section 序号
            outlineSections.value.forEach((sec, idx) => {
              sec.section = idx + 1
            })
          }
        },
      })
    }
  })
})

const addSection = () => {
  const newSection: OutlineSection = {
    section: outlineSections.value.length + 1,
    title: '',
    points: [''],
  }
  outlineSections.value.push(newSection)
}

const deleteSection = (index: number) => {
  outlineSections.value.splice(index, 1)
  // 更新 section 序号
  outlineSections.value.forEach((sec, idx) => {
    sec.section = idx + 1
  })
}

const addPoint = (sectionIndex: number) => {
  outlineSections.value[sectionIndex].points.push('')
}

const deletePoint = (sectionIndex: number, pointIndex: number) => {
  const section = outlineSections.value[sectionIndex]
  if (section.points.length > 1) {
    section.points.splice(pointIndex, 1)
  }
}

const handleConfirm = () => {
  emit('confirm', outlineSections.value)
}

const handleAiModify = async () => {
  if (!modifySuggestion.value.trim()) {
    message.warning('请输入修改建议')
    return
  }

  aiModifying.value = true
  try {
    const res = await aiModifyOutline({
      taskId: props.taskId,
      modifySuggestion: modifySuggestion.value,
    })

    if (res.data.data) {
      outlineSections.value = res.data.data.map((item, index) => ({
        section: item.section ?? index + 1,
        title: item.title ?? '',
        points: item.points ?? [],
      }))
      modifySuggestion.value = ''
      message.success('AI 已根据您的建议修改大纲')
    }
  } catch (error) {
    const err = error as Error
    message.error(err.message || 'AI 修改失败')
  } finally {
    aiModifying.value = false
  }
}
</script>

<style scoped>
.outline-editing-stage {
  max-width: 760px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* Header */
.stage-header {
  text-align: center;
  margin-bottom: 32px;
}

.stage-title {
  font-family: 'Outfit', sans-serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.stage-subtitle {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.6;
}

/* Outline List */
.outline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.outline-section {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  transition: all var(--transition-normal);
}

.outline-section:hover {
  box-shadow: var(--shadow-card);
}

/* Section Header */
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.drag-handle {
  color: var(--color-text-muted);
  font-size: 18px;
  cursor: grab;
  user-select: none;
  line-height: 1;
  transition: color var(--transition-fast);
}

.drag-handle:active {
  cursor: grabbing;
}

.drag-handle:hover {
  color: var(--color-primary);
}

.section-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background-secondary);
  border-radius: var(--radius-sm);
  font-family: 'Outfit', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.section-title-input {
  flex: 1;
  border-radius: var(--radius-md);
  font-family: 'Outfit', sans-serif;
  font-weight: 500;
}

.delete-btn {
  flex-shrink: 0;
  opacity: 0.5;
  transition: opacity var(--transition-fast);
}

.delete-btn:hover {
  opacity: 1;
}

/* Section Points */
.section-points {
  padding-left: 48px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.point-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.point-bullet {
  color: var(--color-text-muted);
  font-size: 14px;
  flex-shrink: 0;
}

.point-input {
  flex: 1;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.delete-point-btn {
  flex-shrink: 0;
  color: var(--color-text-muted);
  opacity: 0.5;
  transition: opacity var(--transition-fast);
  font-size: 14px;
}

.delete-point-btn:hover {
  opacity: 1;
  color: var(--color-error);
}

.add-point-btn {
  border-radius: var(--radius-md);
  border-style: dashed;
  color: var(--color-text-muted);
  border-color: var(--color-border);
  transition: all var(--transition-fast);
  margin-top: 4px;
}

.add-point-btn:hover {
  color: var(--color-primary);
  border-color: var(--color-primary-light);
}

/* AI Chat Section */
.ai-chat-section {
  margin-top: 24px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Outfit', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 14px;
}

.chat-header .anticon {
  color: var(--color-primary);
  font-size: 18px;
}

.chat-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-textarea {
  border-radius: var(--radius-md);
}

.ai-modify-btn {
  align-self: flex-end;
  border-radius: var(--radius-md);
  font-weight: 500;
}

/* Actions */
.actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.add-section-btn {
  height: 46px;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  transition: all var(--transition-normal);
}

.add-section-btn:hover {
  border-color: var(--color-primary-light);
  color: var(--color-primary);
}

.confirm-btn {
  min-width: 200px;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
}

.confirm-btn:not(:disabled):hover {
  box-shadow: 0 6px 20px rgba(14, 165, 233, 0.35);
  transform: translateY(-1px);
}

/* AI 升级提示卡片 */
.ai-upgrade-card {
  margin-top: 24px;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.05) 0%, rgba(99, 102, 241, 0.05) 100%);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: var(--radius-lg);
  padding: 28px 24px;
  text-align: center;
}

.upgrade-card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}

.upgrade-crown-icon {
  color: var(--color-primary);
  font-size: 20px;
}

.upgrade-vip-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: white;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
}

.upgrade-card-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0 0 20px;
  max-width: 360px;
  margin-left: auto;
  margin-right: auto;
}

.upgrade-card-btn {
  height: 40px;
  font-size: 14px;
  font-weight: 600;
  border-radius: var(--radius-md);
  padding: 0 24px;
}
</style>
