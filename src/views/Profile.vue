<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

function goLogin() {
  router.push('/login')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  location.reload()
}
</script>

<template>
  <div class="profile-page">
    <header class="navbar">
      <div class="logo">🛍️ 校园二手集市</div>
      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/product">商品市场</router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>
    </header>

    <main class="profile-content">
      <div class="profile-card">
        <div class="avatar">👤</div>
        <h1>个人中心</h1>
        <p>{{ isLoggedIn ? '欢迎回来' : '欢迎来到校园二手集市' }}</p>

        <div class="info">
          <div>
            <span>用户名</span>
            <strong>{{ userInfo?.nickName || userInfo?.studentNo || '未登录' }}</strong>
          </div>
          <div>
            <span>角色</span>
            <strong>{{ userInfo?.roleName || '—' }}</strong>
          </div>
          <div>
            <span>我的收藏</span>
            <strong>2 件</strong>
          </div>
        </div>

        <div class="actions">
          <button v-if="!isLoggedIn" class="login-btn" @click="goLogin">登 录</button>
          <template v-else>
            <div class="quick-links">
              <button @click="router.push('/my-publishes')">我的发布</button>
              <button @click="router.push('/my-favorites')">我的收藏</button>
              <button @click="router.push('/my-orders')">我的订单</button>
              <button v-if="userInfo?.roleCode === 'ADMIN' || userInfo?.userType === 9" @click="router.push('/admin')">后台管理</button>
            </div>
            <button class="logout-btn" @click="logout">退出登录</button>
          </template>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.profile-page { min-height: 100vh; background: var(--bg); color: var(--text); }

.navbar {
  position: sticky; top: 0; z-index: 100;
  height: 64px; background: rgba(255,255,255,0.9); backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 5%; border-bottom: 1px solid var(--border);
}
.logo { font-size: 18px; font-weight: 700; }
.nav-menu { display: flex; gap: 28px; }
.nav-menu a { text-decoration: none; color: var(--text-secondary); font-size: 14px; transition: color 0.2s; }
.nav-menu a:hover, .nav-menu a.router-link-active { color: var(--primary); }

.profile-content { padding: 60px 5%; display: flex; justify-content: center; }

.profile-card {
  width: 560px; background: #fff; border-radius: 20px; padding: 40px;
  text-align: center; border: 1px solid var(--border); box-shadow: var(--shadow);
}

.avatar {
  width: 80px; height: 80px; margin: 0 auto 16px;
  border-radius: 50%; background: var(--primary-light);
  display: flex; align-items: center; justify-content: center; font-size: 40px;
}

.profile-card h1 { font-size: 26px; margin-bottom: 8px; }
.profile-card p { color: var(--text-light); font-size: 14px; }

.info { margin-top: 32px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.info div { background: var(--bg); padding: 18px; border-radius: 12px; }
.info span { display: block; color: var(--text-light); font-size: 13px; margin-bottom: 6px; }
.info strong { color: var(--primary); font-size: 17px; }

.actions { margin-top: 28px; display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; }

.login-btn, .logout-btn {
  border: none; padding: 10px 28px; border-radius: 10px;
  font-size: 14px; cursor: pointer; transition: 0.2s;
}
.login-btn { background: var(--primary); color: #fff; }
.login-btn:hover { background: var(--primary-hover); }
.logout-btn { background: var(--bg); color: var(--text-secondary); }
.logout-btn:hover { background: var(--border); }

.quick-links { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; margin-bottom: 12px; width: 100%; }
.quick-links button {
  border: 1px solid var(--border); background: #fff; color: var(--text-secondary);
  padding: 7px 14px; border-radius: 8px; cursor: pointer; font-size: 13px; transition: 0.2s;
}
.quick-links button:hover { border-color: var(--primary); color: var(--primary); }

@media (max-width: 700px) {
  .profile-card { width: auto; padding: 28px; }
  .info { grid-template-columns: 1fr; }
}
</style>
