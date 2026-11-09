<script setup>
// ============================================================
// 登录页
// ------------------------------------------------------------
// 三种形态：密码登录（主）、短信登录、扫码登录（占位）
// 密码登录含图形验证码，错误后自动刷新验证码
// ============================================================
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, loginBySms, sendSmsCode } from '../api/auth.js'
import { saveAuth, loadAndCacheMenus, isLoggedIn } from '../utils/auth.js'
import CaptchaBox from '../components/CaptchaBox.vue'

const route = useRoute()
const router = useRouter()

// 已登录直接回跳
if (isLoggedIn()) {
  const redirect = route.query.redirect
  router.replace(typeof redirect === 'string' ? redirect : '/')
}

const mode = ref('password') // password | sms | qr
const loading = ref(false)

// ---- 密码登录表单 ----
const form = ref({ username: '', password: '', captchaCode: '' })
const captchaKey = ref('')
const captchaRef = ref(null)

// ---- 短信登录表单 ----
const smsForm = ref({ phone: '', code: '' })
const smsCountdown = ref(0)
const smsTimer = ref(null)

function startCountdown() {
  smsCountdown.value = 60
  clearInterval(smsTimer.value)
  smsTimer.value = setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0) clearInterval(smsTimer.value)
  }, 1000)
}

async function sendCode() {
  if (smsCountdown.value > 0) return
  if (!/^1[3-9]\d{9}$/.test(smsForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    const res = await sendSmsCode(smsForm.value.phone)
    startCountdown()
    // 真后端字段是 devCode，Mock 是 debugCode，两个都兼容
    const dev = res?.devCode ?? res?.debugCode
    if (dev) {
      ElMessage.success(`验证码已发送（开发环境验证码：${dev}）`)
    } else {
      ElMessage.success('验证码已发送')
    }
  } catch (e) {
    ElMessage.error(e.message || '发送失败')
  }
}

/** 登录成功后的统一处理 */
async function afterLogin(res) {
  saveAuth({
    token: res.token,
    user: res.user,
    roles: res.roles || [],
    perms: res.perms || [],
  })
  await loadAndCacheMenus()
  ElMessage.success('登录成功')
  const redirect = route.query.redirect
  router.push(typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/')
}

async function submitPassword() {
  if (!form.value.username.trim()) { ElMessage.warning('请输入学号或手机号'); return }
  if (!form.value.password) { ElMessage.warning('请输入密码'); return }
  if (!form.value.captchaCode.trim()) { ElMessage.warning('请输入图形验证码'); return }

  loading.value = true
  try {
    const res = await login({
      username: form.value.username.trim(),
      password: form.value.password,
      captchaKey: captchaKey.value,
      captchaCode: form.value.captchaCode.trim(),
    })
    await afterLogin(res)
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
    // 验证码一次性，登录失败必须换一张
    form.value.captchaCode = ''
    captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}

async function submitSms() {
  if (!/^1[3-9]\d{9}$/.test(smsForm.value.phone)) { ElMessage.warning('请输入正确的手机号'); return }
  if (!smsForm.value.code.trim()) { ElMessage.warning('请输入短信验证码'); return }

  loading.value = true
  try {
    // 短信分支：captchaKey 传 sms:{手机号}，后端据此走短信校验，不再校验图形码
    const res = await loginBySms({
      phone: smsForm.value.phone,
      smsCode: smsForm.value.code.trim(),
    })
    await afterLogin(res)
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const switchTabs = [
  { key: 'password', label: '密码登录' },
  { key: 'sms', label: '短信登录' },
  { key: 'qr', label: '扫码登录' },
]

function switchMode(t) {
  if (t.disabled) {
    ElMessage.info(t.tip || '该登录方式暂未开放')
    return
  }
  mode.value = t.key
}

const titleText = computed(() => {
  if (mode.value === 'sms') return '手机号快捷登录'
  if (mode.value === 'qr') return '扫码登录'
  return '欢迎回来'
})
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <aside class="brand-panel">
      <div class="brand-top">
        <div class="brand-logo">🛍️</div>
        <h1>校园集市</h1>
        <p class="brand-sub">同校交易 · 闲置变现 · 当面验货</p>
      </div>

      <ul class="brand-points">
        <li><span class="dot"></span>校园实名认证，买卖更放心</li>
        <li><span class="dot"></span>同校当面交易，不用等快递</li>
        <li><span class="dot"></span>AI 智能助手，帮你快速找货</li>
      </ul>

      <div class="brand-foot">© 2026 校园二手集市 · 课程设计项目</div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="form-panel">
      <div class="form-card">
        <h2>{{ titleText }}</h2>
        <p class="form-sub">登录校园集市，继续你的闲置交易</p>

        <!-- 形态切换 -->
        <div class="tabs">
          <button
            v-for="t in switchTabs"
            :key="t.key"
            :class="['tab', { active: mode === t.key, disabled: t.disabled }]"
            type="button"
            :aria-disabled="t.disabled ? 'true' : 'false'"
            :title="t.disabled ? t.tip : ''"
            @click="switchMode(t)"
          >
            {{ t.label }}
          </button>
        </div>

        <!-- ========== 密码登录 ========== -->
        <form v-if="mode === 'password'" class="form-body" @submit.prevent="submitPassword">
          <label class="field">
            <span class="label-text">学号 / 手机号</span>
            <input v-model="form.username" placeholder="请输入学号或手机号" autocomplete="username" />
          </label>

          <label class="field">
            <span class="label-text">密码</span>
            <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
          </label>

          <div class="field">
            <span class="label-text">图形验证码</span>
            <div class="captcha-row">
              <input
                v-model="form.captchaCode"
                class="captcha-input"
                placeholder="输入右侧 4 位字符"
                maxlength="6"
                @keyup.enter="submitPassword"
              />
              <CaptchaBox ref="captchaRef" v-model="captchaKey" />
            </div>
          </div>

          <div class="link-row">
            <router-link to="/forgot" class="text-link">忘记密码？</router-link>
          </div>

          <button class="primary-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中…' : '登 录' }}
          </button>

          <p class="bottom-tip">
            还没有账号？<router-link to="/register" class="text-link strong">立即注册</router-link>
          </p>
        </form>

        <!-- ========== 短信登录 ========== -->
        <form v-else-if="mode === 'sms'" class="form-body" @submit.prevent="submitSms">
          <label class="field">
            <span class="label-text">手机号</span>
            <input v-model="smsForm.phone" placeholder="请输入手机号" maxlength="11" />
          </label>

          <div class="field">
            <span class="label-text">短信验证码</span>
            <div class="sms-row">
              <input v-model="smsForm.code" class="sms-input" placeholder="6 位验证码" maxlength="6" />
              <button class="sms-btn" type="button" :disabled="smsCountdown > 0" @click="sendCode">
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
              </button>
            </div>
          </div>

          <button class="primary-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中…' : '登 录' }}
          </button>

          <p class="bottom-tip">演示手机号：13800000008（管理员）/ 开发期验证码会直接提示在页面上</p>
        </form>

        <!-- ========== 扫码登录（占位） ========== -->
        <div v-else class="qr-body">
          <div class="qr-box">
            <div class="qr-grid">
              <span v-for="i in 64" :key="i" :class="['qr-cell', { dark: (i * 7 + 3) % 3 === 0 }]"></span>
            </div>
            <div class="qr-corner tl"></div>
            <div class="qr-corner tr"></div>
            <div class="qr-corner bl"></div>
          </div>
          <p class="qr-tip">打开手机校园集市 App 扫码登录</p>
          <p class="qr-note">该功能待接入，当前版本请使用密码登录</p>
          <button class="ghost-btn" type="button" @click="mode = 'password'">返回密码登录</button>
        </div>

        <!-- 测试账号提示 -->
        <div class="demo-tip">
          <p><strong>测试账号</strong></p>
          <p>管理员：2024110640 / 123456</p>
          <p>普通学生：2024777777 / 123456</p>
        </div>

        <router-link to="/" class="back-home">← 返回首页</router-link>
      </div>
    </main>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.login-page { min-height: 100vh; display: flex; background: #f8fafc; }

/* ================= 左侧品牌 ================= */
.brand-panel {
  flex: 1;
  background: linear-gradient(160deg, #0f172a 0%, #1e40af 55%, #0891b2 100%);
  color: #fff;
  padding: 64px 56px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}
.brand-panel::after {
  content: '';
  position: absolute;
  width: 420px; height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.12), transparent 68%);
  right: -140px; bottom: -120px;
  pointer-events: none;
}
.brand-top { position: relative; z-index: 1; }
.brand-logo { font-size: 52px; margin-bottom: 18px; }
.brand-top h1 { font-size: 34px; font-weight: 800; margin: 0 0 10px; letter-spacing: 1px; }
.brand-sub { font-size: 15px; opacity: 0.82; margin: 0; letter-spacing: 0.5px; }

.brand-points { list-style: none; padding: 0; margin: 48px 0 0; position: relative; z-index: 1; }
.brand-points li { display: flex; align-items: center; gap: 10px; font-size: 14px; opacity: 0.92; margin-bottom: 16px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #67e8f9; flex-shrink: 0; }

.brand-foot { font-size: 12px; opacity: 0.55; position: relative; z-index: 1; }

/* ================= 右侧表单 ================= */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 32px;
  background: #fff;
}
.form-card { width: 100%; max-width: 380px; }

.form-card h2 { font-size: 26px; font-weight: 700; color: #0f172a; margin: 0 0 6px; }
.form-sub { color: #94a3b8; font-size: 13px; margin: 0 0 24px; }

/* Tab */
.tabs { display: flex; gap: 4px; background: #f1f5f9; padding: 4px; border-radius: 12px; margin-bottom: 22px; }
.tab {
  flex: 1; border: none; background: transparent; padding: 8px 0;
  font-size: 13px; color: #64748b; cursor: pointer; border-radius: 9px;
  transition: all 0.18s;
}
.tab.active { background: #fff; color: #0f172a; font-weight: 600; box-shadow: 0 1px 4px rgba(15,23,42,0.08); }
.tab.disabled, .tab.disabled:hover {
  color: #cbd5e1; cursor: not-allowed; background: transparent;
  text-decoration: line-through; text-decoration-color: #e2e8f0;
}

/* 字段 */
.form-body { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 7px; }
.label-text { font-size: 13px; color: #475569; font-weight: 500; }
.field input {
  width: 100%; padding: 11px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color 0.18s; background: #fff;
}
.field input:focus { border-color: #2563eb; }

.captcha-row { display: flex; gap: 10px; align-items: center; }
.captcha-input { flex: 1; }

.sms-row { display: flex; gap: 10px; }
.sms-input { flex: 1; }
.sms-btn {
  flex-shrink: 0; border: 1.5px solid #2563eb; background: #fff; color: #2563eb;
  border-radius: 10px; padding: 0 16px; font-size: 13px; cursor: pointer; white-space: nowrap;
}
.sms-btn:disabled { border-color: #cbd5e1; color: #94a3b8; cursor: not-allowed; }

.link-row { display: flex; justify-content: flex-end; margin-top: -4px; }
.text-link { font-size: 12px; color: #2563eb; text-decoration: none; }
.text-link:hover { text-decoration: underline; }
.text-link.strong { font-weight: 600; }

.primary-btn {
  margin-top: 4px; width: 100%; padding: 13px; border: none; border-radius: 10px;
  background: #2563eb; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: background 0.18s;
}
.primary-btn:hover:not(:disabled) { background: #1d4ed8; }
.primary-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.bottom-tip { margin: 4px 0 0; font-size: 12px; color: #94a3b8; text-align: center; }

/* 扫码 */
.qr-body { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 8px 0 4px; }
.qr-box {
  position: relative; width: 180px; height: 180px; padding: 12px;
  border: 1px solid #e2e8f0; border-radius: 14px; background: #fff;
}
.qr-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 2px; width: 100%; height: 100%; }
.qr-cell { border-radius: 2px; background: #fff; }
.qr-cell.dark { background: #0f172a; }
.qr-corner { position: absolute; width: 26px; height: 26px; border: 3px solid #0f172a; border-radius: 4px; }
.qr-corner.tl { top: 14px; left: 14px; border-right: none; border-bottom: none; }
.qr-corner.tr { top: 14px; right: 14px; border-left: none; border-bottom: none; }
.qr-corner.bl { bottom: 14px; left: 14px; border-right: none; border-top: none; }
.qr-tip { font-size: 13px; color: #334155; margin: 0; }
.qr-note { font-size: 12px; color: #94a3b8; margin: 0; }
.ghost-btn {
  border: 1px solid #e2e8f0; background: #fff; color: #475569;
  padding: 8px 20px; border-radius: 18px; font-size: 13px; cursor: pointer;
}

/* 测试账号 */
.demo-tip {
  margin-top: 24px; background: #f8fafc; border: 1px dashed #e2e8f0;
  border-radius: 10px; padding: 12px 14px; font-size: 12px; color: #64748b; line-height: 1.7;
}
.demo-tip p { margin: 0; }
.demo-tip p strong { color: #334155; }

.back-home { display: block; text-align: center; margin-top: 20px; font-size: 12px; color: #94a3b8; text-decoration: none; }
.back-home:hover { color: #2563eb; }

@media (max-width: 900px) {
  .brand-panel { display: none; }
}
</style>
