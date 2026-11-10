// ============================================================
// 商品接口层（axios + Mock 双模式）
// ------------------------------------------------------------
// USE_MOCK = true  ：返回本地 mock（src/data/products.js）
// USE_MOCK = false ：请求 Spring Boot 后端（/api/v1/goods）
//
// 【字段适配层】后端实体是 goods 表驼峰字段，页面历史用的是
// name/category/condition/seller/location 这套中文语义字段。
// 这里统一在 API 层做一次映射，页面组件不用改一行代码就能切后端：
//   goods.title        → name           商品标题
//   goods.price        → price          售价 decimal(10,2)
//   goods.categoryId   → category       分类（这里按字典表转成中文名）
//   goods.quality      → condition      成色（字典码 → 中文）
//   goods.tradePlace   → location       线下交易地点
//   goods.images       → icon           前端暂时用 emoji 占位
//   goods.sellerName   → seller         卖家昵称
// ============================================================

import http from './http'
import { USE_MOCK } from './config'
import { productList, addProductLocally } from '../data/products.js'
import { QUALITY_MAP, QUALITY_CODE_BY_TEXT } from '../data/dict.js'
import { CATEGORY_NAME_BY_ID, CATEGORY_ID_BY_NAME, iconOfCategory } from '../data/categories.js'

const delay = (ms = 120) => new Promise((r) => setTimeout(r, ms))

// 分类 id ↔ 名称映射统一由 data/categories.js 维护（以数据库 goods_category 为准）
export { CATEGORY_NAME_BY_ID }

function qualityToText(quality) {
  if (!quality) return '未知'
  return QUALITY_MAP[quality] || String(quality)
}

/**
 * 后端商品对象 → 页面使用的商品对象
 * 同时保留原始字段（raw），方便排查联调时的字段差异
 */
export function normalizeGoods(g = {}) {
  const categoryId = g.categoryId ?? g.category_id ?? null
  return {
    ...g,
    id: g.id ?? g.goodsId,
    name: g.title ?? g.name ?? '未命名商品',
    price: Number(g.price ?? 0),
    category: g.category ?? CATEGORY_NAME_BY_ID[categoryId] ?? '未分类',
    categoryId,
    condition: g.condition ?? qualityToText(g.quality),
    quality: g.quality ?? null,
    seller: g.sellerName ?? g.seller ?? `用户${g.sellerId ?? ''}`,
    sellerId: g.sellerId ?? g.seller_id ?? null,
    location: g.tradePlace ?? g.location ?? '同校交易',
    description: g.description ?? '',
    images: g.images ?? null,
    icon: g.icon ?? iconOfCategory(g.category ?? CATEGORY_NAME_BY_ID[categoryId]),
    goodsStatus: g.goodsStatus ?? g.goods_status ?? 'ON_SHELF',
    auditStatus: g.auditStatus ?? g.audit_status ?? 'PASS',
    createTime: g.createTime ?? g.create_time ?? '',
    raw: g,
  }
}

/**
 * 获取商品列表
 * @param {{ keyword?: string, categoryId?: number, page?: number, pageSize?: number, sortField?: string, sortOrder?: string }} params
 */
export async function fetchProducts(params = {}) {
  if (USE_MOCK) {
    await delay()
    let list = [...productList.value]
    const { keyword, categoryId } = params
    if (keyword) {
      const kw = keyword.trim().toLowerCase()
      list = list.filter(
        (p) =>
          (p.name || '').toLowerCase().includes(kw) ||
          (p.description || '').toLowerCase().includes(kw)
      )
    }
    if (categoryId) {
      list = list.filter((p) => p.categoryId === categoryId)
    }
    return list.map(normalizeGoods)
  }

  // 真实后端：GET /api/v1/goods/list?keyword=&categoryId=&page=&pageSize=&sortField=&sortOrder=
  // 注意是 /goods/list，不是 /goods——后端没有给 /goods 挂 GET 映射
  const res = await http.get('/goods/list', { params })
  // 后端分页结构 {records,total,current,size}，非分页时可能是数组
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeGoods)
}

/**
 * 根据 id 获取单个商品
 */
export async function fetchProductById(id) {
  if (USE_MOCK) {
    await delay()
    const p = productList.value.find((x) => x.id === Number(id))
    return p ? normalizeGoods(p) : null
  }
  // 真实后端：GET /api/v1/goods/detail/{id}
  // 旧的 /goods/{id} 现在要求登录（匿名返回 code=401），详情接口已挪到 /goods/detail/{id}
  try {
    const data = await http.get(`/goods/detail/${id}`)
    return data ? normalizeGoods(data) : null
  } catch (e) {
    if (String(e).includes('404') || String(e).includes('不存在')) return null
    throw e
  }
}

/**
 * 我的发布列表（后端：GET /api/v1/goods/my）
 * 后端返回当前登录人发布的商品，字段同 /goods
 */
export async function fetchMyGoods(params = {}) {
  if (USE_MOCK) {
    await delay()
    // Mock：拿全部本地商品当"我发布的"演示
    return productList.value.map(normalizeGoods)
  }
  const res = await http.get('/goods/my', { params })
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeGoods)
}

/**
 * 上架 / 下架自己的商品
 * 后端（马智贤）：PUT /api/v1/goods/{id}/status?status=ON_SHELF|OFF_SHELF
 * status 走 query；传别的值 400；只能改自己发布的商品
 */
export async function updateGoodsStatus(id, status) {
  if (USE_MOCK) {
    await delay(100)
    const p = productList.value.find((x) => x.id === Number(id))
    if (p) p.goodsStatus = status
    return { id, status }
  }
  return http.put(`/goods/${id}/status`, null, { params: { status } })
}

/**
 * 发布新商品
 * 前端表单 → 后端 goods 字段（驼峰）
 */
export async function createProduct(form) {
  if (USE_MOCK) {
    await delay()
    return addProductLocally(form)
  }
  // 真实后端：POST /api/v1/goods
  // 表单里给的是中文分类名，这里转成后端要的 categoryId
  const categoryId = form.categoryId ?? CATEGORY_ID_BY_NAME[form.category] ?? null
  // 表单里成色也是中文（form.condition），后端 goods.quality 存字典码值（NEW/90/80/70/USED）
  const quality =
    QUALITY_CODE_BY_TEXT[form.condition] ??
    (form.quality ? QUALITY_CODE_BY_TEXT[form.quality] ?? form.quality : '90')
  const payload = {
    title: form.name,
    price: Number(form.price),
    categoryId,
    description: form.description ?? '',
    quality,
    tradePlace: form.location ?? '',
    images: form.images ?? [],
  }
  const data = await http.post('/goods', payload)
  return data ? normalizeGoods(data) : null
}
