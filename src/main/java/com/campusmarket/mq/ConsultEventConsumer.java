package com.campusmarket.mq;

import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.entity.EventLog;
import com.campusmarket.service.ConsultStatService;
import com.campusmarket.service.EventLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 市场事件消费者：从 Artemis 主队列拉取事件，按 event_id 幂等落库。
 * 并发数固定为 1，保证单消费者按节奏消化；唯一索引兜底重复投递。
 * 唯一索引冲突：记日志后正常 ack，不重试、不外抛。
 * JSON 解析失败：原样转入死信队列 DLQ 等待排查，不静默丢弃。
 * 落库成功后重建统计快照写入 stat_snapshot。
 */
@Component
public class ConsultEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ConsultEventConsumer.class);

    @Autowired
    private EventLogService eventService;

    @Autowired
    private ConsultStatService statService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = MqConstants.CONSULT_QUEUE, concurrency = "1")
    public void onMessage(String json) {
        ConsultEventDTO dto;
        try {
            dto = objectMapper.readValue(json, ConsultEventDTO.class);
        } catch (Exception e) {
            log.error("[MQ-消费者] 消息解析失败，转入死信队列: {}", json, e);
            sendToDlq(json, "json_parse_failed");
            return;
        }

        // 幂等：同一 eventId 已入库则跳过，唯一索引做兜底
        long exists = eventService.lambdaQuery()
                .eq(EventLog::getEventId, dto.getEventId())
                .count();
        if (exists > 0) {
            log.info("[MQ-消费者] 重复事件已忽略 eventId={}", dto.getEventId());
            statService.rebuildSnapshot();
            return;
        }

        EventLog event = new EventLog();
        BeanUtils.copyProperties(dto, event);
        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        event.setCreatedAt(LocalDateTime.now());

        try {
            eventService.save(event);
        } catch (DuplicateKeyException e) {
            // 并发/重投下唯一索引冲突：记日志后正常 ack，不重试、不抛出
            log.info("[MQ-消费者] 唯一索引冲突，重复事件已忽略 eventId={}", dto.getEventId());
            statService.rebuildSnapshot();
            return;
        }

        log.info("[MQ-消费者] 事件落库 eventId={} type={} sessionId={} node={}",
                event.getEventId(), event.getEventType(), event.getSessionId(), event.getNodeName());

        // 落库后重算快照并写 stat_snapshot，大屏只读这张表
        statService.rebuildSnapshot();
    }

    /**
     * 将无法处理的原始消息转入死信队列，附带失败原因属性。
     */
    private void sendToDlq(String json, String reason) {
        try {
            jmsTemplate.send(MqConstants.DLQ_ADDRESS, session -> {
                jakarta.jms.TextMessage message = session.createTextMessage(json);
                message.setStringProperty("reason", reason);
                return message;
            });
        } catch (Exception dlqException) {
            log.error("[MQ-消费者] 转入死信队列失败: {}", json, dlqException);
        }
    }
}
