import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth } from '../utils/auth.js'

const http = axios.create({
  baseURL: '/api/v1', // 走 vite proxy，转发到后端
  timeout: 10000,
})

// 请求拦截器：自动带 token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 防止 401 连续弹多个框 / 连续跳转
let redirecting = false

function goLoginWithRedirect() {
  if (redirecting) return
  redirecting = true
  clearAuth()
  const current = window.location.pathname + window.location.search
  // 避免已在登录页时死循环
  const isLoginPage = current.includes('/login')
  const target = isLoginPage ? '/campus-market/login' : `/campus-market/login?redirect=${encodeURIComponent(current)}`
  window.location.href = target
}

// 响应拦截器：统一处理返回体 {code, msg, data}
http.interceptors.response.use(
  (res) => {
    const body = res.data || {}
    const { code, msg, data } = body

    // 后端未按约定包装（直接返回业务数据）时原样放行，避免误伤
    if (code === undefined) return body

    if (code === 1 || code === 200) return data

    if (code === 401) {
      ElMessage.error('登录已失效，请重新登录')
      goLoginWithRedirect()
      return Promise.reject(new Error(msg || '未登录'))
    }

    if (code === 403) {
      ElMessage.error(msg || '没有操作权限')
      return Promise.reject(new Error(msg || '没有操作权限'))
    }

    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (err) => {
    const status = err?.response?.status
    const msg = err?.response?.data?.msg

    if (status === 401) {
      ElMessage.error(msg || '登录已失效，请重新登录')
      goLoginWithRedirect()
      return Promise.reject(new Error(msg || '未登录'))
    }
    if (status === 403) {
      ElMessage.error(msg || '没有操作权限')
      return Promise.reject(new Error(msg || '没有操作权限'))
    }
    if (err.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
      return Promise.reject(err)
    }
    ElMessage.error(msg || '网络异常，请检查后端服务是否启动')
    return Promise.reject(err)
  }
)

export default http
