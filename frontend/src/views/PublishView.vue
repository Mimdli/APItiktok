<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as videoApi from '../api/video'

const router = useRouter()

const ALLOWED_EXT = ['.mp4', '.mov', '.avi', '.mkv', '.webm', '.flv']
const MAX_SIZE = 50 * 1024 * 1024

const title = ref('')
const file = ref(null)
const loading = ref(false)
const error = ref('')
const success = ref('')

function validateFile(selected) {
  if (!selected) return '请选择视频文件'
  const name = selected.name.toLowerCase()
  const ext = name.slice(name.lastIndexOf('.'))
  if (!ALLOWED_EXT.includes(ext)) {
    return `不支持的视频格式，仅支持：${ALLOWED_EXT.join('、')}`
  }
  if (selected.size > MAX_SIZE) {
    return '视频大小不能超过 50MB'
  }
  return ''
}

function onFileChange(e) {
  const selected = e.target.files?.[0] || null
  const msg = validateFile(selected)
  if (msg) {
    error.value = msg
    file.value = null
    e.target.value = ''
    return
  }
  error.value = ''
  file.value = selected
}

async function submit() {
  error.value = ''
  success.value = ''

  const fileError = validateFile(file.value)
  if (fileError) {
    error.value = fileError
    return
  }
  if (!title.value.trim()) {
    error.value = '请输入视频标题'
    return
  }

  loading.value = true
  try {
    await videoApi.publishVideo(file.value, title.value.trim())
    success.value = '发布成功'
    title.value = ''
    file.value = null
    setTimeout(() => router.push('/profile'), 800)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page publish-page">
    <header class="header">
      <h1>发布视频</h1>
      <p class="hint-text">支持 mp4 / mov / avi / mkv / webm / flv，最大 50MB</p>
    </header>

    <form class="form" @submit.prevent="submit">
      <label class="field">
        视频标题
        <input v-model="title" type="text" maxlength="128" placeholder="输入标题" />
      </label>

      <label class="field">
        选择视频
        <input type="file" accept="video/*" @change="onFileChange" />
      </label>

      <p v-if="file" class="hint-text">已选：{{ file.name }}</p>
      <p v-if="error" class="error-text">{{ error }}</p>
      <p v-if="success" class="success-text">{{ success }}</p>

      <button class="btn-primary" type="submit" :disabled="loading">
        {{ loading ? '上传中...' : '发布' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.publish-page {
  padding: 20px 16px;
  background: #111;
  min-height: 100%;
}

.header h1 {
  font-size: 22px;
  margin-bottom: 8px;
}

.form {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

input[type='text'],
input[type='file'] {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.success-text {
  color: #4cd964;
  font-size: 14px;
}
</style>
