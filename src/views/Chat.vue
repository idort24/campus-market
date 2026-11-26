<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessages, getSessions, sendMessage, normalizeMessage } from '../api/chat.js'
import { createChatSocket } from '../api/chatSocket.js'
import { isAdmin } from '../utils/auth.js'

const route = useRoute()
const router = useRouter()

// 对方 id：路由 /chat/:userId
const peerId = computed(() => route.params.userId || route.query.userId || '')
// 标题按当前角色区分：管理员（卖家侧）看买家，学生（买家侧）看卖家
const titleText = computed(() => (isAdmin() ? '与买家聊天' : '与卖家聊天'))
const messages = ref([])
const sessions = ref([])
const input = ref('')
const loading = ref(false)
const sending = ref(false)
const online = ref(false)

let socket = null

async function loadSessions() {
  try {
    sessions.value = await getSessions()
  } catch {
    // 会话列表失败不阻断对话窗
  }
}

async function load() {
  if (!peerId.value) {
    messages.value = []
    return
  }
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

function openSession(userId) {
  if (userId != null) router.push(`/chat/${userId}`)
}

// 最后一条消息时间只展示「MM-dd HH:mm」
function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}

// WebSocket 推送回调：新消息入库后服务端推给接收方；ack 是发送方自己消息的落库回执
function handleSocketMessage(payload) {
  if (!payload.msg) {
    if (payload.type === 'error') ElMessage.warning(payload.msg || '消息发送异常')
    return
  }
  const m = normalizeMessage(payload.msg)
  if (payload.type === 'message') {
    // 只接收当前会话对方发来的消息
    if (String(m.senderId) !== String(peerId.value)) {
      // 来自其他会话：刷新会话列表的未读与最后一条
      loadSessions()
      return
    }
    messages.value.push(m)
  } else if (payload.type === 'ack') {
    if (!messages.value.some((x) => x.id && x.id === m.id)) messages.value.push(m)
  } else if (payload.type === 'error') {
    ElMessage.warning(payload.msg || '消息发送异常')
  }
}

onMounted(async () => {
  await loadSessions()
  await load()
  const token = localStorage.getItem('token')
  if (token) {
    socket = createChatSocket({
      token,
      onMessage: handleSocketMessage,
      onStatus: (s) => { online.value = s === 'connected' },
    })
  }
})

onUnmounted(() => socket?.close())

// 切换会话：重新拉历史并刷新列表未读
watch(peerId, async () => {
  await load()
  await loadSessions()
})

async function send() {
  const content = input.value.trim()
  if (!content || sending.value) return
  sending.value = true
  try {
    // 优先走 WebSocket 实时通道；连接未就绪时降级走 REST 发送
    const sent = socket?.send({ type: 'chat', receiverId: peerId.value, content })
    if (!sent) {
      const msg = await sendMessage({ receiverId: peerId.value, content })
      if (msg) messages.value.push(msg)
    }
    input.value = ''
    loadSessions()
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
      <aside class="session-pane">
        <div class="session-head">消息会话</div>
        <div v-if="!sessions.length" class="session-empty">暂无会话</div>
        <div
          v-for="s in sessions"
          :key="s.userId"
          :class="['session-item', { active: String(s.userId) === String(peerId) }]"
          @click="openSession(s.userId)"
        >
          <div class="session-avatar">💬</div>
          <div class="session-body">
            <div class="session-top">
              <span class="session-name">{{ s.nickName }}</span>
              <span class="session-time">{{ formatTime(s.lastTime) }}</span>
            </div>
            <div class="session-bottom">
              <span class="session-last">{{ s.mineLast ? '我：' : '' }}{{ s.lastMessage || '图片消息' }}</span>
              <span v-if="s.unreadCount > 0" class="session-badge">{{ s.unreadCount }}</span>
            </div>
          </div>
        </div>
      </aside>

      <section v-if="peerId" class="chat-box">
        <div class="chat-header">
          <span>{{ titleText }}（用户 #{{ peerId }}）</span>
          <span class="ws-status">
            <span :class="['ws-dot', online ? 'on' : 'off']"></span>
            {{ online ? '在线' : '重连中' }}
          </span>
        </div>
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
      </section>

      <section v-else class="chat-box empty-box">
        <div class="pick-empty">从左侧选择一个会话开始聊天</div>
      </section>
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

.chat-container { max-width: 960px; margin: 24px auto; padding: 0 16px; display: flex; gap: 16px; align-items: stretch; }
.session-pane { width: 280px; flex-shrink: 0; background: #fff; border: 1px solid #f0f0f0; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; }
.session-head { padding: 14px 18px; font-weight: 600; border-bottom: 1px solid #f0f0f0; }
.session-empty { padding: 40px 16px; text-align: center; color: #94a3b8; font-size: 13px; }
.session-item { display: flex; gap: 10px; padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f5f5f5; }
.session-item.active { background: #eff6ff; }
.session-avatar { width: 40px; height: 40px; border-radius: 12px; background: #f1f5f9; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.session-body { flex: 1; min-width: 0; }
.session-top { display: flex; justify-content: space-between; align-items: center; }
.session-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.session-time { font-size: 11px; color: #94a3b8; }
.session-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.session-last { font-size: 12px; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 170px; }
.session-badge { min-width: 18px; height: 18px; border-radius: 9px; background: #ef4444; color: #fff; font-size: 11px; display: inline-flex; align-items: center; justify-content: center; padding: 0 5px; }
.chat-box { flex: 1; min-width: 0; border: 1px solid #f0f0f0; border-radius: 16px; overflow: hidden; background: #fff; }
.empty-box { display: flex; align-items: center; justify-content: center; min-height: 320px; }
.pick-empty { color: #94a3b8; font-size: 14px; }
.chat-header { padding: 16px 20px; background: #f8fafc; font-weight: 600; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
.ws-status { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 400; color: #64748b; }
.ws-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.ws-dot.on { background: #16a34a; }
.ws-dot.off { background: #94a3b8; }
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
