﻿﻿﻿<script setup lang="ts">
import { type MenuProps, message } from 'ant-design-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { USER_ROLE_ADMIN, USER_ROLE_USER, USER_ROLE_VIP } from '@/constant/user.ts'
import { LogoutOutlined, CrownOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'
import { headerMenuItems } from '@/config/menu.ts'

const router = useRouter()
const route = useRoute()

const selectedKeys = computed(() => [route.path])

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
}
// JS 中引入 Store
const loginUserStore = useLoginUserStore()

const roleLabel = computed(() => {
  const role = loginUserStore.loginUser.userRole
  if (role === USER_ROLE_ADMIN) return '管理员'
  if (role === USER_ROLE_VIP) return 'VIP'
  return '升级会员'
})
const isRegularUser = computed(() => loginUserStore.loginUser.userRole === USER_ROLE_USER || !loginUserStore.loginUser.userRole)

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      username: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
// 菜单配置项
const originItems = headerMenuItems

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))
</script>

<template>
  <a-layout-header class="global-header">
    <RouterLink class="brand" to="/">
      <img class="logo" src="@/assets/logo.png" alt="logo" />
      <span class="title">AI 内容创作平台</span>
    </RouterLink>
    <a-menu
      class="header-menu"
      mode="horizontal"
      :items="menuItems"
      :selected-keys="selectedKeys"
      @click="handleMenuClick"
    />

    <div class="user-login-status">
      <RouterLink v-if="isRegularUser" to="/vip" class="vip-link">
        <CrownOutlined />
        <span>升级会员</span>
      </RouterLink>
      <span v-else-if="loginUserStore.loginUser.id" class="vip-badge">
        <CrownOutlined />
        <span>{{ roleLabel }}</span>
      </span>
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space class="user-info">
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.username ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="doLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  box-shadow: 0 1px 8px rgb(0 0 0 / 6%);
}

.brand {
  display: inline-flex;
  flex: 1;
  align-items: center;
  color: #1f1f1f;
  text-decoration: none;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 8px;
  object-fit: contain;
}

.title {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.header-menu {
  flex: 0 auto;
  min-width: 0;
  border-bottom: none;
  justify-content: center;
  padding: 0 100px;
}

.header-menu :deep(.ant-menu-item) {
  margin: 0 12px;
  padding: 0;
}

.header-menu :deep(.ant-menu-item:first-child) {
  margin-left: 0;
}

.header-menu :deep(.ant-menu-item:last-child) {
  margin-right: 0;
}

.user-login-status {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.vip-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-normal);
  text-decoration: none;
  line-height: 1.5;
}

.vip-link:hover {
  background: var(--bg-primary-hover);
  box-shadow: 0 6px 20px rgba(14, 165, 233, 0.35);
  color: #fff;
}

.vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #1890ff;
  background: var(--color-background);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-background);
  line-height: 1.5;
}

.user-info {
  cursor: pointer;
}

@media (max-width: 768px) {
  .global-header {
    height: auto;
    min-height: 64px;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px 16px;
  }

  .brand {
    flex: none;
  }

  .title {
    font-size: 16px;
  }

  .user-login-status {
    flex: none;
  }

  .header-menu {
    flex: 1;
  }
}
</style>
