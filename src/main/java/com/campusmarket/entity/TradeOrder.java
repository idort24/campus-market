package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long buyerId;          // 买家ID buyer_id
    private Long sellerId;         // 卖家ID seller_id
    private Long goodsId;          // 关联商品ID goods_id
    private String tradePlace;     // 约定线下交易地点 trade_place
    private String orderStatus;    // 待确认/待交易/已完成/已取消，字典 order_status
    private String orderNote;      // 订单备注 order_note
    private LocalDateTime confirmTime; // 卖家确认时间 confirm_time
    private LocalDateTime finishTime;  // 交易完成时间 finish_time
    private LocalDateTime cancelTime;  // 取消时间 cancel_time
    private String extJson;        // 订单扩展属性 ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;     // 逻辑删除 is_deleted
}
