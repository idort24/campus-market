package com.campusmarket.mq;

import com.campusmarket.dto.ConsultEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * 咨询事件生产者：把校验后的事件转 JSON 投递到 Artemis 主队列
 */
@Component
public class ConsultEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ConsultEventProducer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void send(ConsultEventDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            jmsTemplate.convertAndSend(MqConstants.CONSULT_QUEUE, json);
            log.info("[MQ-生产者] 事件已投递 eventId={} topic={} sessionId={}",
                    dto.getEventId(), dto.getTopic(), dto.getSessionId());
        } catch (Exception e) {
            log.error("[MQ-生产者] 事件投递失败 eventId={}", dto.getEventId(), e);
            throw new RuntimeException("事件投递失败", e);
        }
    }
}
