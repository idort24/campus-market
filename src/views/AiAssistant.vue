<script setup>
import { ref, nextTick } from 'vue'

// ============ 消息列表 ============
const WELCOME_MSG =
  '你好，我是校园集市智能助手 🤖\n我可以帮你寻找校园二手商品、查询商品信息、提供定价参考。'

const messages = ref([{ role: 'ai', text: WELCOME_MSG }])
const input = ref('')
const loading = ref(false)
const listEl = ref(null)

// ============ 滚动到底部 ============
const scrollToBottom = () => {
  nextTick(() => {
    if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight
  })
}

// ============ 模拟 AI 回复（第一阶段前端模拟，后续可接真实 API） ============
const getReply = (q) => {
  if (/高数|教材|课本|数学书/.test(q)) {
    return (
      '当然可以！我为你找到了一些高数教材：\n\n' +
      '📖 高等数学（第七版）· 同济大学 · ¥20\n' +
      '📖 高等数学（第六版）· 九成新 · ¥15\n\n' +
      '需要我把详细信息和卖家联系方式发给你吗？'
    )
  }
  if (/电脑|笔记本|显示器|主机|键盘/.test(q)) {
    return (
      '为你找到以下数码好物：\n\n' +
      '💻 联想笔记本 · 八代i5 · ¥1800\n' +
      '🖥️ 24寸显示器 · 无坏点 · ¥350\n\n' +
      '这些都是校园内的同学发布的，可以当面验货哦。'
    )
  }
  if (/耳机/.test(q)) {
    return (
      '为你找到以下耳机：\n\n' +
      '🎧 蓝牙耳机 · 使用半年 · ¥50\n' +
      '🎧 有线耳机 · 全新 · ¥20\n\n' +
      '需要对比更多款式吗？'
    )
  }
  if (/自行车|单车|骑行/.test(q)) {
    return (
      '为你找到以下自行车：\n\n' +
      '🚲 山地自行车 · 变速24寸 · ¥180\n' +
      '🚲 折叠自行车 · 通勤神器 · ¥120\n\n' +
      '都是校园自提，欢迎当面看车。'
    )
  }
  if (/价格|多少钱|贵不贵|便宜|定价/.test(q)) {
    return (
      '根据校园历史成交数据，这类商品的价格一般在 ¥15 - ¥200 之间，\n' +
      '具体取决于成色和品牌。\n\n' +
      '建议你在商品详情页查看"同款比价"，获取更准确的参考区间。'
    )
  }
  if (/你好|您好|在吗|hello|hi/i.test(q)) {
    return (
      '你好！我是校园集市智能助手 🤖\n\n' +
      '你可以问我：\n' +
      '· 找某类商品（如"高数教材"）\n' +
      '· 商品价格参考\n' +
      '· 平台使用问题'
    )
  }
  if (/谢谢|感谢/.test(q)) {
    return '不客气！很高兴能帮到你 😊\n还有其他需要帮忙的吗？'
  }
  if (/交易|付款|转账|微信|支付宝|定金/.test(q)) {
    return (
      '温馨提示：平台提倡校园内面对面交易，请勿提前转账付款，谨防诈骗！\n\n' +
      '交易前请确认商品与描述一致，保留好聊天记录。'
    )
  }
  return (
    '收到你的问题啦！\n\n' +
    '我正在为你查找校园二手市场中的相关信息。\n\n' +
    '目前你可以试试问我：\n' +
    '📚 找高数教材\n' +
    '🚲 找自行车\n' +
    '🎧 耳机价格\n\n' +
    '（注：当前为演示版本，接入 AI 接口后我会更聪明哦）'
  )
}

// ============ 发送消息 ============
const sendMessage = () => {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  loading.value = true
  scrollToBottom()
  // 模拟 AI 思考时间
  setTimeout(() => {
    messages.value.push({ role: 'ai', text: getReply(text) })
    loading.value = false
    scrollToBottom()
  }, 900 + Math.random() * 600)
}

// ============ 快捷提问 ============
const quickAsk = (text) => {
  input.value = text
  sendMessage()
}

// ============ 清空聊天 ============
const clearChat = () => {
  messages.value = [{ role: 'ai', text: WELCOME_MSG }]
  scrollToBottom()
}

// ============ 返回首页 ============
const goHome = () => {
  window.location.hash = '#/'
}
</script>

<template>
  <div class="assistant-page">
    <!-- 顶部栏 -->
    <header class="assistant-header">
      <button class="back-btn" @click="goHome">← 返回首页</button>
      <div class="header-title">
        <span class="bot-icon">🤖</span>
        <div>
          <h1>AI 智能助手</h1>
          <p>在线 · 帮你找好物</p>
        </div>
      </div>
      <button class="clear-btn" @click="clearChat">🗑️ 清空聊天</button>
    </header>

    <!-- 聊天消息列表 -->
    <div class="chat-box" ref="listEl">
      <div
        v-for="(m, i) in messages"
        :key="i"
        :class="['msg-row', m.role]"
      >
        <div class="avatar">{{ m.role === 'ai' ? '🤖' : '👤' }}</div>
        <div class="bubble">
          <p class="msg-text">{{ m.text }}</p>
          <span v-if="m.role === 'ai'" class="ai-tag">智能助手自动回复</span>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="msg-row ai">
        <div class="avatar">🤖</div>
        <div class="bubble typing">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>

      <!-- 快捷提问 -->
      <div v-if="messages.length <= 1 && !loading" class="quick-tips">
        <p>试试问我：</p>
        <div class="quick-btns">
          <button @click="quickAsk('我想找一本高数教材')">📚 找高数教材</button>
          <button @click="quickAsk('有没有二手自行车')">🚲 找自行车</button>
          <button @click="quickAsk('蓝牙耳机多少钱')">🎧 耳机价格</button>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <footer class="input-bar">
      <input
        v-model="input"
        @keyup.enter="sendMessage"
        placeholder="请输入你的问题..."
        maxlength="200"
      />
      <button
        class="send-btn"
        :disabled="loading || !input.trim()"
        @click="sendMessage"
      >
        发送
      </button>
    </footer>
  </div>
</template>

<style scoped>
.assistant-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fc;
  font-family: system-ui, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei',
    Roboto, sans-serif;
}

/* ========== 顶部栏 ========== */
.assistant-header {
  height: 64px;
  padding: 0 5%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #6c5ce7;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 2px 10px rgba(108, 92, 231, 0.3);
}

.back-btn,
.clear-btn {
  border: none;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover,
.clear-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bot-icon {
  font-size: 28px;
}

.header-title h1 {
  font-size: 18px;
  margin: 0;
}

.header-title p {
  font-size: 12px;
  margin: 2px 0 0;
  opacity: 0.85;
}

/* ========== 聊天区 ========== */
.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 24px 5%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #f7f8fc;
}

.msg-row {
  display: flex;
  gap: 10px;
  max-width: 78%;
}

.msg-row.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.msg-row.user .avatar {
  background: #e9e5ff;
}

.bubble {
  background: #fff;
  border-radius: 14px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  white-space: pre-line;
}

.msg-row.user .bubble {
  background: #6c5ce7;
  color: #fff;
}

.msg-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.ai-tag {
  display: inline-block;
  margin-top: 8px;
  font-size: 11px;
  color: #999;
  background: #f4f4f8;
  border-radius: 4px;
  padding: 2px 8px;
}

/* ========== 加载状态（打字动画） ========== */
.typing {
  display: flex;
  gap: 5px;
  align-items: center;
  padding: 14px 18px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #b8b0e8;
  animation: bounce 1.2s infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

/* ========== 快捷提问 ========== */
.quick-tips {
  align-self: center;
  margin-top: 20px;
  text-align: center;
}

.quick-tips p {
  color: #999;
  font-size: 13px;
  margin: 0 0 10px;
}

.quick-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.quick-btns button {
  border: 1px solid #d6d0f5;
  background: #fff;
  color: #6c5ce7;
  border-radius: 999px;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btns button:hover {
  background: #6c5ce7;
  color: #fff;
}

/* ========== 输入区域 ========== */
.input-bar {
  display: flex;
  gap: 12px;
  padding: 14px 5%;
  background: #fff;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

.input-bar input {
  flex: 1;
  height: 46px;
  border: 1px solid #ddd;
  border-radius: 23px;
  padding: 0 20px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.input-bar input:focus {
  border-color: #6c5ce7;
}

.send-btn {
  width: 90px;
  border: none;
  background: #6c5ce7;
  color: #fff;
  border-radius: 23px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover {
  background: #5a4bd1;
}

.send-btn:disabled {
  background: #c9c4e8;
  cursor: not-allowed;
}

/* ========== 移动端适配 ========== */
@media (max-width: 600px) {
  .msg-row {
    max-width: 88%;
  }
  .back-btn {
    font-size: 12px;
    padding: 8px 10px;
  }
  .clear-btn {
    font-size: 12px;
    padding: 8px 10px;
  }
}
</style>
