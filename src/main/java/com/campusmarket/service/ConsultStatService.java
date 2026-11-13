package com.campusmarket.service;

import com.campusmarket.dto.ConsultSnapshotDTO;

public interface ConsultStatService {

    /**
     * 生成当前咨询统计快照
     */
    ConsultSnapshotDTO snapshot();
}
