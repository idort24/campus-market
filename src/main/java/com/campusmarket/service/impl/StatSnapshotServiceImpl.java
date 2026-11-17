package com.campusmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.entity.StatSnapshot;
import com.campusmarket.mapper.StatSnapshotMapper;
import com.campusmarket.service.StatSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class StatSnapshotServiceImpl extends ServiceImpl<StatSnapshotMapper, StatSnapshot>
        implements StatSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(StatSnapshotServiceImpl.class);

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
        try {
            return lambdaQuery().orderByAsc(StatSnapshot::getStatKey).list();
        } catch (Exception e) {
            // 快照表缺失或查询异常时返回空集合，大屏侧得到全零快照而不是 500；表结构问题由重建接口/初始化脚本修复
            log.warn("[快照] 读取 stat_snapshot 失败，按空快照处理: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
