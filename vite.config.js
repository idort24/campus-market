import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  base: '/campus-market/',
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        // 后端实际端口 8090，跑在后端同学电脑上
        // target 用后端同学的局域网 IP（当前手机热点网段 172.20.10.2）
        target: 'http://172.20.10.2:8090',
        changeOrigin: true
      }
    }
  }
})
