
import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import ProductMarket from '../views/ProductMarket.vue'
import AiAssistant from '../views/AiAssistant.vue'
import Profile from '../views/Profile.vue'
import ProductDetail from '../views/ProductDetail.vue'
import PublishProduct from '../views/PublishProduct.vue'
import CategoryTree from '../views/CategoryTree.vue'
import Login from '../views/Login.vue'
import AdminPermission from '../views/AdminPermission.vue'

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
    { path: '/login', name: 'Login', component: Login },
    { path: '/product', name: 'Product', component: ProductMarket },
    { path: '/product/:id', name: 'ProductDetail', component: ProductDetail },
    { path: '/publish', name: 'Publish', component: PublishProduct },
    { path: '/categories', name: 'Categories', component: CategoryTree },
    { path: '/ai', name: 'AI', component: AiAssistant },
    { path: '/profile', name: 'Profile', component: Profile },

    // 用户端补充
    { path: '/my-publishes', name: 'MyPublishes', component: MyPublishes, meta: { requiresAuth: true } },
    { path: '/my-favorites', name: 'MyFavorites', component: MyFavorites, meta: { requiresAuth: true } },
    { path: '/my-orders', name: 'MyOrders', component: MyOrders, meta: { requiresAuth: true } },
    { path: '/chat/:userId?', name: 'Chat', component: Chat, meta: { requiresAuth: true } },
    { path: '/report/:goodsId', name: 'Report', component: Report, meta: { requiresAuth: true } },

    // 旧的权限占位页（保留兼容）
    { path: '/admin/old', name: 'AdminPermission', component: AdminPermission, meta: { requiresAuth: true } },

    // ===== 管理员后台（嵌套布局） =====
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/users' },
        { path: 'users', name: 'AdminUsers', component: AdminUsers },
        { path: 'goods', name: 'AdminGoods', component: AdminGoods },
        { path: 'categories', name: 'AdminCategories', component: AdminCategories },
        { path: 'reports', name: 'AdminReports', component: AdminReports },
        { path: 'roles', name: 'AdminRoles', component: AdminRoles },
      ]
    }
  ]
})

// ============================================================
// 全局路由守卫
// ============================================================
function getUserInfo() {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} }
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = getUserInfo()

  // 需要登录但没登录
  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员但不是管理员
  if (to.meta.requiresAdmin && token) {
    const isAdmin = userInfo.roleCode === 'ADMIN' || userInfo.userType === 9
    if (!isAdmin) {
      next({ path: '/', query: { denied: '1' } })
      return
    }
  }

  // 已登录访问登录页 → 回首页
  if (to.path === '/login' && token) {
    next('/')
    return
  }

  next()
})

export default router
