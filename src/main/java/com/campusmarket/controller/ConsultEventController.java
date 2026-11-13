package com.campusmarket.controller;

import com.campusmarket.common.R;
import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.entity.ConsultEvent;
import com.campusmarket.mq.ConsultEventProducer;
import com.campusmarket.service.ConsultEventService;
import com.campusmarket.service.ConsultStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 咨询事件接口：演示生产 → MQ → 消费落库 最小链路
 */
@RestController
@RequestMapping("/api/v1/consult")
public class ConsultEventController {

    @Autowired
    private ConsultEventProducer producer;

    @Autowired
    private ConsultEventService eventService;

    @Autowired
    private ConsultStatService statService;

    /**
     * 投递一条咨询事件到 MQ（异步消费落库）
     */
    @PostMapping("/events")
    public R publish(@RequestBody ConsultEventDTO dto) {
        if (dto.getTopic() == null || dto.getTopic().isBlank()) {
            return R.error(400, "主题 topic 不能为空");
        }
        if (dto.getEventId() == null || dto.getEventId().isBlank()) {
            dto.setEventId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (dto.getOccurredAt() == null) {
            dto.setOccurredAt(LocalDateTime.now());
        }
        if (dto.getSessionId() == null || dto.getSessionId().isBlank()) {
            dto.setSessionId("s-" + dto.getEventId().substring(0, 8));
        }
        if (dto.getSource() == null || dto.getSource().isBlank()) {
            dto.setSource("web");
        }

        producer.send(dto);

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", dto.getEventId());
        data.put("topic", dto.getTopic());
        return R.ok(data);
    }

    /**
     * 查询已落库事件（大屏 / 联调核对用，最新在前）
     */
    @GetMapping("/events")
    public R list() {
        var list = eventService.lambdaQuery()
                .orderByDesc(ConsultEvent::getCreateTime)
                .last("limit 200")
                .list();
        return R.ok(list);
    }

    /**
     * 统计快照：大屏首次加载 / 断线重连的 REST 兜底拉取
     */
    @GetMapping("/snapshot")
    public R snapshot() {
        return R.ok(statService.snapshot());
    }
}
