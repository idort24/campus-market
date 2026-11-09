<script setup>
// ============================================================
// 管理后台布局
// ------------------------------------------------------------
// 左侧菜单按当前账号的 perms 过滤：没有权限码的菜单不显示
// 退出登录调 POST /auth/logout（写 Redis 黑名单），再清本地
// ============================================================
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { logout as apiLogout } from '../../api/auth.js'
import { clearAuth, getUserInfo, getUserPerms } from '../../utils/auth.js'

const router = useRouter()
const userInfo = computed(() => getUserInfo())
const perms = computed(() => {
  const p = getUserPerms()
  return Array.isArray(p) ? p : []
})

// 全量菜单（每项绑定自己的权限码，与路由 meta.perm 一致）
const allMenus = [
  { path: '/admin/users', title: '用户管理', icon: 'User', perm: 'user:manage' },
  { path: '/admin/goods', title: '商品审核', icon: 'Goods', perm: 'goods:audit' },
  { path: '/admin/categories', title: '分类管理', icon: 'Menu', perm: 'category:manage' },
  { path: '/admin/reports', title: '举报处理', icon: 'Warning', perm: 'report:handle' },
  { path: '/admin/roles', title: '角色权限', icon: 'Lock', perm: 'dict:manage' },
]

// 只显示当前账号有权限的菜单；不是管理员但带任一管理权限时也能进对应页
const menus = computed(() => {
  const list = allMenus.filter((m) => perms.value.includes(m.perm))
  // 一个权限都没有的异常账号，兜底显示第一项，避免空白侧边栏
  return list.length ? list : allMenus.slice(0, 1)
})

const activeMenu = computed(() => router.currentRoute.value.path)
const currentTitle = computed(() => {
  const m = allMenus.find((m) => m.path === activeMenu.value)
  return m ? m.title : '管理后台'
})

async function logout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await apiLogout()
  } catch {
    // 后端不可用也要让本地登出，避免卡在后台
  }
  clearAuth()
  ElMessage.success('已退出')
  router.push('/login')
}

// 图标组件按名字动态解析，避免打包时未注册
function iconComp(name) {
  return ElementPlusIconsVue[name] || null
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园集市后台</span>
      </div>
      <el-menu :default-active="activeMenu" class="menu" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="iconComp(m.icon)" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-foot">
        <el-button text class="back-link" @click="router.push('/')">← 返回前台</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="crumb">{{ currentTitle }}</div>
        <div class="user">
          <span class="name">{{ userInfo.nickName || userInfo.studentNo || '管理员' }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout { height: 100vh; }
.aside { background: #0f172a; color: #fff; border-right: none; display: flex; flex-direction: column; }
.logo {
  display: flex; align-items: center; gap: 8px;
  padding: 20px 18px; font-size: 15px; font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-icon { font-size: 20px; }
.menu { border-right: none; padding: 12px 0; background: transparent; flex: 1; }
:deep(.el-menu-item) { height: 48px; line-height: 48px; color: #94a3b8; margin: 0 8px; border-radius: 8px; }
:deep(.el-menu-item:hover) { background: rgba(255,255,255,0.06) !important; color: #fff; }
:deep(.el-menu-item.is-active) { background: #2563eb !important; color: #fff; }
.aside-foot { padding: 12px 16px 20px; }
.back-link { color: #64748b; font-size: 13px; }
.back-link:hover { color: #fff; }

.header {
  background: #fff; border-bottom: 1px solid #f0f0f0;
  display: flex; align-items: center; justify-content: space-between;
}
.crumb { font-size: 16px; font-weight: 600; color: #0f172a; }
.user { display: flex; align-items: center; gap: 12px; }
.user .name { font-size: 14px; color: #475569; }
.main { background: #f8fafc; padding: 24px; }
</style>
