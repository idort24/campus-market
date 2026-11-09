import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import ProductMarket from '../views/ProductMarket.vue'
import AiAssistant from '../views/AiAssistant.vue'
import Profile from '../views/Profile.vue'
import ProductDetail from '../views/ProductDetail.vue'
import PublishProduct from '../views/PublishProduct.vue'
import CategoryTree from '../views/CategoryTree.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Forgot from '../views/Forgot.vue'
import AdminPermission from '../views/AdminPermission.vue'
import FileTransfer from '../views/FileTransfer.vue'

// 管理员后台
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUsers from '../views/admin/AdminUsers.vue'
import AdminGoods from '../views/admin/AdminGoods.vue'
import AdminCategories from '../views/admin/AdminCategories.vue'
import AdminReports from '../views/admin/AdminReports.vue'
import AdminRoles from '../views/admin/AdminRoles.vue'

// 用户端补充页
import MyPublishes from '../views/MyPublishes.vue'
import MyFavorites from '../views/MyFavorites.vue'
import MyOrders from '../views/MyOrders.vue'
import Chat from '../views/Chat.vue'
import Report from '../views/Report.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    { path: '/', name: 'Home', component: Home },
    { path: '/login', name: 'Login', component: Login, meta: { guestOnly: true } },
    { path: '/register', name: 'Register', component: Register, meta: { guestOnly: true } },
    { path: '/forgot', name: 'Forgot', component: Forgot, meta: { guestOnly: true } },
    { path: '/product', name: 'Product', component: ProductMarket },
    { path: '/product/:id', name: 'ProductDetail', component: ProductDetail },
    { path: '/publish', name: 'Publish', component: PublishProduct, meta: { requiresAuth: true } },
    { path: '/categories', name: 'Categories', component: CategoryTree },
    { path: '/ai', name: 'AI', component: AiAssistant },
    { path: '/file-transfer', name: 'FileTransfer', component: FileTransfer },
    { path: '/profile', name: 'Profile', component: Profile, meta: { requiresAuth: true } },

    // 用户端补充
    { path: '/my-publishes', name: 'MyPublishes', component: MyPublishes, meta: { requiresAuth: true } },
    { path: '/my-favorites', name: 'MyFavorites', component: MyFavorites, meta: { requiresAuth: true } },
    { path: '/my-orders', name: 'MyOrders', component: MyOrders, meta: { requiresAuth: true } },
    { path: '/chat/:userId?', name: 'Chat', component: Chat, meta: { requiresAuth: true } },
    { path: '/report/:goodsId', name: 'Report', component: Report, meta: { requiresAuth: true } },

    // 旧的权限占位页（保留兼容）
    { path: '/admin/old', name: 'AdminPermission', component: AdminPermission, meta: { requiresAuth: true } },

    // ===== 管理员后台（嵌套布局）=====
    // 每个子页声明所需权限码，守卫按后端下发的 perms 逐页校验
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/users' },
        {
          path: 'users',
          name: 'AdminUsers',
          component: AdminUsers,
          meta: { perm: 'user:manage' },
        },
        {
          path: 'goods',
          name: 'AdminGoods',
          component: AdminGoods,
          meta: { perm: 'goods:audit' },
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: AdminCategories,
          meta: { perm: 'category:manage' },
        },
        {
          path: 'reports',
          name: 'AdminReports',
          component: AdminReports,
          meta: { perm: 'report:handle' },
        },
        {
          path: 'roles',
          name: 'AdminRoles',
          component: AdminRoles,
          meta: { perm: 'dict:manage' },
        },
      ],
    },

    // 兜底：未匹配路径回首页
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// ============================================================
// 全局路由守卫（按后端 perms + 菜单驱动）
// ------------------------------------------------------------
// 1. 需要登录但没 token → /login?redirect=原路径
// 2. meta.perm 声明了权限码 → 校验 userInfo.perms
// 3. meta.requiresAdmin → 校验菜单里是否有 /admin（后端不给我就不放行）
// 4. 已登录访问登录/注册页 → 直接回首页
// ============================================================

const ADMIN_PERMS = [
  'user:manage',
  'user:status',
  'goods:audit',
  'report:handle',
  'category:manage',
  'dict:manage',
]

function readJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function getUserInfo() {
  return readJSON('userInfo', {})
}

function getUserPerms() {
  const info = getUserInfo()
  return Array.isArray(info.perms) ? info.perms : []
}

function getUserMenus() {
  return readJSON('userMenus', [])
}

function isAdminUser(info, perms, menus) {
  if (info.roleCode === 'ADMIN') return true
  const roles = readJSON('userRoles', [])
  if (Array.isArray(roles) && roles.includes('ADMIN')) return true
  if (menus.some((m) => m && m.path === '/admin')) return true
  return perms.some((p) => ADMIN_PERMS.includes(p))
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const info = getUserInfo()
  const perms = getUserPerms()
  const menus = getUserMenus()

  // 1. 未登录访问需登录页面
  const needAuth = to.matched.some((r) => r.meta?.requiresAuth)
  if (needAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 2. 已登录访问登录 / 注册 / 忘记密码 → 回首页
  if (to.meta.guestOnly && token) {
    next('/')
    return
  }

  if (token) {
    // 3. 具体权限码校验（取最深一层路由声明的 perm）
    const permRequired = to.matched
      .map((r) => r.meta?.perm)
      .filter(Boolean)
      .pop()

    if (permRequired) {
      const ok = perms.includes(permRequired) || isAdminUser(info, perms, menus)
      if (!ok) {
        ElMessageDenied(permRequired)
        next({ path: '/', query: { denied: '1' } })
        return
      }
    }

    // 4. 管理后台整体准入：菜单里没有 /admin 且不具备任何管理权限 → 拦截
    const needAdmin = to.matched.some((r) => r.meta?.requiresAdmin)
    if (needAdmin && !isAdminUser(info, perms, menus)) {
      ElMessageDenied()
      next({ path: '/', query: { denied: '1' } })
      return
    }
  }

  next()
})

// 轻量提示：仅首页用 query 提示，避免引入额外依赖
function ElMessageDenied(perm) {
  // 用 sessionStorage 暂存，首页读取后提示（避免在守卫里直接弹窗打断跳转）
  sessionStorage.setItem('cm_denied', perm || 'admin')
}

export default router
