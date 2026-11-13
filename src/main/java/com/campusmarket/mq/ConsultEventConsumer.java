package com.campusmarket.mq;

import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.entity.ConsultEvent;
import com.campusmarket.service.ConsultEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 咨询事件消费者：从 Artemis 主队列拉取事件，按 eventId 幂等校验后落库。
 * 解析失败的消息先记日志直接返回（待完善：应转入死信队列）。
 */
@Component
public class ConsultEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConsultEventConsumer.class);

    @Autowired
    private ConsultEventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @JmsListener(destination = MqConstants.CONSULT_QUEUE)
    public void onMessage(String json) {
        ConsultEventDTO dto;
        try {
            dto = objectMapper.readValue(json, ConsultEventDTO.class);
        } catch (Exception e) {
            log.error("[MQ-消费者] 消息解析失败，消息丢弃: {}", json, e);
            return;
        }

        // 幂等：同一 eventId 已入库则跳过，唯一索引做兜底
        long exists = eventService.lambdaQuery()
                .eq(ConsultEvent::getEventId, dto.getEventId())
                .count();
        if (exists > 0) {
            log.info("[MQ-消费者] 重复事件已忽略 eventId={}", dto.getEventId());
            return;
        }

        ConsultEvent event = new ConsultEvent();
        BeanUtils.copyProperties(dto, event);
        event.setConsumeTime(LocalDateTime.now());
        eventService.save(event);
        log.info("[MQ-消费者] 事件落库 eventId={} topic={} sessionId={}",
                event.getEventId(), event.getTopic(), event.getSessionId());
    }
}
