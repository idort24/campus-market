// ============================================================
// 商品分类 —— 全站唯一数据源
// ------------------------------------------------------------
// 数据 = 张爱阳 MySQL 里 goods_category 表的真实 9 条记录（已定稿，不再改）。
// 表结构：id / category_name / parent_id / sort
//
// 【重要】以后分类只在库里改，前端只改这一个文件；
//        首页分类格子、商品市场筛选、发布表单、mock 分类树都从这里导。
//
// 一级分类（parent_id = 0）：1 教材书籍 / 2 电子产品 / 3 服饰鞋包 / 4 生活用品 / 5 体育器材
// 二级分类：6 计算机教材(父1) / 7 考研资料(父1) / 8 手机(父2) / 9 耳机(父2)
// 6-9 是子分类，不是并列的一级分类，渲染时不要和 1-5 平铺。
// ============================================================

/** 分类原始数据（邻接表，字段名与 goods_category 表一致） */
export const categoryTable = [
  // —— 根分类（parent_id = 0）——
  { id: 1, category_name: '教材书籍', parent_id: 0, sort: 1 },
  { id: 2, category_name: '电子产品', parent_id: 0, sort: 2 },
  { id: 3, category_name: '服饰鞋包', parent_id: 0, sort: 3 },
  { id: 4, category_name: '生活用品', parent_id: 0, sort: 4 },
  { id: 5, category_name: '体育器材', parent_id: 0, sort: 5 },

  // —— 教材书籍(id=1) 的子分类 ——
  { id: 6, category_name: '计算机教材', parent_id: 1, sort: 1 },
  { id: 7, category_name: '考研资料',   parent_id: 1, sort: 2 },

  // —— 电子产品(id=2) 的子分类 ——
  { id: 8, category_name: '手机', parent_id: 2, sort: 1 },
  { id: 9, category_name: '耳机', parent_id: 2, sort: 2 },
]

/** id → 分类名（后端只回 categoryId 时用它转中文名） */
export const CATEGORY_NAME_BY_ID = categoryTable.reduce((acc, c) => {
  acc[c.id] = c.category_name
  return acc
}, {})

/** 分类名 → id（发布商品时把表单里的中文分类转成后端要的 categoryId） */
export const CATEGORY_ID_BY_NAME = categoryTable.reduce((acc, c) => {
  acc[c.category_name] = c.id
  return acc
}, {})

/** 一级分类 id → 图标（首页宫格 / 卡片兜底图标用） */
export const CATEGORY_ICONS = {
  1: '📚',
  2: '📱',
  3: '👗',
  4: '🛋️',
  5: '⚽',
  6: '💻',
  7: '📝',
  8: '📱',
  9: '🎧',
}

/** 一级分类（只取 parent_id = 0，按 sort 排），首页宫格直接用 */
export const ROOT_CATEGORIES = categoryTable
  .filter((c) => c.parent_id === 0)
  .sort((a, b) => a.sort - b.sort)
  .map((c) => ({
    id: c.id,
    name: c.category_name,
    icon: CATEGORY_ICONS[c.id] || '📦',
  }))

/** 一级分类名称数组（商品市场筛选按钮、发布表单下拉共用） */
export const CATEGORY_OPTIONS = ROOT_CATEGORIES.map((c) => c.name)

/** 按分类名取图标（发布商品时给新商品选默认图标） */
export function iconOfCategory(name) {
  const row = categoryTable.find((c) => c.category_name === name)
  return (row && CATEGORY_ICONS[row.id]) || '📦'
}

/**
 * 把邻接表转成 el-tree 需要的嵌套结构（字段与后端驼峰一致）：
 * [{ id, categoryName, parentId, children: [...] }]
 */
export function buildCategoryTree(rows = categoryTable) {
  const map = new Map()
  const roots = []

  rows.forEach((row) => {
    map.set(row.id, {
      id: row.id,
      categoryName: row.category_name,
      parentId: row.parent_id,
      sort: row.sort,
      children: [],
    })
  })

  rows.forEach((row) => {
    const node = map.get(row.id)
    if (row.parent_id && map.has(row.parent_id)) {
      map.get(row.parent_id).children.push(node)
    } else {
      roots.push(node)
    }
  })

  const sortNodes = (list) => {
    list.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    list.forEach((n) => sortNodes(n.children))
  }
  sortNodes(roots)

  return roots
}
