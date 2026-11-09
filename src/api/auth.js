// ============================================================
// 认证相关接口封装（对应后端 7 个接口，见 docs/api-auth.md）
// ------------------------------------------------------------
// 约定：http.js 响应拦截器已拆包，这里直接拿到 data。
// USE_MOCK = true 时走本地 mock/auth.js，false 时走真实后端。
// ============================================================
import http from './http.js'
import { USE_MOCK } from './config.js'
import { authMock } from '../mock/auth.js'

/**
 * 1. 登录（带图形验证码）
 * @param {{username:string, password:string, captchaKey:string, captchaCode:string}} payload
 * @returns {Promise<{token:string, user:object, roles:string[], perms:string[]}>}
 */
export function login(payload) {
  if (USE_MOCK) return authMock.login(payload)
  return http.post('/user/login', payload)
}

/**
 * 2. 注册（学号 + 手机号 + 密码）
 * @param {{studentNo:string, phone:string, password:string}} payload
 */
export function register(payload) {
  if (USE_MOCK) return authMock.register(payload)
  return http.post('/user/register', payload)
}

/**
 * 3. 获取图形验证码
 * ------------------------------------------------------------
 * 后端（马智贤）：POST /api/v1/auth/captcha，返回 {captchaKey, imageBase64}
 * 注意是 POST 不是 GET，方法不一致会直接 405。
 * @returns {Promise<{captchaKey:string, imageBase64:string}>}
 */
export function getCaptcha() {
  if (USE_MOCK) return authMock.getCaptcha()
  return http.post('/auth/captcha')
}

/**
 * 4. 退出登录（后端把 token 写入 Redis 黑名单）
 */
export function logout() {
  if (USE_MOCK) return authMock.logout()
  return http.post('/auth/logout')
}

/**
 * 5. 忘记密码（手机号 + 短信验证码 → 重置新密码）
 * @param {{phone:string, code:string, newPassword:string}} payload
 */
export function resetPassword(payload) {
  if (USE_MOCK) return authMock.resetPassword(payload)
  return http.post('/auth/password/reset', payload)
}

/**
 * 6. 当前用户权限码列表
 * @returns {Promise<string[]>} 如 ['goods:audit', 'user:status']
 */
export function getPermissions() {
  if (USE_MOCK) return authMock.getPermissions()
  return http.get('/user/permissions')
}

/**
 * 7. 当前角色可见菜单（供导航 / 动态路由使用）
 * @returns {Promise<Array<{path:string, title:string, icon:string}>>}
 */
export function getMenus() {
  if (USE_MOCK) return authMock.getMenus()
  return http.get('/user/menus')
}

/**
 * 发送短信验证码（开发期模拟通道，马智贤 2026-11 提供，已上线 8e948d8）
 * ------------------------------------------------------------
 * 后端：POST /api/v1/auth/sms/code  body {phone}
 * 返回：{ code:1, data:{ captchaKey:"sms:13800000008", expireIn:300, devCode:"968871" } }
 * 开发期验证码直接在 devCode 里给出（后端控制台也打 [SMS-CODE] 日志），不用等真实短信。
 */
export function sendSmsCode(phone) {
  if (USE_MOCK) return authMock.sendSmsCode(phone)
  return http.post('/auth/sms/code', { phone })
}

/**
 * 短信验证码登录（登录的短信分支）
 * ------------------------------------------------------------
 * 仍走 POST /user/login，但 captchaKey 传 `sms:{手机号}`，后端据此按手机号查账号、校验短信码。
 * 马智贤确认：
 *   - password 传空字符串不会被拦（短信分支里 password 不参与校验）
 *   - 短信码后端只读 captchaCode，smsCode 字段不认，前端不要再传
 * @param {{phone:string, smsCode:string}} payload
 */
export function loginBySms({ phone, smsCode }) {
  const payload = {
    username: phone,
    password: '',
    captchaKey: `sms:${phone}`,
    captchaCode: smsCode,
  }
  if (USE_MOCK) return authMock.login(payload)
  return http.post('/user/login', payload)
}
