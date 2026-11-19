// ============================================================
// 后台管理接口（角色 / 权限）
// ------------------------------------------------------------
// 与其它 api/*.js 保持同一套约定：USE_MOCK 时走本地 mock，
// false 时走真实后端，页面不直接依赖 mock。
// 后端约定（马智贤）：GET /roles 分页 {records}、GET /permissions/tree、
// GET /roles/{id}/perms、PUT /roles/{id}/perms、POST /roles、
// PUT /roles/{id}、DELETE /roles/{id}。路径若与后端实际不符，只改本文件。
// ============================================================
import http from './http.js'
import { USE_MOCK } from './config.js'
import { adminApi as mockAdmin } from '../mock/admin.js'

export const roleApi = {
  /** 角色列表（返回 {records,total,...} 或数组） */
  list() {
    return USE_MOCK ? mockAdmin.getRoles() : http.get('/roles')
  },

  /** 权限树（菜单 + 按钮） */
  permissionTree() {
    return USE_MOCK ? mockAdmin.getPermissionTree() : http.get('/permissions/tree')
  },

  /** 某个角色已分配的权限 ID */
  rolePerms(roleId) {
    return USE_MOCK ? mockAdmin.getRolePerms(roleId) : http.get(`/roles/${roleId}/perms`)
  },

  /** 分配权限（permIds 需包含半选父节点） */
  assign(roleId, permIds) {
    return USE_MOCK
      ? mockAdmin.assignRolePerms(roleId, permIds)
      : http.put(`/roles/${roleId}/perms`, { permIds })
  },

  /** 新增角色 */
  create(payload) {
    return USE_MOCK ? mockAdmin.createRole(payload) : http.post('/roles', payload)
  },

  /** 编辑角色（角色编码不允许修改） */
  update(roleId, payload) {
    return USE_MOCK ? mockAdmin.updateRole(roleId, payload) : http.put(`/roles/${roleId}`, payload)
  },

  /** 删除角色 */
  remove(roleId) {
    return USE_MOCK ? mockAdmin.deleteRole(roleId) : http.delete(`/roles/${roleId}`)
  },

  /** 启用 / 停用角色 */
  toggleStatus(roleId, status) {
    return USE_MOCK
      ? mockAdmin.toggleRoleStatus(roleId, status)
      : http.put(`/roles/${roleId}`, { status })
  },

  /** 权限 ID 转名称，仅 mock 下用于列表展示 */
  permNames(permIds = []) {
    return mockAdmin.getPermNames(permIds)
  },
}
