package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据表
 */
@Data
@TableName("dict_data")
public class DictData {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联字典类型编码 */
    private String dictType;
    /** 显示文本 */
    private String dictLabel;
    /** 存储数值 */
    private String dictValue;
    /** 排序 */
    private Integer sort;
    /** 扩展JSON字段（预留） */
    private String extJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    private Integer isDeleted;
}
