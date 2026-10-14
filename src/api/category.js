/**
 * 分类接口层
 * 后端就绪前：返回本地 mock（src/data/categories.js，对齐 MySQL goods_category 表）
 * 后端就绪后：把 USE_MOCK 改成 false，请求 Spring Boot 的 GET /api/categories/tree
 */
import { categoryTable, buildCategoryTree } from '../data/categories.js'

const USE_MOCK = true

/**
 * 获取嵌套分类树（el-tree 直接可用）
 */
export async function fetchCategoryTree() {
  if (USE_MOCK) {
    // 模拟网络延迟
    await new Promise((r) => setTimeout(r, 100))
    return buildCategoryTree()
  }

  const response = await fetch('/api/categories/tree')
  if (!response.ok) {
    throw new Error(`加载分类失败：HTTP ${response.status}`)
  }
  return await response.json()
}

/**
 * 获取扁平分类邻接表（与 MySQL goods_category 表一致）
 */
export async function fetchCategoryTable() {
  if (USE_MOCK) {
    await new Promise((r) => setTimeout(r, 50))
    return categoryTable
  }

  const response = await fetch('/api/categories')
  if (!response.ok) {
    throw new Error(`加载分类表失败：HTTP ${response.status}`)
  }
  return await response.json()
}
