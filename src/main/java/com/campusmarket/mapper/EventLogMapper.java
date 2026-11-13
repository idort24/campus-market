package com.campusmarket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.entity.EventLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 事件明细 Mapper：基础 CRUD + 快照聚合查询
 */
@Mapper
public interface EventLogMapper extends BaseMapper<EventLog> {

    /** 总量 / 版本号 / 会话去重数 一次性聚合 */
    @Select("SELECT COUNT(*) AS total, COALESCE(MAX(id), 0) AS version, " +
            "COUNT(DISTINCT session_id) AS sessionCount FROM mq_demo.event_log")
    Map<String, Object> aggregateOverview();

    /** 按事件类型分组统计 */
    @Select("SELECT event_type AS statKey, COUNT(*) AS statValue " +
            "FROM mq_demo.event_log GROUP BY event_type")
    List<Map<String, Object>> aggregateByType();

    /** 按分钟分组统计趋势，分钟键升序 */
    @Select("SELECT DATE_FORMAT(event_time, '%Y-%m-%d %H:%i') AS statKey, COUNT(*) AS statValue " +
            "FROM mq_demo.event_log GROUP BY DATE_FORMAT(event_time, '%Y-%m-%d %H:%i') " +
            "ORDER BY statKey ASC")
    List<Map<String, Object>> aggregateByMinute();
}
