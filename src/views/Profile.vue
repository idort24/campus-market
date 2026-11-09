<script setup>
// ============================================================
// 个人中心
// ------------------------------------------------------------
// 退出登录：先调 POST /auth/logout（后端写 Redis 黑名单），成功后再清本地
// 管理后台入口：按后端菜单 / 权限判断，普通学生看不到
// ============================================================
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout as apiLogout } from '../api/auth.js'
import { clearAuth, getUserInfo, isAdmin, isLoggedIn } from '../utils/auth.js'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const loggingOut = ref(false)

const userInfo = computed(() => getUserInfo())
const loggedIn = computed(() => isLoggedIn())
const adminFlag = computed(() => isAdmin())

function goLogin() {
  router.push({ path: '/login', query: { redirect: '/profile' } })
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  loggingOut.value = true
  try {
    // 真实后端：把 token 写入 Redis 黑名单，立即失效
    await apiLogout()
    ElMessage.success('已退出登录')
  } catch {
    // 后端不可用时仍然要完成本地退出，避免卡在登录态
    ElMessage.warning('服务端退出失败，已在本地退出')
  } finally {
    clearAuth()
    loggingOut.value = false
    router.push('/')
  }
}

const myItems = [
  { icon: '📦', title: '我的发布', desc: '管理我发布的商品', path: '/my-publishes' },
  { icon: '❤️', title: '我的收藏', desc: '我收藏的宝贝', path: '/my-favorites' },
  { icon: '📋', title: '我的订单', desc: '交易订单记录', path: '/my-orders' },
  { icon: '💬', title: '消息聊天', desc: '买家卖家沟通', path: '/chat' },
  { icon: '🚩', title: '举报投诉', desc: '提交商品举报', path: '/report/1' },
  { icon: '⚙️', title: '账号设置', desc: '账号与安全', path: '/profile' },
]
</script>

<template>
  <div class="profile-page">
    <NavBar :show-publish="loggedIn" />

    <main class="container">
      <!-- 用户信息卡 -->
      <div class="user-card">
        <div class="user-left">
          <div class="avatar">👤</div>
          <div class="user-info">
            <div class="name-row">
              <h2>{{ userInfo?.nickName || userInfo?.studentNo || '未登录' }}</h2>
              <span v-if="adminFlag" class="role-tag admin">管理员</span>
              <span v-else-if="loggedIn" class="role-tag">学生</span>
            </div>
            <p class="user-id">
              学号：{{ userInfo?.studentNo || '—' }}
              <span v-if="userInfo?.phone" class="phone">· {{ userInfo.phone }}</span>
            </p>
          </div>
        </div>

        <button v-if="!loggedIn" class="login-btn" @click="goLogin">立即登录</button>
        <button v-else class="logout-btn" :disabled="loggingOut" @click="handleLogout">
          {{ loggingOut ? '退出中…' : '退出登录' }}
        </button>
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

      <!-- 管理员入口（仅管理员可见） -->
      <div v-if="adminFlag" class="admin-banner" @click="router.push('/admin')">
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
.role-tag.admin { background: #fbbf24; color: #92400e; font-weight: 600; }
.user-id { margin: 4px 0 0; font-size: 13px; opacity: 0.85; }
.phone { opacity: 0.75; }
.login-btn, .logout-btn {
  border: none; padding: 10px 20px; border-radius: 20px; font-size: 14px; cursor: pointer;
}
.login-btn { background: #fff; color: #2563eb; font-weight: 600; }
.logout-btn { background: rgba(255,255,255,0.2); color: #fff; }
.logout-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* 统计 */
.stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 20px 0; }
.stat { background: #fff; border-radius: 14px; padding: 18px; text-align: center; border: 1px solid #f0f0f0; }
.stat strong { display: block; font-size: 22px; color: #0f172a; }
.stat span { font-size: 12px; color: #94a3b8; }

/* 功能宫格 */
.section-head { margin: 24px 0 16px; }
.section-head h3 { font-size: 17px; color: #0f172a; margin: 0; }
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
