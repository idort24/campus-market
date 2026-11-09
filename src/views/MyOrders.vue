<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyBuy, confirmOrder, cancelOrder } from '../api/order.js'

const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

const orders = ref([])
const loading = ref(false)

const statusMap = {
  WAIT_CONFIRM: { label: '待付款', color: '#f59e0b', bg: '#fef3c7' },
  WAIT_TRADE: { label: '待交易', color: '#2563eb', bg: '#dbeafe' },
  FINISH: { label: '已完成', color: '#16a34a', bg: '#dcfce7' },
  CANCEL: { label: '已取消', color: '#64748b', bg: '#f1f5f9' },
}

// 后端状态码若不在字典里（比如拼写差异），兜底成灰标签，不要让页面炸掉
function statusStyle(s) {
  return statusMap[s] || { label: s || '未知', color: '#64748b', bg: '#f1f5f9' }
}

async function load() {
  loading.value = true
  try {
    // 后端：GET /api/v1/order/myBuy（我买的订单）
    orders.value = await getMyBuy()
  } catch (e) {
    ElMessage.error(e.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)

/** 后端没有 pay 接口，待付款订单只能取消（PUT /order/cancel/{id}） */
async function pay(order) {
  try {
    await cancelOrder(order.id)
    order.status = 'CANCEL'
    ElMessage.success('订单已取消')
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

/** 确认收货：PUT /api/v1/order/confirm/{id} */
async function confirmDone(order) {
  try {
    await confirmOrder(order.id)
    order.status = 'FINISH'
    ElMessage.success('确认交易完成')
  } catch (e) {
    ElMessage.error(e.message || '确认失败')
  }
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

      <div v-loading="loading" class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-top">
            <span class="order-id">订单号：{{ order.orderNo || order.id }}</span>
            <span class="status" :style="{ color: statusStyle(order.status).color, background: statusStyle(order.status).bg }">
              {{ statusStyle(order.status).label }}
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
                <button v-if="order.status === 'WAIT_CONFIRM'" class="pay-btn" @click="pay(order)">取消订单</button>
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
