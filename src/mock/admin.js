// ============================================================
// 后台管理 Mock 数据（字段统一驼峰，与后端实体一致）
// ------------------------------------------------------------
// 权限码严格对齐后端 AuthInterceptor / sys_permission：
//   user:manage、user:status、goods:audit、report:handle、category:manage、dict:manage
// ============================================================
const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms))

// 角色列表
export const mockRoles = [
  { id: 1, roleCode: 'ADMIN', roleName: '系统管理员', roleDesc: '拥有全部权限', status: 1, createTime: '2026-10-01 10:00:00' },
  { id: 2, roleCode: 'STUDENT', roleName: '普通学生', roleDesc: '浏览与发布闲置', status: 1, createTime: '2026-10-01 10:00:00' },
  { id: 3, roleCode: 'MERCHANT', roleName: '商家', roleDesc: '可发布商品的卖家', status: 1, createTime: '2026-10-15 14:30:00' },
]

// 权限树（对应 sys_permission 表，permType: 1=MENU 存 URL，2=BUTTON 存权限码）
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

// 角色已分配的权限 ID 集合
export const mockRolePerms = {
  1: [1, 11, 2, 21, 22, 3, 31, 4, 41, 5, 51], // ADMIN 全量
  2: [], // STUDENT 无后台权限
  3: [1, 11], // MERCHANT 仅商品相关
}

// 用户列表
export const mockUsers = [
  { id: 1, studentNo: '2024110640', nickName: '何曼妮', userType: 9, accountStatus: 1, creditScore: 100, createTime: '2026-09-01 10:00:00' },
  { id: 2, studentNo: '2024110601', nickName: '张爱阳', userType: 1, accountStatus: 1, creditScore: 95, createTime: '2026-09-01 11:00:00' },
  { id: 3, studentNo: '2024110602', nickName: '马智贤', userType: 1, accountStatus: 1, creditScore: 88, createTime: '2026-09-02 09:30:00' },
  { id: 4, studentNo: '2024110603', nickName: '周泓颖', userType: 1, accountStatus: 1, creditScore: 92, createTime: '2026-09-02 10:15:00' },
  { id: 5, studentNo: '2024110604', nickName: '测试用户A', userType: 1, accountStatus: 0, creditScore: 30, createTime: '2026-09-10 14:00:00' },
]

// 待审核商品
export const mockAuditGoods = [
  { id: 101, title: '九成新《数据结构》教材', sellerName: '张爱阳', price: 25.0, quality: '90', auditStatus: 'PENDING', createTime: '2026-10-25 12:00:00' },
  { id: 102, title: '高数习题集 全新未拆', sellerName: '周泓颖', price: 18.5, quality: 'NEW', auditStatus: 'PENDING', createTime: '2026-10-25 13:30:00' },
  { id: 103, title: '自行车 通勤代步', sellerName: '马智贤', price: 150.0, quality: '70', auditStatus: 'PENDING', createTime: '2026-10-25 15:00:00' },
]

// 举报工单
export const mockReports = [
  { id: 1, goodsTitle: '可疑商品A', reporter: '测试用户A', reason: '疑似假货', status: 'PENDING', createTime: '2026-10-24 10:00:00' },
  { id: 2, goodsTitle: '二手电脑报价异常', reporter: '何曼妮', reason: '价格虚高', status: 'HANDLED', createTime: '2026-10-23 16:00:00' },
]

// 分类树（后台管理用）
export const mockAdminCategories = [
  {
    id: 1, name: '教材教辅', children: [
      { id: 11, name: '计算机教材' },
      { id: 12, name: '数学教材' },
    ],
  },
  {
    id: 2, name: '电子产品', children: [
      { id: 21, name: '手机' },
      { id: 22, name: '电脑' },
    ],
  },
  { id: 3, name: '生活用品', children: [] },
]

export const adminApi = {
  async getRoles() {
    await delay()
    // 后端 GET /roles 返回分页结构，前端兼容取出 records
    return { records: mockRoles, total: mockRoles.length, current: 1, size: 10 }
  },
  async getPermissionTree() { await delay(150); return mockPermissionTree },
  async getRolePerms(roleId) { await delay(); return mockRolePerms[roleId] || [] },
  async assignRolePerms(roleId, permIds) { await delay(300); mockRolePerms[roleId] = permIds; return { success: true } },
  async getUsers() { await delay(); return mockUsers },
  async toggleUserStatus(userId, status) { await delay(); return { success: true, userId, status } },
  async getAuditGoods() { await delay(); return mockAuditGoods },
  async auditGoods(goodsId, pass, remark) { await delay(200); return { success: true, goodsId, pass, remark } },
  async getReports() { await delay(); return mockReports },
  async handleReport(reportId, action) { await delay(200); return { success: true, reportId, action } },
  async getCategoryTree() { await delay(); return mockAdminCategories },
}
