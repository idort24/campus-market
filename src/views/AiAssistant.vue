<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

const WELCOME_MSG = '你好，我是校园集市智能助手 🤖\n我可以帮你寻找校园二手商品、查询商品信息、提供定价参考。'
const messages = ref([{ role: 'ai', text: WELCOME_MSG }])
const input = ref('')
const loading = ref(false)
const listEl = ref(null)

const scrollToBottom = () => {
  nextTick(() => { if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight })
}

const getReply = (q) => {
  if (/高数|教材|课本|数学书/.test(q)) return '当然可以！我为你找到了一些高数教材：\n\n📖 高等数学（第七版）· 同济大学 · ¥20\n📖 高等数学（第六版）· 九成新 · ¥15\n\n需要我把详细信息和卖家联系方式发给你吗？'
  if (/电脑|笔记本|显示器|主机|键盘/.test(q)) return '为你找到以下数码好物：\n\n💻 联想笔记本 · 八代i5 · ¥1800\n🖥️ 24寸显示器 · 无坏点 · ¥350\n\n这些都是校园内的同学发布的，可以当面验货哦。'
  if (/耳机/.test(q)) return '为你找到以下耳机：\n\n🎧 蓝牙耳机 · 使用半年 · ¥50\n🎧 有线耳机 · 全新 · ¥20\n\n需要对比更多款式吗？'
  if (/自行车|单车|骑行/.test(q)) return '为你找到以下自行车：\n\n🚲 山地自行车 · 变速24寸 · ¥180\n🚲 折叠自行车 · 通勤神器 · ¥120\n\n都是校园自提，欢迎当面看车。'
  if (/价格|多少钱|贵不贵|便宜|定价/.test(q)) return '根据校园历史成交数据，这类商品的价格一般在 ¥15 - ¥200 之间，\n具体取决于成色和品牌。\n\n建议你在商品详情页查看"同款比价"，获取更准确的参考区间。'
  if (/你好|您好|在吗|hello|hi/i.test(q)) return '你好！我是校园集市智能助手 🤖\n\n你可以问我：\n· 找某类商品（如"高数教材"）\n· 商品价格参考\n· 平台使用问题'
  if (/谢谢|感谢/.test(q)) return '不客气！很高兴能帮到你 😊\n还有其他需要帮忙的吗？'
  if (/交易|付款|转账|微信|支付宝|定金/.test(q)) return '温馨提示：平台提倡校园内面对面交易，请勿提前转账付款，谨防诈骗！\n\n交易前请确认商品与描述一致，保留好聊天记录。'
  return '收到你的问题啦！\n\n我正在为你查找校园二手市场中的相关信息。\n\n目前你可以试试问我：\n📚 找高数教材\n🚲 找自行车\n🎧 耳机价格'
}

const sendMessage = () => {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  loading.value = true
  scrollToBottom()
  setTimeout(() => {
    messages.value.push({ role: 'ai', text: getReply(text) })
    loading.value = false
    scrollToBottom()
  }, 900 + Math.random() * 600)
}

const quickAsk = (text) => { input.value = text; sendMessage() }
const clearChat = () => { messages.value = [{ role: 'ai', text: WELCOME_MSG }]; scrollToBottom() }
</script>

<template>
  <div class="ai-page">
    <NavBar />

    <div class="chat-container">
      <div class="chat-header">
        <div class="bot-avatar">🤖</div>
        <div>
          <h2>AI 智能助手</h2>
          <p class="online">● 在线 · 帮你找好物</p>
        </div>
        <button class="clear-btn" @click="clearChat">🗑️ 清空</button>
      </div>

      <div class="chat-box" ref="listEl">
        <div v-for="(m, i) in messages" :key="i" :class="['msg-row', m.role]">
          <div class="avatar">{{ m.role === 'ai' ? '🤖' : '👤' }}</div>
          <div class="bubble"><p class="msg-text">{{ m.text }}</p></div>
        </div>

        <div v-if="loading" class="msg-row ai">
          <div class="avatar">🤖</div>
          <div class="bubble typing"><span class="dot"></span><span class="dot"></span><span class="dot"></span></div>
        </div>

        <div v-if="messages.length <= 1 && !loading" class="quick-tips">
          <p>试试问我：</p>
          <div class="quick-btns">
            <button @click="quickAsk('我想找一本高数教材')">📚 找高数教材</button>
            <button @click="quickAsk('有没有二手自行车')">🚲 找自行车</button>
            <button @click="quickAsk('蓝牙耳机多少钱')">🎧 耳机价格</button>
          </div>
        </div>
      </div>

      <footer class="input-bar">
        <input v-model="input" @keyup.enter="sendMessage" placeholder="请输入你的问题..." maxlength="200" />
        <button class="send-btn" :disabled="loading || !input.trim()" @click="sendMessage">发送</button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.ai-page { min-height: 100vh; background: #fafbfc; }
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; }

.chat-container { max-width: 1100px; margin: 24px auto; background: #fff; border-radius: 20px; border: 1px solid #f0f0f0; display: flex; flex-direction: column; height: calc(100vh - 100px); overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.04); }

.chat-header { display: flex; align-items: center; gap: 12px; padding: 16px 24px; border-bottom: 1px solid #f0f0f0; background: #fff; }
.bot-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #2563eb, #06b6d4); display: flex; align-items: center; justify-content: center; font-size: 22px; }
.chat-header h2 { margin: 0; font-size: 17px; }
.online { margin: 2px 0 0; font-size: 12px; color: #10b981; }
.clear-btn { margin-left: auto; border: 1px solid #e2e8f0; background: #fff; padding: 6px 12px; border-radius: 16px; font-size: 12px; cursor: pointer; color: #64748b; }
.clear-btn:hover { border-color: #ef4444; color: #ef4444; }

.chat-box { flex: 1; overflow-y: auto; padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; background: #fafbfc; }
.msg-row { display: flex; gap: 10px; max-width: 70%; }
.msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; box-shadow: 0 2px 6px rgba(0,0,0,0.06); }
.msg-row.user .avatar { background: #dbeafe; }
.bubble { background: #fff; border-radius: 14px; padding: 12px 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); white-space: pre-line; }
.msg-row.user .bubble { background: #2563eb; color: #fff; }
.msg-text { margin: 0; font-size: 14px; line-height: 1.7; word-break: break-word; }

.typing { display: flex; gap: 5px; align-items: center; padding: 14px 18px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #93c5fd; animation: bounce 1.2s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,60%,100% { transform: translateY(0); opacity: 0.4; } 30% { transform: translateY(-6px); opacity: 1; } }

.quick-tips { align-self: center; margin-top: 20px; text-align: center; }
.quick-tips p { color: #94a3b8; font-size: 13px; margin: 0 0 10px; }
.quick-btns { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.quick-btns button { border: 1px solid #bfdbfe; background: #fff; color: #2563eb; border-radius: 999px; padding: 8px 16px; font-size: 13px; cursor: pointer; transition: all .2s; }
.quick-btns button:hover { background: #2563eb; color: #fff; }

.input-bar { display: flex; gap: 12px; padding: 14px 20px; background: #fff; border-top: 1px solid #f0f0f0; }
.input-bar input { flex: 1; height: 44px; border: 1px solid #e2e8f0; border-radius: 22px; padding: 0 18px; font-size: 14px; outline: none; }
.input-bar input:focus { border-color: #2563eb; }
.send-btn { width: 80px; border: none; background: #2563eb; color: #fff; border-radius: 22px; font-size: 14px; font-weight: 600; cursor: pointer; }
.send-btn:hover { background: #1d4ed8; }
.send-btn:disabled { background: #93c5fd; cursor: not-allowed; }
</style>
