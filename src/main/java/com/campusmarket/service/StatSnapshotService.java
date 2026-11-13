package com.campusmarket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.entity.StatSnapshot;

import java.util.List;

public interface StatSnapshotService extends IService<StatSnapshot> {

    /** 按 stat_key 写入或覆盖一条快照指标 */
    void upsert(String statKey, Long statValue, Long version);

    /** 读取全部快照指标（大屏只读快照表） */
    List<StatSnapshot> listAll();
}
