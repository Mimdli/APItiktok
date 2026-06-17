import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.code === 'number' && payload.code !== 200) {
      return Promise.reject(new Error(payload.msg || '请求失败'))
    }
    return payload
  },
  (error) => {
    const status = error.response?.status
    const serverMsg = error.response?.data?.msg

    let msg = serverMsg || error.message || '网络错误'
    if (!serverMsg && (status === 500 || status === 502 || status === 503 || error.code === 'ERR_NETWORK')) {
      msg = '无法连接后端，请确认：1) IDEA 已启动 App.java；2) 终端出现 Tomcat started on port 8080；3) backend/.env 中 DB_PASSWORD 已填写'
    }

    if (status === 401) {
      const auth = useAuthStore()
      auth.clearAuth()
    }
    return Promise.reject(new Error(msg))
  },
)

export default request
