// ============================================================
// 收藏接口层（对齐 goods_collect 表）
// ------------------------------------------------------------
// 后端字段统一驼峰：id、userId、goodsId、createTime
// Mock 分支返回的商品对象也走 normalizeGoods，保证页面字段一致
// ============================================================
import http from './http'
import { USE_MOCK } from './config'
import { productList } from '../data/products.js'
import { normalizeGoods } from './product.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// 我的收藏列表（分页参数预留）
export async function getFavorites(params = {}) {
  if (USE_MOCK) {
    await delay()
    const list = productList.value.filter((p) => [1, 4].includes(p.id))
    return list.map(normalizeGoods)
  }
  const res = await http.get('/favorites', { params })
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map((it) => normalizeGoods(it.goods || it))
}

// 收藏商品
export async function addFavorite(goodsId) {
  if (USE_MOCK) {
    await delay(100)
    return { goodsId }
  }
  return http.post('/favorites', { goodsId })
}

// 取消收藏（后端按 goodsId 取消更稳，收藏记录 id 前端拿不到时也能用）
export async function removeFavorite(goodsId) {
  if (USE_MOCK) {
    await delay(100)
    return { goodsId }
  }
  return http.delete(`/favorites/${goodsId}`)
}
