<script setup>
// ============================================================
// 注册页
// ------------------------------------------------------------
// 字段：学号 / 手机号 / 密码 / 确认密码
// 校验：学号 8-12 位数字、手机号 1[3-9] 开头、密码 ≥6 位、两次一致
// 注册成功默认绑定 STUDENT 角色（由后端完成，前端只做展示）
// ============================================================
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth.js'

const router = useRouter()
const loading = ref(false)

const form = ref({
  studentNo: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const errors = ref({})

const canSubmit = computed(() => {
  const f = form.value
  return f.studentNo && f.phone && f.password && f.confirmPassword
})

function validate() {
  const f = form.value
  const e = {}
  if (!/^\d{8,12}$/.test(f.studentNo.trim())) e.studentNo = '学号应为 8-12 位数字'
  if (!/^1[3-9]\d{9}$/.test(f.phone.trim())) e.phone = '请输入正确的 11 位手机号'
  if (!f.password || f.password.length < 6) e.password = '密码至少 6 位'
  if (f.confirmPassword !== f.password) e.confirmPassword = '两次输入的密码不一致'
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    await register({
      studentNo: form.value.studentNo.trim(),
      phone: form.value.phone.trim(),
      password: form.value.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="reg-card">
      <div class="brand-row">
        <span class="brand-icon">🛍️</span>
        <span class="brand-name">校园集市</span>
      </div>

      <h2>创建账号</h2>
      <p class="sub">注册后自动绑定普通学生角色，即可浏览和发布闲置</p>

      <form class="form-body" @submit.prevent="submit">
        <label class="field">
          <span class="label-text">学号</span>
          <input v-model="form.studentNo" placeholder="请输入学号（8-12 位数字）" maxlength="12" />
          <span v-if="errors.studentNo" class="err">{{ errors.studentNo }}</span>
        </label>

        <label class="field">
          <span class="label-text">手机号</span>
          <input v-model="form.phone" placeholder="请输入 11 位手机号" maxlength="11" />
          <span v-if="errors.phone" class="err">{{ errors.phone }}</span>
        </label>

        <label class="field">
          <span class="label-text">密码</span>
          <input v-model="form.password" type="password" placeholder="至少 6 位" autocomplete="new-password" />
          <span v-if="errors.password" class="err">{{ errors.password }}</span>
        </label>

        <label class="field">
          <span class="label-text">确认密码</span>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="new-password" />
          <span v-if="errors.confirmPassword" class="err">{{ errors.confirmPassword }}</span>
        </label>

        <button class="primary-btn" type="submit" :disabled="loading || !canSubmit">
          {{ loading ? '注册中…' : '注 册' }}
        </button>
      </form>

      <p class="bottom-tip">
        已有账号？<router-link to="/login" class="text-link">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(160deg, #eef4ff 0%, #f8fafc 55%, #ecfeff 100%);
}
.reg-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 18px;
  padding: 36px 34px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
  border: 1px solid #f0f0f0;
}

.brand-row { display: flex; align-items: center; gap: 8px; margin-bottom: 22px; }
.brand-icon { font-size: 24px; }
.brand-name { font-size: 16px; font-weight: 800; color: #0f172a; }

.reg-card h2 { font-size: 24px; font-weight: 700; color: #0f172a; margin: 0 0 6px; }
.sub { color: #94a3b8; font-size: 13px; margin: 0 0 26px; line-height: 1.6; }

.form-body { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 7px; }
.label-text { font-size: 13px; color: #475569; font-weight: 500; }
.field input {
  width: 100%; padding: 11px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color 0.18s;
}
.field input:focus { border-color: #2563eb; }
.err { font-size: 12px; color: #ef4444; }

.primary-btn {
  margin-top: 8px; width: 100%; padding: 13px; border: none; border-radius: 10px;
  background: #2563eb; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: background 0.18s;
}
.primary-btn:hover:not(:disabled) { background: #1d4ed8; }
.primary-btn:disabled { opacity: 0.55; cursor: not-allowed; }

.bottom-tip { margin: 20px 0 0; font-size: 12px; color: #94a3b8; text-align: center; }
.text-link { color: #2563eb; text-decoration: none; font-weight: 600; }
.text-link:hover { text-decoration: underline; }
</style>
