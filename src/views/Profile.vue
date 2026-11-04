<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 从 localStorage 读取登录用户信息
const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || 'null')
  } catch {
    return null
  }
})

const isLoggedIn = computed(() => !!localStorage.getItem('token') && !!userInfo.value)

function goLogin() {
  router.push('/login')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <div class="profile-page">

    <header class="navbar">
      <div class="logo">
        🛍️ 校园二手集市
      </div>

      <nav class="nav-menu">

        <router-link to="/">
          首页
        </router-link>

        <router-link to="/product">
          商品市场
        </router-link>

        <router-link to="/publish">
          发布商品
        </router-link>

        <router-link to="/categories">
          分类管理
        </router-link>

        <router-link to="/ai">
          AI助手
        </router-link>

        <router-link to="/profile">
          个人中心
        </router-link>

        <router-link to="/admin">
          权限管理
        </router-link>

      </nav>
    </header>


    <main class="profile-content">

      <div class="profile-card">

        <div class="avatar">
          👤
        </div>

        <h1>个人中心</h1>

        <p>{{ isLoggedIn ? '欢迎回来，正在使用系统' : '欢迎来到校园二手集市个人中心' }}</p>

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
          <button v-if="!isLoggedIn" class="login-btn" @click="goLogin">
            登 录
          </button>
          <template v-else>
            <div class="quick-links">
              <button @click="router.push('/my-publishes')">我的发布</button>
              <button @click="router.push('/my-favorites')">我的收藏</button>
              <button @click="router.push('/my-orders')">我的订单</button>
              <button v-if="userInfo?.roleCode === 'ADMIN' || userInfo?.userType === 9" @click="router.push('/admin')">后台管理</button>
            </div>
            <button class="logout-btn" @click="logout">
              退出登录
            </button>
          </template>
        </div>

      </div>

    </main>

  </div>
</template>


<style scoped>

.profile-page {
  min-height: 100vh;
  background: #f7f8fc;
  color: #222;
}


/* 顶部导航 */

.navbar {
  height: 70px;
  background: white;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 0 10%;

  border-bottom: 1px solid #eee;
}

.logo {
  font-size: 22px;
  font-weight: bold;
}

.nav-menu {
  display: flex;
  gap: 35px;
}

.nav-menu a {
  text-decoration: none;
  color: #666;
}

.nav-menu a.router-link-active {
  color: #6c5ce7;
  font-weight: bold;
}


/* 页面 */

.profile-content {
  padding: 80px 10%;
  display: flex;
  justify-content: center;
}


/* 卡片 */

.profile-card {
  width: 600px;
  background: white;
  border-radius: 20px;
  padding: 50px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 90px;
  height: 90px;

  margin: 0 auto 20px;

  border-radius: 50%;

  background: #eee9ff;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 45px;
}

.profile-card h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.profile-card p {
  color: #888;
}


/* 信息 */

.info {
  margin-top: 35px;

  display: grid;
  grid-template-columns: repeat(3, 1fr);

  gap: 20px;
}

.info div {
  background: #f7f5ff;

  padding: 20px;

  border-radius: 12px;
}

.info span {
  display: block;

  color: #888;

  font-size: 14px;

  margin-bottom: 8px;
}

.info strong {
  color: #6c5ce7;
  font-size: 18px;
}

/* 登录 / 退出按钮 */
.actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 14px;
}

.login-btn,
.logout-btn {
  border: none;
  padding: 11px 34px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
}

.login-btn {
  background: #6c5ce7;
  color: #fff;
}

.login-btn:hover { background: #5a4bd1; }

.logout-btn {
  background: #f1f1f6;
  color: #666;
}

.logout-btn:hover { background: #e6e6ee; }

.quick-links {
  display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; margin-bottom: 16px;
}
.quick-links button {
  border: 1px solid #ddd; background: #fff; color: #555;
  padding: 8px 16px; border-radius: 8px; cursor: pointer; font-size: 13px;
}
.quick-links button:hover { border-color: #6c5ce7; color: #6c5ce7; }


@media (max-width: 700px) {

  .navbar {
    padding: 0 5%;
  }

  .nav-menu {
    gap: 15px;
  }

  .profile-card {
    width: auto;
    padding: 30px;
  }

  .info {
    grid-template-columns: 1fr;
  }

}

</style>