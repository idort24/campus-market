
import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import ProductMarket from '../views/ProductMarket.vue'
import AiAssistant from '../views/AiAssistant.vue'
import Profile from '../views/Profile.vue'
import ProductDetail from '../views/ProductDetail.vue'
import PublishProduct from '../views/PublishProduct.vue'
import CategoryTree from '../views/CategoryTree.vue'
import FileTransfer from '../views/FileTransfer.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/product',
      name: 'Product',
      component: ProductMarket
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: ProductDetail
    },
    {
      path: '/publish',
      name: 'Publish',
      component: PublishProduct
    },
    {
      path: '/categories',
      name: 'Categories',
      component: CategoryTree
    },
    {
      path: '/ai',
      name: 'AI',
      component: AiAssistant
    },
    {
      path: '/file-transfer',
      name: 'FileTransfer',
      component: FileTransfer
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile
    }
  ]
})

export default router
