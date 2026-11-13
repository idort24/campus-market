package com.campusmarket.ws;

import com.campusmarket.dto.ConsultSnapshotDTO;
import com.campusmarket.service.ConsultStatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 快照定时广播：每 250ms 合并生成一次快照，推送给所有在线大屏。
 * 无在线连接时跳过查询，避免空跑；版本号由快照自带，前端据此丢弃旧数据。
 */
@Component
public class ConsultBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(ConsultBroadcastService.class);

    @Autowired
    private ConsultStatService statService;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 250)
    public void broadcastSnapshot() {
        if (ConsultWebSocketServer.onlineCount() == 0) {
            return;
        }
        ConsultSnapshotDTO snapshot = statService.snapshot();
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            ConsultWebSocketServer.broadcast(json);
        } catch (Exception e) {
            log.warn("[WS] 快照序列化/广播失败: {}", e.getMessage());
        }
    }
}
