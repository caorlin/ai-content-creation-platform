<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
    <a-layout-content class="basic-layout-content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.basic-layout-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 88px;
}

@media (max-width: 768px) {
  .basic-layout-content {
    padding: 16px 16px 88px;
  }
}
</style>
