// ============================================================
// 商品分类 mock 数据（已对齐张爱阳 MySQL 的 goods_category 表）
// ------------------------------------------------------------
// 表结构：
//   id            bigint       分类ID（主键）
//   category_name varchar(100) 分类名称
//   parent_id     bigint       父分类ID，0 表示根节点
//   sort          int          排序
//
// 当前数据 = MySQL 里 goods_category 表的真实 9 条记录（2026-10-13 dump）。
// 后端接口就绪后，直接用接口返回的数组替换 categoryTable 即可。
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

/**
 * 把邻接表转成 Element Plus el-tree 需要的嵌套结构：
 * [
 *   { id, category_name, parent_id, children: [ { id, category_name, children: [...] } ] }
 * ]
 */
export function buildCategoryTree(rows = categoryTable) {
  const map = new Map()
  const roots = []

  rows.forEach((row) => {
    map.set(row.id, {
      id: row.id,
      category_name: row.category_name,
      parent_id: row.parent_id,
      sort: row.sort,
      children: []
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

  // 按 sort 排序
  const sortNodes = (list) => {
    list.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    list.forEach((n) => sortNodes(n.children))
  }
  sortNodes(roots)

  return roots
}
