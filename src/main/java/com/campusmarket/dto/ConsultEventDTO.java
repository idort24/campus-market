package com.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 市场事件 DTO（生产 / 消费链路统一字段契约，对应 event_log 表）
 */
@Data
public class ConsultEventDTO {

    /** 事件唯一编号，重复投递沿用同一编号，幂等依据 */
    private String eventId;

    /** 会话编号，一人多设备算多个会话 */
    private String sessionId;

    /** 事件类型：view / favorite / order */
    private String eventType;

    /** 商品 ID */
    private Long itemId;

    /** 用户 ID */
    private Long userId;

    /** 产生事件的 Tomcat 节点（tomcat-1 / tomcat-2） */
    private String nodeName;

    /** 事件发生时间 */
    private LocalDateTime eventTime;

    /** 扩展字段（可选，JSON 字符串） */
    private String payload;
}
