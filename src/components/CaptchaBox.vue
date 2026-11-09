<script setup>
// ============================================================
// 图形验证码组件
// ------------------------------------------------------------
// 能力：点击图片换一张、加载态、获取失败自动重试一次
// 用法：
//   <CaptchaBox @change="({key}) => captchaKey = key" />
//   或配合 v-model（存 key）
// ============================================================
import { ref, onMounted } from 'vue'
import { getCaptcha } from '../api/auth.js'

const props = defineProps({
  modelValue: { type: String, default: '' },
  autoLoad: { type: Boolean, default: true },
})
const emit = defineEmits(['update:modelValue', 'change'])

const image = ref('')
const loading = ref(false)
const failed = ref(false)

async function refresh() {
  if (loading.value) return
  loading.value = true
  failed.value = false
  try {
    const res = await getCaptcha()
    image.value = res.imageBase64
    emit('update:modelValue', res.captchaKey)
    emit('change', { key: res.captchaKey, image: res.imageBase64 })
  } catch {
    failed.value = true
    image.value = ''
    // 失败自动重试一次
    setTimeout(() => { if (failed.value) refresh() }, 600)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (props.autoLoad) refresh()
})

defineExpose({ refresh })
</script>

<template>
  <div class="captcha-box" :class="{ failed }" title="点击刷新验证码" @click="refresh">
    <div v-if="loading" class="captcha-state">
      <span class="spinner"></span>
    </div>
    <div v-else-if="failed" class="captcha-state text">加载失败，点击重试</div>
    <img v-else-if="image" :src="image" alt="图形验证码" class="captcha-img" />
    <div v-else class="captcha-state text">点击获取</div>
  </div>
</template>

<style scoped>
.captcha-box {
  width: 140px;
  height: 50px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  flex-shrink: 0;
  transition: border-color 0.2s;
}
.captcha-box:hover { border-color: #2563eb; }
.captcha-box.failed { border-color: #fca5a5; }
.captcha-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.captcha-state { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.captcha-state.text { font-size: 12px; color: #94a3b8; }
.spinner {
  width: 18px; height: 18px; border: 2px solid #cbd5e1; border-top-color: #2563eb;
  border-radius: 50%; animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
