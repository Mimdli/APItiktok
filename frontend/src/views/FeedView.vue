<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as videoApi from '../api/video'

const auth = useAuthStore()
const router = useRouter()

const videos = ref([])
const currentIndex = ref(0)
const viewedIds = ref([])
const loading = ref(true)
const error = ref('')
const feedHint = ref('')
const switching = ref(false)
const toastText = ref('')
let toastTimer = null

const currentVideo = computed(() => videos.value[currentIndex.value] || null)

function resolveVideoUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/') ? url : `/${url}`
}

function markViewed(id) {
  if (!id || viewedIds.value.includes(id)) return
  viewedIds.value.push(id)
}

function showToastMessage(msg) {
  if (toastTimer) clearTimeout(toastTimer)
  toastText.value = msg
  toastTimer = setTimeout(() => {
    toastText.value = ''
    toastTimer = null
  }, 2500)
}

async function loadFeed() {
  loading.value = true
  error.value = ''
  feedHint.value = ''
  try {
    const res = await videoApi.getFeed(viewedIds.value)
    const list = Array.isArray(res.data) ? res.data : []
    if (list.length) {
      videos.value = list
      currentIndex.value = 0
      markViewed(currentVideo.value?.id)
    } else if (viewedIds.value.length) {
      feedHint.value = '暂无更多推荐视频'
    } else {
      feedHint.value = '暂无推荐视频，可先登录后发布视频'
    }
  } catch (e) {
    videos.value = []
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function goNext() {
  if (!currentVideo.value || switching.value) return
  markViewed(currentVideo.value.id)
  feedHint.value = ''

  if (currentIndex.value < videos.value.length - 1) {
    currentIndex.value += 1
    markViewed(currentVideo.value.id)
    return
  }

  switching.value = true
  try {
    const res = await videoApi.getNextVideo(currentVideo.value.id, viewedIds.value)
    const next = res.data
    if (next) {
      const existIndex = videos.value.findIndex(v => v.id === next.id)
      if (existIndex >= 0) {
        currentIndex.value = existIndex
      } else {
        videos.value.push(next)
        currentIndex.value = videos.value.length - 1
      }
      markViewed(next.id)
    } else {
      showToastMessage('已经是最后一个视频了')
    }
  } catch (e) {
    showToastMessage('已经是最后一个视频了')
    console.error('getNextVideo error:', e)
  } finally {
    switching.value = false
  }
}

async function goPrev() {
  if (!currentVideo.value || switching.value) return
  feedHint.value = ''

  if (currentIndex.value > 0) {
    currentIndex.value -= 1
    markViewed(currentVideo.value.id)
    return
  }

  switching.value = true
  try {
    const res = await videoApi.getPrevVideo(currentVideo.value.id, viewedIds.value)
    const prev = res.data
    if (prev) {
      // 如果返回的视频已在列表中，直接跳转到对应位置
      const existIndex = videos.value.findIndex(v => v.id === prev.id)
      if (existIndex >= 0) {
        currentIndex.value = existIndex
      } else {
        videos.value.unshift(prev)
        currentIndex.value = 0
      }
      markViewed(prev.id)
    } else {
      showToastMessage('已经是第一个视频了')
    }
  } catch (e) {
    error.value = e.message
  } finally {
    switching.value = false
  }
}

async function toggleLike() {
  if (!currentVideo.value) return
  if (!auth.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: '/' } })
    return
  }

  const video = currentVideo.value
  const liked = Boolean(video.liked)
  try {
    if (liked) {
      await videoApi.unlikeVideo(video.id)
      video.liked = false
      video.likeCount = Math.max(0, (video.likeCount || 0) - 1)
    } else {
      await videoApi.likeVideo(video.id)
      video.liked = true
      video.likeCount = (video.likeCount || 0) + 1
    }
    error.value = ''
  } catch (e) {
    error.value = e.message
  }
}

let touchStartY = 0

function onTouchStart(e) {
  touchStartY = e.touches[0].clientY
}

function onTouchEnd(e) {
  const delta = e.changedTouches[0].clientY - touchStartY
  if (delta < -50) goNext()
  if (delta > 50) goPrev()
}

function onWheel(e) {
  if (Math.abs(e.deltaY) < 40) return
  if (e.deltaY > 0) goNext()
  else goPrev()
}

watch(
  () => auth.isLoggedIn,
  (loggedIn, wasLoggedIn) => {
    if (loggedIn && !wasLoggedIn) {
      loadFeed()
    }
  },
)

onMounted(loadFeed)
</script>

<template>
  <div
    class="feed-page"
    @touchstart.passive="onTouchStart"
    @touchend.passive="onTouchEnd"
    @wheel.passive="onWheel"
  >
    <div v-if="loading" class="center-msg">加载推荐视频中...</div>

    <div v-else-if="currentVideo" class="player-wrap">
      <video
        :key="currentVideo.id"
        class="video"
        :src="resolveVideoUrl(currentVideo.videoUrl)"
        controls
        playsinline
        autoplay
        loop
      />

      <div class="info">
        <h2>{{ currentVideo.title }}</h2>
        <p>点赞 {{ currentVideo.likeCount || 0 }}</p>
      </div>

      <div class="actions">
        <button type="button" class="action-btn" @click="goPrev">上一个</button>
        <button
          type="button"
          class="action-btn like"
          :class="{ active: currentVideo.liked }"
          @click="toggleLike"
        >
          {{ currentVideo.liked ? '已赞' : '点赞' }}
        </button>
        <button type="button" class="action-btn" @click="goNext">下一个</button>
      </div>
    </div>

    <div v-else class="center-msg">
      <p>{{ feedHint || '暂无视频' }}</p>
      <p v-if="error" class="error-text">{{ error }}</p>
      <button class="btn-ghost retry-btn" type="button" @click="loadFeed">重新加载</button>
      <router-link v-if="!auth.isLoggedIn" class="btn-ghost retry-btn" to="/login">去登录</router-link>
    </div>
  </div>
  <!-- 全局通知浮层 (Teleport 到 body 确保可见) -->
  <Teleport to="body">
    <div v-if="toastText" class="toast-overlay">{{ toastText }}</div>
  </Teleport>
</template>

<style scoped>
.feed-page {
  position: fixed;
  inset: 0;
  background: #000;
  padding-bottom: 56px;
}

.player-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.video {
  flex: 1;
  width: 100%;
  max-height: calc(100% - 140px);
  object-fit: contain;
  background: #000;
}

.info {
  padding: 12px 16px;
}

.info h2 {
  font-size: 18px;
  margin-bottom: 4px;
}

.info p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
  padding: 0 16px 12px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.action-btn.like.active {
  background: #fe2c55;
}

.center-msg {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.75);
}

.hint-text {
  color: #ffd93d;
  font-size: 14px;
  text-align: center;
  margin-top: 8px;
  padding: 8px 16px;
  background: rgba(255, 217, 61, 0.1);
  border-radius: 8px;
}

.retry-btn {
  margin-top: 8px;
}
</style>

<!-- 非 scoped 样式，确保 Teleport 到 body 的 toast 也能生效 -->
<style>
.toast-overlay {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  background: rgba(0, 0, 0, 0.9) !important;
  color: #fff !important;
  padding: 24px 40px !important;
  border-radius: 14px !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  z-index: 99999 !important;
  text-align: center !important;
  border: 2px solid rgba(255, 217, 61, 0.6) !important;
  pointer-events: none !important;
  box-shadow: 0 8px 32px rgba(0,0,0,0.5) !important;
}
</style>
