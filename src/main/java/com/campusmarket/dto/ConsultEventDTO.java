package com.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询事件 DTO（生产 / 消费链路统一字段契约）
 */
@Data
public class ConsultEventDTO {

    /** 事件唯一编号，重复投递沿用同一编号，用于幂等校验 */
    private String eventId;

    /** 会话编号，同一会话可产生多条事件 */
    private String sessionId;

    /** 咨询主题 */
    private String topic;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 来源标记 */
    private String source;
}
