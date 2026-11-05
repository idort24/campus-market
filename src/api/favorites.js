import http from './http'
import { USE_MOCK } from './config'
import { productList } from '../data/products.js'

// 收藏接口层（对齐 goods_collect 表）
// 后端字段：id, user_id, goods_id, create_time, is_deleted

// 我的收藏列表（分页参数预留）
export async function getFavorites(params = {}) {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 150))
    return productList.value.filter((p) => [1, 4].includes(p.id))
  }
  return http.get('/favorites', { params })
}

// 收藏商品
export async function addFavorite(goodsId) {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 100))
    return { id: 999, goods_id: goodsId }
  }
  return http.post('/favorites', { goods_id: goodsId })
}

// 取消收藏
export async function removeFavorite(id) {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 100))
    return { id }
  }
  return http.delete(`/favorites/${id}`)
}
