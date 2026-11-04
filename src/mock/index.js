// ============================================================
// Mock 数据层（第 4 步）
// ------------------------------------------------------------
// 作用：后端接口就绪前，用假数据模拟后端返回。
// 页面接入时先调这里的 mockApi，后端接口好了再切成 http 调用。
// 返回格式统一为 { code: 1, msg: 'ok', data }，与后端约定一致。
// ============================================================

import { productList, addProductLocally } from '../data/products.js'
import { buildCategoryTree } from '../data/categories.js'

// 模拟网络延迟，让 loading 效果可见
const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

export const mockApi = {
  // 商品列表（keyword/category_id 过滤预留）
  async getGoods(params = {}) {
    await delay()
    let list = [...productList.value]
    const { keyword, category_id } = params
    if (keyword) list = list.filter((p) => p.name.includes(keyword))
    if (category_id) list = list.filter((p) => p.category_id === category_id)
    return list
  },

  // 商品详情
  async getGoodsDetail(id) {
    await delay()
    return productList.value.find((p) => p.id === Number(id)) ?? null
  },

  // 发布商品（写入本地响应式列表，页面立即更新）
  async createGoods(form) {
    await delay()
    return addProductLocally(form)
  },

  // 分类树（el-tree 直接可用）
  async getCategoryTree() {
    await delay(100)
    return buildCategoryTree()
  },

  // 我的收藏列表（演示：收藏了 id=1、id=4 两个商品）
  async getFavorites() {
    await delay()
    return productList.value.filter((p) => [1, 4].includes(p.id))
  },

  // 收藏商品
  async addFavorite(goodsId) {
    await delay()
    return { id: 999, goods_id: goodsId }
  },

  // 取消收藏
  async removeFavorite(id) {
    await delay()
    return { id }
  }
}
