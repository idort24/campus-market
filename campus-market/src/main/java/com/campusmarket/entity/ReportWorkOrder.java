package com.campusmarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report_work_order")
public class ReportWorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterUserId;   // 举报人ID reporter_user_id
    private String targetType;     // 举报对象类型 USER用户 / GOODS商品 target_type
    private Long targetId;         // 被举报对象ID target_id
    private String reportReason;   // 举报理由 report_reason
    private String auditResult;    // 处理结果 audit_result
    private String adminRemark;    // 管理员处理备注 admin_remark
    private String status;         // 待处理 / 已处置 / 驳回，字典
    private LocalDateTime dealTime; // 处理时间 deal_time
    private String extJson;        // 扩展 JSON ext_json
    private LocalDateTime createTime; // 创建时间 create_time
    private LocalDateTime updateTime; // 更新时间 update_time

    @TableLogic
    private Integer isDeleted;     // 逻辑删除 is_deleted
}
