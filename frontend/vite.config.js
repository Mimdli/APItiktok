import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/user': 'http://localhost:8080',
      '/video': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    },
  },
})
