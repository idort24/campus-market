// ============================================================
// 收藏接口层（后端对应 goods_collect 表 / goodsCollect 控制器）
// ------------------------------------------------------------
// 路径已按马智贤回复核对（2026-11 联调口径）：
//   我的收藏      GET    /api/v1/goodsCollect/my
//   取消收藏      DELETE /api/v1/goodsCollect/{goodsId}
//   是否已收藏    GET    /api/v1/goodsCollect/has/{goodsId}
//   添加收藏      POST   /api/v1/goodsCollect/add?goodsId=123   ← goodsId 是 query 参数，不是 body
//
// 注意：不是 /favorites，原来写的那套路径后端没有。
// 重复收藏后端返回 code=500，文案「已经收藏过该商品」，页面直接提示即可。
// 后端字段驼峰：id、userId、goodsId、createTime
// ============================================================
import http from './http'
import { USE_MOCK } from './config'
import { productList } from '../data/products.js'
import { normalizeGoods } from './product.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// 我的收藏列表
export async function getFavorites(params = {}) {
  if (USE_MOCK) {
    await delay()
    const list = productList.value.filter((p) => [1, 4].includes(p.id))
    return list.map(normalizeGoods)
  }
  const res = await http.get('/goodsCollect/my', { params })
  const list = Array.isArray(res) ? res : (res?.records || [])
  // 后端可能直接返回 goodsCollect 记录（含 goodsId），也可能带 goods 子对象
  return list.map((it) => normalizeGoods(it.goods || it))
}

// 收藏商品
// 后端口径（马智贤）：POST /api/v1/goodsCollect/add?goodsId=123
// goodsId 走 query，不能放进 JSON body；重复收藏返回 code=500「已经收藏过该商品」
export async function addFavorite(goodsId) {
  if (USE_MOCK) {
    await delay(100)
    return { goodsId }
  }
  return http.post('/goodsCollect/add', null, { params: { goodsId } })
}

// 取消收藏（后端按 goodsId 取消，不按收藏记录 id）
export async function removeFavorite(goodsId) {
  if (USE_MOCK) {
    await delay(100)
    return { goodsId }
  }
  return http.delete(`/goodsCollect/${goodsId}`)
}

// 查询自己是否已收藏某商品（详情页判断收藏按钮状态用）
export async function hasFavorite(goodsId) {
  if (USE_MOCK) {
    await delay(80)
    return [1, 4].includes(Number(goodsId))
  }
  const res = await http.get(`/goodsCollect/has/${goodsId}`)
  // 后端可能返回 true/false，也可能返回 {has: true}
  return typeof res === 'boolean' ? res : !!(res?.has ?? res?.data)
}
