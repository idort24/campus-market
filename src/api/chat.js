// ============================================================
// 聊天接口层（后端 /api/v1/chat）
// ------------------------------------------------------------
// 路径按马智贤 2026-11 交付口径：
//   拉会话历史   GET  /api/v1/chat/messages?userId=对方id
//   发消息       POST /api/v1/chat/messages
//                body { receiverId, content, msgImage }
//                fromUserId 由后端从 token 取，前端不传（防止伪造）
//
// 后端返回字段（chat_message 表）：
//   id / senderId / receiverId / msgContent / msgImage /
//   isAiMessage / readFlag / riskHint / createTime
// 读历史时后端会自动把"对方发给我"的未读标成已读，前端不用另调。
// ============================================================
import http from './http'
import { USE_MOCK } from './config'
import { getUserInfo } from '../utils/auth.js'

const delay = (ms = 150) => new Promise((r) => setTimeout(r, ms))

// Mock 会话（演示用，与后端字段同名，方便切换）
const mockMessages = [
  { id: 1, senderId: 2, receiverId: 8, msgContent: '你好，这本书还在吗？', msgImage: null, isAiMessage: false, readFlag: true, riskHint: null, createTime: '2024-01-15 14:20' },
  { id: 2, senderId: 8, receiverId: 2, msgContent: '在的，九成新，可以图书馆门口交易', msgImage: null, isAiMessage: false, readFlag: true, riskHint: null, createTime: '2024-01-15 14:25' },
  { id: 3, senderId: 2, receiverId: 8, msgContent: '好，那我下午三点过去', msgImage: null, isAiMessage: false, readFlag: false, riskHint: null, createTime: '2024-01-15 14:28' },
]

/**
 * 后端消息 → 页面消息
 * mine = 这条是不是我发的（页面据此决定左右气泡）
 */
export function normalizeMessage(m = {}) {
  const me = getUserInfo()?.id
  const senderId = m.senderId ?? m.fromUserId ?? null
  return {
    ...m,
    id: m.id,
    senderId,
    receiverId: m.receiverId ?? m.toUserId ?? null,
    content: m.msgContent ?? m.content ?? '',
    image: m.msgImage ?? m.image ?? '',
    isAi: !!m.isAiMessage,
    read: m.readFlag ?? m.read ?? true,
    riskHint: m.riskHint ?? '',
    createTime: m.createTime ?? '',
    mine: me != null && String(senderId) === String(me),
  }
}

/** 拉取与某人的会话历史（userId = 对方 id） */
export async function getMessages(userId, params = {}) {
  if (USE_MOCK) {
    await delay()
    return mockMessages.map(normalizeMessage)
  }
  const res = await http.get('/chat/messages', { params: { userId, ...params } })
  const list = Array.isArray(res) ? res : (res?.records || [])
  return list.map(normalizeMessage)
}

/** 发消息（图片消息把 content 留空、传 msgImage） */
export async function sendMessage({ receiverId, content = '', msgImage = '' } = {}) {
  if (USE_MOCK) {
    await delay(120)
    const me = getUserInfo()?.id ?? 8
    const msg = {
      id: Date.now(),
      senderId: me,
      receiverId,
      msgContent: content,
      msgImage: msgImage || null,
      isAiMessage: false,
      readFlag: false,
      riskHint: null,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    }
    mockMessages.push(msg)
    return normalizeMessage(msg)
  }
  const data = await http.post('/chat/messages', { receiverId, content, msgImage })
  return data ? normalizeMessage(data) : null
}
