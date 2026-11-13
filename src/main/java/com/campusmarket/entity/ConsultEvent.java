package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询事件消费记录表（MQ 演示）
 * 消费者从 Artemis 队列拉取事件，按 eventId 幂等落库。
 */
@Data
@TableName("consult_event")
public class ConsultEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 事件唯一编号，重复投递沿用同一编号，用于幂等校验 */
    private String eventId;

    /** 会话编号，同一会话可产生多条事件 */
    private String sessionId;

    /** 咨询主题，用于主题分布统计 */
    private String topic;

    /** 事件发生时间 */
    private LocalDateTime occurredAt;

    /** 来源标记（模拟生成 / 真实来源） */
    private String source;

    /** 消费落库时间 */
    private LocalDateTime consumeTime;

    /** 入库时间 */
    private LocalDateTime createTime;
}
