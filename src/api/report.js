// ============================================================
// 举报接口层（后端 report 控制器）
// ------------------------------------------------------------
// 路径按马智贤核对口径：
//   提交举报   POST /api/v1/report/add
//   我的举报   GET  /api/v1/report/my
//
// 注意：不是 /reports。入参 goodsId + reason + contact。
// ============================================================
import http from './http'
import { USE_MOCK } from './config'

const delay = (ms = 120) => new Promise((r) => setTimeout(r, ms))

/** 提交举报 */
export async function addReport({ goodsId, reason, contact = '' } = {}) {
  if (USE_MOCK) {
    await delay()
    return { id: Date.now(), goodsId, reason, contact, status: 'PENDING' }
  }
  return http.post('/report/add', { goodsId, reason, contact })
}

/** 我的举报记录 */
export async function getMyReports(params = {}) {
  if (USE_MOCK) {
    await delay()
    return []
  }
  const res = await http.get('/report/my', { params })
  return Array.isArray(res) ? res : (res?.records || [])
}
