<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

function goLogin() { router.push('/login') }
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  location.reload()
}

const myItems = [
  { icon: '📦', title: '我的发布', desc: '管理我发布的商品', path: '/my-publishes' },
  { icon: '❤️', title: '我的收藏', desc: '我收藏的宝贝', path: '/my-favorites' },
  { icon: '📋', title: '我的订单', desc: '交易订单记录', path: '/my-orders' },
  { icon: '💬', title: '消息聊天', desc: '买家卖家沟通', path: '/chat' },
  { icon: '🚩', title: '举报记录', desc: '我提交的举报', path: '/report/1' },
  { icon: '⚙️', title: '设置', desc: '账号与安全', path: '/profile' },
]
</script>

<template>
  <div class="profile-page">
    <header class="navbar">
      <div class="nav-inner">
        <div class="logo" @click="router.push('/')">
          <span class="logo-icon">🛍️</span><span class="logo-text">校园集市</span>
        </div>
        <nav class="nav-links">
          <router-link to="/" exact-active-class="active">首页</router-link>
          <router-link to="/product" active-class="active">商品市场</router-link>
          <router-link to="/publish" active-class="active">发布商品</router-link>
          <router-link to="/categories" active-class="active">分类浏览</router-link>
          <router-link to="/ai" active-class="active">AI助手</router-link>
          <router-link to="/profile" active-class="active">个人中心</router-link>
          <router-link to="/admin" active-class="active">管理后台</router-link>
        </nav>
        <div class="nav-actions">
          <router-link to="/publish" class="publish-btn">＋ 发布</router-link>
        </div>
      </div>
    </header>

    <main class="container">
      <!-- 用户信息卡 -->
      <div class="user-card">
        <div class="user-left">
          <div class="avatar">👤</div>
          <div class="user-info">
            <div class="name-row">
              <h2>{{ userInfo?.nickName || userInfo?.studentNo || '未登录' }}</h2>
              <span v-if="userInfo?.roleCode === 'ADMIN'" class="role-tag admin">管理员</span>
              <span v-else-if="isLoggedIn" class="role-tag">学生</span>
            </div>
            <p class="user-id">学号：{{ userInfo?.studentNo || '—' }}</p>
          </div>
        </div>
        <button v-if="!isLoggedIn" class="login-btn" @click="goLogin">立即登录</button>
        <button v-else class="logout-btn" @click="logout">退出登录</button>
      </div>

      <!-- 统计 -->
      <div class="stats">
        <div class="stat"><strong>3</strong><span>已发布</span></div>
        <div class="stat"><strong>2</strong><span>收藏</span></div>
        <div class="stat"><strong>1</strong><span>进行中订单</span></div>
      </div>

      <!-- 功能宫格 -->
      <div class="section-head"><h3>我的服务</h3></div>
      <div class="grid">
        <div v-for="item in myItems" :key="item.title" class="grid-card" @click="router.push(item.path)">
          <div class="grid-icon">{{ item.icon }}</div>
          <div class="grid-title">{{ item.title }}</div>
          <div class="grid-desc">{{ item.desc }}</div>
        </div>
      </div>

      <!-- 管理员入口 -->
      <div v-if="userInfo?.roleCode === 'ADMIN'" class="admin-banner" @click="router.push('/admin')">
        <span>🔐</span>
        <div>
          <strong>进入管理后台</strong>
          <p>用户管理 · 商品审核 · 角色权限</p>
        </div>
        <span class="arrow">→</span>
      </div>
    </main>
  </div>
</template>

<style scoped>
.profile-page { min-height: 100vh; background: #fafbfc; }
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; }

.container { max-width: 1000px; margin: 0 auto; padding: 32px 24px; }

/* 用户卡 */
.user-card {
  background: linear-gradient(135deg, #2563eb 0%, #06b6d4 100%);
  border-radius: 20px; padding: 28px; color: #fff;
  display: flex; justify-content: space-between; align-items: center;
}
.user-left { display: flex; align-items: center; gap: 16px; }
.avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 32px;
}
.name-row { display: flex; align-items: center; gap: 10px; }
.name-row h2 { margin: 0; font-size: 22px; }
.role-tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; background: rgba(255,255,255,0.25); }
.role-tag.admin { background: #fbbf24; color: #92400e; }
.user-id { margin: 4px 0 0; font-size: 13px; opacity: 0.8; }
.login-btn, .logout-btn {
  border: none; padding: 10px 20px; border-radius: 20px; font-size: 14px; cursor: pointer;
}
.login-btn { background: #fff; color: #2563eb; font-weight: 600; }
.logout-btn { background: rgba(255,255,255,0.2); color: #fff; }

/* 统计 */
.stats {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 20px 0;
}
.stat {
  background: #fff; border-radius: 14px; padding: 18px; text-align: center;
  border: 1px solid #f0f0f0;
}
.stat strong { display: block; font-size: 22px; color: #0f172a; }
.stat span { font-size: 12px; color: #94a3b8; }

/* 功能宫格 */
.section-head { margin: 24px 0 16px; }
.section-head h3 { font-size: 17px; color: #0f172a; }
.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.grid-card {
  background: #fff; border-radius: 14px; padding: 20px; cursor: pointer;
  border: 1px solid #f0f0f0; transition: all .2s;
}
.grid-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
.grid-icon { font-size: 28px; margin-bottom: 8px; }
.grid-title { font-size: 14px; font-weight: 600; color: #1e293b; }
.grid-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 管理员入口 */
.admin-banner {
  margin-top: 20px; background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 14px; padding: 18px 20px; display: flex; align-items: center; gap: 14px; cursor: pointer;
}
.admin-banner > span:first-child { font-size: 28px; }
.admin-banner strong { color: #92400e; font-size: 15px; }
.admin-banner p { margin: 2px 0 0; font-size: 12px; color: #b45309; }
.admin-banner .arrow { margin-left: auto; color: #b45309; }

@media (max-width: 700px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
  .user-card { flex-direction: column; gap: 16px; }
}
</style>
