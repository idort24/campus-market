// ============================================================
// 分类接口层（对齐 goods_category 表）
// ------------------------------------------------------------
// 后端字段统一驼峰：id、categoryName、parentId、sortNo、children
// 分类 id 与 api/product.js 的 CATEGORY_NAME_BY_ID 保持一致
// ============================================================
import http from './http'
import { USE_MOCK } from './config.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// 分类树（嵌套结构，el-tree 直接用）
export const getCategoryTree = async () => {
  if (USE_MOCK) {
    await delay()
    return [
      {
        id: 1, categoryName: '数码产品', parentId: 0,
        children: [
          { id: 11, categoryName: '手机', parentId: 1, children: [] },
          { id: 12, categoryName: '电脑', parentId: 1, children: [] },
          { id: 13, categoryName: '耳机音箱', parentId: 1, children: [] },
        ],
      },
      {
        id: 2, categoryName: '教材书籍', parentId: 0,
        children: [
          { id: 21, categoryName: '数学教材', parentId: 2, children: [] },
          { id: 22, categoryName: '计算机教材', parentId: 2, children: [] },
          { id: 23, categoryName: '考研资料', parentId: 2, children: [] },
        ],
      },
      {
        id: 3, categoryName: '生活用品', parentId: 0,
        children: [
          { id: 31, categoryName: '宿舍好物', parentId: 3, children: [] },
          { id: 32, categoryName: '厨具餐具', parentId: 3, children: [] },
        ],
      },
      {
        id: 4, categoryName: '运动用品', parentId: 0,
        children: [
          { id: 41, categoryName: '健身器材', parentId: 4, children: [] },
          { id: 42, categoryName: '球类', parentId: 4, children: [] },
        ],
      },
    ]
  }
  return http.get('/categories')
}

// 扁平分类表（邻接表，用来查父节点等）
export const getCategoryTable = async () => {
  if (USE_MOCK) {
    await delay(80)
    return [
      { id: 1, categoryName: '数码产品', parentId: 0 },
      { id: 11, categoryName: '手机', parentId: 1 },
      { id: 12, categoryName: '电脑', parentId: 1 },
      { id: 13, categoryName: '耳机音箱', parentId: 1 },
      { id: 2, categoryName: '教材书籍', parentId: 0 },
      { id: 21, categoryName: '数学教材', parentId: 2 },
      { id: 22, categoryName: '计算机教材', parentId: 2 },
      { id: 23, categoryName: '考研资料', parentId: 2 },
      { id: 3, categoryName: '生活用品', parentId: 0 },
      { id: 31, categoryName: '宿舍好物', parentId: 3 },
      { id: 32, categoryName: '厨具餐具', parentId: 3 },
      { id: 4, categoryName: '运动用品', parentId: 0 },
      { id: 41, categoryName: '健身器材', parentId: 4 },
      { id: 42, categoryName: '球类', parentId: 4 },
    ]
  }
  return http.get('/categories/table')
}
