// ============================================================
// 商品接口层（前端预留位置）
// ------------------------------------------------------------
// 现在：调用本地 mock 数据（src/data/products.js）
// 未来：把每个函数里的 mock 实现换成 fetch(...) 调用后端即可。
//
// 后端接口约定（张爱阳 Spring Boot，数据库表 goods）：
//   GET    /api/goods               商品列表（?keyword=&categoryId=&page=&size=）
//   GET    /api/goods/{id}          商品详情
//   POST   /api/goods               发布新商品
//
// 字段映射（前端 mock → MySQL goods 表）：
//   前端 name          → goods.title            商品标题
//   前端 price         → goods.price            售价 decimal(10,2)
//   前端 category(字符串) → goods.category_id     外键关联 goods_category.id
//   前端 condition     → goods.quality          字典：NEW / 90 / 80 / 70 / USED
//   前端 location      → goods.trade_place      线下交易地点
//   前端 icon(emoji)   → goods.images           JSON 数组 ["url1","url2"]
//   前端 seller(字符串) → goods.seller_id        外键关联 sys_user.id
//
// 商品状态字段（后端返回，前端后续再加筛选）：
//   audit_status:  PENDING / PASS / REJECT
//   goods_status:  ON_SHELF / OFF_SHELF / SOLD
// ============================================================

import { productList, addProductLocally } from '../data/products.js'

// 后端接口基础地址。开发阶段先留空，等后端起来后改成
// 例如 'http://localhost:8080' 或通过 Vite 代理 /api。
const API_BASE = ''

// 模拟网络延迟，让 loading 效果可见；接入后端后删除。
function delay(ms = 120) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * 获取商品列表
 * @param {{ keyword?: string, category?: string }} _params  预留筛选参数
 * @returns {Promise<Array>}
 */
export async function fetchProducts(_params = {}) {
  // ---- mock 实现 ----
  await delay()
  return [...productList.value]

  // ---- 未来真实接口（参考写法，先注释掉）----
  // const qs = new URLSearchParams(params).toString()
  // const res = await fetch(`${API_BASE}/api/products?${qs}`)
  // if (!res.ok) throw new Error('加载商品列表失败')
  // return res.json()
}

/**
 * 根据 id 获取单个商品
 * @param {number|string} id
 * @returns {Promise<object|null>}
 */
export async function fetchProductById(id) {
  await delay()
  const nid = Number(id)
  return productList.value.find((p) => p.id === nid) ?? null

  // ---- 未来真实接口 ----
  // const res = await fetch(`${API_BASE}/api/products/${id}`)
  // if (res.status === 404) return null
  // if (!res.ok) throw new Error('加载商品详情失败')
  // return res.json()
}

/**
 * 发布新商品
 * @param {{ name:string, price:number|string, category:string,
 *           condition:string, description:string, location:string,
 *           seller?:string, icon?:string }} form
 * @returns {Promise<object>} 新建的商品
 */
export async function createProduct(form) {
  // ---- mock 实现：直接写入本地响应式列表 ----
  await delay()
  return addProductLocally(form)

  // ---- 未来真实接口 ----
  // const res = await fetch(`${API_BASE}/api/products`, {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(form)
  // })
  // if (!res.ok) throw new Error('发布商品失败')
  // return res.json()
}
