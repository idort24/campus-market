<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

/* ========== Mock 数据：角色 + 资源（对齐 RBAC 表：角色表 / 资源表 / 用户-角色关联） ========== */
const ROLES = [
  { id: 1, role_code: 'ADMIN', role_name: '管理员', description: '拥有全部功能权限' },
  { id: 2, role_code: 'USER', role_name: '普通用户', description: '可浏览、发布、收藏商品' }
]

const RESOURCES = [
  {
    id: 1,
    resource_name: '商品管理',
    resource_type: 'MENU',
    children: [
      { id: 11, resource_name: '商品列表', resource_type: 'MENU' },
      { id: 12, resource_name: '发布商品', resource_type: 'BUTTON' },
      { id: 13, resource_name: '收藏商品', resource_type: 'BUTTON' }
    ]
  },
  {
    id: 2,
    resource_name: '分类管理',
    resource_type: 'MENU',
    children: [
      { id: 21, resource_name: '分类树', resource_type: 'MENU' }
    ]
  },
  {
    id: 3,
    resource_name: 'AI 助手',
    resource_type: 'MENU',
    children: [
      { id: 31, resource_name: '智能对话', resource_type: 'BUTTON' }
    ]
  },
  {
    id: 4,
    resource_name: '个人中心',
    resource_type: 'MENU',
    children: [
      { id: 41, resource_name: '查看信息', resource_type: 'MENU' },
      { id: 42, resource_name: '退出登录', resource_type: 'BUTTON' }
    ]
  },
  {
    id: 5,
    resource_name: '权限管理',
    resource_type: 'MENU',
    children: [
      { id: 51, resource_name: '角色授权', resource_type: 'BUTTON' },
      { id: 52, resource_name: '资源管理', resource_type: 'BUTTON' }
    ]
  }
]

// 默认授权：管理员全部；普通用户不含"权限管理"（5, 51, 52）
const DEFAULT_PERMISSION = {
  1: [1, 11, 12, 13, 2, 21, 3, 31, 4, 41, 42, 5, 51, 52],
  2: [1, 11, 12, 13, 2, 21, 3, 31, 4, 41, 42]
}

/* ========== 状态 ========== */
const roles = ref(ROLES)
const resources = ref(RESOURCES)
const currentRoleId = ref(1)
const treeRef = ref(null)
const saved = ref(false)

// 当前登录用户信息
const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || 'null')
  } catch {
    return null
  }
})

// 当前选中的角色
const currentRole = computed(() =>
  roles.value.find((r) => r.id === currentRoleId.value)
)

// 从 localStorage 读取已保存的授权
function loadPermission(roleId) {
  try {
    const store = JSON.parse(localStorage.getItem('rbac_permissions') || '{}')
    return store[roleId] ?? DEFAULT_PERMISSION[roleId] ?? []
  } catch {
    return DEFAULT_PERMISSION[roleId] ?? []
  }
}

// 切换角色时，把树设置为该角色已授权的资源
function selectRole(role) {
  currentRoleId.value = role.id
  saved.value = false
  nextTick(() => {
    treeRef.value?.setCheckedKeys(loadPermission(role.id))
  })
}

// 保存授权
function onSave() {
  const checked = treeRef.value?.getCheckedKeys() ?? []
  const halfChecked = treeRef.value?.getHalfCheckedKeys() ?? []
  const keys = [...checked, ...halfChecked]

  const store = JSON.parse(localStorage.getItem('rbac_permissions') || '{}')
  store[currentRoleId.value] = keys
  localStorage.setItem('rbac_permissions', JSON.stringify(store))

  saved.value = true
  ElMessage.success(`已保存「${currentRole.value.role_name}」的授权`)
}

// 回到首页
function goHome() {
  router.push('/')
}

// 退出登录
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  nextTick(() => {
    treeRef.value?.setCheckedKeys(loadPermission(currentRoleId.value))
  })
})
</script>

<template>
  <div class="rbac-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市 · 权限管理</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/product">商品市场</router-link>
        <router-link to="/publish">发布商品</router-link>
        <router-link to="/categories">分类管理</router-link>
        <router-link to="/ai">AI助手</router-link>
        <router-link to="/profile">个人中心</router-link>
        <router-link to="/admin" active-class="active">权限管理</router-link>
      </nav>
    </header>

    <main class="rbac-container">
      <!-- 标题 + 当前用户 -->
      <section class="rbac-header">
        <div>
          <h1>角色授权管理</h1>
          <p>参考禅道 RBAC 简化模型：用户表 + 角色表 + 资源表（菜单 / 按钮），为角色勾选可用功能</p>
        </div>

        <div class="user-box" v-if="userInfo">
          <span class="user-name">{{ userInfo.nick_name || userInfo.student_no }}</span>
          <span class="user-role">{{ userInfo.role_name || '普通用户' }}</span>
          <button class="logout-btn" @click="logout">退出</button>
        </div>
      </section>

      <div class="rbac-layout">
        <!-- 左：角色列表 -->
        <div class="role-card">
          <h2>角色列表</h2>
          <div
            v-for="role in roles"
            :key="role.id"
            class="role-item"
            :class="{ active: currentRoleId === role.id }"
            @click="selectRole(role)"
          >
            <div class="role-name">{{ role.role_name }}</div>
            <div class="role-code">{{ role.role_code }}</div>
            <div class="role-desc">{{ role.description }}</div>
          </div>
        </div>

        <!-- 右：资源授权 -->
        <div class="resource-card">
          <h2>为「{{ currentRole?.role_name }}」分配功能资源</h2>
          <p class="hint">勾选该角色可以访问的功能（菜单）与可执行的操作（按钮）</p>

          <el-tree
            ref="treeRef"
            :data="resources"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="{ label: 'resource_name', children: 'children' }"
          />

          <div class="save-bar">
            <span v-if="saved" class="saved-tag">✓ 已保存</span>
            <button class="save-btn" @click="onSave">保存授权</button>
          </div>
        </div>
      </div>

      <!-- 模型说明 -->
      <div class="schema-card">
        <h3>RBAC 简化权限模型（对接后端表结构）</h3>
        <p>
          单用户对应单角色：<code>sys_user.role_id</code> → <code>角色表</code>，
          角色通过 <code>角色-资源关联表</code> 获得菜单与按钮资源。
          登录后后端返回 token 与角色，接口鉴权时校验当前用户是否拥有该资源权限。
        </p>
      </div>
    </main>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.rbac-page {
  min-height: 100vh;
  background: #f7f8fc;
  color: #333;
}

.navbar {
  height: 70px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
}

.logo-icon { font-size: 26px; }

.nav-menu { display: flex; gap: 24px; }

.nav-menu a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 0;
}

.nav-menu a:hover,
.nav-menu a.active { color: #6c5ce7; }

.rbac-container {
  width: 84%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 0 60px;
}

.rbac-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.rbac-header h1 { margin: 0 0 8px; font-size: 30px; }
.rbac-header p { margin: 0; color: #888; font-size: 14px; }

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.05);
}

.user-name { font-weight: 600; }

.user-role {
  background: #f0edff;
  color: #6c5ce7;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.logout-btn {
  border: none;
  background: #f1f1f6;
  color: #666;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
}

.rbac-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
}

.role-card,
.resource-card,
.schema-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}

.role-card h2,
.resource-card h2 { margin: 0 0 16px; font-size: 18px; }

.role-item {
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #eee;
  margin-bottom: 12px;
  cursor: pointer;
  transition: 0.2s;
}

.role-item:hover { border-color: #c9c0f5; }

.role-item.active {
  background: #f0edff;
  border-color: #6c5ce7;
}

.role-name { font-weight: 700; font-size: 16px; }
.role-code { color: #6c5ce7; font-size: 12px; margin: 3px 0; }
.role-desc { color: #999; font-size: 12px; line-height: 1.5; }

.hint { color: #999; font-size: 13px; margin: 0 0 16px; }

:deep(.el-tree) { font-size: 14px; }

.save-bar {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.saved-tag { color: #52c41a; font-size: 13px; }

.save-btn {
  border: none;
  background: #6c5ce7;
  color: #fff;
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.2s;
}

.save-btn:hover { background: #5a4bd1; }

.schema-card { margin-top: 22px; }

.schema-card h3 { margin: 0 0 10px; font-size: 16px; }

.schema-card p {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

.schema-card code {
  background: #f3f0ff;
  color: #6c5ce7;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

@media (max-width: 800px) {
  .rbac-layout { grid-template-columns: 1fr; }
  .navbar { padding: 0 4%; }
  .nav-menu { gap: 10px; }
  .nav-menu a { font-size: 12px; }
}
</style>
