package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.StatSnapshot;
import com.campusmarket.mapper.StatSnapshotMapper;
import com.campusmarket.service.StatSnapshotService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatSnapshotServiceImpl extends ServiceImpl<StatSnapshotMapper, StatSnapshot>
        implements StatSnapshotService {

    @Override
    public void upsert(String statKey, Long statValue, Long version) {
        StatSnapshot existing = lambdaQuery().eq(StatSnapshot::getStatKey, statKey).one();
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            StatSnapshot snapshot = new StatSnapshot();
            snapshot.setStatKey(statKey);
            snapshot.setStatValue(statValue);
            snapshot.setVersion(version);
            snapshot.setSnapshotTime(now);
            save(snapshot);
        } else {
            existing.setStatValue(statValue);
            existing.setVersion(version);
            existing.setSnapshotTime(now);
            updateById(existing);
        }
    }

    @Override
    public List<StatSnapshot> listAll() {
        return lambdaQuery().orderByAsc(StatSnapshot::getStatKey).list();
    }
}
