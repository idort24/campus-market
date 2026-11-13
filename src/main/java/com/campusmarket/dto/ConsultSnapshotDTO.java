package com.campusmarket.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 咨询统计快照：大屏一次拉取 / WebSocket 广播的完整数据结构
 */
@Data
public class ConsultSnapshotDTO {

    /** 版本号：取数据库最大主键 id，非连续计数；前端用它丢弃旧版本数据 */
    private Long version;

    /** 已消费事件总数（consumed event 表行数） */
    private Long totalEvents;

    /** 服务会话数（按 sessionId 去重） */
    private Long sessionCount;

    /** 主题分布：topic -> 事件数 */
    private Map<String, Long> topicDistribution;

    /** 分钟趋势，按时间升序 */
    private List<ConsultTrendPointDTO> minuteTrend;

    /** 积压估算：生产数 − 已处理数（非 broker 精确就绪深度） */
    private Long pendingCount;

    /** 快照生成时间 */
    private LocalDateTime snapshotTime;
}
