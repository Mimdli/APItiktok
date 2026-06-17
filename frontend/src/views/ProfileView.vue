<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as videoApi from '../api/video'

const auth = useAuthStore()
const router = useRouter()

const videos = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)
const loading = ref(false)
const error = ref('')

function resolveVideoUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/') ? url : `/${url}`
}

async function loadVideos() {
  loading.value = true
  error.value = ''
  try {
    const res = await videoApi.getMyVideos(page.value, size.value)
    const data = res.data || {}
    videos.value = data.records || []
    total.value = data.total || 0
    pages.value = data.pages || 0
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function removeVideo(id) {
  if (!confirm('确定删除该视频？')) return
  try {
    await videoApi.deleteVideo(id)
    await loadVideos()
  } catch (e) {
    error.value = e.message
  }
}

async function logout() {
  await auth.logout()
  router.replace('/login')
}

function prevPage() {
  if (page.value > 1) {
    page.value -= 1
    loadVideos()
  }
}

function nextPage() {
  if (page.value < pages.value) {
    page.value += 1
    loadVideos()
  }
}

onMounted(loadVideos)
</script>

<template>
  <div class="page profile-page">
    <header class="header">
      <div>
        <h1>我的</h1>
        <p class="hint-text">用户：{{ auth.username }}（ID: {{ auth.userId }}）</p>
      </div>
      <button class="btn-ghost" type="button" @click="logout">退出</button>
    </header>

    <h2 class="section-title">我的视频（共 {{ total }} 条）</h2>

    <p v-if="loading" class="hint-text">加载中...</p>
    <p v-if="error" class="error-text">{{ error }}</p>

    <ul v-if="videos.length" class="video-list">
      <li v-for="item in videos" :key="item.id" class="video-item">
        <video class="thumb" :src="resolveVideoUrl(item.videoUrl)" controls preload="metadata" />
        <div class="meta">
          <p class="title">{{ item.title }}</p>
          <p class="hint-text">点赞 {{ item.likeCount || 0 }}</p>
          <button class="delete-btn" type="button" @click="removeVideo(item.id)">删除</button>
        </div>
      </li>
    </ul>

    <p v-else-if="!loading" class="hint-text empty">还没有发布视频，去「发布」页上传吧</p>

    <div v-if="pages > 1" class="pager">
      <button class="btn-ghost" type="button" :disabled="page <= 1" @click="prevPage">上一页</button>
      <span>{{ page }} / {{ pages }}</span>
      <button class="btn-ghost" type="button" :disabled="page >= pages" @click="nextPage">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  padding: 20px 16px;
  background: #111;
  min-height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.section-title {
  margin: 24px 0 12px;
  font-size: 16px;
}

.video-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.video-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
}

.thumb {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  background: #000;
}

.meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title {
  font-weight: 600;
}

.delete-btn {
  margin-top: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(254, 44, 85, 0.2);
  color: #fe2c55;
  align-self: flex-start;
}

.empty {
  margin-top: 12px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}
</style>
