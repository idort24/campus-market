package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件明细表 event_log（mq_demo 库）。
 * 消费者从 Artemis 队列拉取事件，按 event_id 幂等落库。
 * 注意：本表位于 mq_demo 库，主数据源连的是 campus_market，这里用库名前缀跨库访问。
 */
@Data
@TableName("mq_demo.event_log")
public class EventLog {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 产生事件的 Tomcat 节点，证明负载均衡生效 */
    private String nodeName;

    /** 事件发生时间，分钟趋势按它聚合 */
    private LocalDateTime eventTime;

    /** 扩展字段（JSON 字符串） */
    private String payload;

    /** 落库时间 */
    private LocalDateTime createdAt;
}
