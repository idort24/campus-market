// ============================================================
// 订单接口层（后端 order 控制器）
// ------------------------------------------------------------
// 路径按马智贤核对口径：
//   创建订单      POST /api/v1/order/create
//   我买的        GET  /api/v1/order/myBuy
//   我卖的        GET  /api/v1/order/mySell
//   确认收货      PUT  /api/v1/order/confirm/{id}
//   取消订单      PUT  /api/v1/order/cancel/{id}
//
// 订单状态字典（src/data/dict.js）：
//   WAIT_CONFIRM 待付款 / WAIT_TRADE 待交易 / FINISH 已完成 / CANCEL 已取消
// ============================================================
import http from './http'
import { USE_MOCK } from './config'
import { ORDER_STATUS_MAP } from '../data/dict.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// Mock 订单（演示数据，字段与 normalizeOrder 输出一致）
const mockOrders = [
  { id: 101, orderNo: 'ORD20240101', goodsId: 1, goods: '高等数学教材', price: 25.0, seller: '张爱阳', status: 'WAIT_CONFIRM', time: '2024-01-15 14:30' },
  { id: 102, orderNo: 'ORD20240102', goodsId: 5, goods: '英语四级词汇书', price: 18.5, seller: '周泓颖', status: 'FINISH', time: '2024-01-10 09:15' },
  { id: 103, orderNo: 'ORD20240103', goodsId: 6, goods: '宿舍台灯', price: 12.0, seller: '马智贤', status: 'WAIT_TRADE', time: '2024-01-16 16:45' },
]

/**
 * 后端订单对象 → 页面使用的订单对象
 * 页面字段：id / orderNo / goods / price / seller / status / time
 */
export function normalizeOrder(o = {}) {
  const status = o.orderStatus ?? o.status ?? 'WAIT_CONFIRM'
  return {
    ...o,
    id: o.id,
    orderNo: o.orderNo ?? o.order_no ?? '',
    goodsId: o.goodsId ?? o.goods_id ?? null,
    goods: o.goodsTitle ?? o.goodsName ?? o.goods ?? '商品',
    price: Number(o.price ?? o.amount ?? 0),
    seller: o.sellerName ?? o.seller ?? '',
    buyer: o.buyerName ?? o.buyer ?? '',
    status,
    statusText: ORDER_STATUS_MAP[status] ?? status,
    time: o.createTime ?? o.time ?? '',
  }
}

/** 创建订单 */
export async function createOrder(payload) {
  if (USE_MOCK) {
    await delay()
    const order = normalizeOrder({
      id: Date.now(),
      orderNo: `ORD${Date.now()}`,
      ...payload,
      status: 'WAIT_CONFIRM',
    })
    mockOrders.unshift(order)
    return order
  }
  const data = await http.post('/order/create', payload)
  return data ? normalizeOrder(data) : null
}

/** 我买的订单 */
export async function getMyBuy(params = {}) {
  if (USE_MOCK) {
    await delay()
    return mockOrders.map(normalizeOrder)
  }
  const res = await http.get('/order/myBuy', { params })
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeOrder)
}

/** 我卖的订单 */
export async function getMySell(params = {}) {
  if (USE_MOCK) {
    await delay()
    return mockOrders.map(normalizeOrder)
  }
  const res = await http.get('/order/mySell', { params })
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeOrder)
}

/** 确认收货 */
export async function confirmOrder(id) {
  if (USE_MOCK) {
    await delay(100)
    const o = mockOrders.find((x) => String(x.id) === String(id))
    if (o) o.status = 'FINISH'
    return o ?? null
  }
  return http.put(`/order/confirm/${id}`)
}

/** 取消订单 */
export async function cancelOrder(id) {
  if (USE_MOCK) {
    await delay(100)
    const o = mockOrders.find((x) => String(x.id) === String(id))
    if (o) o.status = 'CANCEL'
    return o ?? null
  }
  return http.put(`/order/cancel/${id}`)
}
