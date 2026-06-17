<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const mode = ref('login')
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  try {
    const form = { username: username.value.trim(), password: password.value }
    if (mode.value === 'login') {
      await auth.login(form)
    } else {
      await auth.register(form)
    }
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="card">
      <h1>简易抖音</h1>
      <p class="subtitle">登录后即可发布与管理视频</p>

      <div class="tabs">
        <button
          type="button"
          :class="{ active: mode === 'login' }"
          @click="mode = 'login'"
        >
          登录
        </button>
        <button
          type="button"
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >
          注册
        </button>
      </div>

      <form @submit.prevent="submit">
        <label>
          用户名
          <input v-model="username" type="text" placeholder="例如 alice" autocomplete="username" />
        </label>
        <label>
          密码
          <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </label>

        <p v-if="error" class="error-text">{{ error }}</p>

        <button class="btn-primary" type="submit" :disabled="loading">
          {{ loading ? '提交中...' : mode === 'login' ? '登录' : '注册' }}
        </button>
      </form>

      <p class="hint-text demo-hint">测试账号：alice / 123456</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(160deg, #111 0%, #1a1a2e 100%);
}

.card {
  width: 100%;
  max-width: 400px;
  padding: 32px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
}

h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.subtitle {
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.tabs button {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.08);
}

.tabs button.active {
  background: #fe2c55;
  color: #fff;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

input {
  padding: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
}

.demo-hint {
  margin-top: 16px;
  text-align: center;
}
</style>
