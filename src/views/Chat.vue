<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessages, sendMessage } from '../api/chat.js'

const route = useRoute()
const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

// 对方 id：路由 /chat/:userId
const peerId = computed(() => route.params.userId || route.query.userId || '')
const messages = ref([])
const input = ref('')
const loading = ref(false)
const sending = ref(false)

async function load() {
  loading.value = true
  try {
    // 后端：GET /api/v1/chat/messages?userId=对方id（进入会话自动把对方发的置为已读）
    messages.value = await getMessages(peerId.value)
  } catch (e) {
    ElMessage.error(e.message || '加载聊天记录失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)

async function send() {
  const content = input.value.trim()
  if (!content || sending.value) return
  sending.value = true
  try {
    // 后端：POST /api/v1/chat/messages，fromUserId 由后端从 token 取
    const msg = await sendMessage({ receiverId: peerId.value, content })
    if (msg) messages.value.push(msg)
    input.value = ''
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <div class="chat-page">
    <NavBar />

    <main class="chat-container">
      <div class="chat-box">
        <div class="chat-header">与卖家聊天（用户 #{{ peerId || 1 }}）</div>
        <div v-loading="loading" class="messages">
          <div
            v-for="m in messages"
            :key="m.id"
            :class="['msg', m.mine ? 'me' : 'them']"
          >
            <template v-if="m.image"><img :src="m.image" alt="图片消息" class="msg-img" /></template>
            <template v-else>{{ m.content }}</template>
          </div>
          <div v-if="!loading && !messages.length" class="msg-empty">还没有聊天记录，发一条试试</div>
        </div>
        <div class="input-bar">
          <input v-model="input" placeholder="输入消息..." :disabled="sending" @keyup.enter="send" />
          <button class="send-btn" :disabled="sending" @click="send">{{ sending ? '发送中…' : '发送' }}</button>
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
.msg-img { max-width: 180px; border-radius: 10px; display: block; }
.msg-empty { text-align: center; color: #94a3b8; font-size: 13px; margin: auto; }
.input-bar { display: flex; gap: 10px; padding: 12px 16px; border-top: 1px solid #f0f0f0; }
.input-bar input { flex: 1; border: 1px solid #e2e8f0; border-radius: 20px; padding: 8px 16px; outline: none; }
.send-btn { border: none; background: #2563eb; color: #fff; padding: 8px 20px; border-radius: 20px; cursor: pointer; }
</style>
