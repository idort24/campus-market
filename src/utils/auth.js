// ============================================================
// 登录态统一管理
// ------------------------------------------------------------
// 所有「写登录态 / 读权限 / 清登录态」都走这里，避免各页面写法不一致。
// localStorage 键：token、userInfo、userRoles、userMenus
// ============================================================
import { getMenus } from '../api/auth.js'

export const LS_TOKEN = 'token'
export const LS_USER = 'userInfo'
export const LS_ROLES = 'userRoles'
export const LS_MENUS = 'userMenus'

/** 读当前用户信息（容错） */
export function getUserInfo() {
  try {
    return JSON.parse(localStorage.getItem(LS_USER) || '{}')
  } catch {
    return {}
  }
}

/** 读角色数组 */
export function getUserRoles() {
  try {
    const arr = JSON.parse(localStorage.getItem(LS_ROLES) || '[]')
    if (Array.isArray(arr) && arr.length) return arr
  } catch { /* ignore */ }
  const info = getUserInfo()
  return info.roleCode ? [info.roleCode] : []
}

/** 读权限码数组 */
export function getUserPerms() {
  const info = getUserInfo()
  return Array.isArray(info.perms) ? info.perms : []
}

/** 是否已登录 */
export function isLoggedIn() {
  return !!localStorage.getItem(LS_TOKEN)
}

/** 是否管理员（仅用于前端展示控制，真实鉴权在后端） */
export function isAdmin() {
  const info = getUserInfo()
  const roles = getUserRoles()
  return info.roleCode === 'ADMIN' || roles.includes('ADMIN')
}

/**
 * 写入登录态
 * @param {{token:string, user:object, roles?:string[], perms?:string[]}} data
 */
export function saveAuth({ token, user, roles = [], perms = [] } = {}) {
  localStorage.setItem(LS_TOKEN, token || '')
  const merged = { ...(user || {}), perms: perms.length ? perms : (user?.perms || []) }
  localStorage.setItem(LS_USER, JSON.stringify(merged))
  localStorage.setItem(LS_ROLES, JSON.stringify(roles.length ? roles : (user?.roleCode ? [user.roleCode] : [])))
  return merged
}

/**
 * 拉取并缓存菜单（登录成功后调用一次）
 * @returns {Promise<Array<{path,title,icon}>>}
 */
export async function loadAndCacheMenus() {
  try {
    const menus = await getMenus()
    if (Array.isArray(menus) && menus.length) {
      localStorage.setItem(LS_MENUS, JSON.stringify(menus))
      return menus
    }
  } catch { /* 取不到就用空，导航会回退基础菜单 */ }
  return []
}

/** 清空登录态 */
export function clearAuth() {
  localStorage.removeItem(LS_TOKEN)
  localStorage.removeItem(LS_USER)
  localStorage.removeItem(LS_ROLES)
  localStorage.removeItem(LS_MENUS)
}
