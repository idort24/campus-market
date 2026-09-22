import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/Home.vue'
import ProductMarket from '../views/ProductMarket.vue'
import AiAssistant from '../views/AiAssistant.vue'
import Profile from '../views/Profile.vue'

const router = createRouter({
  history: createWebHistory(),

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
      path: '/ai',
      name: 'AI',
      component: AiAssistant
    },

    {
      path: '/profile',
      name: 'Profile',
      component: Profile
    }
  ]
})

export default router