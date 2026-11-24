package com.campusmarket.ws;

import com.campusmarket.cache.CacheStore;
import com.campusmarket.common.CacheKeys;
import com.campusmarket.entity.ChatMessage;
import com.campusmarket.service.ChatMessageService;
import com.campusmarket.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 买卖家实时聊天 WebSocket 端点。
 *
 * 业务场景：校园集市买家与卖家站内即时沟通。
 *
 * 核心机制（对应课堂要求）：
 * 1. 双向全双工：基于原生 WebSocket，服务端可主动推送新消息；
 * 2. 握手鉴权：浏览器 WebSocket 不能自定义请求头，令牌走 query 参数 ?token=，
 *    服务端校验 JWT 签名/有效期与退出黑名单；
 * 3. 在线注册表：ONLINE 记录 userId -> 当前连接，投递路由按表定位接收端；
 * 4. 全局顺序号：SEQ 自增生成消息序号，用于顺序校验与去重；接入 Redis 后改为 INCR；
 * 5. 先落库后投递：消息先写 chat_message，再推在线接收方；离线方上线后由 REST 拉历史补推；
 * 6. 心跳与重连由客户端实现：30 秒 ping，服务端回 pong；断线指数退避重连。
 */
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketServer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static ChatMessageService chatMessageService;
    private static CacheStore cacheStore;

    /** 在线注册表：userId -> 当前连接（单端在线；多端可改为 Map<Long, Set<Session>>） */
    private static final Map<Long, Session> ONLINE = new ConcurrentHashMap<>();

    /** 全局消息序号：内存自增；接入 Redis 后改为 INCR 生成，保证多实例唯一且有序 */
    private static final AtomicLong SEQ = new AtomicLong(0);

    /** 由 WsBeanHolder 在 Spring 启动后注入业务 Bean */
    public static void setBeans(ChatMessageService service, CacheStore cache) {
        chatMessageService = service;
        cacheStore = cache;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        Long userId = authenticate(session);
        if (userId == null) {
            closeQuietly(session, CloseReason.CloseCodes.CANNOT_ACCEPT, "鉴权失败");
            return;
        }

        // 同一用户旧连接顶替下线（单端在线语义）
        Session old = ONLINE.put(userId, session);
        if (old != null && old.isOpen() && !old.equals(session)) {
            closeQuietly(old, CloseReason.CloseCodes.VIOLATED_POLICY, "账号在其他端上线");
        }
        session.getUserProperties().put("userId", userId);
        log.info("[WS-CHAT] 连接建立 sessionId={} userId={} 在线={}",
                session.getId(), userId, ONLINE.size());
    }

    @OnMessage
    public void onMessage(Session session, String text) {
        Long me = (Long) session.getUserProperties().get("userId");
        if (me == null) {
            return;
        }

        JsonNode node;
        try {
            node = MAPPER.readTree(text);
        } catch (Exception e) {
            sendError(session, "消息格式错误");
            return;
        }

        String type = node.path("type").asText("");
        switch (type) {
            case "ping":
                sendJson(session, Map.of("type", "pong", "ts", System.currentTimeMillis()));
                break;
            case "chat":
                handleChat(session, me, node);
                break;
            default:
                sendError(session, "未知消息类型: " + type);
        }
    }

    /**
     * 处理一条聊天消息：校验 -> 落库 -> 推接收方 -> 回执发送方。
     */
    private void handleChat(Session session, Long me, JsonNode node) {
        Long receiverId = parseLong(node, "receiverId");
        String content = text(node, "content");
        String msgImage = text(node, "msgImage");

        if (receiverId == null) {
            sendError(session, "缺少 receiverId");
            return;
        }
        if (receiverId.equals(me)) {
            sendError(session, "不能给自己发消息");
            return;
        }
        if (isBlank(content) && isBlank(msgImage)) {
            sendError(session, "消息内容不能为空");
            return;
        }

        ChatMessage message = new ChatMessage();
        message.setSenderId(me);
        message.setReceiverId(receiverId);
        message.setMsgContent(isBlank(content) ? "" : content.trim());
        message.setMsgImage(isBlank(msgImage) ? null : msgImage.trim());
        message.setIsAiMessage(0);
        message.setReadFlag(0);
        message.setRiskHint(0);
        message.setCreateTime(LocalDateTime.now());
        chatMessageService.save(message);

        long seq = SEQ.incrementAndGet();

        // 接收方在线则实时推送；离线不推，上线后 REST 拉历史消息补推
        Session peer = ONLINE.get(receiverId);
        if (peer != null && peer.isOpen()) {
            sendJson(peer, Map.of("type", "message", "seq", seq, "msg", message));
        }

        // 回执给发送方，携带落库后的消息与序号
        sendJson(session, Map.of("type", "ack", "seq", seq, "msg", message));
    }

    @OnClose
    public void onClose(Session session) {
        Long me = (Long) session.getUserProperties().get("userId");
        if (me != null) {
            ONLINE.remove(me, session);
        }
        log.info("[WS-CHAT] 连接关闭 sessionId={} userId={} 在线={}",
                session.getId(), me, ONLINE.size());
    }

    @OnError
    public void onError(Session session, Throwable t) {
        Long me = (Long) session.getUserProperties().get("userId");
        if (me != null) {
            ONLINE.remove(me, session);
        }
        log.warn("[WS-CHAT] 异常 sessionId={} userId={}: {}",
                session.getId(), me, t.getMessage());
    }

    /**
     * 握手鉴权：解析 query 中的 token，校验签名/有效期/退出黑名单。
     */
    private Long authenticate(Session session) {
        URI uri = session.getRequestURI();
        String query = uri == null ? null : uri.getQuery();
        String token = null;
        if (query != null) {
            for (String kv : query.split("&")) {
                if (kv.startsWith("token=")) {
                    token = kv.substring("token=".length());
                    break;
                }
            }
        }
        if (token == null || token.isBlank()) {
            return null;
        }
        if (JwtUtil.isTokenInvalid(token)) {
            return null;
        }
        if (cacheStore != null && cacheStore.exists(CacheKeys.TOKEN_BLACK + token)) {
            return null;
        }
        try {
            return JwtUtil.getUserIdByToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendJson(Session session, Object payload) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = MAPPER.writeValueAsString(payload);
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.warn("[WS-CHAT] 推送失败 sessionId={}: {}", session.getId(), e.getMessage());
        }
    }

    private void sendError(Session session, String msg) {
        sendJson(session, Map.of("type", "error", "msg", msg));
    }

    private void closeQuietly(Session session, CloseReason.CloseCodes code, String reason) {
        try {
            session.close(new CloseReason(code, reason));
        } catch (IOException ignored) {
            // 关闭失败无需处理
        }
    }

    private static Long parseLong(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        try {
            return Long.valueOf(v.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
