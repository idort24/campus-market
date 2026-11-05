package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类树形表（邻接表）
 */
@Data
@TableName("goods_category")
public class GoodsCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类名称 */
    private String categoryName;
    /** 父分类ID，0代表根节点 */
    private Long parentId;
    /** 排序 */
    private Integer sort;
    /** 扩展字段：分类图标、描述等 */
    private String extJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    private Integer isDeleted;
}
