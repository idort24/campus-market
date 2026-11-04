<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const menus = [
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/goods', title: '商品审核', icon: 'Goods' },
  { path: '/admin/categories', title: '分类管理', icon: 'Menu' },
  { path: '/admin/reports', title: '举报处理', icon: 'Warning' },
  { path: '/admin/roles', title: '角色权限', icon: 'Lock' },
]

const activeMenu = computed(() => router.currentRoute.value.path)
const currentTitle = computed(() => {
  const m = menus.find(m => m.path === activeMenu.value)
  return m ? m.title : '管理后台'
})

function logout() {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出')
    router.push('/login')
  })
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
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="crumb">{{ currentTitle }}</div>
        <div class="user">
          <span class="name">{{ userInfo.nickName || userInfo.username || '管理员' }}</span>
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
.aside { background: #0f172a; color: #fff; border-right: none; }
.logo {
  display: flex; align-items: center; gap: 8px;
  padding: 20px 18px; font-size: 15px; font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-icon { font-size: 20px; }
.menu { border-right: none; padding: 12px 0; background: transparent; }
:deep(.el-menu-item) { height: 48px; line-height: 48px; color: #94a3b8; margin: 0 8px; border-radius: 8px; }
:deep(.el-menu-item:hover) { background: rgba(255,255,255,0.06) !important; color: #fff; }
:deep(.el-menu-item.is-active) { background: #2563eb !important; color: #fff; }

.header {
  background: #fff; border-bottom: 1px solid #f0f0f0;
  display: flex; align-items: center; justify-content: space-between;
}
.crumb { font-size: 16px; font-weight: 600; color: #0f172a; }
.user { display: flex; align-items: center; gap: 12px; }
.user .name { font-size: 14px; color: #475569; }
.main { background: #f8fafc; padding: 24px; }
</style>
