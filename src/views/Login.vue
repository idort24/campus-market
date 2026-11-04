<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { USE_MOCK } from '../api/config.js'
import http from '../api/http.js'

const route = useRoute()
const router = useRouter()

const studentNo = ref('')
const password = ref('')
const loading = ref(false)

// Mock 演示账号：任意学号 + 密码 123456 即可登录（管理员角色）
async function onLogin() {
  if (!studentNo.value.trim()) {
    ElMessage.warning('请输入学号')
    return
  }
  if (!password.value) {
    ElMessage.warning('请输入密码')
    return
  }

  loading.value = true
  try {
    let userInfo
    if (USE_MOCK) {
      // Mock 模式：直接模拟登录成功
      await new Promise((r) => setTimeout(r, 500))
      userInfo = {
        id: 1,
        studentNo: studentNo.value.trim(),
        nickName: '何曼妮',
        roleCode: 'ADMIN', // 演示管理员角色，可访问权限管理
        roleName: '管理员',
        perms: ['user:ban', 'goods:audit', 'role:assign']
      }
      localStorage.setItem('token', 'mock-token-' + Date.now())
    } else {
      // 真实后端：POST /api/v1/user/login（经 Vite 代理转发到后端）
      // 返回格式：{ code: 1, msg: "操作成功", data: { token, user } }
      // user 里字段是驼峰：studentNo / nickName / roleCode / perms
      const res = await http.post('/user/login', {
        username: studentNo.value.trim(),
        password: password.value
      })
      localStorage.setItem('token', res.token)
      userInfo = res.user
    }

    localStorage.setItem('userInfo', JSON.stringify(userInfo))

    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <h1>用户登录</h1>
      <p class="sub">登录后可发布商品、收藏商品、管理权限</p>

      <form @submit.prevent="onLogin">
        <div class="form-item">
          <label>学号</label>
          <input v-model="studentNo" type="text" placeholder="例如：2024110606" />
        </div>

        <div class="form-item">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="演示密码：123456" />
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <p class="tip">
        💡 Mock 演示模式：任意学号 + 密码 123456 登录，默认管理员角色
      </p>

      <button class="back-btn" @click="router.push('/')">← 返回首页</button>
    </div>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0edff, #f7f8fc);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 18px;
  padding: 40px 38px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 24px;
}

.logo-icon { font-size: 26px; }

.login-card h1 {
  margin: 0 0 6px;
  font-size: 26px;
}

.sub {
  margin: 0 0 28px;
  color: #888;
  font-size: 13px;
}

.form-item {
  margin-bottom: 18px;
  text-align: left;
}

.form-item label {
  display: block;
  margin-bottom: 7px;
  font-size: 13px;
  color: #555;
  font-weight: 600;
}

.form-item input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e3e3ec;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  background: #fafafc;
  transition: border-color 0.2s;
}

.form-item input:focus {
  border-color: #6c5ce7;
  background: #fff;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  padding: 13px;
  border: none;
  border-radius: 10px;
  background: #6c5ce7;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.login-btn:hover:not(:disabled) { background: #5a4bd1; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.tip {
  margin: 18px 0 0;
  color: #999;
  font-size: 12px;
  line-height: 1.7;
}

.back-btn {
  margin-top: 18px;
  border: none;
  background: none;
  color: #6c5ce7;
  cursor: pointer;
  font-size: 13px;
}
</style>
