// ============================================================
// 后台管理 Mock 数据
// ============================================================
const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms))

// 角色列表
export const mockRoles = [
  { id: 1, role_code: 'ADMIN', role_name: '系统管理员', role_desc: '拥有全部权限', status: 1, create_time: '2026-10-01 10:00:00' },
  { id: 2, role_code: 'STUDENT', role_name: '普通学生', role_desc: '普通借阅者/买家', status: 1, create_time: '2026-10-01 10:00:00' },
  { id: 3, role_code: 'MERCHANT', role_name: '商家', role_desc: '可发布商品的卖家', status: 1, create_time: '2026-10-15 14:30:00' },
]

// 权限树（对应 sys_permission 表，parent_id 树形）
export const mockPermissionTree = [
  {
    id: 1, perm_code: 'goods', perm_name: '商品管理', perm_type: 'MENU', parent_id: 0,
    children: [
      { id: 11, perm_code: 'goods:list', perm_name: '商品查看', perm_type: 'API', parent_id: 1 },
      { id: 12, perm_code: 'goods:publish', perm_name: '商品发布', perm_type: 'API', parent_id: 1 },
      { id: 13, perm_code: 'goods:audit', perm_name: '商品审核', perm_type: 'BUTTON', parent_id: 1 },
    ]
  },
  {
    id: 2, perm_code: 'user', perm_name: '用户管理', perm_type: 'MENU', parent_id: 0,
    children: [
      { id: 21, perm_code: 'user:list', perm_name: '用户查看', perm_type: 'API', parent_id: 2 },
      { id: 22, perm_code: 'user:ban', perm_name: '用户封禁', perm_type: 'BUTTON', parent_id: 2 },
    ]
  },
  {
    id: 3, perm_code: 'category', perm_name: '分类管理', perm_type: 'MENU', parent_id: 0,
    children: [
      { id: 31, perm_code: 'category:edit', perm_name: '分类增删改', perm_type: 'BUTTON', parent_id: 3 },
    ]
  },
  {
    id: 4, perm_code: 'report', perm_name: '举报管理', perm_type: 'MENU', parent_id: 0,
    children: [
      { id: 41, perm_code: 'report:handle', perm_name: '举报处理', perm_type: 'BUTTON', parent_id: 4 },
    ]
  },
  {
    id: 5, perm_code: 'role', perm_name: '角色权限', perm_type: 'MENU', parent_id: 0,
    children: [
      { id: 51, perm_code: 'role:list', perm_name: '角色查看', perm_type: 'API', parent_id: 5 },
      { id: 52, perm_code: 'role:assign', perm_name: '分配权限', perm_type: 'BUTTON', parent_id: 5 },
    ]
  },
]

// 角色已分配的权限 ID 集合
export const mockRolePerms = {
  1: [1, 11, 12, 13, 2, 21, 22, 3, 31, 4, 41, 5, 51, 52], // ADMIN 全量
  2: [1, 11, 12], // STUDENT 只能看商品和发布
  3: [1, 11, 12], // MERCHANT 同 STUDENT
}

// 用户列表
export const mockUsers = [
  { id: 1, student_no: '2024110606', nick_name: '何曼妮', user_type: 9, account_status: 1, credit_score: 100, create_time: '2026-09-01 10:00:00' },
  { id: 2, student_no: '2024110601', nick_name: '张爱阳', user_type: 1, account_status: 1, credit_score: 95, create_time: '2026-09-01 11:00:00' },
  { id: 3, student_no: '2024110602', nick_name: '马智贤', user_type: 1, account_status: 1, credit_score: 88, create_time: '2026-09-02 09:30:00' },
  { id: 4, student_no: '2024110603', nick_name: '周泓颖', user_type: 1, account_status: 1, credit_score: 92, create_time: '2026-09-02 10:15:00' },
  { id: 5, student_no: '2024110604', nick_name: '测试用户A', user_type: 1, account_status: 0, credit_score: 30, create_time: '2026-09-10 14:00:00' },
]

// 待审核商品
export const mockAuditGoods = [
  { id: 101, title: '九成新《数据结构》教材', seller_name: '张爱阳', price: 25.00, quality: '90', audit_status: 'PENDING', create_time: '2026-10-25 12:00:00' },
  { id: 102, title: '高数习题集 全新未拆', seller_name: '周泓颖', price: 18.50, quality: 'NEW', audit_status: 'PENDING', create_time: '2026-10-25 13:30:00' },
  { id: 103, title: '自行车 通勤代步', seller_name: '马智贤', price: 150.00, quality: '70', audit_status: 'PENDING', create_time: '2026-10-25 15:00:00' },
]

// 举报工单
export const mockReports = [
  { id: 1, goods_title: '可疑商品A', reporter: '测试用户A', reason: '疑似假货', status: 'PENDING', create_time: '2026-10-24 10:00:00' },
  { id: 2, goods_title: '二手电脑报价异常', reporter: '何曼妮', reason: '价格虚高', status: 'HANDLED', create_time: '2026-10-23 16:00:00' },
]

// 分类树（后台管理用）
export const mockAdminCategories = [
  {
    id: 1, name: '教材教辅', children: [
      { id: 11, name: '计算机教材' },
      { id: 12, name: '数学教材' },
    ]
  },
  {
    id: 2, name: '电子产品', children: [
      { id: 21, name: '手机' },
      { id: 22, name: '电脑' },
    ]
  },
  { id: 3, name: '生活用品', children: [] },
]

export const adminApi = {
  async getRoles() { await delay(); return mockRoles },
  async getPermissionTree() { await delay(150); return mockPermissionTree },
  async getRolePerms(roleId) { await delay(); return mockRolePerms[roleId] || [] },
  async assignRolePerms(roleId, permIds) { await delay(300); mockRolePerms[roleId] = permIds; return { success: true } },
  async getUsers() { await delay(); return mockUsers },
  async toggleUserStatus(userId, status) { await delay(); return { success: true } },
  async getAuditGoods() { await delay(); return mockAuditGoods },
  async auditGoods(goodsId, pass, remark) { await delay(200); return { success: true } },
  async getReports() { await delay(); return mockReports },
  async handleReport(reportId, action) { await delay(200); return { success: true } },
  async getCategoryTree() { await delay(); return mockAdminCategories },
}
