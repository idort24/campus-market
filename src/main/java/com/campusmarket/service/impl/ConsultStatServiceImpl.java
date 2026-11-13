package com.campusmarket.service.impl;

import com.campusmarket.dto.ConsultSnapshotDTO;
import com.campusmarket.dto.ConsultTrendPointDTO;
import com.campusmarket.entity.ConsultEvent;
import com.campusmarket.mq.ConsultCounterHolder;
import com.campusmarket.service.ConsultEventService;
import com.campusmarket.service.ConsultStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConsultStatServiceImpl implements ConsultStatService {

    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private ConsultEventService eventService;

    @Autowired
    private ConsultCounterHolder counterHolder;

    @Override
    public ConsultSnapshotDTO snapshot() {
        List<ConsultEvent> all = eventService.list();

        long total = all.size();
        long sessionCount = all.stream()
                .map(ConsultEvent::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Long> topicDistribution = all.stream()
                .collect(Collectors.groupingBy(ConsultEvent::getTopic, Collectors.counting()));

        Map<String, Long> byMinute = all.stream()
                .filter(e -> e.getOccurredAt() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getOccurredAt().format(MINUTE_FORMAT),
                        Collectors.counting()));

        List<ConsultTrendPointDTO> minuteTrend = byMinute.entrySet().stream()
                .map(e -> new ConsultTrendPointDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(ConsultTrendPointDTO::getMinute))
                .toList();

        Long version = all.stream()
                .mapToLong(ConsultEvent::getId)
                .max()
                .orElse(0L);

        long pendingCount = Math.max(0, counterHolder.getProduced() - total);

        ConsultSnapshotDTO snapshot = new ConsultSnapshotDTO();
        snapshot.setVersion(version);
        snapshot.setTotalEvents(total);
        snapshot.setSessionCount(sessionCount);
        snapshot.setTopicDistribution(topicDistribution);
        snapshot.setMinuteTrend(minuteTrend);
        snapshot.setPendingCount(pendingCount);
        snapshot.setSnapshotTime(LocalDateTime.now());
        return snapshot;
    }
}
