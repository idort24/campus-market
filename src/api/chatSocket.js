// ============================================================
// 实时聊天 WebSocket 客户端（原生 WebSocket，未用 Socket.IO 等封装）
// ------------------------------------------------------------
// 对应课堂要求：
// 1. 双向全双工：服务端可主动推送新消息；
// 2. 心跳保活：每 30 秒发一次 ping，服务端回 pong；
// 3. 断线重连：指数退避，间隔 1s -> 2s -> 4s -> 8s -> 16s，封顶 30s；
// 4. 握手鉴权：浏览器 WebSocket 不能自定义请求头，令牌走 query 参数 ?token=；
// 5. 消息回调：onMessage(payload) 收到服务端推送（message / ack / error）。
// ============================================================

const HEARTBEAT_MS = 30_000
const INITIAL_BACKOFF = 1_000
const MAX_BACKOFF = 30_000

/**
 * 建立聊天 WebSocket 连接
 * @param {{token:string, onMessage?:(payload:object)=>void, onStatus?:(s:'connected'|'disconnected')=>void}} opts
 * @returns {{send:(payload:object)=>boolean, close:()=>void, ready:boolean}}
 */
export function createChatSocket({ token, onMessage, onStatus } = {}) {
  let ws = null
  let heartbeatTimer = null
  let reconnectTimer = null
  let backoff = INITIAL_BACKOFF
  let manualClose = false
  let closed = false

  function wsUrl() {
    const proto = location.protocol === 'https:' ? 'wss' : 'ws'
    return `${proto}://${location.host}/ws/chat?token=${encodeURIComponent(token)}`
  }

  function connect() {
    if (closed) return
    ws = new WebSocket(wsUrl())

    ws.onopen = () => {
      backoff = INITIAL_BACKOFF
      onStatus?.('connected')
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      let payload
      try {
        payload = JSON.parse(event.data)
      } catch {
        return
      }
      // pong 是心跳回执，不进业务消息流
      if (payload.type === 'pong') return
      onMessage?.(payload)
    }

    ws.onclose = () => {
      stopHeartbeat()
      onStatus?.('disconnected')
      if (!manualClose) scheduleReconnect()
    }

    ws.onerror = () => {
      // 异常交给 onclose 统一走重连逻辑
      ws?.close()
    }
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, HEARTBEAT_MS)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function scheduleReconnect() {
    if (closed || manualClose) return
    reconnectTimer = setTimeout(() => {
      connect()
      backoff = Math.min(backoff * 2, MAX_BACKOFF)
    }, backoff)
  }

  /** 发送消息；连接未就绪返回 false，调用方可降级走 REST */
  function send(payload) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(payload))
      return true
    }
    return false
  }

  function close() {
    closed = true
    manualClose = true
    if (reconnectTimer) clearTimeout(reconnectTimer)
    stopHeartbeat()
    if (ws) {
      ws.onclose = null
      ws.close()
    }
  }

  connect()

  return {
    send,
    close,
    get ready() {
      return !!ws && ws.readyState === WebSocket.OPEN
    },
  }
}
