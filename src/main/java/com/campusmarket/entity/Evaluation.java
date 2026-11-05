package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;          // 关联订单id order_id
    private Long fromUserId;       // 评价人ID from_user_id
    private Long toUserId;         // 被评价人ID to_user_id
    private Integer score;         // 星级1~5
    private String evalContent;    // 评价文字 eval_content
    private String tag;            // 交易标签，逗号分隔
    private Integer isValid;       // 1有效，0管理员标记恶意评价无效 is_valid
    private String extJson;        // 扩展 JSON ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;     // 逻辑删除 is_deleted
}
