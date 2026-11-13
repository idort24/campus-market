package com.campusmarket.service;

import com.campusmarket.dto.ConsultSnapshotDTO;

public interface ConsultStatService {

    /**
     * 聚合 event_log 明细，重算各项指标并写入 stat_snapshot。
     */
    void rebuildSnapshot();

    /**
     * 读取 stat_snapshot 表，组装大屏快照（大屏不直接查明细表）。
     */
    ConsultSnapshotDTO snapshot();
}
