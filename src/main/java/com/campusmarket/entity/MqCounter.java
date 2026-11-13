package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生产计数表 mq_counter（mq_demo 库）。
 * 积压 = 生产总数 produced − 已消费数（event_log 行数）。
 */
@Data
@TableName("mq_demo.mq_counter")
public class MqCounter {

    /** 固定为 produced */
    @TableId(type = IdType.INPUT)
    private String counterKey;

    /** 生产者累计发送条数 */
    private Long counterValue;

    private LocalDateTime updatedAt;
}
