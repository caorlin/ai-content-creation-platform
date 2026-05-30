<script setup lang="ts">
import type { MenuProps } from 'ant-design-vue'
import { headerMenuItems } from '@/config/menu'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const selectedKeys = computed(() => [route.path])

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-left">
      <RouterLink class="brand" to="/">
        <img class="logo" src="/logo.png" alt="logo" />
        <span class="title">AI 内容创作平台</span>
      </RouterLink>
      <a-menu
        class="header-menu"
        mode="horizontal"
        :items="headerMenuItems"
        :selected-keys="selectedKeys"
        @click="handleMenuClick"
      />
    </div>
    <a-button type="primary">登录</a-button>
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
