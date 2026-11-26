<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyBuy, getMySell, confirmOrder, finishOrder, cancelOrder } from '../api/order.js'
import { addEvaluation, getMyEvaluations } from '../api/evaluation.js'

const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')
const currentUserId = computed(() => userInfo.value?.id ?? null)

const tab = ref('buy') // buy=我买的 sell=我卖的
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

function isBuyer(order) {
  return String(order.buyerId) === String(currentUserId.value)
}

// 对方昵称：我买的看卖家，我卖的看买家
function peerName(order) {
  return isBuyer(order) ? (order.seller || '卖家') : (order.buyer || '买家')
}

// 取订单对方：我买的联系卖家，我卖的联系买家
function contactUserId(order) {
  const me = String(currentUserId.value ?? '')
  if (order.buyerId != null && String(order.buyerId) === me) return order.sellerId
  if (order.sellerId != null && String(order.sellerId) === me) return order.buyerId
  return order.sellerId ?? order.buyerId ?? null
}

function goContact(order) {
  const to = contactUserId(order)
  if (to != null) router.push(`/chat/${to}`)
}

async function load() {
  loading.value = true
  try {
    orders.value = tab.value === 'buy' ? await getMyBuy() : await getMySell()
    await loadRated()
  } catch (e) {
    ElMessage.error(e.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
watch(tab, load)

/** 待付款订单只能取消（PUT /order/cancel/{id}） */
async function cancel(order) {
  try {
    await cancelOrder(order.id)
    order.status = 'CANCEL'
    ElMessage.success('订单已取消')
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  }
}

/** 卖家确认订单：待付款 → 待交易（PUT /order/confirm/{id}） */
async function confirm(order) {
  try {
    await confirmOrder(order.id)
    order.status = 'WAIT_TRADE'
    ElMessage.success('已确认订单')
  } catch (e) {
    ElMessage.error(e.message || '确认失败')
  }
}

/** 买家确认收货：待交易 → 已完成（PUT /order/finish/{id}） */
async function finish(order) {
  try {
    await finishOrder(order.id)
    order.status = 'FINISH'
    ElMessage.success('确认交易完成')
  } catch (e) {
    ElMessage.error(e.message || '确认失败')
  }
}

/** 评价弹窗 */
const rateDialog = ref(false)
const rateTarget = ref(null)
const rateScore = ref(5)
const rateContent = ref('')
const ratedOrderIds = ref(new Set())
const submittingRate = ref(false)

async function loadRated() {
  try {
    const list = await getMyEvaluations()
    ratedOrderIds.value = new Set(list.map((e) => String(e.orderId)))
  } catch {
    // 已评价标记拉取失败不影响下单流程
  }
}

function goRate(order) {
  rateTarget.value = order
  rateScore.value = 5
  rateContent.value = ''
  rateDialog.value = true
}

async function submitRate() {
  const order = rateTarget.value
  const content = rateContent.value.trim()
  if (!content) {
    ElMessage.warning('请填写评价内容')
    return
  }
  submittingRate.value = true
  try {
    await addEvaluation({
      orderId: order.id,
      toUserId: order.sellerId,
      score: rateScore.value,
      evalContent: content,
    })
    ratedOrderIds.value.add(String(order.id))
    ElMessage.success('评价成功')
    rateDialog.value = false
  } catch (e) {
    ElMessage.error(e.message || '评价提交失败')
  } finally {
    submittingRate.value = false
  }
}

function isRated(order) {
  return ratedOrderIds.value.has(String(order.id))
}
</script>

<template>
  <div class="page">
    <NavBar />

    <main class="container">
      <div class="page-head">
        <div class="tabs">
          <button :class="['tab', { active: tab === 'buy' }]" @click="tab = 'buy'">我买的</button>
          <button :class="['tab', { active: tab === 'sell' }]" @click="tab = 'sell'">我卖的</button>
        </div>
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
              <div class="goods-icon">
                <img v-if="order.cover" :src="order.cover" alt="商品图片" />
                <template v-else>📦</template>
              </div>
              <div>
                <h4>{{ order.goods }}</h4>
                <p>{{ isBuyer(order) ? '卖家' : '买家' }}：{{ peerName(order) }}</p>
                <p class="time">{{ order.time }}</p>
              </div>
            </div>
            <div class="order-right">
              <span class="price">¥{{ order.price ? Number(order.price).toFixed(2) : '--' }}</span>
              <div class="order-actions">
                <template v-if="isBuyer(order)">
                  <button v-if="order.status === 'WAIT_CONFIRM'" class="pay-btn" @click="cancel(order)">取消订单</button>
                  <button v-if="order.status === 'WAIT_TRADE'" class="confirm-btn" @click="finish(order)">确认收货</button>
                </template>
                <template v-else>
                  <button v-if="order.status === 'WAIT_CONFIRM'" class="confirm-btn" @click="confirm(order)">确认订单</button>
                </template>
                <button v-if="order.status === 'FINISH' && !isRated(order)" class="rate-btn" @click="goRate(order)">去评价</button>
                <span v-else-if="order.status === 'FINISH' && isRated(order)" class="rated-tag">已评价</span>
                <button v-if="contactUserId(order)" class="chat-btn" @click="goContact(order)">
                  {{ isBuyer(order) ? '联系卖家' : '联系买家' }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!loading && !orders.length" class="order-empty">暂无订单</div>
      </div>

      <!-- 评价弹窗 -->
      <div v-if="rateDialog" class="rate-mask" @click.self="rateDialog = false">
        <div class="rate-box">
          <h3>评价交易</h3>
          <p class="rate-goods">{{ rateTarget?.goods }}</p>
          <div class="rate-stars">
            <button
              v-for="n in 5"
              :key="n"
              :class="['star', { on: n <= rateScore }]"
              @click="rateScore = n"
            >★</button>
          </div>
          <textarea v-model="rateContent" rows="4" maxlength="500" placeholder="说说这次交易体验…"></textarea>
          <div class="rate-actions">
            <button class="chat-btn" @click="rateDialog = false">取消</button>
            <button class="confirm-btn" :disabled="submittingRate" @click="submitRate">
              {{ submittingRate ? '提交中…' : '提交评价' }}
            </button>
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
.rate-btn { background: #f59e0b; color: #fff; }
.chat-btn { background: #f1f5f9; color: #475569; }

.tabs { display: flex; gap: 8px; }
.tab { border: 1px solid #e2e8f0; background: #fff; color: #64748b; padding: 6px 16px; border-radius: 16px; font-size: 13px; cursor: pointer; }
.tab.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.goods-icon img { width: 100%; height: 100%; object-fit: cover; border-radius: 12px; }
.order-empty { text-align: center; color: #94a3b8; font-size: 13px; padding: 40px 0; }
.rated-tag { font-size: 12px; color: #16a34a; background: #dcfce7; padding: 6px 12px; border-radius: 8px; }

.rate-mask { position: fixed; inset: 0; background: rgba(15,23,42,0.45); display: flex; align-items: center; justify-content: center; z-index: 200; }
.rate-box { width: 420px; max-width: 92vw; background: #fff; border-radius: 16px; padding: 24px; }
.rate-box h3 { margin: 0 0 8px; font-size: 17px; }
.rate-goods { margin: 0 0 14px; color: #64748b; font-size: 13px; }
.rate-stars { display: flex; gap: 6px; margin-bottom: 14px; }
.star { border: none; background: none; font-size: 26px; color: #cbd5e1; cursor: pointer; padding: 0; }
.star.on { color: #f59e0b; }
.rate-box textarea { width: 100%; border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px 12px; resize: none; outline: none; font-size: 14px; }
.rate-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }
.rate-actions button { padding: 8px 18px; border-radius: 10px; font-size: 13px; border: none; cursor: pointer; }
</style>
