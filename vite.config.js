import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 保留原项目 base 配置（部署在 /campus-market/ 路径下，不影响路由）
  base: '/campus-market/',
  server: {
    port: 5173,
    // 把 /api 请求代理到后端 Spring Boot (8090)，避免跨域
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})
