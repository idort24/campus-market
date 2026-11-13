package com.campusmarket.dto;

import lombok.Data;

/**
 * 死信队列消息查看项（只读浏览，不消费、不移除消息）
 */
@Data
public class DlqMessageDTO {

    /** JMS 消息编号 */
    private String messageId;

    /** 转入死信的原因（如 json_parse_failed） */
    private String reason;

    /** 已投递次数 */
    private Integer deliveryCount;

    /** 消息时间戳（毫秒） */
    private Long timestamp;

    /** 原始消息体（JSON 文本） */
    private String body;
}
