<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const messages = ref([
  { from: 'them', text: '你好，这个商品还在吗？' },
  { from: 'me', text: '在的，可以面交' },
])
const input = ref('')

function send() {
  if (!input.value.trim()) return
  messages.value.push({ from: 'me', text: input.value })
  input.value = ''
  setTimeout(() => {
    messages.value.push({ from: 'them', text: '好的，怎么交易？' })
  }, 500)
}
</script>

<template>
  <div class="chat-page">
    <div class="chat-box">
      <div class="chat-header">与卖家聊天（用户 #{{ route.params.userId || 1 }}）</div>
      <div class="messages">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.from]">
          {{ m.text }}
        </div>
      </div>
      <div class="input-bar">
        <el-input v-model="input" placeholder="输入消息..." @keyup.enter="send" />
        <el-button type="primary" @click="send">发送</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page { max-width: 700px; margin: 20px auto; padding: 0 16px; }
.chat-box { border: 1px solid #e3e3ec; border-radius: 12px; overflow: hidden; background: #fff; }
.chat-header { padding: 14px 18px; background: #f5f6fa; font-weight: 600; border-bottom: 1px solid #eee; }
.messages { padding: 18px; min-height: 300px; display: flex; flex-direction: column; gap: 10px; }
.msg { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; }
.msg.me { align-self: flex-end; background: #6c5ce7; color: #fff; }
.msg.them { align-self: flex-start; background: #f0f0f5; }
.input-bar { display: flex; gap: 10px; padding: 12px; border-top: 1px solid #eee; }
</style>
