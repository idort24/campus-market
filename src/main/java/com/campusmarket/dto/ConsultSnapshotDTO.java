package com.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计快照：大屏一次拉取 / WebSocket 广播的完整数据结构。
 * 数据来自 stat_snapshot 表，大屏不直接 count 明细表。
 */
@Data
public class ConsultSnapshotDTO {

    /** 版本号：max(event_log.id)，非连续计数；前端用它丢弃旧版本数据 */
    private Long version;

    /** 已消费事件总数（event_log 行数） */
    private Long totalEvents;

    /** 会话数（按 session_id 去重） */
    private Long sessionCount;

    /** 类型分布：event_type -> 事件数（view / favorite / order） */
    private Map<String, Long> typeDistribution;

    /** 分钟趋势，按时间升序 */
    private List<ConsultTrendPointDTO> minuteTrend;

    /** 队列积压：生产总数 − 已消费数 */
    private Long backlogCount;

    /** 快照时间 */
    private LocalDateTime snapshotTime;
}
