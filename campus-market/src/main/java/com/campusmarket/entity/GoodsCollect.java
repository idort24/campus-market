package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品收藏表
 */
@Data
@TableName("goods_collect")
public class GoodsCollect {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 收藏用户 id */
    private Long userId;
    /** 商品 id */
    private Long goodsId;
    private LocalDateTime createTime;
    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    private Integer isDeleted;
}
