<script setup lang="ts">
import { type MenuProps, message } from 'ant-design-vue'
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { HomeTwoTone, LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'

const router = useRouter()
const route = useRoute()

const selectedKeys = computed(() => [route.path])

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
}
// JS 中引入 Store
const loginUserStore = useLoginUserStore()

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
const originItems = [
  {
    key: '/',
    icon: () => h(HomeTwoTone),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: 'others',
    title: '编程导航',
  },
]

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
    <div class="header-left">
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
    </div>

    <div class="user-login-status">
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
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  box-shadow: 0 1px 8px rgb(0 0 0 / 6%);
}

.header-left {
  display: flex;
  flex: 1;
  align-items: center;
  min-width: 0;
}

.brand {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  margin-right: 24px;
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
  flex: 1;
  min-width: 0;
  border-bottom: none;
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

  .header-left {
    flex-basis: 100%;
  }

  .brand {
    margin-right: 12px;
  }

  .title {
    font-size: 16px;
  }
}
</style>
