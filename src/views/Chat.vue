<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

const messages = ref([
  { from: 'them', text: '你好，这个商品还在吗？' },
  { from: 'me', text: '在的，可以面交' },
])
const input = ref('')

function send() {
  if (!input.value.trim()) return
  messages.value.push({ from: 'me', text: input.value })
  input.value = ''
  setTimeout(() => { messages.value.push({ from: 'them', text: '好的，怎么交易？' }) }, 500)
}
</script>

<template>
  <div class="chat-page">
    <NavBar />

    <main class="chat-container">
      <div class="chat-box">
        <div class="chat-header">与卖家聊天（用户 #{{ route.params.userId || 1 }}）</div>
        <div class="messages">
          <div v-for="(m, i) in messages" :key="i" :class="['msg', m.from]">{{ m.text }}</div>
        </div>
        <div class="input-bar">
          <input v-model="input" placeholder="输入消息..." @keyup.enter="send" />
          <button class="send-btn" @click="send">发送</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; text-decoration: none; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; text-decoration: none; }

.chat-container { max-width: 700px; margin: 24px auto; padding: 0 16px; }
.chat-box { border: 1px solid #f0f0f0; border-radius: 16px; overflow: hidden; background: #fff; }
.chat-header { padding: 16px 20px; background: #f8fafc; font-weight: 600; border-bottom: 1px solid #f0f0f0; }
.messages { padding: 20px; min-height: 320px; display: flex; flex-direction: column; gap: 12px; }
.msg { max-width: 70%; padding: 10px 14px; border-radius: 14px; font-size: 14px; }
.msg.me { align-self: flex-end; background: #2563eb; color: #fff; }
.msg.them { align-self: flex-start; background: #f1f5f9; }
.input-bar { display: flex; gap: 10px; padding: 12px 16px; border-top: 1px solid #f0f0f0; }
.input-bar input { flex: 1; border: 1px solid #e2e8f0; border-radius: 20px; padding: 8px 16px; outline: none; }
.send-btn { border: none; background: #2563eb; color: #fff; padding: 8px 20px; border-radius: 20px; cursor: pointer; }
</style>
