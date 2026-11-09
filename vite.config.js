import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// 后端地址：默认本机 8090（Spring Boot 默认端口）。
// 后端若跑在别的机器上，启动前设环境变量 VITE_BACKEND=http://192.168.x.x:8090 即可，不用改代码。
const BACKEND = process.env.VITE_BACKEND || 'http://127.0.0.1:8090'

export default defineConfig({
  plugins: [vue()],
  base: '/campus-market/',
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: BACKEND,
        changeOrigin: true
      }
    }
  }
})
