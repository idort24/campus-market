// ============================================================
// 商品接口层（axios + Mock 双模式）
// ------------------------------------------------------------
// USE_MOCK = true  ：返回本地 mock（src/data/products.js）
// USE_MOCK = false ：请求 Spring Boot 后端（RESTful /api/v1/goods）
//
// 后端字段映射（前端 mock → MySQL goods 表）：
//   前端 name          → goods.title           商品标题
//   前端 price         → goods.price           售价 decimal(10,2)
//   前端 category(字符串) → goods.category_id    外键关联 goods_category.id
//   前端 condition     → goods.quality         字典：NEW / 90 / 80 / 70 / USED
//   前端 location      → goods.trade_place     线下交易地点
//   前端 icon(emoji)   → goods.images          JSON 数组
//   前端 seller(字符串) → goods.seller_id       外键关联 sys_user.id
// ============================================================

import http from './http'
import { USE_MOCK } from './config'
import { productList, addProductLocally } from '../data/products.js'

const delay = (ms = 120) => new Promise((r) => setTimeout(r, ms))

/**
 * 获取商品列表
 * @param {{ keyword?: string, category_id?: number, page?: number, page_size?: number }} params
 */
export async function fetchProducts(params = {}) {
  if (USE_MOCK) {
    await delay()
    let list = [...productList.value]
    const { keyword, category_id } = params
    if (keyword) {
      const kw = keyword.trim().toLowerCase()
      list = list.filter(
        (p) => p.name.toLowerCase().includes(kw) || (p.description || '').toLowerCase().includes(kw)
      )
    }
    if (category_id) {
      list = list.filter((p) => p.category_id === category_id)
    }
    return list
  }

  // 真实后端：GET /api/v1/goods?keyword=&category_id=&page=&page_size=&sort_field=&sort_order=
  return http.get('/goods', { params })
}

/**
 * 根据 id 获取单个商品
 */
export async function fetchProductById(id) {
  if (USE_MOCK) {
    await delay()
    return productList.value.find((p) => p.id === Number(id)) ?? null
  }
  // 真实后端：GET /api/v1/goods/{id}
  try {
    return await http.get(`/goods/${id}`)
  } catch (e) {
    if (String(e).includes('404') || String(e).includes('不存在')) return null
    throw e
  }
}

/**
 * 发布新商品
 * @param {object} form 前端发布表单
 */
export async function createProduct(form) {
  if (USE_MOCK) {
    await delay()
    return addProductLocally(form)
  }
  // 真实后端：POST /api/v1/goods
  return http.post('/goods', form)
}
