import axios from 'axios'

const http = axios.create({
  baseURL: '/api/v1',  // 走 vite proxy，转发到后端同学电脑（见 vite.config.js）
  timeout: 10000
})

// 请求拦截器：自动带 token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截器：统一处理返回码
http.interceptors.response.use(
  res => {
    const { code, msg, data } = res.data
    if (code === 1) return data
    if (code === 401) { window.location.href = '/login'; return Promise.reject(msg) }
    return Promise.reject(msg)
  },
  err => Promise.reject(err)
)

export default http
