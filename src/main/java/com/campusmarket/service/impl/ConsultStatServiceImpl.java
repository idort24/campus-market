package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusmarket.dto.ConsultSnapshotDTO;
import com.campusmarket.dto.ConsultTrendPointDTO;
import com.campusmarket.entity.StatSnapshot;
import com.campusmarket.mapper.EventLogMapper;
import com.campusmarket.service.ConsultStatService;
import com.campusmarket.service.MqCounterService;
import com.campusmarket.service.StatSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConsultStatServiceImpl implements ConsultStatService {

    @Autowired
    private EventLogMapper eventLogMapper;

    @Autowired
    private StatSnapshotService snapshotService;

    @Autowired
    private MqCounterService counterService;

    @Override
    public void rebuildSnapshot() {
        Map<String, Object> overview = eventLogMapper.aggregateOverview();
        long total = toLong(overview.get("total"));
        long version = toLong(overview.get("version"));
        long sessionCount = toLong(overview.get("sessionCount"));

        // 清掉上一轮的类型 / 分钟趋势键，避免旧维度残留
        snapshotService.remove(new LambdaQueryWrapper<StatSnapshot>()
                .likeRight(StatSnapshot::getStatKey, "type:"));
        snapshotService.remove(new LambdaQueryWrapper<StatSnapshot>()
                .likeRight(StatSnapshot::getStatKey, "trend:"));

        // 类型分布
        for (Map<String, Object> row : eventLogMapper.aggregateByType()) {
            String type = String.valueOf(row.get("statKey"));
            snapshotService.upsert("type:" + type, toLong(row.get("statValue")), version);
        }

        // 分钟趋势
        for (Map<String, Object> row : eventLogMapper.aggregateByMinute()) {
            String minute = String.valueOf(row.get("statKey"));
            snapshotService.upsert("trend:" + minute, toLong(row.get("statValue")), version);
        }

        long produced = counterService.getProduced();
        long backlog = Math.max(0, produced - total);

        snapshotService.upsert("version", version, version);
        snapshotService.upsert("total", total, version);
        snapshotService.upsert("session", sessionCount, version);
        snapshotService.upsert("backlog", backlog, version);
    }

    @Override
    public ConsultSnapshotDTO snapshot() {
        List<StatSnapshot> rows = snapshotService.listAll();
        Map<String, StatSnapshot> byKey = rows.stream()
                .collect(Collectors.toMap(StatSnapshot::getStatKey, x -> x, (a, b) -> b));

        Map<String, Long> typeDistribution = new LinkedHashMap<>();
        byKey.forEach((key, value) -> {
            if (key.startsWith("type:")) {
                typeDistribution.put(key.substring(5), value.getStatValue());
            }
        });

        List<ConsultTrendPointDTO> minuteTrend = byKey.entrySet().stream()
                .filter(e -> e.getKey().startsWith("trend:"))
                .map(e -> new ConsultTrendPointDTO(e.getKey().substring(6), e.getValue().getStatValue()))
                .sorted(Comparator.comparing(ConsultTrendPointDTO::getMinute))
                .toList();

        LocalDateTime snapshotTime = rows.stream()
                .map(StatSnapshot::getSnapshotTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now());

        ConsultSnapshotDTO snapshot = new ConsultSnapshotDTO();
        snapshot.setVersion(valueOf(byKey, "version"));
        snapshot.setTotalEvents(valueOf(byKey, "total"));
        snapshot.setSessionCount(valueOf(byKey, "session"));
        snapshot.setTypeDistribution(typeDistribution);
        snapshot.setMinuteTrend(minuteTrend);
        snapshot.setBacklogCount(valueOf(byKey, "backlog"));
        snapshot.setSnapshotTime(snapshotTime);
        return snapshot;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString().trim());
    }

    private long valueOf(Map<String, StatSnapshot> byKey, String key) {
        StatSnapshot snapshot = byKey.get(key);
        return snapshot == null || snapshot.getStatValue() == null ? 0L : snapshot.getStatValue();
    }
}
