// ============================================================
// 认证 Mock 层（USE_MOCK = true 时使用）
// ------------------------------------------------------------
// 目的：后端接口就绪前，前端能跑通「注册 → 登录（含验证码）→ 授权 →
//       退出失效 → 忘记密码重置」完整链路，答辩可演示。
// 约定返回结构与真实后端完全一致：
//   {token, user, roles, perms} / string[] / {path,title,icon}[]
// ============================================================

const delay = (ms = 300) => new Promise((r) => setTimeout(r, ms))

// ---------- 权限码（必须与后端 AuthInterceptor 完全一致）----------
export const PERMISSION_CODES = [
  'user:manage',
  'user:status',
  'goods:audit',
  'report:handle',
  'category:manage',
  'dict:manage',
]

// ---------- 菜单定义（后端 /user/menus 的 mock 版本）----------
const MENU_HOME = { path: '/', title: '首页', icon: '🏠' }
const MENU_PRODUCT = { path: '/product', title: '商品市场', icon: '🛍️' }
const MENU_CATEGORY = { path: '/categories', title: '分类浏览', icon: '🗂️' }
const MENU_PROFILE = { path: '/profile', title: '个人中心', icon: '👤' }
const MENU_ADMIN = { path: '/admin', title: '管理后台', icon: '🔐' }

// 管理员 5 条，普通学生 2 条（对应验收：管理员见 5 个菜单、普通用户见 2 个）
const MENUS_BY_ROLE = {
  ADMIN: [MENU_HOME, MENU_PRODUCT, MENU_CATEGORY, MENU_PROFILE, MENU_ADMIN],
  STUDENT: [MENU_HOME, MENU_PRODUCT],
}

// ---------- 内置账号 ----------
const BUILTIN_USERS = [
  {
    id: 1,
    studentNo: '2024110640',
    phone: '13800000000',
    nickName: '管理员',
    password: '123456',
    roleCode: 'ADMIN',
    roleName: '系统管理员',
    createTime: '2026-09-01 10:00:00',
  },
  {
    id: 2,
    studentNo: '2024110601',
    phone: '13900000000',
    nickName: '张爱阳',
    password: '123456',
    roleCode: 'STUDENT',
    roleName: '普通学生',
    createTime: '2026-09-01 11:00:00',
  },
]

// ---------- 本地存储键 ----------
const LS_USERS = 'cm_mock_users'
const LS_CAPTCHA = 'cm_mock_captcha'
const LS_TOKEN_USER = 'cm_mock_token_user'
const LS_SMS = 'cm_mock_sms'

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

// ---------- 用户池（内置 + 本地注册的）----------
function getAllUsers() {
  return [...BUILTIN_USERS, ...readJSON(LS_USERS, [])]
}

function toPublicUser(u) {
  // 对外不暴露密码
  return {
    id: u.id,
    studentNo: u.studentNo,
    phone: u.phone,
    nickName: u.nickName,
    roleCode: u.roleCode,
    roleName: u.roleName,
    createTime: u.createTime,
  }
}

// ---------- 图形验证码：生成 4 位字符 + SVG data URI ----------
const CAPTCHA_CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'

function randomCode(len = 4) {
  let s = ''
  for (let i = 0; i < len; i += 1) {
    s += CAPTCHA_CHARS[Math.floor(Math.random() * CAPTCHA_CHARS.length)]
  }
  return s
}

function buildCaptchaSvg(code) {
  const colors = ['#2563eb', '#0f766e', '#b45309', '#7c3aed']
  const chars = code
    .split('')
    .map((c, i) => {
      const x = 14 + i * 30
      const y = 34 + Math.floor(Math.random() * 6) - 3
      const rotate = Math.floor(Math.random() * 30) - 15
      const color = colors[i % colors.length]
      return `<text x="${x}" y="${y}" font-size="26" font-weight="700" font-family="Arial,Helvetica,sans-serif" fill="${color}" transform="rotate(${rotate} ${x} ${y})">${c}</text>`
    })
    .join('')

  // 干扰线 + 噪点
  const lines = [0, 1, 2]
    .map(() => {
      const x1 = Math.floor(Math.random() * 40)
      const y1 = Math.floor(Math.random() * 50)
      const x2 = 100 + Math.floor(Math.random() * 40)
      const y2 = Math.floor(Math.random() * 50)
      return `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="#cbd5e1" stroke-width="1"/>`
    })
    .join('')

  const dots = Array.from({ length: 18 })
    .map(() => {
      const cx = Math.floor(Math.random() * 140)
      const cy = Math.floor(Math.random() * 50)
      return `<circle cx="${cx}" cy="${cy}" r="1" fill="#cbd5e1"/>`
    })
    .join('')

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="140" height="50" viewBox="0 0 140 50">
    <rect width="140" height="50" fill="#f8fafc"/>
    ${lines}${dots}${chars}
  </svg>`

  // encodeURIComponent + unescape 兼容中文，这里纯 ASCII，直接用 btoa 分支
  const encoded = typeof window !== 'undefined' && window.btoa
    ? window.btoa(unescape(encodeURIComponent(svg)))
    : Buffer.from(svg).toString('base64')

  return `data:image/svg+xml;base64,${encoded}`
}

// ---------- 业务实现 ----------
export const authMock = {
  // 获取图形验证码：key + 图片，答案存本地（真实后端存 Redis，TTL 60s）
  async getCaptcha() {
    await delay(120)
    const code = randomCode()
    const key = `cap_${Date.now()}_${Math.floor(Math.random() * 100000)}`
    writeJSON(LS_CAPTCHA, { key, code, expireAt: Date.now() + 60000 })
    return { captchaKey: key, imageBase64: buildCaptchaSvg(code) }
  },

  // 校验验证码（一次性：无论成功失败都作废，跟后端一致）
  verifyCaptcha(captchaKey, captchaCode) {
    const saved = readJSON(LS_CAPTCHA, null)
    localStorage.removeItem(LS_CAPTCHA)
    if (!saved) return '验证码已失效，请点击图片刷新'
    if (saved.key !== captchaKey) return '验证码已失效，请点击图片刷新'
    if (Date.now() > saved.expireAt) return '验证码已过期，请重新获取'
    if (!captchaCode || saved.code.toUpperCase() !== String(captchaCode).trim().toUpperCase()) {
      return '验证码错误'
    }
    return null
  },

  // 登录
  // 两个分支：
  //   图形码分支：captchaKey 是 getCaptcha() 返回的 key，校验 4 位图形码
  //   短信分支  ：captchaKey 形如 sms:13800000000（马智贤后端口径），校验 6 位短信码，不校验密码
  async login({ username, password, captchaKey, captchaCode, smsCode } = {}) {
    await delay(400)

    const isSms = typeof captchaKey === 'string' && captchaKey.startsWith('sms:')
    if (isSms) {
      const phone = captchaKey.slice(4)
      const code = String(smsCode ?? captchaCode ?? '').trim()
      const saved = readJSON(LS_SMS, null)
      if (!saved || saved.phone !== phone) throw new Error('请先获取短信验证码')
      if (Date.now() > saved.expireAt) throw new Error('短信验证码已过期，请重新获取')
      if (saved.code !== code) throw new Error('短信验证码错误')
      localStorage.removeItem(LS_SMS) // 一次性，避免复用
    } else {
      const capErr = this.verifyCaptcha(captchaKey, captchaCode)
      if (capErr) throw new Error(capErr)
    }

    const account = String(username || '').trim()
    if (!account) throw new Error('请输入学号或手机号')
    if (!isSms && !password) throw new Error('请输入密码')

    const user = getAllUsers().find(
      (u) => u.studentNo === account || u.phone === account
    )
    if (!user) throw new Error('账号不存在，请先注册')
    if (!isSms && user.password !== password) throw new Error('密码错误')

    const token = `mock.${window.btoa(unescape(encodeURIComponent(user.studentNo)))}.${Date.now()}`
    const roles = [user.roleCode]
    const perms = user.roleCode === 'ADMIN' ? [...PERMISSION_CODES] : []

    // token → 用户 映射，供退出/权限接口取用
    const tokenMap = readJSON(LS_TOKEN_USER, {})
    tokenMap[token] = user.studentNo
    writeJSON(LS_TOKEN_USER, tokenMap)

    return { token, user: toPublicUser(user), roles, perms }
  },

  // 注册
  async register({ studentNo, phone, password } = {}) {
    await delay(400)

    const no = String(studentNo || '').trim()
    const ph = String(phone || '').trim()

    if (!/^\d{8,12}$/.test(no)) throw new Error('学号格式不正确（8-12 位数字）')
    if (!/^1[3-9]\d{9}$/.test(ph)) throw new Error('手机号格式不正确')
    if (!password || String(password).length < 6) throw new Error('密码至少 6 位')

    const users = getAllUsers()
    if (users.some((u) => u.studentNo === no)) throw new Error('该学号已注册')
    if (users.some((u) => u.phone === ph)) throw new Error('该手机号已注册')

    const newUser = {
      id: Date.now(),
      studentNo: no,
      phone: ph,
      nickName: `同学${no.slice(-4)}`,
      password: String(password),
      roleCode: 'STUDENT', // 注册默认绑 STUDENT，与后端一致
      roleName: '普通学生',
      createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
    }
    const local = readJSON(LS_USERS, [])
    local.push(newUser)
    writeJSON(LS_USERS, local)

    return { user: toPublicUser(newUser), message: '注册成功，请登录' }
  },

  // 退出登录：清 mapping，模拟后端写 Redis 黑名单
  async logout() {
    await delay(100)
    const token = localStorage.getItem('token')
    if (token) {
      const tokenMap = readJSON(LS_TOKEN_USER, {})
      delete tokenMap[token]
      writeJSON(LS_TOKEN_USER, tokenMap)
    }
    return { success: true }
  },

  // 发送短信验证码（忘记密码）
  async sendSmsCode(phone) {
    await delay(300)
    const ph = String(phone || '').trim()
    if (!/^1[3-9]\d{9}$/.test(ph)) throw new Error('手机号格式不正确')
    const code = String(Math.floor(100000 + Math.random() * 900000))
    writeJSON(LS_SMS, { phone: ph, code, expireAt: Date.now() + 5 * 60000 })
    // 演示方便：把验证码回显出来（真实环境是发短信）
    return { success: true, debugCode: code }
  },

  // 忘记密码：校验短信码 → 重置
  async resetPassword({ phone, code, newPassword } = {}) {
    await delay(400)
    const ph = String(phone || '').trim()
    const saved = readJSON(LS_SMS, null)
    if (!saved || saved.phone !== ph) throw new Error('请先获取短信验证码')
    if (Date.now() > saved.expireAt) throw new Error('短信验证码已过期')
    if (String(code).trim() !== saved.code) throw new Error('短信验证码错误')
    if (!newPassword || String(newPassword).length < 6) throw new Error('新密码至少 6 位')

    // 改内置账号或本地注册账号（本地注册的可直接改）
    const local = readJSON(LS_USERS, [])
    const target = local.find((u) => u.phone === ph)
    if (target) {
      target.password = String(newPassword)
      writeJSON(LS_USERS, local)
    } else {
      const builtin = BUILTIN_USERS.find((u) => u.phone === ph)
      if (!builtin) throw new Error('该手机号未注册')
      builtin.password = String(newPassword)
    }

    localStorage.removeItem(LS_SMS)
    return { success: true, message: '密码已重置，请重新登录' }
  },

  // 当前用户权限码
  async getPermissions() {
    await delay(100)
    const info = readJSON('userInfo', {})
    if (info.roleCode === 'ADMIN') return [...PERMISSION_CODES]
    return Array.isArray(info.perms) ? info.perms : []
  },

  // 当前角色可见菜单
  async getMenus() {
    await delay(100)
    const info = readJSON('userInfo', {})
    const role = info.roleCode || 'STUDENT'
    return MENUS_BY_ROLE[role] || MENUS_BY_ROLE.STUDENT
  },

  // 供调试：清空本地 mock 数据
  resetAll() {
    localStorage.removeItem(LS_USERS)
    localStorage.removeItem(LS_CAPTCHA)
    localStorage.removeItem(LS_TOKEN_USER)
    localStorage.removeItem(LS_SMS)
  },
}
