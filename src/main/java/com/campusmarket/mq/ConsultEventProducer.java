package com.campusmarket.mq;

import com.campusmarket.dto.ConsultEventDTO;
import com.campusmarket.service.MqCounterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * 市场事件生产者：把校验后的事件转 JSON 投递到 Artemis 主队列。
 * 每成功投递一条，mq_counter.produced 加 1，用于积压统计。
 */
@Component
public class ConsultEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ConsultEventProducer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MqCounterService counterService;

    public void send(ConsultEventDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            jmsTemplate.convertAndSend(MqConstants.CONSULT_QUEUE, json);
            counterService.incrementProduced();
            log.info("[MQ-生产者] 事件已投递 eventId={} type={} sessionId={} node={}",
                    dto.getEventId(), dto.getEventType(), dto.getSessionId(), dto.getNodeName());
        } catch (Exception e) {
            log.error("[MQ-生产者] 事件投递失败 eventId={}", dto.getEventId(), e);
            throw new RuntimeException("事件投递失败", e);
        }
    }
}
