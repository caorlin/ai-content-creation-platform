<template>
  <div class="title-selecting-stage">
    <div class="stage-header">
      <h2 class="stage-title">选择标题方案</h2>
      <p class="stage-subtitle">AI 为您生成了以下标题，请选择一个或自定义</p>
    </div>

    <a-radio-group v-model:value="selectedIndex" class="title-options">
      <div v-for="(option, index) in titleOptions" :key="index" class="title-option">
        <a-radio :value="index">
          <div class="title-content">
            <div class="title-main">{{ option.mainTitle }}</div>
            <div class="title-sub">{{ option.subTitle }}</div>
          </div>
        </a-radio>
      </div>

      <div class="title-option custom">
        <a-radio :value="-1">
          <div class="title-content">
            <div class="title-main">自定义标题</div>
          </div>
        </a-radio>

        <div v-if="selectedIndex === -1" class="custom-inputs">
          <a-input v-model:value="customMainTitle" placeholder="输入主标题" class="custom-input" />
          <a-input v-model:value="customSubTitle" placeholder="输入副标题" class="custom-input" />
        </div>
      </div>
    </a-radio-group>

    <div class="description-section">
      <label class="section-label">补充描述（可选）</label>
      <p class="section-tip">补充您对文章的期望、重点强调的内容等</p>
      <a-textarea
        v-model:value="userDescription"
        placeholder="例如：请重点强调技术原理，用通俗的语言讲解..."
        :rows="4"
        :maxlength="500"
        show-count
        class="description-textarea"
      />
    </div>

    <div class="actions">
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
        确认并生成大纲
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface TitleOption {
  mainTitle: string
  subTitle: string
}

interface Props {
  titleOptions: TitleOption[]
  loading?: boolean
}

interface Emits {
  (
    e: 'confirm',
    data: {
      mainTitle: string
      subTitle: string
      userDescription: string
    },
  ): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const emit = defineEmits<Emits>()

const selectedIndex = ref<number>(0)
const customMainTitle = ref('')
const customSubTitle = ref('')
const userDescription = ref('')

const canConfirm = computed(() => {
  if (selectedIndex.value === -1) {
    return customMainTitle.value.trim() && customSubTitle.value.trim()
  }
  return selectedIndex.value >= 0 && selectedIndex.value < props.titleOptions.length
})

const handleConfirm = () => {
  let mainTitle = ''
  let subTitle = ''

  if (selectedIndex.value === -1) {
    mainTitle = customMainTitle.value
    subTitle = customSubTitle.value
  } else {
    const selected = props.titleOptions[selectedIndex.value]
    mainTitle = selected.mainTitle
    subTitle = selected.subTitle
  }

  emit('confirm', {
    mainTitle,
    subTitle,
    userDescription: userDescription.value,
  })
}
</script>

<style scoped>
.title-selecting-stage {
  max-width: 720px;
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

/* Title Options */
.title-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.title-option {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  transition: all var(--transition-normal);
  cursor: pointer;
}

.title-option:hover {
  border-color: var(--color-primary-light);
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

:deep(.ant-radio-wrapper) {
  width: 100%;
  display: flex;
  align-items: flex-start;
}

:deep(.ant-radio) {
  margin-top: 3px;
}

.title-content {
  margin-left: 4px;
}

.title-main {
  font-family: 'Outfit', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.4;
  margin-bottom: 4px;
}

.title-sub {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* Custom Title Inputs */
.custom-inputs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
  padding-left: 28px;
}

.custom-input {
  border-radius: var(--radius-md);
}

/* Description Section */
.description-section {
  margin-top: 32px;
  padding: 20px 24px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.section-label {
  font-family: 'Outfit', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  display: block;
  margin-bottom: 4px;
}

.section-tip {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0 0 12px;
}

.description-textarea {
  border-radius: var(--radius-md);
}

/* Actions */
.actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.confirm-btn {
  min-width: 220px;
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
</style>
