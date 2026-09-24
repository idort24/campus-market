import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
//演示代码
// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/campus-market/',
})