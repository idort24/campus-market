package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计快照表 stat_snapshot（mq_demo 库）。
 * 大屏只读这张表，不直接 count 明细表；stat_key 唯一，按 key upsert。
 */
@Data
@TableName("mq_demo.stat_snapshot")
public class StatSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指标键：version / total / session / type:view / trend:yyyy-MM-dd HH:mm / backlog */
    private String statKey;

    /** 指标值 */
    private Long statValue;

    /** 快照版本号 = 计算时的 max(event_log.id) */
    private Long version;

    /** 快照时间 */
    private LocalDateTime snapshotTime;
}
