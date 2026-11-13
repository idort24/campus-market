package com.campusmarket.ws;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 咨询大屏 WebSocket 端点：维护在线会话集合，向所有大屏广播快照。
 * 端点实例由容器按连接创建，不能注入 Spring Bean，会话集合用静态注册表。
 * 加 @Component 是为了让 ServerEndpointExporter 扫描并注册该端点类。
 */
@Component
@ServerEndpoint("/ws/consult")
public class ConsultWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(ConsultWebSocketServer.class);

    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("[WS] 大屏连接建立 sessionId={} 当前在线={}", session.getId(), SESSIONS.size());
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("[WS] 大屏连接关闭 sessionId={} 当前在线={}", session.getId(), SESSIONS.size());
    }

    /**
     * 向所有在线大屏广播一条快照 JSON
     */
    public static void broadcast(String json) {
        for (Session session : SESSIONS) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.warn("[WS] 推送失败 sessionId={}: {}", session.getId(), e.getMessage());
            }
        }
    }

    public static int onlineCount() {
        return SESSIONS.size();
    }
}
