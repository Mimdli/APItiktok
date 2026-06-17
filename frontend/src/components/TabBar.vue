<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { name: 'feed', path: '/', label: '首页' },
  { name: 'publish', path: '/publish', label: '发布' },
  { name: 'profile', path: '/profile', label: '我的' },
]
</script>

<template>
  <nav class="tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.name"
      :to="tab.path"
      class="tab-item"
      :class="{ active: route.name === tab.name }"
    >
      {{ tab.label }}
    </router-link>
  </nav>
</template>

<style scoped>
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  height: 56px;
  background: rgba(18, 18, 18, 0.95);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.55);
  font-size: 15px;
}

.tab-item.active {
  color: #fe2c55;
  font-weight: 600;
}
</style>
