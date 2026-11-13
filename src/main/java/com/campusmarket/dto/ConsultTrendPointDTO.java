package com.campusmarket.dto;

import lombok.Data;

/**
 * 分钟趋势数据点：某一分钟内的事件数
 */
@Data
public class ConsultTrendPointDTO {

    /** 分钟（yyyy-MM-dd HH:mm） */
    private String minute;

    /** 该分钟事件数 */
    private Long count;

    public ConsultTrendPointDTO(String minute, Long count) {
        this.minute = minute;
        this.count = count;
    }
}
