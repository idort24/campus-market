<script setup>
// ============================================================
// 忘记密码页
// ------------------------------------------------------------
// 三步：填手机号 → 输短信验证码 → 设新密码
// 真实环境依赖后端 POST /auth/password/reset + 短信服务
// ============================================================
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSmsCode, resetPassword } from '../api/auth.js'

const router = useRouter()
const step = ref(1)
const loading = ref(false)

const form = ref({ phone: '', code: '', newPassword: '', confirmPassword: '' })
const countdown = ref(0)
let timer = null

function startCountdown() {
  countdown.value = 60
  clearInterval(timer)
  timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function sendCode() {
  if (countdown.value > 0) return
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    const res = await sendSmsCode(form.value.phone)
    startCountdown()
    step.value = 2
    if (res?.debugCode) {
      ElMessage.success(`验证码已发送（演示环境验证码：${res.debugCode}）`)
    } else {
      ElMessage.success('验证码已发送')
    }
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

async function submit() {
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) { ElMessage.warning('请输入正确的手机号'); return }
  if (!form.value.code.trim()) { ElMessage.warning('请输入短信验证码'); return }
  if (!form.value.newPassword || form.value.newPassword.length < 6) { ElMessage.warning('新密码至少 6 位'); return }
  if (form.value.newPassword !== form.value.confirmPassword) { ElMessage.warning('两次输入的密码不一致'); return }

  loading.value = true
  try {
    await resetPassword({
      phone: form.value.phone,
      code: form.value.code.trim(),
      newPassword: form.value.newPassword,
    })
    ElMessage.success('密码已重置，请用新密码登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="forgot-page">
    <div class="card">
      <router-link to="/login" class="back-link">← 返回登录</router-link>

      <h2>找回密码</h2>
      <p class="sub">通过手机号验证身份后设置新密码</p>

      <!-- 步骤条 -->
      <div class="steps">
        <span :class="['step-dot', { done: step >= 1 }]">1</span>
        <span class="step-line" :class="{ done: step >= 2 }"></span>
        <span :class="['step-dot', { done: step >= 2 }]">2</span>
        <span class="step-line" :class="{ done: step >= 3 }"></span>
        <span :class="['step-dot', { done: step >= 3 }]">3</span>
      </div>
      <div class="step-labels">
        <span>填手机号</span><span>验验证码</span><span>设新密码</span>
      </div>

      <form class="form-body" @submit.prevent="submit">
        <label class="field">
          <span class="label-text">手机号</span>
          <input v-model="form.phone" placeholder="请输入注册时的手机号" maxlength="11" />
        </label>

        <div class="field">
          <span class="label-text">短信验证码</span>
          <div class="sms-row">
            <input v-model="form.code" class="sms-input" placeholder="6 位验证码" maxlength="6" />
            <button class="sms-btn" type="button" :disabled="countdown > 0" @click="sendCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </div>
        </div>

        <label class="field">
          <span class="label-text">新密码</span>
          <input v-model="form.newPassword" type="password" placeholder="至少 6 位" autocomplete="new-password" />
        </label>

        <label class="field">
          <span class="label-text">确认新密码</span>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" autocomplete="new-password" />
        </label>

        <button class="primary-btn" type="submit" :disabled="loading">
          {{ loading ? '提交中…' : '重置密码' }}
        </button>
      </form>

      <p class="bottom-tip">演示账号手机号：管理员 13800000000 / 学生 13900000000</p>
    </div>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }
.forgot-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(160deg, #eef4ff 0%, #f8fafc 55%, #ecfeff 100%);
}
.card {
  width: 100%; max-width: 400px; background: #fff; border-radius: 18px;
  padding: 34px; box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08); border: 1px solid #f0f0f0;
}
.back-link { font-size: 12px; color: #94a3b8; text-decoration: none; display: inline-block; margin-bottom: 18px; }
.back-link:hover { color: #2563eb; }

h2 { font-size: 24px; font-weight: 700; color: #0f172a; margin: 0 0 6px; }
.sub { color: #94a3b8; font-size: 13px; margin: 0 0 24px; }

.steps { display: flex; align-items: center; gap: 0; margin-bottom: 6px; }
.step-dot {
  width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; background: #f1f5f9; color: #94a3b8; flex-shrink: 0;
  transition: all 0.2s;
}
.step-dot.done { background: #2563eb; color: #fff; }
.step-line { flex: 1; height: 2px; background: #e2e8f0; }
.step-line.done { background: #2563eb; }
.step-labels { display: flex; justify-content: space-between; font-size: 11px; color: #94a3b8; margin-bottom: 22px; }

.form-body { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 7px; }
.label-text { font-size: 13px; color: #475569; font-weight: 500; }
.field input {
  width: 100%; padding: 11px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color 0.18s;
}
.field input:focus { border-color: #2563eb; }

.sms-row { display: flex; gap: 10px; }
.sms-input { flex: 1; }
.sms-btn {
  flex-shrink: 0; border: 1.5px solid #2563eb; background: #fff; color: #2563eb;
  border-radius: 10px; padding: 0 16px; font-size: 13px; cursor: pointer; white-space: nowrap;
}
.sms-btn:disabled { border-color: #cbd5e1; color: #94a3b8; cursor: not-allowed; }

.primary-btn {
  margin-top: 8px; width: 100%; padding: 13px; border: none; border-radius: 10px;
  background: #2563eb; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
}
.primary-btn:hover:not(:disabled) { background: #1d4ed8; }
.primary-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.bottom-tip { margin: 18px 0 0; font-size: 12px; color: #94a3b8; text-align: center; line-height: 1.6; }
</style>
