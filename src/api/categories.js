import http from './http'
import { USE_MOCK } from './config.js'

// 分类树（嵌套结构，el-tree 直接用）
export const getCategoryTree = async () => {
  if (USE_MOCK) {
    return [
      { id: 1, category_name: '教材教辅', parent_id: 0, children: [
        { id: 11, category_name: '计算机教材', parent_id: 1, children: [] },
        { id: 12, category_name: '数学教材', parent_id: 1, children: [] },
      ]},
      { id: 2, category_name: '电子产品', parent_id: 0, children: [
        { id: 21, category_name: '手机', parent_id: 2, children: [] },
        { id: 22, category_name: '电脑', parent_id: 2, children: [] },
      ]},
      { id: 3, category_name: '生活用品', parent_id: 0, children: [] },
    ]
  }
  return http.get('/categories')
}

// 扁平分类表（用于查 parent_id 等）
export const getCategoryTable = async () => {
  if (USE_MOCK) {
    return [
      { id: 1, category_name: '教材教辅', parent_id: 0 },
      { id: 11, category_name: '计算机教材', parent_id: 1 },
      { id: 12, category_name: '数学教材', parent_id: 1 },
      { id: 2, category_name: '电子产品', parent_id: 0 },
      { id: 21, category_name: '手机', parent_id: 2 },
      { id: 22, category_name: '电脑', parent_id: 2 },
      { id: 3, category_name: '生活用品', parent_id: 0 },
    ]
  }
  return http.get('/categories/table')
}
