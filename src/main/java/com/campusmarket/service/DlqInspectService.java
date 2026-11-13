package com.campusmarket.service;

import com.campusmarket.dto.DlqMessageDTO;
import com.campusmarket.mq.MqConstants;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 死信队列浏览服务：只读枚举 DLQ 中的异常消息，不确认、不移除。
 */
@Service
public class DlqInspectService {

    private static final Logger log = LoggerFactory.getLogger(DlqInspectService.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 浏览死信队列当前消息列表。
     */
    public List<DlqMessageDTO> browseDlq() {
        return jmsTemplate.browse(MqConstants.DLQ_ADDRESS, (session, browser) -> {
            List<DlqMessageDTO> result = new ArrayList<>();
            Enumeration<?> enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                Message message = (Message) enumeration.nextElement();
                DlqMessageDTO dto = new DlqMessageDTO();
                dto.setMessageId(message.getJMSMessageID());
                dto.setReason(readString(message, "reason"));
                dto.setDeliveryCount((int) readLong(message, "JMSXDeliveryCount"));
                dto.setTimestamp(message.getJMSTimestamp());
                if (message instanceof TextMessage textMessage) {
                    dto.setBody(textMessage.getText());
                }
                result.add(dto);
            }
            return result;
        });
    }

    private String readString(Message message, String property) {
        try {
            return message.getStringProperty(property);
        } catch (Exception e) {
            return null;
        }
    }

    private long readLong(Message message, String property) {
        try {
            return message.getLongProperty(property);
        } catch (Exception e) {
            return 0L;
        }
    }
}
