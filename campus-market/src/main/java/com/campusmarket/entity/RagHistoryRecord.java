package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rag_history_record")
public class RagHistoryRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsCategoryId;  // 关联商品分类ID goods_category_id
    private String goodsDesc;      // 物品简短描述 goods_desc
    private String quality;        // 成色
    private BigDecimal dealPrice;  // 历史成交价格 deal_price
    private LocalDateTime dealTime; // 成交时间 deal_time
    private String sampleSource;   // 数据来源：IMPORT导入/SYSTEM系统 sample_source
    private Integer isValid;       // 1参与检索，0无效样本过滤 is_valid
    private String extJson;        // 附加元数据，用于向量检索过滤条件 ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;     // 逻辑删除 is_deleted
}
