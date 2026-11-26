// ============================================================
// 评价接口层（后端 /api/v1/evaluation）
// ------------------------------------------------------------
//   发表评价   POST /evaluation/add
//              body { orderId, toUserId, score(1~5), evalContent }
//   我的评价   GET  /evaluation/my
//   收到的评价 GET  /evaluation/toUser/{userId}
// ============================================================
import http from './http'
import { USE_MOCK } from './config'

const delay = (ms = 120) => new Promise((r) => setTimeout(r, ms))

export function normalizeEval(e = {}) {
  return {
    ...e,
    id: e.id,
    orderId: e.orderId ?? null,
    fromUserId: e.fromUserId ?? null,
    toUserId: e.toUserId ?? null,
    score: Number(e.score ?? 0),
    content: e.evalContent ?? e.content ?? '',
    tag: e.tag ?? '',
    createTime: e.createTime ?? '',
  }
}

/** 发表评价（买家 → 卖家） */
export async function addEvaluation(payload) {
  if (USE_MOCK) {
    await delay()
    return normalizeEval({ id: Date.now(), ...payload, createTime: new Date().toISOString().slice(0, 19).replace('T', ' ') })
  }
  const data = await http.post('/evaluation/add', payload)
  return data ? normalizeEval(data) : null
}

/** 我给出的评价 */
export async function getMyEvaluations() {
  if (USE_MOCK) {
    await delay()
    return []
  }
  const res = await http.get('/evaluation/my')
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeEval)
}

/** 某用户收到的评价 */
export async function getEvaluationsToUser(userId) {
  if (USE_MOCK) {
    await delay()
    return []
  }
  const res = await http.get(`/evaluation/toUser/${userId}`)
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeEval)
}
