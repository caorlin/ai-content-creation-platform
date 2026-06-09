﻿﻿<template>
  <div id="userLoginPage">
    <div class="form-card">
      <h2 class="form-title">欢迎回来</h2>

      <p class="form-subtitle">登录您的账号继续创作</p>

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

        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block> 登录 </a-button>
        </a-form-item>
      </a-form>

      <div class="form-footer">
        <span>还没有账号？</span>
        <RouterLink to="/user/register" class="register-link">立即注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userLogin } from '@/api/userController.js'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    const redirect = route.query.redirect as string
    router.push({
      path: redirect || '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>


<style scoped>
#userLoginPage {
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
</style>
