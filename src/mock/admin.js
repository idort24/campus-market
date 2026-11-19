// ============================================================
// 后台管理 Mock 数据
// ------------------------------------------------------------
// 角色不再是代码里的常量：初始只用一份种子初始化，之后存在
// localStorage（cm_admin_roles / cm_admin_role_perms），支持
// 新增、编辑、删除、启停、分配权限，刷新页面不丢。
// 权限项本身是系统预置资源（与路由 meta.perm、v-permission 绑定），
// 运行期需要做的是「给角色分配已有权限」，不是临时发明权限码。
// ============================================================
import { buildCategoryTree } from '../data/categories.js'

const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms))

const LS_ROLES = 'cm_admin_roles'
const LS_ROLE_PERMS = 'cm_admin_role_perms'
const LS_SEQ = 'cm_admin_role_seq'

function readJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function writeJSON(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

/* ---------------- 种子数据（仅首次进入时写入一次） ---------------- */

const SEED_ROLES = [
  { id: 1, roleCode: 'ADMIN', roleName: '系统管理员', roleDesc: '拥有全部权限', status: 1, createTime: '2026-10-01 10:00:00', builtin: true },
  { id: 2, roleCode: 'STUDENT', roleName: '普通学生', roleDesc: '浏览与发布闲置', status: 1, createTime: '2026-10-01 10:00:00' },
  { id: 3, roleCode: 'MERCHANT', roleName: '商家', roleDesc: '可发布商品的卖家', status: 1, createTime: '2026-10-15 14:30:00' },
]

const SEED_ROLE_PERMS = {
  1: [1, 11, 2, 21, 22, 3, 31, 4, 41, 5, 51],
  2: [],
  3: [1, 11],
}

// 权限树（系统预置，对应 sys_permission 表，permType: 1=MENU，2=BUTTON）
export const mockPermissionTree = [
  {
    id: 1,
    permCode: '/admin/goods',
    permName: '商品管理',
    permType: 1,
    parentId: 0,
    children: [
      { id: 11, permCode: 'goods:audit', permName: '商品审核', permType: 2, parentId: 1 },
    ],
  },
  {
    id: 2,
    permCode: '/admin/users',
    permName: '用户管理',
    permType: 1,
    parentId: 0,
    children: [
      { id: 21, permCode: 'user:manage', permName: '用户查看/编辑', permType: 2, parentId: 2 },
      { id: 22, permCode: 'user:status', permName: '用户封禁/解封', permType: 2, parentId: 2 },
    ],
  },
  {
    id: 3,
    permCode: '/admin/categories',
    permName: '分类管理',
    permType: 1,
    parentId: 0,
    children: [
      { id: 31, permCode: 'category:manage', permName: '分类增删改', permType: 2, parentId: 3 },
    ],
  },
  {
    id: 4,
    permCode: '/admin/reports',
    permName: '举报处理',
    permType: 1,
    parentId: 0,
    children: [
      { id: 41, permCode: 'report:handle', permName: '举报处理', permType: 2, parentId: 4 },
    ],
  },
  {
    id: 5,
    permCode: '/admin/roles',
    permName: '角色权限',
    permType: 1,
    parentId: 0,
    children: [
      { id: 51, permCode: 'dict:manage', permName: '角色与权限维护', permType: 2, parentId: 5 },
    ],
  },
]

/* 权限码 -> 名称，用于列表展示 */
const PERM_NAME_BY_ID = (() => {
  const map = {}
  const walk = (nodes) => {
    nodes.forEach((n) => {
      map[n.id] = n.permName
      if (n.children) walk(n.children)
    })
  }
  walk(mockPermissionTree)
  return map
})()

/* ---------------- 存储读写 ---------------- */

function loadRoles() {
  const stored = readJSON(LS_ROLES, null)
  if (Array.isArray(stored) && stored.length) return stored
  writeJSON(LS_ROLES, SEED_ROLES)
  if (!readJSON(LS_SEQ, null)) writeJSON(LS_SEQ, SEED_ROLES.length + 1)
  return SEED_ROLES.slice()
}

function saveRoles(list) {
  writeJSON(LS_ROLES, list)
}

function loadRolePerms() {
  const stored = readJSON(LS_ROLE_PERMS, null)
  if (stored && typeof stored === 'object') return stored
  writeJSON(LS_ROLE_PERMS, SEED_ROLE_PERMS)
  return { ...SEED_ROLE_PERMS }
}

function saveRolePerms(map) {
  writeJSON(LS_ROLE_PERMS, map)
}

function nextRoleId() {
  const seq = Number(readJSON(LS_SEQ, SEED_ROLES.length + 1)) || SEED_ROLES.length + 1
  writeJSON(LS_SEQ, seq + 1)
  return seq
}

function nowText() {
  const d = new Date()
  const p = (n) => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

const CODE_RE = /^[A-Z][A-Z0-9_]{1,19}$/

/* ---------------- 对外提供的数据快照（兼容旧引用） ---------------- */

export const mockRoles = loadRoles()
export const mockRolePerms = loadRolePerms()

/* ---------------- 其他后台数据（未改动） ---------------- */

export const mockUsers = [
  { id: 1, studentNo: '2024110640', nickName: '何曼妮', userType: 9, accountStatus: 1, creditScore: 100, createTime: '2026-09-01 10:00:00' },
  { id: 2, studentNo: '2024110601', nickName: '张爱阳', userType: 1, accountStatus: 1, creditScore: 95, createTime: '2026-09-01 11:00:00' },
  { id: 3, studentNo: '2024110602', nickName: '马智贤', userType: 1, accountStatus: 1, creditScore: 88, createTime: '2026-09-02 09:30:00' },
  { id: 4, studentNo: '2024110603', nickName: '周泓颖', userType: 1, accountStatus: 1, creditScore: 92, createTime: '2026-09-02 10:15:00' },
  { id: 5, studentNo: '2024110604', nickName: '测试用户A', userType: 1, accountStatus: 0, creditScore: 30, createTime: '2026-09-10 14:00:00' },
]

export const mockAuditGoods = [
  { id: 101, title: '九成新《数据结构》教材', sellerName: '张爱阳', price: 25.0, quality: '90', auditStatus: 'PENDING', createTime: '2026-10-25 12:00:00' },
  { id: 102, title: '高数习题集 全新未拆', sellerName: '周泓颖', price: 18.5, quality: 'NEW', auditStatus: 'PENDING', createTime: '2026-10-25 13:30:00' },
  { id: 103, title: '自行车 通勤代步', sellerName: '马智贤', price: 150.0, quality: '70', auditStatus: 'PENDING', createTime: '2026-10-25 15:00:00' },
]

export const mockReports = [
  { id: 1, goodsTitle: '可疑商品A', reporter: '测试用户A', reason: '疑似假货', status: 'PENDING', createTime: '2026-10-24 10:00:00' },
  { id: 2, goodsTitle: '二手电脑报价异常', reporter: '何曼妮', reason: '价格虚高', status: 'HANDLED', createTime: '2026-10-23 16:00:00' },
]

export const mockAdminCategories = buildCategoryTree()

/* ---------------- 接口实现 ---------------- */

export const adminApi = {
  /* ---- 角色 ---- */
  async getRoles() {
    await delay()
    const list = loadRoles()
    return { records: list, total: list.length, current: 1, size: 10 }
  },

  async getPermissionTree() {
    await delay(150)
    return mockPermissionTree
  },

  async getRolePerms(roleId) {
    await delay()
    return loadRolePerms()[String(roleId)] || []
  },

  /** 权限 ID -> 权限名，列表展示用 */
  getPermNames(permIds = []) {
    return permIds.map((id) => PERM_NAME_BY_ID[id]).filter(Boolean)
  },

  /**
   * 新增角色
   * @param {{roleCode,roleName,roleDesc?,status?,permIds?}} payload
   */
  async createRole(payload = {}) {
    await delay(300)
    const code = String(payload.roleCode || '').trim().toUpperCase()
    if (!code) throw new Error('角色编码不能为空')
    if (!CODE_RE.test(code)) throw new Error('角色编码只能是大写字母、数字、下划线，且以字母开头，长度 2–20')

    const list = loadRoles()
    if (list.some((r) => r.roleCode === code)) throw new Error(`角色编码「${code}」已存在`)

    const role = {
      id: nextRoleId(),
      roleCode: code,
      roleName: String(payload.roleName || '').trim() || code,
      roleDesc: String(payload.roleDesc || '').trim(),
      status: payload.status === 0 ? 0 : 1,
      createTime: nowText(),
    }
    list.push(role)
    saveRoles(list)

    if (Array.isArray(payload.permIds) && payload.permIds.length) {
      const perms = loadRolePerms()
      perms[String(role.id)] = payload.permIds.slice()
      saveRolePerms(perms)
    }

    return { success: true, role }
  },

  /**
   * 编辑角色（编码不可改，避免破坏已下发的权限码绑定）
   */
  async updateRole(roleId, payload = {}) {
    await delay(200)
    const list = loadRoles()
    const idx = list.findIndex((r) => String(r.id) === String(roleId))
    if (idx < 0) throw new Error('角色不存在')

    const role = list[idx]
    if (payload.roleName !== undefined) role.roleName = String(payload.roleName).trim() || role.roleName
    if (payload.roleDesc !== undefined) role.roleDesc = String(payload.roleDesc).trim()
    if (payload.status !== undefined) role.status = payload.status ? 1 : 0

    saveRoles(list)
    return { success: true, role }
  },

  async toggleRoleStatus(roleId, status) {
    return this.updateRole(roleId, { status })
  },

  /**
   * 删除角色：内置 ADMIN 不可删
   */
  async deleteRole(roleId) {
    await delay(200)
    const list = loadRoles()
    const role = list.find((r) => String(r.id) === String(roleId))
    if (!role) throw new Error('角色不存在')
    if (role.roleCode === 'ADMIN') throw new Error('系统管理员是内置角色，不能删除')

    saveRoles(list.filter((r) => String(r.id) !== String(roleId)))

    const perms = loadRolePerms()
    delete perms[String(roleId)]
    saveRolePerms(perms)
    return { success: true }
  },

  /**
   * 给角色分配权限
   * @param {number} roleId
   * @param {number[]} permIds 含半选父节点，否则菜单拿不到
   */
  async assignRolePerms(roleId, permIds = []) {
    await delay(250)
    const list = loadRoles()
    if (!list.some((r) => String(r.id) === String(roleId))) throw new Error('角色不存在')
    if (list.find((r) => String(r.id) === String(roleId)).roleCode === 'ADMIN') {
      throw new Error('系统管理员的权限固定为全部，不需要单独分配')
    }

    const perms = loadRolePerms()
    perms[String(roleId)] = permIds.slice()
    saveRolePerms(perms)
    return { success: true, total: permIds.length }
  },

  /* ---- 其他后台数据（保持原样） ---- */
  async getUsers() { await delay(); return mockUsers },
  async toggleUserStatus(userId, status) { await delay(); return { success: true, userId, status } },
  async getAuditGoods() { await delay(); return mockAuditGoods },
  async auditGoods(goodsId, pass, remark) { await delay(200); return { success: true, goodsId, pass, remark } },
  async getReports() { await delay(); return mockReports },
  async handleReport(reportId, action) { await delay(200); return { success: true, reportId, action } },
  async getCategoryTree() { await delay(); return mockAdminCategories },
}
