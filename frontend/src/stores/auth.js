import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as userApi from '../api/user'

const TOKEN_KEY = 'tiktok_token'
const USER_ID_KEY = 'tiktok_user_id'
const USERNAME_KEY = 'tiktok_username'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userId = ref(localStorage.getItem(USER_ID_KEY) || '')
  const username = ref(localStorage.getItem(USERNAME_KEY) || '')

  const isLoggedIn = computed(() => Boolean(token.value))

  function persistAuth(data) {
    token.value = data.token
    userId.value = String(data.userId)
    username.value = data.username
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_ID_KEY, String(data.userId))
    localStorage.setItem(USERNAME_KEY, data.username)
  }

  function clearAuth() {
    token.value = ''
    userId.value = ''
    username.value = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_ID_KEY)
    localStorage.removeItem(USERNAME_KEY)
  }

  async function login(form) {
    const res = await userApi.login(form)
    persistAuth(res.data)
  }

  async function register(form) {
    const res = await userApi.register(form)
    persistAuth(res.data)
  }

  async function logout() {
    if (token.value) {
      try {
        await userApi.logout()
      } catch {
        // 注销接口失败时仍清除本地登录态
      }
    }
    clearAuth()
  }

  return {
    token,
    userId,
    username,
    isLoggedIn,
    login,
    register,
    logout,
    clearAuth,
  }
})
