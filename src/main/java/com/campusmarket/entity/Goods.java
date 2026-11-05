package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;           // 卖家用户ID seller_id，关联 sys_user.id
    private Long categoryId;         // 商品分类ID category_id，关联 goods_category.id
    private String title;            // 商品标题 title
    private String description;      // 商品描述 description
    private BigDecimal price;        // 售价 price，decimal(10,2)
    private String quality;          // 成色 quality，字典取值
    private String images;           // 图片OSS地址数组 images，json ["url1","url2"]
    private String tradePlace;       // 线下交易地点 trade_place
    private Integer canBargain;      // 是否可议价 can_bargain，0否 1是
    private String auditStatus;      // 审核状态 audit_status，待审核/审核通过/审核驳回
    private String goodsStatus;      // 商品状态 goods_status，上架/下架/已售出
    private Integer aiGenerateFlag;  // 是否AI生成文案 ai_generate_flag，0手动 1AI
    private String auditRemark;      // 审核驳回理由 audit_remark
    private LocalDateTime auditTime; // 审核时间 audit_time
    private LocalDateTime sellTime;  // 售出时间 sell_time
    private String extJson;          // 扩展字段 ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;       // 逻辑删除 is_deleted
}
