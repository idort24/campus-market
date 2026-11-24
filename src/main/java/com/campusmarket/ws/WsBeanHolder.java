package com.campusmarket.ws;

import com.campusmarket.cache.CacheStore;
import com.campusmarket.service.ChatMessageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket 端点持器。
 *
 * JSR-WebSocket 的端点实例由容器按连接创建，不是 Spring 管理的 Bean，
 * 不能在端点类里直接 @Autowired。这里由 Spring 容器启动后把需要的
 * Service / 缓存实现赋值到端点的静态字段，端点通过静态字段访问业务能力。
 */
@Component
public class WsBeanHolder {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private CacheStore cacheStore;

    @PostConstruct
    public void expose() {
        ChatWebSocketServer.setBeans(chatMessageService, cacheStore);
    }
}
