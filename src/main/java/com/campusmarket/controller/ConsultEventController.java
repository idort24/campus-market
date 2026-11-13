package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.entity.EventLog;
import com.campusmarket.mq.ConsultEventProducer;
import com.campusmarket.service.ConsultStatService;
import com.campusmarket.service.EventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 市场事件接口：演示生产 → MQ → 消费落库 → 快照 → 大屏推送 最小链路
 */
@RestController
@RequestMapping("/api/v1/consult")
public class ConsultEventController {

    @Autowired
    private ConsultEventProducer producer;

    @Autowired
    private EventLogService eventService;

    @Autowired
    private ConsultStatService statService;

    /**
     * 投递一条市场事件到 MQ（异步消费落库）
     */
    @PostMapping("/events")
    public R publish(@RequestBody ConsultEventDTO dto) {
        if (dto.getEventType() == null || dto.getEventType().isBlank()) {
            return R.error(400, "事件类型 eventType 不能为空（view / favorite / order）");
        }
        if (dto.getItemId() == null) {
            return R.error(400, "商品 itemId 不能为空");
        }
        if (dto.getUserId() == null) {
            return R.error(400, "用户 userId 不能为空");
        }
        if (dto.getEventId() == null || dto.getEventId().isBlank()) {
            dto.setEventId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (dto.getEventTime() == null) {
            dto.setEventTime(LocalDateTime.now());
        }
        if (dto.getSessionId() == null || dto.getSessionId().isBlank()) {
            dto.setSessionId("sess-" + dto.getEventId().substring(0, 8));
        }
        if (dto.getNodeName() == null || dto.getNodeName().isBlank()) {
            dto.setNodeName("tomcat-1");
        }

        producer.send(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", dto.getEventId());
        data.put("eventType", dto.getEventType());
        data.put("itemId", dto.getItemId());
        data.put("userId", dto.getUserId());
        return R.ok(data);
    }

    /**
     * 查询已落库事件（联调核对用，最新在前）
     */
    @GetMapping("/events")
    public R list() {
        var list = eventService.lambdaQuery()
                .orderByDesc(EventLog::getId)
                .last("limit 200")
                .list();
        return R.ok(list);
    }

    /**
     * 统计快照：大屏首次加载 / 断线重连的 REST 兜底拉取（只读 stat_snapshot）
     */
    @GetMapping("/snapshot")
    public R snapshot() {
        return R.ok(statService.snapshot());
    }
}
