<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http.js'
import { USE_MOCK } from '../api/config.js'

const router = useRouter()
const studentNo = ref('')
const password = ref('')
const loading = ref(false)

async function login() {
  if (!studentNo.value.trim()) { ElMessage.warning('请输入学号'); return }
  if (!password.value) { ElMessage.warning('请输入密码'); return }
  loading.value = true
  try {
    let userInfo
    if (USE_MOCK) {
      await new Promise(r => setTimeout(r, 500))
      userInfo = {
        id: 1,
        studentNo: studentNo.value.trim(),
        nickName: '何曼妮',
        roleCode: 'ADMIN',
        roleName: '管理员',
        perms: ['user:ban', 'goods:audit', 'role:assign']
      }
      localStorage.setItem('token', 'mock-token-' + Date.now())
    } else {
      const res = await http.post('/user/login', {
        username: studentNo.value.trim(),
        password: password.value
      })
      localStorage.setItem('token', res.token)
      userInfo = res.user
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-logo">🛍️</div>
        <h1>校园二手集市</h1>
        <p>同校交易，闲置变现金</p>
      </div>
      <div class="brand-features">
        <div class="feature"><span class="dot"></span> 实名认证，同校更安心</div>
        <div class="feature"><span class="dot"></span> 当面交易，不用等快递</div>
        <div class="feature"><span class="dot"></span> AI 智能助手，找货更快</div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="sub">登录你的校园集市账号</p>

        <div class="form-item">
          <label>学号</label>
          <input v-model="studentNo" placeholder="请输入学号" @keyup.enter="login" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="login" />
        </div>
        <button class="login-btn" :disabled="loading" @click="login">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
        <p class="tip">管理员测试号：2024110640 / 123456</p>
        <button class="back-btn" @click="router.push('/')">← 返回首页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }
.login-page { min-height: 100vh; display: flex; }

/* 左侧品牌区 */
.login-left {
  flex: 1; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #06b6d4 100%);
  color: #fff; padding: 60px; display: flex; flex-direction: column; justify-content: center;
}
.brand-logo { font-size: 48px; margin-bottom: 16px; }
.login-left h1 { font-size: 36px; font-weight: 800; margin-bottom: 8px; }
.brand p { font-size: 16px; opacity: 0.85; }
.brand-features { margin-top: 48px; display: flex; flex-direction: column; gap: 16px; }
.feature { font-size: 14px; opacity: 0.9; display: flex; align-items: center; gap: 10px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #fff; opacity: 0.8; }

/* 右侧表单 */
.login-right { flex: 1; display: flex; align-items: center; justify-content: center; background: #fff; padding: 40px; }
.login-card { width: 100%; max-width: 360px; }
.login-card h2 { font-size: 28px; font-weight: 700; color: #0f172a; }
.sub { color: #94a3b8; font-size: 14px; margin: 8px 0 32px; }

.form-item { margin-bottom: 20px; }
.form-item label { display: block; margin-bottom: 8px; font-size: 13px; color: #475569; font-weight: 500; }
.form-item input {
  width: 100%; padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color .2s;
}
.form-item input:focus { border-color: #2563eb; }

.login-btn {
  width: 100%; padding: 13px; border: none; border-radius: 10px;
  background: #2563eb; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: background .2s; margin-top: 8px;
}
.login-btn:hover:not(:disabled) { background: #1d4ed8; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.tip { margin: 16px 0 0; color: #cbd5e1; font-size: 12px; text-align: center; }
.back-btn { margin: 20px auto 0; display: block; border: none; background: none; color: #2563eb; cursor: pointer; font-size: 13px; }

@media (max-width: 800px) {
  .login-left { display: none; }
}
</style>
