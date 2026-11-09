<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

const orders = ref([
  { id: 'ORD20240101', goods: '九成新《数据结构》', price: 25.00, seller: '张爱阳', status: 'WAIT_CONFIRM', time: '2024-01-15 14:30' },
  { id: 'ORD20240102', goods: '高数习题集', price: 18.50, seller: '周泓颖', status: 'FINISH', time: '2024-01-10 09:15' },
  { id: 'ORD20240103', goods: '蓝色保温杯', price: 12.00, seller: '马智贤', status: 'WAIT_TRADE', time: '2024-01-16 16:45' },
])

const statusMap = {
  WAIT_CONFIRM: { label: '待付款', color: '#f59e0b', bg: '#fef3c7' },
  WAIT_TRADE: { label: '待交易', color: '#2563eb', bg: '#dbeafe' },
  FINISH: { label: '已完成', color: '#16a34a', bg: '#dcfce7' },
  CANCEL: { label: '已取消', color: '#64748b', bg: '#f1f5f9' },
}

function pay(order) {
  ElMessage.success(`订单 ${order.id} 支付成功`)
  order.status = 'WAIT_TRADE'
}

function confirmDone(order) {
  ElMessage.success('确认交易完成')
  order.status = 'FINISH'
}
</script>

<template>
  <div class="page">
    <NavBar />

    <main class="container">
      <div class="page-head">
        <h2>我的订单</h2>
        <span class="count">共 {{ orders.length }} 笔订单</span>
      </div>

      <div class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-top">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span class="status" :style="{ color: statusMap[order.status].color, background: statusMap[order.status].bg }">
              {{ statusMap[order.status].label }}
            </span>
          </div>
          <div class="order-body">
            <div class="order-goods">
              <div class="goods-icon">📦</div>
              <div>
                <h4>{{ order.goods }}</h4>
                <p>卖家：{{ order.seller }}</p>
                <p class="time">{{ order.time }}</p>
              </div>
            </div>
            <div class="order-right">
              <span class="price">¥{{ order.price.toFixed(2) }}</span>
              <div class="order-actions">
                <button v-if="order.status === 'WAIT_CONFIRM'" class="pay-btn" @click="pay(order)">去付款</button>
                <button v-if="order.status === 'WAIT_TRADE'" class="confirm-btn" @click="confirmDone(order)">确认收货</button>
                <button class="chat-btn" @click="router.push('/chat/1')">联系卖家</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; text-decoration: none; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; text-decoration: none; }

.container { max-width: 900px; margin: 0 auto; padding: 32px 24px; }
.page-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 24px; }
.page-head h2 { margin: 0; font-size: 22px; }
.count { color: #94a3b8; font-size: 13px; }

.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card { background: #fff; border-radius: 16px; border: 1px solid #f0f0f0; overflow: hidden; }
.order-top { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; background: #f8fafc; border-bottom: 1px solid #f0f0f0; }
.order-id { font-size: 13px; color: #64748b; }
.status { font-size: 12px; padding: 3px 10px; border-radius: 10px; font-weight: 500; }
.order-body { display: flex; justify-content: space-between; align-items: center; padding: 20px; }
.order-goods { display: flex; gap: 14px; align-items: center; }
.goods-icon { width: 56px; height: 56px; background: #f1f5f9; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.order-goods h4 { margin: 0 0 4px; font-size: 15px; }
.order-goods p { margin: 0; font-size: 12px; color: #94a3b8; }
.order-goods .time { margin-top: 2px; }
.order-right { text-align: right; }
.price { font-size: 20px; font-weight: 700; color: #ef4444; display: block; margin-bottom: 10px; }
.order-actions { display: flex; gap: 8px; justify-content: flex-end; }
.order-actions button { padding: 6px 14px; border-radius: 8px; font-size: 12px; cursor: pointer; border: none; }
.pay-btn { background: #2563eb; color: #fff; }
.confirm-btn { background: #16a34a; color: #fff; }
.chat-btn { background: #f1f5f9; color: #475569; }
</style>
