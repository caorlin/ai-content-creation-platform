﻿<template>
  <div :id="modal ? undefined : 'userRegisterPage'" :class="{ 'modal-mode': modal }">
    <div class="form-card">
      <h2 class="form-title">创建账号</h2>

      <p class="form-subtitle">注册您的账号开始创作</p>

      <a-form :model="formState" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { min: 8, message: '确认密码长度不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="请确认密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block> 注册 </a-button>
        </a-form-item>
      </a-form>

      <div class="form-footer">
        <span>已有账号？</span>
        <a v-if="modal" class="register-link" @click="emit('switchToLogin')">立即登录</a>
        <RouterLink v-else to="/user/login" class="register-link">立即登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{ modal?: boolean }>()
const emit = defineEmits<{ (e: 'success'): void; (e: 'switchToLogin'): void }>()
import { userRegister } from '@/api/userController.js'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const router = useRouter()

const validateCheckPassword = async (_rule: any, value: string) => {
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const handleSubmit = async (values: any) => {
  const res = await userRegister(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    if (props.modal) {
      emit('success')
      return
    }
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f0 100%);
}

.form-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px 36px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.form-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1f1f1f;
  text-align: center;
}

.form-subtitle {
  margin: 0 0 36px;
  font-size: 14px;
  color: #8c8c8c;
  text-align: center;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-input-affix-wrapper),
:deep(.ant-input) {
  border-radius: 8px;
}

:deep(.ant-input-affix-wrapper .anticon) {
  color: #bfbfbf;
  font-size: 16px;
}

:deep(.ant-btn-primary) {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #8c8c8c;
}

.register-link {
  color: #1890ff;
  font-weight: 600;
  font-size: 14px;
  margin-left: 4px;
  text-decoration: none;
  transition: opacity 0.2s;
}

.register-link:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.modal-mode {
  display: block;
  min-height: auto;
  padding: 0;
  background: none;
}

.modal-mode .form-card {
  box-shadow: none;
  padding: 0;
}
</style>
