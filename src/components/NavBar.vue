<script setup>
// ============================================================
// 统一顶部导航栏
// ------------------------------------------------------------
// 全站用户端页面共用，保证导航完全一致（此前各页面各写一份导致不一致）。
// 菜单构成：
//   1. 用户端固定入口（首页/商品市场/分类浏览/AI助手/个人中心）—— 未登录也能浏览
//   2. 「管理后台」—— 仅当后端 /user/menus 下发了 /admin，或账号具备管理权限时才出现
// 也就是说：普通学生登录看不到「管理后台」，管理员登录才多出这一项。
// ============================================================
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getUserPerms, isAdmin } from '../utils/auth.js'

defineProps({
  // 隐藏右上角「＋发布」按钮（发布页自身可传 false）
  showPublish: { type: Boolean, default: true },
})

const router = useRouter()

const LS_MENUS = 'userMenus'

// 用户端固定入口
const baseMenus = [
  { path: '/', title: '首页' },
  { path: '/product', title: '商品市场' },
  { path: '/categories', title: '分类浏览' },
  { path: '/ai', title: 'AI 助手' },
  { path: '/profile', title: '个人中心' },
]

const menus = ref([])
const token = ref(localStorage.getItem('token'))

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
})
const isLoggedIn = computed(() => !!token.value)
const displayName = computed(() => userInfo.value?.nickName || userInfo.value?.studentNo || '')

function hasAdminEntry() {
  // 后端菜单里明确下发了 /admin
  try {
    const cached = JSON.parse(localStorage.getItem(LS_MENUS) || '[]')
    if (Array.isArray(cached) && cached.some((m) => m && m.path === '/admin')) return true
  } catch { /* ignore */ }
  // 或者账号本身具备管理权限
  return isAdmin() || getUserPerms().length > 0
}

// 已登录 → 基础菜单 + 管理后台（若有权限）；未登录 → 仅基础菜单
function loadMenus() {
  token.value = localStorage.getItem('token')
  menuList.value = token.value && hasAdminEntry()
    ? [...baseMenus, { path: '/admin', title: '管理后台' }]
    : baseMenus
}

const menuList = ref(baseMenus)

onMounted(loadMenus)

// 路由变化时刷新登录态（登录/退出后导航要立刻变）
watch(
  () => router.currentRoute.value.fullPath,
  () => loadMenus()
)

function goLogin() {
  router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
}
</script>

<template>
  <header class="navbar">
    <div class="nav-inner">
      <div class="logo" @click="router.push('/')">
        <span class="logo-icon">🛍️</span><span class="logo-text">校园集市</span>
      </div>

      <nav class="nav-links">
        <router-link
          v-for="m in menuList"
          :key="m.path"
          :to="m.path"
          :exact-active-class="m.path === '/' ? 'active' : ''"
          active-class="active"
        >
          {{ m.title }}
        </router-link>
      </nav>

      <div class="nav-actions">
        <router-link v-if="showPublish && isLoggedIn" to="/publish" class="publish-btn">＋ 发布</router-link>

        <router-link v-if="isLoggedIn" to="/profile" class="user-chip">
          <span class="chip-avatar">👤</span>
          <span class="chip-name">{{ displayName }}</span>
        </router-link>
        <button v-else class="login-btn" @click="goLogin">登录</button>

        <slot />
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  position: sticky; top: 0; z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid #f0f0f0;
}
.nav-inner {
  max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px;
  display: flex; align-items: center; justify-content: space-between; gap: 16px;
}
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; flex-shrink: 0; }
.logo-icon { font-size: 22px; }

.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; text-decoration: none; white-space: nowrap; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }

.nav-actions { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; text-decoration: none; }
.publish-btn:hover { background: #1e293b; }

.user-chip { display: flex; align-items: center; gap: 6px; text-decoration: none; padding: 4px 10px 4px 4px; border-radius: 20px; background: #f1f5f9; }
.user-chip:hover { background: #e2e8f0; }
.chip-avatar { font-size: 14px; }
.chip-name { font-size: 12px; color: #334155; max-width: 72px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.login-btn { border: none; background: #2563eb; color: #fff; padding: 7px 18px; border-radius: 18px; font-size: 13px; cursor: pointer; }
.login-btn:hover { background: #1d4ed8; }

@media (max-width: 900px) {
  .nav-links { gap: 12px; }
  .nav-links a { font-size: 13px; }
  .chip-name { display: none; }
}
</style>
