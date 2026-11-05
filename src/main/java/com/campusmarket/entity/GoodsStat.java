package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品预聚合统计表（大数据查询优化）
 */
@Data
@TableName("goods_stat")
public class GoodsStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类 ID */
    private Long categoryId;
    /** 商品总数 */
    private Long totalCount;
    /** 审核通过数量 */
    private Long auditPassCount;
    /** 上架商品数 */
    private Long upCount;
    /** 已售出数量 */
    private Long soldCount;
    /** 统计快照时间 */
    private LocalDateTime statTime;
    /** 预留统计扩展指标（JSON 字符串） */
    private String extJson;
    private LocalDateTime createTime;
    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    private Integer isDeleted;
}
