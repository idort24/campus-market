// ============================================================
// 分类接口层（对齐 goods_category 表）
// ------------------------------------------------------------
// 数据源唯一：src/data/categories.js（= 数据库 goods_category 的 9 条真实记录）
// 这里不再手写第二份分类数据，Mock 直接用那份生成。
//
// 后端字段驼峰：id、categoryName、parentId、children
// ============================================================
import http from './http'
import { USE_MOCK } from './config.js'
import { categoryTable, buildCategoryTree } from '../data/categories.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// 分类树（嵌套结构，el-tree 直接用）
export const getCategoryTree = async () => {
  if (USE_MOCK) {
    await delay()
    return buildCategoryTree()
  }
  return http.get('/categories')
}

// 扁平分类表（邻接表，用来查父节点等），字段与后端驼峰一致
export const getCategoryTable = async () => {
  if (USE_MOCK) {
    await delay(80)
    return categoryTable.map((c) => ({
      id: c.id,
      categoryName: c.category_name,
      parentId: c.parent_id,
      sort: c.sort,
    }))
  }
  return http.get('/categories/table')
}
