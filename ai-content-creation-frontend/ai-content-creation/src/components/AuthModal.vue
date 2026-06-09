<template>
  <a-modal
    :open="visible"
    :title="null"
    :footer="null"
    :width="420"
    :closable="true"
    @cancel="handleClose"
    destroyOnClose
    centered
    class="auth-modal"
  >
    <div class="auth-modal-body">
      <UserLoginPage
        v-if="mode === 'login'"
        :modal="true"
        @success="handleSuccess"
        @switch-to-register="mode = 'register'"
      />
      <UserRegisterPage
        v-else
        :modal="true"
        @success="handleSuccess"
        @switch-to-login="mode = 'login'"
      />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const mode = ref<'login' | 'register'>('login')

const handleClose = () => {
  emit('update:visible', false)
}

const handleSuccess = () => {
  emit('update:visible', false)
}

watch(() => props.visible, (val) => {
  if (val) {
    mode.value = 'login'
  }
})
</script>

<style scoped>
.auth-modal :deep(.ant-modal-content) {
  border-radius: 16px;
  padding: 0;
  overflow: hidden;
}

.auth-modal :deep(.ant-modal-body) {
  padding: 40px 40px 32px;
}

.auth-modal-body {
  width: 100%;
}
</style>
