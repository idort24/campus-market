package com.campusmarket.mq;

import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.entity.EventLog;
import com.campusmarket.service.ConsultStatService;
import com.campusmarket.service.EventLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 市场事件消费者：从 Artemis 主队列拉取事件，按 event_id 幂等落库。
 * 唯一索引兜底：重复投递抛 DuplicateKeyException 时只记日志并 ack，
 * 不重试、不外抛，避免一直重投最后进死信队列。
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
}
